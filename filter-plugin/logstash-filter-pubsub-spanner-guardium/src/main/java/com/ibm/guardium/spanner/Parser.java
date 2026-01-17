/*
Copyright IBM Corp. 2021, 2022, 2023 All rights reserved.

SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.spanner;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class Parser {

	private static Logger logger = LogManager.getLogger(Parser.class);

	private static String customRegex = "((?i)\\s*intersect\\s+(?i)all)|((?i)\\s*INTERSECT(?i)\\s+DISTINCT)|((?i)\\s*except\\s+(?i)All)|((?i)\\s*EXCEPT\\s+(?i)DISTINCT)";
	private static String redactedRegex = "\"[^\"]*\"|'[^']*'|\\b[\\d]+\\b";

	private static Pattern customRegexPattern = Pattern.compile(customRegex);

	private static Pattern redactedRegexpattern = Pattern.compile(redactedRegex);

	private Parser() {
		throw new IllegalStateException("Parser class");
	}

	/**
	 * parseRecord() method will perform operation on JsonObject input, convert
	 * JsonObject into Record Object and then return the value as response
	 * 
	 * @param JsonObject inputJson
	 * @return Record GUARDIUM Object
	 * @throws Exception
	 * 
	 */
	public static Record parseRecord(JsonObject inputJson) {

		JsonObject protoPayloadJsonObject = inputJson.get(ApplicationConstants.PROTO_PAYLOAD).getAsJsonObject();
		Record record = new Record();

		JsonObject resourceJsonObject = inputJson.get(ApplicationConstants.RESOURCES).getAsJsonObject();
		record.setSessionId(getSessionId(protoPayloadJsonObject, resourceJsonObject));

		record.setDbName(getInstanceNameAndProjectId(resourceJsonObject, ApplicationConstants.COLON)
				+ ApplicationConstants.COLON + filterDatabaseName(protoPayloadJsonObject));
		record.setAppUserName(ApplicationConstants.UNKOWN_STRING);

		record.setAccessor(parseAccessor(protoPayloadJsonObject, resourceJsonObject));
		record.setSessionLocator(parserSesstionLocator(protoPayloadJsonObject));

		record.setData(parseData(protoPayloadJsonObject));

		record.setTime(parseTime(inputJson));

		return record;
	}

	private static String getSessionId(JsonObject protoPayloadJsonObject, JsonObject resourceJsonObject) {
		Integer sessionId = (getCallerIp(protoPayloadJsonObject) + getAppUserName(protoPayloadJsonObject)
				+ getInstanceNameAndProjectId(resourceJsonObject, ApplicationConstants.COLON)
				+ ApplicationConstants.COLON + filterDatabaseName(protoPayloadJsonObject)).hashCode();
		return sessionId.toString();
	}

	/**
	 * parseAccessor() method will perform operation on JsonObject input, convert
	 * JsonObject into Accessor Object and then return the value as response
	 * 
	 * @param JsonObject protoPayloadJsonObject
	 * @return Accessor GUARDIUM Object
	 * 
	 */
	public static Accessor parseAccessor(JsonObject protoPayloadJsonObject, JsonObject resourceJsonObject) {
		Accessor accessor = new Accessor();

		accessor.setServerType(ApplicationConstants.SERVER_TYPE);
		accessor.setServerOs(ApplicationConstants.UNKOWN_STRING);

		accessor.setClientOs(ApplicationConstants.UNKOWN_STRING);
		accessor.setClientHostName(ApplicationConstants.UNKOWN_STRING);

		accessor.setServerHostName(getInstanceNameAndProjectId(resourceJsonObject, ApplicationConstants.UNDERSCORE)
				+ ApplicationConstants.UNDERSCORE + getServiceName(protoPayloadJsonObject));
		accessor.setCommProtocol(ApplicationConstants.UNKOWN_STRING);

		accessor.setDbProtocol(ApplicationConstants.DATA_PROTOCOL);
		accessor.setDbProtocolVersion(ApplicationConstants.UNKOWN_STRING);

		accessor.setOsUser(ApplicationConstants.UNKOWN_STRING);
		accessor.setSourceProgram(ApplicationConstants.UNKOWN_STRING);

		accessor.setClient_mac(ApplicationConstants.UNKOWN_STRING);
		accessor.setServerDescription(ApplicationConstants.UNKOWN_STRING);

		accessor.setDbUser(getAppUserName(protoPayloadJsonObject));

		accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);

		accessor.setServiceName(getInstanceNameAndProjectId(resourceJsonObject, ApplicationConstants.COLON)
				+ ApplicationConstants.COLON + filterDatabaseName(protoPayloadJsonObject));

		return accessor;
	}

	/**
	 * getInstanceName() method performs operation on JsonObject input, gets the
	 * InstanceName value from resourceJsonObject and then return the instanceName
	 * as String
	 * 
	 * @param resourceJsonObject
	 * @return
	 */
	private static String getInstanceNameAndProjectId(JsonObject resourceJsonObject, String key) {
		String instanceNameAndProjectId = StringUtils.EMPTY;
		if (!resourceJsonObject.has(ApplicationConstants.LABELS))
			return instanceNameAndProjectId;
		JsonObject labelsJsonObject = resourceJsonObject.get(ApplicationConstants.LABELS).getAsJsonObject();
		instanceNameAndProjectId = (labelsJsonObject.has(ApplicationConstants.INSTANCE_ID)
				&& labelsJsonObject.has(ApplicationConstants.PROJECT_ID))
						? labelsJsonObject.get(ApplicationConstants.PROJECT_ID).getAsString() + key
								+ labelsJsonObject.get(ApplicationConstants.INSTANCE_ID).getAsString()
						: instanceNameAndProjectId;
		return instanceNameAndProjectId;
	}

	/**
	 * parserSesstionLocator() method will perform operation on JsonObject input,
	 * convert JsonObject into SessionLocator Object and then return the value as
	 * response
	 * 
	 * @param JsonObject protoPayloadJsonObject
	 * @return SessionLocator GUARDIUM Object
	 * 
	 */
	public static SessionLocator parserSesstionLocator(JsonObject protoPayloadJsonObject) {
		SessionLocator sessionLocator = new SessionLocator();
		sessionLocator.setIpv6(false);
		if (Util.isIPv6(getCallerIp(protoPayloadJsonObject))) {
			sessionLocator.setIpv6(true);
			sessionLocator.setClientIpv6(getCallerIp(protoPayloadJsonObject));
			sessionLocator.setServerIpv6(ApplicationConstants.DEFAULT_IPV6);
		} else {
			sessionLocator.setClientIp(getCallerIp(protoPayloadJsonObject));
			sessionLocator.setServerIp(ApplicationConstants.DEFAULT_IP);
		}
		sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);
		sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);

		return sessionLocator;
	}

	/**
	 * parseData() method will perform operation on JsonObject input, convert
	 * JsonObject into Data Object and then return the value as response
	 * 
	 * @param JsonObject protoPayloadJsonObject
	 * @return Data GUARDIUM Object
	 * @throws Exception
	 * 
	 */
	private static Data parseData(JsonObject protoPayloadJsonObject) {
		Data data = new Data();
		String sqlQuery = getSqlQuery(protoPayloadJsonObject);
		String fullSql = getQueryStatementFromRequestToPopulateSql(protoPayloadJsonObject);
		if (!sqlQuery.isEmpty() && !fullSql.isEmpty()) {
			data.setConstruct(parseAsConstruct(sqlQuery, fullSql));
		}
		return data;
	}

	/**
	 * parseAsConstruct() method will perform operation on JsonObject input, convert
	 * JsonObject into Construct Object and then return the value as response
	 * 
	 * @param JsonObject protoPayloadJsonObject
	 * @return Construct GUARDIUM Object
	 * @throws Exception
	 * 
	 */
	private static Construct parseAsConstruct(String sqlQuery, String fullSql) {
		try {
			final Construct construct = new Construct();
			final Sentence sentence = Parser.parseSentence(sqlQuery);
			construct.sentences.add(sentence);
			sqlQuery = fullSql.replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)", " ").trim();
			if (sentence.getVerb().toLowerCase().contains(ApplicationConstants.TABLE)) {
				construct.setRedactedSensitiveDataSql(sqlQuery);
			} else {
				construct.setRedactedSensitiveDataSql(redactedHelper(sqlQuery));
			}
			construct.setFullSql(sqlQuery);
			return construct;
		} catch (final Exception e) {
			throw e;
		}
	}

	private static String redactedHelper(String sqlQuery) {
		final Matcher matcher = redactedRegexpattern.matcher(sqlQuery);
		return matcher.replaceAll(ApplicationConstants.SUBSTITUTE_WITH_QUESTION_MARK);
	}

	/**
	 * this method will perform operation on String input, set the value in Sentence
	 * Object and then return the value as response
	 * 
	 * @param protoPayloadJsonObject
	 * 
	 * @param String                 sqlQuery
	 * @return sentence
	 * @throws Exception
	 * 
	 */

	@SuppressWarnings("unchecked")
	private static Sentence parseSentence(String sqlQuery) {
		Sentence sentence = null;
		Map<String, Object> mp;
		List<Object> objList = null;
		try {
			String sql = regexCustomReplace(sqlQuery);
			mp = ExecuteSqlParser.runJSEngine(sql);
			if (mp.isEmpty()) {
				sentence = new Sentence(ApplicationConstants.UNPARSEABLE);
				return sentence;
			}
			Optional<Object> optional = Optional.ofNullable(mp.get(ApplicationConstants.OBJECTS));
			if (optional.isPresent()) {
				objList = (List<Object>) mp.get(ApplicationConstants.OBJECTS);
			}
			List<Object> verbList = (List<Object>) mp.get(ApplicationConstants.VERBS);
			for (int i = 0; i < verbList.size(); i++) {
				if (i == 0) {
					sentence = new Sentence(sqlQuery);
					if (!objList.isEmpty()) {
						for (Object object : (Set<Object>) objList.get(i)) {
							SentenceObject sentenceObject = new SentenceObject(object.toString());
							sentenceObject.setType(ApplicationConstants.COLLECTION);
							sentence.getObjects().add(sentenceObject);
						}
					}
					sentence.setVerb(verbList.get(i).toString());

				} else {
					Sentence descendent = new Sentence(verbList.get(i).toString());
					for (Object object : (Set<Object>) objList.get(i)) {
						SentenceObject sentenceObject = new SentenceObject(object.toString());
						sentenceObject.setType(ApplicationConstants.COLLECTION);
						descendent.getObjects().add(sentenceObject);
					}
					descendent.setVerb(verbList.get(i).toString());
					sentence.getDescendants().add(descendent);
				}
			}
			return sentence;

		} catch (Exception e) {
			logger.error("Could not find object & verb for the query : " + e.getMessage());
		}
		return sentence;
	}

	private static String regexCustomReplace(String sqlQuery) {
		final Matcher matcher = customRegexPattern.matcher(sqlQuery);
		return matcher.replaceAll(ApplicationConstants.SUBSTITUTE_WITH_INTERSECT);
	}

	/**
	 * parseTime() method will perform operation on JsonObject input, convert
	 * JsonObject into Time Object and then return the value as response
	 * 
	 * @param JsonObject inputJson
	 * @return ExceptionRecord GUARDIUM Object
	 * 
	 */
	private static Time parseTime(JsonObject inputJson) {
		return getTime(getFieldValueByKey(inputJson, ApplicationConstants.TIMESTAMP));
	}

	/**
	 * getTime() method will perform operation on String object convert object into
	 * Time format using ZonedDateTime and return response
	 * 
	 * @param String dateString
	 * @return Time GUARDIUM Object
	 * 
	 */
	public static Time getTime(String dateString) {
		ZonedDateTime date = ZonedDateTime.parse(dateString);
		long millis = date.toInstant().toEpochMilli();
		int minOffset = date.getOffset().getTotalSeconds() / 60;
		return new Time(millis, minOffset, 0);
	}

	/**
	 * filterDatabaseName() method will perform operation on JsonObject object,
	 * filter the database name from request and response by value and return
	 * response
	 * 
	 * @param JsonObject protoPayloadJsonObject
	 * @return String value
	 * 
	 */
	private static String filterDatabaseName(JsonObject protoPayloadJsonObject) {
		JsonObject resourceAttributesJsonObject = null;
		String databaseName = StringUtils.EMPTY;
		if (!protoPayloadJsonObject.has(ApplicationConstants.AUTHORIZATION_INFO))
			return null;
		JsonArray authorizationInfoJsonArray = protoPayloadJsonObject.get(ApplicationConstants.AUTHORIZATION_INFO)
				.getAsJsonArray();
		for (int i = 0; i < authorizationInfoJsonArray.size(); i++) {
			JsonObject jsonObj = authorizationInfoJsonArray.get(i).getAsJsonObject();
			if (jsonObj.has(ApplicationConstants.RESOURCE_ATTRIBUTES)) {
				resourceAttributesJsonObject = jsonObj.get(ApplicationConstants.RESOURCE_ATTRIBUTES).getAsJsonObject();
				break;
			}
		}
		if (null != resourceAttributesJsonObject && resourceAttributesJsonObject.has(ApplicationConstants.NAME)
				&& resourceAttributesJsonObject.has(ApplicationConstants.TYPE) && resourceAttributesJsonObject
						.get(ApplicationConstants.TYPE).getAsString().equals(ApplicationConstants.DATABASE_TYPE)) {
			String[] nameArr = resourceAttributesJsonObject.get(ApplicationConstants.NAME).toString().split("/");
			databaseName = nameArr[nameArr.length - 1].substring(0, nameArr[nameArr.length - 1].length() - 1);
		}
		return databaseName;
	}

	/**
	 * getCallerIp() method will perform operation on JsonObject object, filter the
	 * callerIp from requestMetadata by value and return response
	 * 
	 * @param JsonObject protoPayloadJsonObject
	 * @return String value CallerIp callerIp
	 * 
	 */
	private static String getCallerIp(JsonObject protoPayloadJsonObject) {
		String callerIp = StringUtils.EMPTY;
		if (!protoPayloadJsonObject.has(ApplicationConstants.REQUEST_METADATA)) {
			return callerIp;
		}
		JsonObject requestMetadataIntoJSON = protoPayloadJsonObject.get(ApplicationConstants.REQUEST_METADATA)
				.getAsJsonObject();
		if (!requestMetadataIntoJSON.entrySet().isEmpty() && requestMetadataIntoJSON.entrySet() != null) {
			if (requestMetadataIntoJSON.has(ApplicationConstants.CALLER_IP)) {
				callerIp = CommonUtils.convertIntoString(requestMetadataIntoJSON.get(ApplicationConstants.CALLER_IP));
			}
		}

		return callerIp;
	}

	/**
	 * getQueryStatement() method will perform operation on JsonObject object,
	 * filter the queryStatement from request input by value and return response
	 * 
	 * @param protoPayloadJsonObject
	 * @return queryStatement
	 */

	private static String getQueryStatement(JsonObject protoPayloadJsonObject) {
		String queryStatement = StringUtils.EMPTY;
		if (!protoPayloadJsonObject.has(ApplicationConstants.REQUEST)) {
			return queryStatement;
		}
		JsonObject requestJson = protoPayloadJsonObject.get(ApplicationConstants.REQUEST).getAsJsonObject();
		if (requestJson.entrySet().isEmpty()) {
			return queryStatement;
		}
		String createStatement = requestJson.has(ApplicationConstants.CREATE_STATEMENT)
				? CommonUtils.convertIntoString(requestJson.get(ApplicationConstants.CREATE_STATEMENT))
				: CommonUtils.convertIntoString(requestJson.get(ApplicationConstants.SQL));

		if (!StringUtils.isEmpty(createStatement)) {
			queryStatement = createStatement;
			return queryStatement;
		}
		return queryStatement;

	}

	/**
	 * getQueryStatementFromRequestToPopulateSql() method will perform operation on
	 * JsonObject object, filter the queryStatement from request input by value and
	 * return response
	 * 
	 * @param protoPayloadJsonObject
	 * @return sql
	 */
	private static String getQueryStatementFromRequestToPopulateSql(JsonObject protoPayloadJsonObject) {
		String queryStatement = StringUtils.EMPTY;
		List<String> queryList = new ArrayList<>();
		queryStatement = getQueryStatement(protoPayloadJsonObject);
		if (!queryStatement.isEmpty()) {
			return queryStatement;
		}
		JsonObject requestJson = protoPayloadJsonObject.get(ApplicationConstants.REQUEST).getAsJsonObject();
		if (requestJson.has(ApplicationConstants.STATEMENTS)) {
			JsonArray jsonArray = requestJson.get(ApplicationConstants.STATEMENTS).getAsJsonArray();
			for (int i = 0; i < jsonArray.size(); i++) {
				JsonElement jsonElementStatement = jsonArray.get(i);
				queryStatement = CommonUtils.convertIntoString(jsonElementStatement);
				queryList.add(queryStatement);
			}
		}
		String sql = String.join(", ", queryList);
		return sql;
	}

	/**
	 * getServiceName() method will perform operation on JsonObject object, filter
	 * the callerIp from input by value and return response
	 * 
	 * @param JsonObject protoPayloadJsonObject
	 * @return String value ServiceName serviceName
	 * 
	 */
	private static String getServiceName(JsonObject protoPayloadJsonObject) {
		return CommonUtils.convertIntoString(protoPayloadJsonObject.get(ApplicationConstants.SERVICE_NAME));
	}

	/**
	 * getFieldValueByKey() method will perform operation on JsonObject object,
	 * filter the key from input by value and return response
	 * 
	 * @param JsonObject inputJson
	 * @param String     key
	 * @return String key's value
	 */
	private static String getFieldValueByKey(JsonObject inputJson, String key) {
		return CommonUtils.convertIntoString(inputJson.get(key));
	}

	/**
	 * getAppUserName() method will perform operation on JsonObject object, filter
	 * the appUserName from response input by value and return response
	 * 
	 * @param JsonObject protoPayloadJsonObject
	 * @return String value appUserName
	 * 
	 */
	private static String getAppUserName(JsonObject protoPayloadJsonObject) {
		String appUserName = StringUtils.EMPTY;
		if (!protoPayloadJsonObject.has(ApplicationConstants.AUTHENTICATION_INFO)) {
			return appUserName;
		}
		JsonObject authenticationJSON = protoPayloadJsonObject.get(ApplicationConstants.AUTHENTICATION_INFO)
				.getAsJsonObject();
		if (!authenticationJSON.entrySet().isEmpty() && authenticationJSON.entrySet() != null) {
			appUserName = CommonUtils.convertIntoString(authenticationJSON.get(ApplicationConstants.PRINCIPAL_EMAIL));
		}
		return appUserName;
	}

	/**
	 * getSqlQuery() method will perform operation on JsonObject object, filter the
	 * sqlQuery from request and response input by value and return response
	 * 
	 * @param JsonObject protoPayloadJsonObject
	 * @return String value sqlQuery
	 * 
	 */
	private static String getSqlQuery(JsonObject protoPayloadJsonObject) {
		String sql = getQueryStatementFromRequest(protoPayloadJsonObject);
		return sql;
	}

	/**
	 * getQueryStatementFromRequest() method will perform operation on JsonObject
	 * object, filter the queryStatement from request input by value and return
	 * response
	 * 
	 * @param JsonObject protoPayloadJsonObject
	 * @return String value queryStatement
	 * 
	 */
	private static String getQueryStatementFromRequest(JsonObject protoPayloadJsonObject) {
		String queryStatement = StringUtils.EMPTY;
		List<String> queryList = new ArrayList<>();
		queryStatement = getQueryStatement(protoPayloadJsonObject);
		if (!queryStatement.isEmpty()) {
			return queryStatement;
		}
		JsonObject requestJson = protoPayloadJsonObject.get(ApplicationConstants.REQUEST).getAsJsonObject();
		if (requestJson.has(ApplicationConstants.STATEMENTS)) {
			JsonArray jsonArray = requestJson.get(ApplicationConstants.STATEMENTS).getAsJsonArray();
			for (int i = 0; i < jsonArray.size(); i++) {
				JsonElement jsonElementStatement = jsonArray.get(i);
				queryStatement = CommonUtils.convertIntoString(jsonElementStatement);
				queryList.add(queryStatement);
			}
		}
		String sql = ApplicationConstants.UNKOWN_STRING;
		if (queryList.size() == 1) {
			sql = queryList.get(0).concat(";");
		} else {
			sql = String.join("; ", queryList);
		}
		return sql;
	}

}
