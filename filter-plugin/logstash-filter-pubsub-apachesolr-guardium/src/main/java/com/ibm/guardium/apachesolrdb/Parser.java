/*
Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.apachesolrdb;

import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
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
 * this class contains all the parsing methods for the solr logs and sets all
 * the required fields into guardium object
 * 
 */
public class Parser {
	private static Logger LOGGER = LogManager.getLogger(Parser.class);

	final static DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
			.append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

	final static DateTimeFormatter DATE_TIME_FORMATTER = dateTimeFormatterBuilder.toFormatter();

	/**
	 * this method parse solr qtp logs ,converts into jsonObject data and extracts
	 * required fields from it and sets into Record object.
	 * 
	 * @param JsonObject data
	 * @return record
	 * @throws Exception
	 */
	public static Record parseQtpRecord(JsonObject data) throws Exception {
		Record record = new Record();
		try {
			Map<String, String> map = new HashMap<>();
			getCommonDetails(data, map, ApplicationConstant.lOG_TYPE);
			if (map.get(ApplicationConstant.lOG_TYPE) != null
					&& map.get(ApplicationConstant.lOG_TYPE).equals(ApplicationConstant.INFO)) {
				getCommonDetails(data, map, ApplicationConstant.QTP_FIELDS_MAP);
				record.setData(parseQtpData(map));
			} else {
				getCommonDetails(data, map, ApplicationConstant.EXCEPTION);
				if (map.get(ApplicationConstant.lOG_TYPE) != null
						&& map.get(ApplicationConstant.lOG_TYPE).equals(ApplicationConstant.ERROR)) {
					record.setException(parseQtpException(map));
				}
			}
			record.setDbName(Parser.getServiceName(data, map));
			record.setSessionId(map.get(ApplicationConstant.INSERT_ID));
			record.setAppUserName(ApplicationConstant.NOT_AVAILABLE);
			record.setAccessor(parseAccessor(data, map));
			record.setSessionLocator(parseSessionLocator(map));
			record.setTime(parseTime(map.get(ApplicationConstant.TIMESTAMP)));
		} catch (Exception e) {
			LOGGER.error("Exception occurred while parsing event in parseQtpRecord method:  ", e);
			throw e;
		}
		return record;
	}

	public static String getDbName(Map<String, String> map) {
		String corevalue = ApplicationConstant.UNKNOWN_STRING;
		if (map.get(ApplicationConstant.CLASS).equals(ApplicationConstant.HTTP_SOLR_CALL)) {
			if (map.get(ApplicationConstant.PARAMS).toString().contains(ApplicationConstant.SWAP_CORE)) {
				String coreList = (getSolrCall(map, ApplicationConstant.CORE_KEY_CHECK));
				corevalue = coreList.split(":")[0];
			} else {
				corevalue = (getSolrCall(map, ApplicationConstant.CORE_KEY_CHECK));
			}
		} else {
			corevalue = map.get(ApplicationConstant.CORE).toString();
		}
		return corevalue;
	}

	/**
	 * this method used to set all the accessor fields which is required in guardium
	 * object.
	 * 
	 * @param map
	 * @return accessor
	 */
	private static Accessor parseAccessor(JsonObject data, Map<String, String> map) {
		Accessor accessor = new Accessor();
		accessor.setDbUser(ApplicationConstant.NOT_AVAILABLE);
		accessor.setDbProtocol(ApplicationConstant.DATA_PROTOCOL_STRING);
		accessor.setDbProtocolVersion(ApplicationConstant.UNKNOWN_STRING);
		accessor.setServerType(ApplicationConstant.SERVER_TYPE_STRING);
		accessor.setSourceProgram(ApplicationConstant.UNKNOWN_STRING);
		accessor.setServerHostName(getHostName(data, map));
		accessor.setServiceName(getServiceName(data, map));
		accessor.setServerDescription(ApplicationConstant.UNKNOWN_STRING);
		accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);
		accessor.setClient_mac(ApplicationConstant.UNKNOWN_STRING);
		accessor.setClientHostName(ApplicationConstant.UNKNOWN_STRING);
		accessor.setCommProtocol(ApplicationConstant.UNKNOWN_STRING);
		accessor.setOsUser(ApplicationConstant.UNKNOWN_STRING);
		accessor.setClientOs(ApplicationConstant.UNKNOWN_STRING);
		accessor.setServerOs(ApplicationConstant.UNKNOWN_STRING);
		return accessor;
	}

	/**
	 * this method retrieves HostName form data JsonObject and puts into map object
	 * 
	 * @param data
	 * @param map
	 * @return
	 */
	private static String getHostName(JsonObject data, Map<String, String> map) {
		String hostName = ApplicationConstant.UNKNOWN_STRING;
		if (!data.has(ApplicationConstant.RESOURCE)) {
			return hostName;
		}
		JsonObject resourceJsonObj = data.get(ApplicationConstant.RESOURCE).getAsJsonObject();
		if (resourceJsonObj.has(ApplicationConstant.TYPE) && resourceJsonObj.has(ApplicationConstant.LABELS)) {
			map.put(ApplicationConstant.RESOURCE_TYPE, resourceJsonObj.get(ApplicationConstant.TYPE).getAsString());
			JsonObject labelsJsonObject = resourceJsonObj.get(ApplicationConstant.LABELS).getAsJsonObject();
			if (labelsJsonObject.has(ApplicationConstant.INSTANCE_ID)
					&& labelsJsonObject.has(ApplicationConstant.PROJECT_ID)) {
				map.put(ApplicationConstant.INSTANCE_ID,
						labelsJsonObject.get(ApplicationConstant.INSTANCE_ID).getAsString());
				map.put(ApplicationConstant.PROJECT_ID,
						labelsJsonObject.get(ApplicationConstant.PROJECT_ID).getAsString());
			} else
				return hostName;
			hostName = map.get(ApplicationConstant.PROJECT_ID) + "_" + map.get(ApplicationConstant.INSTANCE_ID) + "_"
					+ ApplicationConstant.DOMAIN;
		}
		return hostName;
	}

	/**
	 * this method retrieves serviceName form data JsonObject and puts into map
	 * object
	 * 
	 * @param data
	 * @param map
	 * @return
	 */

	private static String getServiceName(JsonObject data, Map<String, String> map) {
		String serviceName = ApplicationConstant.UNKNOWN_STRING;
		if (!data.has(ApplicationConstant.RESOURCE)) {
			return serviceName;
		}
		JsonObject resourceJsonObj = data.get(ApplicationConstant.RESOURCE).getAsJsonObject();
		if (resourceJsonObj.has(ApplicationConstant.TYPE) && resourceJsonObj.has(ApplicationConstant.LABELS)) {
			map.put(ApplicationConstant.RESOURCE_TYPE, resourceJsonObj.get(ApplicationConstant.TYPE).getAsString());
			JsonObject labelsJsonObject = resourceJsonObj.get(ApplicationConstant.LABELS).getAsJsonObject();
			if (labelsJsonObject.has(ApplicationConstant.INSTANCE_ID)
					&& labelsJsonObject.has(ApplicationConstant.PROJECT_ID)) {
				map.put(ApplicationConstant.INSTANCE_ID,
						labelsJsonObject.get(ApplicationConstant.INSTANCE_ID).getAsString());
				map.put(ApplicationConstant.PROJECT_ID,
						labelsJsonObject.get(ApplicationConstant.PROJECT_ID).getAsString());
			}
			if (!getDbName(map).equals(ApplicationConstant.UNKNOWN_STRING)) {
				serviceName = map.get(ApplicationConstant.PROJECT_ID) + ":" + map.get(ApplicationConstant.INSTANCE_ID)
						+ ":" + getDbName(map);
			} else {
				serviceName = map.get(ApplicationConstant.PROJECT_ID) + ":" + map.get(ApplicationConstant.INSTANCE_ID);
			}
		}
		return serviceName;
	}

	/**
	 * this method method used to set all the sessionLocator fields which is
	 * required in guardium object.
	 * 
	 * @param map
	 * @return sessionLocator
	 */
	private static SessionLocator parseSessionLocator(Map<String, String> map) {
		SessionLocator sessionLocator = new SessionLocator();
		sessionLocator.setClientIp(ApplicationConstant.DEFAULT_IP);
		sessionLocator.setClientIpv6(ApplicationConstant.UNKNOWN_STRING);
		sessionLocator.setClientPort(ApplicationConstant.DEFAULT_PORT);
		sessionLocator.setIpv6(Boolean.FALSE);
		sessionLocator.setServerIp(ApplicationConstant.DEFAULT_IP);
		sessionLocator.setServerIpv6(ApplicationConstant.UNKNOWN_STRING);
		sessionLocator.setServerPort(ApplicationConstant.DEFAULT_PORT);
		return sessionLocator;
	}

	/**
	 * this method used to convert the timestamp field according to the format of
	 * guardium object.
	 * 
	 * @param dateString
	 * @return Time
	 * @throws Exception
	 */
	public static Time parseTime(String dateString) throws Exception {
		try {
			LocalDate localdate = LocalDate.parse(dateString.split(ApplicationConstant.SPLIT_BY_SPACE)[0],
					DateTimeFormatter.ISO_LOCAL_DATE);
			LocalTime localtime = LocalTime.parse(dateString.split(ApplicationConstant.SPLIT_BY_SPACE)[1],
					DateTimeFormatter.ISO_TIME);
			ZonedDateTime date = ZonedDateTime.of(localdate, localtime, ZonedDateTime.now().getZone());
			long millis = date.toInstant().toEpochMilli();
			int minOffset = date.getOffset().getTotalSeconds() / 60;
			return new Time(millis, minOffset, 0);
		} catch (Exception e) {
			LOGGER.error("Inside Parser :: Error parsing time " + e);
			throw new Exception("Incorrect Time Format");
		}
	}

	/**
	 * This method uses UTF-8 encoding for decoding the URLs.
	 *
	 * @param obj
	 * @return obj
	 */
	public static String decodeContent(String obj) {
		try {
			if (obj != null)
				obj = URLDecoder.decode(obj, "UTF-8");

		} catch (Exception e) {
			LOGGER.error("Exception occured while decoding query: " + e.getMessage());
		}
		return obj;
	}

	/**
	 * this method is used to set dbname and core in HttpSolrCall
	 * 
	 */
	private static String getSolrCall(Map<String, String> map, String key) {
		Map<String, String> solrCallMap = new HashMap<>();
		String core = ApplicationConstant.UNKNOWN_STRING;
		String action = ApplicationConstant.UNKNOWN_STRING;
		if (map.get(ApplicationConstant.PARAMS) != null) {
			String query = CommonUtils.formatFirstChar(map.get(ApplicationConstant.PARAMS));
			String params = CommonUtils.formatFirstAndLastChar(query);
			String arr[] = params.split("&");
			int length = arr.length;
			for (String each : arr) {
				if (each.contains("=") && each.split("=").length == 2) {
					solrCallMap.put(each.split("=")[0], each.split("=")[1]);
				}
			}
			if ((ApplicationConstant.CORE_KEY_CHECK).equals(key)) {
				if (length > 0 && (CommonUtils.ManageCollection(params) || CommonUtils.ManageCluster(params)
						|| CommonUtils.ManageShard(params) || CommonUtils.ManageReplica(params)
						|| CommonUtils.ManageAlias(params))) {
					if (params.contains(ApplicationConstant.KEY_EQUAL_COLLECTION)) {
						core = solrCallMap.get(ApplicationConstant.COLLECTION);
					} else if (params.contains(ApplicationConstant.KEY_EQUAL_NAME)
							&& !params.contains(ApplicationConstant.KEY_EQUAL_TARGET)
							&& !params.contains(ApplicationConstant.CLUSTERPROP)) {
						core = solrCallMap.get(ApplicationConstant.KEY_NAME);
					} else if (params.contains(ApplicationConstant.KEY_EQUAL_TARGET)) {
						core = solrCallMap.get(ApplicationConstant.KEY_TARGET);
					} else if (params.contains(ApplicationConstant.KEY_EQUAL_OTHER)
							&& !params.contains(ApplicationConstant.SWAP_CORE)) {
						core = solrCallMap.get(ApplicationConstant.KEY_OTHER);
					} else if (params.contains(ApplicationConstant.KEY_EQUAL_CORE)
							&& !params.contains(ApplicationConstant.KEY_EQUAL_OTHER)) {
						core = solrCallMap.get(ApplicationConstant.KEY_CORE);
					} else if (params.contains(ApplicationConstant.KEY_EQUAL_CORE)
							&& params.contains(ApplicationConstant.KEY_EQUAL_OTHER)
							&& params.contains(ApplicationConstant.SWAP_CORE)) {
						core = solrCallMap.get(ApplicationConstant.KEY_CORE) + ":"
								+ solrCallMap.get(ApplicationConstant.KEY_OTHER);
					}
				}
				return core;
			}
			if ((ApplicationConstant.DB_KEY_CHECK).equals(key)) {
				if (length > 0 && (CommonUtils.ManageCollection(params) || CommonUtils.ManageCluster(params)
						|| CommonUtils.ManageShard(params) || CommonUtils.ManageReplica(params)
						|| CommonUtils.ManageAlias(params))) {
					action = solrCallMap.get(ApplicationConstant.KEY_ACTION);
				}
				return action;
			}
		}
		return ApplicationConstant.UNKNOWN_STRING;
	}

	/**
	 * this method used to set all the Exception related fields for Qtp logs which
	 * is required in guardium object.
	 * 
	 * @param map
	 * @return exceptionRecord
	 */
	private static ExceptionRecord parseQtpException(Map<String, String> map) {
		ExceptionRecord exceptionRecord = new ExceptionRecord();
		String sql_error_string = map.get(ApplicationConstant.SQL_ERROR_STRING);
		if (sql_error_string.contains(ApplicationConstant.SOLR_SQL_ERROR_MARK_STRING)
				|| sql_error_string.contains(ApplicationConstant.SOLR_ERROR_QTP_MARK_STRING)) {
			String strPattern = "(?<=Failed to execute sqlQuery ')(.*\n?)(?=' against JDBC connection)|(?<=Cannot parse ')(.*\n?)(?=': Lexical error)";
			Matcher matcher = Pattern.compile(strPattern).matcher(sql_error_string);
			if (matcher.find()) {
				map.put(ApplicationConstant.SQL_ERROR_STRING, matcher.group());
				if (sql_error_string.contains(ApplicationConstant.SOLR_SQL_ERROR_MARK_STRING)) {
					exceptionRecord.setDescription(ApplicationConstant.SQL_ERROR_DESCRIPTION);
				} else {
					exceptionRecord.setDescription(ApplicationConstant.LEX_ERROR_DESCRIPTION);
				}
				exceptionRecord.setExceptionTypeId(ApplicationConstant.EXCEPTION_TYPE_AUTHORIZATION_STRING);
				exceptionRecord.setSqlString(map.get(ApplicationConstant.SQL_ERROR_STRING));
			} else {
				exceptionRecord.setExceptionTypeId(ApplicationConstant.EXCEPTION_TYPE_AUTHORIZATION_STRING);
				exceptionRecord.setDescription(map.get(ApplicationConstant.SQL_ERROR_STRING));
				exceptionRecord.setSqlString(ApplicationConstant.NOT_AVAILABLE);
			}
		} else {
			exceptionRecord.setExceptionTypeId(ApplicationConstant.EXCEPTION_TYPE_AUTHORIZATION_STRING);
			exceptionRecord.setDescription(map.get(ApplicationConstant.SQL_ERROR_STRING));
			exceptionRecord.setSqlString(ApplicationConstant.NOT_AVAILABLE);
		}
		return exceptionRecord;
	}

	/**
	 * this method used to set all the data related fields in Data object which is
	 * required in guardium object.
	 * 
	 * @param map
	 * @return data
	 * @throws Exception
	 */
	public static Data parseQtpData(Map<String, String> map) throws Exception {
		Data data = new Data();
		data.setConstruct(parseQtpConstruct(map));
		return data;
	}

	/**
	 * this method will perform operation on map input, set the value in Construct
	 * Object and then return the value as response
	 * 
	 * @param map
	 * @return Construct GUARDIUM Object
	 * @throws Exception
	 * 
	 */

	public static Construct parseQtpConstruct(Map<String, String> map) throws Exception {
		try {
			final Construct construct = new Construct();
			final Sentence sentence = Parser.parseQTPSentence(map);
			construct.sentences.add(sentence);
			construct.setRedactedSensitiveDataSql(getRedactedSql(map));
			construct.setFullSql(getFullSql(map));
			return construct;
		} catch (final Exception e) {
			LOGGER.error("Inside Parser: Error parsing qtp Construct " + e);
			throw e;
		}
	}

	/*
	 * Using this to set redactedDataSql
	 * 
	 * @param map
	 * 
	 * @return redactedsql
	 */

	private static String getRedactedSql(Map<String, String> map) {
		String redactedsql = ApplicationConstant.UNKNOWN_STRING;
		if (!map.get(ApplicationConstant.CORE_COLLECTION).equals("[]")) {
			redactedsql = map.get(ApplicationConstant.CORE_COLLECTION) + " " + map.get(ApplicationConstant.CLASS) + " "
					+ map.get(ApplicationConstant.CORE_FOR_FULLSQL) + " " + ApplicationConstant.WEBAPP_FOR_FULLSQL
					+ map.get(ApplicationConstant.WEBAPP_FOR_FULLSQL) + " " + ApplicationConstant.PATH_FOR_FULLSQL
					+ map.get(ApplicationConstant.PATH_FOR_FULLSQL) + " " + ApplicationConstant.PARAMS + "="
					+ getRedactedData(map);
		} else {
			redactedsql = map.get(ApplicationConstant.CLASS) + " " + map.get(ApplicationConstant.CORE_FOR_FULLSQL) + " "
					+ ApplicationConstant.WEBAPP_FOR_FULLSQL + map.get(ApplicationConstant.WEBAPP_FOR_FULLSQL) + " "
					+ ApplicationConstant.PATH_FOR_FULLSQL + map.get(ApplicationConstant.PATH_FOR_FULLSQL) + " "
					+ ApplicationConstant.PARAMS + "=" + getRedactedData(map);
		}
		return redactedsql;
	}

	/*
	 * Using this to set redactedData in HttpSolrCall
	 * 
	 * @param map
	 * 
	 * @return redact
	 */
	private static String getRedactedData(Map<String, String> map) {
		Map<String, String> redactMap = new LinkedHashMap<>();
		if (map.get(ApplicationConstant.PARAMS) != null) {
			String query = CommonUtils.formatFirstChar(map.get(ApplicationConstant.PARAMS));
			String params = CommonUtils.formatFirstAndLastChar(query);//core=firstCore&other=secondCore&action=SWAP			System.out.println(params);
			if (params.contains("}{")) {
				params = params.replace("}{", "&");
			}
			String arr[] = params.split("&");
			for (String each : arr) {
				if (each.contains("=") && each.split("=").length == 2) {
					redactMap.put(each.split("=")[0], each.split("=")[1]);
				}
				redactMap.put(each.split("=")[0], ApplicationConstant.MASK_STRING);//{core=?, other=?, action=?}
			}
			String redact = redactMap.toString().replace(", ", "&");//{core=?&other=?&action=?}
			return redact;
		}
		return ApplicationConstant.UNKNOWN_STRING;
	}

	/**
	 * Using this to get fullSql.
	 * 
	 * @param event
	 * @return fullSql
	 */

	private static String getFullSql(Map<String, String> map) {
		String fullsql = ApplicationConstant.UNKNOWN_STRING;
		if (!map.get(ApplicationConstant.CORE_COLLECTION).equals("[]")) {
			fullsql = map.get(ApplicationConstant.CORE_COLLECTION) + " " + map.get(ApplicationConstant.CLASS) + " "
					+ map.get(ApplicationConstant.CORE_FOR_FULLSQL) + " " + ApplicationConstant.WEBAPP_FOR_FULLSQL
					+ map.get(ApplicationConstant.WEBAPP_FOR_FULLSQL) + " " + ApplicationConstant.PATH_FOR_FULLSQL
					+ map.get(ApplicationConstant.PATH_FOR_FULLSQL) + " " + ApplicationConstant.PARAMS
					+ decodeContent(map.get(ApplicationConstant.PARAMS));
		} else {
			fullsql = map.get(ApplicationConstant.CLASS) + " " + map.get(ApplicationConstant.CORE_FOR_FULLSQL) + " "
					+ ApplicationConstant.WEBAPP_FOR_FULLSQL + map.get(ApplicationConstant.WEBAPP_FOR_FULLSQL) + " "
					+ ApplicationConstant.PATH_FOR_FULLSQL + map.get(ApplicationConstant.PATH_FOR_FULLSQL) + " "
					+ ApplicationConstant.PARAMS + decodeContent(map.get(ApplicationConstant.PARAMS));
		}
		return fullsql;
	}

	/**
	 * this method will perform operation on map input, set the value in Sentence
	 * Object and then return the value as response
	 * 
	 * @param map
	 * @return sentence
	 * @throws Exception
	 * 
	 */

	private static Sentence parseQTPSentence(Map<String, String> map) throws Exception {
		Sentence sentence = null;
		if (map.get(ApplicationConstant.CLASS).equals(ApplicationConstant.HTTP_SOLR_CALL)) {
			String corevalue = (getSolrCall(map, ApplicationConstant.CORE_KEY_CHECK));
			String pathvalue = (getSolrCall(map, ApplicationConstant.DB_KEY_CHECK));
			sentence = new Sentence(ApplicationConstant.UNKNOWN_STRING);
			if (map.get(ApplicationConstant.PARAMS).toString().contains(ApplicationConstant.SWAP_CORE)) {
				List<String> objlist = Arrays.asList(corevalue.split(":"));
				for (int i = 0; i < objlist.size(); i++) {
					SentenceObject sentenceObject = new SentenceObject(objlist.get(i));
					sentenceObject.setType("collection");
					sentence.getObjects().add(sentenceObject);
				}
			} else {
				sentence.getObjects().add(parseQTPSentenceObject(corevalue));
			}
			sentence.setVerb(pathvalue);
			return sentence;
		}
		if (map.get(ApplicationConstant.PATH) != null) {
			sentence = new Sentence(map.get(ApplicationConstant.CORE));
			sentence.getObjects().add(parseQTPSentenceObject(map.get(ApplicationConstant.CORE)));
			sentence.setVerb(map.get(ApplicationConstant.PATH));
			return sentence;
		}
		return sentence;
	}

	/**
	 * this method will perform operation on string input, set the value in
	 * SentenceObject and then return the response as sentenceObject
	 * 
	 * @param map
	 * @return sentence
	 * 
	 */
	private static SentenceObject parseQTPSentenceObject(String core) {
		SentenceObject sentenceObject = null;
		sentenceObject = new SentenceObject(core);
		sentenceObject.setName(core);
		sentenceObject.setType(ApplicationConstant.COLLECTION);
		return sentenceObject;
	}

	/**
	 * this method extract and puts different values into map on the basis of key
	 * using switch case
	 * 
	 * @param data
	 * @param map
	 * @param key
	 */
	private static void getCommonDetails(JsonObject data, Map<String, String> map, String key) throws Exception {
		if (data.has(ApplicationConstant.INSERT_ID)) {
			map.put(ApplicationConstant.INSERT_ID,
					CommonUtils.formatFirstAndLastChar(data.get(ApplicationConstant.INSERT_ID).toString()));
		}
		if (!data.has(ApplicationConstant.JSONPAYLOAD))
			return;
		JsonObject jsonpayloadObject = data.get(ApplicationConstant.JSONPAYLOAD).getAsJsonObject();
		if (!jsonpayloadObject.has(ApplicationConstant.MESSAGE))
			return;
		String message = jsonpayloadObject.get(ApplicationConstant.MESSAGE).getAsString();
		String[] arr = message.split(ApplicationConstant.SPLIT_BY_SPACE);
		if (arr.length > 8) {
			map.put(ApplicationConstant.CLASS, arr[9]);
			Pattern p = Pattern.compile("\\[(.*?)\\]");
			Matcher m = p.matcher(message);
			while (m.find()) {
				map.put(ApplicationConstant.CORE, m.group(1).trim());
				String core_collection = "[" + map.get(ApplicationConstant.CORE) + "]";
				map.put(ApplicationConstant.CORE_COLLECTION, core_collection);
				break;
			}
			map.put(ApplicationConstant.CORE_FOR_FULLSQL, arr[10]);
			String core = map.get(ApplicationConstant.CORE);
			if (core.contains(ApplicationConstant.CORE_CHECK) && !core.contains(ApplicationConstant.COLLECTION_CHECK)) {
				String collection = core.split(ApplicationConstant.SPLIT_BY_SPACE)[0];
				if (collection.split(ApplicationConstant.CORE_CHECK).length == 2) {
					map.put(ApplicationConstant.CORE, collection.split(ApplicationConstant.CORE_CHECK)[1]);
				}
			} else if (core.contains(ApplicationConstant.COLLECTION_CHECK)) {
				String collection = core.split(ApplicationConstant.SPLIT_BY_SPACE)[0];
				if (collection.split(ApplicationConstant.COLLECTION_CHECK).length == 2) {
					map.put(ApplicationConstant.CORE, collection.split(ApplicationConstant.COLLECTION_CHECK)[1]);
				}
			}
			map.put(ApplicationConstant.TIMESTAMP, arr[0] + " " + arr[1]);
			try {
				switch (key) {
				case ApplicationConstant.lOG_TYPE:
					map.put(ApplicationConstant.lOG_TYPE, arr[2]);
					break;
				case ApplicationConstant.QTP_FIELDS_MAP:
					String params;
					int index = 0;
					for (String each : arr) {
						if (each.contains(ApplicationConstant.PATH)) {
							if (each.split("=").length == 2) {
								String fullpath = each.split("=")[1];
								map.put(ApplicationConstant.PATH_FOR_FULLSQL, fullpath);
								if (each.split("=")[1].contains("/")) {
									String path = each.split("=")[1].split("/")[1];
									map.put(ApplicationConstant.PATH, path);
								}
							} else
								map.put(ApplicationConstant.PATH, ApplicationConstant.UNKNOWN_STRING);
						} else if (each.contains(ApplicationConstant.WEB_APP)) {
							if (each.split("=").length == 2) {
								String webapp = each.split("=")[1];
								map.put(ApplicationConstant.WEBAPP_FOR_FULLSQL, webapp);
							} else
								map.put(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstant.UNKNOWN_STRING);
						}
					}
					if (message.contains(ApplicationConstant.PARAMS)) {
						params = message.split(ApplicationConstant.PARAMS)[1];
						for (int i = 0; i < params.length(); i++) {
							if (params.charAt(i) == '}') {
								index = i;
							}
						}
						map.put(ApplicationConstant.PARAMS, params.substring(0, index + 1));
					}
					break;
				case ApplicationConstant.EXCEPTION:
					try {
						int ind = 0;
						for (int i = 0; i < message.length(); i++) {
							if (message.charAt(i) == ']') {
								ind = i;
								break;
							}
						}
						map.put(ApplicationConstant.SQL_ERROR_STRING, message.substring(ind + 2, message.length()));
					} catch (Exception e) {
						LOGGER.error("Inside getCommonDetails: Error parsing qtp Error events " + e);
					}
					break;
				default:
					break;
				}
			} catch (Exception e) {
				throw e;
			}
		}
	}
}