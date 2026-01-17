/*
Copyright IBM Corp. 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.guardium.scylladb;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Data;

import co.elastic.logstash.api.Event;
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

public class Parser {

	private static Logger log = LogManager.getLogger(Parser.class);

	/**
	 * Method to parse event from Event and set to record object
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public static Record parseRecord(Event event) throws Exception {
		Record record = new Record();
		try {
			Integer hashcode = null;
			if (event.getField(Constants.DB_NAME) != null && event.getField(Constants.USER_NAME) != null
					&& event.getField(Constants.SERVER_IP) != null && event.getField(Constants.CLIENT_IP) != null) {
				hashcode = (event.getField(Constants.SERVER_IP).toString()
						.replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")|(   )", "").replaceAll("(\\s)?#012", " ")
						+ event.getField(Constants.CLIENT_IP).toString()
								.replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")|(   )", "")
								.replaceAll("(\\s)?#012", " ")
						+ event.getField(Constants.DB_NAME).toString()
								.replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")|(   )", "")
								.replaceAll("(\\s)?#012", " ")
						+ event.getField(Constants.USER_NAME).toString()
								.replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")|(   )", "")
								.replaceAll("(\\s)?#012", " "))
						.hashCode();
				record.setSessionId(hashcode.toString().replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")|(   )", "")
						.replaceAll("(\\s)?#012", " "));

			} else {
				record.setSessionId(Constants.NOT_AVAILABLE);
			}
			record.setTime(parseTime(event));
			record.setAppUserName(Constants.UNKNOWN_STRING);
			record.setSessionLocator(parseSessionLocator(event));
			record.setDbName(getDbName(event));
			record.setAccessor(parseAccessor(event));
			if (event.getField(Constants.ERROR_STRING) != null && event.getField(Constants.ERROR_STRING).toString()
					.replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")|(   )", "").replaceAll("(\\s)?#012", " ")
					.equals("true")) {
				record.setException(parseExceptionRecord(event));
			} else {
				record.setData(parseData(event));
			}

		} catch (Exception e) {
			log.error("Exception occurred while parsing event in parseRecord method:  ", e);
			throw new Exception("Exception occured while parsing event in parseevent method: " + e.getMessage());
		}
		return record;
	}

	/**
	 * Method to get the time from Event, set the expected value into respective
	 * Time Object and then return the value as response
	 * 
	 * @param event
	 * @return
	 */
	public static Time parseTime(Event event) {

		if (event.getField(Constants.TIMESTAMP) != null && !event.getField(Constants.TIMESTAMP).toString().isEmpty()) {
			String dateString = event.getField(Constants.TIMESTAMP).toString();
			ZonedDateTime date = ZonedDateTime.parse(dateString);
			long millis = date.toInstant().toEpochMilli();
			int minOffset = date.getOffset().getTotalSeconds() / 60;
			return new Time(millis, minOffset, 0);

		} else {
			LocalDateTime ldt = LocalDateTime.now();
			ZonedDateTime date = ldt.atZone(ZoneId.of("UTC"));
			long millis = date.toInstant().toEpochMilli();
			int minOffset = date.getOffset().getTotalSeconds() / 60;
			return new Time(millis, minOffset, 0);
		}

	}

	/**
	 * Method to get the database name or the service name from the Event
	 * 
	 * @param event
	 * @param event
	 * @return
	 */
	public static String getDbName(Event event) {

		String dbName = Constants.UNKNOWN_STRING;
		if (event.getField(Constants.DB_NAME) != null && !event.getField(Constants.DB_NAME).toString()
				.replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")|(   )", "").replaceAll("(\\s)?#012", " ")
				.isEmpty()) {
			dbName = event.getField(Constants.DB_NAME).toString()
					.replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")|(   )", "").replaceAll("(\\s)?#012", " ");
		} else {
			dbName = Constants.NOT_AVAILABLE;
		}
		return dbName;
	}

	/**
	 * Method to get the dbuser from the Event
	 * 
	 * @param event
	 * @param event
	 * @return
	 */
	public static String getDbUser(Event event) {
		String dbUser = Constants.UNKNOWN_STRING;
		if (event.getField(Constants.USER_NAME) != null && !event.getField(Constants.USER_NAME).toString()
				.replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")|(   )", "").replaceAll("(\\s)?#012", " ")
				.isEmpty()) {
			dbUser = event.getField(Constants.USER_NAME).toString()
					.replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")|(   )", "").replaceAll("(\\s)?#012", " ");
		} else {
			dbUser = Constants.NOT_AVAILABLE;
		}
		return dbUser;
	}

	/**
	 * parseAccessor() method will perform operation on Event event and Event event,
	 * set the expected value into respective Accessor Object and then return the
	 * value as response
	 * 
	 * @param event
	 * @param records
	 * @return
	 */

	public static Accessor parseAccessor(Event event) {
		Accessor accessor = new Accessor();
		accessor.setServiceName(getDbName(event));
		accessor.setDbUser(getDbUser(event));
		accessor.setLanguage(Constants.LANGUAGE);
		accessor.setServerType(Constants.SERVER_TYPE_STRING);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
		accessor.setDbProtocol(Constants.DB_PROTOCOL);
		accessor.setServerHostName(
				event.getField(Constants.SERVERHOSTNAME) != null ? event.getField(Constants.SERVERHOSTNAME).toString()
						: Constants.UNKNOWN_STRING);
		accessor.setSourceProgram(Constants.UNKNOWN_STRING);
		accessor.setClient_mac(Constants.UNKNOWN_STRING);
		accessor.setClientOs(Constants.UNKNOWN_STRING);
		accessor.setCommProtocol(Constants.UNKNOWN_STRING);
		accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
		accessor.setOsUser(Constants.UNKNOWN_STRING);
		accessor.setServerDescription(Constants.UNKNOWN_STRING);
		accessor.setServerOs(Constants.UNKNOWN_STRING);
		accessor.setClientHostName(Constants.UNKNOWN_STRING);

		return accessor;
	}

	/**
	 * parseData() method will perform operation on Event event, set the expected
	 * value into respective event Object and then return the value as response
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public static Data parseData(final Event event) throws Exception {
		Data data = new Data();
		if (event.getField(Constants.CATEGORY) != null && event.getField(Constants.CATEGORY).toString()
				.replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")|(   )", "").replaceAll("(\\s)?#012", " ")
				.replaceAll("  ", "").equals("AUTH")) {
			data.setOriginalSqlCommand("LOGIN");
		} else if (event.getField(Constants.SQL_QUERY) != null) {
			data.setOriginalSqlCommand(event.getField(Constants.SQL_QUERY).toString().replaceAll("(\\\")", "")
					.replaceAll("((\\s)?#012)|((\\s)?#015#012)", "\\\n").replaceAll("(\\\\t)|(\\\\r)|(\\\\)|(\\\")", ""));
		} else {
			data.setOriginalSqlCommand(Constants.NOT_AVAILABLE);
		}
		return data;
	}

	/**
	 * parserSessionLocator() method will perform operation on Event event, set the
	 * expected value into respective SessionLocator Object and then return the
	 * value as response
	 * 
	 * @param event
	 * @return
	 */
	public static SessionLocator parseSessionLocator(Event event) {
		SessionLocator sessionLocator = new SessionLocator();
		String clientIp = event.getField(Constants.CLIENT_IP) != null
				? event.getField(Constants.CLIENT_IP).toString()
						.replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")|(   )", "").replaceAll("(\\s)?#012", " ")
				: Constants.DEFAULT_IP;
		String serverIp = event.getField(Constants.SERVER_IP) != null
				? event.getField(Constants.SERVER_IP).toString()
						.replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")|(   )", "").replaceAll("(\\s)?#012", " ")
				: Constants.DEFAULT_IP;
		sessionLocator.setIpv6(false);
		if (Util.isIPv6(serverIp) && Util.isIPv6(clientIp)) {
			sessionLocator.setIpv6(true);
			sessionLocator.setServerIpv6(serverIp);
			sessionLocator.setClientIpv6(clientIp);
			sessionLocator.setClientIp(Constants.UNKNOWN_STRING);
			sessionLocator.setServerIp(Constants.UNKNOWN_STRING);
		} else { // ipv4
			sessionLocator.setServerIp(serverIp);
			sessionLocator.setClientIp(clientIp);
			sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
			sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);
		}
		sessionLocator.setClientPort(Constants.DEFAULT_PORT);
		sessionLocator.setServerPort(Constants.DEFAULT_PORT);
		return sessionLocator;

	}

	/**
	 * Method to set the value into respective Exception Object and then return the
	 * value as response
	 * 
	 * @param event
	 * @return
	 */
	public static ExceptionRecord parseExceptionRecord(Event event) throws Exception {
		ExceptionRecord exception = new ExceptionRecord();
		if (event.getField(Constants.CATEGORY) != null
				&& event.getField(Constants.CATEGORY).toString().replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")", "")
						.replaceAll("(\\s)?#012", " ").replaceAll("\\s+", " ").equals("AUTH")) {
			exception.setExceptionTypeId(Constants.LOGIN_FAILED);
			exception.setDescription("Authentication failure for user " + getDbUser(event));
			exception.setSqlString(Constants.UNKNOWN_STRING);
		} else {
			exception.setExceptionTypeId(Constants.EXCEPTION_TYPE_SQL_ERROR_STRING);
			exception.setDescription("Error Occurred");
			exception.setSqlString(event.getField(Constants.SQL_QUERY) != null ? event.getField(Constants.SQL_QUERY)
					.toString().replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)|(\\\")", "").replaceAll("(\\s)?#012", " ")
					.replaceAll("\\s+", " ") : Constants.UNKNOWN_STRING);
		}
		return exception;
	}

}

