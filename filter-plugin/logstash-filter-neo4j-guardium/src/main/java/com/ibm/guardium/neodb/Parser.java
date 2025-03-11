//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.neodb;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;

public class Parser {

	private static Logger log = LogManager.getLogger(Parser.class);

	private static final DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
			.append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS[[XXX][X]]"));

	private static final DateTimeFormatter DATE_TIME_FORMATTER = dateTimeFormatterBuilder.toFormatter();

	static LinkedHashMap<String, ArrayList<String>> operations = null;
	static LinkedHashMap<String, String> variables = null;
	static Character randomValue = 65;

	public static Record parseRecord(final JsonObject data) throws ParseException {
		Record record = new Record();

		if (data != null) {

			record.setAppUserName(Constants.NOT_AVAILABLE);

			// DB Name
			String dbName = Constants.NOT_AVAILABLE;

			if (data.has(Constants.DB_NAME) && data.get(Constants.DB_NAME) != null) {
				dbName = data.get(Constants.DB_NAME).getAsString();
			}

			record.setDbName(dbName);

			// Time
			String dateString = Parser.parseTimestamp(data);
			String timeZone = null;
			if (data.has(Constants.MIN_OFF)) {
				timeZone = data.get(Constants.MIN_OFF).getAsString();
			}
			Time time = Parser.getTime(dateString, timeZone);

			if (time != null) {
				record.setTime(time);
			}
			// SeessionLocator
			record.setSessionLocator(Parser.parseSessionLocator(data));

			// Accessor
			record.setAccessor(Parser.parseAccessor(data));

			Parser.parseSessionId(record);

			// Data
			if (data.get(Constants.LOG_LEVEL).toString().contains("INFO")) {
				record.setData(Parser.parseData(data));
			} else {
				record.setException(Parser.parseException(data));
			}

		}
		return record;
	}

// 	---------------------- Session Id -----------------------

	public static void parseSessionId(Record record) {

		Integer hashCode = (record.getSessionLocator().getClientIp() + record.getSessionLocator().getClientPort()
					+ record.getDbName()).hashCode();
		record.setSessionId(hashCode.toString());
	}

//	-----------------------------------------------Accessor-----------------

	public static Accessor parseAccessor(JsonObject data) {
		Accessor accessor = new Accessor();

		accessor.setDbProtocol(Constants.DATA_PROTOCOL_STRING);
		accessor.setServerType(Constants.SERVER_TYPE_STRING);

		String dbUser = Constants.NOT_AVAILABLE;
		if (data.has(Constants.DB_USER) && data.get(Constants.DB_USER) != null) {
			dbUser = data.get(Constants.DB_USER).getAsString();
		}
		accessor.setDbUser(dbUser);

		if(data.has(Constants.SERVER_HOSTNAME) && data.get(Constants.SERVER_HOSTNAME) != null)
			accessor.setServerHostName(data.get(Constants.SERVER_HOSTNAME).getAsString());

		String sourceProgram = Constants.UNKNOWN_STRING;
		if (data.has(Constants.SOURCE_PROGRAM) && data.get(Constants.SOURCE_PROGRAM) != null) {
			sourceProgram = data.get(Constants.SOURCE_PROGRAM).getAsString();
		}
		accessor.setSourceProgram(sourceProgram);

		accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);

		accessor.setClient_mac(Constants.UNKNOWN_STRING);
		accessor.setClientHostName(Constants.UNKNOWN_STRING);
		accessor.setClientOs(Constants.UNKNOWN_STRING);
		accessor.setCommProtocol(Constants.COMM_PROTOCOL);
		accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
		accessor.setOsUser(Constants.UNKNOWN_STRING);
		accessor.setServerDescription(Constants.UNKNOWN_STRING);
		accessor.setServerOs(Constants.UNKNOWN_STRING);
		accessor.setServiceName(Constants.UNKNOWN_STRING);

		return accessor;
	}

//	-----------------------------------------------SessionLocator-----------------

	public static SessionLocator parseSessionLocator(JsonObject data) {
		SessionLocator sessionLocator = new SessionLocator();
		sessionLocator.setIpv6(false);

		int clientPort = 0;
		int serverPort = 0;
		String clientIpAdd = Constants.NOT_AVAILABLE;
		String serverIpAdd = Constants.NOT_AVAILABLE;

		if (data.has(Constants.CLIENT_IP)) {
			String clientIpPortDetails = data.get(Constants.CLIENT_IP).getAsString();
			String clientIp = clientIpPortDetails.substring(7);
			String[] clientIpPort = clientIp.split(":");
			clientIpAdd = clientIpPort[0];
			clientPort = Integer.parseInt(clientIpPort[1]);
		}

		if (data.has(Constants.SERVER_IP)) {
			String serverIPPortDetails = data.get(Constants.SERVER_IP).getAsString();
			String serverIp = serverIPPortDetails.substring(7);
			String[] serverIpPort = serverIp.split(":");
			serverIpAdd = serverIpPort[0];
			serverPort = Integer.parseInt(serverIpPort[1]);
		}

		sessionLocator.setClientIp(clientIpAdd);
		sessionLocator.setClientPort(clientPort);
		sessionLocator.setServerIp(serverIpAdd);
		sessionLocator.setServerPort(serverPort);

		sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
		sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);

		return sessionLocator;
	}

//	-----------------------------------------------Timestamp-----------------

	public static String parseTimestamp(final JsonObject data) {
		String dateString = null;
		if (data.has(Constants.TIMESTAMP)) {
			dateString = data.get(Constants.TIMESTAMP).getAsString();
		}
		return dateString;
	}


	public static Time getTime(String dateString, String timeZone) {
		if (dateString != null) {
			JsonObject data = new JsonObject();
			LocalDateTime dt = LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
			ZoneOffset offset = ZoneOffset.of(ZoneOffset.UTC.getId());
			if (timeZone != null) {
				offset = ZoneOffset.of(timeZone);
			}
			ZonedDateTime zdt = dt.atOffset(offset).toZonedDateTime();
			long millis = zdt.toInstant().toEpochMilli();
			int minOffset = zdt.getOffset().getTotalSeconds() / 60;
			return new Time(millis, minOffset, 0);
		}
		return null;
	}

//  -----------------------------------------------Exception-----------------

	public static ExceptionRecord parseException(JsonObject data) {
		ExceptionRecord exceptionRecord = new ExceptionRecord();

		exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);

		String queryStatement = data.get(Constants.QUERY_STATEMENT).getAsString();
		String[] queryData = queryStatement.split("'} - ");

		String query = queryData[0].toString();
		String exceptionDes = queryData[1].toString();
		exceptionRecord.setDescription(exceptionDes);

		exceptionRecord.setSqlString(query);
		return exceptionRecord;
	}

//	-----------------------------------------------Data Construct----------------

	public static Data parseData(JsonObject inputJSON) {
		Data data = new Data();
		try {
			Construct construct = parseAsConstruct(inputJSON);
			if (construct != null) {
				data.setConstruct(construct);

				if (construct.getFullSql() == null || construct.getFullSql().isEmpty()) {
					construct.setFullSql(Constants.UNKNOWN_STRING);
				}
				if (construct.getRedactedSensitiveDataSql() == null || construct.redactedSensitiveDataSql.isEmpty()) {
					construct.setRedactedSensitiveDataSql(Constants.UNKNOWN_STRING);
				}
			}
		} catch (Exception e) {
			log.error("Neo4j filter: Error parsing JSon in parser" + inputJSON, e);
			throw e;
		}
		return data;
	}

	public static Construct parseAsConstruct(final JsonObject data) {
		try {
			final Sentence sentence = Parser.parseSentence(data);

			final Construct construct = new Construct();
			construct.sentences.add(sentence);

			String queryStatement = data.get(Constants.QUERY_STATEMENT).toString();
			String[] fullSql = queryStatement.split("runtime");
			String query[] =  fullSql[0].split("- \\{\\} - ");
			if(query[0].isEmpty())
				construct.setFullSql(fullSql[0].substring(1).trim());
			else
				construct.setFullSql(query[0].substring(1).trim());
			construct.setRedactedSensitiveDataSql(Parser.parseRedactedSensitiveDataSql(data));
			return construct;
		} catch (final Exception e) {
			throw e;
		}
	}

	protected static Sentence parseSentence(final JsonObject data) {

		Sentence sentence = null;

		if (data.has(Constants.QUERY_STATEMENT)) {
			String queryStatement = data.get(Constants.QUERY_STATEMENT).getAsString();

			sentence = parseQuery(queryStatement.trim());
			
			//For Match (n) return n
			if(sentence == null) {
				Sentence validSentence = new Sentence("MATCH");
				SentenceObject sentenceObject = new SentenceObject("AllNodesOrRelationships");
				sentenceObject.setType(Constants.TYPE); // In graph database, graphs are equivalent to tables in RDBMS

				validSentence.getObjects().add(sentenceObject);
				sentence = validSentence;
			}

		}

		return sentence;
	}

	public static Sentence parseQuery(String query) {

		ArrayList<String> gfg = new ArrayList<String>() {
			{
				add(Constants.ON_CREAT_ST);
				add(Constants.ON_MATC_ST);
				add(Constants.MATCH);
				add(Constants.CREATE);
				add(Constants.MERGE);
				add(Constants.DELETE);
				add(Constants.DETACH_DELET);
				add(Constants.REMOVE);
				add(Constants.SET);
				add(Constants.RETURN);
				add(Constants.WHERE);
				add(Constants.FOREACH);
			}
		};

		if (query.contains(Constants.ON_CREATE_SET))
			query = query.replace(Constants.ON_CREATE_SET, Constants.ON_CREAT_ST);
		if (query.contains(Constants.ON_MATCH_SET))
			query = query.replace(Constants.ON_MATCH_SET, Constants.ON_MATC_ST);
		if (query.contains(Constants.DETACH_DELETE))
			query = query.replace(Constants.DETACH_DELETE, Constants.DETACH_DELET);

		// find the index of all the supported operations in the query.
		Map<Integer, String> ops = new TreeMap<>();

		for (String operation : gfg) {

			int index = 0;

			while (true) {
				index = query.indexOf(operation, index);
				if (index == -1)
					break;
				ops.put(index, operation);
				index++;
			}
		}

		// split the main query into multiple queries using index.
		List<String> queries = new ArrayList<String>();

		int i = 0;
		int lastKeyValue = -1;
		for (Map.Entry<Integer, String> entry : ops.entrySet()) {
			if (i == 0) {
				i++;
				lastKeyValue = entry.getKey();
				continue;
			}

			String subquery = query.substring(lastKeyValue, entry.getKey());
			queries.add(subquery.trim());
			lastKeyValue = entry.getKey();

		}

		if (lastKeyValue != -1) {
			String subquery = query.substring(lastKeyValue);
			queries.add(subquery);
		}

		operations = new LinkedHashMap<String, ArrayList<String>>();
		variables = new LinkedHashMap<String, String>();

		for (String queryString : queries) {
			if (queryString.startsWith(Constants.RETURN) || queryString.startsWith(Constants.WHERE)
					|| queryString.startsWith(Constants.FOREACH))
				continue;
			else if (queryString.startsWith(Constants.ON_CREAT_ST))
				onCreateSet(queryString);
			else if (queryString.startsWith(Constants.ON_MATC_ST))
				onMatchSet(queryString);
			else if (queryString.startsWith(Constants.MATCH))
				match(queryString);
			else if (queryString.startsWith(Constants.CREATE))
				create(queryString);
			else if (queryString.startsWith(Constants.MERGE))
				merge(queryString);
			else if (queryString.startsWith(Constants.SET))
				set(queryString);
			else if (queryString.startsWith(Constants.DELETE))
				delete(queryString);
			else if (queryString.startsWith(Constants.REMOVE))
				remove(queryString);
			else if (queryString.startsWith(Constants.DETACH_DELET))
				detachDelete(queryString);
		}

		// Form the sentences out of different verb and objects

		// convert to ArrayList of key set
		List<String> alKeys = new ArrayList<String>(operations.keySet());

		SentenceObject sentenceObject = null;

		int j = 1;
		Sentence originalSentence = null;

		for (String keys : alKeys) {
			ArrayList<String> operation = operations.get(keys);

			for (String string : operation) {

				if(j == 1) {
					originalSentence = new Sentence(string);
					sentenceObject = new SentenceObject(variables.get(keys));
					if(sentenceObject.getName() == null)
						sentenceObject.setName(Constants.EVERYTHING);
					sentenceObject.setType(Constants.TYPE); // In graph database, graphs are equivalent to tables in RDBMS

					originalSentence.getObjects().add(sentenceObject);
					j++;
				}
				else {
					Sentence decendant = new Sentence(string);
					sentenceObject = new SentenceObject(variables.get(keys));
					if(sentenceObject.getName() == null)
						sentenceObject.setName(Constants.EVERYTHING);
					sentenceObject.setType(Constants.TYPE); // In graph database, graphs are equivalent to tables in RDBMS

					decendant.getObjects().add(sentenceObject);
					originalSentence.getDescendants().add(decendant);
				}
			}
		}

		return originalSentence;
	}

	private static void remove(String queryString) {

		ArrayList<String> operationPerfomed = new ArrayList<>();
		String arr[] = queryString.split(Constants.REMOVE);
		String alias = "";
		if (arr[1].contains(".")) {
			String arrs[] = arr[1].split("\\.", 2);
			alias = arrs[0].trim();
		} else if (arr[1].contains(":")) {
			String arrs[] = arr[1].split("\\:", 2);
			alias = arrs[0].trim();
		}

		if (operations.containsKey(alias)) {
			operationPerfomed = operations.get(alias);
			operations.remove(alias);
		}
		operationPerfomed.add(Constants.REMOVE);
		operations.put(alias, operationPerfomed);
	}

	private static void delete(String queryString) {

		ArrayList<String> operationPerfomed = new ArrayList<>();
		String arr[] = queryString.split(Constants.DELETE);
		String alias = arr[1].trim();
		if (alias.contains("-")) {
			String arrs[] = alias.split("-", 2);
			alias = arrs[0].trim();
		}
		if (operations.containsKey(alias)) {
			operationPerfomed = operations.get(alias);
			operations.remove(alias);
		}
		operationPerfomed.add(Constants.DELETE);
		operations.put(alias, operationPerfomed);
	}

	private static void detachDelete(String queryString) {

		ArrayList<String> operationPerfomed = new ArrayList<>();
		String arr[] = queryString.split(Constants.DETACH_DELET);
		String alias = arr[1].trim();
		if (alias.contains("-")) {
			String arrs[] = alias.split("-", 2);
			alias = arrs[0].trim();
		}
		if (operations.containsKey(alias)) {
			operationPerfomed = operations.get(alias);
			operations.remove(alias);
		}
		operationPerfomed.add(Constants.DETACH_DELETE);
		operations.put(alias, operationPerfomed);
	}

	private static void onMatchSet(String queryString) {

		ArrayList<String> operationPerfomed = new ArrayList<>();
		String arr[] = queryString.split(Constants.ON_MATC_ST);
		String arrs[] = arr[1].split("\\.", 2);
		String alias = arrs[0].trim();
		if (operations.containsKey(alias)) {
			operationPerfomed = operations.get(alias);
			operations.remove(alias);
		}
		operationPerfomed.add(Constants.ON_MATCH_SET);
		operations.put(alias, operationPerfomed);
	}

	private static void onCreateSet(String queryString) {

		ArrayList<String> operationPerfomed = new ArrayList<>();
		String arr[] = queryString.split(Constants.ON_CREAT_ST);
		String arrs[] = arr[1].split("\\.", 2);
		String alias = arrs[0].trim();
		if (operations.containsKey(alias)) {
			operationPerfomed = operations.get(alias);
			operations.remove(alias);
		}
		operationPerfomed.add(Constants.ON_CREATE_SET);
		operations.put(alias, operationPerfomed);

	}

	private static void set(String queryString) {

		ArrayList<String> operationPerfomed = new ArrayList<>();
		String arr[] = queryString.split(Constants.SET);
		String arrs[] = arr[1].split("\\.", 2);
		String alias = arrs[0].trim();
		if (operations.containsKey(alias)) {
			operationPerfomed = operations.get(alias);
			operations.remove(alias);
		}
		operationPerfomed.add(Constants.SET);
		operations.put(alias, operationPerfomed);
	}

	private static void match(String query) {

		String closingBracket = query;

		do {
			int roundIndex = closingBracket.indexOf("(");
			int squareIndex = closingBracket.indexOf("[");

			if (roundIndex != -1 && (roundIndex < squareIndex || squareIndex == -1))
				closingBracket = roundBracket(closingBracket, Constants.MATCH).trim();
			else if (squareIndex != -1 && (roundIndex > squareIndex || roundIndex == -1))
				closingBracket = squareBracket(closingBracket, Constants.MATCH).trim();
			else
				break;
		} while (!closingBracket.isEmpty());

	}

	private static void create(String query) {

		String closingBracket = query;

		do {
			int roundIndex = closingBracket.indexOf("(");
			int squareIndex = closingBracket.indexOf("[");

			if (roundIndex != -1 && (roundIndex < squareIndex || squareIndex == -1))
				closingBracket = roundBracket(closingBracket, Constants.CREATE).trim();
			else if (squareIndex != -1 && (roundIndex > squareIndex || roundIndex == -1))
				closingBracket = squareBracket(closingBracket, Constants.CREATE).trim();
			else
				break;
		} while (!closingBracket.isEmpty());

	}

	private static void merge(String query) {

		String closingBracket = query;

		do {
			int roundIndex = closingBracket.indexOf("(");
			int squareIndex = closingBracket.indexOf("[");

			if (roundIndex != -1 && (roundIndex < squareIndex || squareIndex == -1))
				closingBracket = roundBracket(closingBracket, Constants.MERGE).trim();
			else if (squareIndex != -1 && (roundIndex > squareIndex || roundIndex == -1))
				closingBracket = squareBracket(closingBracket, Constants.MERGE).trim();
			else
				break;
		} while (!closingBracket.isEmpty());

	}

	/*
	 * In order to fetch the alias and the node we use the indexing. On the
	 * basis of index, function/method is called. Variable map is used to store the alias
	 * and nodeName. Operation map is used to store the alias and operation
	 * performed.
	*/
	
	private static String squareBracket(String query, String operation) {

		String closingBracket = "";
		String value = "";
		String operationValue = operation;
		ArrayList<String> operationPerfomed = new ArrayList<String>();

		String[] arr = query.split("\\[", 2);

		String[] arrs = arr[1].split(":", 2);

		if (arrs.length == 2) {
			Pattern pattern = Pattern.compile("^[A-Za-z_|]+[\\sA-Za-z_|]*");
			Matcher matcher = pattern.matcher(arrs[1]);
			if (matcher.find()) {
				value = matcher.group(0);
			}

			// in order to get the alias only and remove all the extra characters.
			// EX : -[rel returns rel
			arrs[0] = arrs[0].replaceAll("[^A-Za-z]+", "");

			pattern = Pattern.compile("^[A-Za-z]+[\\sA-Za-z]*");
			matcher = pattern.matcher(arrs[0]);
			if (!matcher.find()) {
				arrs[0] = "" + randomValue;
				randomValue++;
			}

			if (operations.containsKey(arrs[0].trim())) {
				operationPerfomed = operations.get(arrs[0]);
				operationPerfomed.add(operationValue);
				operations.replace(arrs[0].trim(), operationPerfomed);
			} else {
				operationPerfomed.add(operationValue);
				operations.put(arrs[0].trim(), operationPerfomed);
			}

			variables.put(arrs[0].trim(), value.trim());

			String[] roundValue = arrs[1].split("\\]", 2);
			closingBracket = roundValue[1];
		}

		return closingBracket;

	}

	protected static String roundBracket(String query, String operation) {

		String closingBracket = "";
		String value = "";
		String operationValue = operation;
		ArrayList<String> operationPerfomed = new ArrayList<String>();

		String[] arr = query.split("\\(", 2);

		if (arr[1].indexOf(")") < arr[1].indexOf(":")) {
			String temp[] = arr[1].split("\\)", 2);
			arr[1] = temp[1];
		}

		String[] arrs = arr[1].split(":", 2);

		if (arrs.length == 2) {
			Pattern pattern = Pattern.compile("^[A-Za-z_]+[\\sA-Za-z_]*");
			Matcher matcher = pattern.matcher(arrs[1]);
			if (matcher.find()) {
				value = matcher.group(0);
			}

			// in order to get the alias only and remove all the extra characters.
			// EX : -[rel returns rel
			arrs[0] = arrs[0].replaceAll("[^A-Za-z]+", "");

			pattern = Pattern.compile("^[A-Za-z]+[\\sA-Za-z]*");
			matcher = pattern.matcher(arrs[0]);
			if (!matcher.find()) {
				arrs[0] = "" + randomValue;
				randomValue++;
			}

			if (operations.containsKey(arrs[0].trim())) {
				operationPerfomed = operations.get(arrs[0]);
				operations.remove(arrs[0]);
			}
			operationPerfomed.add(operationValue);
			operations.put(arrs[0].trim(), operationPerfomed);

			variables.put(arrs[0].trim(), value.trim());

			String[] roundValue = arrs[1].split("\\)", 2);
			closingBracket = roundValue[1];
		}

		return closingBracket;

	}

	protected static String parseRedactedSensitiveDataSql(JsonObject data) {
		String redactedData = "";
		redactedData = data.get(Constants.MESSAGE).getAsString();

		return redactedData;
	}

}