/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.intersystemsiris;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

public class Parser {
	private static Logger log = LogManager.getLogger(Parser.class);
	public static final Pattern usedatabasepattern = Pattern.compile("^use\\s+(.*)$", Pattern.CASE_INSENSITIVE);

	/**
	 * Method to parse data from JsonObject and set to record object
	 * 
	 * @param records
	 * @return
	 * @throws Exception
	 */
	public static Record parseRecord(final JsonObject data) throws Exception {

		Record record = new Record();
		try {
			record.setTime(parseTime(data));
			record.setAppUserName(Constants.UNKNOWN_STRING);
			Matcher match = usedatabasepattern.matcher(getusedatabaseEventdata(data));
			if (match.find()) {
				record.setDbName(useDatabase(data));
			} else {
				record.setDbName(data.has(Constants.DB_NAME) && !data.get(Constants.DB_NAME).isJsonNull()
						&& !data.get(Constants.DB_NAME).getAsString().isEmpty()
								? data.get(Constants.DB_NAME).getAsString()
								: Constants.UNKNOWN_STRING);
			}
			record.setSessionLocator(parseSessionLocator(data));
			record.setAccessor(parseAccessor(data));
			record.getAccessor().setServiceName(record.getDbName());
			Integer hashCode = (record.getSessionLocator().getServerIp() + record.getSessionLocator().getClientIp()
					+ record.getDbName() + record.getAccessor().getDbUser()).hashCode();
			record.setSessionId(hashCode.toString());
			if (data.has(Constants.EVENT_TYPE)
					&& !data.get(Constants.EVENT_TYPE).getAsString().equalsIgnoreCase("LoginFailure")) {
				record.setData(parseData(data));
			} else {
				record.setException(parseException(data));
			}

		} catch (Exception e) {
			log.error("Exception occurred while parsing event in parseRecord method:  ", e);
			throw e;
		}

		return record;

	}

	/**
	 * Method to get the database name from the use database query
	 * 
	 * @param data
	 * @return
	 */

	public static String useDatabase(JsonObject data) {
		String usedatabase = getusedatabaseEventdata(data);
		String[] x = usedatabase.split("\\s");
		return x[x.length - 1];

	}

	/**
	 * Method to get the use database event data
	 * 
	 * @param data
	 * @return
	 */

	public static String getusedatabaseEventdata(JsonObject data) {
		String eventdata = data.has(Constants.EVENT_DATA) && !data.get(Constants.EVENT_DATA).isJsonNull()
				&& !data.get(Constants.EVENT_DATA).getAsString().isEmpty()
						? data.get(Constants.EVENT_DATA).getAsString().replaceAll("\\/\\*.*\\*\\/", "")
						: Constants.UNKNOWN_STRING;
		return eventdata;
	}

	/**
	 * Method to get Time from the data
	 * 
	 * @param data
	 * @return
	 */

	private static Time parseTime(JsonObject data) {

		String dateString = data.get(Constants.TIMESTAMP).getAsString();
		DateTimeFormatter originalFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		LocalDateTime dateTime = LocalDateTime.parse(dateString, originalFormatter);
		DateTimeFormatter desiredFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateString = dateTime.format(desiredFormatter);
		ZonedDateTime date = ZonedDateTime.parse(dateString);
		long millis = date.toInstant().toEpochMilli();
		int minOffset = date.getOffset().getTotalSeconds() / 60;
		return new Time(millis, minOffset, 0);

	}

	/**
	 * parseSessionLocator() method will perform operation on JsonObject data, set
	 * the expected value into respective Data Object and then return the value as
	 * response
	 * 
	 * @param data
	 * @return
	 */
	public static SessionLocator parseSessionLocator(JsonObject data) {
		SessionLocator sessionLocator = new SessionLocator();
		String clientIp = data.has(Constants.CLIENT_IP) && !data.get(Constants.CLIENT_IP).isJsonNull()
				&& !data.get(Constants.CLIENT_IP).getAsString().isEmpty() ? data.get(Constants.CLIENT_IP).getAsString()
						: Constants.DEFAULT_IP;
		String serverIp = data.has(Constants.SERVER_IP) && !data.get(Constants.SERVER_IP).isJsonNull()
				&& !data.get(Constants.SERVER_IP).getAsString().isEmpty()
				&& (validateipv4(data.get(Constants.SERVER_IP).getAsString())
						|| validateipv6(data.get(Constants.SERVER_IP).getAsString()))
								? data.get(Constants.SERVER_IP).getAsString()
								: Constants.DEFAULT_IP;
		sessionLocator.setIpv6(false);
		if (Util.isIPv6(serverIp)) {
			sessionLocator.setIpv6(true);
			if (!data.get(Constants.CLIENT_IP).isJsonNull()
					&& data.get(Constants.CLIENT_IP).getAsString().equals(Constants.DEFAULT_IP)) {
				sessionLocator.setClientIpv6(Constants.DEFAULT_IPV6);
			} else if (data.get(Constants.CLIENT_IP).isJsonNull()) {
				sessionLocator.setClientIpv6(Constants.DEFAULT_IPV6);
			} else {
				sessionLocator.setClientIpv6(clientIp);
			}
			sessionLocator.setClientIp(Constants.UNKNOWN_STRING);
			sessionLocator.setServerIpv6(serverIp);
			sessionLocator.setServerIp(Constants.UNKNOWN_STRING);
		} else { // IPv4
			sessionLocator.setServerIp(serverIp);
			sessionLocator.setClientIp(clientIp);
			sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
			sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);
		}
		sessionLocator.setClientPort(Constants.DEFAULT_PORT);
		sessionLocator.setServerPort(Constants.DEFAULT_PORT);
		return sessionLocator;

	}

	/**
	 * parseAccessor() method will perform operation on JsonObject records, set the
	 * expected value into respective Data Object and then return the value as
	 * response
	 * 
	 * @param data
	 * @return
	 */
	public static Accessor parseAccessor(JsonObject data) {

		Accessor accessor = new Accessor();
		accessor.setDbUser(data.has(Constants.DB_USER) && !data.get(Constants.DB_USER).isJsonNull()
				&& !data.get(Constants.DB_USER).getAsString().isEmpty() ? data.get(Constants.DB_USER).getAsString()
						: Constants.NOT_AVAILABLE);
		accessor.setServerHostName(
				data.has(Constants.SERVER_HOSTNAME) && !data.get(Constants.SERVER_HOSTNAME).isJsonNull()
						&& !data.get(Constants.SERVER_HOSTNAME).getAsString().isEmpty()
								? data.get(Constants.SERVER_HOSTNAME).getAsString()
								: Constants.UNKNOWN_STRING);
		accessor.setOsUser(data.has(Constants.OS_USER) && !data.get(Constants.OS_USER).getAsString().isEmpty()
				? data.get(Constants.OS_USER).getAsString()
				: Constants.UNKNOWN_STRING);
		accessor.setDbProtocol(Constants.DB_PROTOCOL);
		accessor.setServerType(Constants.SERVER_TYPE);
		accessor.setClientHostName(Constants.UNKNOWN_STRING);
		accessor.setSourceProgram(Constants.UNKNOWN_STRING);
		accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);
		accessor.setClient_mac(Constants.UNKNOWN_STRING);
		accessor.setCommProtocol(Constants.UNKNOWN_STRING);
		accessor.setClientOs(Constants.UNKNOWN_STRING);
		accessor.setServerOs(Constants.UNKNOWN_STRING);
		accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
		accessor.setServerDescription(Constants.UNKNOWN_STRING);
		return accessor;

	}

	/**
	 * Method to set the value into respective Exception Object and then return the
	 * value as response
	 * 
	 * @param data
	 * @return
	 */
	private static ExceptionRecord parseException(JsonObject data) {
		ExceptionRecord exception = new ExceptionRecord();
		exception.setExceptionTypeId(Constants.EXCEPTION_TYPE_AUTHORIZATION_STRING);
		exception.setDescription(data.has(Constants.EVENT_DATA) && !data.get(Constants.EVENT_DATA).isJsonNull()
				&& !data.get(Constants.EVENT_DATA).getAsString().isEmpty()
						? data.get(Constants.EVENT_DATA).getAsString()
						: Constants.NOT_AVAILABLE);
		exception.setSqlString(Constants.NOT_AVAILABLE);
		return exception;
	}

	/**
	 * parseData() method will perform operation on JsonObject data, set the
	 * expected value into respective Data Object and then return the value as
	 * response
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static Data parseData(JsonObject data) throws Exception {
		Data data1 = new Data();
		try {
			Construct construct = parseAsConstruct(data);
			if (construct != null) {
				data1.setConstruct(construct);
			}
		} catch (Exception e) {
			log.error("IRIS filter: Error parsing parseData method " + data, e);
			throw e;
		}
		return data1;
	}

	/**
	 * Method to get Management Operation Query
	 * 
	 * @param data
	 * @return
	 */
	public static String getManagementQuery(final JsonObject data) {
		String query = Constants.UNKNOWN_STRING;
		if (data.has(Constants.EVENT_TYPE)
				&& data.get(Constants.EVENT_TYPE).getAsString().equalsIgnoreCase("UserChange")
				|| data.get(Constants.EVENT_TYPE).getAsString().equalsIgnoreCase("RoleChange")
				|| data.get(Constants.EVENT_TYPE).getAsString().equalsIgnoreCase("ResourceChange")
				|| data.get(Constants.EVENT_TYPE).getAsString().equalsIgnoreCase("ConfigurationChange")) {
			query = !data.get(Constants.DESCRIPTION).isJsonNull() ? data.get(Constants.DESCRIPTION).getAsString()
					: Constants.UNKNOWN_STRING;
		}
		return query;
	}

	/**
	 * parseAsConstruct() method will perform operation on JsonObject data, set the
	 * expected value into respective construct Object and then return the value as
	 * response
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */

	public static Construct parseAsConstruct(final JsonObject data) throws Exception {
		try {
			final Construct construct = new Construct();
			String pattern = ":%qpar\\(\\d+\\)";
			String sqlQuery = Constants.UNKNOWN_STRING;
			Pattern createUserpattern = Pattern.compile(
					"(Create|Alter)\\s+user\\s+[a-zA-Z0-9%,&@#$^*:`'\".\\->_]+\\s+(IDENTIFY|IDENTIFIED)\\s+BY\\s+(.*)",
					Pattern.CASE_INSENSITIVE);
			if (!getManagementQuery(data).isEmpty()) {
				sqlQuery = getManagementQuery(data);
			} else {
				sqlQuery = !data.get(Constants.EVENT_DATA).isJsonNull()
						? data.get(Constants.EVENT_DATA).getAsString().replaceAll("(\\\\t)|(\\\\r)|(\\\\n)|(\\\\)", " ")
								.replaceAll(pattern, "''").replace("?", "''")
						: Constants.UNKNOWN_STRING;
			}
			ArrayList<Sentence> sentences = Parser.parseQuerySentence(sqlQuery);
			if (sentences.isEmpty()) {
				construct.sentences.add(new Sentence(Constants.UNKNOWN_STRING));
			} else {
				construct.setSentences(sentences);
			}
			String fullSql = !data.get(Constants.EVENT_DATA).isJsonNull() ? data.get(Constants.EVENT_DATA).getAsString()
					.replaceAll("\n|\r", " ").replaceAll("(\\\\t)|(\\\\r)|(\\\\n)", " ") : Constants.UNKNOWN_STRING;
			Matcher match = createUserpattern.matcher(fullSql);
			if (match.find()) {
				char[] password = new char[match.group(3).length()];
				Arrays.fill(password, 'x');
				fullSql = fullSql.replaceAll(Pattern.quote(match.group(3)), new String(password));
			}
			if (getManagementQuery(data).contains("Delete")) {
				construct.setFullSql(getManagementQuery(data));
				construct.setRedactedSensitiveDataSql(getManagementQuery(data));
			} else {
				construct.setFullSql(fullSql);
				construct.setRedactedSensitiveDataSql(getRedactedSql(fullSql));
			}
			return construct;
		} catch (final Exception e) {
			log.error("IRIS filter: Error parsing parseAsConstruct method" + data, e);
			throw e;

		}
	}

	/**
	 * Method to set ReadactedSql and mask all the parameter values in it
	 * 
	 * @param event
	 * @return
	 */

	private static String getRedactedSql(String redactedSql) {
		redactedSql = redactedSql.replaceAll("(%CallArgs\\(\\d+\\)=).*?(?= %CallArgs|$)", "$1?")
				.replaceAll("(%qpar\\(\\d+\\)=).*?(?= %qpar|$)", "$1?");
		if (redactedSql.contains("Create User:") || redactedSql.contains("Modify User:")) {
			Pattern Emailpattern = Pattern.compile(
					"EmailAddress:\\s+([\\w@.]+)?|EmailAddress\\s+modified:\\s+New\\s+value:\\s([\\w@.]+)?\\s+Old\\svalue:\\s([\\w@.]+)?",
					Pattern.CASE_INSENSITIVE);
			Matcher Emailmatch = Emailpattern.matcher(redactedSql);
			Pattern PhoneNoPattern = Pattern.compile(
					"PhoneNumber:\\s+(\\d+)|PhoneNumber\\s+modified:\\s+New\\s+value:\\s(\\d+)?\\s+[^:]+:\\s(\\d+)?",
					Pattern.CASE_INSENSITIVE);
			if (Emailmatch.find()) {
				for (int i = 1; i <= Emailmatch.groupCount(); i++) {
					if (Emailmatch.group(i) != null) {
						redactedSql = redactedSql.replaceAll(Emailmatch.group(i), "?");
					}
				}
			}
			Matcher PhoneNomatch = PhoneNoPattern.matcher(redactedSql);
			if (PhoneNomatch.find()) {
				for (int i = 1; i <= PhoneNomatch.groupCount(); i++) {
					if (PhoneNomatch.group(i) != null) {
						redactedSql = redactedSql.replaceAll(PhoneNomatch.group(i), "?");
					}
				}
			}
		}
		return redactedSql;

	}

	/**
	 * parseSentence() method will perform operation on listofmap,set the expected
	 * value into respective Sentence Object and sentenceObject object then return
	 * the value as response
	 * 
	 * @param listofmap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static ArrayList<Sentence> parseSentence(List<Map<String, Object>> listofmap) {
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();
		for (Map<String, Object> map : listofmap) {
			Object verb = map.get(Constants.VERB);
			Sentence sentence = new Sentence(verb.toString());

			Object objects = map.get(Constants.OBJECTS);
			if (objects != null) {
				for (Object ob : (Set<Object>) objects) {
					SentenceObject sentenceObject = new SentenceObject(ob.toString().trim());
					sentenceObject.setType(Constants.COLLECTION);
					sentence.getObjects().add(sentenceObject);
				}
			}
			List<Map<String, Object>> decs = (List<Map<String, Object>>) map.get(Constants.DESCENDANTS);
			if (decs != null) {
				ArrayList<Sentence> descendants = parseSentence(decs);
				sentence.setDescendants(descendants);

			}
			sentences.add(sentence);

		}
		return sentences;
	}

	/**
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
			listofmap = IrisParser.parseSQL(sqlQuery);

			sentences = parseSentence(listofmap);
			return sentences;
		} catch (final Exception e) {
			log.error(" IRIS filter: Error parsing parseQuerySentence " + e);
			throw e;
		}
	}

	/**
	 * Method to validate the IPv4 server ip
	 * 
	 * @param ip
	 * @return
	 */
	public static boolean validateipv4(final String ip) {
		return ip.matches(Constants.PATTERN_ipv4);
	}

	/**
	 * Method to validate the IPv6 server ip
	 * 
	 * @param ip
	 * @return
	 */
	public static boolean validateipv6(final String ip) {
		return ip.matches(Constants.PATTERN_ipv6);
	}
}
