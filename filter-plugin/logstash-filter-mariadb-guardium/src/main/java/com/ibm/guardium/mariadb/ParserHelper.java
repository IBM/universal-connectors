/*
© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.guardium.mariadb;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ibm.guardium.mariadb.constant.ApplicationConstant;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import co.elastic.logstash.api.Event;

/**
 * Parser Class will perform operation on parsing events and messages from the
 * MariaDB audit logs into a Guardium record instance Guardium records include
 * the accessor, the sessionLocator, data, and exceptions.
 *
 * @className @ParserHelper
 *
 */

public class ParserHelper {
	private static Logger log = LogManager.getLogger(ParserHelper.class);
	private static final DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
			.append(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));

	private static final DateTimeFormatter DATE_TIME_FORMATTER = dateTimeFormatterBuilder.toFormatter();
	static List<String> lst = new ArrayList<>();
	/**
	 * parseRecord method to parse data from event and set to record object
	 * 
	 * @param event
	 * @return record
	 */
	public static Record parseRecord(final Event event) throws Exception {
		Record record = new Record();
		try {
			record.setAccessor(parseAccessor(event));
			record.setSessionLocator(parseSessionLocator(event));
			record.setTime(getTime(event));
			record.setSessionId(getSessionId(event));
			record.setAppUserName(event.getField(ApplicationConstant.USERNAME_KEY) != null
					? event.getField(ApplicationConstant.USERNAME_KEY).toString()
					: ApplicationConstant.UNKNOWN_STRING);
			record.setDbName(getDbName(event));
			if (event.getField(ApplicationConstant.RETCODE_KEY) != null
					&& event.getField(ApplicationConstant.RETCODE_KEY).toString().trim() != ""
					&& Integer.parseInt(event.getField(ApplicationConstant.RETCODE_KEY).toString().trim()) == 0) {
				record.setData(parseData(event));
			} else if (event.getField(ApplicationConstant.RETCODE_KEY) != null
					&& event.getField(ApplicationConstant.RETCODE_KEY).toString().trim() != ""
					&& Integer.parseInt(event.getField(ApplicationConstant.RETCODE_KEY).toString().trim()) != 0) {
				record.setException(parseExceptionRecord(event));
			} else {
				throw new Exception("Invalid Record : ");
			}

		} catch (Exception e) {
			log.error("Exception occurred while parsing event in parseRecord method:  ", e);
			throw e;
		}
		return record;
	}

	/**
	 * Using this method to set details about the user who accessed the Accessor
	 * 
	 * @param event
	 * @return accessor
	 */
	public static Accessor parseAccessor(final Event event) throws Exception{
		Accessor accessor = new Accessor();
		try {
			accessor.setServerHostName(event.getField(ApplicationConstant.SERVERHOST_KEY) != null
					? event.getField(ApplicationConstant.SERVERHOST_KEY).toString()
					: ApplicationConstant.UNKNOWN_STRING);
			accessor.setClientHostName(event.getField(ApplicationConstant.HOSTNAME_KEY) != null
					? event.getField(ApplicationConstant.HOSTNAME_KEY).toString()
					: ApplicationConstant.UNKNOWN_STRING);
			accessor.setDbUser(getDbUser(event));
			accessor.setSourceProgram(ApplicationConstant.UNKNOWN_STRING);
			accessor.setServerType(ApplicationConstant.SERVER_TYPE_STRING);
			accessor.setLanguage(ApplicationConstant.LANGUAGE_STRING);
			accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
			accessor.setDbProtocol(ApplicationConstant.DBPROTOCAL_STRING);
			accessor.setClient_mac(ApplicationConstant.UNKNOWN_STRING);
			accessor.setClientOs(ApplicationConstant.UNKNOWN_STRING);
			accessor.setCommProtocol(ApplicationConstant.UNKNOWN_STRING);
			accessor.setDbProtocolVersion(ApplicationConstant.UNKNOWN_STRING);
			accessor.setOsUser(ApplicationConstant.UNKNOWN_STRING);
			accessor.setServerDescription(ApplicationConstant.UNKNOWN_STRING);
			accessor.setServerOs(ApplicationConstant.UNKNOWN_STRING);
			accessor.setServiceName(getDbName(event));
		} catch (Exception e) {
			log.error("Exception occurred while parsing event in parseAccessor method: ", e);
			throw e;
		}
		return accessor;
	}
	
	public static String getSessionId(final Event event) throws Exception {
		String sessionId = ApplicationConstant.NOT_AVAILABLE;
		if (event.getField(ApplicationConstant.CONNECTIONID_KEY) != null) {
			sessionId = Integer.toString((event.getField(ApplicationConstant.CONNECTIONID_KEY).toString()
					+ getDbName(event) + getDbUser(event)).hashCode());
		}
		return sessionId;

	}
	private static String getDbUser(Event event) {
		String dbUser = event.getField(ApplicationConstant.USERNAME_KEY) != null
				? event.getField(ApplicationConstant.USERNAME_KEY).toString()
				: ApplicationConstant.NOT_AVAILABLE;
		return dbUser;
	}

	private static String getDbName(Event event) {
		String dbName = event.getField(ApplicationConstant.DATABASE_KEY) != null
				? event.getField(ApplicationConstant.DATABASE_KEY).toString()
				: ApplicationConstant.UNKNOWN_STRING;
		return dbName;
	}
	
	/**
	 * Using this method to set details about the user who accessed the Exception
	 * source
	 * 
	 * @param event
	 * @return Exception
	 */
	public static ExceptionRecord parseExceptionRecord(final Event event) throws Exception {
		ExceptionRecord exception = new ExceptionRecord();
		try {
			if (event.getField(ApplicationConstant.RETCODE_KEY).equals("1045")) {
				exception.setExceptionTypeId(ApplicationConstant.EXCEPTION_TYPE_AUTHENTICATION_STRING);
				exception.setDescription("Authentication Failed (1045)");
				exception.setSqlString(event.getField(ApplicationConstant.OBJECT_KEY) != null
						? event.getField(ApplicationConstant.OBJECT_KEY).toString()
						: ApplicationConstant.NOT_AVAILABLE);

			} else if (event.getField(ApplicationConstant.RETCODE_KEY) != null) {
				exception.setExceptionTypeId(ApplicationConstant.EXCEPTION_TYPE_AUTHORIZATION_STRING);
				exception.setDescription("Error (" + event.getField(ApplicationConstant.RETCODE_KEY) + ")");
				exception.setSqlString(event.getField(ApplicationConstant.OBJECT_KEY) != null
						? event.getField(ApplicationConstant.OBJECT_KEY).toString()
						: ApplicationConstant.NOT_AVAILABLE);
			}

		} catch (Exception e) {
			log.error("Exception occurred while parsing event in parseExceptionRecord method: ", e);
			throw new Exception("Unable to parse the record.");
		}

		return exception;
	}

	/**
	 * Using this to set data object.
	 * 
	 * @param event
	 * @return Data
	 */

	public static Data parseData(final Event event) throws Exception {
		Data data = new Data();
		try {
			data.setOriginalSqlCommand(parseSQL(event));
		} catch (Exception e) {
			log.error("Exception occured while parsing event in parseData method:  ", e);
			throw new Exception("Exception occured while parsing event in parseData method: " + e.getMessage());
		}
		return data;
	}

	/**
	 * Using this method describes location details about the data source
	 * connection/session: Who connected, from which client IP and port, to what
	 * server IP and port
	 * 
	 * @param event
	 * @return SessionLocator
	 * @throws ParseException
	 */
	public static SessionLocator parseSessionLocator(final Event event) {
		SessionLocator sessionLocator = new SessionLocator();
		try {
			sessionLocator.setClientIp(event.getField(ApplicationConstant.CLIENT_IP) != null
					? event.getField(ApplicationConstant.CLIENT_IP).toString()
					: ApplicationConstant.CLIENT_IP_STRING);
			sessionLocator.setServerIp(event.getField(ApplicationConstant.SERVER_IP) != null
					? event.getField(ApplicationConstant.SERVER_IP).toString()
					: ApplicationConstant.SERVER_IP_STRING);
			sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);
			sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
			sessionLocator.setClientIpv6(ApplicationConstant.NOT_AVAILABLE);
			sessionLocator.setServerIpv6(ApplicationConstant.NOT_AVAILABLE);
			sessionLocator.setIpv6(Boolean.FALSE);
		} catch (Exception e) {
			log.error("Exception occurred while parsing event in getSessionLocator method: ", e);
		}
		return sessionLocator;

	}

	/**
	 * Using this method to set time.
	 * 
	 * @param event
	 * @return Time
	 */
	public static Time getTime(final Event event) {
		String dateString = event.getField(ApplicationConstant.TIMESTAMP_KEY).toString();
		String timeZone = event.getField(ApplicationConstant.MIN_OFFSET).toString();
		if(dateString!=null) {
			LocalDateTime dt = LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
			ZoneOffset offset = ZoneOffset.of(ZoneOffset.UTC.getId());
			if(timeZone != null ){
				offset = ZoneOffset.of(timeZone);
			}
			ZonedDateTime zdt = dt.atOffset(offset).toZonedDateTime();
			long millis = zdt.toInstant().toEpochMilli();
			int minOffset = zdt.getOffset().getTotalSeconds() / 60;
			return new Time(millis, minOffset, 0);
		}
		return null;
	}


	/**
	 * parseSQL() method will perform operation on String inputs, set the expected
	 * value into respective dataset name and then return the value as response
	 *
	 * @param event
	 * @methodName @parseSQL
	 * @return String Value
	 *
	 */

	public static String parseSQL(final Event event) throws Exception {
		StringBuilder builder = null;
		String fullSql = null;
		if (event.getField(ApplicationConstant.OBJECT_KEY) != null
				&& event.getField(ApplicationConstant.OBJECT_KEY).toString().trim() != "") {
			builder = new StringBuilder(event.getField(ApplicationConstant.OBJECT_KEY).toString().trim());
			builder.deleteCharAt(builder.length() - 1).deleteCharAt(0);
			fullSql = builder.toString().replaceAll("\\\\n","\n").replaceAll("\\\\r","\r").replaceAll("\\\\'", "'").replaceAll("\\\\","").replaceAll("\\\\t","\t");
			// Check for create user Query - Need to replace the '*' with 'x'.
			Pattern createUserPattern = Pattern.compile("^create\\s+user", Pattern.CASE_INSENSITIVE);			
			Matcher matcher = createUserPattern.matcher(fullSql);
			if (matcher.find()) {
				// Code to replace '*' with 'x'
				createUserPattern = Pattern.compile("[\\*]{2,}", Pattern.CASE_INSENSITIVE);
				matcher = createUserPattern.matcher(fullSql);
				if (matcher.find()) {
					char[] buf = new char[matcher.group(0).length()];
					Arrays.fill(buf, 'x');
					fullSql = fullSql.replaceAll(Pattern.quote(matcher.group(0)), new String(buf));

				}
			}
		} else {
			throw new Exception("Object is empty or  not parseable;");
		}
		return fullSql;
	}
}