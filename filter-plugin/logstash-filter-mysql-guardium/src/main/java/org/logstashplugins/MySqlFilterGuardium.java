package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import com.google.gson.*;

import java.util.*;
import java.io.File;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.ibm.guardium.universalconnector.commons.structures.*;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.GuardConstants;

// class name must match plugin name
@LogstashPlugin(name = "mysql_filter_guardium")
public class MySqlFilterGuardium implements Filter {

    public static final String LOG42_CONF="log4j2uc.properties";
    public static final String LOGSTASH_TAG_MYSQL_PARSE_ERROR = "_mysqlguardium_parse_error";
    public static final String LOGSTASH_TAG_MYSQL_IGNORE = "_mysqlguardium_ignore";
    
    public static final String EXCEPTION_TYPE_SQL_ERROR_STRING = "SQL_ERROR";
    public static final String EXCEPTION_TYPE_LOGIN_FAILED_STRING = "LOGIN_FAILED";
    
    private static final String QUERY_STRING = "Query";
    private static final String CONNECT_STRING = "Connect";
    
    private static final String CLASS_TYPE_GENERAL = "general";
    private static final String CLASS_TYPE_CONNECTION = "connection";
    private static final String CLASS_TYPE_AUDIT = "audit";
    private static final String CLASS_TYPE_TABLE_ACCESS = "table_access";
    private static final String DATA_TYPE_TABLE_ACCESS = "table_access_data";
    private static final String DATA_TYPE_GENERAL = "general_data";
    private static final String DATA_TYPE_CONNECTION = "connection_data";
    
    private static final String MYSQL_AUDIT_START_SIGNAL = "mysql_audit_log: "; 
    public static final String DATA_PROTOCOL_STRING = "MySQL native audit";
    public static final String UNKNOWN_STRING = "";
    public static final String SERVER_TYPE_STRING = "MySql";
    private static final String MASK_STRING = "?";

    private static final String DATE_FORMAT_ISO = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT_ISO);
    
    static {
        try {
            String uc_etc = System.getenv("UC_ETC");
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            File file = new File(uc_etc + File.separator + LOG42_CONF);
            context.setConfigLocation(file.toURI());
        } catch (Exception e){
            System.err.println("Failed to load log4j configuration "+e.getMessage());
            e.printStackTrace();
        }
    }

    private static Logger log = LogManager.getLogger(MySqlFilterGuardium.class);

    public static final PluginConfigSpec<String> SOURCE_CONFIG =
            PluginConfigSpec.stringSetting("source", "message");

    private String id;
    private String sourceField;
    private final static Set<String> LOCAL_IP_LIST = new HashSet<>(
        Arrays.asList("127.0.0.1", "0:0:0:0:0:0:0:1", "::1"));

    public MySqlFilterGuardium(String id, Configuration config, Context context) {
        // constructors should validate configuration options
        this.id = id;
        this.sourceField = config.get(SOURCE_CONFIG);
    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
        for (Event e : events) {

            if (log.isDebugEnabled()) {
               log.debug("MySql filter: Got Event: " + logEvent(e));
            }

            String messageString = e.getField("message").toString();

            //log.warn("MessageString  " + messageString);
            //log.warn("\n\n");

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
                    JsonObject inputJSON = (JsonObject) JsonParser.parseString(mysqlMsgString);
                    final String class_type = inputJSON.get("class").getAsString();
                    final String timestamp = inputJSON.get("timestamp").getAsString();
                    final int connection_id = inputJSON.get("connection_id").getAsInt();

                    if ( class_type.equals(CLASS_TYPE_CONNECTION) 
                        || class_type.equals(CLASS_TYPE_GENERAL)) {
                            
                        Record record = new Record();
                        boolean validRecord = false;

                        record.setDbName(UNKNOWN_STRING);
                        
                        if (inputJSON.has(DATA_TYPE_CONNECTION)) {
                            String eventField = inputJSON.get("event").getAsString();
                            if (eventField.equals("connect")) {
                                final JsonObject conn_data = inputJSON.get(DATA_TYPE_CONNECTION).getAsJsonObject();
                                final int status = conn_data.get("status").getAsInt();
                                final String dbName = conn_data.get("db").getAsString();
                                
                                record.setDbName(dbName);
                                
                                if (status != 0) {
                                    // https://dev.mysql.com/doc/refman/8.0/en/error-message-components.html                                
                                    ExceptionRecord exceptionRecord = new ExceptionRecord();
                                    record.setException(exceptionRecord);

                                    exceptionRecord.setExceptionTypeId(EXCEPTION_TYPE_LOGIN_FAILED_STRING);
                                    exceptionRecord.setDescription("Login Failed (" + status + ")"); 
                                    exceptionRecord.setSqlString(UNKNOWN_STRING);
                                    validRecord = true;
                                }
                            }
                        }                         
                        else if (inputJSON.has(DATA_TYPE_GENERAL)) {
                            final JsonObject gen_data = inputJSON.get(DATA_TYPE_GENERAL).getAsJsonObject();
                            final String command = gen_data.get("command").getAsString(); 
                            final int status = gen_data.get("status").getAsInt();
                        
                            if (command.equals(QUERY_STRING)) {
                            
                                final String query = gen_data.get("query").getAsString();
                                if (status != 0) {
                                    // https://dev.mysql.com/doc/refman/8.0/en/error-message-components.html                                
                                    ExceptionRecord exceptionRecord = new ExceptionRecord();
                                    record.setException(exceptionRecord);

                                    exceptionRecord.setExceptionTypeId(EXCEPTION_TYPE_SQL_ERROR_STRING);
                                    exceptionRecord.setDescription("Error (" + status + ")"); 
                                    exceptionRecord.setSqlString(query);
                                    validRecord = true;
                                }
                                else {
                                     Data data = new Data();
                                     record.setData(data);
                                     if (query != null)
                                     {
                                         data.setOriginalSqlCommand(query);
                                         validRecord = true;
                                     }
                                }
                            }

                        } // end general_data
                        if (validRecord) {
                            record.setSessionId(""+connection_id);
                            record.setAppUserName(UNKNOWN_STRING);
                                
                            Time unixTime = getTimestamp(timestamp);
                            record.setTime(unixTime);
                                
                            record.setSessionLocator(parseSessionLocator(e, inputJSON));
                            record.setAccessor(parseAccessor(e, inputJSON));

                            this.correctIPs(e, record);
                                
                            final GsonBuilder builder = new GsonBuilder();
                            builder.serializeNulls();
                            final Gson gson = builder.create();
                            e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
                        } // validRecord
                        else {
                            e.tag(LOGSTASH_TAG_MYSQL_IGNORE);
                        }
                        
                    } // end general or connection data
                    else {
                        e.tag(LOGSTASH_TAG_MYSQL_IGNORE);
                    }
                } catch (Exception exception) {
                    // TODO log event removed? 
                    //events.remove(e);
                    log.error("Error parsing mysql event " + logEvent(e), exception);
                    e.tag(LOGSTASH_TAG_MYSQL_PARSE_ERROR);
                }
                matchListener.filterMatched(e);
            }
        } // for events
        return events;
    }

    public static synchronized Time getTimestamp(String dateString) throws ParseException {
        if (dateString == null){
            log.warn("DateString is null");
            return new Time(0, 0, 0);
        }
        Date date = DATE_FORMATTER.parse(dateString);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
        long millis = zdt.toInstant().toEpochMilli();
        int  minOffset = zdt.getOffset().getTotalSeconds()/60;
        return new Time(millis, minOffset, 0);
    }
        
    private static SessionLocator parseSessionLocator(Event e, JsonObject data) {
        SessionLocator sessionLocator = new SessionLocator();       
        String serverIp = "0.0.0.0";
        int serverPort = SessionLocator.PORT_DEFAULT;

        if (e.getField("server_ip") instanceof String)
            serverIp = e.getField("server_ip").toString();
                    
        sessionLocator.setClientIp(UNKNOWN_STRING);
        sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
        sessionLocator.setClientIpv6(UNKNOWN_STRING);

        if (Util.isIPv6(serverIp)){
            sessionLocator.setServerIpv6(serverIp);
            sessionLocator.setIpv6(true);
            sessionLocator.setServerIp(UNKNOWN_STRING);
        } else {
            sessionLocator.setServerIp(serverIp);
            sessionLocator.setIpv6(false);
            sessionLocator.setServerIpv6(UNKNOWN_STRING);
        }

        sessionLocator.setServerPort(serverPort);

        if (data.has("login")) {
            JsonObject login = data.getAsJsonObject("login");
            String address = login.get("ip").getAsString();
            int port = SessionLocator.PORT_DEFAULT; // port not available, login.get("port").getAsInt();
            if (Util.isIPv6(address)) {
                sessionLocator.setIpv6(true);
                sessionLocator.setClientIpv6(address);
                sessionLocator.setClientPort(port);
                sessionLocator.setClientIp(UNKNOWN_STRING);
            } else { // ipv4 
                sessionLocator.setIpv6(false);
                sessionLocator.setClientIp(address);
                sessionLocator.setClientPort(port);
                sessionLocator.setClientIpv6(UNKNOWN_STRING);
            }
        }
        return sessionLocator;
    }

    public static Accessor parseAccessor(Event e, JsonObject data) {
        Accessor accessor = new Accessor();
        String serverHostname = UNKNOWN_STRING;
        String sourceProgram = UNKNOWN_STRING;
        String osStr = UNKNOWN_STRING;
        String osUser = UNKNOWN_STRING;
        
        if (e.getField("server_hostname") instanceof String)
            serverHostname = e.getField("server_hostname").toString();

        accessor.setDbProtocol(DATA_PROTOCOL_STRING);
        accessor.setServerType(SERVER_TYPE_STRING);

        if (data.has("account")) {
            JsonObject login = data.getAsJsonObject("account");
            String user = login.get("user").getAsString();

            accessor.setDbUser(user);
        }
        
       if (data.has(DATA_TYPE_CONNECTION)) {
            JsonObject connection_data = data.getAsJsonObject(DATA_TYPE_CONNECTION);
            if (connection_data.has("connection_attributes")) {
                JsonObject connection_attribs = connection_data.getAsJsonObject("connection_attributes");
                if (connection_attribs.has("_os"))
                    osStr = connection_attribs.get("_os").getAsString();
                if (connection_attribs.has("os_user"))
                    osUser = connection_attribs.get("os_user").getAsString();
            }
        }
 
        accessor.setServerHostName(serverHostname); 
        accessor.setSourceProgram(sourceProgram);

        accessor.setLanguage("MYSQL");
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);

        accessor.setClient_mac(UNKNOWN_STRING);
        accessor.setClientHostName(UNKNOWN_STRING);
        accessor.setClientOs(UNKNOWN_STRING);
        accessor.setCommProtocol(UNKNOWN_STRING);
        accessor.setDbProtocolVersion(UNKNOWN_STRING);
        accessor.setOsUser(osUser);
        accessor.setServerDescription(UNKNOWN_STRING);
        accessor.setServerOs(osStr);
        accessor.setServiceName(UNKNOWN_STRING);

        return accessor;
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
    public Collection<PluginConfigSpec<?>> configSchema() {
        // should return a list of all configuration options for this plugin
        return Collections.singletonList(SOURCE_CONFIG);
    }

    @Override
    public String getId() {
        return this.id;
    }
}
