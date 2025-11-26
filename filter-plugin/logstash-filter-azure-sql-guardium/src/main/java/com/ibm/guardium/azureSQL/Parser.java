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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import co.elastic.logstash.api.Event;

public class Parser {

  private static String getFieldString(final Event e, final String key, final String defaulString) {
    Object v = e.getField(key);
    if (v == null) {
      return defaulString;
    }
    return v.toString();
  }

  public static Record parseRecord(final Event e) throws ParseException {

    Record record = new Record();
    record.setSessionId(getFieldString(e, Constants.Session_ID, Constants.UNKNOWN_STRING));

    String databaseName = Constants.NOT_AVAILABLE;
    if (e.getField(Constants.DATABASE_NAME) != null) {
      databaseName = e.getField(Constants.DATABASE_NAME).toString();
    }
    record.setDbName(databaseName);

    record.setAppUserName(Constants.APP_USER_NAME);

    record.setTime(Parser.parseTimestamp(e));

    record.setSessionLocator(Parser.parseSessionLocator(e));

    record.setAccessor(Parser.parseAccessor(e));

    if (e.getField(Constants.SUCCEEDED).toString().contains("true")) {
      Data data = new Data();
      data.setOriginalSqlCommand(getFieldString(e, Constants.STATEMENT, Constants.NOT_AVAILABLE));
      record.setData(data);
    } else {
      ExceptionRecord exceptionRecord = new ExceptionRecord();
      exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);
      exceptionRecord.setDescription(
          getFieldString(e, Constants.ADDITIONAL_INFORMATION, Constants.UNKNOWN_STRING));
      exceptionRecord.setSqlString(getFieldString(e, Constants.STATEMENT, Constants.NOT_AVAILABLE));
      record.setException(exceptionRecord);
    }
    return record;
  }

  public static Time parseTimestamp(final Event e) {
    long date = 0;
    String dateString = e.getField(Constants.TIMESTAMP).toString();
    try {
      date = Long.parseLong(dateString);
    } catch (NumberFormatException nfe) {
      // fallback to current time in nanoseconds to avoid failure
      date = System.currentTimeMillis() * 1_000_000L;
    }
    long mini = date / 1000000;

    return new Time(mini, 0, 0);
  }

  public static SessionLocator parseSessionLocator(final Event e) {

    SessionLocator sessionLocator = new SessionLocator();

    sessionLocator.setClientIp(getFieldString(e, Constants.Client_IP, Constants.DEFAULT_IP));
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
    accessor.setClientHostName(
        getFieldString(e, Constants.CLIENT_HOST_NAME, Constants.UNKNOWN_STRING));
    accessor.setClientOs(Constants.UNKNOWN_STRING);
    accessor.setDbUser(getFieldString(e, Constants.User_Name, Constants.NOT_AVAILABLE));
    accessor.setServerType(Constants.SERVER_TYPE_STRING);
    accessor.setCommProtocol(Constants.UNKNOWN_STRING);
    accessor.setDbProtocol(Constants.DATA_PROTOCOL_STRING);
    accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
    accessor.setSourceProgram(getFieldString(e, Constants.APPLICATION_NAME, Constants.NOT_AVAILABLE));
    accessor.setClient_mac(Constants.UNKNOWN_STRING);
    accessor.setServerDescription(Constants.UNKNOWN_STRING);

    String databaseName = Constants.NOT_AVAILABLE;
    if (e.getField(Constants.DATABASE_NAME) != null) {
      databaseName = e.getField(Constants.DATABASE_NAME).toString();
    }
    accessor.setServiceName(databaseName);
    accessor.setServerOs(Constants.UNKNOWN_STRING);
    accessor.setServerHostName(
        getFieldString(e, Constants.Server_Hostname, Constants.UNKNOWN_STRING));
    accessor.setOsUser(Constants.UNKNOWN_STRING);
    return accessor;
  }
}
