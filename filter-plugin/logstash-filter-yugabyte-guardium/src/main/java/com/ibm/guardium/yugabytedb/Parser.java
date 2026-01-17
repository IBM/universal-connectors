//
// Copyright 2021-2022 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.yugabytedb;

import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class Parser {

	private static Logger log = LogManager.getLogger(Parser.class);

	public static Record parseRecord(final Map data) throws ParseException {
		Record record = new Record();

		if (data != null) {
			if(log.isDebugEnabled()){
				log.debug("Event Now: ",data);
			}
			try{
				record.setAppUserName(Constants.NOT_AVAILABLE);

				Optional<Object> optDbName = Optional.ofNullable(data.get(Constants.DB_NAME));
				String dbName = Constants.UNKNOWN_STRING;
				if(optDbName.isPresent()) {
					dbName = optDbName.get().toString();
				}

				record.setDbName(dbName);

				Optional<Object> optType = Optional.ofNullable(data.get(Constants.TYPE));
				boolean isSqlLog = optType.isPresent() && optType.get().toString().equalsIgnoreCase(Constants.SQL);

				Double epochTimestamp = Double.valueOf(data.get(Constants.TIMESTAMP).toString());
				if(isSqlLog){
					epochTimestamp = epochTimestamp * 1000;
				}
				Time time = Parser.getTime(epochTimestamp.longValue());
				if (time != null) {
					record.setTime(time);
				}

				SessionLocator sessionLocator = Parser.parseSessionLocator(data);
				record.setSessionLocator(sessionLocator);
				record.setAccessor(Parser.parseAccessor(data));

				Optional optSessionID = Optional.ofNullable(data.get(Constants.SESSION_ID));
				if(optSessionID.isPresent()){
					record.setSessionId(optSessionID.get().toString());
				} else {
					Integer hashCode = (sessionLocator.getClientIp() + sessionLocator.getClientPort() + dbName
							+ sessionLocator.getServerIp() + sessionLocator.getServerPort()).hashCode();
					record.setSessionId(hashCode.toString());
				}

				if(isSqlLog){
					Optional optEventType = Optional.ofNullable(data.get(Constants.EVENT_TYPE));
					if(optEventType.isPresent()){
						String eventType = optEventType.get().toString().trim();
						boolean isError = Arrays.asList(Constants.AUTH_ERROR, Constants.SQL_ERROR).stream().anyMatch(eventType::equals);
						if (isError) {
							record.setException(Parser.parseException(data));
						} else {
							record.setData(Parser.parseData(data));
						}
					}
				} else {
					Optional optEventCategory = Optional.ofNullable(data.get(Constants.EVENT_CATEGORY));
					Optional optEventType = Optional.ofNullable(data.get(Constants.EVENT_TYPE));
					if(optEventCategory.isPresent()){
						String eventCategory = optEventCategory.get().toString().trim();
						if(optEventType.isPresent()) {
							String eventType = optEventType.get().toString().trim();
							boolean isAuthError = (eventCategory.equalsIgnoreCase(Constants.CATEGORY_AUTH)
									&& eventType.equalsIgnoreCase(Constants.LOGIN_ERROR));
							boolean isError = isAuthError || eventCategory.equalsIgnoreCase(Constants.CATEGORY_ERROR);
							if(isError){
								record.setException(Parser.parseException(data));
							} else {
								record.setData(Parser.parseData(data));
							}
						}
					}
				}

			} catch (Exception e) {
				log.error("Yugabyte filter: Error occurred while creating record object: " + data, e);
				throw e;
			}
		}
		return record;
	}

	public static Accessor parseAccessor(Map data) {
		Accessor accessor = new Accessor();

		try {
			Optional optUsername = Optional.ofNullable(data.get(Constants.USERNAME));
			if (optUsername.isPresent()) {
				accessor.setDbUser(optUsername.get().toString());
			} else {
				accessor.setDbUser(Constants.UNKNOWN_STRING);
			}

			Optional optServerOs = Optional.ofNullable(data.get(Constants.SERVER_OS));
			if (optServerOs.isPresent()) {
				accessor.setServerOs(optServerOs.get().toString());
			} else {
				accessor.setServerOs(Constants.NOT_AVAILABLE);
			}

			accessor.setClientOs(Constants.UNKNOWN_STRING);
			accessor.setClientHostName(Constants.UNKNOWN_STRING);
			accessor.setCommProtocol(Constants.UNKNOWN_STRING);

			accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
			accessor.setOsUser(Constants.UNKNOWN_STRING);
			accessor.setClient_mac(Constants.UNKNOWN_STRING);
			accessor.setServerDescription(Constants.UNKNOWN_STRING);

			Optional optServiceName = Optional.ofNullable(data.get(Constants.DB_NAME));
			if (optServiceName.isPresent()) {
				accessor.setServiceName(optServiceName.get().toString());
			} else {
				accessor.setServiceName(Constants.UNKNOWN_STRING);
			}

			accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);

			Optional<Object> optType = Optional.ofNullable(data.get(Constants.TYPE));
			String sourceProgram = Constants.NOT_AVAILABLE;
			boolean isSqlLog = optType.isPresent() && optType.get().toString().equalsIgnoreCase(Constants.SQL);

			String serverHostName = Constants.UNKNOWN_STRING;

			Optional optServerHost = Optional.ofNullable(data.get(Constants.SERVER_HOST));
			if (optServerHost.isPresent()) {
				serverHostName = optServerHost.get().toString();
			}
			String dbLogType;
			if (isSqlLog) {
				accessor.setServerType(Constants.SERVER_TYPE_PG);
				accessor.setDbProtocol(Constants.DB_PROTOCOL_PG);
				accessor.setLanguage(Constants.LANGUAGE_PG);
				dbLogType = Constants.POSTGRES + "_";
				Optional optApplicationName = Optional.ofNullable(data.get(Constants.APPLICATION_NAME));
				if (optApplicationName.isPresent()) {
					sourceProgram = optApplicationName.get().toString();
					boolean isValueAsUnknown = Arrays.asList(Constants.UNKNOWN_AS_VALUE).stream().anyMatch(sourceProgram::equalsIgnoreCase);
					if(isValueAsUnknown){
						sourceProgram = Constants.NOT_AVAILABLE;
					}
				}
			} else {
				accessor.setServerType(Constants.SERVER_TYPE_CASSANDRA);
				accessor.setDbProtocol(Constants.DB_PROTOCOL_CASSANDRA);
				accessor.setLanguage(Constants.LANGUAGE_CASSANDRA);
				dbLogType = Constants.CASSANDRA + "_";
			}

			accessor.setServerHostName(dbLogType + serverHostName);
			accessor.setSourceProgram(sourceProgram);
		} catch (Exception e){
			log.error("Yugabyte filter: Error occurred while parsing accessor object: " + data, e);
			throw e;
		}

		return accessor;
	}

	public static SessionLocator parseSessionLocator(Map data) {
		SessionLocator sessionLocator = new SessionLocator();
		try {
			sessionLocator.setIpv6(false);

			int clientPort = 0;
			int serverPort = 0;
			String clientIpAdd = Constants.UNKNOWN_STRING;
			String serverIpAdd = Constants.UNKNOWN_STRING;

			if (data.containsKey(Constants.CLIENT_IP) && data.get(Constants.CLIENT_IP) != null) {
				clientIpAdd = data.get(Constants.CLIENT_IP).toString();
			}

			if (data.containsKey(Constants.CLIENT_PORT) && data.get(Constants.CLIENT_PORT) != null) {
				clientPort = Integer.valueOf(data.get(Constants.CLIENT_PORT).toString());
			}

			if(data.containsKey(Constants.SERVER_IP)){
				serverIpAdd = data.get(Constants.SERVER_IP).toString();
			}

			sessionLocator.setClientIp(clientIpAdd);
			sessionLocator.setClientPort(clientPort);
			sessionLocator.setServerIp(serverIpAdd);
			sessionLocator.setServerPort(serverPort);

			sessionLocator.setClientIpv6(Constants.NOT_AVAILABLE);
			sessionLocator.setServerIpv6(Constants.NOT_AVAILABLE);
		} catch (Exception e) {
			log.error("Yugabyte filter: Error occurred while parsing session locator object:" + data, e);
			throw e;
		}
		return sessionLocator;
	}
	public static Time getTime(Long epochWithMilliseconds) {
		Instant epochInstant = Instant.ofEpochMilli(epochWithMilliseconds);
		OffsetDateTime date = epochInstant.atOffset(ZoneOffset.UTC);
		long millis = date.toInstant().toEpochMilli();
		int minOffset = date.getOffset().getTotalSeconds()/60;
		return new Time(millis, minOffset, 0);
	}
	public static ExceptionRecord parseException(Map data) {
		ExceptionRecord exceptionRecord = new ExceptionRecord();
		try{
			String eventType = data.get(Constants.EVENT_TYPE).toString();
			String exceptionTypeId = Constants.SQL_ERROR;
			if(eventType.equalsIgnoreCase(Constants.AUTH_ERROR) || eventType.equalsIgnoreCase(Constants.LOGIN_ERROR)){
				exceptionTypeId = Constants.LOGIN_FAILED;
			}
			exceptionRecord.setExceptionTypeId(exceptionTypeId);


			String sqlCommand = Constants.NOT_AVAILABLE;

			Optional optSQLCommand = Optional.ofNullable(data.get(Constants.QUERY));
			if(optSQLCommand.isPresent()){
				sqlCommand = optSQLCommand.get().toString()
						.replaceAll("^\"|\"$", "");
			}

			exceptionRecord.setSqlString(sqlCommand);

			Optional optExceptionDescription = Optional.ofNullable(data.get(Constants.ERROR_DESCRIPTION));
			String description = Constants.NOT_AVAILABLE;

			if(optExceptionDescription.isPresent()) {
				description = optExceptionDescription.get().toString();
			}
			exceptionRecord.setDescription(description);

		} catch (Exception e) {
			log.error("Yugabyte filter: Error occurred while parsing exception object:" + data, e);
			throw e;
		}
		return exceptionRecord;
	}

	public static Data parseData(Map event) {

		Data data = new Data();
		try {
			String sqlCommand = Constants.NOT_AVAILABLE;

			Optional optSQLCommand = Optional.ofNullable(event.get(Constants.QUERY));
			if(optSQLCommand.isPresent()){
				sqlCommand = optSQLCommand.get().toString()
						.replaceAll("^\"|\"$", "");
			}
			data.setOriginalSqlCommand(sqlCommand);

		} catch(Exception e) {
			log.error("Yugabyte filter: Error occurred while parsing data object:" + event, e);
			throw e;
		}
		return data;
	}
}