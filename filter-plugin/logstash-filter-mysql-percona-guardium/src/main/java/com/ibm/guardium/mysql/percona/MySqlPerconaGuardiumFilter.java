package com.ibm.guardium.mysql.percona;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import com.google.gson.*;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.io.File;
import java.time.ZonedDateTime;

import com.ibm.guardium.universalconnector.commons.structures.*;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;

// class name must match plugin name
@LogstashPlugin(name = "mysql_percona_guardium_filter")
public class MySqlPerconaGuardiumFilter implements Filter {

    public static final PluginConfigSpec<String> LOG_LEVEL_CONFIG = PluginConfigSpec.stringSetting("log_level", null);

    public static final String LOG42_CONF="log4j2uc.properties";
    public static final String LOGSTASH_TAG_MYSQL_PARSE_ERROR = "_mysqlguardium_parse_error";
    public static final String LOGSTASH_TAG_MYSQL_IGNORE = "_mysqlguardium_ignore";

    private static final String MYSQL_AUDIT_START_SIGNAL = "percona-audit: ";
    public static final String DATA_PROTOCOL_STRING = "MySQL Percona audit";
    public static final String UNKNOWN_STRING = "";
    public static final String SERVER_TYPE_STRING = "MySql";

    private static Logger log = LogManager.getLogger(MySqlPerconaGuardiumFilter.class);

    private final static Set<String> LOCAL_IP_LIST = new HashSet<>(Arrays.asList("127.0.0.1", "0:0:0:0:0:0:0:1", "::1"));

    private static final DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
                                                                                .append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss z"));
    private static final DateTimeFormatter DATE_TIME_FORMATTER = dateTimeFormatterBuilder.toFormatter();

    private static final GsonBuilder builder = new GsonBuilder().serializeNulls();

    private String id;
//    private String logLevel;

    static {
        try {
            String uc_etc = System.getenv("UC_ETC")!=null ? System.getenv("UC_ETC") : "";
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            File file = new File(uc_etc + File.separator + LOG42_CONF);
            context.setConfigLocation(file.toURI());
        } catch (Exception e){
            System.err.println("Failed to load log4j configuration "+e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return Collections.singletonList(LOG_LEVEL_CONFIG);
    }

    public MySqlPerconaGuardiumFilter(String id, Configuration config, Context context) {
        this.id = id;
//        this.logLevel = config.get(LOG_LEVEL_CONFIG);
//        if (logLevel!=null) {
//            System.out.println("Setting log level of MySqlPerconaGuardiumFilter to "+logLevel);
//            Configurator.setLevel(LogManager.getLogger(MySqlPerconaGuardiumFilter.class).getName(), Level.valueOf(logLevel));
//        }
    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
        for (Event e : events) {

            if (log.isDebugEnabled()) {
               log.debug("MySql Percona filter: Got Event: " + logEvent(e));
            }

            String messageString = e.getField("message").toString();

            int mysqlIndex = messageString.indexOf(MYSQL_AUDIT_START_SIGNAL);

            if (mysqlIndex != -1) {
                
                String mysqlMsgString = messageString.substring(mysqlIndex + MYSQL_AUDIT_START_SIGNAL.length());
                int msgStrLen = mysqlMsgString.length();

                 // Remove last comma to get proper json string
                if (mysqlMsgString.charAt(msgStrLen-1) == ',')
                {
                    // remove last character (,)
                    mysqlMsgString = mysqlMsgString.substring(0, msgStrLen -1);
                }

                try {
                    JsonObject messageJson = (JsonObject) JsonParser.parseString(mysqlMsgString);
                    JsonObject audit_record = messageJson.get("audit_record").getAsJsonObject();
                    boolean validRecord = false;
                    Record record = new Record();

                    String status = getFieldAsString(audit_record, "status", "-1");
                    if ("0".equals(status)) {
                        String sql = getFieldAsString(audit_record, "sqltext", null);
                        if (sql != null && !sql.trim().isEmpty())
                        {
                            Data data = new Data();
                            data.setOriginalSqlCommand(sql);

                            record.setData(data);
                            validRecord = true;
                        }
                    } else {
                        ExceptionRecord exceptionRecord = new ExceptionRecord();
                        exceptionRecord.setExceptionTypeId(status);
                        exceptionRecord.setDescription(status);
                        exceptionRecord.setSqlString(getFieldAsString(audit_record, "sqltext", UNKNOWN_STRING));

                        record.setException(exceptionRecord);
                        validRecord = true;
                    }


                    if (validRecord) {

                        String sessionId = getFieldAsString(audit_record, "connection_id", "");
                        record.setSessionId(sessionId);

                        String dbName = getFieldAsString(audit_record,"db", UNKNOWN_STRING);
                        record.setDbName(dbName);

                        record.setAppUserName(UNKNOWN_STRING);

                        Time time = getTime(getFieldAsString(audit_record, "timestamp", null));
                        record.setTime(time);

                        record.setSessionLocator(parseSessionLocator(e, audit_record));
                        record.setAccessor(parseAccessor(e, audit_record));
                        // workaround for db name
                        record.getAccessor().setServiceName(dbName);

                        this.correctIPs(e, record);

                        e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, builder.create().toJson(record));
                    } // validRecord
                    else {
                        e.tag(LOGSTASH_TAG_MYSQL_IGNORE);
                    }

                    matchListener.filterMatched(e);

                } catch (Exception exception) {
                    //events.remove(e);
                    log.error("Error parsing mysql event " + logEvent(e), exception);
                    e.tag(LOGSTASH_TAG_MYSQL_PARSE_ERROR);
                }
            }
        } // for events
        return events;
    }


    private static String getFieldAsString(JsonObject jsonObject, String fieldName, String defaultValue) {
        if (jsonObject.get(fieldName) == null) {
            return defaultValue;
        }
        return jsonObject.get(fieldName).getAsString();
    }

    public static Time getTime(String dateString){
        if (dateString == null){
            log.warn("DateString is null");
            return new Time(0, 0, 0);
        }
        ZonedDateTime date = ZonedDateTime.parse(dateString, DATE_TIME_FORMATTER);
        long millis = date.toInstant().toEpochMilli();
        int  minOffset = date.getOffset().getTotalSeconds()/60;
        //int  minDst = date.getOffset().getRules().isDaylightSavings(date.toInstant()) ? 60 : 0;
        return new Time(millis, minOffset, 0);
    }

    private static SessionLocator parseSessionLocator(Event e, JsonObject audit_record) {
        SessionLocator sessionLocator = new SessionLocator();
        sessionLocator.setClientIp(UNKNOWN_STRING);
        sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
        sessionLocator.setClientIpv6(UNKNOWN_STRING);

        sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);
        String serverIp = e.getField("server_ip") instanceof String ? e.getField("server_ip").toString() : "0.0.0.0";
        if (Util.isIPv6(serverIp)){
            sessionLocator.setServerIpv6(serverIp);
            sessionLocator.setIpv6(true);
            sessionLocator.setServerIp(UNKNOWN_STRING);
        } else {
            sessionLocator.setServerIp(serverIp);
            sessionLocator.setIpv6(false);
            sessionLocator.setServerIpv6(UNKNOWN_STRING);
        }

        sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
        String clientIp = getFieldAsString(audit_record, "ip", UNKNOWN_STRING);
        if (Util.isIPv6(clientIp)) {
            sessionLocator.setIpv6(true);
            sessionLocator.setClientIpv6(clientIp);
            sessionLocator.setClientIp(UNKNOWN_STRING);
        } else { // ipv4
            sessionLocator.setIpv6(false);
            sessionLocator.setClientIp(clientIp);
            sessionLocator.setClientIpv6(UNKNOWN_STRING);
        }

        return sessionLocator;
    }

    public static Accessor parseAccessor(Event e, JsonObject audit_record) {
        Accessor accessor = new Accessor();
        accessor.setDbProtocol(DATA_PROTOCOL_STRING);
        accessor.setServerType(SERVER_TYPE_STRING);
        accessor.setLanguage("MYSQL");
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);

        accessor.setDbUser(processDbUser(getFieldAsString(audit_record, "user", UNKNOWN_STRING)));
        accessor.setOsUser(getFieldAsString(audit_record,"os_user", UNKNOWN_STRING));
        accessor.setClientHostName(getFieldAsString(audit_record, "host", UNKNOWN_STRING));

        if (e.getField("server_hostname") instanceof String) {
            accessor.setServerHostName(e.getField("server_hostname").toString());
        }

        return accessor;
    }

    static String processDbUser(String dbName) {
        if (!UNKNOWN_STRING.equals(dbName)) {
            // Example of dbname - "UserName1[UserName1] @ abc.com [1.2.3.4]"
            int indexOf = dbName.lastIndexOf('@');
            if (indexOf > 0) {
                dbName = dbName.substring(0, indexOf);
                indexOf = dbName.lastIndexOf('[');
                if (indexOf > 0) {
                    dbName = dbName.substring(0, indexOf);
                }
            }
        }
        return dbName.trim();
    }

    private static String logEvent(Event event){
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("{ ");
            boolean first = true;
            for (Map.Entry<String, Object> stringObjectEntry : event.getData().entrySet()) {
                if (!first){
                    sb.append(",");
                }
                sb.append("\""+stringObjectEntry.getKey()+"\" : \""+stringObjectEntry.getValue()+"\"");
                first = false;
            }
            sb.append(" }");
            return sb.toString();
        } catch (Exception e){
            log.error("Failed to create event log string", e);
            return null;
        }
    }
    
   private void correctIPs(Event e, Record record) {
        // Note: IP needs to be in ipv4/ipv6 format
        SessionLocator sessionLocator = record.getSessionLocator();
        
        if (sessionLocator == null)
           log.error("SessionLocator is NULL");
        
        String sessionServerIp;
        String sessionClientIp;

        if (sessionLocator.isIpv6())
        {
            sessionServerIp = sessionLocator.getServerIpv6();
            sessionClientIp = sessionLocator.getClientIpv6();
        }
        else
        {
            sessionServerIp = sessionLocator.getServerIp();
            sessionClientIp = sessionLocator.getClientIp();
        }

        if (LOCAL_IP_LIST.contains(sessionServerIp)
                || sessionServerIp.equals("")
                || sessionServerIp.equalsIgnoreCase("(NONE)")) {
            if (e.getField("server_ip") instanceof String) {
                String ip = e.getField("server_ip").toString();
                if (ip != null) {
                    if (Util.isIPv6(ip)){
                        sessionLocator.setServerIpv6(ip);
                        sessionLocator.setIpv6(true);
                    } else {
                        sessionLocator.setServerIp(ip);
                        sessionLocator.setIpv6(false);
                    }
                } else if (sessionServerIp.equalsIgnoreCase("(NONE)")) {
                    sessionLocator.setServerIp("0.0.0.0");
                }
            }
        }

        if (LOCAL_IP_LIST.contains(sessionClientIp)
            || sessionClientIp.equals("")
            || sessionClientIp.equalsIgnoreCase("(NONE)")) {
            // as clientIP & serverIP were equal
            if (sessionLocator.isIpv6()) {
                sessionLocator.setClientIpv6(sessionLocator.getServerIpv6());
                sessionLocator.setClientIp(UNKNOWN_STRING);
            }
            else {
                sessionLocator.setClientIp(sessionLocator.getServerIp());
                sessionLocator.setClientIpv6(UNKNOWN_STRING);
            }
        }
   }


    @Override
    public String getId() {
        return this.id;
    }
}
