package com.ibm.guardium.saphana;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

			if (data.has(Constants.SCHEMA_NAME) && !data.get(Constants.SCHEMA_NAME).isJsonNull())
				record.setDbName(data.get(Constants.SCHEMA_NAME).getAsString());
			else
				record.setDbName(Constants.UNKNOWN_STRING);

			String dateString = Parser.parseTimestamp(data);
			Time time = Parser.getTime(dateString);
			if (time != null)
				if(data.has(Constants.OFFSET))
				{
					long t=(time.getTimstamp()) - (data.get(Constants.OFFSET).getAsInt()*60000);
					time.setTimstamp(t);
				}
				record.setTime(time);

			record.setSessionLocator(Parser.parseSessionLocator(data));
			record.setAccessor(Parser.parseAccessor(data));

			if (data.has(Constants.APP_USER) && !data.get(Constants.APP_USER).isJsonNull())
				record.setAppUserName(data.get(Constants.APP_USER).getAsString());
			else
				record.setAppUserName(Constants.NOT_AVAILABLE);

			if (data.get(Constants.ACTION_STATUS).getAsString().contains(Constants.UNSUCCESSFUL)) {
				record.setException(Parser.parseException(data));
			} else {
				record.setData(Parser.parseData(data));
			}
		}
		return record;
	}

	// ----------- TIME

	public static String parseTimestamp(final JsonObject data) {
		String dateString = null;
		dateString = data.get(Constants.TIMESTAMP).getAsString();
		return dateString;
	}

	public static Time getTime(String dateString) {
			if(dateString.matches("\\d+"))
			{
				long millis = Long.parseLong(dateString);
				return new Time(millis,0, 0);
			}
			else
			{
			ZonedDateTime date = ZonedDateTime.parse(dateString.replaceAll(" ", "T").concat("Z"),
				DateTimeFormatter.ISO_DATE_TIME);
			long millis = date.toInstant().toEpochMilli();
			int minOffset = date.getOffset().getTotalSeconds() / 60;
			return new Time(millis, minOffset, 0);
			}
	}

	// ----------- Session Locator

	private static SessionLocator parseSessionLocator(JsonObject data) {
		SessionLocator sessionLocator = new SessionLocator();
		sessionLocator.setIpv6(false);

		if (data.has(Constants.CLIENT_IP) && !data.get(Constants.CLIENT_IP).isJsonNull())
			sessionLocator.setClientIp(data.get(Constants.CLIENT_IP).getAsString());
		else
			sessionLocator.setClientIp(Constants.NOT_AVAILABLE);

		if (data.has(Constants.CLIENT_PORT) && !data.get(Constants.CLIENT_PORT).isJsonNull())
			sessionLocator.setClientPort(data.get(Constants.CLIENT_PORT).getAsInt());
		else
			sessionLocator.setClientPort(Constants.NOT_AVAILABLE_INT);

		if (data.has(Constants.SERVER_PORT) && !data.get(Constants.SERVER_PORT).isJsonNull())
			sessionLocator.setServerPort(data.get(Constants.SERVER_PORT).getAsInt());
		else
			sessionLocator.setServerPort(Constants.NOT_AVAILABLE_INT);

		if (data.has(Constants.SERVER_IP) && !data.get(Constants.SERVER_IP).isJsonNull())
			sessionLocator.setServerIp(data.get(Constants.SERVER_IP).getAsString());
		else
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

		if (data.has(Constants.DB_USER) && !data.get(Constants.DB_USER).isJsonNull())
			accessor.setDbUser(data.get(Constants.DB_USER).getAsString());
		else
			accessor.setDbUser(Constants.NOT_AVAILABLE);

		if (data.has(Constants.SERVER_HOST) && !data.get(Constants.SERVER_HOST).isJsonNull())
			accessor.setServerHostName(data.get(Constants.SERVER_HOST).getAsString());
		else
			accessor.setServerHostName(Constants.NOT_AVAILABLE);

		if (data.has(Constants.SOURCE_PROGRAM) && !data.get(Constants.SOURCE_PROGRAM).isJsonNull() && !data.get(Constants.SOURCE_PROGRAM).getAsString().contains("%"))
			accessor.setSourceProgram(data.get(Constants.SOURCE_PROGRAM).getAsString());
		else
			accessor.setSourceProgram(Constants.SOURCE_PROGRAM_HARD_CODE);

		if (data.has(Constants.CLIENT_HOST) && !data.get(Constants.CLIENT_HOST).isJsonNull())
			accessor.setClientHostName(data.get(Constants.CLIENT_HOST).getAsString());
		else
			accessor.setClientHostName(Constants.NOT_AVAILABLE);

		if (data.has(Constants.SERVER_HOST) && !data.get(Constants.SERVER_HOST).isJsonNull())
			accessor.setServerHostName(data.get(Constants.SERVER_HOST).getAsString());
		else
			accessor.setServerHostName(Constants.NOT_AVAILABLE);

		if (data.has(Constants.SERVICE_NAME) && !data.get(Constants.SERVICE_NAME).isJsonNull())
			accessor.setServiceName(data.get(Constants.SERVICE_NAME).getAsString());
		else
			accessor.setServiceName(Constants.NOT_AVAILABLE);

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
		try {
			if (inputJSON.has(Constants.EXEC_STATEMENT) && !inputJSON.get(Constants.EXEC_STATEMENT).isJsonNull())
				data.setOriginalSqlCommand(inputJSON.get(Constants.EXEC_STATEMENT).getAsString());
			else
				data.setOriginalSqlCommand(Constants.NOT_AVAILABLE);

		} catch (Exception e) {
			log.error("SAPHANA filter: Error parsing JSON " + inputJSON, e);
			throw e;
		}
		return data;
	}

	// ------ Error

	private static ExceptionRecord parseException(JsonObject data) {

		ExceptionRecord exceptionRecord = new ExceptionRecord();
		if (data.has(Constants.EXEC_STATEMENT)) {
			exceptionRecord.setSqlString(data.get(Constants.EXEC_STATEMENT).getAsString());
			exceptionRecord.setDescription(Constants.UNKNOWN_STRING);
			exceptionRecord.setExceptionTypeId(Constants.UNKNOWN_STRING);
		} else {
			exceptionRecord.setSqlString(Constants.UNKNOWN_STRING);
			exceptionRecord.setDescription(Constants.AUTH_FAIL);
			exceptionRecord.setExceptionTypeId(Constants.LOGIN_FAILED);

		}

		return exceptionRecord;
	}

}