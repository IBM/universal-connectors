/*
Copyright IBM Corp. 2021, 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.trino;

import com.google.gson.JsonObject;

import com.ibm.guardium.universalconnector.commons.structures.*;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ibm.guardium.trino.Constants.*;
import static com.ibm.guardium.trino.Constants.DB_PROTOCOL;
import static com.ibm.guardium.trino.Constants.SERVER_TYPE;
import static com.ibm.guardium.trino.Constants.DEFAULT_IP;

import static com.ibm.guardium.universalconnector.commons.custom_parsing.PropertyConstant.*;
import static com.ibm.guardium.universalconnector.commons.structures.Accessor.LANGUAGE_FREE_TEXT_STRING;

import com.ibm.guardium.trino.sql_parsing.SqlParser;

/**
 * Parser Class will perform operation on parsing events and messages from the Trino audit logs into
 * a Guardium record instance Guardium records include the accessor, the sessionLocator, data, and
 * exceptions. If there are no errors, the data contains details about the query "construct"
 *
 * @className Parser
 */
public class Parser {

  private static final SqlParser sqlParser = new SqlParser();
  private static final InetAddressValidator inetAddressValidator =
          InetAddressValidator.getInstance();

  public Parser() {
  }

  public static Record parseRecord(JsonObject data) {
    Record record = new Record();
    record.setSessionId(EMPTY);
    String DbName = data.has(Context) ? getDbName(data.getAsJsonObject(Context)) : NOT_AVAILABLE;
    record.setDbName(DbName);
    record.setAppUserName(NOT_AVAILABLE);
    String sqlString = data.has(Metadata) ? getSqlString(data.getAsJsonObject(Metadata)) : EMPTY;
    record.setException(
            data.has(FailureInfo)
                    ? getException(data.getAsJsonObject(FailureInfo), sqlString)
                    : getException(data, sqlString));
    record.setAccessor(getAccessor(data, DbName));
    record.setSessionLocator(getSessionLocator(data));
    record.setTime(getTimestamp(data));
    if (!record.isException()) record.setData(getData(sqlString));

    return record;
  }

  protected static Accessor getAccessor(JsonObject data, String DbName) {
    Accessor accessor = new Accessor();

    accessor.setServiceName(DbName);
    accessor.setDbUser(
            data.has(Context) ? getDbUser(data.getAsJsonObject(Context)) : NOT_AVAILABLE);
    accessor.setDbProtocolVersion(EMPTY);
    accessor.setDbProtocol(DB_PROTOCOL);
    accessor.setServerType(SERVER_TYPE);
    accessor.setServerOs(EMPTY);
    accessor.setServerDescription(EMPTY);
    accessor.setServerHostName(NOT_AVAILABLE);
    accessor.setClientHostName(EMPTY);
    accessor.setClient_mac(EMPTY);
    accessor.setClientOs(EMPTY);
    accessor.setCommProtocol(EMPTY);
    accessor.setOsUser(EMPTY);
    accessor.setSourceProgram(EMPTY);
    accessor.setLanguage(LANGUAGE_FREE_TEXT_STRING);
    accessor.setDataType(DATA_TYPE);

    return accessor;
  }

  private static String getSqlString(JsonObject metadata) {
    return metadata.has(SQLCommand) ? metadata.get(SQLCommand).getAsString() : EMPTY;
  }

  private static String getDbName(JsonObject metadata) {
    return metadata.has("schema")
            ? !metadata.get("schema").isJsonNull() ? metadata.get("schema").getAsString()
            : NOT_AVAILABLE
            : NOT_AVAILABLE;

  }

  private static String getDbUser(JsonObject context) {
    return context.has(DBUser) ? context.get(DBUser).getAsString() : NOT_AVAILABLE;
  }

  private static ExceptionRecord getException(JsonObject data, String sqlString) {
    String error = EMPTY;
    if (data.has("failureMessage")) error = data.get("failureMessage").getAsString();

    if (error.isEmpty()) return null;

    // default typeId
    String typeId = error.toUpperCase().contains("LOGIN") ? "LOGIN_FAILED" : "SQL_ERROR";

    ExceptionRecord exceptionRecord = new ExceptionRecord();
    exceptionRecord.setExceptionTypeId(typeId);
    exceptionRecord.setDescription(error);
    exceptionRecord.setSqlString(sqlString);

    return exceptionRecord;
  }

  private static Data getData(String sqlString) {
    return sqlParser.parseStatement(sqlString);
  }

  static SessionLocator getSessionLocator(JsonObject data) {
    SessionLocator sessionLocator = new SessionLocator();

    // set default values
    sessionLocator.setIpv6(false);
    sessionLocator.setClientIpv6(DEFAULT_IPV6);
    sessionLocator.setServerIpv6(DEFAULT_IPV6);
    sessionLocator.setClientIp(DEFAULT_IP);
    sessionLocator.setServerIp(DEFAULT_IP);

    String clientIp = data.has(Context) ? getClientIp(data.getAsJsonObject(Context)) : DEFAULT_IP;
    String serverIp = data.has(Context) ? getServerIp(data.getAsJsonObject(Context)) : DEFAULT_IP;
    if (clientIp != null && inetAddressValidator.isValidInet6Address(clientIp)) {
      // If client IP is IPv6, set both client and server to IPv6
      sessionLocator.setIpv6(true);
      sessionLocator.setClientIpv6(clientIp);
      sessionLocator.setServerIpv6(serverIp); // Set server IP to default IPv6

    } else if (clientIp != null && inetAddressValidator.isValidInet4Address(clientIp)) {
      // If client IP is IPv4, set both client and server IP to IPv4
      sessionLocator.setClientIp(clientIp);
      // Cloud Databases: Set server IP to 0.0.0.0
      sessionLocator.setServerIp(serverIp);
    }

    // Set port numbers
    sessionLocator.setClientPort(DEFAULT_PORT);
    sessionLocator.setServerPort(DEFAULT_PORT);

    return sessionLocator;
  }

  private static String getClientIp(JsonObject context) {
    return context.has(ClientIP) ? context.get(ClientIP).getAsString() : DEFAULT_IP;
  }

  private static String getServerIp(JsonObject context) {
    return context.has(ServerIP) ? context.get(ServerIP).getAsString() : DEFAULT_IP;
  }

  private static final Pattern URI_PORT_PATTERN = Pattern.compile("http://[^:/]+:(\\d+)/.*");

  private static int getServerPort(JsonObject metadata) {
    if (!metadata.has(URI)) return DEFAULT_PORT;
    String uri = metadata.get(URI).getAsString();
    Matcher port = URI_PORT_PATTERN.matcher(uri);
    if (port.find()) {
      return Integer.parseInt(port.group(1));
    }
    return DEFAULT_PORT;
  }

  static Time getTimestamp(JsonObject data) {
    String time = EMPTY;
    if (data.has(Time)) time = data.get(Time).getAsString();
    if (time.isEmpty()) return new Time(0L, 0, 0);

    try {
      ZonedDateTime date = ZonedDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
      long millis = date.toInstant().toEpochMilli();
      int minOffset = date.getOffset().getTotalSeconds() / 60;
      int minDst = date.getZone().getRules().isDaylightSavings(date.toInstant()) ? 60 : 0;
      return new Time(millis, minOffset, minDst);
    } catch (Exception ex) {
      return new Time(0L, 0, 0);
    }
  }
}
