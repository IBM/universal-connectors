//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.cassandra;

import java.text.ParseException;
import java.util.Map;

import com.ibm.guardium.universalconnector.commons.structures.*;
import org.apache.commons.validator.routines.InetAddressValidator;

public class Parser {
	static ExceptionRecord exceptionRecord;

	public static UCRecord parseRecord(final Map<String, String> data) throws ParseException {

		UCRecord record = new UCRecord();

		record.setSessionId(Constants.UNKNOWN_STRING);

		record.setDbName(
				data.containsKey(Constants.KEYSPACE) ? data.get(Constants.KEYSPACE) : Constants.UNKNOWN_STRING);

		record.setAppUserName(Constants.UNKNOWN_STRING);

		record.setTime(new Time(Long.parseLong(data.get(Constants.TIMESTAMP)), 0, 0));

		record.setSessionLocator(Parser.parseSessionLocator(data));

		record.setAccessor(Parser.parseAccessor(data));

		setExceptionOrDataPart(record, data);

		return record;

	}

	public static void setDbName(UCRecord record, Map<String, String> data) {
		if (data.containsKey(Constants.KEYSPACE)) {
			record.setDbName(data.get(Constants.KEYSPACE));
		} else {
			record.setDbName(Constants.UNKNOWN_STRING);
		}
	}

	// Form Session Locator
	public static SessionLocator parseSessionLocator(Map<String, String> data) {
		SessionLocator sessionLocator = new SessionLocator();

		int clientPort = Constants.CLIENT_PORT_VALUE;
		int serverPort = Constants.SERVER_PORT_VALUE;
		String clientIpAdd = Constants.CLIENT_IP_VALUE;
		String serverIpAdd = Constants.SERVER_IP_VALUE;
		String clientIpv6Add = Constants.UNKNOWN_STRING;
		String serverIpv6Add = Constants.UNKNOWN_STRING;

		// Get an `InetAddressValidator`
		InetAddressValidator validator = InetAddressValidator.getInstance();

		if (data.containsKey(Constants.CLIENT_IP)) {
			String clientIp = data.get(Constants.CLIENT_IP).replace("/", "");

			if (validator.isValidInet4Address(clientIp)) {
				sessionLocator.setIpv6(false);
				clientIpAdd = clientIp;
				clientPort = Integer.parseInt(data.get(Constants.CLIENT_PORT));
			} else if (validator.isValidInet6Address(clientIp)) {
				sessionLocator.setIpv6(true);
				clientIpv6Add = clientIp;
			}
		}

		if (data.containsKey(Constants.SERVER_IP)) {
			String serverIp = data.get(Constants.SERVER_IP);
			if (validator.isValidInet4Address(serverIp)) {
				serverIpAdd = serverIp;
			} else if (validator.isValidInet6Address(serverIp)) {
				if (!sessionLocator.isIpv6())
					sessionLocator.setIpv6(true);
				serverIpv6Add = serverIp;
			}
		}

		sessionLocator.setClientIp(clientIpAdd);
		sessionLocator.setClientPort(clientPort);
		sessionLocator.setServerIp(serverIpAdd);
		sessionLocator.setServerPort(serverPort);

		sessionLocator.setClientIpv6(clientIpv6Add);
		sessionLocator.setServerIpv6(serverIpv6Add);

		return sessionLocator;
	}

	public static Accessor parseAccessor(Map<String, String> data) {
		Accessor accessor = new Accessor();
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
		accessor.setDbUser(data.get(Constants.USER));
		accessor.setServerType(Constants.SERVER_TYPE_STRING);
		accessor.setServerOs(Constants.UNKNOWN_STRING);
		accessor.setClientOs(Constants.UNKNOWN_STRING);
		accessor.setServerHostName(data.get(Constants.SERVER_HOSTNAME));
		accessor.setClientHostName(Constants.UNKNOWN_STRING);
		accessor.setDbProtocol(Constants.DATA_PROTOCOL_STRING);
		accessor.setCommProtocol(Constants.UNKNOWN_STRING);
		accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
		accessor.setOsUser(Constants.UNKNOWN_STRING);
		accessor.setSourceProgram(Constants.UNKNOWN_STRING);
		accessor.setClient_mac(Constants.UNKNOWN_STRING);
		accessor.setServerDescription(Constants.UNKNOWN_STRING);
		accessor.setServiceName(Constants.UNKNOWN_STRING);
		accessor.setLanguage(Constants.CASS_LANGUAGE);
		return accessor;
	}

	public static void setExceptionOrDataPart(final UCRecord record, Map<String, String> data) {
		String operation = data.get(Constants.OPERATION);
		if (data.get(Constants.CATEGORY).equals(Constants.AUTH)) {
			if (data.get(Constants.TYPE).equals(Constants.LOGIN_SUCCESS)) {
				setData(data, record);
			} else {
				exceptionRecord = new ExceptionRecord();
				String[] error = operation.split(Constants.OPERATION_SPLIT1);
				exceptionRecord.setExceptionTypeId(Constants.LOGIN_FAILED);
				setException(data, record, error);
			}
		} else if (data.get(Constants.CATEGORY).equals(Constants.ERROR)) {
			exceptionRecord = new ExceptionRecord();
			String[] error = new String[2];
			if(operation.contains(Constants.OPERATION_SPLIT2))
				error = operation.split(Constants.OPERATION_SPLIT2);
			else
				error = operation.split(Constants.OPERATION_SPLIT1);
			exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);
			setException(data, record, error);
		} else {
			setData(data, record);
		}
	}

	static void setException(Map<String, String> data, UCRecord record, String[] error) {

		exceptionRecord.setDescription(error[1]);
		exceptionRecord.setSqlString(error[0]);
		record.setException(exceptionRecord);
	}

	static void setData(Map<String, String> data, UCRecord record) {
		Data outputData = new Data();
		outputData.setOriginalSqlCommand(data.get(Constants.OPERATION));
		record.setData(outputData);
	}
}