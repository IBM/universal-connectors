package com.ibm.guardium.azureSQL;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;

import co.elastic.logstash.api.Event;

public class Parser {
	
	public static Record parseRecord(final Event e) throws ParseException {
		
			Record record = new Record();
		
			record.setSessionId(e.getField(Constants.Session_ID).toString());

			String databaseName = Constants.UNKNOWN_STRING;
			if(e.getField(Constants.DATABASE_NAME) !=null){
				databaseName=e.getField(Constants.DATABASE_NAME).toString();
			}
			record.setDbName(databaseName);
			
			record.setAppUserName(Constants.APP_USER_NAME);
			
			record.setTime(Parser.parseTimestamp(e));
			
			record.setSessionLocator(Parser.parseSessionLocator(e));
			
			record.setAccessor(Parser.parseAccessor(e));
			
				if(e.getField(Constants.SUCCEEDED).toString().contains("true")){
					Data data=new Data();
					data.setOriginalSqlCommand(e.getField(Constants.STATEMENT).toString());
					record.setData(data);
				}else {
					ExceptionRecord exceptionRecord = new ExceptionRecord();
					exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);
					exceptionRecord.setDescription(e.getField(Constants.ADDITIONAL_INFORMATION).toString());
					exceptionRecord.setSqlString(e.getField(Constants.STATEMENT).toString());
					record.setException(exceptionRecord);
				}
		return record;
	}
	
	public static Time parseTimestamp(final Event e) {
		
		long date=0;
			String dateString = e.getField(Constants.TIMESTAMP).toString();
			date=Long.parseLong(dateString);
			long mini=date/1000000;
			
			return new Time(mini,0,0);
	}

	public static SessionLocator parseSessionLocator(final Event e) {

				SessionLocator sessionLocator = new SessionLocator();
			
				String clientIp = (e.getField(Constants.Client_IP) != null && !e.getField(Constants.Client_IP).toString().isEmpty()) ? e.getField(Constants.Client_IP).toString() : Constants.DEFAULT_IP;
				sessionLocator.setClientIp(clientIp);
				sessionLocator.setClientPort(Constants.DEFAULT_PORT);
				sessionLocator.setServerIp(Constants.DEFAULT_IP);
				sessionLocator.setServerPort(Constants.DEFAULT_PORT);
				sessionLocator.setIpv6(false);
				sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
				sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);

			return sessionLocator;
	}
	
	public static Accessor parseAccessor(final Event e) {

			Accessor accessor = new Accessor();			
			accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
			accessor.setLanguage(Constants.LANGUAGE);
			accessor.setClientHostName(e.getField(Constants.CLIENT_HOST_NAME).toString());
			accessor.setClientOs(Constants.UNKNOWN_STRING);
			accessor.setDbUser(e.getField(Constants.User_Name).toString());
			accessor.setServerType(Constants.SERVER_TYPE_STRING);
			accessor.setCommProtocol(Constants.UNKNOWN_STRING);
			accessor.setDbProtocol(Constants.DATA_PROTOCOL_STRING);
			accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
			accessor.setSourceProgram(e.getField(Constants.APPLICATION_NAME).toString());
			accessor.setClient_mac(Constants.UNKNOWN_STRING);
			accessor.setServerDescription(Constants.UNKNOWN_STRING);

			String databaseName = Constants.UNKNOWN_STRING;
			if(e.getField(Constants.DATABASE_NAME) !=null){
				databaseName=e.getField(Constants.DATABASE_NAME).toString();
			}

			accessor.setServiceName(databaseName);
			accessor.setServerOs(Constants.UNKNOWN_STRING);
			accessor.setServerHostName(e.getField(Constants.Server_Hostname).toString());
			accessor.setOsUser(Constants.UNKNOWN_STRING);		
		return accessor;
	}
	
}
