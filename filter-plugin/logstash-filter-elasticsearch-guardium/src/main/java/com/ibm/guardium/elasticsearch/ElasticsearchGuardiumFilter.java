/*
Copyright IBM Corp. 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.elasticsearch;

import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import com.google.gson.JsonObject;
import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

/**
 * This class is used to convert log events into guardium object as per
 * requirement
 */

@LogstashPlugin(name = "elasticsearch_guardium_filter")
public class ElasticsearchGuardiumFilter implements Filter {

	private static Logger log = LogManager.getLogger(ElasticsearchGuardiumFilter.class);

	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "elasticsearch_json_parse_error";
	public static final String LOGSTASH_TAG_JSON_PARSE_SKIP = "elasticsearch_skip_json";
	private String id;
	
	public ElasticsearchGuardiumFilter(String id, Configuration config, Context context) {
		this.id = id;
	}

	@Override
	public Collection<PluginConfigSpec<?>> configSchema() {
		return Collections.singletonList(SOURCE_CONFIG);
	}

	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		for (Event event : events) {
			if (event.getField("message") instanceof String && event.getField("message") != null) {
				String messageValue = event.getField("message").toString();
				try {
					JsonObject inputJSON = new Gson().fromJson(messageValue, JsonObject.class);
					Record record = Parser.parseRecord(inputJSON);
					Time time=record.getTime();
					long t = (time.getTimstamp()) - (Long.parseLong(event.getField("totalOffset").toString()) * 60000);
					time.setTimstamp(t);
					record.setTime(time);
					final Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
					event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
					matchListener.filterMatched(event);
				} catch (Exception exception) {
					log.error("Elasticsearch filter: Error parsing event ", exception);
					event.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
				}
			} else {
				event.tag(LOGSTASH_TAG_JSON_PARSE_SKIP);
			}

		}
		return events;
	}

	@Override
	public String getId() {
		return this.id;
	}

}
