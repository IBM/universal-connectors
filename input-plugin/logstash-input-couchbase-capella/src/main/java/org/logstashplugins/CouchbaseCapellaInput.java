package org.logstashplugins;

import co.elastic.logstash.api.*;
import com.ibm.guardium.couchbase.capella.CapellaAuditLogApiManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import com.google.common.io.ByteSource;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;



// class name must match plugin name
@LogstashPlugin(name="couchbase_capella_input")
public class CouchbaseCapellaInput implements Input {

    private static final Logger log = LogManager.getLogger(CouchbaseCapellaInput.class);
    private static final int HALF_A_HOUR = 30 * 60;
    private static final int FIFTEEN_MINS = 15 * 60;


    public static final String LOG42_CONF="log4j2uc.properties";
    static {
        try {
            String uc_etc = System.getenv("UC_ETC");
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            File file = new File(uc_etc +File.separator+LOG42_CONF);
            context.setConfigLocation(file.toURI());
        } catch (Exception e){
            System.err.println("Failed to load log4j configuration "+e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * INTERVAL_CONFIG is in second, it is the pulling period of the input plugin. If the result processing time
     * is greater than this value, the program will not wait and immediately do next pull.
     */
    public static final PluginConfigSpec<Long> INTERVAL_CONFIG =
            PluginConfigSpec.numSetting(Constants.PLUGIN_CONFIG_KEY_INTERVAL, Constants.DEFAULT_QUERY_INTERVAL);
    /**
     * QUERY_LENGTH_CONFIG the query time length in second. For example, given
     * QUERY_INTERVAL is 10s, QUERY_LENGTH is 1 minute (60s), and the process start at 1:30:00,
     * the query is performed like this:
     * At 1:30:00, we query 00:59:00 to 1:00:00, notice capella require to query at least 15 minutes ago,
     * so for the first query we deduct 30 minutes.
     * At 1:30:10, we query 1:00:00 to 1:01:00.
     * At 1:30:20, we query 1:01:00 to 1:02:00.
     */
    public static final PluginConfigSpec<Long> QUERY_LENGTH_CONFIG =
            PluginConfigSpec.numSetting(Constants.PLUGIN_CONFIG_KEY_QUERY_LENGTH, Constants.DEFAULT_QUERY_LENGTH);

    public static final PluginConfigSpec<String> TYPE_CONFIG =
            PluginConfigSpec.stringSetting(Constants.PLUGIN_CONFIG_KEY_TYPE, Constants.PLUGIN_TYPE);
    public static final PluginConfigSpec<String> API_BASE_URL_CONFIG =
            PluginConfigSpec.stringSetting(Constants.PLUGIN_CONFIG_KEY_API_URL, Constants.V4_API);
    public static final PluginConfigSpec<String> ORG_ID_CONFIG =
            PluginConfigSpec.stringSetting(Constants.PLUGIN_CONFIG_KEY_ORG);
    public static final PluginConfigSpec<String> PROJECT_ID_CONFIG =
            PluginConfigSpec.stringSetting(Constants.PLUGIN_CONFIG_KEY_PROJ);
    public static final PluginConfigSpec<String> CLUSTER_ID_CONFIG =
            PluginConfigSpec.stringSetting(Constants.PLUGIN_CONFIG_KEY_CLUSTER);
    public static final PluginConfigSpec<String> AUTH_TOKEN_CONFIG =
            PluginConfigSpec.stringSetting(Constants.PLUGIN_CONFIG_KEY_AUTH);

    protected String pluginType;
    protected String id;
    protected String apiBaseURL;
    protected String organizationID;
    protected String projectID;
    protected String clusterID;
    protected String authToken;

    /**
     * See INTERVAL_CONFIG
     */
    protected long interval;
    /**
     * See QUERY_LENGTH_CONFIG
     */
    protected long queryLength;
    protected long nextPull;
    protected long lastQueryEndEpoch;
    private final CountDownLatch done = new CountDownLatch(1);
    private volatile boolean stopped;
    private class queryPara {
        long start;
        long end;
    }

    /**
     * CouchbaseCapellaInput implements an input plugin that pulls Couchbase Capella DB's
     * Audit logs into logstash pipeline. As per requested by logstash input plugin,
     * all plugins must provide a constructor that accepts id, Configuration, and Context
     * @param id, a string based id for the plugin.
     * @param config, input plugin configuration.
     * @param context, input plugin running context.
     */
    //
    public CouchbaseCapellaInput(String id, Configuration config, Context context) {

        this.id = id;
        interval = config.get(INTERVAL_CONFIG);
        queryLength = config.get(QUERY_LENGTH_CONFIG);
        pluginType = config.get(TYPE_CONFIG);
        apiBaseURL = config.get(API_BASE_URL_CONFIG);
        if (apiBaseURL == null || apiBaseURL.isEmpty()) {
            throw new IllegalArgumentException("API Base URL ID is empty");
        }
        organizationID = config.get(ORG_ID_CONFIG);
        if (organizationID == null || organizationID.isEmpty()) {
            throw new IllegalArgumentException("Organization ID is empty");
        }
        projectID = config.get(PROJECT_ID_CONFIG);
        if (projectID == null || projectID.isEmpty()) {
            throw new IllegalArgumentException("Project ID is empty");
        }
        clusterID = config.get(CLUSTER_ID_CONFIG);
        if (clusterID == null || clusterID.isEmpty()) {
            throw new IllegalArgumentException("Cluster ID is empty");
        }
        authToken = config.get(AUTH_TOKEN_CONFIG);
        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalArgumentException("Organization ID is empty");
        }

    }

    /**
     * start is then entrance of the plugin pulling logic.
     * After every interval we query queryLength.
     * @param consumer is the sink of the data.
     * Capella support date format as YYYY-MM-DDTHH:MM:SSUTC
     */
    @Override
    public void start(Consumer<Map<String, Object>> consumer) {

        long now = System.currentTimeMillis() / 1000L;

        // Capella API requires the start querying time has to be at least 15 minutes before NOW.
        long queryEndTimeEpoch = now - HALF_A_HOUR;
        long queryStartTimeEpoch = queryEndTimeEpoch - this.queryLength;

        this.nextPull = now + this.interval;
        CapellaAuditLogApiManager mgr = new CapellaAuditLogApiManager();
        queryPara queryRange = new queryPara();
        queryRange.end = queryEndTimeEpoch;
        queryRange.start = queryStartTimeEpoch;


        try {
            while (!stopped) {

                String queryEndTime = epochSecToISO8601DateTimeString(queryRange.end);
                String queryStartTime = epochSecToISO8601DateTimeString(queryRange.start);

                log.info("Capella input query from {} to {}", queryStartTime, queryEndTime);

                String jobID = mgr.createExportJob(apiBaseURL, organizationID, projectID, clusterID, authToken,
                        queryStartTime, queryEndTime);
                if (jobID == null || jobID.isBlank()){
                    try {
                        log.debug("capella input plugin: no audit log export job ID");
                        sleepTillNexPull();
                    } catch (InterruptedException e) {
                        log.error("interrupted", e);
                        break;
                    }
                    queryPara newRange =  calculateQueryPara(queryRange.start, queryRange.end);
                    queryRange.start = newRange.start;
                    queryRange.end = newRange.end;
                    continue;
                }
                String downloadURL = mgr.getExportJobStatus(apiBaseURL, organizationID, projectID, clusterID, authToken,jobID);
                if (downloadURL == null || downloadURL.isBlank()){
                    try {
                        log.debug("capella input plugin: empty audit log download URL");
                        sleepTillNexPull();
                    } catch (InterruptedException e) {
                        log.error("interrupted", e);
                        break;
                    }
                    queryPara newRange =  calculateQueryPara(queryRange.start, queryRange.end);
                    queryRange.start = newRange.start;
                    queryRange.end = newRange.end;
                    continue;
                }

                ByteSource byteSource = mgr.downloadAuditLogFile(downloadURL);
                if (byteSource == null) {
                    try {
                        log.debug("capella input plugin: empty audit log");
                        sleepTillNexPull();
                    } catch (InterruptedException e) {
                        log.error("interrupted", e);
                        break;
                    }
                    queryPara newRange =  calculateQueryPara(queryRange.start, queryRange.end);
                    queryRange.start = newRange.start;
                    queryRange.end = newRange.end;
                    continue;
                }

                // Open an InputStream and wrap it with BufferedReader to read line by line
                try (InputStream in = byteSource.openStream(); BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in, StandardCharsets.UTF_8))) {

                    String originalLine;
                    while ((originalLine = reader.readLine()) != null) {
                        String line = refineLine(originalLine);
                        if (line == null) {
                            log.debug("invalid json exception: {}", originalLine);
                            continue;
                        }
                        // Drop system generated logs
                        if (line.contains("\"user\":\"@")){
                            log.debug("Drop logs: {}", line);
                            continue;
                        }
                        
                        HashMap map = new HashMap();
                        map.put(Constants.OUTPUT_KEY_MESSAGE, line);
                        map.put(Constants.OUTPUT_KEY_ORG, organizationID);
                        map.put(Constants.OUTPUT_KEY_PROJ, projectID);
                        map.put(Constants.OUTPUT_KEY_CLUSTER, clusterID);
                        map.put(Constants.OUTPUT_KEY_TYPE,pluginType);
                        log.debug("capella input plugin: input message is {}", line);

                        consumer.accept(map);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try {
                    sleepTillNexPull();
                } catch (InterruptedException e) {
                    log.error("interrupted", e);
                    break;
                }

                queryPara newRange =  calculateQueryPara(queryRange.start, queryRange.end);
                queryRange.start = newRange.start;
                queryRange.end = newRange.end;
                if (queryEndTimeEpoch - queryStartTimeEpoch <= FIFTEEN_MINS) {
                    try {
                        log.debug("Waiting for interval range lager than 15 mins");
                        TimeUnit.SECONDS.sleep(FIFTEEN_MINS);
                    } catch (InterruptedException e) {
                        log.error("interrupted", e);
                        break;
                    }

                }
            }
        } finally {
            stopped = true;
            log.info("capella input plugin stopped");
            done.countDown();
        }


    }

    @Override
    public void stop() {
        stopped = true; // set flag to request cooperative stop of input
    }

    @Override
    public void awaitStop() throws InterruptedException {
        done.await(); // blocks until input has stopped
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return List.of(INTERVAL_CONFIG, QUERY_LENGTH_CONFIG, TYPE_CONFIG, API_BASE_URL_CONFIG,
                ORG_ID_CONFIG, PROJECT_ID_CONFIG, CLUSTER_ID_CONFIG, AUTH_TOKEN_CONFIG);
    }

    @Override
    public String getId() {
        return this.id;
    }


    /**
     *  sleepTillNexPull compares current time and next pull time. If next pull time is later than current time, sleep
     *  till next pull time; If next pull time is earlier than current time (which means we already passed next pull time)
     *  just pull without sleep.
     * @throws InterruptedException
     */
    private void sleepTillNexPull() throws InterruptedException {
        long now = System.currentTimeMillis() / 1000L;
        long timeToSleepInSec = this.nextPull - now;
        if (timeToSleepInSec > 0) {
            log.info("capella input plugin sleep for {} seconds", timeToSleepInSec);
            TimeUnit.SECONDS.sleep(timeToSleepInSec);
            this.nextPull = this.nextPull + this.interval;
        } else {
            log.info("next pull time is already passed, just pull without waiting");
            this.nextPull = now + this.interval;
        }
    }


    protected String epochSecToISO8601DateTimeString(long epoch) {
        Instant instant = Instant.ofEpochSecond(epoch);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
        return localDateTime.atOffset(ZoneOffset.UTC).format(formatter);
    }

    /**
     * In this method we ignored the padding at beginning of the log
     * @param jsonString
     * @return
     */
    private String refineLine(String jsonString) {

        jsonString =
                jsonString
                        .replace("\u0000", "") // Remove null characters
                        .replaceAll("\\p{C}", "") // Remove control characters
                        .replace("\u00A0", "") // Remove non-breaking spaces
                        .trim();
        if(jsonString.indexOf("{") > 0) {
            jsonString = jsonString.substring(jsonString.indexOf("{"));
        }
        if (jsonString.isBlank()||jsonString.contains(".json")
                || !(jsonString.startsWith("{") && jsonString.endsWith("}"))
                || jsonString.matches("\\{\\s*}")) {
            // rule out string not start { and end with }
            // rule out empty json e.g. { }
            return null;
        }
        try {
            JsonParser.parseString(jsonString);
            return jsonString;
        } catch (JsonSyntaxException e) {
            log.error("invalid json exception: {}", jsonString);
        }
        return null;
    }

    private queryPara calculateQueryPara(long currentStart, long currentEnd) {
        // recalculate query start time and end time
        long queryStartTimeEpoch = currentEnd;
        long queryEndTimeEpoch = currentEnd + this.queryLength;

        // Adjust query end time edge cases
        long now = System.currentTimeMillis() / 1000L;
        if (queryEndTimeEpoch >= now) {
            queryEndTimeEpoch = now;
        }
        if (queryEndTimeEpoch <= queryStartTimeEpoch) {
            queryEndTimeEpoch = now - HALF_A_HOUR;
            queryStartTimeEpoch = queryEndTimeEpoch - HALF_A_HOUR;
        }

        queryPara res = new queryPara();
        res.start = queryStartTimeEpoch;
        res.end = queryEndTimeEpoch;
        return res;
    }
}
