/*
Copyright IBM Corp. 2021, 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.databricks;

import com.google.gson.JsonParser;
import com.ibm.guardium.databricks.sql_parsing.SqlParser;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.JSONException;

/**
 * Parser Class will perform operation on parsing events and messages from the
 * databrick audit logs into a Guardium record instance Guardium records include
 * the accessor, the sessionLocator, data, and exceptions. If there are no
 * errors, the data contains details about the query "construct"
 *
 * @className @Parser
 */
public class Parser {
    private static Logger logger = LogManager.getLogger(Parser.class);

    public Parser() {
        /**
         * Parses the given SQL query and returns a list of maps representing its
         * structure.
         *
         * @param sqlQuery The SQL query to parse.
         * @return A list of maps representing the parsed SQL structure.
         */
    }

    public static Record parseRecord(final JsonObject records) {

        if (logger.isDebugEnabled()) {
            logger.debug("Event Now: ", records);
        }

        Record record = new Record();
        try {
            if (records.has(Constants.PROPERTIES)) {
                JsonObject properties = records.get(Constants.PROPERTIES).getAsJsonObject();
                JsonObject requestParams = getRequestParams(properties);

                record.setTime(parseTime(records));
                String subId = getSubscriptionId(records);// resourceId
                String accountId = getAccountId(records);// second part of resourceId
                String sessionId = getSessionId(requestParams);
                String serviceName = getServiceName(properties);
                String dbName = subId+":"+serviceName;
                record.setSessionId(sessionId);
                record.setDbName(dbName);
                record.setAppUserName(Constants.UNKNOWN_STRING);
                record.setAccessor(parseAccessor(subId, accountId, properties, records));
                record.getAccessor().setServiceName(dbName);
                record.setSessionLocator(parserSessionLocator(properties));

                String response = properties.get(Constants.RESPONSE).toString();


                int stateCodeInt =getStatusCode(response);

                if (stateCodeInt ==-1 ||stateCodeInt ==200) {
                    record.setData(parseData(properties, requestParams));
                } else {
                    record.setException(parseException(stateCodeInt, response, properties));
                }
            }
        } catch (Exception e) {
            logger.error("Exception occurred while parsing record: ", e);
        }
        return record;
    }

    /**
     * Method to get queryStatement from JsonObject
     *
     * @param properties
     * @return
     */
    private static JsonObject getRequestParams(JsonObject properties) {
        if (!properties.has(Constants.REQUEST_PARAMS)) {
            throw new IllegalArgumentException("Request params not found in properties");
        }
        String requestParams = properties.get(Constants.REQUEST_PARAMS).getAsString();
        JsonObject requestParamsJsonObj = new Gson().fromJson(requestParams, JsonObject.class);

        return requestParamsJsonObj;

    }

    private static int getStatusCode(String response){
        String stateCode = "-1";

        if (!response.equalsIgnoreCase("null")) {
            response = response.replaceAll("\\\\",
                    "").replaceAll("^\"|\"$", "");
            String[] responseArr = response.split(",");
            stateCode = responseArr[0].replaceAll("[^0-9]", "");
        }
        return Integer.parseInt(stateCode);
    }

    private static String getSessionId(JsonObject requestParams) {
        String sessionId = Constants.UNKNOWN_STRING;
        String uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        if (requestParams.get(Constants.SESSION_ID) != null) {
            sessionId = requestParams.get(Constants.SESSION_ID).toString();
            if (!sessionId.matches(uuidRegex)) {
                sessionId = Constants.UNKNOWN_STRING;
            }
        }

        return sessionId;
    }

    /**
     * Method to get the time from JsonObject, set the expected value into
     * respective Time Object and then return the value as response
     *
     * @param records
     * @return
     */
    private static Time parseTime(JsonObject records) {
        String dateString = "2000-01-01 00:00:00 AM";
        if (records.has(Constants.TIMESTAMP)) {
            dateString = records.get(Constants.TIMESTAMP).getAsString();
        }
        ZonedDateTime date = ZonedDateTime.parse(dateString);
        long millis = date.toInstant().toEpochMilli();
        int minOffset = date.getOffset().getTotalSeconds() / 60;
        return new Time(millis, minOffset, 0);
    }

    /**
     * Method to get SubscriptionId from the JsonObject
     *
     * @param records
     * @return
     */
    private static String getSubscriptionId(JsonObject records) {
        String subId = Constants.UNKNOWN_STRING;
        if (records.has(Constants.RESOURCEID)) {
            subId = records.get(Constants.RESOURCEID).getAsString();
            if (subId.contains("/")) {
                subId = subId.split("/")[2];
            }
        }
        return subId;
    }

    /**
     * Method to get InstanceName from the JsonObject
     *
     * @param records
     * @return
     */
    private static String getInstanceName(JsonObject records) {
        String instanceName = Constants.UNKNOWN_STRING;
        if (records.has(Constants.RESOURCEID)) {
            instanceName = records.get(Constants.RESOURCEID).getAsString();
            if (instanceName.contains("/")) {
                String[] stringArr =instanceName.split("/");
                instanceName = stringArr[stringArr.length-1];
            }
        }
        return instanceName;
    }

    /**
     * Method to get accountId from the JsonObject
     *
     * @param records
     * @return
     */
    private static String getAccountId(JsonObject records) {
        String accountId = Constants.UNKNOWN_STRING;
        if (records.has(Constants.RESOURCEID)) {
            accountId = records.get(Constants.RESOURCEID).getAsString();
            String[] resourceId = accountId.split("/");
            accountId = resourceId[2];
        }
        return accountId;
    }

    /**
     * Method to set the value into respective Exception Object and then return the
     * value as response
     *
     * @return
     */
    private static ExceptionRecord parseException(int statusCode, String response, JsonObject properties) {
        ExceptionRecord exception = new ExceptionRecord();
        exception.setExceptionTypeId(Constants.SQL_ERROR);
        if(getRequestParams(properties).has(Constants.COMMAND_TEXT)){
            String commandText = getRequestParams(properties).get(Constants.COMMAND_TEXT).toString();
            exception.setSqlString(getQueryStatement(commandText));
        }else {
            exception.setSqlString(getServiceName(properties)+"/"+getActionName(properties));
        }
        if(getServiceName(properties).equalsIgnoreCase(Constants.ACCOUNTS)){
            exception.setExceptionTypeId(Constants.LOGIN_FAILED);
        }
        response=response.replaceAll("\\\\","");
        response=response.substring(1,response.length()-1);
        String errorMessage ="";
        try {
            JsonObject jsonObject= JsonParser.parseString(response).getAsJsonObject();
            // Accessing values from the JSON object
            errorMessage = jsonObject.get("errorMessage").getAsString();
            exception.setDescription("Error:" + statusCode +" Action: " + getServiceName(properties)+"/"+getActionName(properties) + " Message: " + errorMessage);
        } catch (Exception e) {
            exception.setDescription("Error:" + statusCode+ " Action: " + getServiceName(properties)+"/"+getActionName(properties)+" Message: " + response );
            exception.setSqlString(Constants.NOT_AVAILABLE);
        }
        return exception;
    }

    /**
     * parseData() method will perform operation on JsonObject records, set the
     * expected value into respective Data Object and then return the value as
     * response
     *
     * @param requestParams
     * @return
     */
    static Data parseData(JsonObject properties, JsonObject requestParams) {
        Data data = new Data();
        String actionName = getActionName(properties);
        try {
            if (getServiceName(properties).equalsIgnoreCase(Constants.DATABRICKSSQL) ||getServiceName(properties).equalsIgnoreCase(Constants.NOTEBOOK)) {
                if (requestParams.has(Constants.COMMAND_TEXT)) {
                    String commandText = requestParams.get(Constants.COMMAND_TEXT).toString();
                    SqlParser sqlParser = new SqlParser();
                    data = sqlParser.parseStatement(getQueryStatement(commandText));
                    data.setOriginalSqlCommand(getQueryStatement(commandText));
                } else {
                    data.setConstruct(parseConstruct(actionName));
                    data.setOriginalSqlCommand(actionName);

                }
            }else {
                data.setConstruct(parseConstruct(actionName));
                data.setOriginalSqlCommand(actionName);
            }

        } catch (Exception e) {
            logger.error(" Databricks filter: Error parsing parseData method ", e);
        }
        return data;
    }

    private static Construct parseConstruct(String fullsqlString) {
        Sentence sentence = new Sentence(fullsqlString);
        sentence.setObjects(null);
        Construct construct = new Construct();
        construct.sentences.add(sentence);
        construct.setFullSql(fullsqlString);

        return construct;
    }

    /**
     * Method to get queryStatement from JsonObject
     *
     * @param commandText
     * @return
     */
    private static String getQueryStatement(String commandText) {
        String queryStatement = StringUtils.EMPTY;
        // Remove tabs, newlines, carriage returns, and extra spaces
        commandText = commandText.replaceAll("\"", " ").trim();
        commandText = commandText.replaceAll("--.*?\\\\n", "");

        commandText = commandText.replaceAll("\\r|\\t|\\n", " ").replaceAll(System.getProperty("line.separator"), "");
        commandText = commandText.replaceAll("\\\\n", " ");
        commandText = commandText.replaceAll("\\s+", " ");
        if (!StringUtils.isEmpty(commandText)) {
            queryStatement = commandText;
        }
        return queryStatement;
    }

    private static String getServiceName(JsonObject record) {
        String serviceName = Constants.UNKNOWN_STRING;
        if (record.has(Constants.SERVICE_NAME)) {
            serviceName = record.get(Constants.SERVICE_NAME).getAsString();
        }
        return serviceName;
    }

    private static String getActionName(JsonObject record) {
        String actionName = Constants.UNKNOWN_STRING;
        if (record.has(Constants.ACTION_NAME)) {
            actionName = record.get(Constants.ACTION_NAME).getAsString();
        }
        return actionName;
    }

    /**
     * parserSessionLocator() method will perform operation on JsonObject records,
     * set the expected value into respective SessionLocator Object and then return
     * the value as response
     *
     * @param properties
     * @return
     */
    private static SessionLocator parserSessionLocator(JsonObject properties) {
        SessionLocator sessionLocator = new SessionLocator();
        String sourceIP = Constants.DEFAULT_IP;
        if ((properties instanceof JsonObject) && properties.has(Constants.SOURCE_IP)
                && !properties.get(Constants.SOURCE_IP).toString().equalsIgnoreCase("null")) {
            sourceIP = properties.get(Constants.SOURCE_IP).getAsString();
        }

        sessionLocator.setIpv6(false);
        if (Util.isIPv6(sourceIP)) {
            sessionLocator.setIpv6(true);
            sessionLocator.setClientIpv6(sourceIP);
            sessionLocator.setClientIp(Constants.DEFAULT_IP);

        } else { // ipv4
            sessionLocator.setServerIp(Constants.DEFAULT_IP);
            sessionLocator.setClientIp(sourceIP);
            sessionLocator.setClientIpv6(Constants.DEFAULT_IPV6);
            sessionLocator.setServerIpv6(Constants.DEFAULT_IPV6);
        }
        sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);
        sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);

        return sessionLocator;
    }

    /**
     * parseAccessor() method will perform operation on JsonObject records, set the
     * expected value into respective Accessor Object and then return the value as
     * response
     *
     * @return
     */
    static Accessor parseAccessor(String subId, String accountId, JsonObject properties, JsonObject record) {
        Accessor accessor = new Accessor();

        // Set client host name
        String clientHostName = Constants.UNKNOWN_STRING;
        if (properties.has(Constants.SOURCE_IP)
                && !properties.get(Constants.SOURCE_IP).toString().equalsIgnoreCase("null")) {
            clientHostName = properties.get(Constants.SOURCE_IP).getAsString();
        }
        accessor.setClientHostName(clientHostName);
        accessor.setServerHostName(
                !subId.isEmpty() && !accountId.isEmpty()
                        ? subId+":"+getInstanceName(record)
                        : Constants.UNKNOWN_STRING);

        // Set database user

        if (record.has(Constants.IDENTITY)) {
            String identity = record.get(Constants.IDENTITY).toString().split(",")[0].split(":")[1];
            accessor.setDbUser(identity.replaceAll("\"", "").replace("\\", "").replace("\\\\", ""));
        } else {
            accessor.setDbUser(Constants.NOT_AVAILABLE);
        }

        // Set server type and protocol
        accessor.setServerType(Constants.SERVER_TYPE);
        accessor.setDbProtocol(Constants.DATA_PROTOCOL);
        accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);

        // Set source program (user agent)
        accessor.setSourceProgram(properties.has(Constants.USER_AGENT)
                ? (properties.get(Constants.USER_AGENT).toString().isEmpty()||properties.get(Constants.USER_AGENT).toString().contains("\"")?Constants.UNKNOWN_STRING:properties.get(Constants.USER_AGENT).toString())
                : Constants.UNKNOWN_STRING);

        // Set server description
        accessor.setServerDescription(properties.has(Constants.ACTION_NAME)
                ? properties.get(Constants.ACTION_NAME).toString()
                : Constants.UNKNOWN_STRING);

        // Set language and data type
        accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);

        // Set additional fields
        accessor.setClient_mac(Constants.UNKNOWN_STRING);
        accessor.setCommProtocol(Constants.UNKNOWN_STRING);
        accessor.setOsUser(Constants.UNKNOWN_STRING);
        accessor.setClientOs(Constants.UNKNOWN_STRING);
        accessor.setServerOs(Constants.UNKNOWN_STRING);

        return accessor;
    }

}