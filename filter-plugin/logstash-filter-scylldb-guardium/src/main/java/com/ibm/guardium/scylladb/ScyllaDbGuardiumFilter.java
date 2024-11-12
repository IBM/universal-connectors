/*
Copyright IBM Corp. 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.scylladb;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;
import com.google.gson.*;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.UCRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;

/**
 * This class is used to convert log events into guardium object as per
 * requirement
 */

@LogstashPlugin(name = "scylladb_guardium_filter")
public class ScyllaDbGuardiumFilter implements Filter {

	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "scylladb_json_parse_error";
	private String id;
	private Logger log = LogManager.getLogger(ScyllaDbGuardiumFilter.class);

	public ScyllaDbGuardiumFilter(String id, Configuration config, Context context) {
		this.id = id;
	}

	@Override
	public Collection<PluginConfigSpec<?>> configSchema() {
		return Collections.singletonList(SOURCE_CONFIG);
	}

	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		for (Event event : events) {
			if (log.isDebugEnabled()) {
				log.debug("Event now {}:",event.getData());		
			}
			try {
				UCRecord rec = Parser.parseRecord(event);
				final GsonBuilder builder = new GsonBuilder();
				builder.serializeNulls();
				final Gson gson = builder.disableHtmlEscaping().create();
				event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(rec));
				matchListener.filterMatched(event);
			} 
			catch (Exception exception) {
				log.error("Scylladb filter: Error parsing event ", exception);
				log.error("Event that caused exception: {}",event.getData());
				event.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
			}
		}
		return events;
	}

	@Override
	public String getId() {
		return this.id;
	}

}
