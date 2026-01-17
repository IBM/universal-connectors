/*
 Copyright IBM Corp. 2021, 2022 All rights reserved.

SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.couchdb;

import java.net.URLDecoder;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.guardium.couchdb.Parser;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import co.elastic.logstash.api.Event;

public class Parser {

	
	private static Logger log = LogManager.getLogger(Parser.class);


	/***
	 * Parses logs and returns a Guard Record object
	 * 
	 * @param data
	 * @return
	 * @throws Exception 
	 * @throws ParseException
	 */
	public static Record parseRecord(Event event) throws Exception {
		Record record = new Record();
		try {
			record.setAppUserName(ApplicationConstant.UNKNOWN_STRING);
			record.setAccessor(parseAccessor(event));
			record.setDbName(event.getField(ApplicationConstant.DB_NAME).toString());
			record.setSessionLocator(parseSessionLocator(event));
			record.setTime(getTime(event));
			String connectionId=ApplicationConstant.UNKNOWN_STRING;
			if(event.getField(ApplicationConstant.ID)!=null) {
				 connectionId=event.getField(ApplicationConstant.ID).toString();
			}
			Integer hashCode = (record.getSessionLocator().getServerIp() + record.getSessionLocator().getClientIp()
					+record.getSessionLocator().getClientPort()+record.getSessionLocator().getServerPort()+record.getDbName()+record.getAccessor().getDbUser()+connectionId).hashCode();
			record.setSessionId(hashCode.toString());
			if(event.getField(ApplicationConstant.STATUS)!= null && event.getField(ApplicationConstant.STATUS).toString().startsWith("2"))
				record.setData(parseData(event));
			else 
				record.setException(parseExceptionRecord(event));
		} catch (Exception e) {
			log.error("Exception occurred while parsing event in parseRecord method:  ", e);
			throw e;
		}
		return record;
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
		sessionLocator.setClientIp(event.getField(ApplicationConstant.CLIENT_IP)!=null 
				? event.getField(ApplicationConstant.CLIENT_IP).toString()
						: ApplicationConstant.DEFAULT_IP);
		sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
		sessionLocator.setServerIp(event.getField(ApplicationConstant.SERVER_IP)!= null 
				? event.getField(ApplicationConstant.SERVER_IP).toString()
						: ApplicationConstant.DEFAULT_IP);
		sessionLocator.setServerPort(event.getField(ApplicationConstant.SERVER_PORT) != null 
				? Integer.valueOf(event.getField(ApplicationConstant.SERVER_PORT).toString())
						: SessionLocator.PORT_DEFAULT);
		sessionLocator.setIpv6(Boolean.FALSE);
		sessionLocator.setClientIpv6(ApplicationConstant.UNKNOWN_STRING);
		sessionLocator.setServerIpv6(ApplicationConstant.UNKNOWN_STRING);
		return sessionLocator;
	}


	/**
	 * Using this method to set details about the user who accessed the Accessor
	 * 
	 * @param event
	 * @return Accessor
	 */
	public static Accessor parseAccessor(final Event event) {
		Accessor accessor = new Accessor();
		String username = null;
		accessor.setClientHostName(ApplicationConstant.UNKNOWN_STRING);
		if(event.getField(ApplicationConstant.USER_NAME)!= null) {
			 username = event.getField(ApplicationConstant.USER_NAME).toString();
			if(username.contains("undefined")) {
				username = ApplicationConstant.NA_STRING;
			}
		}
		accessor.setDbUser(username);
		accessor.setServerType(ApplicationConstant.SERVER_TYPE_STRING);
		accessor.setDbProtocol(ApplicationConstant.DATA_PROTOCOL);
		accessor.setDbProtocolVersion(ApplicationConstant.UNKNOWN_STRING);
		accessor.setSourceProgram(ApplicationConstant.UNKNOWN_STRING);
		accessor.setServerHostName(event.getField(ApplicationConstant.SERVER_HOSTNAME) != null
				? event.getField(ApplicationConstant.SERVER_HOSTNAME).toString()
						: ApplicationConstant.UNKNOWN_STRING);
		accessor.setServiceName(ApplicationConstant.SERVER_TYPE_STRING);
		accessor.setServerDescription(ApplicationConstant.UNKNOWN_STRING);
		accessor.setLanguage(ApplicationConstant.LANGUAGE);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
		accessor.setClient_mac(ApplicationConstant.UNKNOWN_STRING);
		accessor.setCommProtocol(ApplicationConstant.UNKNOWN_STRING);
		accessor.setOsUser(ApplicationConstant.UNKNOWN_STRING);
		accessor.setClientOs(ApplicationConstant.UNKNOWN_STRING);
		accessor.setServerOs(ApplicationConstant.UNKNOWN_STRING);
		return accessor;
	}

	public static Data parseData(final Event event){
		Data data = new Data();
		String logmessage = null ;
		if(event.getField(ApplicationConstant.LOGMESSAGE) != null) {
			logmessage = event.getField(ApplicationConstant.LOGMESSAGE).toString();
			if(logmessage.contains("?")) {
				String[] msg = logmessage.split("\\?");
				logmessage = msg[0] +"?'"+ msg[1]+"'";
			}
		}		
		data.setOriginalSqlCommand(event.getField(ApplicationConstant.VERB) != null
				? decodeContent("RESTAPI "+event.getField(ApplicationConstant.VERB).toString()+" uri=/"+event.getField(ApplicationConstant.DB_NAME).toString()+logmessage)
						: ApplicationConstant.UNKNOWN_STRING);

		if(data.getOriginalSqlCommand()!= null) {
			String Sql = data.getOriginalSqlCommand();
			Sql = Sql.replace("null", ApplicationConstant.UNKNOWN_STRING);
			data.setOriginalSqlCommand(Sql);
		}

		return data;
	}


	/**
	 * Using this method to set details about the user who accessed the Exception
	 * source
	 * 
	 * @param event
	 * @return Exception
	 */
	public static ExceptionRecord parseExceptionRecord(final Event event) {
		ExceptionRecord exception = new ExceptionRecord();
		exception.setSqlString(event.getField(ApplicationConstant.VERB) != null
				? decodeContent(event.getField(ApplicationConstant.VERB).toString()+" "+event.getField(ApplicationConstant.DB_NAME).toString())
						: ApplicationConstant.UNKNOWN_STRING);

		if(event.getField(ApplicationConstant.STATUS)!=null && event.getField(ApplicationConstant.STATUS).equals("401")) {
			exception.setExceptionTypeId(ApplicationConstant.EXCEPTION_TYPE_AUTHENTICATION_STRING);
			exception.setDescription("Authentication Failed (401)");
		}
		else {
			exception.setExceptionTypeId(ApplicationConstant.EXCEPTION_TYPE_AUTHORIZATION_STRING);
			exception.setDescription("Error (" + event.getField(ApplicationConstant.STATUS) + ")");
		}

		return exception;
	}

	public static Time getTime(final Event event) throws Exception {
		try{
			Optional<Object> optTimestamp = Optional.ofNullable(event.getField(ApplicationConstant.TIMESTAMP));
			Optional<Object> optTimeZone = Optional.ofNullable(event.getField(ApplicationConstant.MIN_OFFSET));
			if(optTimestamp.isPresent()){
				String dateString = optTimestamp.get().toString();

				if(!dateString.isEmpty()){

					LocalDateTime dt = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
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
			log.error("Exception occurred while parsing event in parseTime method: ", e);
			throw new Exception("Incorrect Time Format");
		}
		return null;
	}

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
