package com.ibm.guardium.couchbasedb;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.validator.routines.InetAddressValidator;

import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;

public class Parser {

	private static Logger log = LogManager.getLogger(Parser.class);	
	
	public static Boolean checkForUIGeneratedQueries(final JsonObject data) {
		Boolean isUIGeneratedQuery=false;
		if(data.has(Constants.CLIENT_CONTEXT_ID) && !data.get(Constants.CLIENT_CONTEXT_ID).isJsonNull() && 
				data.get(Constants.CLIENT_CONTEXT_ID).getAsString().startsWith(Constants.INTERNAL)) {
			isUIGeneratedQuery=true;			
		}
		return isUIGeneratedQuery;
	}

	public static Record parseRecord(final JsonObject data) throws ParseException {
		
		Record record = new Record();
		
			record.setSessionId(parseSessionID(data));
			
			record.setDbName(Constants.NOT_AVAILABLE);
			
			record.setAppUserName(Constants.UNKNOWN_STRING);
			
			record.setTime(Parser.parseTimestamp(data));
			
			record.setSessionLocator(Parser.parseSessionLocator(data));
			
			record.setAccessor(Parser.parseAccessor(data));
			
			setExceptionOrDataPart(record,data);
			
		return record;
	}
	
	public static void setExceptionOrDataPart(final Record record,final JsonObject data){
	
		if(record.getAccessor().getDataType().equals(Constants.TEXT)) {
			
			if (data.has(Constants.STATUS) && !data.get(Constants.STATUS).isJsonNull()){
				String status=data.get(Constants.STATUS).getAsString();
				
				if(status.equals(Constants.SUCCESS)){
					record.setData(Parser.parseDataForSniffer(data));
				}else if(status.equals(Constants.FATAL) ||status.equals(Constants.ERRORS)){
					record.setException(Parser.parseExceptionForN1QL(data));
				}	
			}
				}else {
					if ((data.has(Constants.ERROR_MESSAGE) && !data.get(Constants.ERROR_MESSAGE).isJsonNull())) {
						record.setException(Parser.parseExceptionForAPI(data));
					} 
					else {
						if (data.get(Constants.ID).getAsInt() == 8264 || data.get(Constants.ID).getAsInt() == 8193) {
							record.setException(Parser.parseExceptionForAPI(data));
						}
						else {
							record.setData(Parser.parseData(data));
						}
						}
					}
	}
	
	public static String parseSessionID(final JsonObject data){
		
		String sessionID=Constants.UNKNOWN_STRING;
		
		if (data.has(Constants.REQUEST_ID) && !data.get(Constants.REQUEST_ID).isJsonNull()){
			sessionID=data.get(Constants.REQUEST_ID).getAsString();
		}
		else if(data.has(Constants.SESSION_ID) && !data.get(Constants.SESSION_ID).isJsonNull()) {
			sessionID=data.get(Constants.SESSION_ID).getAsString();
		}
		return sessionID;	
	}
	
	//Parse Timestamp
	public static Time parseTimestamp(final JsonObject data) {
		
		String dateString = null;
		if(data.has(Constants.TIMESTAMP) && !data.get(Constants.TIMESTAMP).isJsonNull()) {
			dateString=data.get(Constants.TIMESTAMP).getAsString();
		}
		if(dateString!=null) {
			ZonedDateTime date = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
			long millis = date.toInstant().toEpochMilli();
			int minOffset = date.getOffset().getTotalSeconds() / 60;
			return new Time(millis, minOffset, 0);
		}
		
		return null;
	}


	//Form Session Locator
	public static SessionLocator parseSessionLocator(JsonObject data) {

			SessionLocator sessionLocator = new SessionLocator();
			
			setClientIPAndPort(sessionLocator,data);
			setServerIPAndPort(sessionLocator,data);
			 
			sessionLocator.setIpv6(false);
			sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
			sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);
	
			return sessionLocator;
	}
	
	public static void setServerIPAndPort(final SessionLocator sessionLocator, final JsonObject data){
		
		String serverIp = Constants.DEFAULT_IP;
		int serverPort = Constants.DEFAULT_SERVER_PORT;
		
		if(data.has(Constants.NODE) && !data.get(Constants.NODE).isJsonNull()) {
			String nodeData[] = data.get(Constants.NODE).getAsString().split(":");
			serverIp = nodeData[0];
			
			InetAddressValidator validator = InetAddressValidator.getInstance();
			
			if (!validator.isValid(serverIp)|| serverIp.equals(Constants.LOOPBACK_ADDRESS)) {
				serverIp = data.get(Constants.SERVER_IP).getAsString();
			}
			serverPort = Integer.parseInt(nodeData[1]);
			
		}else {
				serverIp = data.get(Constants.SERVER_IP).getAsString();
		}
		sessionLocator.setServerIp(serverIp);
		sessionLocator.setServerPort(serverPort);
		
	}

	public static void setClientIPAndPort(final SessionLocator sessionLocator, final JsonObject data){
		String clientIp = Constants.DEFAULT_IP;
		int clientPort = Constants.DEFAULT_CLIENT_PORT;
		JsonObject remoteString = null;
		
		if(data.has(Constants.REMOTE) && !data.get(Constants.REMOTE).isJsonNull()) {
			remoteString = data.getAsJsonObject(Constants.REMOTE);
			
			if (remoteString.has(Constants.IP) && !remoteString.get(Constants.IP).isJsonNull()) {
				clientIp = remoteString.get(Constants.IP).getAsString();
			}
			if (remoteString.has(Constants.PORT) && !remoteString.get(Constants.PORT).isJsonNull()) {
				clientPort = remoteString.get(Constants.PORT).getAsInt();
			}
		}
		sessionLocator.setClientIp(clientIp);
		sessionLocator.setClientPort(clientPort);
	}
	
	// ---------- Accessor
	public static Accessor parseAccessor(final JsonObject data) {
		Accessor accessor = new Accessor();

		setLanguageandDataType(accessor,data);
		setUsername(accessor,data);
		
		accessor.setServerHostName(data.get(Constants.SERVER_HOSTNAME).getAsString());
		accessor.setClientHostName(Constants.UNKNOWN_STRING);
		accessor.setServerType(Constants.SERVER_TYPE_STRING);
		accessor.setServerOs(Constants.UNKNOWN_STRING);
		accessor.setClientOs(Constants.UNKNOWN_STRING);
		accessor.setCommProtocol(Constants.UNKNOWN_STRING);
		accessor.setDbProtocol(Constants.DATA_PROTOCOL_STRING);
		accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
		accessor.setOsUser(Constants.UNKNOWN_STRING);
		
		setSourceProgram(accessor,data);
		
		accessor.setClient_mac(Constants.UNKNOWN_STRING);
		accessor.setServerDescription(Constants.UNKNOWN_STRING);
		accessor.setServiceName(Constants.UNKNOWN_STRING);
		
		return accessor;
	}
	
	public static void setLanguageandDataType(final Accessor accessor,final JsonObject data) {
		
		if(data.has(Constants.DESCRIPTION) && !data.get(Constants.DESCRIPTION).isJsonNull() 
				&& data.get(Constants.DESCRIPTION).getAsString().contains(Constants.N1QL)) {
			accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
			accessor.setLanguage(Constants.COUCHB_LANGUAGE);
		}else {
			accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);
			accessor.setLanguage(Constants.FREE_TEXT);
		}
	}
	public static void setUsername(final Accessor accessor,final JsonObject data) {
		
		String user = Constants.NOT_AVAILABLE;
		if (data.has(Constants.REAL_USERID) && !data.get(Constants.REAL_USERID).isJsonNull()) {
			JsonObject realUserID = data.getAsJsonObject(Constants.REAL_USERID);

			if (realUserID.has(Constants.USER) && !realUserID.get(Constants.USER).isJsonNull()) {
				user = realUserID.get(Constants.USER).getAsString();
			}
		}
		accessor.setDbUser(user);
	}
	public static void setSourceProgram(final Accessor accessor,final JsonObject data) {
		
		String sourceProgram = Constants.UNKNOWN_STRING;
		if (data.has(Constants.USER_AGENT) && !data.get(Constants.USER_AGENT).isJsonNull())
			sourceProgram = data.get(Constants.USER_AGENT).getAsString();
		accessor.setSourceProgram(sourceProgram);
	}
	// ------ Parse Data

	public static Data parseDataForSniffer(final JsonObject inputJSON) {
		
		Data data = null;
		if (inputJSON.has(Constants.STATEMENT) && !inputJSON.get(Constants.STATEMENT).isJsonNull()) {
			data=new Data();
			data.setOriginalSqlCommand(Constants.PREPENDED_STRING+
					inputJSON.get(Constants.STATEMENT).getAsString());
		}
		
		return data;
	}
	
	
	public static Data parseData(final JsonObject inputJSON) {
		Data data = new Data();
		try {
			Construct construct = parseAsConstruct(inputJSON);
			if (construct != null) {
				data.setConstruct(construct);		
			}
		} catch (Exception e) {
			log.error("CouchbaseDB filter: Error parsing Json " + inputJSON, e);
			throw e;
		}
		return data;
	}

	public static Construct parseAsConstruct(final JsonObject data) {
		try {
			final Sentence sentence = Parser.parseSentence(data);

			final Construct construct = new Construct();
			construct.sentences.add(sentence);
			
			if (data.has(Constants.NAME) && !data.get(Constants.NAME).isJsonNull()) {
				construct.setFullSql(data.get(Constants.NAME).getAsString());	
			}
			construct.setRedactedSensitiveDataSql(data.toString());
			
			return construct;
		} catch (final Exception e) {
			throw e;
		}
	}

	protected static Sentence parseSentence(final JsonObject data) {

		Sentence sentence = null;
		String verb = Constants.UNKNOWN_STRING;
		String object_name = Constants.UNKNOWN_STRING;
		
		if (data.has(Constants.HTTP_METHOD) && !data.get(Constants.HTTP_METHOD).isJsonNull()) {
				verb = data.get(Constants.HTTP_METHOD).getAsString();
				sentence = new Sentence(verb);
				if (data.has(Constants.NAME) && !data.get(Constants.NAME).isJsonNull()) {
					object_name = data.get(Constants.NAME).getAsString();
			}
		}//else block is added for testing only so as to see, not properly handled events on QS page
		else {
			if(data.has(Constants.DESCRIPTION) && !data.get(Constants.DESCRIPTION).isJsonNull()) {
				verb=data.get(Constants.DESCRIPTION).getAsString();
				sentence = new Sentence(verb);
			}
			if(data.has(Constants.NAME) && !data.get(Constants.NAME).isJsonNull()) {
				object_name=data.get(Constants.NAME).getAsString();
			}
		}

		SentenceObject sentenceObject = new SentenceObject(object_name);
		sentenceObject.setType(Constants.OBJECT_TYPE);
		sentence.getObjects().add(sentenceObject);

		return sentence;
	}

	
	// ------ Error

	private static ExceptionRecord parseExceptionForN1QL(final JsonObject data) {

		ExceptionRecord exceptionRecord = new ExceptionRecord();
		
		if (data.has(Constants.ID) && !data.get(Constants.ID).isJsonNull()) {
			if (data.get(Constants.ID).getAsInt() == 8193 || data.get(Constants.ID).getAsInt() == 8264) {
				exceptionRecord.setExceptionTypeId(Constants.LOGIN_FAILED);
			} else {
				exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);
			}	
		}
		else {
			exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);
		}
		
		if (data.has(Constants.DESCRIPTION) && !data.get(Constants.DESCRIPTION).isJsonNull()) {
			exceptionRecord.setDescription(data.get(Constants.DESCRIPTION).getAsString());
		}

		String query = Constants.UNKNOWN_STRING;
		if (data.has(Constants.STATEMENT) && !data.get(Constants.STATEMENT).isJsonNull()) {
			query = Constants.PREPENDED_STRING+data.get(Constants.STATEMENT).getAsString();
		}

		exceptionRecord.setSqlString(query);
		return exceptionRecord;
	}
	
	private static ExceptionRecord parseExceptionForAPI(final JsonObject data) {

		ExceptionRecord exceptionRecord = new ExceptionRecord();
		if (data.has(Constants.ID) && !data.get(Constants.ID).isJsonNull()) {
			if (data.get(Constants.ID).getAsInt() == 8193 || data.get(Constants.ID).getAsInt() == 8264) {
				exceptionRecord.setExceptionTypeId(Constants.LOGIN_FAILED);
			} else {
				exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);
			}	
		}else {
			exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);
		}
		
		if (data.has(Constants.DESCRIPTION) && !data.get(Constants.DESCRIPTION).isJsonNull()) {
			exceptionRecord.setDescription(data.get(Constants.DESCRIPTION).getAsString());
		}

		String query = Constants.UNKNOWN_STRING;
		if (data.has(Constants.NAME) && !data.get(Constants.NAME).isJsonNull()) {
			query = data.get(Constants.NAME).getAsString();
		}

		exceptionRecord.setSqlString(query);
		return exceptionRecord;
	}

}