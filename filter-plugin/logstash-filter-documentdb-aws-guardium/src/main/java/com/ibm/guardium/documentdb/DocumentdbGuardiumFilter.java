/*
Copyright 2022-2023 IBM Inc. All rights reserved
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.documentdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

/**
 * DocumentDB Guardium Filter plugin for Logstash. Processes DocumentDB audit and profiler logs and
 * converts them to Guardium Record format.
 *
 * <p>This refactored version uses utility classes and constants for better maintainability.
 */
@LogstashPlugin(name = "documentdb_guardium_filter")
public class DocumentdbGuardiumFilter implements Filter {

  public static final PluginConfigSpec<String> SOURCE_CONFIG =
      PluginConfigSpec.stringSetting("source", "message");

  // Reuse Gson instances to avoid creating new ones for every event (Performance Optimization)
  private static final Gson GSON_PARSER = new Gson();
  private static final Gson GSON_SERIALIZER = new GsonBuilder().serializeNulls().create();
  private static final Gson GSON_SERIALIZER_NO_ESCAPE =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

  private static final Logger log = LogManager.getLogger(DocumentdbGuardiumFilter.class);

  private final Parser parser;
  private final String id;
  private final List<Event> skippedEvents = new ArrayList<>();

  public DocumentdbGuardiumFilter(String id, Configuration config, Context context) {
    this.id = id;
    this.parser = new Parser();
  }

  /**
   * Formats JsonSyntaxException message, truncating everything from the troubleshooting link
   * onwards.
   *
   * @param jse The JsonSyntaxException to format
   * @return Formatted exception message
   */
  private static String formatJsonSyntaxException(JsonSyntaxException jse) {
    String message = jse.toString();
    int linkIndex = message.indexOf("See https://github.com/google/gson");
    if (linkIndex != -1) {
      return message.substring(0, linkIndex).trim();
    }
    return message;
  }

  @Override
  public Collection<PluginConfigSpec<?>> configSchema() {
    return Collections.singletonList(SOURCE_CONFIG);
  }

  @Override
  public String getId() {
    return this.id;
  }

  /** Filter event to create Guard record object(s) for each Audit/Profiler event. */
  @Override
  public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
    skippedEvents.clear(); // Clear from previous invocation
    for (Event e : events) {
      processEvent(e, matchListener);
    }
    events.removeAll(skippedEvents);
    return events;
  }

  /**
   * Processes a single event and converts it to a Guardium record.
   *
   * @param e The event to process
   * @param matchListener The filter match listener
   */
  private void processEvent(Event e, FilterMatchListener matchListener) {
    String messageString = EventUtils.getMessageField(e);
    if (messageString == null) {
      return;
    }

    if (!ValidationUtils.isProperlyClosedJson(messageString)) {
      handleInvalidJson(e, messageString, matchListener);
      return;
    }

    if (messageString.contains(Constants.DOCUMENTDB_AUDIT_SIGNAL)) {
      processAuditEvent(e, messageString, matchListener);
    } else if (messageString.contains(Constants.DOCUMENTDB_PROFILER_SIGNAL)) {
      processProfilerEvent(e, messageString, matchListener);
    } else {
    }
  }

  /** Handles invalid JSON by creating an exception record. */
  private void handleInvalidJson(Event e, String messageString, FilterMatchListener matchListener) {
    String errorMsg = Constants.ERROR_JSON_VALIDATION_FAILED;
    Record record = parser.parseRecordException(null, errorMsg, messageString);
    updateEventWithException(
        record, errorMsg, messageString, e, Constants.LOGSTASH_TAG_JSON_DEPTH_ERROR, matchListener);
  }

  /** Processes an audit event. */
  private void processAuditEvent(Event e, String messageString, FilterMatchListener matchListener) {
    try {
      JsonObject inputJSON = GSON_PARSER.fromJson(messageString, JsonObject.class);
      if (shouldSkipAuditEvent(inputJSON)) {
        handleSkippedEvent(e, messageString, matchListener);
        return;
      }
      Record record = parser.parseAuditRecord(inputJSON);
      enrichRecordWithServerInfo(e, record);
      correctIPs(e, record);
      String recordJson = GSON_SERIALIZER.toJson(record);
      e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, recordJson);
      matchListener.filterMatched(e);
    } catch (StackOverflowError soe) {
      handleStackOverflowError(e, messageString, matchListener);
    } catch (OutOfMemoryError oom) {
      handleOutOfMemoryError(e, messageString, matchListener);
    } catch (JsonSyntaxException jse) {
      handleJsonSyntaxError(e, messageString, jse, matchListener, true);
    } catch (Exception exception) {
      handleGenericError(e, messageString, exception, matchListener, true);
    }
  }

  /** Processes a profiler event. */
  private void processProfilerEvent(
      Event e, String messageString, FilterMatchListener matchListener) {
    try {
      if (!StringUtils.containsAnyProfilerKey(messageString)) {
        return;
      }

      JsonObject inputJSON = GSON_PARSER.fromJson(messageString, JsonObject.class);

      if (shouldSkipProfilerEvent(inputJSON)) {
        handleSkippedEvent(e, messageString, matchListener);
        return;
      }

      Record record = parser.parseProfilerRecord(inputJSON);
      enrichRecordWithServerInfo(e, record);
      correctIPs(e, record);

      e.setField(
          GuardConstants.GUARDIUM_RECORD_FIELD_NAME, GSON_SERIALIZER_NO_ESCAPE.toJson(record));

      if (record.getDbName().equals(Constants.UNKNOWN_STRING)) {
        handleMissingDbName(e, messageString, record, matchListener);
        return;
      }

      matchListener.filterMatched(e);

    } catch (StackOverflowError soe) {
      handleStackOverflowError(e, messageString, matchListener);
    } catch (OutOfMemoryError oom) {
      handleOutOfMemoryError(e, messageString, matchListener);
    } catch (JsonSyntaxException jse) {
      handleJsonSyntaxError(e, messageString, jse, matchListener, false);
    } catch (Exception exception) {
      handleGenericError(e, messageString, exception, matchListener, false);
    }
  }

  /** Checks if an audit event should be skipped. */
  private boolean shouldSkipAuditEvent(JsonObject inputJSON) {
    final String atype = inputJSON.get(Constants.FIELD_ATYPE).getAsString();
    // Never skip authCheck events
    if (atype.equals(Constants.AUTH_TYPE_AUTHCHECK)) {
      return false;
    }
    
    final JsonObject param = inputJSON.get(Constants.FIELD_PARAM).getAsJsonObject();

    boolean shouldSkip = (atype.equals(Constants.AUTH_TYPE_AUTHENTICATE)
            && param.get(Constants.FIELD_ERROR).getAsString().equals(Constants.ERROR_CODE_SUCCESS))
        || (param.has(Constants.FIELD_NS) && param.get(Constants.FIELD_NS).getAsString().isEmpty());
    return shouldSkip;
  }

  /** Checks if a profiler event should be skipped. */
  private boolean shouldSkipProfilerEvent(JsonObject inputJSON) {
    return (!inputJSON.has(Constants.FIELD_NS))
        || (inputJSON.has(Constants.FIELD_NS)
            && inputJSON.get(Constants.FIELD_NS).getAsString().isEmpty());
  }

  /** Enriches a record with server hostname information from the event. */
  private void enrichRecordWithServerInfo(Event e, Record record) {
    String serverHostnamePrefix = EventUtils.getServerHostnamePrefix(e);
    if (serverHostnamePrefix != null) {
      record
          .getAccessor()
          .setServerHostName(serverHostnamePrefix + Constants.SERVER_HOSTNAME_SUFFIX);
      String dbName = record.getDbName();
      record.setDbName(
          !dbName.isEmpty() ? serverHostnamePrefix + ":" + dbName : serverHostnamePrefix);
    }
    record.getAccessor().setServiceName(record.getDbName());
  }

  /** Corrects IP addresses in the record based on event data. */
  private void correctIPs(Event e, Record record) {
    SessionLocator sessionLocator = record.getSessionLocator();
    String sessionServerIp = sessionLocator.getServerIp();


    if (ValidationUtils.isDocumentInternalCommandIp(sessionServerIp)) {
      String ip = EventUtils.getValidatedEventServerIp(e);
      if (ip != null) {
        if (Util.isIPv6(ip)) {
          sessionLocator.setServerIpv6(ip);
          sessionLocator.setIpv6(true);
        } else {
          sessionLocator.setServerIp(ip);
          sessionLocator.setIpv6(false);
        }
      } else if (sessionServerIp.equalsIgnoreCase(Constants.DOCUMENT_INTERNAL_API_IP)) {
        sessionLocator.setServerIp(Constants.DEFAULT_IP);
      }
    }

    if (ValidationUtils.isDocumentInternalCommandIp(sessionLocator.getClientIp())) {
      // Store the original client port before updating IP
      int originalClientPort = sessionLocator.getClientPort();
      if (sessionLocator.isIpv6()) {
        sessionLocator.setClientIpv6(sessionLocator.getServerIpv6());
      } else {
        sessionLocator.setClientIp(sessionLocator.getServerIp());

      }
      
      // Restore the client port (important for authCheck events where port is parsed from remote_ip)
      sessionLocator.setClientPort(originalClientPort);
    }

  }

  /** Handles a skipped event. */
  private void handleSkippedEvent(
      Event e, String messageString, FilterMatchListener matchListener) {
    e.tag(Constants.LOGSTASH_TAG_SKIP);
    skippedEvents.add(e);
    String errorMsg = Constants.ERROR_INVALID_AUTHENTICATE_LOG;
    Record record = parser.parseRecordException(null, errorMsg, messageString);
    updateEventWithException(
        record, errorMsg, messageString, e, Constants.LOGSTASH_TAG_SKIP, matchListener);
  }

  /** Handles missing database name in profiler event. */
  private void handleMissingDbName(
      Event e, String messageString, Record record, FilterMatchListener matchListener) {
    e.tag(Constants.LOGSTASH_TAG_SKIP);
    String errorMsg = Constants.ERROR_MISSING_DB_NAME;
    record = parser.parseRecordException(record, errorMsg, messageString);
    updateEventWithException(
        record, errorMsg, messageString, e, Constants.LOGSTASH_TAG_SKIP, matchListener);
  }

  /** Handles StackOverflowError. */
  private void handleStackOverflowError(
      Event e, String messageString, FilterMatchListener matchListener) {
    log.error(
        "DocumentDB filter: JSON nesting too deep (StackOverflow), skipping event {} ",
        EventUtils.logEvent(e));
    String errorMsg = Constants.ERROR_JSON_NESTING_TOO_DEEP;
    Record record = parser.parseRecordException(null, errorMsg, messageString);
    updateEventWithException(
        record, errorMsg, messageString, e, Constants.LOGSTASH_TAG_JSON_DEPTH_ERROR, matchListener);
  }

  /** Handles OutOfMemoryError. */
  private void handleOutOfMemoryError(
      Event e, String messageString, FilterMatchListener matchListener) {
    log.error(
        "DocumentDB filter: Insufficient memory to process event, skipping {} ",
        EventUtils.logEvent(e));
    String errorMsg = Constants.ERROR_INSUFFICIENT_MEMORY;
    Record record = parser.parseRecordException(null, errorMsg, messageString);
    updateEventWithException(
        record, errorMsg, messageString, e, Constants.LOGSTASH_TAG_JSON_PARSE_ERROR, matchListener);
    System.gc(); // Suggest garbage collection
  }

  /** Handles JsonSyntaxException. */
  private void handleJsonSyntaxError(
      Event e,
      String messageString,
      JsonSyntaxException jse,
      FilterMatchListener matchListener,
      boolean isAudit) {
    String eventType = isAudit ? "audit" : "profiler";
    log.error(
        "DocumentDB filter: Error parsing docDb {} event {} \n {} ",
        eventType,
        EventUtils.logEvent(e),
        formatJsonSyntaxException(jse));
    String errorMsg =
        isAudit ? Constants.ERROR_PARSING_AUDIT_EVENT : Constants.ERROR_PARSING_PROFILER_EVENT;
    // Append exception message to error description
    String exceptionMsg = formatJsonSyntaxException(jse);
    if (exceptionMsg != null && !exceptionMsg.isEmpty()) {
      errorMsg = errorMsg + " - " + exceptionMsg;
    }
    Record record = parser.parseRecordException(null, errorMsg, messageString);
    updateEventWithException(
        record, errorMsg, messageString, e, Constants.LOGSTASH_TAG_JSON_PARSE_ERROR, matchListener);
  }

  /** Handles generic exceptions. */
  private void handleGenericError(
      Event e,
      String messageString,
      Exception exception,
      FilterMatchListener matchListener,
      boolean isAudit) {
    String eventType = isAudit ? "audit" : "profiler";
    log.error(
        "DocumentDB filter: Error parsing docDb {} event {} ",
        eventType,
        EventUtils.logEvent(e),
        exception);
    String errorMsg =
        isAudit ? Constants.ERROR_PARSING_AUDIT_EVENT : Constants.ERROR_PARSING_PROFILER_EVENT;
    // Append exception message to error description
    String exceptionMsg = exception.getMessage();
    if (exceptionMsg != null && !exceptionMsg.isEmpty()) {
      errorMsg = errorMsg + " - " + exceptionMsg;
    }
    Record record = parser.parseRecordException(null, errorMsg, messageString);
    updateEventWithException(
        record, errorMsg, messageString, e, Constants.LOGSTASH_TAG_JSON_PARSE_ERROR, matchListener);
  }

  /** Updates an event with exception information. */
  private void updateEventWithException(
      Record record,
      String errorMsg,
      String eventLog,
      Event e,
      String tag,
      FilterMatchListener matchListener) {
    record = parser.parseRecordException(record, errorMsg, eventLog);
    e.tag(tag);
    e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, GSON_SERIALIZER.toJson(record));
    matchListener.filterMatched(e);
  }
}
