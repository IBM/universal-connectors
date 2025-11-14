// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.singlestore;

import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.structures.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static Logger log = LogManager.getLogger(Parser.class);

    private DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .optionalStart()
            .appendLiteral(' ')
            .optionalEnd()
            .optionalStart()
            .appendZoneText(TextStyle.SHORT)
            .optionalEnd();

    private DateTimeFormatter DATE_TIME_FORMATTER = dateTimeFormatterBuilder.toFormatter();

    // 	---------------------- Record----------------------------
    public Record parseRecord(final JsonObject data) {
        try {
            if (data != null) {
                log.info("Start Parsing Record");
                Record record = getInitialRecord(data);

                // Get message and parse to map with null check
                if (data.has("message") && !data.get("message").isJsonNull()) {
                    String message = data.get("message").getAsString();
                    Map<String, String> logMap = SingleStoreLogFormat.parseLog(message);

                    // Check if EVENT_TYPE exists in the map
                    String eventType = logMap.get(SingleStoreLogFormat.EVENT_TYPE);
                    if (eventType != null && !SingleStoreLogFormat.USER_LOGIN.equals(eventType)) {
                        record.setData(parseData(data));
                    }
                }

                return record;
            } else {
                return null;
            }

        } catch (Exception e) {
            log.error("SingleStore filter: Error parsing Record {}", e.getMessage());
            return null;
        }
    }

    // 	---------------------- ExceptionRecord-----------------------------
    public Record parseExceptionRecord(final JsonObject data) {
        try {
            log.info("Start Parsing ExceptionRecord");

            Record record = getInitialRecord(data);

            if (data.has("message") && !data.get("message").isJsonNull()) {
                String message = data.get("message").getAsString();
                Map<String, String> logMap = SingleStoreLogFormat.parseLog(message);

                ExceptionRecord exceptionRecord = new ExceptionRecord();
                exceptionRecord.setExceptionTypeId(Constants.EXCEPTION_TYPE_LOGIN_FAILED_STRING);

                // Get login status with null check
                String loginStatus = logMap.get(SingleStoreLogFormat.LOGIN_STATUS);
                exceptionRecord.setDescription("Login Failed (" + (loginStatus != null ? loginStatus : "Unknown") + ")");
                exceptionRecord.setSqlString(Constants.UNKNOWN_STRING);
                record.setException(exceptionRecord);
            }

            return record;
        } catch (Exception e) {
            log.error("SingleStore filter: Error parsing ExceptionRecord {}", e.getMessage());
            return null;
        }
    }

    // 	---------------------- Create Record with Time, session ....-------------------
    public Record getInitialRecord(final JsonObject data) {
        Record record = new Record();

        record.setAppUserName(Constants.NOT_AVAILABLE);

        // DB Name
        String dbName = Constants.NOT_AVAILABLE;

        if (data.has(Constants.DB_NAME) && !data.get(Constants.DB_NAME).isJsonNull()) {
            dbName = data.get(Constants.DB_NAME).getAsString();
        }
        record.setDbName(dbName);

        // Time
        Time time = getTime(data);
        if (time != null) {
            record.setTime(time);
        }

        // SessionLocator
        record.setSessionLocator(parseSessionLocator(data));

        // Accessor
        record.setAccessor(parseAccessor(data, record.getDbName()));

        // SessionId
        record.setSessionId("");

        return record;
    }

    // 	---------------------- Timestamp-------------------
    protected Time getTime(final JsonObject data) {

        String dateString = null;
        if (data.has(Constants.TIMESTAMP) && !data.get(Constants.TIMESTAMP).isJsonNull()) {
            dateString = data.get(Constants.TIMESTAMP).getAsString();
        }

        if (dateString != null && !dateString.isEmpty()) {
            try {
                ZonedDateTime date = ZonedDateTime.parse(dateString, DATE_TIME_FORMATTER);
                long millis = date.toInstant().toEpochMilli();
                int minOffset = date.getOffset().getTotalSeconds() / 60;
                return new Time(millis, minOffset, 0);
            } catch (Exception e) {
                log.error("Error parsing timestamp '{}': {}", dateString, e.getMessage());
                return null;
            }
        } else {
            return new Time(0, 0, 0);
        }
    }

    // 	---------------------- SessionLocator------------------------------
    protected SessionLocator parseSessionLocator(JsonObject data) {
        SessionLocator sessionLocator = new SessionLocator();

        // Get an `InetAddressValidator`
        InetAddressValidator validator = InetAddressValidator.getInstance();

        int clientPort = Constants.CLIENT_PORT_VALUE;
        int serverPort = Constants.SERVER_PORT_VALUE;
        String clientIpAdd = Constants.CLIENT_IP_VALUE;
        String serverIpAdd = Constants.SERVER_IP_VALUE;
        String clientIpv6Add = Constants.UNKNOWN_STRING;
        String serverIpv6Add = Constants.UNKNOWN_STRING;

        if (data.has(Constants.CLIENT_IP) && !data.get(Constants.CLIENT_IP).isJsonNull()) {
            String clientIp = data.get(Constants.CLIENT_IP).getAsString();

            if (clientIp != null && !clientIp.isEmpty()) {
                if (validator.isValidInet4Address(clientIp)) {
                    sessionLocator.setIpv6(false);
                    clientIpAdd = clientIp;
                } else if (validator.isValidInet6Address(clientIp)) {
                    sessionLocator.setIpv6(true);
                    clientIpv6Add = clientIp;
                }
            }
        }

        if (data.has(Constants.SERVER_IP) && !data.get(Constants.SERVER_IP).isJsonNull()) {
            String serverIp = data.get(Constants.SERVER_IP).getAsString();
            if (serverIp != null && !serverIp.isEmpty()) {
                if (validator.isValidInet4Address(serverIp)) {
                    serverIpAdd = serverIp;
                } else if (validator.isValidInet6Address(serverIp)) {
                    if (!sessionLocator.isIpv6()) {
                        sessionLocator.setIpv6(true);
                        serverIpv6Add = serverIp;
                    }
                }
            }
        }

        if (data.has(Constants.SERVER_PORT) && !data.get(Constants.SERVER_PORT).isJsonNull()) {
            try {
                serverPort = Integer.parseInt(data.get(Constants.SERVER_PORT).getAsString());
            } catch (NumberFormatException e) {
                log.error("Error parsing server port: {}", e.getMessage());
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

    // 	---------------------- Accessor--------------------------------------
    protected Accessor parseAccessor(JsonObject data, String dbName) {
        Accessor accessor = new Accessor();

        accessor.setDbProtocol(Constants.DB_PROTOCOL);
        accessor.setSourceProgram(Constants.UNKNOWN_STRING);
        accessor.setServerType(Constants.SERVER_TYPE_STRING);

        String dbUser = Constants.NOT_AVAILABLE;
        if (data.has(Constants.DB_USER) && !data.get(Constants.DB_USER).isJsonNull()) {
            dbUser = data.get(Constants.DB_USER).getAsString();
        }
        accessor.setDbUser(dbUser);

        // GRD-114546: Set to N.A. to fix multiple S-Taps
		accessor.setServerHostName(Constants.NOT_AVAILABLE);

        accessor.setLanguage(Constants.LANGUAGE_MEMSQL_STRING);
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);

        accessor.setClient_mac(Constants.UNKNOWN_STRING);
        accessor.setClientHostName(Constants.UNKNOWN_STRING);
        accessor.setClientOs(Constants.UNKNOWN_STRING);
        accessor.setCommProtocol(Constants.UNKNOWN_STRING);
        accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
        accessor.setOsUser(Constants.UNKNOWN_STRING);
        accessor.setServerDescription(Constants.UNKNOWN_STRING);
        accessor.setServerOs(Constants.UNKNOWN_STRING);
        accessor.setServiceName(dbName != null ? dbName : Constants.UNKNOWN_STRING);

        return accessor;
    }

    protected Data parseData(JsonObject inputJSON) {
        Data data = new Data();

        String originalQuery = inputJSON.get(Constants.QUERY_STATEMENT).getAsString();

        if (originalQuery != null && originalQuery.startsWith("\"") && originalQuery.endsWith("\"")) {
            originalQuery = originalQuery.substring(1);
            originalQuery = originalQuery.substring(0, originalQuery.length() - 1);
        }

        if (originalQuery == null || originalQuery.trim().isEmpty()) {
            originalQuery = Constants.UNKNOWN_STRING;
        }

        String cleanedQuery = cleanQuery(originalQuery);

        // Use the cleaned query instead of the original query
        data.setOriginalSqlCommand(cleanedQuery);

        return data;
    }

    // 	---------------------- Clean The Query --------
    protected String cleanQuery(String query) {
        if (query == null || query.isEmpty()) {
            return "";
        }

        try {
            String regexObject = "OBJECT\\(\\)\\*/\\s*SELECT";
            String regex = "/\\*!\\d+\\s+";
            String commentRegex = "^\\s*/\\*.*?\\*/\\s*"; // More general pattern to match any comment at the beginning
            String NESTED_SELECT_REGEX = "(?i)(?<=SELECT\\s)(.*?\\((?:[^()]*|\\((?:[^()]*|\\([^()]*\\))*\\))*\\))";

            // Compile the pattern
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

            // Create a matcher for the input query
            Matcher matcher = pattern.matcher(query);

            if (matcher.find()) {
                query = matcher.replaceAll("");
            }

            // Compile the pattern
            pattern = Pattern.compile(regexObject, Pattern.CASE_INSENSITIVE);

            // Create a matcher for the input query
            matcher = pattern.matcher(query);

            if (matcher.find()) {
                query = matcher.replaceAll("SELECT");
            }

            // Compile the pattern for SQL comments at the beginning of queries
            pattern = Pattern.compile(commentRegex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

            // Create a matcher for the input query
            matcher = pattern.matcher(query);

            // Only remove the comment if it exists, otherwise leave the query unchanged
            if (matcher.find()) {
                query = matcher.replaceAll("");
            }

            query = query.replace("`", "");

            if (query.length() >= 6 && query.toUpperCase().startsWith("SELECT")) {
                if (hasNestedSelectInFields(query)) {
                    int indexMainFrom = findMainFromIndexInQuery(query);
                    if (indexMainFrom > 0) {
                        query = query.substring(0, 6) + " * " + query.substring(indexMainFrom);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error cleaning query: {}", e.getMessage());
        }

        return query;
    }

    protected int findMainFromIndexInQuery(String query) {
        if (query == null || query.isEmpty()) {
            return -1;
        }

        String lowerQuery = query.toLowerCase();
        int openParenCount = 0;

        for (int i = 0; i < lowerQuery.length(); i++) {
            char c = lowerQuery.charAt(i);

            if (c == '(') {
                openParenCount++;
            } else if (c == ')') {
                openParenCount--;
            } else if (i + 4 <= lowerQuery.length() && lowerQuery.startsWith("from", i)) {
                // Check if 'from' is not within parentheses
                if (openParenCount == 0) {
                    return i;
                }
            }
        }

        return -1; // No valid FROM found
    }

    protected boolean hasNestedSelectInFields(String query) {
        if (query == null || query.isEmpty()) {
            return false;
        }

        try {
            // Regex pattern to find nested SELECT statements within parentheses in the field part
            Pattern nestedSelectPattern = Pattern.compile("\\(\\s*select\\s+.*?\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = nestedSelectPattern.matcher(query);
            return matcher.find();
        } catch (Exception e) {
            log.error("Error checking for nested SELECT: {}", e.getMessage());
            return false;
        }
    }
}