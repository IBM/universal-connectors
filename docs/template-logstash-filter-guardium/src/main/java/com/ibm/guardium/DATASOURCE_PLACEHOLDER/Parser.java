/*
Copyright IBM Corp. 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.DATASOURCE_PLACEHOLDER;

import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;

import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

/**
 * Parser class for converting [DATASOURCE_NAME] audit logs into Guardium Record format.
 * 
 * This parser supports both JSON and text-based log formats.
 * 
 * INSTRUCTIONS:
 * 1. Replace DATASOURCE_PLACEHOLDER with your data source name
 * 2. Determine if your logs are JSON, text, or mixed format
 * 3. Implement appropriate parsing methods for your log format
 * 4. Update field extraction logic to match your log structure
 * 5. Add helper methods as needed for your specific data source
 */
public class Parser {
	private static Logger log = LogManager.getLogger(Parser.class);

	/**
	 * Main method to parse audit log into a Guardium Record
	 * This method handles both JSON and text-based formats
	 * 
	 * @param input The input object (JsonObject for JSON logs, String for text logs)
	 * @return A Guardium Record object
	 * @throws Exception if parsing fails
	 */
	public static Record parseRecord(final Object input) throws Exception {
		
		if (log.isDebugEnabled()) {
			log.debug("Parsing event: {}", input);
		}
		
		Record record = new Record();
		
		try {
			// Determine input type and parse accordingly
			if (input instanceof JsonObject) {
				return parseJsonRecord((JsonObject) input);
			} else if (input instanceof String) {
				return parseTextRecord((String) input);
			} else {
				throw new Exception("Unsupported input type: " + input.getClass().getName());
			}
			
		} catch (Exception e) {
			log.error("Exception occurred while parsing event in parseRecord method: ", e);
			throw e;
		}
	}

	/**
	 * Parse JSON-formatted audit log
	 * 
	 * @param inputJSON The JSON object containing the audit log
	 * @return A Guardium Record object
	 * @throws Exception if parsing fails
	 */
	private static Record parseJsonRecord(final JsonObject inputJSON) throws Exception {
		Record record = new Record();
		
		// TODO: Extract and validate required fields from your JSON audit log
		// Example:
		// if (!inputJSON.has("timestamp")) {
		//     throw new Exception("Missing required field: timestamp");
		// }
		
		// 1. Parse and set the timestamp
		record.setTime(parseTimeFromJson(inputJSON));
		
		// 2. Extract and set database name
		String databaseName = extractDatabaseNameFromJson(inputJSON);
		record.setDbName(databaseName);
		
		// 3. Extract and set session ID
		String sessionId = extractSessionIdFromJson(inputJSON);
		record.setSessionId(sessionId);
		
		// 4. Extract and set app user name
		String appUserName = extractAppUserNameFromJson(inputJSON);
		record.setAppUserName(appUserName);
		
		// 5. Parse accessor (user and client information)
		record.setAccessor(parseAccessorFromJson(inputJSON));
		
		// 6. Parse session locator (network information)
		record.setSessionLocator(parseSessionLocatorFromJson(inputJSON));
		
		// 7. Check if this is an error/exception or successful operation
		if (isErrorFromJson(inputJSON)) {
			record.setException(parseExceptionFromJson(inputJSON));
		} else {
			record.setData(parseDataFromJson(inputJSON));
		}
		
		return record;
	}

	/**
	 * Parse text-formatted audit log
	 * 
	 * @param inputText The text string containing the audit log
	 * @return A Guardium Record object
	 * @throws Exception if parsing fails
	 */
	private static Record parseTextRecord(final String inputText) throws Exception {
		Record record = new Record();
		
		// TODO: Define regex patterns to extract fields from text logs
		// Example patterns:
		// Pattern timestampPattern = Pattern.compile("timestamp=(\\S+)");
		// Pattern userPattern = Pattern.compile("user=(\\S+)");
		// Pattern dbPattern = Pattern.compile("database=(\\S+)");
		
		// 1. Parse and set the timestamp
		record.setTime(parseTimeFromText(inputText));
		
		// 2. Extract and set database name
		String databaseName = extractDatabaseNameFromText(inputText);
		record.setDbName(databaseName);
		
		// 3. Extract and set session ID
		String sessionId = extractSessionIdFromText(inputText);
		record.setSessionId(sessionId);
		
		// 4. Extract and set app user name
		String appUserName = extractAppUserNameFromText(inputText);
		record.setAppUserName(appUserName);
		
		// 5. Parse accessor (user and client information)
		record.setAccessor(parseAccessorFromText(inputText));
		
		// 6. Parse session locator (network information)
		record.setSessionLocator(parseSessionLocatorFromText(inputText));
		
		// 7. Check if this is an error/exception or successful operation
		if (isErrorFromText(inputText)) {
			record.setException(parseExceptionFromText(inputText));
		} else {
			record.setData(parseDataFromText(inputText));
		}
		
		return record;
	}

	// ========== JSON Parsing Methods ==========

	private static Time parseTimeFromJson(JsonObject inputJSON) {
		// TODO: Extract timestamp field from your JSON audit log
		String dateString = inputJSON.get("timestamp").getAsString(); // Replace with your field name
		ZonedDateTime date = ZonedDateTime.parse(dateString);
		long millis = date.toInstant().toEpochMilli();
		int minOffset = date.getOffset().getTotalSeconds() / 60;
		return new Time(millis, minOffset, 0);
	}

	private static String extractDatabaseNameFromJson(JsonObject inputJSON) {
		// TODO: Implement logic to extract database name from JSON
		// The default value is N.A.
		return ApplicationConstants.NOT_AVAILABLE;
	}

	private static String extractSessionIdFromJson(JsonObject inputJSON) {
		// TODO: Implement logic to extract or generate session ID from JSON
		return "";
	}

	private static String extractAppUserNameFromJson(JsonObject inputJSON) {
		// TODO: Implement logic to extract app user name from JSON
		return "";
	}

	private static boolean isErrorFromJson(JsonObject inputJSON) {
		// TODO: Implement logic to detect errors in JSON
		return false;
	}

	private static ExceptionRecord parseExceptionFromJson(JsonObject inputJSON) {
		ExceptionRecord exception = new ExceptionRecord();
		// We have 2 types of exceptions here; SQL_ERROR and LOGIN_FAILED
		exception.setExceptionTypeId("EXCEPTION TYPE");
		exception.setDescription("Error DESCRIPTION");
		// If there is a query statement we need to set it here
		exception.setSqlString(ApplicationConstants.NOT_AVAILABLE);
		return exception;
	}

	private static Data parseDataFromJson(JsonObject inputJSON) throws Exception {
		// The goal of this method is to parse the query statement and extract useful information from it
		// For some of the query languages like MySql or MsSql there are available high performance Sniffer parsers available that we can use here instead of parsing the query in the universal connector
		// In the case of using a Sniffer parser, please read the README section about how to use sniffer parsers
		Data data = new Data();
		Construct construct = parseConstructFromJson(inputJSON);
		if (construct != null) {
			data.setConstruct(construct);
		}
		return data;
	}

	private static Construct parseConstructFromJson(JsonObject inputJSON) throws Exception {
		final Construct construct = new Construct();
		Sentence sentence = parseSentenceFromJson(inputJSON);
		construct.sentences.add(sentence);
		String fullSql = inputJSON.toString(); // Replace with actual query extraction
		construct.setFullSql(fullSql);
		construct.setRedactedSensitiveDataSql(fullSql);
		return construct;
	}

	private static Sentence parseSentenceFromJson(JsonObject inputJSON) {
		// Verb is the action in your query eg. SELECT or INSERT
		// OBJECTS are the objects you have in your statement eg. table1
		String verb = "VERB"; // TODO: Extract verb from JSON
		Sentence sentence = new Sentence(verb);
		SentenceObject sentenceObject = new SentenceObject("OBJECT");
		sentenceObject.setType(ApplicationConstants.COLLECTION);
		sentence.getObjects().add(sentenceObject);
		return sentence;
	}

	private static SessionLocator parseSessionLocatorFromJson(JsonObject inputJSON) {
		SessionLocator sessionLocator = new SessionLocator();
		String clientIp = ApplicationConstants.DEFAULT_IP; // TODO: Extract from JSON
		
		sessionLocator.setIpv6(Boolean.FALSE);
		if (Util.isIPv6(clientIp)) {
			sessionLocator.setIpv6(true);
			sessionLocator.setServerIpv6(ApplicationConstants.DEFAULT_IPV6);
			sessionLocator.setClientIpv6(clientIp);
			sessionLocator.setClientIp(clientIp);
		} else {
			sessionLocator.setServerIp(ApplicationConstants.DEFAULT_IP);
			sessionLocator.setClientIp(clientIp);
			sessionLocator.setClientIpv6(ApplicationConstants.UNKNOWN_STRING);
		}

		// Set the ports if possible
		sessionLocator.setServerPort(ApplicationConstants.PORT_DEFAULT);
		sessionLocator.setClientPort(ApplicationConstants.PORT_DEFAULT);
		return sessionLocator;
	}

	private static Accessor parseAccessorFromJson(JsonObject inputJSON) {
		Accessor accessor = new Accessor();
		accessor.setDbUser(ApplicationConstants.NOT_AVAILABLE); // TODO: Extract from JSON
		accessor.setServerHostName("DATASOURCE_SERVER"); // TODO: Extract from JSON
		accessor.setServerType(ApplicationConstants.SERVER_TYPE);
		accessor.setDbProtocol(ApplicationConstants.DATA_PROTOCOL);
		accessor.setDbProtocolVersion(ApplicationConstants.UNKNOWN_STRING);
		accessor.setSourceProgram(ApplicationConstants.UNKNOWN_STRING);
		accessor.setServerDescription(ApplicationConstants.UNKNOWN_STRING);
		accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);
		accessor.setClient_mac(ApplicationConstants.UNKNOWN_STRING);
		accessor.setClientHostName(ApplicationConstants.UNKNOWN_STRING);
		accessor.setCommProtocol(ApplicationConstants.UNKNOWN_STRING);
		accessor.setOsUser(ApplicationConstants.UNKNOWN_STRING);
		accessor.setClientOs(ApplicationConstants.UNKNOWN_STRING);
		accessor.setServerOs(ApplicationConstants.UNKNOWN_STRING);
		accessor.setServiceName(ApplicationConstants.UNKNOWN_STRING);
		return accessor;
	}

	// ========== Text Parsing Methods ==========

	private static Time parseTimeFromText(String inputText) {
		// TODO: Extract timestamp from text using regex
		// Example:
		// Pattern pattern = Pattern.compile("timestamp=(\\S+)");
		// Matcher matcher = pattern.matcher(inputText);
		// if (matcher.find()) {
		//     String dateString = matcher.group(1);
		//     ZonedDateTime date = ZonedDateTime.parse(dateString);
		//     long millis = date.toInstant().toEpochMilli();
		//     int minOffset = date.getOffset().getTotalSeconds() / 60;
		//     return new Time(millis, minOffset, 0);
		// }
		
		// Default: use current time
		long millis = System.currentTimeMillis();
		return new Time(millis, 0, 0);
	}

	private static String extractDatabaseNameFromText(String inputText) {
		// TODO: Extract database name using regex
		// Example:
		// Pattern pattern = Pattern.compile("database=(\\S+)");
		// Matcher matcher = pattern.matcher(inputText);
		// if (matcher.find()) {
		//     return matcher.group(1);
		// }
		// Default value is N.A.
		return ApplicationConstants.NOT_AVAILABLE;
	}

	private static String extractSessionIdFromText(String inputText) {
		// TODO: Extract or generate session ID from text
		return "";
	}

	private static String extractAppUserNameFromText(String inputText) {
		// TODO: Extract app user name using regex
		return "";
	}

	private static boolean isErrorFromText(String inputText) {
		// TODO: Detect errors in text logs
		// Example: check for keywords like "ERROR", "FAILED", "DENIED"
		return inputText.contains("ERROR") || inputText.contains("FAILED");
	}

	private static ExceptionRecord parseExceptionFromText(String inputText) {
		ExceptionRecord exception = new ExceptionRecord();
		// Exception type can be SQL_ERROR or LOGIN_FAILED
		exception.setExceptionTypeId("EXCEPTION TYPE");
		exception.setDescription("Error description");
		// Set the query statement if available
		exception.setSqlString("");
		return exception;
	}

	private static Data parseDataFromText(String inputText) throws Exception {
		Data data = new Data();
		Construct construct = parseConstructFromText(inputText);
		if (construct != null) {
			data.setConstruct(construct);
		}
		return data;
	}

	private static Construct parseConstructFromText(String inputText) throws Exception {
		final Construct construct = new Construct();
		Sentence sentence = parseSentenceFromText(inputText);
		construct.sentences.add(sentence);
		construct.setFullSql(inputText);
		construct.setRedactedSensitiveDataSql(inputText);
		return construct;
	}

	private static Sentence parseSentenceFromText(String inputText) {
		// TODO: Extract verb (operation) from text
		// Example: look for SQL keywords or operation names
		String verb = "OBJECT";
		Sentence sentence = new Sentence(verb);
		SentenceObject sentenceObject = new SentenceObject("VERB");
		sentenceObject.setType(ApplicationConstants.COLLECTION);
		sentence.getObjects().add(sentenceObject);
		return sentence;
	}

	private static SessionLocator parseSessionLocatorFromText(String inputText) {
		SessionLocator sessionLocator = new SessionLocator();
		
		// TODO: Extract client IP using regex
		// Example:
		// Pattern pattern = Pattern.compile("client_ip=(\\S+)");
		// Matcher matcher = pattern.matcher(inputText);
		// String clientIp = matcher.find() ? matcher.group(1) : ApplicationConstants.DEFAULT_IP;
		String clientIp = ApplicationConstants.DEFAULT_IP;
		
		sessionLocator.setIpv6(Boolean.FALSE);
		if (Util.isIPv6(clientIp)) {
			sessionLocator.setIpv6(true);
			sessionLocator.setServerIpv6(ApplicationConstants.DEFAULT_IPV6);
			sessionLocator.setClientIpv6(clientIp);
			sessionLocator.setClientIp(ApplicationConstants.UNKNOWN_STRING);
		} else {
			sessionLocator.setServerIp(ApplicationConstants.DEFAULT_IP);
			sessionLocator.setClientIp(clientIp);
			sessionLocator.setClientIpv6(ApplicationConstants.UNKNOWN_STRING);
		}
		
		sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);
		sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
		return sessionLocator;
	}

	private static Accessor parseAccessorFromText(String inputText) {
		Accessor accessor = new Accessor();
		
		// TODO: Extract user from text using regex
		accessor.setDbUser(ApplicationConstants.NOT_AVAILABLE);
		accessor.setServerHostName("DATASOURCE_SERVER");
		accessor.setServerType(ApplicationConstants.SERVER_TYPE);
		accessor.setDbProtocol(ApplicationConstants.DATA_PROTOCOL);
		accessor.setDbProtocolVersion(ApplicationConstants.UNKNOWN_STRING);
		accessor.setSourceProgram(ApplicationConstants.UNKNOWN_STRING);
		accessor.setServerDescription(ApplicationConstants.UNKNOWN_STRING);
		accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);
		accessor.setClient_mac(ApplicationConstants.UNKNOWN_STRING);
		accessor.setClientHostName(ApplicationConstants.UNKNOWN_STRING);
		accessor.setCommProtocol(ApplicationConstants.UNKNOWN_STRING);
		accessor.setOsUser(ApplicationConstants.UNKNOWN_STRING);
		accessor.setClientOs(ApplicationConstants.UNKNOWN_STRING);
		accessor.setServerOs(ApplicationConstants.UNKNOWN_STRING);
		accessor.setServiceName(ApplicationConstants.UNKNOWN_STRING);
		return accessor;
	}

	/**
	 * Helper method to extract value using regex pattern
	 * 
	 * @param input The input string
	 * @param pattern The regex pattern
	 * @param groupIndex The capture group index (usually 1)
	 * @return Extracted value or empty string if not found
	 */
	protected static String extractWithRegex(String input, String pattern, int groupIndex) {
		try {
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(input);
			if (m.find()) {
				return m.group(groupIndex);
			}
		} catch (Exception e) {
			log.error("Error extracting with regex pattern: " + pattern, e);
		}
		return StringUtils.EMPTY;
	}
}

