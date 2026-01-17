/*

© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0

*/
package com.ibm.guardium.firebase;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
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
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;

/**
 * Parser Class will perform operation on parsing events and messages from the
 * FireBase Realtime dataacess audit logs into a Guardium record instance
 * Guardium records include the accessor, the sessionLocator, data, and
 * exceptions. If there are no errors, the data contains details about the query
 * "construct"
 *
 * @className @Parser
 * 
 */
public class Parser {

	/**
	 * parseRecord() method will perform operation on JsonObject input, convert
	 * JsonObject into Record Object and then return the value as response
	 * 
	 * @param JsonObject inputJson
	 * @methodName @parseRecord
	 * @return Record GUARDIUM Object
	 */
	public static Record parseRecord(JsonObject inputJson) {
		Record record = new Record();

		JsonObject protoPayload = inputJson.get(ApplicationConstants.PROTO_PAYLOAD).getAsJsonObject();
		String databaseId = getDatabaseName(protoPayload);
		String projectId = getProjectId(inputJson);
		String serverHostName = getFieldValueByKey(protoPayload, ApplicationConstants.SERVICE_NAME);
		serverHostName = projectId.concat("_").concat(serverHostName);
		String sql = getFullSQLQueryFromEvent(protoPayload);
		String appUserName = getAppUserName(protoPayload);
		record.setSessionId(getSessionId(appUserName, databaseId, projectId));
		record.setDbName(databaseId.equals("")?projectId:projectId.concat(":".concat(databaseId)));
		record.setAppUserName(appUserName);
		record.setAccessor(parseAccessor(appUserName, serverHostName, protoPayload, projectId));
		record.getAccessor().setServiceName(record.getDbName());
		record.setSessionLocator(parserSessionLocator(inputJson));
		record.setTime(parseTime(getFieldValueByKey(inputJson, ApplicationConstants.TIMESTAMP)));
		record.setException(parseException(sql, inputJson));

		if (null == record.getException()) {
			String verb = getVerbFromProtopayload(protoPayload);
			record.setData(parseData(sql, verb, databaseId));
		}
		return record;
	}
	
	private static String getSessionId(String appUserName, String databaseId, String projectId) {
		Integer sessionId = (appUserName + (projectId.concat(":".concat(databaseId)))).hashCode();
		return sessionId.toString();
	}



	/**
	 *
	 * getFullSQLQueryFromEvent() method will perform operation on JsonObject
	 * protoPayload, for getting the SQL value as a string using different Operation
	 * then return the value as response
	 * 
	 * @param JsonObject inputJson
	 * @methodName @getFullSQLQueryFromEvent
	 * @return String query Object
	 * 
	 */
	private static String getFullSQLQueryFromEvent(JsonObject protoPayload) {
		if (!protoPayload.has(ApplicationConstants.REQUEST)) {
			return StringUtils.EMPTY;
		}
		return String.valueOf(protoPayload.get(ApplicationConstants.REQUEST).getAsJsonObject());
	}

	/**
	 *
	 * getVerbFromProtopayload() method will perform operation on JsonObject
	 * protoPayload, for getting the Verb value as a string using different
	 * Operation then return the value as response
	 * 
	 * @param JsonObject inputJson
	 * @methodName @getVerbFromProtopayload
	 * @return String protoPayload Object
	 * 
	 */
	private static String getVerbFromProtopayload(JsonObject protoPayload) {
		String verb = StringUtils.EMPTY;
		if (!protoPayload.has(ApplicationConstants.REQUEST))
			return verb;

		JsonObject requestJsonObject = protoPayload.get(ApplicationConstants.REQUEST).getAsJsonObject();
		if (requestJsonObject.has(ApplicationConstants.TYPE)) {
			verb = CommonUtils.convertIntoString(requestJsonObject.get(ApplicationConstants.TYPE));
		}

		if (verb.contains("/")) {
			String[] array = verb.split("/");
			verb = String.valueOf(array[array.length - 1]);
			if (verb.contains(".")) {
				int lestIndex = verb.lastIndexOf('.');
				verb = verb.substring(lestIndex + 1, verb.length());
			}
		}
		return verb;
	}

	/**
	 *
	 * getDatabaseName() method will perform operation on JsonObject protoPayload,
	 * for getting the databaseName value as a string using different Operation then
	 * return the value as response
	 * 
	 * @param JsonObject inputJson
	 * @methodName @getDatabaseName
	 * @return String query Object
	 * 
	 */
	private static String getDatabaseName(JsonObject protoPayload) {
		String databaseName = StringUtils.EMPTY;
		if (!protoPayload.has(ApplicationConstants.REQUEST)) {
			return databaseName;
		}
		JsonObject requestJson = protoPayload.get(ApplicationConstants.REQUEST).getAsJsonObject();

		if (requestJson.has(ApplicationConstants.DATABASE_ID)) {
			databaseName = CommonUtils.convertIntoString(requestJson.get(ApplicationConstants.DATABASE_ID));
		}

		if (requestJson.has(ApplicationConstants.NAME)) {
			databaseName = CommonUtils.convertIntoString(requestJson.get(ApplicationConstants.NAME));
		}

		if (databaseName.contains("/")) {
			String[] array = databaseName.split("/");
			databaseName = String.valueOf(array[array.length - 1]);
		}
		return databaseName;
	}

	/**
	 * parseAccessor() method will perform operation on String inputs, set the
	 * expected value into respective Accessor Object and then return the value as
	 * response
	 * 
	 * @param serverHostName
	 * @param projectId
	 * @param String         appUserName
	 * @param JsonObject     protoPayload
	 * @methodName @parseAccessor
	 * @return Accessor GUARDIUM Object
	 * 
	 */
	private static Accessor parseAccessor(String appUserName, String serverHostName, JsonObject protoPayload,
			String projectId) {
		Accessor accessor = new Accessor();
		String callerSuppliedUserAgent = getCallerSuppliedUserAgent(protoPayload);

		accessor.setServerType(ApplicationConstants.SERVER_TYPE_STRING);
		accessor.setServerOs(ApplicationConstants.UNKOWN_STRING);

		accessor.setClientOs(callerSuppliedUserAgent);
		accessor.setClientHostName(ApplicationConstants.UNKOWN_STRING);

		accessor.setServerHostName(serverHostName);
		accessor.setCommProtocol(ApplicationConstants.UNKOWN_STRING);

		accessor.setDbProtocol(ApplicationConstants.DATA_PROTOCOL_STRING);
		accessor.setDbProtocolVersion(ApplicationConstants.UNKOWN_STRING);

		accessor.setOsUser(ApplicationConstants.UNKOWN_STRING);
		accessor.setSourceProgram(ApplicationConstants.UNKOWN_STRING);

		accessor.setClient_mac(ApplicationConstants.UNKOWN_STRING);
		accessor.setServerDescription(ApplicationConstants.UNKOWN_STRING);

		accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);

		accessor.setDbUser(appUserName);
		
		

		return accessor;
	}

	/**
	 * getCallerSuppliedUserAgent() method will perform operation on JsonObject
	 * inputJson object convert into callerSuppliedUserAgent and return response
	 * 
	 * @param JsonObject protoPayloadJsonObject
	 * @methodName @getCallerSuppliedUserAgent
	 * @return String
	 * 
	 */
	private static String getCallerSuppliedUserAgent(JsonObject protoPayloadJsonObject) {
		JsonObject requestMetadataIntoJSON = validateKeyExistance(protoPayloadJsonObject,
				ApplicationConstants.REQUEST_METADATA);
		if (requestMetadataIntoJSON.has(ApplicationConstants.CALLER_SUPPLIED_USER_AGENT)) {
			return CommonUtils
					.convertIntoString(requestMetadataIntoJSON.get(ApplicationConstants.CALLER_SUPPLIED_USER_AGENT));
		}
		return StringUtils.EMPTY;
	}

	/**
	 * parserSessionLocator() method will perform operation on String input, set
	 * the expected value into respective SessionLocator Object and then return the
	 * value as response
	 * 
	 * @param JsonObject inputJson
	 * @methodName @parserSessionLocator
	 * @return SessionLocator GUARDIUM Object
	 * 
	 */
	private static SessionLocator parserSessionLocator(JsonObject inputJson) {
		JsonObject protoPayload = inputJson.get(ApplicationConstants.PROTO_PAYLOAD).getAsJsonObject();
		String callerIp = getCallerIp(protoPayload);

		SessionLocator sessionLocator = new SessionLocator();
		sessionLocator.setIpv6(false);

		if (Util.isIPv6(callerIp)) {
			sessionLocator.setIpv6(true);
			sessionLocator.setClientIpv6(callerIp);
			sessionLocator.setServerIpv6(ApplicationConstants.DEFAULT_IPV6);

			} else {
			sessionLocator.setClientIp(callerIp);
			sessionLocator.setServerIp(ApplicationConstants.DEFAULT_IPV4);
		}

		sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
		sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);
		

		if (inputJson.has(ApplicationConstants.HTTP_REQUEST)) {
			JsonObject httpJson = inputJson.get(ApplicationConstants.HTTP_REQUEST).getAsJsonObject();
			String serverIp = CommonUtils.convertIntoString(httpJson.get(ApplicationConstants.SERVER_IP));
			serverIp=!StringUtils.isEmpty(serverIp)?serverIp:"0.0.0.0";
			sessionLocator.setServerIp(serverIp);
		}

		return sessionLocator;
	}

	/**
	 * parseData() method will perform operation on String input, set the expected
	 * value into respective Data Object and then return the value as response
	 * 
	 * @param object
	 * 
	 * @param protoPayload
	 * 
	 * @param JsonObject   protoPayloadJsonObject
	 * @methodName @parseData
	 * @return Data GUARDIUM Object
	 * 
	 */
	private static Data parseData(String sql, String statementType, String object) {
		Data data = new Data();
		data.setConstruct(parseAsConstruct(sql, statementType, object));
		return data;
	}

	/**
	 * parseAsConstruct() method will perform operation on String input, set the
	 * expected value into respective Construct Object and then return the value as
	 * response
	 * 
	 * @param object
	 * 
	 * @param protoPayload
	 * 
	 * @param String       sql
	 * @methodName @parseAsConstruct
	 * @return Construct GUARDIUM Object
	 * 
	 */
	private static Construct parseAsConstruct(String sql, String statementType, String object) {
		final Sentence sentence = parseSentence(sql, statementType, object);
		final Construct construct = new Construct();
		construct.setFullSql(sql);
		construct.setRedactedSensitiveDataSql(sql);
		construct.sentences.add(sentence);
		return construct;
	}

	/**
	 * Sentence() method will perform operation on String input, set the expected
	 * value into respective Construct Object and then return the value as response
	 * 
	 * @param object
	 * 
	 * 
	 * @param String statementType
	 * @param String sql
	 * @methodName @parseSentence
	 * @return Sentence GUARDIUM Object
	 * 
	 */
	protected static Sentence parseSentence(String sql, String statementType, String object) {
		Sentence sentence = null;
		sentence = new Sentence(statementType);
		SentenceObject sentenceObject = new SentenceObject(object);
		sentenceObject.setType(ApplicationConstants.COLLECTION);
		sentence.getObjects().add(sentenceObject);
		return sentence;
	}

	/**
	 * parseException() method will perform operation on String inputs and
	 * JsonObject, set the expected value into respective ExceptionRecord Object and
	 * then return the value as response
	 * 
	 * @param String     severity
	 * @param String     sql
	 * @param JsonObject protoPayload
	 * @methodName @parseException
	 * @return ExceptionRecord GUARDIUM Object
	 * 
	 */
	public static ExceptionRecord parseException(String sql, JsonObject inputJson) {
		JsonObject protoPayload = inputJson.get(ApplicationConstants.PROTO_PAYLOAD).getAsJsonObject();
		String severity = getFieldValueByKey(inputJson, ApplicationConstants.SEVERITY);
		ExceptionRecord exceptionRecord = null;
		if (severity.equalsIgnoreCase("ERROR")) {
			exceptionRecord = new ExceptionRecord();
			exceptionRecord.setExceptionTypeId(ApplicationConstants.EXCEPTION_TYPE_STRING);
			exceptionRecord.setDescription(getExceptionCodeAndMessage(protoPayload));
			exceptionRecord.setSqlString(sql);
		}
		return exceptionRecord;
	}

	/**
	 * parseTime() method will perform operation on String inputs, set the expected
	 * value into respective Time Object and then return the value as response
	 * 
	 * @param String dateString
	 * @methodName @parseException
	 * @return ExceptionRecord GUARDIUM Object
	 * 
	 */
	private static Time parseTime(String dateString) {
		ZonedDateTime date = ZonedDateTime.parse(dateString);
		long millis = date.toInstant().toEpochMilli();
		int minOffset = date.getOffset().getTotalSeconds() / 60;
		return new Time(millis, minOffset, 0);
	}

	/**
	 * getAppUserName() method will perform operation on JsonObject inputJson object
	 * convert into appUserName and return response
	 * 
	 * @param JsonObject inputJson
	 * @methodName @getAppUserName
	 * @return String
	 * 
	 */
	private static String getAppUserName(JsonObject protoPayloadJsonObject) {
		String appUserName = StringUtils.EMPTY;
		JsonObject authenticationJSON = validateKeyExistance(protoPayloadJsonObject,
				ApplicationConstants.AUTHENTICATION_INFO);
		if (authenticationJSON.has(ApplicationConstants.PRINCIPAL_EMAIL)) {
			appUserName = CommonUtils.convertIntoString(authenticationJSON.get(ApplicationConstants.PRINCIPAL_EMAIL));
		}
		return appUserName;
	}

	/**
	 * getCallerIp() method will perform operation on JsonObject inputJson object
	 * convert into callerIp and return response
	 * 
	 * @param JsonObject protoPayloadJsonObject
	 * @methodName @getCallerIp
	 * @return String
	 * 
	 */
	private static String getCallerIp(JsonObject protoPayloadJsonObject) {
		String callerIp = StringUtils.EMPTY;
		JsonObject requestMetadataIntoJSON = validateKeyExistance(protoPayloadJsonObject,
				ApplicationConstants.REQUEST_METADATA);
		if (requestMetadataIntoJSON.has(ApplicationConstants.CALLER_IP)) {
			callerIp = CommonUtils.convertIntoString(requestMetadataIntoJSON.get(ApplicationConstants.CALLER_IP));
		}
		return callerIp;
	}

	/**
	 * getExceptionCodeAndMessage() method will perform operation on JsonObject
	 * protoPayload and String as a key object into Exception and code and return
	 * response
	 * 
	 * @param JsonObject protoPayload
	 * @param String     key
	 * @methodName @getExceptionCodeAndMessage
	 * @return String
	 * 
	 */
	private static String getExceptionCodeAndMessage(JsonObject protoPayload) {
		JsonObject status = validateKeyExistance(protoPayload, ApplicationConstants.STATUS);
		if (status.entrySet().isEmpty()) {
			return StringUtils.EMPTY;
		}
		return CommonUtils.convertIntoString(status.get(ApplicationConstants.MESSAGE));
	}

	/**
	 *
	 * validateKeyExistance() method will perform operation on JsonObject jsonObject
	 * and key as a String, and if key exist then get the value and send JsonObject
	 * as a reponse
	 * 
	 * @param JsonObject jsonObject
	 * @param String     key
	 * @methodName @validateKeyExistance
	 * @return JsonObject jsonObject
	 * 
	 */
	public static JsonObject validateKeyExistance(JsonObject jsonObject, String key) {
		if (!jsonObject.has(key)) {
			return jsonObject;
		}
		return jsonObject.get(key).getAsJsonObject();
	}

	/**
	 *
	 * getFieldValueByKey() method will perform operation on JsonObject jsonObject
	 * and key as a String, and if key exist then get the value and send Value of
	 * that key as a reponse
	 * 
	 * @param JsonObject jsonObject
	 * @param String     key
	 * @methodName @validateKeyExistance
	 * @return String value
	 * 
	 */
	private static String getFieldValueByKey(JsonObject jsonObject, String key) {
		return CommonUtils.convertIntoString(jsonObject.get(key));
	}

	/**
	 *
	 * getProjectId() method will perform operation on JsonObject protoPayload, for
	 * getting the projectId value as a string using different Operation then return
	 * the value as response
	 * 
	 * @param JsonObject inputJson
	 * @methodName getProjectId
	 * @return String query Object
	 * 
	 */
	private static String getProjectId(JsonObject inputJson) {
		String projectId = StringUtils.EMPTY;
		JsonObject resourceJSON = validateKeyExistance(inputJson, ApplicationConstants.RESOURCE);
		if (!resourceJSON.has(ApplicationConstants.LABELS)) {
			return projectId;
		}
		JsonObject labels = resourceJSON.get(ApplicationConstants.LABELS).getAsJsonObject();
		if (!labels.has(ApplicationConstants.PROJECT_ID)) {
			return projectId;
		}
		return CommonUtils.convertIntoString(labels.get(ApplicationConstants.PROJECT_ID));
	}

}
