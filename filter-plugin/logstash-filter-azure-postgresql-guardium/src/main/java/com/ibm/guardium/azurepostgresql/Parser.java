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

public class Parser {

	private static Logger log = LogManager.getLogger(Parser.class);

	public static Record parseRecord(final Event e) throws ParseException {

		Record record = new Record();

		String dbname = Constants.NA;
		if (e.getField(Constants.DATABASE_NAME) != null) {
			dbname = e.getField(Constants.DATABASE_NAME).toString();

		}
		record.setDbName(dbname);

		record.setAppUserName(Constants.APP_USER_NAME);

		record.setTime(Parser.parseTimestamp(e));

		record.setSessionLocator(Parser.parseSessionLocator(e));

		record.setAccessor(Parser.parseAccessor(e));

		parseSessionId(e, record);

		if (e.getField(Constants.SUCCEEDED).toString().contains("LOG")) {

			Data data = new Data();

			if (e.getField(Constants.STATEMENT) != null) {
				data.setOriginalSqlCommand(e.getField(Constants.STATEMENT).toString().replaceAll("^\\\"|\\\"$", "").replace("\"\"", "\""));

			} else {
				data.setOriginalSqlCommand(Constants.NA);
			}
			record.setData(data);

		} else {
			if (e.getField(Constants.SUCCEEDED).toString().contains("FATAL")
					&& (e.getField(Constants.PREFIX).toString().contains("28P01"))) {

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
		} catch (Exception exe) {
			exe.printStackTrace();
		}
		return new Time(millis, 0, 0);
	}

	public static void parseSessionId(final Event e, final Record record) {

		if (e.getField(Constants.SESSION_ID) != null) {
			record.setSessionId(e.getField(Constants.SESSION_ID).toString());
		} else {
			Integer hashCode = (record.getSessionLocator().getClientIp() + record.getSessionLocator().getClientPort()
					+ record.getDbName()).hashCode();
			record.setSessionId(hashCode.toString());
		}

	}

	public static SessionLocator parseSessionLocator(final Event e) {

		SessionLocator sessionLocator = new SessionLocator();

		String clientIp = Constants.DEFAULT_IP;
		if (e.getField(Constants.CLIENT_IP) != null) {
			clientIp = (e.getField(Constants.CLIENT_IP).toString());
		}
		sessionLocator.setClientIp(clientIp);

		int clientPort = 0;
		if (e.getField(Constants.CLIENT_PORT) != null) {
			clientPort = Parser.parseClientPort(e);
		}
		sessionLocator.setClientPort(clientPort);

		sessionLocator.setServerIp(Constants.DEFAULT_IP);
		sessionLocator.setServerPort(Constants.DEFAULT_PORT);
		sessionLocator.setIpv6(false);
		sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
		sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);

		return sessionLocator;
	}

	public static int parseClientPort(final Event e) {

		int i = 0;
		try {
			String s = e.getField(Constants.CLIENT_PORT).toString();
			i = Integer.valueOf(s);
		} catch (Exception exec) {
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

		String DbUser = Constants.NA;
		if (e.getField(Constants.USER_NAME) != null) {
			DbUser = e.getField(Constants.USER_NAME).toString();
		}
		accessor.setDbUser(DbUser);

		accessor.setServerType(Constants.SERVER_TYPE_STRING);
		accessor.setCommProtocol(Constants.COMM_PROTOCOL);
		accessor.setDbProtocol(Constants.DATA_PROTOCOL_STRING);
		accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
		accessor.setClient_mac(Constants.UNKNOWN_STRING);
		accessor.setServerDescription(Constants.UNKNOWN_STRING);

		String serviceName = Constants.NA;
		if (e.getField(Constants.DATABASE_NAME) != null) {
			serviceName = e.getField(Constants.DATABASE_NAME).toString();
		}
		accessor.setServiceName(serviceName);
		accessor.setServerOs(Constants.UNKNOWN_STRING);
		String ServerHostName = Constants.UNKNOWN_STRING;
		if (e.getField(Constants.SERVER_HOSTNAME) != null) {
			ServerHostName = e.getField(Constants.SERVER_HOSTNAME).toString();
		}

		accessor.setServerHostName(ServerHostName);

		String sourceProgram = Constants.NA;
		if (e.getField(Constants.APPLICATION_NAME) != null) {

			if (e.getField(Constants.APPLICATION_NAME).toString().contains("[unknown]")) {
				sourceProgram = Constants.UNKNOWN_STRING;
			} else {
				sourceProgram = (e.getField(Constants.APPLICATION_NAME).toString());
			}
		}
		accessor.setSourceProgram(sourceProgram);

		String OsUser = Constants.UNKNOWN_STRING;
		if (e.getField(Constants.USER_NAME) != null) {
			OsUser = (e.getField(Constants.USER_NAME).toString());
		}
		accessor.setOsUser(OsUser);
		return accessor;
	}
}