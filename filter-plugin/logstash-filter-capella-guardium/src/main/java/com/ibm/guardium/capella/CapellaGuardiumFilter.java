/*
Copyright IBM Corp. 2021, 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.capella;

import co.elastic.logstash.api.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.custom_parsing.ParserFactory;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.ibm.guardium.capella.Constants.*;

@LogstashPlugin(name = "capella_guardium_filter")
public class CapellaGuardiumFilter implements Filter {

  public static final PluginConfigSpec<String> SOURCE_CONFIG =
      PluginConfigSpec.stringSetting("source", "message");
  private static Logger logger = LogManager.getLogger(CapellaGuardiumFilter.class);

  public static final String LOG42_CONF = "log4j2uc.properties";

  static {
    try {
      String uc_etc = System.getenv("UC_ETC");
      LoggerContext context = (LoggerContext) LogManager.getContext(false);
      File file = new File(uc_etc + File.separator + LOG42_CONF);
      context.setConfigLocation(file.toURI());
    } catch (Exception e) {
      System.err.println("Failed to load log4j configuration " + e.getMessage());
      e.printStackTrace();
    }
  }

  private String id;
  private String filter;

  public CapellaGuardiumFilter(String id, String filter, Configuration config, Context context) {
    this(id, config, context);
    this.filter = filter;
  }

  public CapellaGuardiumFilter(String id, Configuration config, Context context) {
    this.id = id;
  }

  @Override
  public Collection<PluginConfigSpec<?>> configSchema() {
    return Collections.singletonList(SOURCE_CONFIG);
  }

  /**
   * Returns the id
   *
   * @return id
   */
  @Override
  public String getId() {
    return this.id;
  }

  /**
   * Filters the received events by skipping the invalid ones and normalizing them by parsing the
   * provided payloads into Guardium Generic Records.
   *
   * @param events A list of received events
   * @param filterMatchListener The listener for this plugin
   * @return A list of normalized events
   */
  public Collection<Event> filter(
      Collection<Event> events, FilterMatchListener filterMatchListener) {

    Parser parser = new Parser(ParserFactory.ParserType.json);
    ;

    ArrayList<Event> skippedEvents = new ArrayList<>();
    final Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    for (Event e : events) {
      if (logger.isDebugEnabled()) {
        logger.debug("Event Now: {}", e.getData());
      }
      if (!(e.getField(MESSAGE) instanceof String)
          || (filter != null && !String.valueOf(e.getField(MESSAGE)).contains(filter))) {
        e.tag(INVALID_MSG);
      } else {

        Record record = parser.parseRecord(e.getField(MESSAGE).toString());
        String recordStr = gson.toJson(record);
        if (recordStr == null || recordStr.isEmpty()) {
          continue;
        }
        e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, recordStr);
        filterMatchListener.filterMatched(e);
      }
    }
    events.removeAll(skippedEvents);
    return events;
  }
}
