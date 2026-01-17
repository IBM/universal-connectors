/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.azuremysql;

import java.sql.Date;
import java.text.SimpleDateFormat;
import com.ibm.guardium.universalconnector.commons.Util;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.guardium.azuremysql.Constants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import co.elastic.logstash.api.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Parser {

	private static Logger log = LogManager.getLogger(Parser.class);

	/**
	 * Method to parse data from JsonObject and set to record object
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static Record parseRecord(final JsonObject data) throws Exception {
		Record record = new Record();
		try {
			JsonObject properties = data.get(Constants.PROPERTIES).getAsJsonObject();
			record.setSessionId(getSessionId(data, properties));
			record.setTime(parseTime(properties));
			record.setAppUserName(Constants.UNKNOWN_STRING);
			record.setSessionLocator(parseSessionLocator(properties));
			record.setDbName(getDbName(data, properties));
			record.setAccessor(parseAccessor(data, properties));
			if (properties.has(Constants.ERROR_CODE)
					&& properties.get(Constants.ERROR_CODE).getAsString().equals("0")) {
				record.setData(parseData(properties));
			} else if (properties.get(Constants.EVENT_CATEGORY).getAsString().equals("connection_log")) {

				record.setData(parseData(properties));

			} else {
				record.setException(parseExceptionRecord(properties));
			}
		} catch (Exception e) {
			log.error("Exception occurred while parsing event in parseRecord method:  ", e);
			throw new Exception("Exception occured while parsing event in parseData method: " + e.getMessage());
		}
		return record;
	}

	/**
	 * Method to get the time from JsonObject, set the expected value into
	 * respective Time Object and then return the value as response
	 * 
	 * @param Property
	 * @return
	 */
	public static Time parseTime(JsonObject Property) {
		if (Property.has(Constants.EVENT_CATEGORY)
				&& (Property.get(Constants.EVENT_CATEGORY).getAsString().equals("connection_log")
						|| Property.get(Constants.EVENT_CATEGORY).getAsString().equals("table_access_log"))) {
			LocalDateTime ldt = LocalDateTime.now();
			ZonedDateTime date = ldt.atZone(ZoneId.of("UTC"));
			long millis = date.toInstant().toEpochMilli();
			int minOffset = date.getOffset().getTotalSeconds() / 60;
			return new Time(millis, minOffset, 0);
		}
		String dateString = Property.get(Constants.TIMESTAMP).getAsString();
		ZonedDateTime date = ZonedDateTime.parse(dateString);
		long millis = date.toInstant().toEpochMilli();
		int minOffset = date.getOffset().getTotalSeconds() / 60;
		return new Time(millis, minOffset, 0);

	}

	/**
	 * Method to get the subscription ID from the JsonObject
	 * 
	 * @param data
	 * @return
	 */

	public static String getSubscriptionID(JsonObject data) {
		if (data.has(Constants.RESOURCE_ID) && !data.get(Constants.RESOURCE_ID).getAsString().isEmpty()) {
			String res = data.get(Constants.RESOURCE_ID).getAsString();
			String[] listWord = res.split("/");
			String SubscriptionID = listWord[2];
			return SubscriptionID;
		} else {
			return Constants.UNKNOWN_STRING;
		}

	}

	/**
	 * Method to get the server name from the JsonObject
	 * 
	 * @param data
	 * @param Property
	 * @return
	 */
	public static String getServerName(JsonObject data) {
		if (data.has(Constants.RESOURCE_ID) && !data.get(Constants.RESOURCE_ID).getAsString().isEmpty()) {
			String res = data.get(Constants.RESOURCE_ID).getAsString();
			String[] listWord = res.split("/");
			String ServerName = listWord[listWord.length - 1];
			return ServerName;
		} else {
			return Constants.UNKNOWN_STRING;
		}

	}

	/**
	 * Method to get the database name or the service name from the JsonObject
	 * 
	 * @param data
	 * @param Property
	 * @return
	 */
	public static String getDbName(JsonObject data, JsonObject Property) {

		String dbName = Constants.UNKNOWN_STRING;
		if (Property.has(Constants.DB_NAME) && Property.get(Constants.DB_NAME).getAsString().isEmpty()) {
			dbName = getSubscriptionID(data) + ":" + getServerName(data);
		} else if (Property.has(Constants.DB_NAME)) {
			dbName = getSubscriptionID(data) + ":" + getServerName(data) + ":"
					+ Property.get(Constants.DB_NAME).getAsString();
		} else {
			dbName = getSubscriptionID(data) + ":" + getServerName(data);
		}
		return dbName;
	}

	/**
	 * Method to get the server host name from the JsonObject
	 * 
	 * @param data
	 * @return
	 */
	public static String getServerHostName(JsonObject data) {

		String serverhostname = Constants.UNKNOWN_STRING;
		serverhostname = getSubscriptionID(data) + "-" + getServerName(data) + "." + "azure.com";
		return serverhostname;
	}

	/**
	 * Method to get the dbuser from the JsonObject
	 * 
	 * @param data
	 * @param Property
	 * @return
	 */
	public static String getDbUser(JsonObject data, JsonObject Property) {
		// StringBuffer user = new StringBuffer();
		if (Property.has(Constants.DB_USER) && !Property.get(Constants.DB_USER).getAsString().isEmpty()) {
			return Property.get(Constants.DB_USER).getAsString();
		} else {
			return Constants.NOT_AVAILABLE;
		}

	}

	/**
	 * Method to get the Session Id from the JsonObject
	 * 
	 * @param data
	 * @param Property
	 * @return
	 */
	public static String getSessionId(JsonObject data, JsonObject Property) {
		// here we write a hash code
		Integer hashcode = null;
		if (Property.has(Constants.DB_NAME) && Property.has(Constants.CLIENT_IP)
				&& !Property.get(Constants.CLIENT_IP).getAsString().isEmpty()) {
			hashcode = (Property.get(Constants.CLIENT_IP) + getServerName(data) + Property.get(Constants.DB_NAME))
					.hashCode();
		} else if (Property.has(Constants.CLIENT_IP) && !Property.get(Constants.CLIENT_IP).getAsString().isEmpty()) {
			hashcode = (Property.get(Constants.CLIENT_IP) + getServerName(data)).hashCode();
		} else {
			hashcode = (Constants.UNKNOWN_STRING + getServerName(data)).hashCode();
		}
		return hashcode.toString();
	}

	/**
	 * parseAccessor() method will perform operation on JsonObject data and
	 * JsonObject Property, set the expected value into respective Accessor Object
	 * and then return the value as response
	 * 
	 * @param data
	 * @param records
	 * @return
	 */

	public static Accessor parseAccessor(JsonObject data, JsonObject Property) {
		Accessor accessor = new Accessor();
		accessor.setClientHostName(Constants.UNKNOWN_STRING);
		accessor.setDbProtocol(Constants.DB_PROTOCOL);
		accessor.setClientHostName(Constants.UNKNOWN_STRING);
		accessor.setServiceName(getDbName(data, Property));
		accessor.setServerHostName(getServerHostName(data));
		accessor.setDbUser(getDbUser(data, Property));
		accessor.setLanguage(Constants.Language);
		accessor.setSourceProgram(Constants.UNKNOWN_STRING);
		accessor.setServerType(Constants.SERVER_TYPE);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
		accessor.setClient_mac(Constants.UNKNOWN_STRING);
		accessor.setClientOs(Constants.UNKNOWN_STRING);
		accessor.setCommProtocol(Constants.UNKNOWN_STRING);
		accessor.setDbProtocol(Constants.Language);
		accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
		accessor.setOsUser(Constants.UNKNOWN_STRING);
		accessor.setServerDescription(Constants.UNKNOWN_STRING);
		accessor.setServerOs(Constants.UNKNOWN_STRING);

		return accessor;
	}

	/**
	 * parseData() method will perform operation on JsonObject data, set the
	 * expected value into respective Data Object and then return the value as
	 * response
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static Data parseData(final JsonObject data) throws Exception {
		Data data1 = new Data();
		if (data.has(Constants.SQL_TEXT)) {
			data1.setOriginalSqlCommand(data.get(Constants.SQL_TEXT).getAsString());
		} else if (!data.has(Constants.SQL_TEXT) && data.has(Constants.EVENT_SUBCATEGORY)
				&& data.get(Constants.EVENT_CATEGORY).getAsString().equals("connection_log")) {
			data1.setOriginalSqlCommand(data.get(Constants.EVENT_SUBCATEGORY).getAsString());
		}
		return data1;
	}

	/**
	 * parserSessionLocator() method will perform operation on JsonObject data, set
	 * the expected value into respective SessionLocator Object and then return the
	 * value as response
	 * 
	 * @param data
	 * @return
	 */
	public static SessionLocator parseSessionLocator(JsonObject data) {
		SessionLocator sessionLocator = new SessionLocator();
		String clientIp = data.has(Constants.CLIENT_IP) ? data.get(Constants.CLIENT_IP).getAsString()
				: Constants.DEFAULT_IP;
		sessionLocator.setIpv6(false);
		if (Util.isIPv6(clientIp)) {
			sessionLocator.setIpv6(true);
			sessionLocator.setServerIpv6(Constants.DEFAULT_IPV6);
			sessionLocator.setClientIpv6(clientIp);
			sessionLocator.setClientIp(Constants.UNKNOWN_STRING);
		} else { // ipv4
			sessionLocator.setServerIp(Constants.DEFAULT_IP);
			sessionLocator.setClientIp(clientIp);
			sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
		}
		sessionLocator.setClientPort(Constants.DEFAULT_PORT);
		sessionLocator.setServerPort(Constants.DEFAULT_PORT);
		return sessionLocator;

	}

	/**
	 * Method to set the value into respective Exception Object and then return the
	 * value as response
	 * 
	 * @param data
	 * @return
	 */
	public static ExceptionRecord parseExceptionRecord(JsonObject data) throws Exception {
		ExceptionRecord exception = new ExceptionRecord();
		exception.setExceptionTypeId(Constants.EXCEPTION_TYPE_AUTHORIZATION_STRING);
		exception.setDescription(
				data.has(Constants.ERROR_CODE) ? "Error Occured (" + data.get(Constants.ERROR_CODE).getAsString() + ")"
						: "Error Occured (" + Constants.UNKNOWN_STRING + ")");
		exception.setSqlString(
				data.has(Constants.SQL_TEXT) ? data.get(Constants.SQL_TEXT).getAsString() : Constants.UNKNOWN_STRING);
		return exception;
	}

}
