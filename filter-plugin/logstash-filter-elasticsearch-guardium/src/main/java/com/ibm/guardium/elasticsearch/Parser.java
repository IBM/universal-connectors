/*
Copyright IBM Corp. 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.elasticsearch;


import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonObject;
import com.ibm.guardium.elasticsearch.constant.ApplicationConstant;
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

public class Parser {

	private static Logger log = LogManager.getLogger(Parser.class);
	private static final DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
			.append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSSZ"));

	private static final DateTimeFormatter DATE_TIME_FORMATTER = dateTimeFormatterBuilder.toFormatter();
	/**
	 * Method to parse data from JsonObject and set to record object
	 * 
	 * @param data,totalOffset
	 * @return
	 * @throws Exception
	 */
	public static Record parseRecord(final JsonObject data,final Object totalOffset) throws Exception {
		Record record = new Record();
		try {
			record.setTime(parseTime(data,totalOffset));
			record.setAppUserName(ApplicationConstant.UNKNOWN_STRING);
			record.setDbName(ApplicationConstant.UNKNOWN_STRING);
			record.setSessionLocator(parseSessionLocator(data));
			record.setAccessor(parseAccessor(data));
			Integer hashCode = (record.getSessionLocator().getServerIp() + record.getSessionLocator().getClientIp()
					+ record.getSessionLocator().getClientPort() + record.getAccessor().getDbUser()).hashCode();
			record.setSessionId(hashCode.toString());
			if (data.has(ApplicationConstant.EVENTACTION_KEY)
					&& !data.get(ApplicationConstant.EVENTACTION_KEY).getAsString().isEmpty()
					&& data.get(ApplicationConstant.EVENTACTION_KEY).getAsString()
							.equalsIgnoreCase(ApplicationConstant.AUTHENTICATION_SUCCESS_STRING)) {
				record.setData(parseData(data, record));
			} else {
				record.setException(parseException(data));
			}
		} catch (Exception e) {
			log.error("Exception occurred while parsing event in parseRecord method:  ", e);
			throw new Exception("Exception occured while parsing event in parseData method: " + e.getMessage());
		}

		return record;
	}

	/**
	 * Method to get Time from the JsonObject, set the expected value into
	 * respective Time Object and then return the value as response
	 * 
	 * @param data,totalOffSet
	 * @return
	 * @throws ParseException 
	 */

	private static Time parseTime(JsonObject data,Object totalOffSet) throws ParseException {

		String dateString = data.get(ApplicationConstant.TIMESTAMP_KEY).getAsString();
		LocalDateTime localDateTime = LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
		localDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
		ZonedDateTime date = ZonedDateTime.parse(localDateTime.toString().concat("Z"), DateTimeFormatter.ISO_DATE_TIME);
		long millis = date.toInstant().toEpochMilli();
		int minOffset = date.getOffset().getTotalSeconds() / 60;
		Time time = new Time(millis, minOffset, 0);
		long t = (time.getTimstamp()) - (Long.parseLong(totalOffSet.toString()) * 60000);
		time.setTimstamp(t);
	    return time;
	}

	/**
	 * parseSessionLocator() method will perform operation on JsonObject data, set
	 * the expected value into respective SessionLocator Object and then return the
	 * value as response
	 * 
	 * @param data
	 * @return
	 */
	public static SessionLocator parseSessionLocator(JsonObject data) {
		SessionLocator sessionLocator = new SessionLocator();
		String clientIp = ApplicationConstant.DEFAULT_IP;
		int clientPort = ApplicationConstant.DEFAULT_PORT;
		if (data.has(ApplicationConstant.ORIGINADDRESS_KEY)
				&& !data.get(ApplicationConstant.ORIGINADDRESS_KEY).getAsString().isEmpty()) {

			String originAddress = data.get(ApplicationConstant.ORIGINADDRESS_KEY).getAsString();
			String clientIpPort[] = getClientIpPort(originAddress);
			clientIp = clientIpPort[0];
			clientPort = Integer.parseInt(clientIpPort[1]) ;
		}
		String serverIp = data.has(ApplicationConstant.HOSTIP_KEY)
				&& !data.get(ApplicationConstant.HOSTIP_KEY).getAsString().isEmpty()
						? data.get(ApplicationConstant.HOSTIP_KEY).getAsString()
						: ApplicationConstant.DEFAULT_IP;
		sessionLocator.setIpv6(false);
		if (Util.isIPv6(clientIp) && Util.isIPv6(serverIp)) {
			sessionLocator.setIpv6(true);
			sessionLocator.setClientIpv6(clientIp);
			sessionLocator.setClientIp(ApplicationConstant.UNKNOWN_STRING);
			sessionLocator.setServerIpv6(serverIp);
			sessionLocator.setServerIp(ApplicationConstant.UNKNOWN_STRING);
		} else { // ipv4
			sessionLocator.setServerIp(serverIp);
			sessionLocator.setClientIp(clientIp);
			sessionLocator.setClientIpv6(ApplicationConstant.UNKNOWN_STRING);
			sessionLocator.setServerIpv6(ApplicationConstant.UNKNOWN_STRING);
		}
		sessionLocator.setClientPort(clientPort);
		sessionLocator.setServerPort(ApplicationConstant.DEFAULT_PORT);
		return sessionLocator;
	}

	/**
	 * parseAccessor() method will perform operation on JsonObject data, set the
	 * expected value into respective Accessor Object and then return the value as
	 * response
	 * 
	 * @param data
	 * @return
	 */
	public static Accessor parseAccessor(JsonObject data) {

		Accessor accessor = new Accessor();
		accessor.setDbUser(data.has(ApplicationConstant.USERNAME_KEY)
				&& !data.get(ApplicationConstant.USERNAME_KEY).getAsString().isEmpty()
						? data.get(ApplicationConstant.USERNAME_KEY).getAsString()
						: ApplicationConstant.NOT_AVAILABLE);
		accessor.setServerHostName(data.has(ApplicationConstant.HOSTNAME_KEY)
				&& !data.get(ApplicationConstant.HOSTNAME_KEY).getAsString().isEmpty()
						? data.get(ApplicationConstant.HOSTNAME_KEY).getAsString()
						: ApplicationConstant.UNKNOWN_STRING);

		accessor.setDbProtocol(ApplicationConstant.DBPROTOCAL_STRING);
		accessor.setClientHostName(ApplicationConstant.UNKNOWN_STRING);
		accessor.setServiceName(ApplicationConstant.UNKNOWN_STRING);
		accessor.setLanguage(ApplicationConstant.LANGUAGE_STRING);
		accessor.setSourceProgram(ApplicationConstant.UNKNOWN_STRING);
		accessor.setServerType(ApplicationConstant.SERVER_TYPE_STRING);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
		accessor.setClient_mac(ApplicationConstant.UNKNOWN_STRING);
		accessor.setClientOs(ApplicationConstant.UNKNOWN_STRING);
		accessor.setCommProtocol(ApplicationConstant.UNKNOWN_STRING);
		accessor.setDbProtocolVersion(ApplicationConstant.UNKNOWN_STRING);
		accessor.setOsUser(ApplicationConstant.UNKNOWN_STRING);
		accessor.setServerDescription(ApplicationConstant.UNKNOWN_STRING);
		accessor.setServerOs(ApplicationConstant.UNKNOWN_STRING);
		return accessor;

	}

	/**
	 * parseData() method will perform operation on JsonObject data, set the
	 * expected value into respective Data Object and then return the value as
	 * response
	 * 
	 * @param jsonData,record
	 * @return
	 * @throws Exception
	 */
	public static Data parseData(final JsonObject jsonData, Record record) throws Exception {

		Data data = new Data();
		StringBuilder sb= new StringBuilder();
		sb.append("__AWS ").append("\"session_info\"" ).append(":{").append('"').append(ApplicationConstant.APP_USER_NAME_STRING).append('"')
				 .append(":").append('"').append(record.getAppUserName()).append('"').append(",").append('"')
				 .append(ApplicationConstant.SERVER_HOST_STRING).append('"').append(":").append('"').append(record.getAccessor().getServerHostName())
				 .append('"').append("} ").append(jsonData.get(ApplicationConstant.REQUESTMETHOD_KEY).getAsString()).append(" ")
                 .append(jsonData.get(ApplicationConstant.URLPATH_KEY).getAsString()).append(getUrlQueryParameter(jsonData))
				 .append(getRequestBody(jsonData));
		String originalSqlCommand=sb.toString().replaceAll("\r", "").replaceAll("\\r", "").replaceAll("\\n", " ").replaceAll("\\\\n", " ");
		data.setOriginalSqlCommand(originalSqlCommand);
		return data;
	}

	/**
	 * Method to set the value into respective Exception Object and then return the
	 * value as response
	 * 
	 * @param data
	 * @return
	 */
	public static ExceptionRecord parseException(JsonObject data) {
		ExceptionRecord exception = new ExceptionRecord();
		exception.setExceptionTypeId(ApplicationConstant.EXCEPTION_TYPE_AUTHENTICATION_STRING);
		exception.setDescription(ApplicationConstant.EXCEPTION_DESCRIPTION_AUTHENTICATION_STRING);
		exception.setSqlString(ApplicationConstant.NOT_AVAILABLE);
		return exception;
	}

	/**
	 * Method to get the URL Query Parameter from the JsonObject
	 * 
	 * @param jsonData
	 * @return
	 */
	public static String getUrlQueryParameter(JsonObject jsonData) {
		if (jsonData.has(ApplicationConstant.URLQUERY_KEY)
				&& !jsonData.get(ApplicationConstant.URLQUERY_KEY).getAsString().isEmpty()
				&& !jsonData.get(ApplicationConstant.URLQUERY_KEY).getAsString()
						.equalsIgnoreCase(ApplicationConstant.ERROR_TRACE_STRING)
				&& !jsonData.get(ApplicationConstant.URLQUERY_KEY).getAsString()
						.equalsIgnoreCase(ApplicationConstant.PRETTY_STRING)) {

			String queryParameter = jsonData.get(ApplicationConstant.URLQUERY_KEY).getAsString();
			if (queryParameter.contains(ApplicationConstant.PRETTY_STRING)) {
				String queryParameterArray[] = queryParameter.split("&pretty");
				return "?" + queryParameterArray[0].replace("&", "&&");
			} else {
				return "?" + queryParameter.replace("&", "&&");
			}
		} else {
			return ApplicationConstant.UNKNOWN_STRING;
		}
	}

	/**
	 * Method to get the Request Body from the JsonObject
	 * 
	 * @param jsonData
	 * @return
	 */
	public static String getRequestBody(JsonObject jsonData) {
		if (jsonData.has(ApplicationConstant.REQUESTBODY_KEY)
				&& !jsonData.get(ApplicationConstant.REQUESTBODY_KEY).getAsString().isEmpty()) {
			String requestBody="# ";
			requestBody = requestBody+jsonData.get(ApplicationConstant.REQUESTBODY_KEY).getAsString();
			String urlPath=jsonData.get(ApplicationConstant.URLPATH_KEY).getAsString();
			requestBody=urlPath.startsWith("/_sql")?requestBody.replace("'", "''"):requestBody;
			if (requestBody.contains("\"mode\":\"cli\"")) {
				String requestBodyArray[] = requestBody.split("mode");
				String desiredRequestBody = requestBodyArray[0];
				return desiredRequestBody.substring(0, desiredRequestBody.lastIndexOf(",")) + "}";
			} else {
				return requestBody;
			}

		} else {
			return ApplicationConstant.UNKNOWN_STRING;
		}
	}
	/**
	 * Method to get the Client Ip and Port from the String object
	 * 
	 * @param originAddress
	 * @return
	 */
	public static String[] getClientIpPort(String originAddress) {
		String[] clientIpPort = { originAddress.substring(0, originAddress.lastIndexOf(":")),
				originAddress.substring(originAddress.lastIndexOf(":") + 1) };
		if (clientIpPort[0].contains("::1")) {
			clientIpPort[0] = ApplicationConstant.DEFAULT_IP;
		}
		return clientIpPort;
	}
}
