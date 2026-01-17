/*
 * Copyright 2020-2021 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */
package com.ibm.neptune.connector;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import com.ibm.neptune.connector.constant.ApplicationConstants;

import co.elastic.logstash.api.Event;

public class ParserHelper {

	/**
	 * This method get the event data as input, overrides the properties which are
	 * not in standard format. Creates the Guardium Object and returns it.
	 * 
	 * @param event
	 * @param record
	 * @return Record
	 * @throws Exception
	 */
	private static final Logger LOG = LogManager.getLogger(ParserHelper.class);

	public static Record parseRecord(Event event, Record record) throws Exception {
		String fullSQL = null;

		try {
			record.setAppUserName(ApplicationConstants.UNKNOWN_STRING);
			record.setDbName(event.getField(ApplicationConstants.DBNAME_PREFIX) != null
					? event.getField(ApplicationConstants.DBNAME_PREFIX).toString().split(ApplicationConstants.DOT)[0]
					: ApplicationConstants.UNKNOWN_STRING);

			fullSQL = event.getField(ApplicationConstants.PAYLOAD) != null
					? event.getField(ApplicationConstants.PAYLOAD).toString()
					: ApplicationConstants.UNKNOWN_STRING;

			if (event.getField(ApplicationConstants.MESSAGE) != null && event.getField(ApplicationConstants.MESSAGE)
					.toString().contains(ApplicationConstants.QUERY_TYPE_GREMLIN)) {

				if (event.getField(ApplicationConstants.REQUEST_HEADERS) != null
						&& event.getField(ApplicationConstants.REQUEST_HEADERS).toString()
								.contains(ApplicationConstants.UNKNOWN)
						&& !fullSQL.isEmpty() && fullSQL.contains("args")) {

					String temp = fullSQL.split("args=\\{gremlin=")[1];

					fullSQL = temp.contains("aliases") || temp.contains("userAgent") || temp.contains("batchSize")
							? temp.split("(, aliases)|(, userAgent)|(, batchSize)")[0]
							: fullSQL;

				}
			}
			event.setField(ApplicationConstants.PAYLOAD,
					fullSQL.replaceAll("(\\n)|(\\r)", ApplicationConstants.SPACE).trim());

			updateServerHost(event);

			record = prepareGuardRecordData(event, record);

			record.getAccessor().setServiceName(record.getDbName());

			Integer serverPort = new Integer(record.getSessionLocator().getServerPort());
			Integer clientPort = new Integer(record.getSessionLocator().getClientPort());
			Integer hashCode = (record.getSessionLocator().getServerIp() + record.getSessionLocator().getClientIp()
					+ serverPort.toString() + clientPort.toString() + record.getDbName()).hashCode();
			record.setSessionId(hashCode.toString());

		} catch (Exception e) {
			LOG.error("Exception occured while parsing the event in parseRecord method");
			throw e;
		}

		return record;
	}

	/**
	 * It prepares the Accessor data and set it in the Guardium Object. It accepts
	 * HttpHeaders as input and parses the information to get accessor data.
	 * 
	 * @param httpHeaders
	 * @param record
	 * @return record
	 */
	public static Accessor parseAccessor(Event event, Record record) throws Exception {

		Accessor accessor = new Accessor();

		String callerIAM = ApplicationConstants.UNKNOWN_STRING;
		String dbUser = ApplicationConstants.NOT_AVAILABLE;

		try {

			try {

				callerIAM = event.getField(ApplicationConstants.CALLER_IAM) != null && (event
						.getField(ApplicationConstants.CALLER_IAM).toString().contains(ApplicationConstants.UNKNOWN)
						|| event.getField(ApplicationConstants.CALLER_IAM).toString().contains("null"))
								? ApplicationConstants.UNKNOWN_STRING
								: event.getField(ApplicationConstants.CALLER_IAM).toString();

			} catch (Exception e) {

				callerIAM = ApplicationConstants.UNKNOWN_STRING;
			}

			if (!callerIAM.isEmpty()) {

				String[] temp = callerIAM.split(ApplicationConstants.DOUBLE_COLON)[1].split(":user/");
				if (temp.length == 1)
					dbUser = temp[0];
				else
					dbUser = temp[1];

			}

			accessor.setSourceProgram(ApplicationConstants.UNKNOWN_STRING);
			accessor.setServerHostName(
					event.getField(ApplicationConstants.SERVER_HOSTNAME_PREFIX) != null
							? event.getField(ApplicationConstants.SERVER_HOSTNAME_PREFIX).toString()
									.split(ApplicationConstants.DOT)[0] + ".aws.com"
							: ApplicationConstants.UNKNOWN_STRING);
			accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
			accessor.setServerType(ApplicationConstants.NEPTUNE);
			accessor.setClient_mac(ApplicationConstants.UNKNOWN_STRING);
			accessor.setClientHostName(ApplicationConstants.UNKNOWN_STRING);
			accessor.setClientOs(ApplicationConstants.UNKNOWN_STRING);
			accessor.setCommProtocol(ApplicationConstants.UNKNOWN_STRING);
			accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);
			accessor.setDbProtocol(ApplicationConstants.NEPTUNE_AWS);
			accessor.setDbProtocolVersion(ApplicationConstants.UNKNOWN_STRING);
			accessor.setDbUser(dbUser);
			accessor.setOsUser(ApplicationConstants.UNKNOWN_STRING);
			accessor.setServerDescription(ApplicationConstants.UNKNOWN_STRING);
			accessor.setServerOs(ApplicationConstants.UNKNOWN_STRING);
			accessor.setServiceName(record.getDbName());

		} catch (Exception e) {
			LOG.error("Exception occured while parsing the event in parseAccessor method");
			throw e;
		}

		return accessor;
	}

	/**
	 * This methods prepares the SessionLocator Data for Guardium Object, which
	 * takes client and server infos and set it in the Record Object.
	 * 
	 * @param client
	 * @param server
	 * @param record
	 * @return Record
	 */
	public static SessionLocator parseSessionLocator(final String client, final String server) throws Exception {
		SessionLocator sessionLocator = new SessionLocator();
		String serverIP = ApplicationConstants.DEFAULT_IP;
		String serverPort = ApplicationConstants.DEFAULT_PORT;

		String clientIP = ApplicationConstants.DEFAULT_IP;
		String clientPort = ApplicationConstants.DEFAULT_PORT;

		try {
			String[] serverDetails = server.split(ApplicationConstants.COLON);
			try {
				if (serverDetails[0] != null) {
					serverIP = serverDetails[0];
					for (String each : serverIP.split(ApplicationConstants.DOT)) {
						Integer.parseInt(each);
					}
				}
			} catch (Exception e) {
				serverIP = ApplicationConstants.DEFAULT_IP;
			}

			try {
				if (serverDetails[1] != null) {
					serverPort = serverDetails[1];
				}
				Integer.parseInt(serverPort);
			} catch (Exception e) {
				serverPort = ApplicationConstants.DEFAULT_PORT;

			}
			sessionLocator.setServerIp(serverIP);
			sessionLocator.setServerPort(Integer.parseInt(serverPort));

			// extract client data.

			String[] clientDetails = client.split(ApplicationConstants.COLON);
			try {
				if (clientDetails[0] != null) {
					clientIP = clientDetails[0];
					for (String each : clientIP.split(ApplicationConstants.DOT)) {
						Integer.parseInt(each);
					}
				}
			} catch (Exception e) {
				clientIP = ApplicationConstants.DEFAULT_IP;
			}

			try {

				if (clientDetails[1] != null) {
					clientPort = clientDetails[1];
				}
				Integer.parseInt(clientPort);
			} catch (Exception e) {
				clientPort = ApplicationConstants.DEFAULT_PORT;

			}
			sessionLocator.setClientIp(clientIP);
			sessionLocator.setClientPort(Integer.parseInt(clientPort));
			sessionLocator.setIpv6(isIPv6(clientIP));
			sessionLocator.setClientIpv6(ApplicationConstants.UNKNOWN_STRING);
			sessionLocator.setServerIpv6(ApplicationConstants.UNKNOWN_STRING);

		} catch (Exception e) {
			LOG.error("Exception occured while parsing the event in parseSessionLocator method", e);

		}

		return sessionLocator;
	}

	/**
	 * It Prepares the Data object and set it in the Data part of Guardium Record
	 * Object.
	 * 
	 * @param payload
	 * @param record
	 * @return Record
	 */
	public static Data parseData(Event event, Record record) throws Exception {
		Data data = new Data();
		Construct construct;
		try {
			construct = parseConstruct(event, record);
			data.setConstruct(construct);
		} catch (Exception e) {
			LOG.error("Exception occured while parsing the event in parseData method");
			throw e;
		}
		return data;
	}

	public static Construct parseConstruct(Event event, Record record) throws Exception {
		Construct construct = new Construct();
		try {

			if (event.getField(ApplicationConstants.MESSAGE).toString()
					.contains(ApplicationConstants.QUERY_TYPE_SPARQL)) {

				construct.setFullSql(!event.getField(ApplicationConstants.PAYLOAD).toString().isEmpty()
						? decodeContent(event.getField(ApplicationConstants.PAYLOAD).toString()
								.split(ApplicationConstants.EQUAL, 2)[1])
										.replaceAll("(\\n)|(\\r)", ApplicationConstants.SPACE).trim()
						: event.getField(ApplicationConstants.PAYLOAD).toString());
			} else {
				construct.setFullSql(event.getField(ApplicationConstants.PAYLOAD).toString());
			}

			construct.setRedactedSensitiveDataSql(event.getField(ApplicationConstants.MESSAGE).toString());
			construct.sentences.add(parseSentence(event, record));

		} catch (Exception e) {

			LOG.error("Exception occured while parsing the event in parseConstruct method");
			throw e;
		}

		return construct;

	}

	public static Sentence parseSentence(Event event, Record record) throws Exception {
		Sentence sentence = new Sentence(ApplicationConstants.VERB);

		Map<String, List<Object>> map = null;
		String fullSql = event.getField(ApplicationConstants.PAYLOAD).toString();

		try {

			if (event.getField(ApplicationConstants.MESSAGE).toString()
					.contains(ApplicationConstants.QUERY_TYPE_GREMLIN)
					&& event.getField(ApplicationConstants.MESSAGE).toString().contains("op='eval'")) {

				map = GremlinParser
						.parseGremlin(fullSql.replaceAll("(\\.\\s*next\\s*[(]\\s*[)])|(,\\s*(Order)?\\s*\\.?\\s*decr)",
								ApplicationConstants.UNKNOWN_STRING).replaceAll("\"+", "\""));

			} else if (event.getField(ApplicationConstants.MESSAGE).toString()
					.contains(ApplicationConstants.QUERY_TYPE_SPARQL)) {

				map = !fullSql.isEmpty()
						? fullSql.split(ApplicationConstants.EQUAL)[0].contains(ApplicationConstants.QUERY)
								? SparkQLParser
										.parseQuery(decodeContent(fullSql.split(ApplicationConstants.EQUAL, 2)[1]))
								: SparkQLParser
										.parseUpdate(decodeContent(fullSql.split(ApplicationConstants.EQUAL, 2)[1]))
						: null;

			}

			if (map != null) {
				int index = 0;

				for (Object verb : map.get(ApplicationConstants.VERB)) {
					if (index == 0) {
						sentence.setVerb(verb.toString());
						if (!map.get(ApplicationConstants.OBJECT).isEmpty()) {
							parseSentenceObject(sentence, map, index);
						}
					}

					else if (index <= map.get(ApplicationConstants.OBJECT).size() - 1) {
						Sentence descedant = new Sentence(verb.toString());
						parseSentenceObject(descedant, map, index);
						sentence.getDescendants().add(descedant);

					} else {
						break;
					}
					index++;

				}

				List<Object> dbNameList = map.get(ApplicationConstants.DBNAME);
				if (dbNameList != null && !dbNameList.isEmpty() && !dbNameList.get(0).toString().isEmpty()) {

					record.setDbName(record.getDbName() + ApplicationConstants.COLON + dbNameList.get(0).toString());
				}

			}

		} catch (Exception e) {

			LOG.error("Exception occured while parsing the event in parseSentence method");
			throw e;

		}

		return sentence;
	}

	@SuppressWarnings("unchecked")
	private static void parseSentenceObject(Sentence sentence, Map<String, List<Object>> map, int index) {

		for (Object object : (List<Object>) map.get(ApplicationConstants.OBJECT).get(index)) {
			SentenceObject sentenceObject = new SentenceObject(object.toString());
			sentenceObject.setType(ApplicationConstants.TYPE);
			sentence.getObjects().add(sentenceObject);
		}

	}

	/**
	 * It prepares the Time object, which accepts input Timestamp , process it and
	 * set it in the Time field of Guardium Record Object.
	 * 
	 * @param timestamp
	 * @param record
	 * @return Record
	 */
	public static Time parseTime(String timestamp) throws Exception {

		Time time = new Time();

		try {

			time.setTimstamp(Long.parseLong(timestamp));
			TimeZone currentTimeZone = TimeZone.getDefault();
			int minOffsetFromGMT = currentTimeZone.getOffset(Long.parseLong(timestamp));
			time.setMinOffsetFromGMT(minOffsetFromGMT);

		} catch (Exception e) {

			LOG.error("Exception occured while parsing the event in parseTime method");
			throw e;

		}

		return time;

	}

	/**
	 * This methods accepts the event object as input, prepares SessionLocator,
	 * Time, Data, and Accessor information and set it in the Guardium Object once
	 * prepared.
	 * 
	 * @param event
	 * @param record
	 * @return Record
	 * @throws Exception
	 */
	public static Record prepareGuardRecordData(Event event, Record record) throws Exception {

		String client = ApplicationConstants.UNKNOWN_STRING;
		String server = ApplicationConstants.UNKNOWN_STRING;
		String timestamp = ApplicationConstants.UNKNOWN_STRING;

		try {

			server = event.getField(ApplicationConstants.SERVER_HOST) != null
					? event.getField(ApplicationConstants.SERVER_HOST).toString()
					: ApplicationConstants.UNKNOWN_STRING;
			client = event.getField(ApplicationConstants.CLIENT_HOST) != null
					? event.getField(ApplicationConstants.CLIENT_HOST).toString()
					: ApplicationConstants.UNKNOWN_STRING;
			record.setSessionLocator(parseSessionLocator(client, server));

			timestamp = event.getField(ApplicationConstants.TIMESTAMP) != null
					? event.getField(ApplicationConstants.TIMESTAMP).toString()
					: ApplicationConstants.UNKNOWN_STRING;
			record.setTime(parseTime(timestamp));

			record.setAccessor(parseAccessor(event, record));

			record.setData(parseData(event, record));

		} catch (Exception e) {
			LOG.error("Exception occured while parsing the event in prepareGuardRecordData method");
			throw e;
		}
		return record;
	}

	/**
	 * This method checks if the input IP is of IPv6.
	 * 
	 * @param ip
	 * @return true, if its IPv6, and false if it's not IPv6.
	 */
	public static boolean isIPv6(String ip) {

		return ip.contains(ApplicationConstants.COLON);
	}

	public static String decodeContent(String obj) {

		try {
			if (obj != null)
				obj = URLDecoder.decode(obj, ApplicationConstants.UTF);

		} catch (Exception e) {
			LOG.error("Exception occured while decoding query: " + obj, e);
		}
		return obj;
	}

	/**
	 * This method will set the appropriate value for serverhost.
	 * 
	 * @param event
	 * @throws Exception
	 */
	public static void updateServerHost(Event event) throws Exception {

		try {

			if (event.getField(ApplicationConstants.SERVER_HOST) != null
					&& event.getField(ApplicationConstants.SERVER_HOST).toString()
							.contains(ApplicationConstants.FORWARDSLASH)) {

				String tempServer = event.getField(ApplicationConstants.SERVER_HOST).toString()
						.split(ApplicationConstants.FORWARDSLASH)[1];

				event.setField(ApplicationConstants.SERVER_HOST, tempServer);
			}

		} catch (Exception e) {
			LOG.error("Exception occured while parsing the event in updateServerHost method");
			throw e;

		}

	}

}
