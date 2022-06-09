/*
#Copyright 2020-2021 IBM Inc. All rights reserved
#SPDX-License-Identifier: Apache-2.0
#*/
package com.ibm.guardium.azurepostgresql;

import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;

import java.util.Date;

import java.text.ParseException;

import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Accessor;

import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Data;

import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.ExceptionRecord;

import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Record;

import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.SessionLocator;

import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Time;

import co.elastic.logstash.api.Event;

public class Parser {

	private static Logger log = LogManager.getLogger(Parser.class);

	public static Record parseRecord(final Event e) throws ParseException {

		Record record = new Record();

		record.setSessionId(e.getField(Constants.SESSION_ID).toString());

		record.setDbName(e.getField(Constants.DATABASE_NAME).toString());

		record.setAppUserName(Constants.APP_USER_NAME);

		record.setTime(Parser.parseTimestamp(e));

		record.setSessionLocator(Parser.parseSessionLocator(e));

		record.setAccessor(Parser.parseAccessor(e));

		if (e.getField(Constants.SUCCEEDED).toString().contains("LOG")) {

			Data data = new Data();
			data.setOriginalSqlCommand(e.getField(Constants.STATEMENT).toString());
			record.setData(data);
			
		} else {
			if (e.getField(Constants.SUCCEEDED).toString().contains("FATAL") && (e.getField(Constants.SQL_STATE).toString().contains("28P01"))) 
			{
				
				ExceptionRecord exceptionRecord = new ExceptionRecord();
				exceptionRecord.setExceptionTypeId(Constants.LOGIN_ERROR);
				exceptionRecord.setDescription(e.getField(Constants.MESSAGE).toString());
				exceptionRecord.setSqlString(Constants.NA);
				record.setException(exceptionRecord);
			} else {
			
				ExceptionRecord exceptionRecord = new ExceptionRecord();
				exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);
				exceptionRecord.setDescription(e.getField(Constants.MESSAGE).toString());
				exceptionRecord.setSqlString(Constants.NA);
				record.setException(exceptionRecord);
			}
		}
		return record;
	}

	public static Time parseTimestamp(final Event e) {

		long millis = 0;
		try {

			String dateString = e.getField(Constants.TIMESTAMP).toString();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = (sdf.parse(dateString));
			millis = date.getTime();
		} 
		catch (Exception exe)
		{
			exe.printStackTrace();
		}
		return new Time(millis, 0, 0);
	}

	public static SessionLocator parseSessionLocator(final Event e) {

		SessionLocator sessionLocator = new SessionLocator();
		sessionLocator.setClientIp(e.getField(Constants.CLIENT_IP).toString());
		sessionLocator.setClientPort(Parser.parseClientPORT(e));
		sessionLocator.setServerIp(Constants.DEFAULT_IP);
		sessionLocator.setServerPort(Constants.DEFAULT_PORT);
		sessionLocator.setIpv6(false);
		sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
		sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);
	
		return sessionLocator;
	}

	public static int parseClientPORT(final Event e) {

		int i = 0;
		try {
			String s = e.getField(Constants.CLIENT_PORT).toString();
			i = Integer.valueOf(s);
		}
		catch (Exception exec)
		{
			exec.printStackTrace();
		}
		return i;
	}

	public static Accessor parseAccessor(final Event e) {

		Accessor accessor = new Accessor();
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
		accessor.setLanguage(Constants.LANGUAGE);
		accessor.setClientHostName(Constants.UNKNOWN_STRING);
		accessor.setClientOs(Constants.UNKNOWN_STRING);
		accessor.setDbUser(e.getField(Constants.USER_NAME).toString());
		accessor.setServerType(Constants.SERVER_TYPE_STRING);
		accessor.setCommProtocol(Constants.COMM_PROTOCOL);
		accessor.setDbProtocol(Constants.DATA_PROTOCOL_STRING);
		accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
		accessor.setClient_mac(Constants.UNKNOWN_STRING);
		accessor.setServerDescription(Constants.UNKNOWN_STRING);
		accessor.setServiceName(Constants.UNKNOWN_STRING);
		accessor.setServerOs(Constants.UNKNOWN_STRING);
		accessor.setServerHostName(e.getField(Constants.SERVER_HOSTNAME).toString());
		if(e.getField(Constants.APPLICATION_NAME).toString().contains("[unknown]"))
		{
			accessor.setSourceProgram(Constants.UNKNOWN_STRING);
		}
		else
		{
		accessor.setSourceProgram(e.getField(Constants.APPLICATION_NAME).toString());
		}
		accessor.setOsUser(e.getField(Constants.USER_NAME).toString());

		return accessor;
	}
}