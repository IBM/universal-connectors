/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.guardium.icd.postgresql;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.guardium.icd.postgresql.ApplicationConstant;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import co.elastic.logstash.api.Event;

public class Parser {

	private static Logger log = LogManager.getLogger(Parser.class);

	/***
	 * Parses logs and returns a Guard Record object
	 *
	 * @param event
	 * @return
	 * @throws Exception
	 * @throws ParseException
	 */
	public static Record parseRecord(Event event) throws Exception {
		Record record = new Record();
		try {
			record.setAppUserName(event.getField(ApplicationConstant.USER_NAME) != null
					? event.getField(ApplicationConstant.USER_NAME).toString()
					: ApplicationConstant.UNKNOWN_STRING);
			record.setDbName(getDbName(event));
			record.setSessionId(getSessionId(event));
			record.setAccessor(parseAccessor(event));
			record.setSessionLocator(parseSessionLocator(event));
			record.setTime(getTime(event));
			if (event.getField(ApplicationConstant.STATEMENT) != null
					&& event.getField(ApplicationConstant.STATEMENT).toString().startsWith("F")){
				record.setException(parseExceptionRecord(event));
			} else {
				record.setData(parseData(event));
			}
		} catch (Exception e) {
			log.error("Exception occurred while parsing event in parseRecord method:  ", e);
			throw new Exception("Exception occured while parsing event in parseData method: " + e.getMessage());
		}
		return record;
	}

	/**
	 * Method to get the Session Id from the event
	 *
	 * @param event
	 * @return
	 */
	private static String getSessionId(Event event) {
		String sessionId = ApplicationConstant.NOT_AVAILABLE;
		if (event.getField(ApplicationConstant.SESSION_ID) != null
				&& !event.getField(ApplicationConstant.SESSION_ID).toString().isEmpty()) {
			sessionId = event.getField(ApplicationConstant.SESSION_ID).toString();
		}
		return sessionId;
	}

	/**
	 * Method to get the database name from the event
	 *
	 * @param event
	 * @return
	 */
	private static String getDbName(Event event) {
		String dbName = StringUtils.EMPTY;
		String accountId = event.getField(ApplicationConstant.ACCOUNT_ID) != null
				? event.getField(ApplicationConstant.ACCOUNT_ID).toString()
				: ApplicationConstant.UNKNOWN_STRING;
		String db = event.getField(ApplicationConstant.DB_NAME) != null
				? event.getField(ApplicationConstant.DB_NAME).toString()
				: ApplicationConstant.UNKNOWN_STRING;
		String instanceId = event.getField(ApplicationConstant.DETAILS) != null
				? event.getField(ApplicationConstant.DETAILS).toString()
				: ApplicationConstant.UNKNOWN_STRING;
		dbName = accountId + instanceId + ":" + db;
		return dbName;
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
		String clientIp = (event.getField(ApplicationConstant.CLIENT_IP) != null && !event.getField(ApplicationConstant.CLIENT_IP).toString().isEmpty())
				? event.getField(ApplicationConstant.CLIENT_IP).toString()
				: ApplicationConstant.DEFAULT_IP;
		if (Util.isIPv6(clientIp)) {
			sessionLocator.setIpv6(true);
			sessionLocator.setClientIpv6(clientIp);
			sessionLocator.setServerIpv6(ApplicationConstant.DEFAULT_IPV6);

		} else {
			sessionLocator.setClientIp(clientIp);
			sessionLocator.setServerIp(ApplicationConstant.DEFAULT_IP);
		}
		sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
		sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);
		return sessionLocator;
	}

	/**
	 * Using this method to set details about the user who accessed the Accessor
	 *
	 * @param event
	 * @return Accessor
	 */
	public static Accessor parseAccessor(final Event event) {
		Accessor accessor = new Accessor();
		accessor.setClientHostName(ApplicationConstant.UNKNOWN_STRING);
		accessor.setDbUser((event.getField(ApplicationConstant.USER_NAME) != null && !event.getField(ApplicationConstant.USER_NAME).toString().isEmpty())
				? event.getField(ApplicationConstant.USER_NAME).toString()
				: ApplicationConstant.NOT_AVAILABLE);
		accessor.setServerType(ApplicationConstant.SERVER_TYPE_STRING);
		accessor.setServiceName(getDbName(event));
		accessor.setDbProtocol(ApplicationConstant.DB_PROTOCOL);
		accessor.setDbProtocolVersion(ApplicationConstant.UNKNOWN_STRING);
		accessor.setSourceProgram(event.getField(ApplicationConstant.SOURCE_PROGRAM) != null
				? event.getField(ApplicationConstant.SOURCE_PROGRAM).toString()
				: ApplicationConstant.NOT_AVAILABLE);
		if (event.getField(ApplicationConstant.INCLUDE_ACCOUNTID_IN_HOST).toString().equalsIgnoreCase("true")) {
			accessor.setServerHostName(event.getField(ApplicationConstant.ACCOUNT_ID) != null
					? event.getField(ApplicationConstant.ACCOUNT_ID).toString().concat(".ibm.com")
					: ApplicationConstant.UNKNOWN_STRING);
		}
		else {
			accessor.setServerHostName("Postgres.ibm.com");
		}
		accessor.setServerDescription(ApplicationConstant.UNKNOWN_STRING);
		accessor.setLanguage(ApplicationConstant.LANGUAGE);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
		accessor.setClient_mac(ApplicationConstant.UNKNOWN_STRING);
		accessor.setClientHostName(ApplicationConstant.UNKNOWN_STRING);
		accessor.setCommProtocol(ApplicationConstant.UNKNOWN_STRING);
		accessor.setOsUser(ApplicationConstant.UNKNOWN_STRING);
		accessor.setClientOs(ApplicationConstant.UNKNOWN_STRING);
		accessor.setServerOs(ApplicationConstant.UNKNOWN_STRING);
		return accessor;
	}

	/**
	 * parseData() method will perform operation on JsonObject data, set the
	 * expected value into respective Data Object and then return the value as
	 * response
	 *
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public static Data parseData(final Event event) {
		Data data = new Data();
		if (event.getField(ApplicationConstant.SQL_QUERY) != null) {
			String query = event.getField(ApplicationConstant.SQL_QUERY).toString();
			String str = query.toLowerCase();
			if (str.startsWith("\\\"")) {
				query = query.substring(2, query.length() - 2).replaceAll("\\\\\"\\\\\"", "\"");
			}
			if (str.contains(",misc,???,,,merge")) {
				query = query.substring(12);
			}
			data.setOriginalSqlCommand(query);
		}
		return data;
	}

	/**
	 * Using this method to set details about the user who accessed the Exception
	 * source
	 *
	 * @param event
	 * @return Exception
	 */
	public static ExceptionRecord parseExceptionRecord(final Event event) {
		ExceptionRecord exception = new ExceptionRecord();

		String errorCode = event.getField(ApplicationConstant.ERROR_CODE).toString();
		if(errorCode.equals("28P01")) {
			exception.setExceptionTypeId(ApplicationConstant.LOGIN_FAILED);
		} else {
			exception.setExceptionTypeId(ApplicationConstant.EXCEPTION_TYPE_SQL_ERROR_STRING);
		}
		exception.setDescription(errorCode);
		exception.setSqlString(event.getField(ApplicationConstant.SQL_QUERY).toString());

		return exception;
	}

	/**
	 * Method to get the time from JsonObject, set the expected value into
	 * respective Time Object and then return the value as response
	 *
	 * @param event
	 * @return
	 */
	public static Time getTime(Event event) throws Exception {
		String timeStamp = event.getField(ApplicationConstant.TIMESTAMP) != null
				? event.getField(ApplicationConstant.TIMESTAMP).toString()
				: ApplicationConstant.UNKNOWN_STRING;
		String timeZone = event.getField(ApplicationConstant.TIMEZONE) != null
				? event.getField(ApplicationConstant.TIMEZONE).toString()
				: ApplicationConstant.UNKNOWN_STRING;
		String dateString = timeStamp+ " " + timeZone;
		Long millis = 0L;
		int minOffset = 0;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
		if (!dateString.trim().isEmpty()) {
			Date date = df.parse(dateString);
			millis = date.getTime();
			minOffset = 0;

		}
		return new Time(millis, minOffset, 0);
	}
}