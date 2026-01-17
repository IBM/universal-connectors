package com.ibm.guardium.teradatadb;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
	
	public static Record parseRecord(final Event e) throws ParseException {
		
			Record record = new Record();
		
			record.setSessionId(e.getField(Constants.SESSION_ID).toString());
			
			record.setDbName(Constants.NOT_AVAILABLE);
			
			record.setAppUserName(Constants.UNKNOWN_STRING);
			
			record.setTime(Parser.parseTimestamp(e));
			
			record.setSessionLocator(Parser.parseSessionLocator(e));
			
			record.setAccessor(Parser.parseAccessor(e));
			
			if(e.getField(Constants.ERROR_TEXT) == null){
				Data data=new Data();
				data.setOriginalSqlCommand(e.getField(Constants.SQL_TEXT_INFO).toString());
				record.setData(data);
			}else {
				ExceptionRecord exceptionRecord = new ExceptionRecord();
				exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);
				exceptionRecord.setDescription(e.getField(Constants.ERROR_TEXT).toString());
				exceptionRecord.setSqlString(e.getField(Constants.SQL_TEXT_INFO).toString());
				record.setException(exceptionRecord);
			}
			//Fix for GRD-80022 Adding rows returned to the GuardRecord
			if (e.includes(Constants.ROWS_RETURNED)  && (null != e.getField(Constants.ROWS_RETURNED))) {
				String recordsAffected = e.getField(Constants.ROWS_RETURNED).toString();
				double doubleValueRecords = Double.parseDouble(recordsAffected);
				int rowsReturned = (int) doubleValueRecords;
				record.setRecordsAffected(rowsReturned);
			}
			return record;
	}
	
	public static Time parseTimestamp(final Event e) {

			String dateString = e.getField(Constants.TIME_FIELD).toString();
			ZonedDateTime date = ZonedDateTime.parse(dateString,DateTimeFormatter.ISO_DATE_TIME);
			long millis = date.toInstant().toEpochMilli();
			return new Time(millis,0,0);
	}

	public static SessionLocator parseSessionLocator(final Event e) {

				SessionLocator sessionLocator = new SessionLocator();
			
				sessionLocator.setClientIp(e.getField(Constants.CLIENT_IP).toString());
				sessionLocator.setClientPort(Constants.DEFAULT_PORT);
				sessionLocator.setServerIp(e.getField(Constants.SERVER_IP).toString());
				sessionLocator.setServerPort(Constants.DEFAULT_PORT);
				sessionLocator.setIpv6(false);
				sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
				sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);

			return sessionLocator;
	}
	
	public static Accessor parseAccessor(final Event e) {

			Accessor accessor = new Accessor();			
			accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
			accessor.setLanguage(Constants.TERADATA_LANGUAGE);
			accessor.setClientHostName(Constants.UNKNOWN_STRING);
			accessor.setClientOs(Constants.UNKNOWN_STRING);
			accessor.setDbUser(e.getField(Constants.USER_NAME).toString());
			accessor.setServerType(Constants.SERVER_TYPE_STRING);
			accessor.setCommProtocol(Constants.UNKNOWN_STRING);
			accessor.setDbProtocol(Constants.DATA_PROTOCOL_STRING);
			accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
			accessor.setSourceProgram(e.getField(Constants.LOGON_SOURCE).toString());
			accessor.setClient_mac(Constants.UNKNOWN_STRING);
			accessor.setServerDescription(Constants.UNKNOWN_STRING);
			accessor.setServiceName(Constants.NOT_AVAILABLE);
			accessor.setServerOs(Constants.UNKNOWN_STRING);
			accessor.setServerHostName(e.getField(Constants.SERVER_HOSTNAME).toString());
			accessor.setOsUser(e.getField(Constants.OS_USER).toString());
		return accessor;
	}
	
}