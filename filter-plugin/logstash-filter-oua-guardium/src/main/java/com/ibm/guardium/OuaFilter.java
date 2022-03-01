package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;
import com.google.gson.*;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.GuardConstants;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.Util;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.text.ParseException;
import java.io.File;
import java.util.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

// class name must match plugin name
@LogstashPlugin(name = "oua_filter")
public class OuaFilter implements Filter {

    public static final String LOG42_CONF="log4j2uc.properties";
    static {
        try {
            String uc_etc = System.getenv("UC_ETC");
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            File file = new File(uc_etc +File.separator+LOG42_CONF);
            context.setConfigLocation(file.toURI());
        } catch (Exception e){
            System.err.println("Failed to load log4j configuration "+e.getMes***REMOVED***ge());
            e.printStackTrace();
        }
    }
    private static Logger log = LogManager.getLogger(OuaFilter.class);

    public static final PluginConfigSpec<String> SOURCE_CONFIG =
            PluginConfigSpec.stringSetting("source", "mes***REMOVED***ge");
    public static final String LOGSTASH_TAG_PARSE_ERROR = "_ouaguardium_parse_error";
    
    private String id;
    private String sourceField; 

	// first part is days since Unix Epoch
	// second part is UTC time since Unix Epoch
	// +000018533 22:00:03.981871
    private static String AUDIT_DATE_FORMAT = "HH:mm:ss.SSSSSS";
    private static final DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ofPattern(AUDIT_DATE_FORMAT));
    private static final DateTimeFormatter DATE_TIME_FORMATTER = dateTimeFormatterBuilder.toFormatter();

	public static final String SERVER_TYPE_STRING = "Oracle";
	public static final String DATA_PROTOCOL_STRING = "Oracle Unified Audit";
	public static final String LANGUAGE_STRING = "ORACLE";
	public static final String UNKNOWN_STRING = "";
	public static final String CLIENT_IP_TAG = "client_host_ip";
	public static final String SERVER_IP_TAG = "server_host_ip";
	public static final String CLIENT_HOST_TAG = "client_host_name";
	public static final String SERVER_HOST_TAG = "server_host_name";
	public static final String OS_USER_TAG = "os_user";
	public static final String SOURCE_PROGRAM_TAG = "client_program_name";
	public static final String DB_NAME_TAG = "dbname";
	public static final String TIMESTAMP_TAG = "event_timestamp";
	public static final String SQL_TAG = "sql_text";
	public static final String DB_USER_TAG = "userid";
	public static final String SESSION_ID_TAG = "sessionid";
	public static final String CON_NAME_TAG = "con_name";
	public static final String RETURN_CODE_TAG = "return_code";

    public OuaFilter(String id, Configuration config, Context context) {
        // constructors should validate configuration options
        this.id = id;
        if (config != null) {
            this.sourceField = config.get(SOURCE_CONFIG);
        }
    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
        ArrayList<Event> skippedEvents = new ArrayList<>();
        for (Event e : events) {
            if (log.isDebugEnabled()) {
               log.debug("Event: "+logEvent(e));
            }
			if (e.getField("mes***REMOVED***ge") instanceof String) {
				try {
            		Record record = OuaFilter.parseRecord(e);

            		final GsonBuilder builder = new GsonBuilder();
            		builder.serializeNulls();
            		final Gson gson = builder.create();
            		e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));

            		if (log.isDebugEnabled()) {
            		   log.debug("Record Event: "+logEvent(e));
            		}
            		matchListener.filterMatched(e); // Flag OK for filter input/parsing/out
            	} catch (Exception exception) {
                    log.error("Error parsing OUA event "+logEvent(e), exception);
            	    e.tag(LOGSTASH_TAG_PARSE_ERROR);
            	}
			}
        }

        // Remove skipped mongodb events from reaching output
        // FIXME log which events skipped
        events.removeAll(skippedEvents);
        return events;
    }

    public static Record parseRecord(final Event event) throws ParseException {
        Record record = new Record();
		
		if (event.getField(OuaFilter.DB_NAME_TAG) instanceof String) {
			record.setDbName(event.getField(OuaFilter.DB_NAME_TAG).toString());
		} else {
			record.setDbName(OuaFilter.UNKNOWN_STRING);
		}

		if (event.getField(OuaFilter.SESSION_ID_TAG) instanceof Long) {
			record.setSessionId(event.getField(OuaFilter.SESSION_ID_TAG).toString());
		} else {
			record.setSessionId(OuaFilter.UNKNOWN_STRING);
		}

		record.setSessionLocator(OuaFilter.parseSessionLocator(event));
        record.setAccessor(OuaFilter.parseAccessor(event));
        record.setData(OuaFilter.parseData(event));

		record.setTime(OuaFilter.getTime(event));
		record.setException(OuaFilter.parseExceptionRecord(event));

		return record;
	}

	private static ExceptionRecord parseExceptionRecord(final Event event) {
		ExceptionRecord exception_record = null;
		int return_code = 0;

		if (event.getField(OuaFilter.RETURN_CODE_TAG) instanceof Long) {
			return_code = Integer.parseInt(event.getField(OuaFilter.RETURN_CODE_TAG).toString());
		}

		if (return_code != 0) {
			exception_record = new ExceptionRecord();

			exception_record.setDescription(String.format("ORA-%05d", return_code));
			if (return_code == 1017 || return_code == 1004 || return_code == 1005 ||
					return_code == 1040 || return_code == 1045 || return_code == 1988 ||
					return_code == 12317 || return_code == 1267 || return_code == 28000 ||
					return_code == 28001 || return_code == 28030 || return_code == 28273 ||
					return_code == 28009) {
				exception_record.setExceptionTypeId("LOGIN_FAILED");
			} else {
				exception_record.setExceptionTypeId("SQL_ERROR");
			}
			if (event.getField(OuaFilter.SQL_TAG) instanceof String) {
				exception_record.setSqlString(event.getField(OuaFilter.SQL_TAG).toString());
			} else {
				exception_record.setSqlString(OuaFilter.UNKNOWN_STRING);
			}
		}

		return exception_record;
	}

	private static Data parseData(final Event event) {
		Data data = new Data();

		if (event.getField(OuaFilter.SQL_TAG) instanceof String) {
			data.setOriginalSqlCommand(event.getField(OuaFilter.SQL_TAG).toString());
		} else {
			data.setOriginalSqlCommand(OuaFilter.UNKNOWN_STRING);
		}

		return data;
	}
	
	private static Time getTime(final Event event) {
		if (event.getField(OuaFilter.TIMESTAMP_TAG) instanceof String) {
			String[] timestamp_parts = event.getField(OuaFilter.TIMESTAMP_TAG).toString().split(" ");

			if (timestamp_parts.length != 2) {
				return new Time(0, 0, 0);
			}

			LocalTime time = LocalTime.parse(timestamp_parts[1], DATE_TIME_FORMATTER);
			long millis = ((Long.parseLong(timestamp_parts[0].substring(1)) * 24) + time.getHour()) * 60 * 60;
			millis += (time.getMinute() * 60) + time.getSecond();
			millis *= 1000;
			millis += time.getNano() / 1000000;


			return new Time(millis, 0, 0);
		} else {
			return new Time(0, 0, 0);
		}
	}

    private static SessionLocator parseSessionLocator(final Event event) {
        SessionLocator sessionLocator = new SessionLocator();
        sessionLocator.setIpv6(false);

        sessionLocator.setClientIp(OuaFilter.UNKNOWN_STRING);
        sessionLocator.setClientIpv6(OuaFilter.UNKNOWN_STRING);
        sessionLocator.setServerIp(OuaFilter.UNKNOWN_STRING);
        sessionLocator.setServerIpv6(OuaFilter.UNKNOWN_STRING);

		if (event.getField(OuaFilter.CLIENT_IP_TAG) instanceof String) {
			String address = event.getField(OuaFilter.CLIENT_IP_TAG).toString();
			if (Util.isIPv6(address)) {
				sessionLocator.setIpv6(true);
        		sessionLocator.setClientIpv6(address);
			} else {
        		sessionLocator.setClientIp(address);
			}
		}
		if (event.getField(OuaFilter.SERVER_IP_TAG) instanceof String) {
			String address = event.getField(OuaFilter.SERVER_IP_TAG).toString();
			if (Util.isIPv6(address)) {
				sessionLocator.setIpv6(true);
        		sessionLocator.setServerIpv6(address);
			} else {
        		sessionLocator.setServerIp(address);
			}
		}

        return sessionLocator;
    }

	private static Accessor parseAccessor(final Event event) {
        Accessor accessor = new Accessor();

		accessor.setLanguage(OuaFilter.LANGUAGE_STRING);
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
        accessor.setDbProtocol(OuaFilter.DATA_PROTOCOL_STRING);
        accessor.setServerType(OuaFilter.SERVER_TYPE_STRING);

		if (event.getField(OuaFilter.CLIENT_HOST_TAG) instanceof String) {
			accessor.setClientHostName(event.getField(OuaFilter.CLIENT_HOST_TAG).toString());
		} else {
			accessor.setClientHostName(OuaFilter.UNKNOWN_STRING);
		}

		if (event.getField(OuaFilter.SERVER_HOST_TAG) instanceof String) {
			accessor.setServerHostName(event.getField(OuaFilter.SERVER_HOST_TAG).toString());
		} else {
			accessor.setServerHostName(OuaFilter.UNKNOWN_STRING);
		}

		if (event.getField(OuaFilter.OS_USER_TAG) instanceof String) {
			accessor.setOsUser(event.getField(OuaFilter.OS_USER_TAG).toString());
		} else {
			accessor.setOsUser(OuaFilter.UNKNOWN_STRING);
		}

		if (event.getField(OuaFilter.SOURCE_PROGRAM_TAG) instanceof String) {
			accessor.setSourceProgram(event.getField(OuaFilter.SOURCE_PROGRAM_TAG).toString());
		} else {
			accessor.setSourceProgram(OuaFilter.UNKNOWN_STRING);
		}

		if (event.getField(OuaFilter.DB_USER_TAG) instanceof String) {
			accessor.setDbUser(event.getField(OuaFilter.DB_USER_TAG).toString());
		} else {
			accessor.setDbUser(OuaFilter.UNKNOWN_STRING);
		}

		if (event.getField(OuaFilter.CON_NAME_TAG) instanceof String) {
			accessor.setServiceName(event.getField(OuaFilter.CON_NAME_TAG).toString());
		} else {
			accessor.setServiceName(OuaFilter.UNKNOWN_STRING);
		}

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
