/*
Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.greenplumdb;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.guardium.universalconnector.commons.structures.Accessor;
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
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;

import co.elastic.logstash.api.Event;

/**
 * 
 * @author shivam_agarwal
 *
 */
public class Parser {

	private static final Logger LOGGER = LogManager.getLogger(Parser.class);
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS z");

	/**
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public static Record parseRecord(final Event event) throws Exception {

		final Record record = new Record();

		try {
			String fullSQl = event.getField(ApplicationConstant.QUERY) != null
					? event.getField(ApplicationConstant.QUERY).toString()
					: ApplicationConstant.EMPTY;

			event.setField(ApplicationConstant.REMOTE_PORT,
					event.getField(ApplicationConstant.REMOTE_PORT) != null
							&& !event.getField(ApplicationConstant.REMOTE_PORT).toString().isEmpty()
									? event.getField(ApplicationConstant.REMOTE_PORT).toString()
											.replaceAll(ApplicationConstant.DOUBLE_QUOTE, ApplicationConstant.EMPTY)
									: ApplicationConstant.EMPTY);

			event.setField(ApplicationConstant.QUERY,
					fullSQl.length() > 0 && fullSQl.substring(0, 1).contains("\"")
							? fullSQl.substring(1, fullSQl.length() - 1)
							: fullSQl);

			record.setAppUserName(ApplicationConstant.EMPTY);
			record.setDbName(event.getField(ApplicationConstant.DATABASE_NAME) != null
					? event.getField(ApplicationConstant.DATABASE_NAME).toString()
					: ApplicationConstant.EMPTY);
			record.setTime(parseTime(event));
			record.setSessionLocator(parseSessionLocator(event));
			record.setAccessor(parseAccessor(event, record));

			if (event.getField(ApplicationConstant.FILE_NAME) != null && (event.getField(ApplicationConstant.FILE_NAME)
					.toString().equals(ApplicationConstant.ELOG)
					|| event.getField(ApplicationConstant.FILE_NAME).toString().equals(ApplicationConstant.AUTH))) {
				record.setException(parseExceptionRecord(event));
			} else {
				record.setData(parseData(event));
			}

			record.setSessionId(parseSessionID(record, event));

		} catch (Exception e) {
			LOGGER.error("Exception occured while parsing the event in parseRecord method");
			throw e;
		}

		return record;

	}

	/**
	 * 
	 * @param record
	 * @param event
	 * @return
	 */
	private static String parseSessionID(final Record record, final Event event) {

		final String processID = event.getField(ApplicationConstant.PROCESS_ID) != null
				? event.getField(ApplicationConstant.PROCESS_ID).toString()
				: ApplicationConstant.EMPTY;

		final String connetionID = event.getField(ApplicationConstant.CONNECTION_ID) != null
				? event.getField(ApplicationConstant.CONNECTION_ID).toString()
				: ApplicationConstant.EMPTY;

		return (new Integer((connetionID + processID + record.getDbName()
				+ (new Integer(record.getSessionLocator().getClientPort())).toString()).hashCode())).toString();
	}

	/**
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public static ExceptionRecord parseExceptionRecord(final Event event) throws Exception {
		ExceptionRecord exception = new ExceptionRecord();
		exception.setSqlString(
				event.getField(ApplicationConstant.QUERY) != null ? event.getField(ApplicationConstant.QUERY).toString()
						: ApplicationConstant.EMPTY);

		exception.setExceptionTypeId(event.getField(ApplicationConstant.FILE_NAME) != null
				&& event.getField(ApplicationConstant.FILE_NAME).toString().equals(ApplicationConstant.ELOG)
						? ApplicationConstant.EXCEPTION_TYPE_AUTHORIZATION_STRING
						: ApplicationConstant.EXCEPTION_TYPE_AUTHENTICATION_STRING);

		exception.setDescription(event.getField(ApplicationConstant.EVENT_MESSAGE) != null
				? event.getField(ApplicationConstant.EVENT_MESSAGE).toString()
				: ApplicationConstant.EMPTY);
		return exception;

	}

	/**
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public static Data parseData(final Event event) throws Exception {
		Data data = new Data();
		try {

			data.setOriginalSqlCommand(parseQuery(event).replaceAll("[\"]{2,}", ApplicationConstant.EMPTY));
		} catch (Exception e) {
			LOGGER.error("Exception occured while parsing the event in parseData method");
			throw e;
		}

		return data;
	}

	/**
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	private static String parseQuery(final Event event) throws Exception {

		final String query = event.getField(ApplicationConstant.QUERY) != null
				? event.getField(ApplicationConstant.QUERY).toString()
				: ApplicationConstant.EMPTY;
		if (query.isEmpty()) {
			LOGGER.error("Exception occured in parseQuery method");
			throw new Exception("Query is Empty, will not be parsed");
		}

		return query;
	}

	/**
	 * 
	 * @param event
	 * @param record
	 * @return
	 * @throws Exception
	 */
	public static Accessor parseAccessor(final Event event, final Record record) throws Exception {
		Accessor accessor = new Accessor();
		try {

			accessor.setDbUser(event.getField(ApplicationConstant.USER_NAME) != null
					&& !event.getField(ApplicationConstant.USER_NAME).toString().isEmpty()
							? event.getField(ApplicationConstant.USER_NAME).toString()
							: ApplicationConstant.NOT_AVAILABLE);
			accessor.setServerHostName(event.getField(ApplicationConstant.SERVER_HOSTNAME) != null
					? event.getField(ApplicationConstant.SERVER_HOSTNAME).toString()
					: ApplicationConstant.EMPTY);
			accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
			accessor.setLanguage(ApplicationConstant.LANGUAGE);
			accessor.setServiceName(record.getDbName());
			accessor.setDbProtocol(ApplicationConstant.DB_PROTOCOL);
			accessor.setServerType(ApplicationConstant.SERVER_TYPE);
			accessor.setClientHostName(ApplicationConstant.EMPTY);
			accessor.setDbProtocolVersion(ApplicationConstant.EMPTY);
			accessor.setSourceProgram(ApplicationConstant.EMPTY);
			accessor.setServerDescription(ApplicationConstant.EMPTY);
			accessor.setCommProtocol(ApplicationConstant.EMPTY);
			accessor.setClientOs(ApplicationConstant.EMPTY);
			accessor.setServerOs(ApplicationConstant.EMPTY);

		} catch (Exception e) {
			LOGGER.error("Exception occurred while parsing event in parseAccessor method");
			throw e;
		}
		return accessor;
	}

	/**
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public static SessionLocator parseSessionLocator(final Event event) throws Exception {
		SessionLocator sessionLocator = new SessionLocator();
		try {

			final String remoteHost = event.getField(ApplicationConstant.REMOTE_HOST) != null
					&& !event.getField(ApplicationConstant.REMOTE_HOST).toString().isEmpty()
							? event.getField(ApplicationConstant.REMOTE_HOST).toString()
							: ApplicationConstant.DEFAULT_IPV4;

			final String serverIP = event.getField(ApplicationConstant.SERVER_IP) != null
					&& !event.getField(ApplicationConstant.SERVER_IP).toString().isEmpty()
							? event.getField(ApplicationConstant.SERVER_IP).toString()
							: ApplicationConstant.DEFAULT_IPV4;

			final String remotePort = !event.getField(ApplicationConstant.REMOTE_PORT).toString().isEmpty()
					? event.getField(ApplicationConstant.REMOTE_PORT).toString()
					: ApplicationConstant.DEFAULT_PORT;

			boolean ipv6 = false;

			if (isIPV6(remoteHost)) {
				sessionLocator.setClientIpv6(remoteHost);
				sessionLocator.setServerIpv6(serverIP);
				ipv6 = true;
			} else {
				sessionLocator.setClientIp(remoteHost.contains(ApplicationConstant.LOCAL) ? serverIP : remoteHost);
				sessionLocator.setServerIp(serverIP);
			}

			sessionLocator.setServerPort(Integer.parseInt(ApplicationConstant.DEFAULT_PORT));
			sessionLocator.setClientPort(Integer.parseInt(remotePort));
			sessionLocator.setIpv6(ipv6);

		} catch (Exception e) {
			LOGGER.error("Exception occurred while parsing event in parseSessionLocator method");
			throw e;
		}
		return sessionLocator;
	}

	/**
	 * 
	 * @param remoteHost
	 * @return
	 */
	private static boolean isIPV6(final String remoteHost) {

		return remoteHost.contains(ApplicationConstant.COLON);
	}

	/**
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public static Time parseTime(final Event event) throws Exception {
		try{
			Optional<Object> optTimestamp = Optional.ofNullable(event.getField(ApplicationConstant.TIMESTAMP));
			Optional<Object> optTimeZone = Optional.ofNullable(event.getField(ApplicationConstant.MIN_OFFSET));
			if(optTimestamp.isPresent()){
				String dateString = optTimestamp.get().toString();
				if(!dateString.isEmpty()){

					if(dateString.contains("EDT"))
						dateString = dateString.replaceAll("EDT", "").concat("Z");
					LocalDateTime dt = LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
					ZoneOffset offset = ZoneOffset.of(ZoneOffset.UTC.getId());

					if(optTimeZone.isPresent()){
						String timeZone = optTimeZone.get().toString();
						if(!timeZone.isEmpty()){
							offset = ZoneOffset.of(timeZone);
						}
					}
					ZonedDateTime zdt = dt.atOffset(offset).toZonedDateTime();
					long millis = zdt.toInstant().toEpochMilli();
					int minOffset = zdt.getOffset().getTotalSeconds() / 60;
					return new Time(millis, minOffset, 0);
				}
			}
		}
		catch (Exception e) {
			LOGGER.error("Exception occurred while parsing event in parseTime method: ", e);
			throw new Exception("Incorrect Time Format");
		}
		return null;
	}

}
