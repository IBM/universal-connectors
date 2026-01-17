/*
 Copyright IBM Corp. 2021, 2022 All rights reserved.
 SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.apachesolrdb;

import java.net.URLDecoder;
import java.text.ParseException;
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

import co.elastic.logstash.api.Event;

/**
 * This class contains all the parsing methods for the solr logs and sets all
 * the required fields into guardium object
 * 
 *
 */
public class Parser {
	private static Logger log = LogManager.getLogger(Parser.class);
	final static DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
			.append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
	final static DateTimeFormatter DATE_TIME_FORMATTER = dateTimeFormatterBuilder.toFormatter();

	/**
	 * this method is used to parse data from event and set to record object
	 * 
	 * @param event
	 * @return record
	 * @throws Exception
	 */
	public static Record parseQtpRecord(final Event event) throws Exception {
		Record record = new Record();
		try {
			record.setSessionId(getSessionId(event));
			record.setAppUserName(ApplicationConstant.NOT_AVAILABLE);
			record.setDbName(Parser.getDbName(event));
			record.setAccessor(parseAccessor(event));
			record.setSessionLocator(parseSessionLocator(event));
			record.setTime(parseTime(event));
			if (event.getField(ApplicationConstant.LOG_TYPE) instanceof String
					&& event.getField(ApplicationConstant.LOG_TYPE).toString()
							.equalsIgnoreCase(ApplicationConstant.INFO_EVENT)) {
				record.setData(parseQtpData(event));
			} else {
				if (event.getField(ApplicationConstant.LOG_TYPE) instanceof String
						&& event.getField(ApplicationConstant.LOG_TYPE).toString()
								.equalsIgnoreCase(ApplicationConstant.ERROR)) {
					record.setException(parseQtpExceptionRecord(event));
				}
			}
		} catch (Exception e) {
			log.error("Exception occurred while parsing event in parseQtpRecord method:  ", e);
			throw e;
		}
		return record;
	}

	/**
	 * this method is used to get Core/Collection
	 * 
	 */
	public static String getCoreName(final Event event) {
		String core_value = ApplicationConstant.UNKNOWN_STRING;
		if (event.getField(ApplicationConstant.COLLECTION) instanceof String) {
			core_value = event.getField(ApplicationConstant.COLLECTION).toString();
			if (core_value.contains(ApplicationConstant.CORE_CHECK)
					&& !core_value.contains(ApplicationConstant.COLLECTION_CHECK)) {
				core_value = core_value.split(ApplicationConstant.SPLIT_BY_SPACE)[0]
						.split(ApplicationConstant.CORE_CHECK)[1];
			} else if (core_value.contains(ApplicationConstant.COLLECTION_CHECK)) {
				core_value = core_value.split(ApplicationConstant.SPLIT_BY_SPACE)[0]
						.split(ApplicationConstant.COLLECTION_CHECK)[1];
			}
		}
		return core_value;
	}

	/**
	 * this method is used to get Verb
	 * 
	 */
	public static String getVerb(final Event event) {
		String verb = ApplicationConstant.UNKNOWN_STRING;
		if (event.getField(ApplicationConstant.VERB) instanceof String) {
			verb = event.getField(ApplicationConstant.VERB).toString();
			if (verb.contains("/")) {
				verb = verb.split("/")[1];
			}
		}
		return verb;
	}

	/**
	 * this method is used to get SessionId
	 * 
	 */
	public static String getSessionId(final Event event) {
		String sessionId = ApplicationConstant.UNKNOWN_STRING;
		if (event.getField(ApplicationConstant.SERVER_IP) instanceof String) {
			sessionId = Integer
					.toString((event.getField(ApplicationConstant.SERVER_IP).toString() + getDbName(event)).hashCode());
		}
		return sessionId;
	}

	/**
	 * this method is used to get DbName
	 * 
	 */
	public static String getDbName(final Event event) {
		String corevalue = ApplicationConstant.UNKNOWN_STRING;
		if (event.getField(ApplicationConstant.CLASS) instanceof String && event.getField(ApplicationConstant.CLASS)
				.toString().equalsIgnoreCase(ApplicationConstant.HTTP_SOLR_CALL)) {
			if (event.getField(ApplicationConstant.QUERY_STRING).toString().contains(ApplicationConstant.SWAP_CORE)) {
				String coreList = (getSolrCall(event, ApplicationConstant.CORE_KEY_CHECK));
				corevalue = coreList.split(":")[0];
			} else {
				corevalue = (getSolrCall(event, ApplicationConstant.CORE_KEY_CHECK));
			}
		} else {
			corevalue = getCoreName(event);
		}
		return corevalue;
	}

	/**
	 * this method is used to set object and verb in HttpSolrCall
	 * 
	 */
	private static String getSolrCall(Event event, String key) {
		Map<String, String> map = new HashMap<>();
		String core = ApplicationConstant.UNKNOWN_STRING;
		String action = ApplicationConstant.UNKNOWN_STRING;
		if (event.getField(ApplicationConstant.QUERY_STRING) instanceof String) {
			String params = CommonUtils
					.formatFirstAndLastChar(event.getField(ApplicationConstant.QUERY_STRING).toString());
			String arr[] = params.split("&");
			int length = arr.length;
			for (String each : arr) {
				if (each.contains(ApplicationConstant.SPLIT_BY_EQUAL)
						&& each.split(ApplicationConstant.SPLIT_BY_EQUAL).length == 2)
					map.put(each.split(ApplicationConstant.SPLIT_BY_EQUAL)[0],
							each.split(ApplicationConstant.SPLIT_BY_EQUAL)[1]);
			}
			if ((ApplicationConstant.CORE_KEY_CHECK).equals(key)) {
				if (length > 0 && (CommonUtils.ManageCollection(params) || CommonUtils.ManageCluster(params)
						|| CommonUtils.ManageShard(params) || CommonUtils.ManageReplica(params)
						|| CommonUtils.ManageAlias(params))) {
					if (params.contains(ApplicationConstant.KEY_EQUAL_COLLECTION)) {
						core = map.get(ApplicationConstant.COLLECTION);
					} else if (params.contains(ApplicationConstant.KEY_EQUAL_NAME)
							&& !params.contains(ApplicationConstant.KEY_EQUAL_TARGET)
							&& !params.contains(ApplicationConstant.CLUSTERPROP)) {
						core = map.get(ApplicationConstant.KEY_NAME);
					} else if (params.contains(ApplicationConstant.KEY_EQUAL_TARGET)) {
						core = map.get(ApplicationConstant.KEY_TARGET);
					} else if (params.contains(ApplicationConstant.KEY_EQUAL_OTHER)
							&& !params.contains(ApplicationConstant.SWAP_CORE)) {
						core = map.get(ApplicationConstant.KEY_OTHER);
					} else if (params.contains(ApplicationConstant.KEY_EQUAL_CORE)
							&& !params.contains(ApplicationConstant.KEY_EQUAL_OTHER)) {
						core = map.get(ApplicationConstant.KEY_CORE);
					} else if (params.contains(ApplicationConstant.KEY_EQUAL_CORE)
							&& params.contains(ApplicationConstant.KEY_EQUAL_OTHER)
							&& params.contains(ApplicationConstant.SWAP_CORE)) {
						core = map.get(ApplicationConstant.KEY_CORE) + ":" + map.get(ApplicationConstant.KEY_OTHER);
					}
				}
				return core;
			}
			if ((ApplicationConstant.DB_KEY_CHECK).equals(key)) {
				if (length > 0 && (CommonUtils.ManageCollection(params) || CommonUtils.ManageCluster(params)
						|| CommonUtils.ManageShard(params) || CommonUtils.ManageReplica(params)
						|| CommonUtils.ManageAlias(params))) {
					action = map.get(ApplicationConstant.KEY_ACTION);
				}
				return action;
			}
		}
		return ApplicationConstant.UNKNOWN_STRING;
	}

	/**
	 * Using this method to set details about the user who accessed the Accessor
	 * 
	 * @param event
	 * @return Accessor
	 */
	public static Accessor parseAccessor(final Event event) {
		Accessor accessor = new Accessor();
		accessor.setClientHostName(ApplicationConstant.UNKNOWN_STRING);
		accessor.setDbUser(ApplicationConstant.NOT_AVAILABLE);
		accessor.setServerType(ApplicationConstant.SERVER_TYPE);
		accessor.setDbProtocol(ApplicationConstant.DATA_PROTOCOL);
		accessor.setDbProtocolVersion(ApplicationConstant.UNKNOWN_STRING);
		accessor.setSourceProgram(ApplicationConstant.UNKNOWN_STRING);
		accessor.setServerHostName(event.getField(ApplicationConstant.SERVER_HOSTNAME) instanceof String
				? event.getField(ApplicationConstant.SERVER_HOSTNAME).toString()
				: ApplicationConstant.UNKNOWN_STRING);
		accessor.setServiceName(Parser.getDbName(event));
		accessor.setServerDescription(ApplicationConstant.UNKNOWN_STRING);
		accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);
		accessor.setClient_mac(ApplicationConstant.UNKNOWN_STRING);
		accessor.setClientHostName(ApplicationConstant.UNKNOWN_STRING);
		accessor.setCommProtocol(ApplicationConstant.UNKNOWN_STRING);
		accessor.setOsUser(ApplicationConstant.UNKNOWN_STRING);
		accessor.setClientOs(ApplicationConstant.UNKNOWN_STRING);
		accessor.setServerOs(event.getField(ApplicationConstant.SERVER_OS) instanceof String
				? event.getField(ApplicationConstant.SERVER_OS).toString()
				: ApplicationConstant.UNKNOWN_STRING);
		return accessor;
	}

	/**
	 * Using this method describes location details about the data source
	 * connection/session: Who connected, from which client IP and port, to what
	 * server IP and port
	 * 
	 * @param event
	 * @return SessionLocator
	 * @throws ParseException
	 */
	public static SessionLocator parseSessionLocator(final Event event) {
		SessionLocator sessionLocator = new SessionLocator();
		sessionLocator.setIpv6(Boolean.FALSE);
		if (event.getField(ApplicationConstant.SERVER_IP) instanceof String) {
			String address = event.getField(ApplicationConstant.SERVER_IP).toString();
			if (Util.isIPv6(address)) {
				sessionLocator.setIpv6(true);
				sessionLocator.setServerIpv6(address);
				sessionLocator.setClientIpv6(ApplicationConstant.DEFAULT_IPV6);
			} else { // ipv4
				sessionLocator.setServerIp(address);
				sessionLocator.setClientIp(ApplicationConstant.DEFAULT_IP);
				sessionLocator.setServerIp(event.getField(ApplicationConstant.SERVER_IP).toString());
			}
			sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);
			sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
		}
		return sessionLocator;
	}

	/**
	 * Using this method to set time.
	 * 
	 * @param event
	 * @return Time
	 * @throws Exception
	 */
	public static Time parseTime(final Event event) throws Exception {
		if (event.getField(ApplicationConstant.TIMESTAMP) instanceof String) {
			try {
				String[] arr = event.getField(ApplicationConstant.TIMESTAMP).toString()
						.split(ApplicationConstant.SPLIT_BY_SPACE);
				LocalDate localdate = LocalDate.parse((arr[0]), DateTimeFormatter.ISO_LOCAL_DATE);
				LocalTime localtime = LocalTime.parse((arr[1]), DateTimeFormatter.ISO_TIME);
				ZonedDateTime date = ZonedDateTime.of(localdate, localtime, ZonedDateTime.now().getZone());
				long millis = date.toInstant().toEpochMilli();
				int minOffset = date.getOffset().getTotalSeconds() / 60;
				return new Time(millis, minOffset, 0);
			} catch (Exception e) {
				log.error("Exception occurred while parsing event in parseTime method: ", e);
				throw new Exception("Incorrect Time Format");
			}
		}
		return null;
	}

	/**
	 * Using this to set data object.
	 * 
	 * @param event
	 * @return Data
	 * @throws Exception
	 */
	public static Data parseQtpData(final Event event) throws Exception {
		Data data = new Data();
		data.setConstruct(parseQtpAsConstruct(event));
		return data;
	}

	/**
	 * Using this to get fullSql.
	 * 
	 * @param event
	 * @return fullSql
	 */
	private static String getFullSql(final Event event) {
		String fullsql = ApplicationConstant.UNKNOWN_STRING;
		if (event.getField(ApplicationConstant.COLLECTION) instanceof String) {
			String core_collection = "[" + event.getField(ApplicationConstant.COLLECTION) + "]";
			fullsql = core_collection + " " + event.getField(ApplicationConstant.CLASS) + " " + "["
					+ event.getField(ApplicationConstant.KEY_CORE) + "]" + " "
					+ event.getField(ApplicationConstant.WEBAPP_FOR_FULLSQL) + "="
					+ event.getField(ApplicationConstant.WEB_APP) + " "
					+ event.getField(ApplicationConstant.PATH_FOR_FULLSQL) + "="
					+ event.getField(ApplicationConstant.VERB) + " " + event.getField(ApplicationConstant.PARAMS)
					+ decodeContent(event.getField(ApplicationConstant.QUERY_STRING).toString());
		} else {
			fullsql = event.getField(ApplicationConstant.CLASS) + " " + "["
					+ event.getField(ApplicationConstant.KEY_CORE) + "]" + " "
					+ event.getField(ApplicationConstant.WEBAPP_FOR_FULLSQL) + "="
					+ event.getField(ApplicationConstant.WEB_APP) + " "
					+ event.getField(ApplicationConstant.PATH_FOR_FULLSQL) + "="
					+ event.getField(ApplicationConstant.VERB) + " " + event.getField(ApplicationConstant.PARAMS)
					+ decodeContent(event.getField(ApplicationConstant.QUERY_STRING).toString());
		}
		return fullsql;
	}

	/**
	 * Using this to perform operation on input, convert event into Construct Object
	 * and then return the value as response
	 *
	 * @param event
	 * @return Construct GUARDIUM Object
	 * @throws Exception
	 *
	 */

	public static Construct parseQtpAsConstruct(final Event event) throws Exception {
		final Construct construct = new Construct();
		Sentence sentence = Parser.parseQtpSentence(event);
		construct.sentences.add(sentence);
		construct.setRedactedSensitiveDataSql(getRedactedSql(event));
		construct.setFullSql(getFullSql(event));
		return construct;
	}

	/**
	 * Using this to set redactedData in HttpSolrCall
	 * 
	 * @param event
	 * @return redact
	 */
	private static String getRedactedData(Event event) {
		Map<String, String> redactMap = new LinkedHashMap<>();
		if (!(event.getField(ApplicationConstant.QUERY_STRING) instanceof String)) {
			return ApplicationConstant.UNKNOWN_STRING;
		}
		String params = CommonUtils.formatFirstAndLastChar(event.getField(ApplicationConstant.QUERY_STRING).toString());//core=testnew&other=TestCore&action=SWAP
		if (params.contains("}{")) {
			params = params.replace("}{", "&");
		}
		String arr[] = params.split("&");
		for (String each : arr) {
			if (each.contains(ApplicationConstant.SPLIT_BY_EQUAL)
					&& each.split(ApplicationConstant.SPLIT_BY_EQUAL).length == 2) {
				redactMap.put(each.split(ApplicationConstant.SPLIT_BY_EQUAL)[0],
						each.split(ApplicationConstant.SPLIT_BY_EQUAL)[1]);
				redactMap.put(each.split(ApplicationConstant.SPLIT_BY_EQUAL)[0], ApplicationConstant.MASK_STRING);//{core=?, other=?, action=?}
			}
		}
		String redact = redactMap.toString().replace(", ", "&");//{core=?&other=?&action=?}
		return redact;
	}

	/**
	 * Using this to set redactedDataSql
	 * 
	 * @param eventF
	 * @return redactedsql
	 */
	private static String getRedactedSql(final Event event) {
		String redactedsql = ApplicationConstant.UNKNOWN_STRING;
		if (event.getField(ApplicationConstant.COLLECTION) instanceof String) {
			String core_collection = "[" + event.getField(ApplicationConstant.COLLECTION) + "]";
			redactedsql = core_collection + " " + event.getField(ApplicationConstant.CLASS) + " " + "["
					+ event.getField(ApplicationConstant.KEY_CORE) + "]" + " "
					+ event.getField(ApplicationConstant.WEBAPP_FOR_FULLSQL) + "="
					+ event.getField(ApplicationConstant.WEB_APP) + " "
					+ event.getField(ApplicationConstant.PATH_FOR_FULLSQL) + "="
					+ event.getField(ApplicationConstant.VERB) + " " + event.getField(ApplicationConstant.PARAMS)
					+ getRedactedData(event);
		} else {
			redactedsql = event.getField(ApplicationConstant.CLASS) + " " + "["
					+ event.getField(ApplicationConstant.KEY_CORE) + "]" + " "
					+ event.getField(ApplicationConstant.WEBAPP_FOR_FULLSQL) + "="
					+ event.getField(ApplicationConstant.WEB_APP) + " "
					+ event.getField(ApplicationConstant.PATH_FOR_FULLSQL) + "="
					+ event.getField(ApplicationConstant.VERB) + " " + event.getField(ApplicationConstant.PARAMS)
					+ getRedactedData(event);
		}
		return redactedsql;
	}

	/**
	 * Using this to perform operation on input, convert event into Sentence and
	 * then return the value as response
	 *
	 * @param event
	 * @return Sentence GUARDIUM Object
	 * @throws Exception
	 *
	 */
	protected static Sentence parseQtpSentence(final Event event) throws Exception {
		Sentence sentence = null;
		if (event.getField(ApplicationConstant.CLASS) instanceof String && event.getField(ApplicationConstant.CLASS)
				.toString().equalsIgnoreCase(ApplicationConstant.HTTP_SOLR_CALL)) {
			String corevalue = (getSolrCall(event, ApplicationConstant.CORE_KEY_CHECK));
			String pathvalue = (getSolrCall(event, ApplicationConstant.DB_KEY_CHECK));
			sentence = new Sentence(ApplicationConstant.UNKNOWN_STRING);
			if (event.getField(ApplicationConstant.QUERY_STRING).toString().contains(ApplicationConstant.SWAP_CORE)) {
				List<String> objlist = Arrays.asList(corevalue.split(":"));
				for (int i = 0; i < objlist.size(); i++) {
					SentenceObject sentenceObject = new SentenceObject(objlist.get(i));
					sentenceObject.setType("collection");
					sentence.getObjects().add(sentenceObject);
				}
			} else {
				sentence.getObjects().add(parseQtpSentenceObject(corevalue));
			}
			sentence.setVerb(pathvalue);
			return sentence;
		} else if (event.getField(ApplicationConstant.VERB) != null) {
			final String core = getCoreName(event);
			sentence = new Sentence(core);
			sentence.getObjects().add(parseQtpSentenceObject(core));
			sentence.setVerb(getVerb(event));
			return sentence;
		}
		return sentence;
	}

	/**
	 * Using this to perform operation on input, convert String core into
	 * sentenceObject Object and then return the value as response
	 *
	 * @param String core
	 * @return sentenceobject
	 *
	 */
	protected static SentenceObject parseQtpSentenceObject(String core) {
		SentenceObject sentenceObject = null;
		sentenceObject = new SentenceObject(core);
		sentenceObject.setName(core);
		sentenceObject.setType("collection");
		return sentenceObject;
	}

	/**
	 * Using this method to set details about the user who accessed the Exception
	 * source
	 * 
	 * @param event
	 * @return Exception
	 */
	public static ExceptionRecord parseQtpExceptionRecord(final Event event) {
		ExceptionRecord exception = new ExceptionRecord();
		String original = (event.getField(ApplicationConstant.ERROR_MSG).toString());
		String sql_error_string = (event.getField(ApplicationConstant.ERROR_MSG).toString())
				.replaceAll("(\\t)|(\\r)|(\\n)", " ");
		if (sql_error_string.contains(ApplicationConstant.SOLR_SQL_ERROR_STRING)
				|| sql_error_string.contains(ApplicationConstant.SOLR_QTP_ERROR_STRING)) {
			String strPattern = "(?<=Failed to execute sqlQuery ').*?(?=' against JDBC connection)|(?<=Cannot parse ').*?(?=': Lexical error)";
			Matcher matcher = Pattern.compile(strPattern).matcher(sql_error_string);
			if (matcher.find()) {
				String match = matcher.group();
				if (sql_error_string.contains(ApplicationConstant.SOLR_SQL_ERROR_STRING)) {
					exception.setDescription(ApplicationConstant.SQL_ERROR_DESCRIPTION);
				} else {
					exception.setDescription(ApplicationConstant.LEX_ERROR_DESCRIPTION);
				}
				exception.setExceptionTypeId(ApplicationConstant.EXCEPTION_TYPE_AUTHORIZATION_STRING);
				exception.setSqlString(match);
			} else {
				exception.setExceptionTypeId(ApplicationConstant.EXCEPTION_TYPE_AUTHORIZATION_STRING);
				exception.setDescription(original.split("\\n|\\r|\\t")[0]);
				exception.setSqlString(ApplicationConstant.NOT_AVAILABLE);
			}
		} else {
			exception.setExceptionTypeId(ApplicationConstant.EXCEPTION_TYPE_AUTHORIZATION_STRING);
			exception.setDescription(original.split("\\n|\\r|\\t")[0]);
			exception.setSqlString(ApplicationConstant.NOT_AVAILABLE);
		}
		return exception;
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
			log.error("Exception occured while decoding query: " + e.getMessage());
		}
		return obj;
	}
}
