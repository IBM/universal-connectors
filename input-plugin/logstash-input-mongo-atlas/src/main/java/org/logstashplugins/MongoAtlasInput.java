package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Input;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;
import org.apache.commons.lang3.StringUtils;
import com.ibm.guardium.mongodb.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import java.io.File;
//public class MongoAtlasInput{
//
//}
// class name must match plugin name
@LogstashPlugin(name="mongo_atlas_input")
public class MongoAtlasInput implements Input {
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
    private static Logger log = LogManager.getLogger(MongoAtlasInput.class);

    public static final PluginConfigSpec<Long> INTERVAL_CONFIG =
            PluginConfigSpec.numSetting("interval", 300);

    public static final PluginConfigSpec<String> PUBLIC_KEY_CONFIG =
            PluginConfigSpec.stringSetting("public-key", "message");
    public static final PluginConfigSpec<String> TYPE_CONFIG =
            PluginConfigSpec.stringSetting("type", "mongodb");
    public static final PluginConfigSpec<String> PRIVATE_KEY_CONFIG =
            PluginConfigSpec.stringSetting("private-key", "message");
    public static final PluginConfigSpec<String> GROUP_ID_CONFIG =
            PluginConfigSpec.stringSetting("group-id", "message");
    public static final PluginConfigSpec<String> HOSTNAME_CONFIG =
            PluginConfigSpec.stringSetting("hostname", "message");
    public static final PluginConfigSpec<String> FILE_NAME_CONFIG =
            PluginConfigSpec.stringSetting("filename", "mongodb-audit-log.gz");
    private String id;
    private long interval;
    private String privateKey;
    private String publicKey;
    private String groupId;
    private String hostname;
    private String fileName;
    private String pluginType;
    private final CountDownLatch done = new CountDownLatch(1);
    private volatile boolean stopped;

    // all plugins must provide a constructor that accepts id, Configuration, and Context
    public MongoAtlasInput(String id, Configuration config, Context context) {
        // constructors should validate configuration options
        this.id = id;
        interval = config.get(INTERVAL_CONFIG);
        privateKey = config.get(PRIVATE_KEY_CONFIG);
        publicKey = config.get(PUBLIC_KEY_CONFIG);
        groupId = config.get(GROUP_ID_CONFIG);
        hostname = config.get(HOSTNAME_CONFIG);
        fileName = config.get(FILE_NAME_CONFIG);
        pluginType = config.get(TYPE_CONFIG);
    }

    @Override
    public void start(Consumer<Map<String, Object>> consumer) {

        // The start method should push Map<String, Object> instances to the supplied QueueWriter
        // instance. Those will be converted to Event instances later in the Logstash event
        // processing pipeline.
        //
        // Inputs that operate on unbounded streams of data or that poll indefinitely for new
        // events should loop indefinitely until they receive a stop request. Inputs that produce
        // a finite sequence of events should loop until that sequence is exhausted or until they
        // receive a stop request, whichever comes first.
        int eventCount = 0;
        long loopStartTime = System.currentTimeMillis() / 1000L;
        long lasttime = loopStartTime - this.interval;
        try {
            while (!stopped) {
                    loopStartTime = System.currentTimeMillis() / 1000L;
                    String allText = MongoApi.getResponseFromJsonURL(this.publicKey, this.privateKey,this.groupId,this.hostname,this.fileName, lasttime, loopStartTime);
                    if (allText != null) {
                        lasttime = loopStartTime;
                        String lines[] = allText.split("\\r?\\n");
                        for (String line : lines
                        ) {
                            HashMap map = new HashMap();
                            map.put("message", line);
                            map.put("hostname", hostname);
                            map.put("groupId", groupId);
                            map.put("type", pluginType);
                            if (log.isDebugEnabled()) {
                                log.debug(line);
                            }
                            consumer.accept(map);
                        }
                    }
                    long curTimeEpochInSec = System.currentTimeMillis() / 1000L;
                    long timeToSleepInSec = loopStartTime + this.interval - curTimeEpochInSec;
                    if (timeToSleepInSec >= 0) {
                        try {
                            Thread.sleep(timeToSleepInSec * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

            }
        } finally {
            stopped = true;
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
        // should return a list of all configuration options for this plugin
        return Arrays.asList(INTERVAL_CONFIG,PUBLIC_KEY_CONFIG,PRIVATE_KEY_CONFIG,GROUP_ID_CONFIG,HOSTNAME_CONFIG,TYPE_CONFIG);
    }

    @Override
    public String getId() {
        return this.id;
    }
}
