/*
© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.documentdb;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

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

/**
 * Parser class for DocumentDB audit and profiler logs.
 * Converts DocumentDB log events into Guardium Record structures.
 */
public class Parser {
  private static final Logger log = LogManager.getLogger(Parser.class);

  // Deprecated constants - use Constants class instead
  @Deprecated
  public static final String DATA_PROTOCOL_STRING = Constants.DATA_PROTOCOL;
  @Deprecated
  public static final String UNKOWN_STRING = Constants.UNKNOWN_STRING;
  @Deprecated
  public static final String SERVER_TYPE_STRING = Constants.SERVER_TYPE;
  @Deprecated
  public static final String COMPOUND_OBJECT_STRING = Constants.COMPOUND_OBJECT;
  @Deprecated
  public static final String EXCEPTION_TYPE_AUTHORIZATION_STRING = Constants.EXCEPTION_TYPE_AUTHORIZATION;
  @Deprecated
  public static final String EXCEPTION_TYPE_AUTHENTICATION_STRING = Constants.EXCEPTION_TYPE_AUTHENTICATION;
  @Deprecated
  public static final String NA_STRING = Constants.NOT_AVAILABLE;
  @Deprecated
  public static final String DEFAULT_IP = Constants.DEFAULT_IP;
  @Deprecated
  public static final String DEFAULT_IPV6 = Constants.DEFAULT_IPV6;
  @Deprecated
  public static final String LOGIN_FAILED = Constants.EXCEPTION_TYPE_AUTHENTICATION;
  @Deprecated
  public static final String SQL_ERROR = Constants.EXCEPTION_TYPE_AUTHORIZATION;
  @Deprecated
  public static final String UC_PARSER_ERROR = Constants.UC_PARSER_ERROR;
  @Deprecated
  public static final String UC_AUDIT_ERROR = Constants.UC_AUDIT_ERROR;
  @Deprecated
  public static final java.util.Set<String> REDACTION_IGNORE_STRINGS = Constants.REDACTION_IGNORE_STRINGS;

  /**
   * Parses Audit logs and returns a Guard Record object
   *
   * @param data
   * @return
   * @throws ParseException
   */
  public Record parseAuditRecord(final JsonObject data) throws ParseException {
    String atype = data.get(Constants.FIELD_ATYPE).getAsString();
    // Handle authCheck events separately
    if (atype.equalsIgnoreCase(Constants.AUTH_TYPE_AUTHCHECK)) {
      return parseAuthCheckRecord(data);
    }
    
    Record record = new Record();
    final JsonObject param = data.get(Constants.FIELD_PARAM).getAsJsonObject();
    if (param.get(Constants.FIELD_ERROR) != null) {
      param.get(Constants.FIELD_ERROR).getAsString();
    }

    // Setting db name
    String dbName = Constants.UNKNOWN_STRING;
    if (param != null && param.has(Constants.FIELD_NS)) {
      final String ns = param.get(Constants.FIELD_NS).getAsString();
      dbName = StringUtils.extractDbNameFromNs(ns);
    }
    record.setDbName(dbName);

    record.setAppUserName(Constants.UNKNOWN_STRING);

    // Setting sessionLocator object
    record.setSessionLocator(parseSessionLocatorDocumentDb(data));

    // Setting accessor
    record.setAccessor(parseAccessorDocumentDb(data));

    if(atype.equalsIgnoreCase(Constants.AUTH_TYPE_AUTHENTICATE)) {
      String result = param.get(Constants.FIELD_ERROR).getAsString();
      if (result.equals(Constants.ERROR_CODE_SUCCESS)) {
        record.setData(parseDataDocumentDb(data));
      } else {
        record.setException(parseException(data, result));
      }
    }else {
      record.setData(parseDataDocumentDb(data));
    }

    record.setSessionId(Constants.UNKNOWN_STRING);
    // Setting timestamp
    String dateString = getTimestampStringDocumentDb(data);

    Time time = parseTimeDocumentDb(dateString);
    record.setTime(time);

    return record;
  }

  /**
   * Parses authCheck audit logs and returns a Guard Record object
   *
   * @param data
   * @return
   * @throws ParseException
   */
  public Record parseAuthCheckRecord(final JsonObject data) throws ParseException {
    Record record = new Record();
    
    // Get param object - cache to avoid multiple lookups
    JsonObject param = extractParamObject(data);

    // Setting db name from param.ns
    String dbName = Constants.UNKNOWN_STRING;
    if (param != null && param.has(Constants.FIELD_NS)) {
      final String ns = param.get(Constants.FIELD_NS).getAsString();
      dbName = StringUtils.extractDbNameFromNs(ns);
    }
    record.setDbName(dbName);

    // Extract app user name from users array at top level
    String appUserName = extractAppUserName(data);
    record.setAppUserName(appUserName);

    // Setting sessionLocator object
    SessionLocator sessionLocator = parseSessionLocatorAuthCheck(data);

    record.setSessionLocator(sessionLocator);

    // Setting accessor
    Accessor accessor = parseAccessorAuthCheck(data);

    record.setAccessor(accessor);

    // Setting data
    Data recordData = parseDataAuthCheck(data);
    record.setData(recordData);

    record.setSessionId(Constants.UNKNOWN_STRING);

    // Setting timestamp
    String dateString = getTimestampStringDocumentDb(data);
    Time time = parseTimeDocumentDb(dateString);

    record.setTime(time);
    return record;
  }

  /**
   * Extracts app user name from users array in authCheck events
   *
   * @param data
   * @return
   */
  private String extractAppUserName(JsonObject data) {
    if (!data.has("users")) {
      return Constants.UNKNOWN_STRING;
    }
    
    JsonElement usersElement = data.get("users");
    com.google.gson.JsonArray usersArray = null;
    
    if (usersElement.isJsonArray()) {
      usersArray = usersElement.getAsJsonArray();
    } else if (usersElement.isJsonPrimitive() && usersElement.getAsJsonPrimitive().isString()) {
      // users is a string (from stdin toString()), try to parse it
      try {
        usersArray = com.google.gson.JsonParser.parseString(usersElement.getAsString()).getAsJsonArray();
      } catch (Exception e) {
        // Parsing failed, return unknown
        return Constants.UNKNOWN_STRING;
      }
    }
    
    // Extract username from first user in array
    if (usersArray != null && usersArray.size() > 0) {
      JsonObject firstUser = usersArray.get(0).getAsJsonObject();
      if (firstUser.has(Constants.FIELD_USER)) {
        return firstUser.get(Constants.FIELD_USER).getAsString();
      }
    }
    
    return Constants.UNKNOWN_STRING;
  }

  /**
   * Parses the query and returns a Data instance for authCheck events
   *
   * @param inputJSON
   * @return
   */
  public Data parseDataAuthCheck(JsonObject inputJSON) {
    Data data = new Data();
    try {
      Construct construct = parseAsConstructAuthCheck(inputJSON);
      if (construct != null) {
        data.setConstruct(construct);
        if (construct.getFullSql() == null) {
          construct.setFullSql(Constants.UNKNOWN_STRING);
        }
        if (construct.getRedactedSensitiveDataSql() == null) {
          construct.setRedactedSensitiveDataSql(Constants.UNKNOWN_STRING);
        }

      }
    } catch (Exception e) {

      throw e;
    }
    return data;
  }

  /**
   * Parses authCheck event and returns Construct
   *
   * @param data
   * @return
   */
  public Construct parseAsConstructAuthCheck(final JsonObject data) {
    try {
      final Sentence sentence = parseSentenceAuthCheck(data);
      final Construct construct = new Construct();
      construct.sentences.add(sentence);
      String fullSql = "\"atype\": "+data.get(Constants.FIELD_ATYPE).toString()+","+data.get(Constants.FIELD_PARAM);
      construct.setFullSql(fullSql);
      construct.setRedactedSensitiveDataSql(fullSql);

      return construct;
    } catch (final Exception e) {

      throw e;
    }
  }

  /**
   * Parses authCheck audit log JsonObject and returns Sentence
   *
   * @param data
   * @return
   */
  protected Sentence parseSentenceAuthCheck(final JsonObject data) {
    JsonObject param = extractParamObject(data);
    String command = param.has(Constants.FIELD_COMMAND) ? param.get(Constants.FIELD_COMMAND).getAsString() : "authCheck";
    Sentence sentence = new Sentence(command);
    sentence.getObjects().add(parseSentenceObjectAuthCheck(param));
    return sentence;
  }

  /**
   * Extracts param object from data, handling different formats (JsonObject, String, or fallback to data)
   * This method centralizes param extraction logic to avoid code duplication
   *
   * @param data
   * @return JsonObject representing param
   */
  private JsonObject extractParamObject(final JsonObject data) {
    if (data.has(Constants.FIELD_PARAM) && !data.get(Constants.FIELD_PARAM).isJsonNull()) {
      JsonElement paramElement = data.get(Constants.FIELD_PARAM);
      if (paramElement.isJsonObject()) {
        return paramElement.getAsJsonObject();
      } else if (paramElement.isJsonPrimitive() && paramElement.getAsJsonPrimitive().isString()) {
        // param is a string (from stdin toString()), try to parse it
        try {
          return com.google.gson.JsonParser.parseString(paramElement.getAsString()).getAsJsonObject();
        } catch (Exception e) {
          return data;
        }
      }
    }
    // If no param field or invalid format, use data itself as fallback
    return data;
  }

  /**
   * Parses JsonObject passed as argument and returns SentenceObject for authCheck
   *
   * @param param
   * @return
   */
  protected SentenceObject parseSentenceObjectAuthCheck(JsonObject param) {
    SentenceObject sentenceObject;
    if(param.has(Constants.FIELD_NS)) {
      String collection = StringUtils.extractCollectionFromNs(param.get(Constants.FIELD_NS).getAsString());
      sentenceObject = new SentenceObject(collection);
    } else {

      sentenceObject = new SentenceObject(param.toString());
    }
    sentenceObject.setType("collection");
    return sentenceObject;
  }

  /**
   * Creates Accessor object for authCheck events
   *
   * @param data
   * @return
   */
  public Accessor parseAccessorAuthCheck(JsonObject data) {
    Accessor accessor = new Accessor();

    accessor.setDbProtocol(Constants.DATA_PROTOCOL);
    accessor.setServerType(Constants.SERVER_TYPE);
    
    // Get dbUser from users array - cache result to avoid duplicate extraction
    String dbUsers = extractAppUserName(data);
    accessor.setDbUser(dbUsers.isEmpty() ? Constants.NOT_AVAILABLE : dbUsers);

    // Extract source program
    String sourceProgram = Constants.UNKNOWN_STRING;
    if (data.has(Constants.FIELD_APP_NAME)) {
      sourceProgram = StringUtils.removeWhitespace(data.get(Constants.FIELD_APP_NAME).getAsString().trim());
    }
    accessor.setSourceProgram(sourceProgram);
    
    // Set static fields
    accessor.setServerHostName(Constants.DEFAULT_SERVER_HOSTNAME);
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

    // Extract and set service name (db name) - use centralized param extraction
    String dbName = Constants.UNKNOWN_STRING;
    JsonObject param = extractParamObject(data);
    if (param.has(Constants.FIELD_NS)) {
      dbName = StringUtils.extractDbNameFromNs(param.get(Constants.FIELD_NS).getAsString());
    }
    accessor.setServiceName(dbName);

    return accessor;
  }

  /**
   * Parses JSON object and returns session locator for authCheck events
   *
   * @param data
   * @return
   */
  private SessionLocator parseSessionLocatorAuthCheck(JsonObject data) {
    SessionLocator sessionLocator = new SessionLocator();
    sessionLocator.setIpv6(false);

    sessionLocator.setClientIp(Constants.DEFAULT_IP);
    sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
    sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);

    String remote = data.has(Constants.FIELD_REMOTE_IP) ? data.get(Constants.FIELD_REMOTE_IP).getAsString() : null;
    if (remote != null && remote.indexOf(':') > -1) {
      String[] remoteobjects = remote.split(":");
      if(remoteobjects.length > 1) {
        sessionLocator.setClientIp(remoteobjects[0]);
        // For authCheck, parse the port from remote_ip
        try {
          int port = Integer.parseInt(remoteobjects[1]);
          sessionLocator.setClientPort(port);
        } catch (NumberFormatException e) {

          sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
        }
      }
    }
    
    sessionLocator.setServerIp(Constants.DEFAULT_IP);
    sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);

    return sessionLocator;
  }

  /**
   * Parses Profiler logs and returns a Guard Record object
   *
   * @param data
   * @return
   * @throws ParseException
   */
  public Record parseProfilerRecord(final JsonObject data) throws ParseException {
  Record record = new Record();

  // Setting db name
  String dbName = Constants.UNKNOWN_STRING;

  if (data != null && data.has(Constants.FIELD_NS)) {
  	final String ns = data.get(Constants.FIELD_NS).getAsString();
      dbName = StringUtils.extractDbNameFromNs(ns);
  }
  record.setDbName(dbName);

  record.setAppUserName(Constants.UNKNOWN_STRING);

    // Setting sessionLocator object
    record.setSessionLocator(parseSessionLocatorDocumentDb(data));

    // Setting accessor
    record.setAccessor(parseAccessorDocumentDb(data));

    record.setData(parseDataDocumentDb(data));
    record.setSessionId(Constants.UNKNOWN_STRING);

    // Setting timestamp
    String dateString = getTimestampStringDocumentDb(data);
    Time time = parseTimeDocumentDb(dateString);
		record.setTime(time);
		return record;
	}

  /**
   * Converts timestamp jsonObject and returns a string
   *
   * @param data
   * @return
   */
  public String getTimestampStringDocumentDb(final JsonObject data) {
  String dateString = null;
  if (data.has(Constants.FIELD_TS)) {
  	dateString = data.get(Constants.FIELD_TS).getAsString();
  }
  return dateString;
 }

  /**
   * This method will parse timestamp String into Time object
   *
   * @param dateString
   * @return
   */
  public Time parseTimeDocumentDb(String dateString) {
		Date date = new java.util.Date(Long.parseLong(dateString));
		return new Time(date.getTime(), date.getTimezoneOffset(), 0);
	}

  /**
   * Parses audit/profiler log JsonObject and returns Sentence
   *
   * @param data
   * @return
   */
  protected Sentence parseSentenceDocumentDb(final JsonObject data) {

  Sentence sentence = null;
  // + main object
  String atype = "";
  if (data.has(Constants.FIELD_ATYPE)) {
  	atype = data.get(Constants.FIELD_ATYPE).getAsString();
  }
  if (!atype.isEmpty()) {// Audit Logs
  	JsonObject param = data.getAsJsonObject(Constants.FIELD_PARAM);
  	sentence = new Sentence(atype);
  	sentence.getObjects().add(parseSentenceObjectDocumentDbAudit(param,atype));
  } else {// Profiler logs
  	final JsonObject command = data.get(Constants.FIELD_COMMAND).getAsJsonObject();
  	if (data.has(Constants.FIELD_OP) && data.get(Constants.FIELD_OP).getAsString().equals(Constants.UPDATE_KEY)
  			|| data.has(Constants.FIELD_OP) && data.get(Constants.FIELD_OP).getAsString().equals(Constants.DELETE_KEY)) {
  		String key = data.get(Constants.FIELD_OP).getAsString();
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
   *
   * @param command
   * @return
   */
  protected SentenceObject parseSentenceObjectDocumentDbAudit(JsonObject command, String aType) {
  SentenceObject sentenceObject;
  if(aType.equalsIgnoreCase(Constants.AUTH_TYPE_AUTHENTICATE) && command.has(Constants.FIELD_USER)) {
  	sentenceObject = new SentenceObject(command.get(Constants.FIELD_USER).getAsString());
  }else if(command.has(Constants.FIELD_NS)) {
      sentenceObject = new SentenceObject(StringUtils.extractCollectionFromNs(command.get(Constants.FIELD_NS).getAsString()));
  }else if(command.has("userName")){
      sentenceObject = new SentenceObject(command.get("userName").getAsString());
  }else if(aType.equalsIgnoreCase(Constants.AUTH_TYPE_CREATE_ROLE) && command.has("role")) {
  	sentenceObject = new SentenceObject(command.get("role").getAsString());
  }else if(aType.equalsIgnoreCase(Constants.AUTH_TYPE_DROP_ROLE) && command.has("roleName")) {
  	sentenceObject = new SentenceObject(command.get("roleName").getAsString());
  }else {
  	sentenceObject = new SentenceObject(command.toString());
  }
  sentenceObject.setType("collection"); // this used to be default value, but since sentence is defined in

  return sentenceObject;
 }

  /**
   * Parses JsonObject passed as argument and returns Sentence object
   *
   * @param command
   * @return
   */
  protected SentenceObject parseSentenceObjectDocumentDbProfiler(JsonObject command) {
  SentenceObject sentenceObject = new SentenceObject(Constants.UNKNOWN_STRING);
  if (command != null && command.has(Constants.FIELD_NS)) {
      sentenceObject = new SentenceObject(StringUtils.extractCollectionFromNs(command.get(Constants.FIELD_NS).getAsString()));
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
  	if(data.has(Constants.FIELD_PARAM)) {
  		construct.setFullSql("\"atype\": "+data.get(Constants.FIELD_ATYPE).toString()+","+data.get(Constants.FIELD_PARAM));
  		construct.setRedactedSensitiveDataSql("\"atype\": "+data.get(Constants.FIELD_ATYPE).toString()+","+data.get(Constants.FIELD_PARAM));
  	}else if(data.has(Constants.FIELD_COMMAND)) {
  		construct.setFullSql(data.get(Constants.FIELD_COMMAND).toString());
  		construct.setRedactedSensitiveDataSql(data.get(Constants.FIELD_COMMAND).toString());
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
   * @see Data
   */
  public Data parseDataDocumentDb(JsonObject inputJSON) {
  Data data = new Data();
  try {
  	Construct construct = parseAsConstructDocumentDb(inputJSON);
  	if (construct != null) {
  		data.setConstruct(construct);

  		if (construct.getFullSql() == null) {
  			construct.setFullSql(Constants.UNKNOWN_STRING);
  		}
  		if (construct.getRedactedSensitiveDataSql() == null) {
  			construct.setRedactedSensitiveDataSql(Constants.UNKNOWN_STRING);
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
  if (resultCode.equals(Constants.ERROR_CODE_UNAUTHORIZED)) {
  	exceptionRecord.setExceptionTypeId(Constants.EXCEPTION_TYPE_AUTHORIZATION);
  	exceptionRecord.setDescription("Unauthorized to perform the operation (13)");

  } else if (resultCode.equals(Constants.ERROR_CODE_AUTH_FAILED)) {
  	exceptionRecord.setExceptionTypeId(Constants.EXCEPTION_TYPE_AUTHENTICATION);
  	exceptionRecord.setDescription("Authentication Failed (18)");
  } else { // prep for unknown error code
  	exceptionRecord.setExceptionTypeId(Constants.EXCEPTION_TYPE_AUTHORIZATION);
  	exceptionRecord.setDescription("Error (" + resultCode + ")");
  }

  exceptionRecord.setSqlString(data.getAsJsonObject(Constants.FIELD_PARAM).get(Constants.FIELD_MESSAGE).getAsString());
  return exceptionRecord;
 }

  /**
   * Creates Accessor object to be used in Guard Record
   *
   * @param data
   * @return
   */
  public Accessor parseAccessorDocumentDb(JsonObject data) {
  Accessor accessor = new Accessor();

  accessor.setDbProtocol(Constants.DATA_PROTOCOL);
  accessor.setServerType(Constants.SERVER_TYPE);
  String dbUsers = Constants.NOT_AVAILABLE;
  if (data.has(Constants.FIELD_USER)) {
  	dbUsers = (data.get(Constants.FIELD_USER)==null || data.get(Constants.FIELD_USER).getAsString().isEmpty())?Constants.NOT_AVAILABLE:data.get(Constants.FIELD_USER).getAsString();
  }if(data.has(Constants.FIELD_PARAM) && data.get(Constants.FIELD_PARAM).getAsJsonObject().has(Constants.FIELD_USER)) {
  	String usr=data.get(Constants.FIELD_PARAM).getAsJsonObject().get(Constants.FIELD_USER).getAsString();
  	dbUsers = (usr==null || usr.isEmpty())?Constants.NOT_AVAILABLE:usr;
  }
  accessor.setDbUser(dbUsers);

  String sourceProgram = Constants.UNKNOWN_STRING;
  if (data.has(Constants.FIELD_APP_NAME)) {
      sourceProgram = StringUtils.removeWhitespace(data.get(Constants.FIELD_APP_NAME).getAsString().trim());
  }
  accessor.setSourceProgram(sourceProgram);
  accessor.setServerHostName(Constants.DEFAULT_SERVER_HOSTNAME);
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

  String dbName = Constants.UNKNOWN_STRING;

  if (data != null && data.has(Constants.FIELD_NS)) {
  	final String ns = data.get(Constants.FIELD_NS).getAsString();
  	dbName = StringUtils.extractDbNameFromNs(ns);
  }
  accessor.setServiceName(dbName);

  return accessor;
 }

  /**
   * Parses JSON object and returns session locator
   *
   * @param data
   * @return
   */
  private SessionLocator parseSessionLocatorDocumentDb(JsonObject data) {
  SessionLocator sessionLocator = new SessionLocator();
  sessionLocator.setIpv6(false);

    sessionLocator.setClientIp(Constants.DEFAULT_IP); // Default value for Client IP
  sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
  sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);

  String remote = data.has(Constants.FIELD_CLIENT) ? data.get(Constants.FIELD_CLIENT).getAsString()
  		: (data.has(Constants.FIELD_REMOTE_IP) ? data.get(Constants.FIELD_REMOTE_IP).getAsString() : null);
  if (remote != null && remote.indexOf(':') > -1) {
  	String[] remoteobjects = remote.split(":");
  
  	if(remoteobjects.length>1) {
  		sessionLocator.setClientIp(remoteobjects[0]);
  	}
  	sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
  }
    sessionLocator.setServerIp(Constants.DEFAULT_IP); // In AWS databases setting this field to 0.0.0.0
  sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);// In AWS databases setting this field to -1
  return sessionLocator;
 }
  /**
   * Attempts to extract partial information from a malformed audit log JSON string.
   * This method uses lenient parsing to extract whatever fields are available,
   * even if the JSON is not fully valid. This helps provide better error context
   * in exception records.
   *
   * @param record The record to populate with extracted information
   * @param auditLogJsonString The potentially malformed JSON string
   */
  private void extractPartialInformation(Record record, String auditLogJsonString) {
    if (auditLogJsonString == null || auditLogJsonString.trim().isEmpty()) {
      return;
    }

    try {
      // Try lenient parsing - may succeed even with some malformed fields like invalid timestamp
      JsonObject partialJson = com.google.gson.JsonParser.parseString(auditLogJsonString).getAsJsonObject();
      
      // Check event type and use appropriate existing parsing method
      String atype = partialJson.has(Constants.FIELD_ATYPE)
          ? partialJson.get(Constants.FIELD_ATYPE).getAsString() : null;
      
      if (Constants.AUTH_TYPE_AUTHCHECK.equals(atype)) {
        // Use existing authCheck parsing methods
        record.setSessionLocator(parseSessionLocatorAuthCheck(partialJson));
        record.setAccessor(parseAccessorAuthCheck(partialJson));
        record.setAppUserName(extractAppUserName(partialJson));
        // Extract db name
        JsonObject param = extractParamObject(partialJson);
        if (param != null && param.has(Constants.FIELD_NS)) {
          record.setDbName(StringUtils.extractDbNameFromNs(param.get(Constants.FIELD_NS).getAsString()));
        }
      } else if (atype != null) {
        // Use existing audit parsing methods
        record.setSessionLocator(parseSessionLocatorDocumentDb(partialJson));
        record.setAccessor(parseAccessorDocumentDb(partialJson));
        // Extract db name from param.ns
        if (partialJson.has(Constants.FIELD_PARAM)) {
          JsonObject param = partialJson.getAsJsonObject(Constants.FIELD_PARAM);
          if (param.has(Constants.FIELD_NS)) {
            record.setDbName(StringUtils.extractDbNameFromNs(param.get(Constants.FIELD_NS).getAsString()));
          }
        }
      } else if (partialJson.has(Constants.FIELD_COMMAND)) {
        // Use existing profiler parsing methods
        record.setSessionLocator(parseSessionLocatorDocumentDb(partialJson));
        record.setAccessor(parseAccessorDocumentDb(partialJson));
        // Extract db name from ns
        if (partialJson.has(Constants.FIELD_NS)) {
          record.setDbName(StringUtils.extractDbNameFromNs(partialJson.get(Constants.FIELD_NS).getAsString()));
        }
      }
    } catch (Exception e) {
      // Silently fail - defaults will be used by buildExceptionRecord
      log.debug("Could not extract partial information from malformed log", e);
    }
  }


  private Record buildExceptionRecord(
      Record record, String error, String auditLogJsonString, String exceptionType) {
    if (record == null) {
      record = new Record();
      // Try to extract partial information from the malformed JSON
      extractPartialInformation(record, auditLogJsonString);
    }

    Data data = record.getData();
    ExceptionRecord exceptionRecord = new ExceptionRecord();

    // Set exception type
    exceptionRecord.setExceptionTypeId(exceptionType);

    // Determine SQL string based on whether data was partially parsed
    if (data != null
        && data.getOriginalSqlCommand() != null
        && !data.getOriginalSqlCommand().isEmpty()) {
      exceptionRecord.setSqlString(data.getOriginalSqlCommand());
    } else {
      // Truncate audit log string if too long to avoid memory issues
      String sqlString = auditLogJsonString;
      if (sqlString != null && sqlString.length() > Constants.MAX_SQL_STRING_LENGTH) {
        sqlString = StringUtils.truncate(sqlString, Constants.MAX_SQL_STRING_LENGTH, "... [truncated]");
      }
      exceptionRecord.setSqlString(sqlString != null ? sqlString : Constants.UNKNOWN_STRING);
    }

    // Clear data and set exception
    record.setData(null);
    exceptionRecord.setDescription(error != null ? error : "Unknown parsing error");
    record.setException(exceptionRecord);

    // Set default values for required fields
    record.setDbName(ValidationUtils.getValueOrDefault(record.getDbName(), Constants.NOT_AVAILABLE));
    record.setAppUserName(ValidationUtils.getValueOrDefault(record.getAppUserName(), Constants.NOT_AVAILABLE));
    record.setSessionId(ValidationUtils.getValueOrDefault(record.getSessionId(), Constants.UNKNOWN_STRING));

    // Ensure time is set (required field to avoid NullPointerException)
    if (record.getTime() == null) {
      record.setTime(new Time(System.currentTimeMillis(), 0, 0));
    }

    // Ensure accessor and session locator are properly initialized
    Accessor accessor = getAccessor(record);
    record.setAccessor(accessor);

    SessionLocator sessionLocator = getSessionLocator(record);
    record.setSessionLocator(sessionLocator);

    return record;
  }

  public Record parseRecordException(Record record, String error, String auditLogJsonString) {
    // For backward compatibility, determine exception type based on data availability
    Data data = record != null ? record.getData() : null;
    if (data != null
        && data.getOriginalSqlCommand() != null
        && !data.getOriginalSqlCommand().isEmpty()) {
      return buildExceptionRecord(record, error, auditLogJsonString, Constants.UC_PARSER_ERROR);
    } else {
      return buildExceptionRecord(record, error, auditLogJsonString, Constants.UC_AUDIT_ERROR);
    }
  }

  protected SessionLocator getSessionLocator(Record record) {
    SessionLocator sessionLocator = record.getSessionLocator();
    if (sessionLocator == null) {
      sessionLocator = new SessionLocator();
      sessionLocator.setIpv6(false);
    }

    sessionLocator.setClientIp(
        ValidationUtils.getValueOrDefault(sessionLocator.getClientIp(), Constants.DEFAULT_IP));
    sessionLocator.setClientIpv6(
        ValidationUtils.getValueOrDefault(sessionLocator.getClientIpv6(), Constants.DEFAULT_IPV6));
    sessionLocator.setServerIp(
        ValidationUtils.getValueOrDefault(sessionLocator.getServerIp(), Constants.DEFAULT_IP));
    sessionLocator.setServerIpv6(
        ValidationUtils.getValueOrDefault(sessionLocator.getServerIpv6(), Constants.DEFAULT_IPV6));
    sessionLocator.setIpv6(Boolean.TRUE.equals(sessionLocator.isIpv6()));
    sessionLocator.setServerPort(sessionLocator.getServerPort());
    sessionLocator.setClientPort(sessionLocator.getClientPort());
    return sessionLocator;
  }

  @Deprecated
  protected String getValueOrSetDefault(String value, String defaultValue) {
    return ValidationUtils.getValueOrDefault(value, defaultValue);
  }

  private Accessor getAccessor(Record record) {
    Accessor accessor = record.getAccessor();
    if (accessor == null) accessor = new Accessor();
    accessor.setDbUser(ValidationUtils.getValueOrDefault(accessor.getDbUser(), Constants.NOT_AVAILABLE));
    accessor.setDbProtocol(Constants.DATA_PROTOCOL);
    accessor.setServerType(Constants.SERVER_TYPE);
    accessor.setServerHostName(ValidationUtils.getValueOrDefault(accessor.getServerHostName(), Constants.NOT_AVAILABLE));
    accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
    accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);
    accessor.setServiceName(ValidationUtils.getValueOrDefault(accessor.getServiceName(), Constants.NOT_AVAILABLE));
    
    // Set all remaining null fields to UNKNOWN_STRING to avoid null values in JSON output
    accessor.setServerOs(ValidationUtils.getValueOrDefault(accessor.getServerOs(), Constants.UNKNOWN_STRING));
    accessor.setClientOs(ValidationUtils.getValueOrDefault(accessor.getClientOs(), Constants.UNKNOWN_STRING));
    accessor.setClientHostName(ValidationUtils.getValueOrDefault(accessor.getClientHostName(), Constants.UNKNOWN_STRING));
    accessor.setCommProtocol(ValidationUtils.getValueOrDefault(accessor.getCommProtocol(), Constants.UNKNOWN_STRING));
    accessor.setDbProtocolVersion(ValidationUtils.getValueOrDefault(accessor.getDbProtocolVersion(), Constants.UNKNOWN_STRING));
    accessor.setOsUser(ValidationUtils.getValueOrDefault(accessor.getOsUser(), Constants.UNKNOWN_STRING));
    accessor.setSourceProgram(ValidationUtils.getValueOrDefault(accessor.getSourceProgram(), Constants.UNKNOWN_STRING));
    accessor.setClient_mac(ValidationUtils.getValueOrDefault(accessor.getClient_mac(), Constants.UNKNOWN_STRING));
    accessor.setServerDescription(ValidationUtils.getValueOrDefault(accessor.getServerDescription(), Constants.UNKNOWN_STRING));
    
    return accessor;
  }
}