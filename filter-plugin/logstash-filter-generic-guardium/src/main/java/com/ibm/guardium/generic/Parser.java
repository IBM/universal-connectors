//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.generic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
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
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;

public class Parser {

	private static Logger log = LogManager.getLogger(Parser.class);

	/*
	 * private static final DateTimeFormatterBuilder dateTimeFormatterBuilder = new
	 * DateTimeFormatterBuilder()
	 * .append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
	 * 
	 * private static final DateTimeFormatter DATE_TIME_FORMATTER =
	 * dateTimeFormatterBuilder.toFormatter();
	 */

	public static Record parseRecord(final JsonObject data) throws ParseException {
		
		Record record = new Record();
		if (data != null) {
			
			String sessionID=setSessionID(data);
			record.setSessionId(sessionID);
			
			String dbName = setDbName(data);
			record.setDbName(dbName);
			
			String appUsername = setAppUsername(data);
			record.setAppUserName(appUsername);

			Time time = Parser.parseTimestamp(data);
			record.setTime(time);

			record.setSessionLocator(Parser.parseSessionLocator(data));
			record.setAccessor(Parser.parseAccessor(data));
			
			setExceptionOrDataPart(record,data);
		}
			
		return record;
	}
	
	public static String setAppUsername(final JsonObject data){
		
		String appUsername = Constants.NOT_AVAILABLE;
		
		if (data.has(Constants.APPUSERNAME) && data.get(Constants.APPUSERNAME)!=null){
			appUsername= data.get(Constants.APPUSERNAME).getAsString();
		}
		return appUsername;	
	}
	
	public static Time parseTimestamp(final JsonObject data) throws ParseException {
		
		String dateString = null;
		if(data.has(Constants.TIMESTAMP) && data.get(Constants.TIMESTAMP)!=null) {
			dateString=data.get(Constants.TIMESTAMP).getAsString();
		}
		if(dateString!=null) {
			//DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ");
			//ZonedDateTime date = ZonedDateTime.parse(dateString, formatter);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
			Date date = df.parse(dateString);
			long millis = date.getTime();   //date.toInstant().toEpochMilli();
			int minOffset = 0;
			return new Time(millis, minOffset, 0);	
		}
		
		return null;
	}
	
	public static String setDbName(final JsonObject data){
		
		String dbName=Constants.UNKNOWN_STRING;
		
		if (data.has(Constants.DBNAME) && data.get(Constants.DBNAME)!=null){
			dbName = data.get(Constants.DBNAME).getAsString();
		}
		return dbName;	
	}
	
    public static String setSessionID(final JsonObject data){
		
		String sessionID=Constants.UNKNOWN_STRING;
		
		if (data.has(Constants.SESSIONID) && data.get(Constants.SESSIONID)!=null){
			sessionID = data.get(Constants.SESSIONID).getAsString();
		}
		return sessionID;	
	}

	//Form Session Locator
	public static SessionLocator parseSessionLocator(JsonObject data) {

			SessionLocator sessionLocator = new SessionLocator();
			
			setClientIPAndPort(sessionLocator, data);
			setServerIPAndPort(sessionLocator, data);
			/*
			 * if(sourceIP != null) clientIp = sourceIP; int clientPort = -1; String
			 * serverIp = "0.0.0.0"; int serverPort = -1;
			 */
			setIpv6(sessionLocator, data);
			setClientIpv6(sessionLocator, data);
			setServerIpv6(sessionLocator, data);
	
			return sessionLocator;
	}
	
	public static void setServerIpv6(final SessionLocator sessionLocator, final JsonObject data){
		
		String serverIpv6 = Constants.UNKNOWN_STRING;
		
		if(data.has(Constants.SERVERIPV6) && data.get(Constants.SERVERIPV6)!=null) {
			serverIpv6 = data.get(Constants.SERVERIPV6).getAsString();
		}
		sessionLocator.setServerIpv6(serverIpv6);
		
	}
	
	public static void setClientIpv6(final SessionLocator sessionLocator, final JsonObject data){
		
		String clientIpv6 = Constants.UNKNOWN_STRING;
		
		if(data.has(Constants.CLIENTIPV6) && data.get(Constants.CLIENTIPV6)!=null) {
			clientIpv6 = data.get(Constants.CLIENTIPV6).getAsString();
		}
		sessionLocator.setClientIpv6(clientIpv6);
		
	}
	
	public static void setIpv6(final SessionLocator sessionLocator, final JsonObject data) {
		boolean ipv6 = false;
		
		if(data.has(Constants.ISIPV6) && data.get(Constants.ISIPV6)!=null) {
			ipv6 = data.get(Constants.SERVERIP).getAsBoolean();
		}
		sessionLocator.setIpv6(ipv6);
	}
	
	public static void setServerIPAndPort(final SessionLocator sessionLocator, final JsonObject data){
		
		String serverIp = Constants.UNKNOWNSERVERIP;
		int serverPort = -1;
		
		if(data.has(Constants.SERVERIP) && data.get(Constants.SERVERIP)!=null) {
			serverIp = data.get(Constants.SERVERIP).getAsString();
		}
		if (data.has(Constants.SERVERPORT) && data.get(Constants.SERVERPORT)!=null) {
			serverPort = data.get(Constants.SERVERPORT).getAsInt();
	    }
		sessionLocator.setServerIp(serverIp);
		sessionLocator.setServerPort(serverPort);
		
	}

	
	public static void setClientIPAndPort(final SessionLocator sessionLocator, final JsonObject data){
		String clientIp = Constants.UNKNOWNCLIENTIP;
		int clientPort = 0;
		
		if(data.has(Constants.CLIENTIP) && data.get(Constants.CLIENTIP)!=null && 
				(!(data.get(Constants.CLIENTIP).getAsString()).equalsIgnoreCase("local machine") || 
				!(data.get(Constants.CLIENTIP).getAsString()).equalsIgnoreCase("Unknown"))) {
			clientIp = data.get(Constants.CLIENTIP).getAsString();
		}
		if (data.has(Constants.CLIENTPORT) && data.get(Constants.CLIENTPORT)!=null) {
				clientPort = data.get(Constants.CLIENTPORT).getAsInt();
		}
		sessionLocator.setClientIp(clientIp);
		sessionLocator.setClientPort(clientPort);
	}
	
	
	
	
	// ---------- Accessor
	public static Accessor parseAccessor(final JsonObject data) {
		Accessor accessor = new Accessor();

		setLanguageandDataType(accessor,data);
		setUsername(accessor,data);
		
		setServerDetails(accessor, data);
		setClientDetails(accessor, data);
		
		if(data.has(Constants.COMMPROTO) && data.get(Constants.COMMPROTO)!=null) {
			accessor.setCommProtocol(data.get(Constants.COMMPROTO).getAsString());
		}
		else accessor.setCommProtocol(Constants.UNKNOWN_STRING);
		
		if(data.has(Constants.DBPROTOCOL) && data.get(Constants.DBPROTOCOL)!=null) {
			accessor.setDbProtocol(data.get(Constants.DBPROTOCOL).getAsString());
		}
		else accessor.setDbProtocol(Constants.UNKNOWN_STRING);
		
		if(data.has(Constants.DBPROTOCOLVERSION) && data.get(Constants.DBPROTOCOLVERSION)!=null) {
			accessor.setDbProtocolVersion(data.get(Constants.DBPROTOCOLVERSION).getAsString());
		}
		else accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);

		if(data.has(Constants.SOURCEPROGRAM) && data.get(Constants.SOURCEPROGRAM)!=null) {
			accessor.setSourceProgram(data.get(Constants.SOURCEPROGRAM).getAsString());
		}
		else accessor.setSourceProgram(Constants.UNKNOWN_STRING);
		
		
		if(data.has(Constants.SERVICENAME) && data.get(Constants.SERVICENAME)!=null) {
			accessor.setServiceName(data.get(Constants.SERVICENAME).getAsString());
		}
		else accessor.setServiceName(Constants.UNKNOWN_STRING);
		
		if(data.has(Constants.OSUSER) && data.get(Constants.OSUSER)!=null) {
			accessor.setOsUser(data.get(Constants.OSUSER).getAsString());
		}
		else accessor.setOsUser(Constants.UNKNOWN_STRING);

		return accessor;
	}
	
	public static void setClientDetails(final Accessor accessor,final JsonObject data) {
		String clientOs = Constants.UNKNOWN_STRING;
		String clienthostname = Constants.UNKNOWN_STRING;
		String clientmac = Constants.UNKNOWN_STRING;
		
		if(data.has(Constants.CLIENTOS) && data.get(Constants.CLIENTOS)!=null) {
			clientOs = data.get(Constants.CLIENTOS).getAsString();
		}
		accessor.setClientOs(clientOs);
		
		if(data.has(Constants.CLIENTHOSTNAME) && data.get(Constants.CLIENTHOSTNAME)!=null) {
			clienthostname = data.get(Constants.CLIENTHOSTNAME).getAsString();
		}
		accessor.setClientHostName(clienthostname);
		
		if(data.has(Constants.CLIENTMAC) && data.get(Constants.CLIENTMAC)!=null) {
			clienthostname = data.get(Constants.CLIENTMAC).getAsString();
		}
		accessor.setClient_mac(clientmac);
	}
	
	public static void setServerDetails(final Accessor accessor,final JsonObject data) {
		String serverType  = Constants.UNKNOWN_STRING;
		String serverOs = Constants.UNKNOWN_STRING;
		String serverHostname = Constants.UNKNOWN_STRING;
		String serverDesc = Constants.UNKNOWN_STRING;
		
		if(data.has(Constants.SERVERTYPE) && data.get(Constants.SERVERTYPE)!=null) {
			serverType = data.get(Constants.SERVERTYPE).getAsString();
		}
		accessor.setServerType(serverType);
		
		if(data.has(Constants.SERVEROS) && data.get(Constants.SERVEROS)!=null) {
			serverOs = data.get(Constants.SERVEROS).getAsString();
		}
		accessor.setServerOs(serverOs);
		
		if(data.has(Constants.SERVERHOSTNAME) && data.get(Constants.SERVERHOSTNAME)!=null) {
			serverHostname = data.get(Constants.SERVERHOSTNAME).getAsString();
		}
		accessor.setServerHostName(serverHostname);
		
		if(data.has(Constants.SERVERDESC) && data.get(Constants.SERVERDESC)!=null) {
			serverDesc = data.get(Constants.SERVERDESC).getAsString();
		}
		accessor.setServerDescription(serverDesc);
	}
	
	public static void setLanguageandDataType(final Accessor accessor,final JsonObject data) {
			accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
			String language = Constants.UNKNOWN_STRING;
			
			if(data.has(Constants.LANGUAGE) && data.get(Constants.LANGUAGE)!=null) {
				language = data.get(Constants.LANGUAGE).getAsString();
			}
			accessor.setLanguage(language);
	}
	public static void setUsername(final Accessor accessor,final JsonObject data) {
		
		String user = Constants.NOT_AVAILABLE;
		
		if(data.has(Constants.DBUSER) && data.get(Constants.DBUSER)!=null) {
			user = data.get(Constants.DBUSER).getAsString();
		}
		accessor.setDbUser(user);
	}
	
	
	public static void setExceptionOrDataPart(final Record record,final JsonObject data){
		
		if(record.getAccessor().getDataType().equals(Constants.TEXT)) {
			Data retData = new Data();
			
			if (data.has(Constants.ORIGINALSQLCOMMAND) && data.get(Constants.ORIGINALSQLCOMMAND)!= null){
				String originalSqlCmd = data.get(Constants.ORIGINALSQLCOMMAND).getAsString();
				retData.setOriginalSqlCommand(originalSqlCmd);
				record.setData(retData);
				
			}
			else {
				if(data.has(Constants.SQLSTRING) || data.has (Constants.SQLSTATE) || data.has(Constants.DESCRIPTION)	){
					ExceptionRecord exceptionRecord = new ExceptionRecord();
					String query = Constants.UNKNOWN_STRING;
					String sqlState = Constants.UNKNOWN_STRING;
					String description = Constants.UNKNOWN_STRING;
					
					if (data.has(Constants.SQLSTATE) && data.get(Constants.SQLSTATE) != null)
						sqlState = data.get(Constants.SQLSTATE).getAsString();
					
					if (data.has(Constants.DESCRIPTION) && data.get(Constants.DESCRIPTION) != null)
						description = data.get(Constants.DESCRIPTION).getAsString();

					if (data.has(Constants.SQLSTRING) && data.get(Constants.SQLSTRING)!= null) {
						query = data.get(Constants.SQLSTRING).getAsString();
					}
					exceptionRecord.setExceptionTypeId(sqlState);
					exceptionRecord.setDescription(description);
					exceptionRecord.setSqlString(query);
					record.setException(exceptionRecord);
				}
			}
		}
	}
}