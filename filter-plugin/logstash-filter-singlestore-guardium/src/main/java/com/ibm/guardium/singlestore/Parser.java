//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.singlestore;

import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.structures.*;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

	private static final Logger log = LogManager.getLogger(Parser.class);

	private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
			.appendPattern("yyyy-MM-dd HH:mm:ss.SSS")
			.optionalStart()
			.appendPattern("[XXX][X][Z]") // Handling different time zone offsets and UTC
			.optionalEnd()
			.toFormatter();

	// 	---------------------- Record----------------------------
	public static Record parseRecord(final JsonObject data) {
		try{
			if (data != null) {
				log.info("Start Parsing Record");

				Record record=Parser.getInitialRecord(data);
				// Data
				if (!data.get("message").getAsString().split(",")[5].equals("USER_LOGIN")){
					record.setData(Parser.parseData(data));
				}
				
				return record;
			}else
				return new Record();

		}catch (Exception e){
			log.error("SingleStore filter: Error parsing Record {}",e.getMessage());
			return new Record();
		}
	}

	// 	---------------------- ExceptionRecord-----------------------------
	public static Record parseExceptionRecord(final JsonObject data)  {
		try{
			log.info("Start Parsing ExceptionRecord");

			Record record=Parser.getInitialRecord(data);

			String message = data.get("message").getAsString();
			ExceptionRecord exceptionRecord = new ExceptionRecord();
			exceptionRecord.setExceptionTypeId(Constants.EXCEPTION_TYPE_LOGIN_FAILED_STRING);
			exceptionRecord.setDescription("Login Failed (" + message.split(",")[11] + ")");
			exceptionRecord.setSqlString(Constants.UNKNOWN_STRING);
			record.setException(exceptionRecord);

			return record;
		}catch (Exception e){
			log.error("SingleStore filter: Error parsing ExceptionRecord {}",e.getMessage());
			return new Record();
		}

	}

	// 	---------------------- Create Record with Time, session ....-------------------
	public static Record getInitialRecord(final JsonObject data) {

		Record record=new Record();

		record.setAppUserName(Constants.NOT_AVAILABLE);

		// DB Name
		String dbName = Constants.NOT_AVAILABLE;

		if (data.has(Constants.DB_NAME) && data.get(Constants.DB_NAME) != null) {

			dbName = data.get(Constants.DB_NAME).getAsString();
		}
		record.setDbName(dbName);

		// Time
		Time time = Parser.getTime(data);
		if (time != null) {
			record.setTime(time);
		}

		// SessionLocator
		record.setSessionLocator(Parser.parseSessionLocator(data));

		// Accessor
		record.setAccessor(Parser.parseAccessor(data,record.getDbName()));

		// SessionId
		record.setSessionId(Parser.parseSessionId());

		return record;
	}

	// 	---------------------- Timestamp-------------------
	public static Time getTime(final JsonObject data) {
		String dateString = null;
		if (data.has(Constants.TIMESTAMP)) {
			dateString = data.get(Constants.TIMESTAMP).getAsString();
		}

		if (dateString != null) {
			ZonedDateTime date = ZonedDateTime.parse(dateString, DATE_TIME_FORMATTER);
			long millis = date.toInstant().toEpochMilli();
			int minOffset = date.getOffset().getTotalSeconds() / 60;
			return new Time(millis, minOffset, 0);
		} else {
			return null;
		}

	}

	// 	---------------------- SessionLocator------------------------------
	public static SessionLocator parseSessionLocator(JsonObject data) {
		SessionLocator sessionLocator = new SessionLocator();

		// Get an `InetAddressValidator`
		InetAddressValidator validator = InetAddressValidator.getInstance();

		int clientPort = Constants.CLIENT_PORT_VALUE;
		int serverPort = Constants.SERVER_PORT_VALUE;
		String clientIpAdd = Constants.CLIENT_IP_VALUE;
		String serverIpAdd = Constants.SERVER_IP_VALUE;
		String clientIpv6Add = Constants.UNKNOWN_STRING;
		String serverIpv6Add = Constants.UNKNOWN_STRING;

		if (data.has(Constants.CLIENT_IP)) {
			String clientIp = data.get(Constants.CLIENT_IP).getAsString();

			if (validator.isValidInet4Address(clientIp)) {
				sessionLocator.setIpv6(false);
				clientIpAdd = clientIp;
			} else if (validator.isValidInet6Address(clientIp)) {
				sessionLocator.setIpv6(true);
				clientIpv6Add = clientIp;
			}
		}

		if (data.has(Constants.SERVER_IP)) {
			String serverIp = data.get(Constants.SERVER_IP).getAsString();
			if (validator.isValidInet4Address(serverIp)) {
				serverIpAdd = serverIp;
			} else if (validator.isValidInet6Address(serverIp)) {
				if (!sessionLocator.isIpv6()){
					sessionLocator.setIpv6(true);
					serverIpv6Add = serverIp;
				}
			}
		}

		if (data.has(Constants.SERVER_PORT)) {
			serverPort = Integer.parseInt(data.get(Constants.SERVER_PORT).getAsString());
		}


		sessionLocator.setClientIp(clientIpAdd);
		sessionLocator.setClientPort(clientPort);
		sessionLocator.setServerIp(serverIpAdd);
		sessionLocator.setServerPort(serverPort);

		sessionLocator.setClientIpv6(clientIpv6Add);
		sessionLocator.setServerIpv6(serverIpv6Add);

		return sessionLocator;
	}

	// 	---------------------- Session Id -----
	public static String parseSessionId() {
        return "";
	}

	// 	---------------------- Accessor--------------------------------------
	public static Accessor parseAccessor(JsonObject data,String dbName) {
		Accessor accessor = new Accessor();

		accessor.setDbProtocol(Constants.DB_PROTOCOL);
		accessor.setSourceProgram(Constants.UNKNOWN_STRING);
		accessor.setServerType(Constants.SERVER_TYPE_STRING);

		String dbUser = Constants.NOT_AVAILABLE;
		if (data.has(Constants.DB_USER) && data.get(Constants.DB_USER) != null) {
			dbUser = data.get(Constants.DB_USER).getAsString();
		}
		accessor.setDbUser(dbUser);

		if(data.has(Constants.SERVER_HOSTNAME) && data.get(Constants.SERVER_HOSTNAME) != null){
			accessor.setServerHostName(data.get(Constants.SERVER_HOSTNAME).getAsString());
		}

		accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);

		accessor.setClient_mac(Constants.UNKNOWN_STRING);
		accessor.setClientHostName(Constants.UNKNOWN_STRING);
		accessor.setClientOs(Constants.UNKNOWN_STRING);
		accessor.setCommProtocol(Constants.UNKNOWN_STRING);
		accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
		accessor.setOsUser(Constants.UNKNOWN_STRING);
		accessor.setServerDescription(Constants.UNKNOWN_STRING);
		accessor.setServerOs(Constants.UNKNOWN_STRING);
		accessor.setServiceName(dbName);

		return accessor;
	}


	public static Data parseData(JsonObject inputJSON) {
		Data data = new Data();
		Construct construct = new Construct();
		String originalQuery = inputJSON.get(Constants.QUERY_STATEMENT).toString();

		if(originalQuery.startsWith("\"")){
			originalQuery = originalQuery.substring(1);
		}
		if(originalQuery.endsWith("\"")){
			originalQuery = originalQuery.substring(0, originalQuery.length() - 1);
		}

		String query= Parser.cleanQuery(originalQuery);

		construct.setFullSql(originalQuery);

		if (construct.getFullSql() == null || construct.getFullSql().isEmpty()) {
			construct.setFullSql(Constants.UNKNOWN_STRING);
		}

		Sentence sentence = Parser.parseQuery(query);
        ArrayList<Sentence> sentenceList = new ArrayList<>();
        sentenceList.add(sentence);

        construct.setSentences(sentenceList);
        data.setConstruct(construct);

        return data;
	}

	// 	---------------------- Parse Query invoke functions to extract Command, Object and build sentence list--------------------------------
	public static Sentence parseQuery(String query) {

		if(query == null || query.isEmpty()){
			return new Sentence("");
		}

		Map.Entry<String, String> mapCommandObject= extractCommandAndObjectName(query);
		if(mapCommandObject != null){
			Sentence originalSentence=new Sentence(mapCommandObject.getKey());
			originalSentence.setObjects(buildSentenceObjectList(mapCommandObject.getValue()));
			return originalSentence;
		}
		return new Sentence("");
	}

	// 	---------------------- Build Sentence Object List--------------------------------
	private static ArrayList<SentenceObject> buildSentenceObjectList(String object) {

		ArrayList<SentenceObject> sentenceObjectList=new ArrayList<>();
		SentenceObject sentenceObject=new SentenceObject(object);
		sentenceObject.setType(Constants.TYPE);
		sentenceObjectList.add(sentenceObject);

		return sentenceObjectList;

	}

	// 	---------------------- Function to extract from the query Command & Object-------
	public static Map.Entry<String, String> extractCommandAndObjectName(String sql) {

		String ddlRegex = "(?i)^(CREATE\\s+(DATABASE|TABLE|INDEX|RESOURCE POOL|PIPELINE|FUNCTION|PROCEDURE|VIEW|LINK|ROLE))\\s+([\\w.]+)";
		String dmlRegex = "(?i)^(INSERT INTO|UPDATE|DELETE FROM|REPLACE INTO|LOAD DATA.*?INTO TABLE|MERGE INTO|TRUNCATE TABLE)\\s+([\\w.]+)";
		String selectRegex = "(?i)^(SELECT)\\s+.*?\\sFROM\\s+([\\w.]+)";
		String showRegex = "(?i)^(SHOW TABLE STATUS)\\s+FROM\\s+`?([\\w.]+)`?$";

		Pattern ddlPattern = Pattern.compile(ddlRegex);
		Matcher ddlMatcher = ddlPattern.matcher(sql);

		if (ddlMatcher.find()) {
			return new AbstractMap.SimpleEntry<>(ddlMatcher.group(1), ddlMatcher.group(3));
		}

		Pattern dmlPattern = Pattern.compile(dmlRegex);
		Matcher dmlMatcher = dmlPattern.matcher(sql);

		if (dmlMatcher.find()) {
			return new AbstractMap.SimpleEntry<>(dmlMatcher.group(1), dmlMatcher.group(2));
		}

		Pattern selectPattern = Pattern.compile(selectRegex);
		Matcher selectMatcher = selectPattern.matcher(sql);

		if (selectMatcher.find()) {
			return new AbstractMap.SimpleEntry<>(selectMatcher.group(1), selectMatcher.group(2));
		}

		Pattern showPattern = Pattern.compile(showRegex);
		Matcher showMatcher = showPattern.matcher(sql);

		if (showMatcher.find()) {
			return new AbstractMap.SimpleEntry<>(showMatcher.group(1), showMatcher.group(2));
		}

		if(sql.substring(0,6).equalsIgnoreCase("SELECT")){
			return new AbstractMap.SimpleEntry<>("SELECT", "");
		}
		return null;
	}

	// 	---------------------- Clean The Query --------
	public static String cleanQuery(String query) {

		if(query == null || query.isEmpty()){
			return "";
		}

		String regexObject = "OBJECT\\(\\)\\*/\\s*SELECT";
		String regex = "/\\*!\\d+\\s+";
		String NESTED_SELECT_REGEX = "(?i)(?<=SELECT\\s)(.*?\\((?:[^()]*|\\((?:[^()]*|\\([^()]*\\))*\\))*\\))";

		// Compile the pattern
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

		// Create a matcher for the input query
		Matcher matcher = pattern.matcher(query);

		if(matcher.find()){
			query = matcher.replaceAll("");
		}

		// Compile the pattern
		pattern = Pattern.compile(regexObject, Pattern.CASE_INSENSITIVE);

		// Create a matcher for the input query
		matcher = pattern.matcher(query);

		if(matcher.find()){
			query = matcher.replaceAll("SELECT");
		}

		query = query.replace("*/", "");
		query = query.replace("`", "");

		if(query.toUpperCase().startsWith("SELECT")){

			if(Parser.hasNestedSelectInFields(query)){
				int indexMainFrom = Parser.findMainFromIndexInQuery(query);
				query = query.substring(0,6) +" * " +query.substring(indexMainFrom);
			}
		}

		return query;
	}

	public static int findMainFromIndexInQuery(String query) {
		String lowerQuery = query.toLowerCase();
		int openParenCount = 0;

		for (int i = 0; i < lowerQuery.length(); i++) {
			char c = lowerQuery.charAt(i);

			if (c == '(') {
				openParenCount++;
			} else if (c == ')') {
				openParenCount--;
			} else if (lowerQuery.startsWith("from", i)) {
				// Check if 'from' is not within parentheses
				if (openParenCount == 0) {
					return i;
				}
			}
		}

		return -1; // No valid FROM found
	}

	public static boolean hasNestedSelectInFields(String query) {
		// Regex pattern to find nested SELECT statements within parentheses in the field part
		Pattern nestedSelectPattern = Pattern.compile("\\(\\s*select\\s+.*?\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher matcher = nestedSelectPattern.matcher(query);

        return matcher.find();
    }
}