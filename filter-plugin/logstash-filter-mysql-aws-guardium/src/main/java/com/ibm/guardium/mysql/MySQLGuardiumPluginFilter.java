//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.mysql;

import java.io.File;
import java.text.ParseException;
import java.util.*;

import com.google.gson.*;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

@LogstashPlugin(name = "mysql_guardium_plugin_filter")
public class MySQLGuardiumPluginFilter implements Filter {

    public static final String LOG42_CONF = "log4j2uc.properties";

    static {
        try {
            String uc_etc = System.getenv("UC_ETC");
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            File file = new File(uc_etc + File.separator + LOG42_CONF);
            context.setConfigLocation(file.toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String id;
    public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
    private static Logger log = LogManager.getLogger(MySQLGuardiumPluginFilter.class);

    public MySQLGuardiumPluginFilter(String id, Configuration config, Context context) {
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
            log.info(Constants.EVENT_NOW_MYSQL, e.getData());
            try {

                if(null != e.getData()
                        && null != e.getData().get(Constants.COMMAND) && e.getData().get(Constants.COMMAND).toString()
                        .equals(Constants.COMMAND_UNKNOWN) && null != e.getData().get(Constants.DATABASE_USER_NAME)
                        && e.getData().get(Constants.DATABASE_USER_NAME).toString().equals(Constants.RDS_ADMIN)){
                    continue;
                }

                Record record = Parser.parseRecord(e);

                final GsonBuilder builder = new GsonBuilder();

                builder.serializeNulls();

                final Gson gson = builder.disableHtmlEscaping().create();

                String jsonRecord = gson.toJson(record);

                jsonRecord = StringEscapeUtils.unescapeJson(jsonRecord);

                e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, jsonRecord);

                matchListener.filterMatched(e);

            } catch (ParseException ex) {
                log.error("Given Event Is Not An Instance Of String " + e.getField(Constants.RECORDS));
                ex.printStackTrace();
            }
        }
        return events;
    }

}

