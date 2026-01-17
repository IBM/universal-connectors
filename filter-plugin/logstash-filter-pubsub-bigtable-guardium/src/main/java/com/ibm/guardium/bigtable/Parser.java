/*
Copyright IBM Corp. 2021, 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.bigtable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.guardium.bigtable.errorcode.BigTableErrorCodes;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.time.ZonedDateTime;
import java.util.Locale;

import static com.ibm.guardium.bigtable.ApplicationConstants.*;
import static com.ibm.guardium.bigtable.CommonUtils.convertToInt;

/**
 * Parser Class will perform operation on parsing events and messages from the
 * BigQuery audit logs into a Guardium record instance Guardium records include
 * the accessor, the sessionLocator, data, and exceptions. If there are no
 * errors, the data contains details about the query "construct"
 *
 * @className @Parser
 */
public class Parser {
    static InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();

    /**
     * parseRecord() method will perform operation on JsonObject input, convert
     * JsonObject into Record Object and then return the value as response
     *
     * @param input the json input
     * @return Record GUARDIUM Object
     * @methodName @parseRecord
     */
    public static Record parseRecord(JsonObject input) {
        var protoPayload = getChildJsonObject(input, PROTO_PAYLOAD);

        // Add some simple check on mandatory tags
        if (protoPayload == null)
            return null;

        var dataBaseDetails = extractDatabaseDetails(
                getChildJsonObject(input, RESOURCE), protoPayload);

        var projectId = dataBaseDetails.projectId;
        var instanceId = dataBaseDetails.instanceId;
        var tableId = dataBaseDetails.tableId;

        var bigTableRecord = new Record();

        bigTableRecord.setDbName(getDbName(projectId, instanceId));

        String fullSql = getFullSql(projectId, instanceId, tableId, protoPayload);

        ExceptionRecord exceptionRecord = parseException(getChildString(input, SEVERITY),
                getChildJsonObject(protoPayload, STATUS), fullSql);
        if (exceptionRecord != null) {
            bigTableRecord.setException(exceptionRecord);
            // If there is no syntax error, we add this to full sql report
            if (exceptionRecord.getDescription() != null && !exceptionRecord.getDescription().toLowerCase().contains(SYNTAX_ERROR)) {
                bigTableRecord.setData(Parser.parseData(protoPayload, projectId, instanceId, tableId, fullSql));
            }
        }
        else
            bigTableRecord.setData(Parser.parseData(protoPayload, projectId, instanceId, tableId, fullSql));

        var appUserName = getAppUserName(protoPayload);
        bigTableRecord.setAppUserName(appUserName);
        bigTableRecord
                .setAccessor(parseAccessor(appUserName, projectId, getFieldValueByKey(protoPayload, SERVICE_NAME)));

        bigTableRecord.setSessionLocator(parseSessionLocator(protoPayload));

        bigTableRecord.setTime(parseTime(getFieldValueByKey(input, TIMESTAMP)));

        bigTableRecord.setSessionId(StringUtils.EMPTY);

        return bigTableRecord;
    }

    static String getDbName(String projectId, String instanceId) {
        if (instanceId.isEmpty())
            return projectId;
        if (projectId.isEmpty())
            return instanceId;
        return projectId + COLON + instanceId;
    }

    /**
     * This method parses different json objects to extract projectId, instanceId
     * and tableId if any.
     *
     * @param resource     the resource json object
     * @param protoPayload the protoPayload json object
     * @return a DataBaseDetails object containg projectId, instanceId and tableId
     */
    static DataBaseDetails extractDatabaseDetails(JsonObject resource, JsonObject protoPayload) {
        DataBaseDetails dataBaseDetails = new DataBaseDetails();
        var resourceName = getChildString(protoPayload, RESOURCE_NAME);
        if (!resourceName.isEmpty()) {
            dataBaseDetails = extractDatabaseDetailsFromResource(resourceName);
            if (dataBaseDetails.isAllSet())
                return dataBaseDetails;
        }

        if (resource != null) {
            var resourceLabels = getChildJsonObject(resource, LABELS);
            dataBaseDetails.projectId = getChildString(resourceLabels, PROJECT_ID);
            dataBaseDetails.instanceId = getChildString(resourceLabels, INSTANCE_ID);
            dataBaseDetails.tableId = getChildString(resourceLabels, TABLE_ID);

            // if we already found all 3 of projectId, instanceId and tableId we are all set
            // otherwise we need to .
            if (dataBaseDetails.isAllSet())
                return dataBaseDetails;
        }

        return extractDatabaseDetailsFromAuthorizationInfo(
                getChildJsonArray(protoPayload, AUTHORIZATION_INFO));
    }

    /**
     * This methos parses authorizationInfo json object to extract projectId,
     * instanceId and tableId if any.
     * An example of protoPayload.authorizationInfo:
     * authorizationInfo: [
     * {
     * resource: "projects/test-Project/instances/test-instance/tables/test-table",
     * permission: "bigtable.tables.mutateRows",
     * granted: true,
     * resourceAttributes: {}
     * }
     * ]
     *
     * @param authorizationInfo the authorizationInfo json object.
     * @return a DataBaseDetails object containg projectId, instanceId and tableId
     */
    private static DataBaseDetails extractDatabaseDetailsFromAuthorizationInfo(JsonArray authorizationInfo) {
        DataBaseDetails dataBaseDetails = new DataBaseDetails();
        if (authorizationInfo == null || authorizationInfo.isEmpty())
            return dataBaseDetails;

        boolean found = false;
        for (int j = 0; j < authorizationInfo.size() && !found; j++) {
            if (authorizationInfo.get(j) == null || authorizationInfo.get(j).getAsJsonObject() == null)
                continue;

            dataBaseDetails = extractDatabaseDetailsFromResource(
                    getChildString(authorizationInfo.get(j).getAsJsonObject(), RESOURCE));

            if (dataBaseDetails.isAllSet())
                found = true;
        }

        return dataBaseDetails;
    }

    /**
     * This method parses authorizationInfoResource json object to extract
     * projectId, instanceId and tableId if any.
     * An example of : protoPayload.authorizationInfo.resource:
     * "projects/test-Project/instances/test-instance/tables/test-table"
     *
     * @param resource the authorizationInfo json object.
     * @return a DataBaseDetails object containg projectId, instanceId and tableId
     */
    static DataBaseDetails extractDatabaseDetailsFromResource(String resource) {
        DataBaseDetails dataBaseDetails = new DataBaseDetails();

        var authorizationInfoResourceArr = resource.split(SLASH);
        int i = 0;
        int len = authorizationInfoResourceArr.length;
        while (i < len - 1) {
            switch (authorizationInfoResourceArr[i].toLowerCase()) {
                case PROJECTS:
                    dataBaseDetails.projectId = authorizationInfoResourceArr[i + 1];
                    i += 2;
                    break;
                case INSTANCES:
                    dataBaseDetails.instanceId = authorizationInfoResourceArr[i + 1];
                    i += 2;
                    break;
                case TABLES:
                    dataBaseDetails.tableId = authorizationInfoResourceArr[i + 1];
                    i += 2;
                    break;
                default:
                    i += 1;
                    break;
            }
        }
        return dataBaseDetails;
    }

    /**
     * Parsing the input to extract info to create a Data object
     *
     * @param protoPayload the protoPayload json object
     * @param projectId    the projectId value
     * @param instanceId   the instanceId value
     * @param tableId      the tableId value
     * @param fullSql        the value extracted for fullSql field
     * @return a Data object
     */
    static Data parseData(JsonObject protoPayload, String projectId, String instanceId,
            String tableId,
            String fullSql) {
        Data data = new Data();
        Construct construct = parseAsConstruct(protoPayload, projectId, instanceId, tableId, fullSql);
        data.setConstruct(construct);
        data.setOriginalSqlCommand(fullSql);
        construct.setRedactedSensitiveDataSql(fullSql);
        return data;
    }

    /**
     * Parsing the protoPayload json object to extract info to create a Construct
     * object
     *
     * @param protoPayload the protoPayload json object
     * @param instanceId   the instanceId value
     * @param tableId      the tableId value
     * @return a Construct object
     */
    static Construct parseAsConstruct(JsonObject protoPayload, String projectId, String instanceId, String tableId, String fullSql) {
        final Sentence sentence = parseSentence(protoPayload, projectId, instanceId, tableId);
        final Construct construct = new Construct();
        construct.sentences.add(sentence);
        construct.setFullSql(fullSql);
        construct.setRedactedSensitiveDataSql(fullSql);

        return construct;
    }

    static String getVerb(JsonObject protoPayload) {
        var verb = getChildString(protoPayload, METHOD_NAME);
        var lastIndex = verb.lastIndexOf(DOT);
        if (lastIndex > -1 && lastIndex < (verb.length() - 1))
            verb = verb.substring(lastIndex + 1);
        return verb;
    }

    /**
     * Parsing the protoPayload json object to extract info to create a Sentence
     * object
     *
     * @param protoPayload the protoPayload json object
     * @param instanceId   the instanceId value
     * @param tableId      the tableId value
     * @return a Sentence object
     */
    protected static Sentence parseSentence(JsonObject protoPayload, String projectId, String instanceId,
            String tableId) {
        var sentence = new Sentence(getVerb(protoPayload));
        if (!projectId.isEmpty()) {
            var projectObject = new SentenceObject(projectId);
            projectObject.setType(PROJECT);
            sentence.getObjects().add(projectObject);
        }

        if (!instanceId.isEmpty()) {
            var instanceObject = new SentenceObject(instanceId);
            instanceObject.setType(INSTANCE);
            sentence.getObjects().add(instanceObject);
        }

        if (!tableId.isEmpty()) {
            var tableObject = new SentenceObject(tableId);
            tableObject.setType(TABLE);
            sentence.getObjects().add(tableObject);
        }

        return sentence;
    }
    static String getFullSql(String projectId, String instanceId, String tableId,
                             JsonObject protoPayload) {
        var resource = getChildString(protoPayload, RESOURCE_NAME);
        if (resource.isEmpty())
            resource = getResourceName(projectId, instanceId, tableId);

        String fullSql;
        var verb = getVerb(protoPayload);
        if (verb.equalsIgnoreCase(EXECUTE_QUERY))
            fullSql = parseExecuteQuery(protoPayload, resource);
        else
            fullSql = verb + ON + resource;

        return fullSql;
    }

    static String getResourceName(String projectId, String instanceId, String tableId) {
        String resource = StringUtils.EMPTY;
        if (!projectId.isEmpty())
            resource += PROJECTS + SLASH + projectId;

        if (!instanceId.isEmpty())
            resource += INSTANCES + SLASH + instanceId;

        if (!tableId.isEmpty())
            resource += TABLES + SLASH + tableId;
        return resource;
    }

    static String parseExecuteQuery(JsonObject protoPayload, String resource) {
        var authorizationInfo = getChildJsonArray(protoPayload, AUTHORIZATION_INFO);
        if (authorizationInfo == null || authorizationInfo.isEmpty())
            return EXECUTE_QUERY + ON + resource;

        StringBuilder sb = new StringBuilder();
        for (var i = 0; i < authorizationInfo.size(); i++) {
            if (authorizationInfo.get(i) == null || authorizationInfo.get(i).getAsJsonObject() == null)
                continue;

            if (!sb.toString().isEmpty())
                sb.append(SEMICOLON);

            var authorizationInfoItem = authorizationInfo.get(i).getAsJsonObject();
            var permission = getChildString(authorizationInfoItem, PERMISSION);
            permission = lastPart(permission, DOT);

            var name = StringUtils.EMPTY;
            var resourceAttribute = getChildJsonObject(authorizationInfoItem, RESOURCE_ATTRIBUTES);
            if (resourceAttribute != null) {
                name = getChildString(resourceAttribute, NAME);
                if (name.isEmpty())
                    name = resource;
            }

            sb.append(permission).append(ON).append(name);
        }

        return sb.toString();
    }

    static String lastPart(String source, String charToSearch) {
        int lastIndex = source.lastIndexOf(charToSearch);
        if (lastIndex > -1 && lastIndex < source.length() - 1)
            return source.substring(lastIndex + 1);
        return source;
    }

    /**
     * This method returns a string containing request, response and some more
     * useful pieces if exist in the log
     *
     * @param input        the input json object
     * @param protoPayload the protoPayload json object
     * @param noSql        the value extracted for noSql field
     * @return a string of some important values
     */
    public static String getOriginalSqlCommand(JsonObject input, JsonObject protoPayload, JsonObject noSql) {
        noSql.addProperty(INSERT_ID, getChildString(input, INSERT_ID));

        var response = getChildJsonObject(input, RESPONSE);
        if (response != null)
            noSql.add(RESPONSE, response);

        var authorizationInfo = getChildJsonArray(protoPayload, AUTHORIZATION_INFO);
        if (authorizationInfo != null)
            noSql.add(AUTHORIZATION_INFO, authorizationInfo);

        var authenticationInfo = getChildJsonObject(protoPayload, AUTHENTICATION_INFO);
        if (authenticationInfo != null)
            noSql.add(AUTHENTICATION_INFO, authenticationInfo);

        return noSql.toString();
    }

    static JsonObject getChildJsonObject(JsonObject parentJsonObject, String childName) {
        if (!parentJsonObject.has(childName) || parentJsonObject.get(childName).getAsJsonObject() == null
                || parentJsonObject.get(childName).getAsJsonObject().entrySet().isEmpty()) {
            return null;
        }

        return parentJsonObject.getAsJsonObject(childName);
    }

    static JsonArray getChildJsonArray(JsonObject parentJsonObject, String childName) {
        if (!parentJsonObject.has(childName) || parentJsonObject.get(childName).getAsJsonArray() == null
                || parentJsonObject.get(childName).getAsJsonArray().isEmpty()) {
            return null;
        }

        return parentJsonObject.getAsJsonArray(childName);
    }

    static String getChildString(JsonObject parentJsonObject, String childName) {
        if (parentJsonObject == null || !parentJsonObject.has(childName)
                || parentJsonObject.get(childName) == null)
            return StringUtils.EMPTY;

        return CommonUtils.convertIntoString(parentJsonObject.get(childName));
    }

    /**
     * parseAccessor() method will perform operation on String inputs, set the
     * expected value into respective Accessor Object and then return the value as
     * response
     *
     * @param appUserName user name
     * @param projectId   project_id
     * @param serviceName serviceName
     * @return an accessor object
     */

    static Accessor parseAccessor(String appUserName, String projectId, String serviceName) {
        var accessor = new Accessor();

        accessor.setServerType(SERVER_TYPE_STRING);
        accessor.setServerOs(UNKOWN_STRING);

        accessor.setClientOs(UNKOWN_STRING);
        accessor.setClientHostName(UNKOWN_STRING);

        accessor.setServerHostName(projectId + UNDERLINE + serviceName);

        accessor.setCommProtocol(UNKOWN_STRING);

        accessor.setDbProtocol(DATA_PROTOCOL_STRING);
        accessor.setDbProtocolVersion(UNKOWN_STRING);

        accessor.setOsUser(UNKOWN_STRING);
        accessor.setSourceProgram(UNKOWN_STRING);

        accessor.setClient_mac(UNKOWN_STRING);
        accessor.setServerDescription(UNKOWN_STRING);

        accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);

        accessor.setDbUser(appUserName);
        accessor.setServiceName(serviceName);
        return accessor;
    }

    /**
     * parserSesstionLocator() method will perform operation on String input, set
     * the expected value into respective SessionLocator Object and then return the
     * value as response
     *
     * @param protoPayload the protoPayload json object
     * @return SessionLocator GUARDIUM Object
     * @methodName @parserSesstionLocator
     */
    static SessionLocator parseSessionLocator(JsonObject protoPayload) {
        String callerIp = getCallerIp(protoPayload);
        SessionLocator sessionLocator = new SessionLocator();
        sessionLocator.setIpv6(false);
        if (inetAddressValidator.isValidInet6Address(callerIp)) {
            sessionLocator.setIpv6(true);
            sessionLocator.setClientIpv6(callerIp);
            sessionLocator.setServerIpv6(DEFAULT_IPV6);
        } else if (inetAddressValidator.isValidInet4Address(callerIp)) {
            sessionLocator.setClientIp(callerIp);
            sessionLocator.setServerIp(DEFAULT_IP);
        } else {
            sessionLocator.setClientIp(DEFAULT_IP);
            sessionLocator.setServerIp(DEFAULT_IP);
        }
        sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
        sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);
        return sessionLocator;
    }

    /**
     * parseTime() method will perform operation on String inputs, set the expected
     * value into respective Time Object and then return the value as response
     *
     * @return ExceptionRecord GUARDIUM Object
     */
    public static Time parseTime(String dateString) {
        ZonedDateTime date = ZonedDateTime.parse(dateString);
        long millis = date.toInstant().toEpochMilli();
        int minOffset = date.getOffset().getTotalSeconds() / 60;
        return new Time(millis, minOffset, 0);
    }

    /**
     * getAppUserName() method will perform operation on JsonObject input object
     * convert into appUserName and return response
     *
     * @return String
     * @methodName @getAppUserName
     */
    static String getAppUserName(JsonObject protoPayloadJsonObject) {
        var authenticationJSON = getChildJsonObject(protoPayloadJsonObject, AUTHENTICATION_INFO);
        if (authenticationJSON != null)
            return CommonUtils.convertIntoString(authenticationJSON.get(PRINCIPAL_EMAIL));
        return StringUtils.EMPTY;
    }

    /**
     * getCallerIp() method will perform operation on JsonObject input object
     * convert into callerIp and return response
     *
     * @param protoPayload the protoPayload json object
     * @return String
     * @methodName @getCallerIp
     */

    static String getCallerIp(JsonObject protoPayload) {
        var requestMetadataJson = getChildJsonObject(protoPayload, REQUEST_METADATA);
        var callerIp = getChildString(requestMetadataJson, CALLER_IP);
        if (!callerIp.isEmpty())
            return callerIp;
        return DEFAULT_IP;
    }

    /**
     * getFieldValueByKey() method will perform operation on JsonObject jsonObject
     * and String as a key object into JsonObject and return response
     *
     * @return the value of argument key extracted from the jsonObject
     * @methodName @getFieldValueByKey
     */
    static String getFieldValueByKey(JsonObject jsonObject, String key) {
        return CommonUtils.convertIntoString(jsonObject.get(key));
    }

    static ExceptionRecord parseException(String severity, JsonObject status, String fullSql) {
        var statusMessage = StringUtils.EMPTY;
        var statusCode = -1;
        if (status != null) {
            statusCode = convertToInt(status.get(CODE));
            if (statusCode > 0) {
                statusMessage = STATUS_CODE + statusCode + DESCRIPTION
                        + getChildString(status, MESSAGE);
            }
        }

        if (severity.equalsIgnoreCase(ERROR)
                || severity.equalsIgnoreCase(CRITICAL) || statusCode > 0) {

            ExceptionRecord exceptionRecord = new ExceptionRecord();
            exceptionRecord.setExceptionTypeId(SQL_ERROR);
            exceptionRecord.setDescription(statusMessage);
            exceptionRecord.setSqlString(fullSql);
            return exceptionRecord;
        }

        return null;
    }

    static class DataBaseDetails {
        String projectId;
        String instanceId;
        String tableId;

        DataBaseDetails() {
            projectId = StringUtils.EMPTY;
            instanceId = StringUtils.EMPTY;
            tableId = StringUtils.EMPTY;
        }

        boolean isAllSet() {
            return !projectId.isEmpty() && !instanceId.isEmpty() && !tableId.isEmpty();
        }
    }
}
