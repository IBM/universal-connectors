/*

Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0

*/
package com.ibm.guardium.firestore;

import java.net.URLDecoder;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
 * FireStore data access audit logs into a Guardium record instance Guardium
 * records include the accessor, the sessionLocator, data, and exceptions. If
 * there are no errors, the data contains details about the query "construct"
 *
 * @className @Parser
 * 
 */
public class Parser {

	private static Logger logger = LogManager.getLogger(Parser.class);

	/**
	 *
	 * parseRecord() method will perform operation on JsonObject input, convert
	 * JsonObject into Record Object and then return the value as response
	 * 
	 * @param JsonObject inputJson
	 * @methodName @parseRecord
	 * @return Record GUARDIUM Object
	 * 
	 */
	public static Record parseRecord(JsonObject inputJson) throws Exception {
		Record record = new Record();
		String serverHostName = ApplicationConstants.SERVER_HOST_NAME_STRING;
		String projectId = StringUtils.EMPTY;
		JsonObject protoPayload = inputJson.get(ApplicationConstants.PROTO_PAYLOAD).getAsJsonObject();
		String appUserName = getAppUserName(protoPayload);
		String databaseName = getDatabaseName(protoPayload);

		if (protoPayload.has(ApplicationConstants.SERVICE_NAME)) {
			projectId = getProjectId(inputJson);
			serverHostName = getFieldValueByKey(protoPayload, ApplicationConstants.SERVICE_NAME);
			serverHostName = projectId.concat("_").concat(serverHostName);
		}

		String sql = getFullSQLQueryFromEvent(protoPayload);
		record.setSessionId(getFieldValueByKey(inputJson, ApplicationConstants.INSERTID));
		record.setDbName(projectId.concat("-".concat(databaseName)));
		record.setAppUserName(appUserName);
		record.setAccessor(parseAccessor(appUserName, serverHostName, protoPayload, projectId));
		record.getAccessor().setServiceName(record.getDbName());
		record.setSessionLocator(parserSessionLocator(inputJson));
		record.setTime(parseTime(getFieldValueByKey(inputJson, ApplicationConstants.TIMESTAMP)));
		record.setException(parseException(sql, inputJson));

		if (null == record.getException()) {
			String verb = getVerbFromProtopayload(protoPayload);
			String object = getObjectFromProtopayload(protoPayload);
			record.setData(parseData(protoPayload, sql, verb, object));
		}
		return record;
	}

	/**
	 *
	 * getObjectFromProtopayload() method will perform operation on JsonObject
	 * protoPayload, for getting the Object value as a string using different
	 * Operation then return the value as response
	 * 
	 * @param JsonObject protoPayload
	 * @methodName @getObjectFromProtopayload
	 * @return String query Object
	 * 
	 */
	private static String getObjectFromProtopayload(JsonObject protoPayload) throws Exception {
		String object = StringUtils.EMPTY;
		if (!protoPayload.has(ApplicationConstants.REQUEST)) {
			return object;
		}
		JsonObject requestJsonObject = protoPayload.get(ApplicationConstants.REQUEST).getAsJsonObject();
		try {
			if (requestJsonObject.has(ApplicationConstants.ADD_TARGET)) {
				JsonObject addTargetJsonObject = requestJsonObject.get(ApplicationConstants.ADD_TARGET)
						.getAsJsonObject();
				String queryType = addTargetJsonObject.has(ApplicationConstants.QUERY) ? ApplicationConstants.QUERY
						: ApplicationConstants.DOCUMENTS;
				object = getObjectFromQueryEvent(addTargetJsonObject, object, queryType);
			} else if (requestJsonObject.has(ApplicationConstants.WRITES)
					|| requestJsonObject.has(ApplicationConstants.MASK)) {
				String queryType = requestJsonObject.has(ApplicationConstants.WRITES) ? ApplicationConstants.WRITES
						: ApplicationConstants.MASK;
				object = getObjectFromWriteAndMaskEvent(requestJsonObject, object, queryType);
			} else if (requestJsonObject.has(ApplicationConstants.STRUCTURED_QUERY)) {
				object = getObjectFromRequestStructuredQueryEvent(requestJsonObject);
			}
			//single document
			else if(requestJsonObject.has(ApplicationConstants.DOCUMENTS)) {
				String collectionId = CommonUtils.convertIntoString(requestJsonObject.get(ApplicationConstants.DOCUMENTS).getAsJsonArray());
				object = parseCollectionId(collectionId);
			}
			//collectionid
			else if (requestJsonObject.has(ApplicationConstants.COLLECTIONID)) {
			object=	CommonUtils.convertIntoString(requestJsonObject.get(ApplicationConstants.COLLECTIONID));
			}
			else if(requestJsonObject.has(ApplicationConstants.INDEX)) {
				JsonObject addTargetJsonObject = requestJsonObject.get(ApplicationConstants.INDEX)
						.getAsJsonObject();
				String collectionId = CommonUtils.convertIntoString(addTargetJsonObject.get(ApplicationConstants.NAME));
				object = parseCollectionId(collectionId);
			}
			else if(requestJsonObject.has(ApplicationConstants.NAME)) {
				String collectionId = CommonUtils.convertIntoString(requestJsonObject.get(ApplicationConstants.NAME));
				String[] msg = collectionId.split("/");
				if(msg.length>6) {
				object=msg[5];
				}
			}
			else if (requestJsonObject.has(ApplicationConstants.FIELD)) {
				JsonObject addTargetJsonObject = requestJsonObject.get(ApplicationConstants.FIELD)
						.getAsJsonObject();
				String collectionId = CommonUtils.convertIntoString(addTargetJsonObject.get(ApplicationConstants.NAME));				
				String[] msg = collectionId.split("/");
				if(msg.length>6) {
				object=decodeContent(msg[5]);
				}
			}
		} catch (Exception ex) {
			logger.error("found exception in getObjectFromWriteAndMaskEvent", ex);
			throw ex;
		}
		return object;
	}

	public static String decodeContent(String obj) {
        try {
            if (obj != null)
                obj = URLDecoder.decode(obj, "UTF-8");

        } catch (Exception e) {
            logger.error("Exception occured while decoding query: " + e.getMessage());
        }
        return obj;
    }
	/**
	 *
	 * getObjectFromRequestStructuredQueryEvent() method will perform operation on
	 * JsonObject protoPayload, for getting the object from Query event value as a
	 * string using different Operation then return the value as response
	 * 
	 * @param JsonObject requestJsonObject
	 * @methodName @getObjectFromRequestStructuredQueryEvent
	 * @return String query Object
	 * 
	 */
	private static String getObjectFromRequestStructuredQueryEvent(JsonObject requestJsonObject) {
		String object = StringUtils.EMPTY;
		JsonObject structuralQueryJsonObject = requestJsonObject.get(ApplicationConstants.STRUCTURED_QUERY)
				.getAsJsonObject();
		if (!structuralQueryJsonObject.has(ApplicationConstants.FROM)) {
			return object;
		}

		JsonArray fromJsonArray = structuralQueryJsonObject.get(ApplicationConstants.FROM).getAsJsonArray();
		if (fromJsonArray.size() == 0) {
			return object;
		}
		JsonObject fromJsonObject = fromJsonArray.get(0).getAsJsonObject();
		if (!fromJsonObject.has(ApplicationConstants.COLLECTIONID)) {
			return object;
		}
		return CommonUtils.convertIntoString(fromJsonObject.get(ApplicationConstants.COLLECTIONID));
	}

	/**
	 *
	 * getObjectFromQueryEvent() method will perform operation on JsonObject
	 * protoPayload, for getting the object from Query event value as a string using
	 * different Operation then return the value as response
	 * 
	 * @param JsonObject requestJsonObject
	 * @param String     object
	 * @param String     queryType
	 * @methodName @getObjectFromQueryEvent
	 * @return String query Object
	 * 
	 */
	private static String getObjectFromQueryEvent(JsonObject addTargetJsonObject, String object, String queryType) {
		switch (queryType) {
		case "query":
			JsonObject queryJsonObject = addTargetJsonObject.get(ApplicationConstants.QUERY).getAsJsonObject();
			if (!queryJsonObject.has(ApplicationConstants.STRUCTURED_QUERY)) {
				return object;
			}
			JsonObject structuralQueryJsonObject = queryJsonObject.get(ApplicationConstants.STRUCTURED_QUERY)
					.getAsJsonObject();
			if (!structuralQueryJsonObject.has(ApplicationConstants.FROM)) {
				return object;
			}

			JsonArray fromJsonArray = structuralQueryJsonObject.get(ApplicationConstants.FROM).getAsJsonArray();
			if (fromJsonArray.size() == 0) {
				return object;
			}
			JsonObject fromJsonObject = fromJsonArray.get(0).getAsJsonObject();
			if (!fromJsonObject.has(ApplicationConstants.COLLECTIONID)) {
				return object;
			}
			object = CommonUtils.convertIntoString(fromJsonObject.get(ApplicationConstants.COLLECTIONID));
			break;
		case "documents":
			JsonObject documentJsonObject = addTargetJsonObject.get(ApplicationConstants.DOCUMENTS).getAsJsonObject();
			if (!documentJsonObject.has(ApplicationConstants.DOCUMENTS)) {
				return object;
			}
			JsonArray jsonObjectArray = documentJsonObject.get(ApplicationConstants.DOCUMENTS).getAsJsonArray();
			if (jsonObjectArray.size() == 0) {
				return object;
			}
			String collectionId = CommonUtils.convertIntoString(jsonObjectArray.get(0));
			object = parseCollectionId(collectionId);
			break;
		default:
			break;
		}
		return object;
	}
	private static String parseCollectionId(String collectionId) {
		String[] msg = collectionId.split("/");
		StringBuilder msg1 = new StringBuilder(ApplicationConstants.UNKOWN_STRING);
		// projects/project-sccd/databases/(default)/documents/1-june-collection/doc/coll2/doc2/coll3/doc3/coll4/doc4
		if (msg != null && msg.length > 5) {
			for (int i = 5; i < msg.length; i = i + 2) {
				msg1.append(msg[i]).append(".");
			}
		}
		return msg1.toString().substring(0, msg1.length() - 1);
	}

	/**
	 *
	 * getObjectFromWriteAndMaskEvent() method will perform operation on JsonObject
	 * protoPayload, for getting the object from Write and Mask event value as a
	 * string using different Operation then return the value as response
	 * 
	 * @param JsonObject requestJsonObject
	 * @param String     object
	 * @param String     queryType
	 * @methodName @getObjectFromWriteAndMaskEvent
	 * @return String query Object
	 * 
	 */
	private static String getObjectFromWriteAndMaskEvent(JsonObject requestJsonObject, String object,
			String queryType) {
		switch (queryType) {
		case "writes":
			JsonArray writesJsonArray = requestJsonObject.get(ApplicationConstants.WRITES).getAsJsonArray();
			if (writesJsonArray.size() == 0) {
				return object;
			}
			JsonObject writeJsonObject = writesJsonArray.get(0).getAsJsonObject();
			if (writeJsonObject.has(ApplicationConstants.UPDATE)) {
				JsonObject updateJsonObject = writeJsonObject.get(ApplicationConstants.UPDATE).getAsJsonObject();
				if (!updateJsonObject.has(ApplicationConstants.NAME)) {
					return object;
				}
				String collectionId = CommonUtils.convertIntoString(updateJsonObject.get(ApplicationConstants.NAME));
				object = parseCollectionId(collectionId);			
			}
			if (writeJsonObject.has(ApplicationConstants.DELETE)) {
				if (!writeJsonObject.has(ApplicationConstants.DELETE)) {
					return object;
				}
				String collectionId = CommonUtils.convertIntoString(writeJsonObject.get(ApplicationConstants.DELETE));
				object = parseCollectionId(collectionId);			
			} else if (writeJsonObject.has(ApplicationConstants.COLLECTIONID)) {
				object = CommonUtils.convertIntoString(writeJsonObject.get(ApplicationConstants.COLLECTIONID));
			}
			break;
		case "mask":
			object = requestJsonObject.has(ApplicationConstants.COLLECTIONID)
					? CommonUtils.convertIntoString(requestJsonObject.get(ApplicationConstants.COLLECTIONID))
					: StringUtils.EMPTY;
			break;
		default:
			break;
		}
		return object;
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
		if (resourceJSON.entrySet().isEmpty() || !resourceJSON.has(ApplicationConstants.LABELS)) {
			return projectId;
		}
		JsonObject labels = resourceJSON.get(ApplicationConstants.LABELS).getAsJsonObject();
		if (!labels.has(ApplicationConstants.PROJECT_ID)) {
			return projectId;
		}
		return CommonUtils.convertIntoString(labels.get(ApplicationConstants.PROJECT_ID));
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
		String query = StringUtils.EMPTY;
		if (!protoPayload.has(ApplicationConstants.REQUEST)) {
			return query;
		}
		JsonObject requestJsonObject = protoPayload.get(ApplicationConstants.REQUEST).getAsJsonObject();
		if (requestJsonObject.has(ApplicationConstants.ADD_TARGET)) {
			JsonObject addTargetJsonObject = requestJsonObject.get(ApplicationConstants.ADD_TARGET).getAsJsonObject();
			query = getFullSQL(addTargetJsonObject, ApplicationConstants.ADD_TARGET);
		} else if (requestJsonObject.has(ApplicationConstants.WRITES)) {
			query = getFullSQL(requestJsonObject, ApplicationConstants.WRITES);
		} else if (requestJsonObject.has(ApplicationConstants.STRUCTURED_QUERY)) {
			query = String.valueOf(requestJsonObject.get(ApplicationConstants.STRUCTURED_QUERY).getAsJsonObject());
		} else if(requestJsonObject.has(ApplicationConstants.INDEX)) {
			JsonObject addTargetJsonObject = requestJsonObject.get(ApplicationConstants.INDEX)
					.getAsJsonObject();
			query = String.valueOf(addTargetJsonObject);
		}
		else if(requestJsonObject.has(ApplicationConstants.FIELD)){
			JsonObject addTargetJsonObject = requestJsonObject.get(ApplicationConstants.FIELD)
					.getAsJsonObject();
			query = decodeContent(String.valueOf(addTargetJsonObject));
		}
		else {
			query = String.valueOf(protoPayload.get(ApplicationConstants.REQUEST).getAsJsonObject());
			
		}
		return query;
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
	private static String getDatabaseName(JsonObject protoPayload) throws Exception {
		String databaseName = StringUtils.EMPTY;
		boolean isIndexesEvent=false;
		if (protoPayload.has(ApplicationConstants.RESOURCE_NAME)) {
			databaseName = CommonUtils.convertIntoString(protoPayload.get(ApplicationConstants.RESOURCE_NAME));
		}
		if (databaseName.contains("/")) {
		    	String[] db=databaseName.split("/");
				if(db.length>3) {
					databaseName=db[3];	
				}			
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
	 * 
	 * @param String         appUserName
	 * @param String         projectId
	 * @param String         serverHostName
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
		//accessor.setServiceName(projectId);
		

		return accessor;
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
		sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);
		sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);
		
		if (inputJson.has(ApplicationConstants.HTTP_REQUEST)) {
			JsonObject httpJson = inputJson.get(ApplicationConstants.HTTP_REQUEST).getAsJsonObject();
			String serverIp = CommonUtils.convertIntoString(httpJson.get(ApplicationConstants.SERVER_IP));
			serverIp = !StringUtils.isEmpty(serverIp) ? serverIp : "0.0.0.0";
			sessionLocator.setServerIp(serverIp);
		}
		return sessionLocator;
	}

	/**
	 * parseData() method will perform operation on String input, set the expected
	 * value into respective Data Object and then return the value as response
	 * 
	 * @param object
	 * @param JsonObject protoPayload
	 * @param String     sql
	 * @param String     verb
	 * @methodName @parseData
	 * @return Data GUARDIUM Object
	 * 
	 */
	private static Data parseData(JsonObject protoPayload, String sql, String verb, String object) {
		Data data = new Data();
		data.setConstruct(parseAsConstruct(protoPayload, sql, verb, object));
		return data;
	}

	/**
	 * parseAsConstruct() method will perform operation on String input, set the
	 * expected value into respective Construct Object and then return the value as
	 * response
	 * 
	 * @param object
	 * @param JsonObject protoPayload
	 * @param String     sqlQuery
	 * @param String     verb
	 * @methodName @parseAsConstruct
	 * @return Construct GUARDIUM Object
	 * 
	 */
	private static Construct parseAsConstruct(JsonObject protoPayload, String sqlQuery, String verb, String object) {
		final Construct construct = new Construct();
		final Sentence sentence = parseSentence(sqlQuery, verb, object);
		construct.sentences.add(sentence);
		construct.setFullSql(sqlQuery);
		construct.setRedactedSensitiveDataSql(getRedactedQueryFromEvent(protoPayload));
		return construct;
	}

	/**
	 * parseSentence() method will perform operation on String inputs and
	 * JsonObject, set the expected value into respective Sentence Object and then
	 * return the value as response
	 * 
	 * @param object
	 * 
	 * @param String sqlQuery
	 * @param String verb
	 * @methodName @parseSentence
	 * @return ExceptionRecord GUARDIUM Object
	 * 
	 */
	private static Sentence parseSentence(String sqlQuery, String verb, String object) {
		Sentence sentence = null;
		sentence = new Sentence(sqlQuery);
		sentence.getObjects().add(parseSentenceObject(object));
		sentence.setVerb(verb);
		return sentence;
	}

	/**
	 * parseSentenceObject() method will perform operation on String inputs and
	 * JsonObject, set the expected value into respective SentenceObject Object and
	 * then return the value as response
	 * 
	 * @param String sqlQuery
	 * @methodName @parseSentenceObject
	 * @return ExceptionRecord GUARDIUM Object
	 * 
	 */
	private static SentenceObject parseSentenceObject(String sqlQuery) {
		SentenceObject sentenceObject = null;
		sentenceObject = new SentenceObject(sqlQuery);
		sentenceObject.setName(sqlQuery);
		sentenceObject.setType(ApplicationConstants.COLLECTIONS);
		return sentenceObject;
	}

	/**
	 * parseException() method will perform operation on String inputs and
	 * JsonObject, set the expected value into respective ExceptionRecord Object and
	 * then return the value as response
	 * 
	 * @param String     sql
	 * @param JsonObject inputJson
	 * @methodName @parseException
	 * @return ExceptionRecord GUARDIUM Object
	 * 
	 */
	private static ExceptionRecord parseException(String sql, JsonObject inputJson) {
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
		if (!authenticationJSON.entrySet().isEmpty()) {
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
		if (!requestMetadataIntoJSON.entrySet().isEmpty()
				&& requestMetadataIntoJSON.has(ApplicationConstants.CALLER_IP)) {
			callerIp = CommonUtils.convertIntoString(requestMetadataIntoJSON.get(ApplicationConstants.CALLER_IP));
		}
		return callerIp;
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
		String callerSuppliedUserAgent = StringUtils.EMPTY;
		JsonObject requestMetadataIntoJSON = validateKeyExistance(protoPayloadJsonObject,
				ApplicationConstants.REQUEST_METADATA);
		if (!requestMetadataIntoJSON.entrySet().isEmpty()
				&& requestMetadataIntoJSON.has(ApplicationConstants.CALLER_SUPPLIED_USER_AGENT)) {
			callerSuppliedUserAgent = CommonUtils
					.convertIntoString(requestMetadataIntoJSON.get(ApplicationConstants.CALLER_SUPPLIED_USER_AGENT));
		}
		return callerSuppliedUserAgent;
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

	private static String getFullSQL(JsonObject jsonObject, String key) {
		String fullSqlQuery = StringUtils.EMPTY;

		switch (key) {
		case "addTarget":
			if (jsonObject.has(ApplicationConstants.QUERY)) {
				fullSqlQuery = getQueryFromStructuralQuery(jsonObject);
			} else if (jsonObject.has(ApplicationConstants.DOCUMENTS)) {
				fullSqlQuery = getQueryFromRequestDocument(jsonObject);
			}
			break;
		case "writes":
			JsonArray writeJsonObject = jsonObject.get(ApplicationConstants.WRITES).getAsJsonArray();
			fullSqlQuery = String.valueOf(writeJsonObject);
			break;
		case "mask":
			JsonObject maskJsonObject = jsonObject.get(ApplicationConstants.MASK).getAsJsonObject();
			fullSqlQuery = String.valueOf(maskJsonObject);
			break;
		default:
			break;
		}
		return fullSqlQuery;
	}

	/**
	 *
	 * getQueryFromStructuralQuery() method will perform operation on JsonObject
	 * addTargetJsonObject, for getting the query from Structured_Query value as a
	 * string using different Operation then return the value as response
	 * 
	 * @param JsonObject addTargetJsonObject
	 * @methodName @getQueryFromStructuralQuery
	 * @return String query Object
	 * 
	 */
	private static String getQueryFromStructuralQuery(JsonObject addTargetJsonObject) {
		String query = StringUtils.EMPTY;
		JsonObject queryJsonObject = addTargetJsonObject.get(ApplicationConstants.QUERY).getAsJsonObject();
		if (!queryJsonObject.has(ApplicationConstants.STRUCTURED_QUERY)) {
			return query;
		}
		return String.valueOf(queryJsonObject.get(ApplicationConstants.STRUCTURED_QUERY).getAsJsonObject());

	}

	/**
	 *
	 * getQueryFromRequestDocument() method will perform operation on JsonObject
	 * addTargetJsonObject, for getting the query from DOCUMENT value as a string
	 * using different Operation then return the value as response
	 * 
	 * @param JsonObject addTargetJsonObject
	 * @methodName @getQueryFromRequestDocument
	 * @return String query Object
	 * 
	 */
	private static String getQueryFromRequestDocument(JsonObject addTargetJsonObject) {
		String query = StringUtils.EMPTY;
		JsonObject documentJsonObject = addTargetJsonObject.get(ApplicationConstants.DOCUMENTS).getAsJsonObject();
		if (!documentJsonObject.has(ApplicationConstants.DOCUMENTS)) {
			return query;
		}
		return String.valueOf(documentJsonObject.get(ApplicationConstants.DOCUMENTS));
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
		try {
			if (verb.contains("/")) {
				String[] array = verb.split("/");
				verb = String.valueOf(array[array.length - 1]);
				if (verb.contains(".")) {
					int lestIndex = verb.lastIndexOf('.');
					verb = verb.substring(lestIndex + 1, verb.length());
				}
			}
		} catch (Exception ex) {
			logger.error("found exception in verb", ex);
			throw ex;
		}
		return verb;
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
	 * @methodName @getFieldValueByKey
	 * @return String value
	 * 
	 */
	private static String getFieldValueByKey(JsonObject jsonObject, String key) {
		return CommonUtils.convertIntoString(jsonObject.get(key));
	}

	/**
	 *
	 * getRedactedQueryFromEvent() method will set RedactedQuery along with Masking
	 * of filters in query
	 *
	 * @param JsonObject protoPayload
	 * @methodName @getRedactedQueryFromEvent
	 * @return String value
	 *
	 */
	private static String getRedactedQueryFromEvent(JsonObject protoPayload) {
		String query = StringUtils.EMPTY;
		if (!protoPayload.has(ApplicationConstants.REQUEST)) {
			return query;
		}
		JsonObject requestJsonObject = protoPayload.get(ApplicationConstants.REQUEST).getAsJsonObject();
		JsonObject structuredQuery = requestJsonObject.getAsJsonObject(ApplicationConstants.STRUCTURED_QUERY);
		try {
			doMaskingRec(structuredQuery.entrySet(), ApplicationConstants.REDACTED_KEY,
					ApplicationConstants.REDACTED_MASK);
			return structuredQuery.toString();
		} catch (Exception e) {
			return query;
		}
	}

	/**
	 *
	 * doMaskingRec() method will recursively parse the JSON and do the masking
	 *
	 * @param Object obj
	 * @param String maskField
	 * @param String maskValue
	 * @methodName @doMaskingRec
	 * @return void
	 *
	 */
	private static void doMaskingRec(Object obj, String maskField, String maskValue) {
		if (obj instanceof Set) {
			for (Object entry : (Set) obj) {
				Map.Entry<String, JsonElement> e = (Map.Entry<String, JsonElement>) entry;
				Object value = e.getValue();
				doMaskingRec(value, maskField, maskValue);
			}
		} else if (obj instanceof JsonArray) {
			JsonArray arr = ((JsonArray) obj);
			Iterator<JsonElement> itr = arr.iterator();
			while (itr.hasNext()) {
				Object o = itr.next();
				if (o instanceof JsonObject) {
					JsonObject jo = (JsonObject) o;
					doMaskingRec(jo.entrySet(), maskField, maskValue);
				}
			}
		} else if (obj instanceof JsonObject) {
			JsonObject jsonObject = ((JsonObject) obj);
			Set<Map.Entry<String, JsonElement>> set = jsonObject.entrySet();
			Iterator<Map.Entry<String, JsonElement>> itr = set.iterator();
			while (itr.hasNext()) {
				Map.Entry<String, JsonElement> map = itr.next();
				String key = map.getKey();
				Object value = map.getValue();
				if (key.equals(maskField) && (value instanceof JsonObject)) {
					JsonObject json = (JsonObject) value;
					Set<Map.Entry<String, JsonElement>> entries = json.entrySet();
					Iterator<Map.Entry<String, JsonElement>> itr2 = entries.iterator();
					while (itr2.hasNext()) {
						Map.Entry<String, JsonElement> e = itr2.next();
						JsonObject jsonObj = new JsonObject();
						jsonObj.addProperty(e.getKey(), maskValue);
						map.setValue(jsonObj);
					}
				}
				doMaskingRec(set, maskField, maskValue);
			}
		}
	}
}
