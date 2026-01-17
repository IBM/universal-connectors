/*
Copyright IBM Corp. 2021, 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.capella;

import com.ibm.guardium.universalconnector.commons.custom_parsing.CustomParser;
import com.ibm.guardium.universalconnector.commons.custom_parsing.ParserFactory;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.ibm.guardium.capella.Constants.*;
import static com.ibm.guardium.universalconnector.commons.custom_parsing.PropertyConstant.DEFAULT_STRING;
import static com.ibm.guardium.universalconnector.commons.custom_parsing.PropertyConstant.SERVICE_NAME;
import static com.ibm.guardium.universalconnector.commons.structures.Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL;

/**
 * Parser Class will perform operation on parsing events and messages from the Capella audit logs
 * into a Guardium record instance Guardium records include the accessor, the sessionLocator, data,
 * and exceptions. If there are no errors, the data contains details about the query "construct"
 *
 * @className @Parser
 */
public class Parser extends CustomParser {
  private static Logger logger = LogManager.getLogger(Parser.class);

  private String statement;
  private boolean needSniffer;

  public Parser(ParserFactory.ParserType parserType) {
    super(parserType);
  }

  @Override
  public String getConfigFileContent() {
    return ConfigurationGenerator.getConfig();
  }

  @Override
  protected String getDbUser(String payload) {
    String domain = getNotNullString(getValue(payload, DB_USER_DOMAIN));
    String user = getNotNullString(getValue(payload, DB_USER));
    if (!user.isEmpty() && !domain.isEmpty()) return user + "@" + domain;
    return NOT_AVAILABLE;
  }

  @Override
  public Record parseRecord(String payload) {
    if (!isValid(payload)) return null;

    statement = getValue(payload, STATEMENT);
    if (statement != null && !statement.isEmpty()) needSniffer = true;
    else needSniffer = false;
    return extractRecord(payload);
  }

  @Override
  protected String getDbName(String payload) {
    String value = this.getValue(payload, DB_NAME);
    if (value == null) value = this.getValue(payload, BUCKET);
    return value != null ? value : NOT_AVAILABLE;
  }

  @Override
  protected Data getData(String payload, String sqlString) {
    Data data = new Data();
    Construct construct = new Construct();
    if (needSniffer) {
      data.setOriginalSqlCommand(PREPENDED_STRING + statement);
    } else {
      String verb = getNotNullString(getValue(payload, VERB));

      Sentence sentence = new Sentence(verb);

      String obj = getNotNullString(getValue(payload, OBJECT));
      if (!obj.isEmpty()) {
        SentenceObject sentenceObject = new SentenceObject(obj);
        sentenceObject.setType(OBJECT);
        sentence.getObjects().add(sentenceObject);
      }

      String index = getNotNullString(getValue(payload, INDEX));
      if (!index.isEmpty()) {
        SentenceObject sentenceObject = new SentenceObject(index);
        sentenceObject.setType(INDEX);
        sentence.getObjects().add(sentenceObject);
      }

      construct.sentences.add(sentence);
      construct.setFullSql(sqlString);
      data.setConstruct(construct);
      data.setOriginalSqlCommand(sqlString);
    }
    return data;
  }

  @Override
  protected String getDataType(String payload) {
    if (needSniffer) return Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL;

    return DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL;
  }

  @Override
  protected String getServiceName(String payload) {
    return getDbName(payload);
  }

  protected String getStatus(String payload) {
    String value = getValue(payload, SQL_STATUS);
    return value != null ? value : DEFAULT_STRING;
  }

  @Override
  protected ExceptionRecord getException(String payload, String sqlString) {
    String serviceName = getValue(payload, SERVICE_NAME);
    ExceptionRecord exceptionRecord = new ExceptionRecord();
    String status = getStatus(payload);

    if (serviceName != null && serviceName.toLowerCase().contains("authentication failure")) {
      exceptionRecord.setExceptionTypeId(LOGIN_FAILED);
      exceptionRecord.setDescription(serviceName);
      exceptionRecord.setSqlString(sqlString);
      return exceptionRecord;
    } else if (statement != null && !status.contains(SUCCESS_STATUS)) {
      exceptionRecord.setDescription(serviceName);
      exceptionRecord.setSqlString(statement);
      exceptionRecord.setExceptionTypeId(SQL_ERROR);
      return exceptionRecord;
    }
    return null;
  }

  @Override
  protected String getLanguage(String payload) {
    if (needSniffer) return COUCHBASE;

    return Accessor.LANGUAGE_FREE_TEXT_STRING;
  }

  @Override
  protected Time getTimestamp(String payload) {
    String value = this.getValue(payload, TIMESTAMP);
    if (value != null) {
      try {
        ZonedDateTime date = ZonedDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
        long millis = date.toInstant().toEpochMilli();
        int minOffset = date.getOffset().getTotalSeconds() / 60;
        return new Time(millis, minOffset, 0);
      } catch (Exception e) {
        logger.error("Time {} is invalid.", value, e);
      }
    }

    return new Time(0, 0, 0);
  }
}
