/*
 *
 * Copyright 2020-2021 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 *
 */
package com.ibm.guardium.hdfs;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;
import com.google.gson.*;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.util.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.security.MessageDigest;
import java.nio.ByteBuffer;

// class name must match plugin name
@LogstashPlugin(name = "hdfs_guardium_filter")
public class HdfsGuardiumFilter implements Filter {

    private static Logger log = null;

    public static final PluginConfigSpec<String> SOURCE_CONFIG =
            PluginConfigSpec.stringSetting("source", "message");
    public static final String LOGSTASH_TAG_SKIP_NOT_HDFS = "_hdfsguardium_skip_not_hdfs";
    public static final String LOGSTASH_TAG_PARSE_ERROR = "_hdfsguardium_parse_error";
    
    private String id;

	public static final String HDFS_AUDIT_MARK_STRING = "FSNamesystem.audit";
    public static final String DATA_PROTOCOL_STRING = "HDFS native audit";
    public static final String UNKNOWN_STRING = "";
    public static final String SERVER_TYPE_STRING = "HDFS";
	public static final String APP_NAME_STRING = "HADOOP CLIENT PROGRAM";
	public static final String NULL_STRING = "null";
	public static final String TRUE_STRING = "true";
	public static final String FALSE_STRING = "false";
	public static final String ALLOW_TAG_STRING = "allowed";
	public static final String CMD_TAG_STRING = "cmd";
	public static final String SRC_TAG_STRING = "src";
	public static final String DST_TAG_STRING = "dst";
	public static final String PERM_TAG_STRING = "perm";
	public static final String IP_TAG_STRING = "ip";
	public static final String USER_TAG_STRING = "ugi";
	public static final String TIME_TAG_STRING = "timestamp";
	public static final String HOSTNAME_TAG_STRING = "hostname";
	public static final String EVENT_TAG_STRING = "event";
	public static final String TIMEZONE_TAG_STRING = "timezone";
	public static final String SPACE_STRING = " ";
	public static final String EQUAL_STRING = "=";
	public static final String KERBEROS_MARK_STRING = " (auth:KERBEROS)";
	public static final String VIA_MARK_STRING = " via ";
	public static final String PROXY_MARK_STRING = " (auth:PROXY)";
    public static final String EXCEPTION_TYPE_AUTHORIZATION_STRING = "SQL_ERROR";
	public static final String EXCEPTION_DESCRIPTION_STRING = "Operation not allowed";
	public static final String HOST_INFO_TAG_STRING = "host";

	// 2020-08-15 17:04:03,741
    private static String AUDIT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss,SSS";
    private static final DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ofPattern(AUDIT_DATE_FORMAT));
    private static final DateTimeFormatter DATE_TIME_FORMATTER = dateTimeFormatterBuilder.toFormatter();

    public HdfsGuardiumFilter(String id, Configuration config, Context context) {
        // constructors should validate configuration options
        this.id = id;
    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		if (log == null) {
			log = LogManager.getLogger(HdfsGuardiumFilter.class);
			log.error("Got logger at filter instantiation");
		}
        ArrayList<Event> skippedEvents = new ArrayList<>();
        for (Event e : events) {
            log.debug("Event: " + logEvent(e));
			if (e.getField("message") instanceof String) {
				// String msg_string = e.getField("message").toString();
				if (e.getField("message").toString().contains(HdfsGuardiumFilter.HDFS_AUDIT_MARK_STRING)) {
					try {
            			Record record = HdfsGuardiumFilter.parseRecord(e);

            			final GsonBuilder builder = new GsonBuilder();
            			builder.serializeNulls();
            			final Gson gson = builder.create();
            			e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));

            			log.debug("Record Event: "+logEvent(e));
            			matchListener.filterMatched(e); // Flag OK for filter input/parsing/out
            		} catch (Exception exception) {
            		    log.error("Error parsing HDFS event " + logEvent(e), exception);
            		    e.tag(LOGSTASH_TAG_PARSE_ERROR);
            		}
				} else {
            		e.tag(LOGSTASH_TAG_SKIP_NOT_HDFS);
				}
			}
        }

        // Remove skipped mongodb events from reaching output
        events.removeAll(skippedEvents);
        return events;
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
    
	public static Record parseRecord(final Event event) throws ParseException {
        Record record = new Record();

        record.setSessionId(HdfsGuardiumFilter.UNKNOWN_STRING);
        record.setDbName(HdfsGuardiumFilter.UNKNOWN_STRING);
		// should be first user of a PROXY ugi
		if (event.getField(HdfsGuardiumFilter.USER_TAG_STRING) instanceof String) {
        	record.setAppUserName(parseAppUserName(event.getField(HdfsGuardiumFilter.USER_TAG_STRING).toString()));
		} else {
        	record.setAppUserName(HdfsGuardiumFilter.UNKNOWN_STRING);
		}
        
		record.setSessionLocator(HdfsGuardiumFilter.parseSessionLocator(event));
        record.setAccessor(HdfsGuardiumFilter.parseAccessor(event));
        
		if (event.getField(HdfsGuardiumFilter.ALLOW_TAG_STRING) instanceof String) {
			if (event.getField(HdfsGuardiumFilter.ALLOW_TAG_STRING).equals(HdfsGuardiumFilter.TRUE_STRING)) {
				record.setData(HdfsGuardiumFilter.parseData(event));
			} else {
				record.setException(HdfsGuardiumFilter.parseException(event));
			}
		}
 
		record.getSessionLocator().setClientPort(HdfsGuardiumFilter.getSessionHash(record.getAppUserName(), record.getAccessor().getDbUser(), record.getSessionLocator().getClientIp()));
		record.getAccessor().setServiceName(HdfsGuardiumFilter.UNKNOWN_STRING);

        record.setTime(HdfsGuardiumFilter.getTime(event));

        return record;
    }

	public static short getSessionHash(String app_user, String user, String client_ip) {
		StringBuilder sb = new StringBuilder();
		short hash_id = 0;
		try {
			MessageDigest msg_digest = MessageDigest.getInstance("SHA-256");

			sb.append(app_user).append(user).append(client_ip);

			byte[] digest = msg_digest.digest(sb.toString().getBytes());

			ByteBuffer bb = ByteBuffer.wrap(digest);

			hash_id = bb.getShort();

			if (hash_id < 0) {
				hash_id *= -1;
			}
		} catch (Exception e) {
		}

		return hash_id;
	}

	public static String parseAppUserName(String ugi) {
		String result = HdfsGuardiumFilter.UNKNOWN_STRING;
		String[] str_parts = ugi.split(HdfsGuardiumFilter.VIA_MARK_STRING);

		if (str_parts.length > 1) {
			result = str_parts[0];
		}

		return trimUserAuth(result);
	}

	public static String parseDbUser(String ugi) {
		String result = HdfsGuardiumFilter.UNKNOWN_STRING;
		String[] str_parts = ugi.split(HdfsGuardiumFilter.VIA_MARK_STRING);

		if (str_parts.length > 1) {
			result = str_parts[1];
		} else {
			result = str_parts[0];
		}

		return trimUserAuth(result);
	}

	private static String trimUserAuth(String user) {
		if (user.contains(HdfsGuardiumFilter.KERBEROS_MARK_STRING)) {
			user = user.substring(0, user.length() - HdfsGuardiumFilter.KERBEROS_MARK_STRING.length());
		} else if (user.contains(HdfsGuardiumFilter.PROXY_MARK_STRING)) {
			user = user.substring(0, user.length() - HdfsGuardiumFilter.PROXY_MARK_STRING.length());
		}

		return user;
	}

    private static SessionLocator parseSessionLocator(Event event) {
        SessionLocator sessionLocator = new SessionLocator();
        sessionLocator.setIpv6(false);

        sessionLocator.setClientIp(HdfsGuardiumFilter.UNKNOWN_STRING);
        sessionLocator.setClientIpv6(HdfsGuardiumFilter.UNKNOWN_STRING);
        sessionLocator.setServerIp(HdfsGuardiumFilter.UNKNOWN_STRING);
        sessionLocator.setServerIpv6(HdfsGuardiumFilter.UNKNOWN_STRING);

		if (event.getField(HdfsGuardiumFilter.IP_TAG_STRING) instanceof String) {
			String address = event.getField(HdfsGuardiumFilter.IP_TAG_STRING).toString();
			if (Util.isIPv6(address)) {
				sessionLocator.setIpv6(true);
        		sessionLocator.setClientIpv6(address);
			} else {
        		sessionLocator.setClientIp(address);
			}
		}
		if (event.getField(HdfsGuardiumFilter.HOST_INFO_TAG_STRING) instanceof HashMap) {
			String address = "";
			ArrayList<String> ip_list = ((ArrayList<String>)((HashMap<String, Object>)event.getField(HdfsGuardiumFilter.HOST_INFO_TAG_STRING)).get(HdfsGuardiumFilter.IP_TAG_STRING));
			if (!ip_list.isEmpty()) {
				address = ip_list.get(ip_list.size() - 1);
			}
			if (!address.isEmpty()) {
				if (Util.isIPv6(address)) {
					sessionLocator.setIpv6(true);
        			sessionLocator.setServerIpv6(address);
				} else {
        			sessionLocator.setServerIp(address);
				}
			}
		}

        return sessionLocator;
    }
 
	private static Accessor parseAccessor(Event event) {
        Accessor accessor = new Accessor();

        accessor.setDbProtocol(HdfsGuardiumFilter.DATA_PROTOCOL_STRING);
        accessor.setServerType(HdfsGuardiumFilter.SERVER_TYPE_STRING);

		if (event.getField(HdfsGuardiumFilter.USER_TAG_STRING) instanceof String) {
			// should be username without any auth info
			accessor.setDbUser(parseDbUser(event.getField(HdfsGuardiumFilter.USER_TAG_STRING).toString()));
		} else {
			accessor.setDbUser(HdfsGuardiumFilter.UNKNOWN_STRING);
		}

		if (event.getField(HdfsGuardiumFilter.HOST_INFO_TAG_STRING) instanceof HashMap) {
			accessor.setServerHostName((String)((HashMap<String, Object>)event.getField(HdfsGuardiumFilter.HOST_INFO_TAG_STRING)).get(HdfsGuardiumFilter.HOSTNAME_TAG_STRING));
		} else {
        	accessor.setServerHostName(HdfsGuardiumFilter.UNKNOWN_STRING);
		}
        accessor.setSourceProgram(HdfsGuardiumFilter.APP_NAME_STRING);

        accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);

        accessor.setClient_mac(HdfsGuardiumFilter.UNKNOWN_STRING);
		if (event.getField(HdfsGuardiumFilter.HOSTNAME_TAG_STRING) instanceof String) {
        	accessor.setClientHostName(event.getField(HdfsGuardiumFilter.HOSTNAME_TAG_STRING).toString());
		} else {
        	accessor.setClientHostName(HdfsGuardiumFilter.UNKNOWN_STRING);
		}
        accessor.setClientOs(HdfsGuardiumFilter.UNKNOWN_STRING);
        accessor.setCommProtocol(HdfsGuardiumFilter.UNKNOWN_STRING);
        accessor.setDbProtocolVersion(HdfsGuardiumFilter.UNKNOWN_STRING);
        accessor.setOsUser(HdfsGuardiumFilter.UNKNOWN_STRING);
        accessor.setServerDescription(HdfsGuardiumFilter.UNKNOWN_STRING);
        accessor.setServerOs(HdfsGuardiumFilter.UNKNOWN_STRING);
        accessor.setServiceName(HdfsGuardiumFilter.UNKNOWN_STRING);

        return accessor;
    }

	private static Time getTime(final Event event) {
		String audit_date_string = null;

	 	if (event.getField(HdfsGuardiumFilter.TIME_TAG_STRING) instanceof String) {
			audit_date_string = event.getField(HdfsGuardiumFilter.TIME_TAG_STRING).toString();
		} else {
			log.error("Unable to get timestamp from event");
			return new Time(0, 0, 0);
		}
		
		if (event.getField(HdfsGuardiumFilter.EVENT_TAG_STRING) instanceof HashMap) {
			String timezone = ((HashMap<String, String>)event.getField(HdfsGuardiumFilter.EVENT_TAG_STRING)).get(HdfsGuardiumFilter.TIMEZONE_TAG_STRING);
			ZonedDateTime date = LocalDateTime.parse(audit_date_string, DATE_TIME_FORMATTER).atZone(ZoneId.of(timezone));
			long millis = date.toInstant().toEpochMilli();
			int  minOffset = date.getOffset().getTotalSeconds()/60;
			return new Time(millis, minOffset, 0);
		} else {
			log.error("Unable to get timezone from event, using local timezone");
			ZonedDateTime date = LocalDateTime.parse(audit_date_string, DATE_TIME_FORMATTER).atZone(ZoneId.systemDefault());
			long millis = date.toInstant().toEpochMilli();
			int  minOffset = date.getOffset().getTotalSeconds()/60;
			return new Time(millis, minOffset, 0);
		}
	}
 
	private static Data parseData(Event event) {
        Data data = new Data();
        try {
            Construct construct = HdfsGuardiumFilter.ParseAsConstruct(event);
            if (construct != null) {
                data.setConstruct(construct);

				// if (event.getField(HdfsGuardiumFilter.CMD_TAG_STRING) instanceof String) {
				// 	StringBuilder orig_sql_bld = new StringBuilder();
				// 	orig_sql_bld.append(HdfsGuardiumFilter.CMD_TAG_STRING);
				// 	orig_sql_bld.append(HdfsGuardiumFilter.EQUAL_STRING);
				// 	orig_sql_bld.append(event.getField(HdfsGuardiumFilter.CMD_TAG_STRING));
        		// 	data.setFullSql(orig_sql_bld.toString());
				// } else {
                //     construct.setFullSql(HdfsGuardiumFilter.UNKNOWN_STRING);
				// }
            }
        } catch (Exception e) {
            throw e;
        }
        return data;
    }
 
	private static Construct ParseAsConstruct(final Event event) {
        try {
            final Sentence sentence = HdfsGuardiumFilter.parseSentence(event);
            
            final Construct construct = new Construct();
            construct.sentences.add(sentence);
 
			construct.setFullSql(buildSqlString(event));
			construct.setRedactedSensitiveDataSql(construct.getFullSql());
            return construct;
        } catch (final Exception e) {
            throw e;
        }
    }

	public static String buildSqlString(final Event event) {
		StringBuilder full_sql_bld = new StringBuilder();
		if (event.getField(HdfsGuardiumFilter.CMD_TAG_STRING) instanceof String) {
			full_sql_bld.append(HdfsGuardiumFilter.CMD_TAG_STRING);
			full_sql_bld.append(HdfsGuardiumFilter.EQUAL_STRING);
			full_sql_bld.append(event.getField(HdfsGuardiumFilter.CMD_TAG_STRING));
		}
		if (event.getField(HdfsGuardiumFilter.SRC_TAG_STRING) instanceof String &&
				!event.getField(HdfsGuardiumFilter.SRC_TAG_STRING).toString().equals(HdfsGuardiumFilter.NULL_STRING)) {
			full_sql_bld.append(HdfsGuardiumFilter.SPACE_STRING);
			full_sql_bld.append(HdfsGuardiumFilter.SRC_TAG_STRING);
			full_sql_bld.append(HdfsGuardiumFilter.EQUAL_STRING);
			full_sql_bld.append(event.getField(HdfsGuardiumFilter.SRC_TAG_STRING));
		}
		if (event.getField(HdfsGuardiumFilter.DST_TAG_STRING) instanceof String &&
				!event.getField(HdfsGuardiumFilter.DST_TAG_STRING).toString().equals(HdfsGuardiumFilter.NULL_STRING)) {
			full_sql_bld.append(HdfsGuardiumFilter.SPACE_STRING);
			full_sql_bld.append(HdfsGuardiumFilter.DST_TAG_STRING);
			full_sql_bld.append(HdfsGuardiumFilter.EQUAL_STRING);
			full_sql_bld.append(event.getField(HdfsGuardiumFilter.DST_TAG_STRING));
		}
		if (event.getField(HdfsGuardiumFilter.PERM_TAG_STRING) instanceof String &&
				!event.getField(HdfsGuardiumFilter.PERM_TAG_STRING).toString().equals(HdfsGuardiumFilter.NULL_STRING)) {
			full_sql_bld.append(HdfsGuardiumFilter.SPACE_STRING);
			full_sql_bld.append(HdfsGuardiumFilter.PERM_TAG_STRING);
			full_sql_bld.append(HdfsGuardiumFilter.EQUAL_STRING);
			full_sql_bld.append(event.getField(HdfsGuardiumFilter.PERM_TAG_STRING));
		}
		return full_sql_bld.toString();
	}
 
    private static Sentence parseSentence(final Event event) {
        Sentence sentence = null;

		if (event.getField(HdfsGuardiumFilter.CMD_TAG_STRING) instanceof String) {
			sentence = new Sentence(event.getField(HdfsGuardiumFilter.CMD_TAG_STRING).toString());
		} else {
			sentence = new Sentence(HdfsGuardiumFilter.UNKNOWN_STRING);
		}

		if (event.getField(HdfsGuardiumFilter.SRC_TAG_STRING) instanceof String &&
				!event.getField(HdfsGuardiumFilter.SRC_TAG_STRING).toString().equals(HdfsGuardiumFilter.NULL_STRING)) {
			SentenceObject src_object = new SentenceObject(event.getField(HdfsGuardiumFilter.SRC_TAG_STRING).toString());
			src_object.setType(HdfsGuardiumFilter.SRC_TAG_STRING);
			sentence.getObjects().add(src_object);
		}

		if (event.getField(HdfsGuardiumFilter.DST_TAG_STRING) instanceof String &&
				!event.getField(HdfsGuardiumFilter.DST_TAG_STRING).toString().equals(HdfsGuardiumFilter.NULL_STRING)) {
			SentenceObject dst_object = new SentenceObject(event.getField(HdfsGuardiumFilter.DST_TAG_STRING).toString());
			dst_object.setType(HdfsGuardiumFilter.DST_TAG_STRING);
			sentence.getObjects().add(dst_object);
		}

		if (event.getField(HdfsGuardiumFilter.PERM_TAG_STRING) instanceof String &&
				!event.getField(HdfsGuardiumFilter.PERM_TAG_STRING).toString().equals(HdfsGuardiumFilter.NULL_STRING)) {
			SentenceObject perm_object = new SentenceObject(event.getField(HdfsGuardiumFilter.PERM_TAG_STRING).toString());
			perm_object.setType(HdfsGuardiumFilter.PERM_TAG_STRING);
			sentence.getObjects().add(perm_object);
		}

        return sentence;
    }

	private static ExceptionRecord parseException(final Event event) {
		ExceptionRecord exception_record = new ExceptionRecord();
		exception_record.setExceptionTypeId(HdfsGuardiumFilter.EXCEPTION_TYPE_AUTHORIZATION_STRING);
		exception_record.setDescription(HdfsGuardiumFilter.EXCEPTION_DESCRIPTION_STRING);
		exception_record.setSqlString(buildSqlString(event));
		return exception_record;
	}
}
