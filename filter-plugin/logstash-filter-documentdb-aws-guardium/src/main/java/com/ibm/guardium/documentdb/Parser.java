/*
© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.documentdb;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
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
	private Logger log = LogManager.getLogger(Parser.class);

	public static final String DATA_PROTOCOL_STRING = "DocumentDB";
	public static final String UNKOWN_STRING = "";
	public static final String SERVER_TYPE_STRING = "DocumentDB";
	public static final String COMPOUND_OBJECT_STRING = "[json-object]";
	public static final String EXCEPTION_TYPE_AUTHORIZATION_STRING = "SQL_ERROR";
	public static final String EXCEPTION_TYPE_AUTHENTICATION_STRING = "LOGIN_FAILED";
	public static final String NA_STRING = "N/A";
	public static final String DEFAULT_IP = "0.0.0.0";

	/**
	 * These arguments will not be redacted, as they only contain collection/field
	 * names rather than sensitive values.
	 */
	public static Set<String> REDACTION_IGNORE_STRINGS = new HashSet<>(
			Arrays.asList("from", "localField", "foreignField", "as", "connectFromField", "connectToField"));

	/**
	 * Helper method to remove all whitespace from a string More efficient than regex replaceAll for
	 * simple character removal
	 */
	private static String removeWhitespace(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (!Character.isWhitespace(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Helper method to extract database name from namespace string (e.g., "db.collection" -> "db")
	 * Uses indexOf instead of regex split for better performance
	 */
	private static String extractDbNameFromNs(String ns) {
		if (ns == null || ns.isEmpty()) {
			return UNKOWN_STRING;
		}
		int dotIndex = ns.indexOf('.');
		return dotIndex > 0 ? ns.substring(0, dotIndex) : ns;
	}

	/**
	 * Helper method to extract collection name from namespace string (e.g., "db.collection" ->
	 * "collection") Uses indexOf instead of regex split for better performance
	 */
	private static String extractCollectionFromNs(String ns) {
		if (ns == null || ns.isEmpty()) {
			return ns;
		}
		int dotIndex = ns.indexOf('.');
		return dotIndex > 0 ? ns.substring(dotIndex + 1) : ns;
	}

	/**
	 * Parses Audit logs and returns a Guard Record object
	 * @param data
	 * @return
	 * @throws ParseException
	 */
	public Record parseAuditRecord(final JsonObject data) throws ParseException {
		Record record = new Record();
		final JsonObject param = data.get("param").getAsJsonObject();
		if (param.get("error") != null) {
			param.get("error").getAsString();
		}

		// Setting db name
		String dbName = Parser.UNKOWN_STRING;
		if (param != null && param.has("ns")) {
			final String ns = param.get("ns").getAsString();
			dbName = extractDbNameFromNs(ns);
		}
		record.setDbName(dbName);

		record.setAppUserName(Parser.UNKOWN_STRING);

		// Setting sessionLocator object
		record.setSessionLocator(parseSessionLocatorDocumentDb(data));

		// Setting accessor
		record.setAccessor(parseAccessorDocumentDb(data));

		if(data.get("atype").getAsString().equalsIgnoreCase("authenticate")) {
			String result = param.get("error").getAsString();
			if (result.equals("0")) {
				record.setData(parseDataDocumentDb(data));
			} else {
				record.setException(parseException(data, result));
			}
		}else {
			record.setData(parseDataDocumentDb(data));
		}

		record.setSessionId(UNKOWN_STRING);
		// Setting timestamp
		String dateString = getTimestampStringDocumentDb(data);

		Time time = parseTimeDocumentDb(dateString);
		record.setTime(time);

		return record;
	}

	/**
	 * Parses Profiler logs and returns a Guard Record object
	 * @param data
	 * @return
	 * @throws ParseException
	 */
	public Record parseProfilerRecord(final JsonObject data) throws ParseException {
		Record record = new Record();

		// Setting db name
		String dbName = Parser.UNKOWN_STRING;

		if (data != null && data.has("ns")) {
			final String ns = data.get("ns").getAsString();
			dbName = extractDbNameFromNs(ns);
		}
		record.setDbName(dbName);

		record.setAppUserName(Parser.UNKOWN_STRING);

		// Setting sessionLocator object
		record.setSessionLocator(parseSessionLocatorDocumentDb(data));

		// Setting accessor
		record.setAccessor(parseAccessorDocumentDb(data));

		record.setData(parseDataDocumentDb(data));
		record.setSessionId(UNKOWN_STRING);

		// Setting timestamp
		String dateString = getTimestampStringDocumentDb(data);
		Time time = parseTimeDocumentDb(dateString);
		record.setTime(time);
		return record;
	}

	/**
	 * Converts timestamp jsonObject and returns a string
	 * @param data
	 * @return
	 */
	public String getTimestampStringDocumentDb(final JsonObject data) {
		String dateString = null;
		if (data.has("ts")) {
			dateString = data.get("ts").getAsString();
		}
		return dateString;
	}

	/**
	 * This method will parse timestamp String into Time object
	 * @param dateString
	 * @return
	 */
	public Time parseTimeDocumentDb(String dateString) {
		Date date = new java.util.Date(Long.parseLong(dateString));
		return new Time(date.getTime(), date.getTimezoneOffset(), 0);
	}

	/**
	 * Parses audit/profiler log JsonObject and returns Sentence 
	 * @param data
	 * @return
	 */
	protected Sentence parseSentenceDocumentDb(final JsonObject data) {

		Sentence sentence = null;
		// + main object
		String atype = "";
		if (data.has("atype")) {
			atype = data.get("atype").getAsString();
		}
		if (!atype.isEmpty()) {// Audit Logs
			JsonObject param = data.getAsJsonObject("param");
			sentence = new Sentence(atype);
			sentence.getObjects().add(parseSentenceObjectDocumentDbAudit(param,atype));
		} else {// Profiler logs
			final JsonObject command = data.get("command").getAsJsonObject();
			if (data.has("op") && data.get("op").getAsString().equals("update")
					|| data.has("op") && data.get("op").getAsString().equals("remove")) {
				String key = data.get("op").getAsString();
				sentence = new Sentence(key);
			} else {
				for (Iterator<Entry<String, JsonElement>> iterator = command.entrySet().iterator(); iterator.hasNext();) {
					String key = iterator.next().getKey();
					sentence = new Sentence(key);
					break;
				}
			}
			sentence.getObjects().add(parseSentenceObjectDocumentDbProfiler(data));
			
		}
		
		return sentence;
	}

	/**
	 * Parses JsonObject passed as argument and returns Sentence object
	 * @param command
	 * @return
	 */
	protected SentenceObject parseSentenceObjectDocumentDbAudit(JsonObject command, String aType) {
		SentenceObject sentenceObject;
		if(aType.equalsIgnoreCase("authenticate") && command.has("user")) {
			sentenceObject = new SentenceObject(command.get("user").getAsString());
		}else if(command.has("ns")) {
			sentenceObject = new SentenceObject(extractCollectionFromNs(command.get("ns").getAsString()));
		}else if(command.has("userName")){
			sentenceObject = new SentenceObject(command.get("userName").getAsString());
		}else if(aType.equalsIgnoreCase("createRole") && command.has("role")) {
			sentenceObject = new SentenceObject(command.get("role").getAsString());
		}else if(aType.equalsIgnoreCase("dropRole") && command.has("roleName")) {
			sentenceObject = new SentenceObject(command.get("roleName").getAsString());
		}else {
			sentenceObject = new SentenceObject(command.toString());
		}
		sentenceObject.setType("collection"); // this used to be default value, but since sentence is defined in

		return sentenceObject;
	}
	
	/**
	 * Parses JsonObject passed as argument and returns Sentence object
	 * @param command
	 * @return
	 */
	protected SentenceObject parseSentenceObjectDocumentDbProfiler(JsonObject command) {
		SentenceObject sentenceObject = new SentenceObject(UNKOWN_STRING);
		if (command != null && command.has("ns")) {
			sentenceObject = new SentenceObject(extractCollectionFromNs(command.get("ns").getAsString()));
		}
		else {
			sentenceObject = new SentenceObject(command.toString());
		}
		sentenceObject.setType("collection"); // this used to be default value, but since sentence is defined in

		return sentenceObject;
	}

	public Construct parseAsConstructDocumentDb(final JsonObject data) {
		try {
			final Sentence sentence = parseSentenceDocumentDb(data);
			final Construct construct = new Construct();
			construct.sentences.add(sentence);
			if(data.has("param")) {
				construct.setFullSql("\"atype\": "+data.get("atype").toString()+","+data.get("param"));
				construct.setRedactedSensitiveDataSql("\"atype\": "+data.get("atype").toString()+","+data.get("param"));
			}else if(data.has("command")) {
				construct.setFullSql(data.get("command").toString());
				construct.setRedactedSensitiveDataSql(data.get("command").toString());
			}
			return construct;
		} catch (final Exception e) {
			throw e;
		}
	}

	/**
	 * Parses the query and returns a Data instance.
	 * 
	 * @param inputJSON
	 * @return
	 * 
	 * @see Data
	 */
	public Data parseDataDocumentDb(JsonObject inputJSON) {
		Data data = new Data();
		try {
			Construct construct = parseAsConstructDocumentDb(inputJSON);
			if (construct != null) {
				data.setConstruct(construct);

				if (construct.getFullSql() == null) {
					construct.setFullSql(UNKOWN_STRING);
				}
				if (construct.getRedactedSensitiveDataSql() == null) {
					construct.setRedactedSensitiveDataSql(Parser.UNKOWN_STRING);
				}
			}
		} catch (Exception e) {
			log.error("DocumentDB filter: Error parsing JSon {}", inputJSON, e);
			throw e;
		}
		return data;
	}

	/**
	 * Creates an ExceptionRecord to be used in Record, instead of Data.
	 * 
	 * @param data
	 * @param resultCode
	 * @return
	 */
	private ExceptionRecord parseException(JsonObject data, String resultCode) {
		ExceptionRecord exceptionRecord = new ExceptionRecord();
		if (resultCode.equals("13")) {
			exceptionRecord.setExceptionTypeId(Parser.EXCEPTION_TYPE_AUTHORIZATION_STRING);
			exceptionRecord.setDescription("Unauthorized to perform the operation (13)");

		} else if (resultCode.equals("18")) {
			exceptionRecord.setExceptionTypeId(Parser.EXCEPTION_TYPE_AUTHENTICATION_STRING);
			exceptionRecord.setDescription("Authentication Failed (18)");
		} else { // prep for unknown error code
			exceptionRecord.setExceptionTypeId(Parser.EXCEPTION_TYPE_AUTHORIZATION_STRING);
			exceptionRecord.setDescription("Error (" + resultCode + ")");
		}

		exceptionRecord.setSqlString(data.getAsJsonObject("param").get("message").getAsString());
		return exceptionRecord;
	}

	/**
	 * Creates Accessor object to be used in Guard Record
	 * @param data
	 * @return
	 */
	public Accessor parseAccessorDocumentDb(JsonObject data) {
		Accessor accessor = new Accessor();



		accessor.setDbProtocol(Parser.DATA_PROTOCOL_STRING);
		accessor.setServerType(Parser.SERVER_TYPE_STRING);
		String dbUsers = NA_STRING;
		if (data.has("user")) {
			dbUsers = (data.get("user")==null || data.get("user").getAsString().isEmpty())?NA_STRING:data.get("user").getAsString();
		}if(data.has("param") && data.get("param").getAsJsonObject().has("user")) {
			String usr=data.get("param").getAsJsonObject().get("user").getAsString();
			dbUsers = (usr==null || usr.isEmpty())?NA_STRING:usr;
		}
		accessor.setDbUser(dbUsers);


		String sourceProgram = Parser.UNKOWN_STRING;
		if (data.has("appName")) {
			sourceProgram = removeWhitespace(data.get("appName").getAsString().trim());
		}
		accessor.setSourceProgram(sourceProgram);
		accessor.setServerHostName("documentdb.amazonaws.com");
		accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);
		accessor.setClient_mac(Parser.UNKOWN_STRING);
		accessor.setClientHostName(Parser.UNKOWN_STRING);
		accessor.setClientOs(Parser.UNKOWN_STRING);
		accessor.setCommProtocol(Parser.UNKOWN_STRING);
		accessor.setDbProtocolVersion(Parser.UNKOWN_STRING);
		accessor.setOsUser(Parser.UNKOWN_STRING);
		accessor.setServerDescription(Parser.UNKOWN_STRING);
		accessor.setServerOs(Parser.UNKOWN_STRING);

		String dbName = Parser.UNKOWN_STRING;

		if (data != null && data.has("ns")) {
			final String ns = data.get("ns").getAsString();
			dbName = ns.split("\\.")[0]; // sometimes contains "."; fallback OK.
		}
		accessor.setServiceName(dbName);

		return accessor;
	}

	/**
	 * Parses JSON object and returns session locator 
	 * @param data
	 * @return
	 */
	private SessionLocator parseSessionLocatorDocumentDb(JsonObject data) {
		SessionLocator sessionLocator = new SessionLocator();
		sessionLocator.setIpv6(false);

		sessionLocator.setClientIp(DEFAULT_IP); // Default value for Client IP
		sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
		sessionLocator.setClientIpv6(Parser.UNKOWN_STRING);

		String remote = data.has("client") ? data.get("client").getAsString()
				: (data.has("remote_ip") ? data.get("remote_ip").getAsString() : null);
		if (remote != null && remote.indexOf(':') > -1) {
			String[] remoteobjects = remote.split(":");
		
			if(remoteobjects.length>1) {
				sessionLocator.setClientIp(remoteobjects[0]);
				sessionLocator.setClientPort(Integer.parseInt(remoteobjects[1]));
			}else {
				sessionLocator.setClientIp(DEFAULT_IP);
				sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
			}
		}
		sessionLocator.setServerIp(DEFAULT_IP); // In AWS databases setting this field to 0.0.0.0
		sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);// In AWS databases setting this field to -1
		return sessionLocator;
	}
}