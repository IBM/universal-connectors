/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.azure.cosmos;

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

import java.net.URLDecoder;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Parser {
	private static Logger log = LogManager.getLogger(Parser.class);

	/**
	 * Method to parse data from JsonObject and set to record object
	 * 
	 * @param records
	 * @return
	 * @throws Exception
	 */
	public static Record parseRecord(final JsonObject records) throws Exception {
		
		if(log.isDebugEnabled()){
		    log.debug("Event Now: ",records);
		}
		
		Record record = new Record();
		try {
			
			if(records.has(ApplicationConstants.PROPERTIES) && records.get(ApplicationConstants.PROPERTIES).getAsJsonObject()!=null){
			JsonObject	properties = records.get(ApplicationConstants.PROPERTIES).getAsJsonObject();
			record.setTime(parseTime(records));
			String subId = getSubscriptionId(records);
			String databaseName = getDatabaseName(records, properties);
			String accountId = getAccountId(records);
			Integer hashcode=properties.has(ApplicationConstants.ACTIVITY_ID)?properties.get(ApplicationConstants.ACTIVITY_ID).getAsString().hashCode():ApplicationConstants.UNKNOWN_STRING.hashCode();
			record.setSessionId(hashcode.toString());
			String dbName = subId.concat(":").concat(accountId);
			if(!databaseName.isEmpty()) {
				dbName = dbName.concat(":").concat(databaseName);
			}
			record.setDbName(dbName);
			record.setAppUserName(ApplicationConstants.UNKNOWN_STRING);
			record.setAccessor(parseAccessor(subId, accountId, properties ));
			record.getAccessor().setServiceName(record.getDbName());
			record.setSessionLocator(parserSessionLocator(records, properties));

			if (SuccessCode.findByNumber(getStatusCode(properties)) != null) {
				record.setData(parseData(records, properties));
			} else {
				record.setException(parseException(records, properties));
			}
		}
		} catch (Exception e) {
			log.error("Exception occurred while parsing event in parseRecord method:  ", e);
			throw e;
		}

		return record;
	}

	/**
	 * Method to get the database name from the JsonObject
	 * 
	 * @param records
	 * @param properties
	 * @return
	 */
	private static String getDatabaseName(JsonObject records, JsonObject properties) {
		String dbname = ApplicationConstants.UNKNOWN_STRING;
		if (getCategory(records).equalsIgnoreCase(ApplicationConstants.CONTROl_PLANE_REQUESTS)) {
			if (properties.has(ApplicationConstants.RESOURCE)) {
				String resourceUri = properties.get(ApplicationConstants.RESOURCE).getAsString();
				if (resourceUri.contains("/")) {
					dbname = resourceUri.split("/")[1];
				}
				return dbname;
			}
		} else if (getCategory(records).equalsIgnoreCase(ApplicationConstants.DATA_PLANE_REQUESTS)) {
			if (properties.has(ApplicationConstants.DATABASE_NAME)) {
				dbname = properties.get(ApplicationConstants.DATABASE_NAME).getAsString();
				return dbname;
			}
		} else {
			if (properties.has(ApplicationConstants.DATABASENAME)) {
				dbname = properties.get(ApplicationConstants.DATABASENAME).getAsString();
				return dbname;
			}
		}

		return dbname;
	}

	/**
	 * Method to get the status code from the JsonObject
	 * 
	 * @param properties
	 * @return
	 */
	private static int getStatusCode(JsonObject properties) {
		int code = SuccessCode.OK.getCode();
		if (properties.has(ApplicationConstants.STATUS_CODE)) {
			code = Integer.parseInt(properties.get(ApplicationConstants.STATUS_CODE).getAsString());
		} else if (properties.has(ApplicationConstants.HTTPS_STATUS)) {
			code = Integer.parseInt(properties.get(ApplicationConstants.HTTPS_STATUS).getAsString());
		}
		return code;
	}

	/**
	 * Method to get SubscriptionId from the JsonObject
	 * 
	 * @param records
	 * @return
	 */
	private static String getSubscriptionId(JsonObject records) {
		String subId = ApplicationConstants.UNKNOWN_STRING;
		if (records.has(ApplicationConstants.RESOURCEID)) {
			subId = records.get(ApplicationConstants.RESOURCEID).getAsString();
			if (subId.contains("/")) {
				subId = subId.split("/")[2];
			}
		}
		return subId;
	}

	/**
	 * Method to get category from the JsonObject
	 * 
	 * @param records
	 * @return
	 */
	private static String getCategory(JsonObject records) {
		String category = ApplicationConstants.UNKNOWN_STRING;
		if (records.has(ApplicationConstants.CATEGORY)) {
			category = records.get(ApplicationConstants.CATEGORY).getAsString();
		}
		return category;
	}

	/**
	 * Method to get accountId from the JsonObject
	 * 
	 * @param records
	 * @return
	 */
	private static String getAccountId(JsonObject records) {
		String accountId = ApplicationConstants.UNKNOWN_STRING;
		if (records.has(ApplicationConstants.RESOURCEID)) {
			accountId = records.get(ApplicationConstants.RESOURCEID).getAsString();
			String[] resourceId = accountId.split("/");
			accountId = resourceId[resourceId.length - 1];
		}
		return accountId;
	}

	/**
	 * Method to set the value into respective Exception Object and then return the
	 * value as response
	 * 
	 * @param records
	 * @param properties
	 * @return
	 */
	private static ExceptionRecord parseException(JsonObject records, JsonObject properties) {
		ExceptionRecord exception = new ExceptionRecord();
		exception.setExceptionTypeId(ApplicationConstants.EXCEPTION_TYPE_AUTHORIZATION_STRING);
		exception.setDescription("Error (" + getStatusCode(properties) + ")");
		exception.setSqlString(ApplicationConstants.NOT_AVAILABLE);
		return exception;
	}

	/**
	 * parseData() method will perform operation on JsonObject records, set the
	 * expected value into respective Data Object and then return the value as
	 * response
	 * 
	 * @param records
	 * @param properties
	 * @return
	 * @throws Exception
	 */
	private static Data parseData(JsonObject records, JsonObject properties) throws Exception {
		Data data = new Data();
		try {
			Construct construct = parseAsConstruct(records, properties);
			if (construct != null) {
				data.setConstruct(construct);
			}
		} catch (Exception e) {
			log.error(" Cosmos filter: Error parsing parseData method " + records, e);
			throw e;
		}
		return data;
	}

	/**
	 * parseAsConstruct() method will perform operation on JsonObject records, set
	 * the expected value into respective construct Object and then return the value
	 * as response
	 * 
	 * @param records
	 * @param properties
	 * @return
	 * @throws Exception
	 */
	private static Construct parseAsConstruct(JsonObject records, JsonObject properties) throws Exception {
		try {

			final Construct construct = new Construct();
			if (getCategory(records).equalsIgnoreCase(ApplicationConstants.QUERY_RUNTIME_STATISTICS)) {
				ArrayList<Sentence> sentences = Parser.parseQuerySentence(getQueryStatement(records, properties));
				construct.setSentences(sentences);
			} else {

				Sentence sentence = Parser.parseSentence(records, properties);
				construct.sentences.add(sentence);
			}
			if (getCategory(records).equalsIgnoreCase(ApplicationConstants.DATA_PLANE_REQUESTS)) {
				construct.setFullSql(getFullsqlForDataPlane(records, properties));
				construct.setRedactedSensitiveDataSql(getFullsqlForDataPlane(records, properties));
			} else if (getCategory(records).equalsIgnoreCase(ApplicationConstants.QUERY_RUNTIME_STATISTICS)) {
				String fullsql = getQueryStatement(records, properties);
				construct.setFullSql(fullsql);
				construct.setRedactedSensitiveDataSql(fullsql);
			} else {
				construct.setFullSql(records.toString());
				construct.setRedactedSensitiveDataSql(records.toString());
			}
			return construct;
		} catch (final Exception e) {
			log.error(" Cosmos filter: Error parsing parseAsConstruct method" + records, e);
			throw e;
		}
	}

	/**
	 * parseSentence() method will perform operation on JsonObject records, set the
	 * expected value into respective Sentence Object and then return the value as
	 * response
	 * 
	 * @param records
	 * @param properties
	 * @return
	 */
	private static Sentence parseSentence(JsonObject records, JsonObject properties) {
		Sentence sentence = new Sentence(
				records.has(ApplicationConstants.OPERATION) ? records.get(ApplicationConstants.OPERATION).getAsString()
						: ApplicationConstants.UNKNOWN_STRING);
		sentence.getObjects().add(parseSentenceObject(records, properties));
		return sentence;
	}

	/**
	 * parseSentenceObject() method will perform operation on JsonObject records,
	 * set the expected value into respective SentenceObject Object and then return
	 * the value as response
	 * 
	 * @param records
	 * @param properties
	 * @return
	 */
	private static SentenceObject parseSentenceObject(JsonObject records, JsonObject properties) {
		SentenceObject sentenceObject = null;
		if (getCategory(records).equalsIgnoreCase(ApplicationConstants.CONTROl_PLANE_REQUESTS)) {
			if(properties.has(ApplicationConstants.RESOURCE))
			{
				sentenceObject=new SentenceObject( properties.get(ApplicationConstants.RESOURCE).getAsString());
			}
			else if(properties.has(ApplicationConstants.ROLE_DEFINITION_ID)) {
				sentenceObject=new SentenceObject(properties.get(ApplicationConstants.ROLE_DEFINITION_ID).getAsString());	
			}
			else if(properties.has(ApplicationConstants.ROLE_ASSIGNMENT_ID)) {
				sentenceObject=new SentenceObject(properties.get(ApplicationConstants.ROLE_ASSIGNMENT_ID).getAsString());
			}
			else {
				sentenceObject=new SentenceObject(ApplicationConstants.UNKNOWN_STRING);
			}
		} else {
			sentenceObject = new SentenceObject(
					properties != null && properties.has(ApplicationConstants.REQUEST_RESOURCE)
							? decodeContent(properties.get(ApplicationConstants.REQUEST_RESOURCE).getAsString())
							: ApplicationConstants.UNKNOWN_STRING);
		}
		sentenceObject.setType(ApplicationConstants.COLLECTION); // this used to be default value, but since sentence is
																	// defined in

		return sentenceObject;
	}

	/**
	 * 
	 * Method to get object and verb from sqlQuery
	 * 
	 * @param sqlQuery
	 * @return
	 * @throws Exception
	 */
	private static ArrayList<Sentence> parseQuerySentence(String sqlQuery) throws Exception {
		ArrayList<Sentence> sentences;
		List<Map<String, Object>> listofmap;
		try {
			listofmap = SQLParser.parseSQL(sqlQuery);

			sentences = parseSentences(listofmap);
			return sentences;
		} catch (final Exception e) {
			log.error(" Cosmos filter: Error parsing parseQuerySentence " + e);
			throw e;
		}
	}

	/**
	 * parseSentences() method will perform operation on listofmap,set the expected
	 * value into respective Sentence Object and sentenceObject object then return
	 * the value as response
	 * 
	 * @param listofmap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static ArrayList<Sentence> parseSentences(List<Map<String, Object>> listofmap) {
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();
		for (Map<String, Object> map : listofmap) {
			Object verb = map.get(ApplicationConstants.VERB);
			Sentence sentence = new Sentence(verb.toString());

			Object objects = map.get(ApplicationConstants.OBJECTS);
			if (objects != null) {
				for (Object ob : (Set<Object>) objects) {
					SentenceObject sentenceObject = new SentenceObject(ob.toString().trim());
					sentenceObject.setType(ApplicationConstants.COLLECTION);
					sentence.getObjects().add(sentenceObject);
				}
			}
			List<Map<String, Object>> decs = (List<Map<String, Object>>) map.get(ApplicationConstants.DESCENDANTS);
			if (decs != null) {
				ArrayList<Sentence> descendants = parseSentences(decs);
				sentence.setDescendants(descendants);

			}
			sentences.add(sentence);

		}
		return sentences;
	}

	/**
	 * Method to get FullsqlDataPlane from JsonObject
	 * 
	 * @param records
	 * @param properties
	 * @return
	 */
	private static String getFullsqlForDataPlane(JsonObject records,JsonObject properties) {
		StringBuilder sb=new StringBuilder();
		sb.append("{ \"time\": ").append(records.get(ApplicationConstants.TIMESTAMP).toString()).append(",")
				.append("\"resourceId\": ").append(records.get("resourceId")).append(",").append("\"category\": ")
				.append(records.get(ApplicationConstants.CATEGORY)).append(",").append("\"operationName\": ")
				.append(records.get(ApplicationConstants.OPERATION)).append(",").append("\"properties\": { ")
				.append("\"activityId\": ").append(properties.get(ApplicationConstants.ACTIVITY_ID)).append(",")
				.append("\"requestResourceType\": ").append(properties.get(ApplicationConstants.REQUEST_RESOURCE_TYPE))
				.append(",").append("\"requestResourceId\": ")
				.append(decodeContent(properties.get(ApplicationConstants.REQUEST_RESOURCE).getAsString())).append(",")
				.append("\"statusCode\": ").append(properties.get(ApplicationConstants.STATUS_CODE)).append(",")
				.append("\"clientIpAddress\": ").append(properties.get(ApplicationConstants.CLIENT_IP)).append(",")
				.append("\"aadPrincipalId\": ").append(properties.get(ApplicationConstants.AAD_PRINCIPAL_ID))
				.append(",").append("\"subscriptionId\": ").append(properties.get(ApplicationConstants.SUBSCRIPTIONID))
				.append(",").append("\"databaseName\": ").append(properties.get(ApplicationConstants.DATABASE_NAME))
				.append(",").append("\"collectionName\": ").append(properties.get(ApplicationConstants.COllECTION_NAME))
				.append("} }");
		return sb.toString();
	}

	/**
	 * Method to get queryStatement from JsonObject
	 * 
	 * @param records
	 * @param properties
	 * @return
	 */
	private static String getQueryStatement(JsonObject records, JsonObject properties) {
		String queryStatement = StringUtils.EMPTY;
		if (!properties.has(ApplicationConstants.QUERYTEXT)) {
			return queryStatement;
		}
		String jsonStr = properties.get(ApplicationConstants.QUERYTEXT).getAsString();
		JsonObject jsonObj=new Gson().fromJson(jsonStr,JsonObject.class);
		String Statement = jsonObj.get(ApplicationConstants.QUERY).getAsString();
		Statement = Statement.replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)", " ");
		if (!StringUtils.isEmpty(Statement)) {
			queryStatement = Statement;
			return queryStatement;
		}

		return queryStatement;

	}


	/**
	 * parserSessionLocator() method will perform operation on JsonObject records,
	 * set the expected value into respective SessionLocator Object and then return
	 * the value as response
	 * 
	 * @param records
	 * @param properties
	 * @return
	 */
	private static SessionLocator parserSessionLocator(JsonObject records, JsonObject properties) {
		SessionLocator sessionLocator = new SessionLocator();
		String clientIp = properties.has(ApplicationConstants.CLIENT_IP)
				? properties.get(ApplicationConstants.CLIENT_IP).getAsString()
				: ApplicationConstants.DEFAULT_IP;

		sessionLocator.setIpv6(Boolean.FALSE);
		if (Util.isIPv6(clientIp)) {
			sessionLocator.setIpv6(true);
			sessionLocator.setServerIpv6(ApplicationConstants.DEFAULT_IPV6);
			sessionLocator.setClientIpv6(clientIp);
			sessionLocator.setClientIp(ApplicationConstants.UNKNOWN_STRING);
		} else { // ipv4
			sessionLocator.setServerIp(ApplicationConstants.DEFAULT_IP);
			sessionLocator.setClientIp(clientIp);
			sessionLocator.setClientIpv6(ApplicationConstants.UNKNOWN_STRING);
		}
		sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);
		sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
		return sessionLocator;
	}

	/**
	 * parseAccessor() method will perform operation on JsonObject records, set the
	 * expected value into respective Accessor Object and then return the value as
	 * response
	 * 
	 * @param subId
	 * @param accountId
	 * @param records
	 * @return
	 */
	private static Accessor parseAccessor(String subId, String accountId, JsonObject properties) {
		Accessor accessor = new Accessor();
		accessor.setClientHostName(ApplicationConstants.UNKNOWN_STRING);
		accessor.setDbUser(properties.has(ApplicationConstants.AAD_PRINCIPAL_ID)
				&& !properties.get(ApplicationConstants.AAD_PRINCIPAL_ID).getAsString().isEmpty()
						? properties.get(ApplicationConstants.AAD_PRINCIPAL_ID).getAsString()
						: ApplicationConstants.NOT_AVAILABLE);
		accessor.setServerType(ApplicationConstants.SERVER_TYPE);
		accessor.setDbProtocol(ApplicationConstants.DATA_PROTOCOL);
		accessor.setDbProtocolVersion(ApplicationConstants.UNKNOWN_STRING);
		accessor.setSourceProgram(ApplicationConstants.UNKNOWN_STRING);
		accessor.setServerHostName(
				!subId.isEmpty() && !accountId.isEmpty() ? subId.concat("-").concat(accountId).concat(".azure.com")
						: "cosmos.azure.com");
		accessor.setServerDescription(ApplicationConstants.UNKNOWN_STRING);
		accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);
		accessor.setClient_mac(ApplicationConstants.UNKNOWN_STRING);
		accessor.setClientHostName(ApplicationConstants.UNKNOWN_STRING);
		accessor.setCommProtocol(ApplicationConstants.UNKNOWN_STRING);
		accessor.setOsUser(ApplicationConstants.UNKNOWN_STRING);
		accessor.setClientOs(ApplicationConstants.UNKNOWN_STRING);
		accessor.setServerOs(ApplicationConstants.UNKNOWN_STRING);
		return accessor;
	}

	/**
	 * Method to get the time from JsonObject, set the expected value into
	 * respective Time Object and then return the value as response
	 * 
	 * @param records
	 * @return
	 */
	private static Time parseTime(JsonObject records) {
		String dateString = records.get(ApplicationConstants.TIMESTAMP).getAsString();
		ZonedDateTime date = ZonedDateTime.parse(dateString);
		long millis = date.toInstant().toEpochMilli();
		int minOffset = date.getOffset().getTotalSeconds() / 60;
		return new Time(millis, minOffset, 0);
	}
	/**
	 *  This method uses UTF-8 encoding for decoding the URLs
	 * 
	 * @param obj
	 * @return
	 */
	public static String decodeContent(String obj) {
		try {
			if (obj != null)
				obj = URLDecoder.decode(obj, "UTF-8");

		} catch (Exception e) {
			log.error("Exception occured while decoding" + e.getMessage());
		}
		return obj;
	}

}
