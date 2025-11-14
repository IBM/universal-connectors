/*
 * Copyright © 2025 Software GmbH, Darmstadt, Germany and/or its licensors
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.softwareag.adabas.auditing.logstash;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

// class name must match plugin name
@LogstashPlugin(name = "adabas_guardium_filter")
public class AdabasGuardiumFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(AdabasGuardiumFilter.class);

    public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source",
            "adabas-auditing");

    private final String id;
    private final String sourceField;
    private final Parser parser;

    public AdabasGuardiumFilter(final String id, final Configuration config, final Context context) {
        this.id = id;
        this.sourceField = config.get(SOURCE_CONFIG);
        this.parser = new Parser();
        logger.debug("Adabas Auditing Filter created with id: {} and source field: {}", id, sourceField);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Event> filter(final Collection<Event> events, final FilterMatchListener matchListener) {
        for (final Event e : events) {
            final Object f = e.getField(sourceField);
            if (f instanceof HashMap<?, ?>) {
                final HashMap<String, Object> map = (HashMap<String, Object>) f;
                logger.debug("Event map: {}", map);

                Record record = parser.parseRecord(map);

                if (record.getAccessor() != null) {
                    final GsonBuilder builder = new GsonBuilder();
                    builder.serializeNulls();
                    final Gson gson = builder.create();
                    e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
                    e.setField("type", sourceField);
                    matchListener.filterMatched(e);
                } else {
                    e.tag(Constants.LOGSTASH_TAG_SKIP_NOT_COMMAND);
                }
            }
        }
        return events;
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
}
