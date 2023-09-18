package com.ibm.guardium.auroramysql;

import java.text.ParseException;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.structures.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Parser {

	private static Logger log = LogManager.getLogger(Parser.class);

	public static Record parseRecord(final JsonObject data) throws ParseException {
		Record record = new Record();

		if (data != null) {
			if (data.has(Constants.SESSION_ID) && !data.get(Constants.SESSION_ID).isJsonNull()) {
				record.setSessionId(data.get(Constants.SESSION_ID).getAsString());
			} else
				record.setSessionId(Constants.NOT_AVAILABLE);

			if (data.has(Constants.SERVER_INSTANCE) && !data.get(Constants.SERVER_INSTANCE).isJsonNull()) {
				if (data.has(Constants.DB_NAME) && !data.get(Constants.DB_NAME).isJsonNull())
					record.setDbName(data.get(Constants.SERVER_INSTANCE).getAsString() + ":"
							+ data.get(Constants.DB_NAME).getAsString());
				else
					record.setDbName(
							data.get(Constants.SERVER_INSTANCE).getAsString() + ":" + Constants.UNKNOWN_STRING);
			}

			long dateString = Parser.parseTimestamp(data);
			Time time = Parser.getTime(dateString);
			if (time != null)
				record.setTime(time);

			record.setSessionLocator(Parser.parseSessionLocator(data));
			record.setAccessor(Parser.parseAccessor(data));
			record.setAppUserName(Constants.APP_USER);

			if (data.get(Constants.ACTION_STATUS).getAsInt() != 0) {
				record.setException(Parser.parseException(data));
			} else {
				record.setData(Parser.parseData(data));
			}
		}
		return record;
	}

	// ----------- TIME

	public static long parseTimestamp(final JsonObject data) {
		long dateString = 0;
		dateString = data.get(Constants.TIMESTAMP).getAsLong();
		return dateString;
	}

	public static Time getTime(Long dateString) {

		long millis = dateString / 1000;
		return new Time(millis, 0, 0);

	}

	// ----------- Session Locator

	private static SessionLocator parseSessionLocator(JsonObject data) {
		SessionLocator sessionLocator = new SessionLocator();
		sessionLocator.setIpv6(false);

		if (data.has(Constants.CLIENT_IP) && !data.get(Constants.CLIENT_IP).isJsonNull())

		{
			String clientAdd = data.get(Constants.CLIENT_IP).getAsString();
			if (clientAdd.equalsIgnoreCase("localhost") || clientAdd.equals("%")) {
				sessionLocator.setClientIp(Constants.IP); // Set the appropriate IP for localhost
			} else {
				sessionLocator.setClientIp(clientAdd);
			}
		}
		else {
			sessionLocator.setClientIp(Constants.IP);
		}

		sessionLocator.setServerPort(Constants.PORT);
		sessionLocator.setClientPort(Constants.PORT);
		sessionLocator.setServerIp(Constants.IP);
		sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
		sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);

		return sessionLocator;
	}

	// ---------- Accessor

	public static Accessor parseAccessor(JsonObject data) {
		Accessor accessor = new Accessor();

		accessor.setDbProtocol(Constants.DB_PROTOCOL);
		accessor.setServerType(Constants.SERVER_TYPE);
		accessor.setServerHostName(data.get(Constants.SERVERHOSTNAME).getAsString());

		if (data.has(Constants.SERVER_INSTANCE) && !data.get(Constants.SERVER_INSTANCE).isJsonNull()) {
			if (data.has(Constants.DB_NAME) && !data.get(Constants.DB_NAME).isJsonNull())
				accessor.setServiceName(data.get(Constants.SERVER_INSTANCE).getAsString() + ":"
						+ data.get(Constants.DB_NAME).getAsString());
			else
				accessor.setServiceName(
						data.get(Constants.SERVER_INSTANCE).getAsString() + ":" + Constants.UNKNOWN_STRING);
		}

		accessor.setClientHostName(Constants.UNKNOWN_STRING);

		if (data.has(Constants.DB_USER) && !data.get(Constants.DB_USER).isJsonNull())
			accessor.setDbUser(data.get(Constants.DB_USER).getAsString());
		else
			accessor.setDbUser(Constants.NOT_AVAILABLE);

		accessor.setLanguage(Constants.Language);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
		accessor.setClient_mac(Constants.UNKNOWN_STRING);
		accessor.setClientOs(Constants.UNKNOWN_STRING);
		accessor.setCommProtocol(Constants.UNKNOWN_STRING);
		accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
		accessor.setOsUser(Constants.UNKNOWN_STRING);
		accessor.setServerDescription(Constants.UNKNOWN_STRING);
		accessor.setServerOs(Constants.UNKNOWN_STRING);

		return accessor;
	}

	// ------ Parse Data

	public static Data parseData(JsonObject inputJSON) {
		Data data = new Data();
		String sql;
		try {
			if (inputJSON.has(Constants.EXEC_STATEMENT) && !inputJSON.get(Constants.EXEC_STATEMENT).isJsonNull()) {
				sql = inputJSON.get(Constants.EXEC_STATEMENT).getAsString();
				data.setOriginalSqlCommand(sql.substring(1, sql.length() - 1));
			} else {
				data.setOriginalSqlCommand(inputJSON.get(Constants.AUDIT_ACTION).getAsString());
			}

		} catch (Exception e) {
			log.error("Aurora-Mysql filter: Error parsing JSON " + inputJSON, e);
			throw e;
		}
		return data;
	}

	// ------ Error

	private static ExceptionRecord parseException(JsonObject data) {

		ExceptionRecord exceptionRecord = new ExceptionRecord();
		if (data.has(Constants.EXEC_STATEMENT)) {
			exceptionRecord.setSqlString(data.get(Constants.EXEC_STATEMENT).getAsString());
			exceptionRecord.setDescription("ERROR Code=" + data.get(Constants.ACTION_STATUS).getAsString());
			exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);

		} else {
			exceptionRecord.setSqlString(Constants.UNKNOWN_STRING);
			exceptionRecord.setDescription(data.get(Constants.AUDIT_ACTION).getAsString());
			exceptionRecord.setExceptionTypeId(Constants.LOGIN_FAILED);

		}

		return exceptionRecord;
	}

}
