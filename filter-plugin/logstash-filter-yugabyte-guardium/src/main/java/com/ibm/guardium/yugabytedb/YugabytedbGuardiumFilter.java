//
// Copyright 2021-2022 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.yugabytedb;

import co.elastic.logstash.api.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.UCRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.text.ParseException;
import java.util.*;

@LogstashPlugin(name = "yugabytedb_guardium_filter")
public class YugabytedbGuardiumFilter implements Filter {

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
    public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("message");
    private static Logger log = LogManager.getLogger(YugabytedbGuardiumFilter.class);

    public YugabytedbGuardiumFilter(String id, Configuration config, Context context) {
        this.id = id;
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        // should return a list of all configuration options for this plugin
        return Collections.singletonList(SOURCE_CONFIG);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {

        for (Event e : events) {
            try {
                UCRecord record = Parser.parseRecord(e.toMap());
                final GsonBuilder builder = new GsonBuilder();
                builder.serializeNulls();
                final Gson gson = builder.create();
                e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
                matchListener.filterMatched(e);

            } catch (ParseException pe) {
                log.error("Yugabyte filter: Error parsing yugabyte event " + e.getField(Constants.MESSAGE).toString(), pe);
                e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
            } catch (Exception exception) {
                log.error("Yugabyte filter: Error parsing yugabyte event " + e.getField(Constants.MESSAGE).toString(),
                        exception);
                e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
            }
        }
        return events;
    }
}
