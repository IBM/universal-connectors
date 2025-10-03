/*
Copyright IBM Corp. 2021, 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.alloydb;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.custom_parsing.CustomParser;
import com.ibm.guardium.universalconnector.commons.custom_parsing.ParserFactory;
import static com.ibm.guardium.alloydb.Constants.*;
import static com.ibm.guardium.universalconnector.commons.custom_parsing.PropertyConstant.*;

import com.ibm.guardium.universalconnector.commons.custom_parsing.SqlParser;
import com.ibm.guardium.universalconnector.commons.structures.*;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.regex.*;
import java.util.*;

/**
 * Parser Class will perform operation on parsing events and messages from the AlloyDB audit logs
 * into a Guardium record instance Guardium records include the accessor, the sessionLocator, data,
 * and exceptions. If there are no errors, the data contains details about the query "construct"
 *
 * @className @Parser
 */
public class Parser extends CustomParser {
  private static Logger logger = LogManager.getLogger(Parser.class);
  private static final String HOST_REGEX = "host=([a-fA-F0-9:.]+)";
  private static final String PORT_REGEX = "port=(\\d+)";
  private static final String DB_REGEX = "db=([^,]+)";
  private static final String USER_REGEX = "user=([^ ]+)";
  private static final String SQL_STATEMENT_REGEX = "(?i)statement:\\s+(.*)";
  private static final String ERROR_REGEX = "ERROR:(.*)";
  private static final String FATAL_ERROR_REGEX = "FATAL:(.*)";
  ;
  private Map<String, String> textPayloadFields;
  private static final Pattern hostPattern = Pattern.compile(HOST_REGEX);
  private static final Pattern portPattern = Pattern.compile(PORT_REGEX);
  private static final Pattern sqlStatementPattern =
      Pattern.compile(SQL_STATEMENT_REGEX, Pattern.DOTALL);
  private static final Pattern dbPattern = Pattern.compile(DB_REGEX);
  private static final Pattern dbUserPattern = Pattern.compile(USER_REGEX);
  private static final Pattern errorPattern = Pattern.compile(ERROR_REGEX);
  private static final Pattern fatalErrorPattern = Pattern.compile(FATAL_ERROR_REGEX);

  public Parser(ParserFactory.ParserType parserType) {
    super(parserType);
  }

  @Override
  public String getConfigFileContent() {
    return ConfigurationGenerator.getConfig();
  }

  @Override
  public Record parseRecord(String payload) {
    textPayloadFields = new HashMap<>();
    if (!isValid(payload)) return null;

    String textPayload = getValue(payload, TEXT_PAYLOAD);
    parseTextPayload(textPayload);
    Record record = extractRecord(payload);
    Data data = record.getData();
    if (data != null && Objects.equals(data.getOriginalSqlCommand(), NOT_AVAILABLE)) {
      throw new RuntimeException("SQL command is not available" + payload);
    }
    return record;
  }

  private void parseTextPayload(String textPayload) {
    textPayloadFields.put(IP, DEFAULT_IP);
    textPayloadFields.put(PORT, "-1");
    textPayloadFields.put(Constants.DB_USER, NOT_AVAILABLE);
    textPayloadFields.put(SQL_STATEMENT, NOT_AVAILABLE);
    textPayloadFields.put(Constants.DB_NAME, NOT_AVAILABLE);
    textPayloadFields.put(ERROR, NOT_AVAILABLE);
    if (textPayload == null) {
      return;
    }

    Matcher matcher = hostPattern.matcher(textPayload);
    if (matcher.find()) {
      textPayloadFields.put(IP, matcher.group(1));
    }

    matcher = portPattern.matcher(textPayload);
    if (matcher.find()) {
      textPayloadFields.put(PORT, matcher.group(1));
    }

    matcher = sqlStatementPattern.matcher(textPayload);
    if (matcher.find()) {
      textPayloadFields.put(SQL_STATEMENT, matcher.group(1));
    }

    matcher = dbPattern.matcher(textPayload);
    if (matcher.find()) {
      textPayloadFields.put(Constants.DB_NAME, matcher.group(1));
    }

    matcher = dbUserPattern.matcher(textPayload);
    if (matcher.find()) {
      textPayloadFields.put(Constants.DB_USER, matcher.group(1));
    }

    matcher = errorPattern.matcher(textPayload);
    if (matcher.find()) {
      textPayloadFields.put(ERROR, matcher.group(1));
    }

    matcher = fatalErrorPattern.matcher(textPayload);
    if (matcher.find()) {
      textPayloadFields.put(ERROR, matcher.group(1));
    }
  }

  @Override
  protected Integer getClientPort(String payload) {
    int clientPort;
    try {
      clientPort = Integer.parseInt(textPayloadFields.get(PORT));
      return clientPort;
    } catch (Exception e) {
      return -1;
    }
  }

  @Override
  protected String getClientIp(String payload) {
    String clientIp;
    if (Objects.equals(textPayloadFields.get(IP), DEFAULT_IP)) {
      clientIp = getValue(payload, CLIENT_IP);
    } else {
      clientIp = textPayloadFields.get(IP);
    }

    if (Objects.equals(clientIp, "") || clientIp == null) {
      return DEFAULT_IP;
    }
    return clientIp;
  }

  @Override
  protected String getClientIpv6(String payload) {
    String clientIp;
    if (Objects.equals(textPayloadFields.get(IP), DEFAULT_IP)) {
      clientIp = getValue(payload, CLIENT_IP);
    } else {
      clientIp = textPayloadFields.get(IP);
    }

    if (Objects.equals(clientIp, "") || clientIp == null) {
      return DEFAULT_IPV6;
    }
    return clientIp;
  }

  @Override
  protected String getSqlString(String payload) {
    String sqlString = textPayloadFields.get(SQL_STATEMENT);
    if (sqlString != null && !sqlString.equals(NOT_AVAILABLE)) {
      return sqlString
          .replace("\n", " ")
          .replaceAll("\\s{2,}", " ")
          .replaceAll("\\(\\s+", "(")
          .replaceAll("\\s+\\)", ")")
          .trim();
    } else {
      return sqlString;
    }
  }

  @Override
  protected String getDbUser(String payload) {
    String dbUser = "";
    if (Objects.equals(textPayloadFields.get(Constants.DB_USER), NOT_AVAILABLE)) {
      dbUser = getValue(payload, Constants.DB_USER);
    } else {
      dbUser = textPayloadFields.get(Constants.DB_USER);
    }
    if (Objects.equals(dbUser, "") || dbUser == null) {
      dbUser = NOT_AVAILABLE;
    }
    return dbUser;
  }

  @Override
  protected String getDbName(String payload) {
    String dbName = "";
    if (Objects.equals(textPayloadFields.get(Constants.DB_NAME), NOT_AVAILABLE)) {
      dbName = getValue(payload, Constants.DB_NAME);
    } else {
      dbName = textPayloadFields.get(Constants.DB_NAME);
    }
    if (Objects.equals(dbName, "") || dbName == null) {
      dbName = NOT_AVAILABLE;
    }
    return dbName;
  }

  @Override
  protected String getServiceName(String payload) {
    return getDbName(payload);
  }

  @Override
  protected ExceptionRecord getException(String payload, String sqlString) {
    String severityType = getValue(payload, Constants.EXCEPTION_TYPE_ID);
    if (!Objects.equals(severityType, "ERROR")
        && !Objects.equals(severityType, "CRITICAL")
        && (!Objects.equals(severityType, "ALERT"))) {

      return null;
    } else {
      ExceptionRecord exceptionRecord = new ExceptionRecord();
      if (Objects.equals(severityType, "ALERT")) {
        exceptionRecord.setExceptionTypeId(EXCEPTION_TYPE_AUTHENTICATION_STRING);
      } else {
        exceptionRecord.setExceptionTypeId(EXCEPTION_TYPE_AUTHORIZATION_STRING);
      }
      exceptionRecord.setDescription(textPayloadFields.get(ERROR));
      exceptionRecord.setSqlString(getSqlString(payload));
      return exceptionRecord;
    }
  }

  @Override
  protected String getLanguage(String payload) {
    return getValue(payload, LANGUAGE);
  }

  @Override
  protected String getDataType(String payload) {
    return getValue(payload, DATA_TYPE);
  }

  @Override
  protected String getServerType(String payload) {
    return getValue(payload, Constants.SERVER_TYPE);
  }
}
