//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.dynamodb;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.*;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Parser {

	private static Logger log = LogManager.getLogger(Parser.class);

	public static Record parseRecord(final JsonObject data) throws ParseException {
		Record record = new Record();

		String query =  Constants.UNKNOWN_STRING;
		String sessionID =  Constants.UNKNOWN_STRING;

		if(data != null) {
			if(data.has(Constants.REQUEST_ID)) 
				sessionID = data.get(Constants.REQUEST_ID).getAsString();

			if(sessionID != null || !sessionID.isEmpty())
				record.setSessionId(sessionID);
			else
				record.setSessionId(Constants.NOT_AVAILABLE);

			record.setDbName(Parser.parseDbName(data));

			String dateString = Parser.parseTimestamp(data);
			Time time = Parser.getTime(dateString);
			if(time != null)
				record.setTime(time);

			record.setSessionLocator(Parser.parseSessionLocator(data));
	        record.setAccessor(Parser.parseAccessor(data));

	        if(data.has(Constants.ERROR_CODE) && data.has(Constants.ERROR_MESSAGE)) {
	        	record.setException(Parser.parseException(data));
	        } else {
	        	record.setData(Parser.parseData(data));
	        }
		}
		return record;
	}

	private static String parseDbName(JsonObject data) {

		String dbName = Constants.UNKNOWN_STRING;
		if(data.has(Constants.RESOURCES) && data.get(Constants.RESOURCES) != null) {
			JsonArray resources = (JsonArray) data.get(Constants.RESOURCES);
			JsonObject obj = (JsonObject) resources.get(0);
			String arn = obj.get("ARN").getAsString();
			String arns[] = arn.split(":");
			String acc_no = arns[4];
			String tableNames = arns[5];
			String tableString[] = tableNames.split("/");
			String tableName = tableString[1];
			dbName = acc_no + ":" + tableName;
		}
		return dbName;
	}

	//----------- TIME
	
	public static String parseTimestamp(final JsonObject data) {
		String dateString = null;
		dateString = data.get(Constants.EVENT_TIME).getAsString();
		return dateString;
	}

	public static Time getTime(String dateString) {
		ZonedDateTime date = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
		long millis = date.toInstant().toEpochMilli();
		int minOffset = date.getOffset().getTotalSeconds() / 60;
		return new Time(millis, minOffset, 0);
	}

	//----------- Session Locator 
	
	private static SessionLocator parseSessionLocator(JsonObject data) {
		SessionLocator sessionLocator = new SessionLocator();
		sessionLocator.setIpv6(false);

		String clientIp = Constants.NOT_AVAILABLE;
		String sourceIP = data.get(Constants.SOURCE_IP_ADDRESS).getAsString();
		if(sourceIP != null)
			clientIp = sourceIP;

		try{
			String arr[] = clientIp.split("\\.");
			for (String string : arr) {
				int test = Integer.parseInt(string);
			}
		}
		catch(Exception e) {
			clientIp = "0.0.0.0";
		}

		sessionLocator.setClientIp(clientIp);
		sessionLocator.setClientPort(Constants.CLIENT_PORT);
		sessionLocator.setServerIp(Constants.SERVER_IP);
		sessionLocator.setServerPort(Constants.SERVER_PORT);
		sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
		sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);

		return sessionLocator;
	}
	
	//---------- Accessor 

	public static Accessor parseAccessor(JsonObject data) {
		Accessor accessor = new Accessor();

		accessor.setDbProtocol(Constants.DATA_PROTOCOL_STRING);
		accessor.setServerType(Constants.SERVER_TYPE_STRING);

		String dbUsers = Constants.NOT_AVAILABLE;
		String clientHostName = Constants.UNKNOWN_STRING;

		if(data.has(Constants.USER_IDENTITY) && data.get(Constants.USER_IDENTITY) != null && !(data.get(Constants.USER_IDENTITY).isJsonNull())) {
			JsonObject userIdentity = data.getAsJsonObject(Constants.USER_IDENTITY);

			if(userIdentity.has(Constants.ARN) && userIdentity.get(Constants.ARN).getAsString() != null) {
				String arnNumber = userIdentity.get(Constants.ARN).getAsString();

				String arnNumbers[] = arnNumber.split("::");
				dbUsers = arnNumbers[1];
			}

			if(userIdentity.has(Constants.ACCOUNT_ID) && userIdentity.get(Constants.ACCOUNT_ID).getAsString() != null) 
				clientHostName = userIdentity.get(Constants.ACCOUNT_ID).getAsString();
		}

		accessor.setDbUser(dbUsers);
		accessor.setClientHostName(clientHostName);

		String eventSource = Constants.UNKNOWN_STRING;
		
		if(data.has(Constants.ACCOUNT_ID)) {
			String account_id = data.get(Constants.ACCOUNT_ID).getAsString();
			eventSource = account_id + "_AWS_Dynamodb";
		}
		else 
			eventSource = "dynamodb.amazon.com";

		accessor.setServerHostName(eventSource);

		String sourceProgram = Constants.UNKNOWN_STRING;

		if(data.has(Constants.USER_AGENT) && data.get(Constants.USER_AGENT) != null)
			sourceProgram = data.get(Constants.USER_AGENT).getAsString();
		accessor.setSourceProgram(sourceProgram);

		accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
		accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);

		accessor.setClient_mac(Constants.UNKNOWN_STRING);
		accessor.setClientOs(Constants.UNKNOWN_STRING);
		accessor.setCommProtocol(Constants.UNKNOWN_STRING);
		accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
		accessor.setOsUser(Constants.UNKNOWN_STRING);
		accessor.setServerDescription(Constants.UNKNOWN_STRING);
		accessor.setServerOs(Constants.UNKNOWN_STRING);
		accessor.setServiceName(Constants.UNKNOWN_STRING);

		return accessor;
	}
	
	//------ Parse Data
	
	public static Data parseData(JsonObject inputJSON) {
        Data data = new Data();
        try {
            Construct construct = parseAsConstruct(inputJSON);
            if (construct != null) {
                data.setConstruct(construct);

                if (construct.getFullSql() == null) {
                    construct.setFullSql(Constants.UNKNOWN_STRING);
                }
                if (construct.getRedactedSensitiveDataSql() == null) {
                    construct.setRedactedSensitiveDataSql(Constants.UNKNOWN_STRING);
                }
            }
        } catch (Exception e) {
            log.error("DynamoDB filter: Error parsing Json " + inputJSON, e);
            throw e;
        }
        return data;
    }

    public static Construct parseAsConstruct(final JsonObject data) {
        try {
            final Sentence sentence = Parser.parseSentence(data);
            
            final Construct construct = new Construct();
            construct.sentences.add(sentence);
            
            construct.setFullSql(data.toString());
            
            construct.setRedactedSensitiveDataSql(Parser.parseRedactedSensitiveDataSql(data));
            return construct;
        } catch (final Exception e) {
            throw e;
        }
    }
    
    protected static Sentence parseSentence(final JsonObject data) {
        
        Sentence sentence = null;

        String verb = Constants.UNKNOWN_STRING;
        String name = Constants.UNKNOWN_STRING;
        
        if(data.has(Constants.EVENT_NAME)) {
        	String eventName = data.get(Constants.EVENT_NAME).getAsString();
        	if(eventName != null || !eventName.isEmpty())
        		verb = eventName;
        }

        sentence = new Sentence(verb);

        if(data.has(Constants.REQUEST_PARAMETERS) && data.get(Constants.REQUEST_PARAMETERS) != null && !(data.get(Constants.REQUEST_PARAMETERS).isJsonNull())) {
        	String tableName = Constants.UNKNOWN_STRING;

			JsonObject requestParam = data.getAsJsonObject(Constants.REQUEST_PARAMETERS);
			if(requestParam.has(Constants.TABLE_NAME))
				tableName = requestParam.get(Constants.TABLE_NAME).getAsString();
			if(tableName != null || !tableName.isEmpty())
				name = tableName;
	    }

        SentenceObject sentenceObject = null;
        sentenceObject = new SentenceObject(name);
        sentenceObject.setType(Constants.OBJECT_TYPE);

        sentence.getObjects().add(sentenceObject);

        return sentence;
    }

    protected static String parseRedactedSensitiveDataSql(JsonObject data) {

    	if(data.has(Constants.USER_IDENTITY) && data.get(Constants.USER_IDENTITY) != null && !(data.get(Constants.REQUEST_PARAMETERS).isJsonNull())) {
			JsonObject userIdentity = data.getAsJsonObject(Constants.USER_IDENTITY);
			if(userIdentity.has(Constants.PRINCIPAL_ID) && userIdentity.get(Constants.PRINCIPAL_ID).getAsString() != null) {
				userIdentity.remove(Constants.PRINCIPAL_ID);
				userIdentity.addProperty(Constants.PRINCIPAL_ID, Constants.MASK_STRING);
			}
			if(userIdentity.has(Constants.ACCOUNT_ID) && userIdentity.get(Constants.ACCOUNT_ID).getAsString() != null) {
				userIdentity.remove(Constants.ACCOUNT_ID);
				userIdentity.addProperty(Constants.ACCOUNT_ID, Constants.MASK_STRING);
			}
			if(userIdentity.has(Constants.ACCESS_KEY_ID) && userIdentity.get(Constants.ACCESS_KEY_ID).getAsString() != null) {
				userIdentity.remove(Constants.ACCESS_KEY_ID);
				userIdentity.addProperty(Constants.ACCESS_KEY_ID, Constants.MASK_STRING);
			}
			data.remove(Constants.USER_IDENTITY);
			data.add(Constants.USER_IDENTITY, userIdentity);
    	}

    	if(data.has(Constants.REQUEST_ID)) {
    		data.remove(Constants.REQUEST_ID);
    		data.addProperty(Constants.REQUEST_ID, Constants.MASK_STRING);
    	}

    	if(data.has(Constants.EVENT_ID)) {
    		data.remove(Constants.EVENT_ID);
    		data.addProperty(Constants.EVENT_ID, Constants.MASK_STRING);
    	}

    	return data.toString();

    }
    
    //------ Error 

    private static ExceptionRecord parseException(JsonObject data) {

    	ExceptionRecord exceptionRecord = new ExceptionRecord();
    	exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);
    	exceptionRecord.setDescription(data.get(Constants.ERROR_MESSAGE).getAsString());

    	String query = Constants.UNKNOWN_STRING;
        if(data.has(Constants.REQUEST_PARAMETERS) && data.has(Constants.EVENT_NAME)) {
        	String tableName = Constants.UNKNOWN_STRING;
        	String eventName = Constants.UNKNOWN_STRING;
        	if(data.get(Constants.REQUEST_PARAMETERS) != null && !(data.get(Constants.REQUEST_PARAMETERS).isJsonNull())) {
				JsonObject remote = data.getAsJsonObject(Constants.REQUEST_PARAMETERS);
				if(remote.has(Constants.TABLE_NAME))
					tableName = remote.get(Constants.TABLE_NAME).getAsString();
        	}
        	if(data.get(Constants.EVENT_NAME) != null)
        		eventName = data.get(Constants.EVENT_NAME).getAsString();
			query = eventName + tableName;
        }
    	exceptionRecord.setSqlString(query); 
    	return exceptionRecord;
    }

}