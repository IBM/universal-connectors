/*
Copyright IBM Corp. 2021, 2022, 2023 All rights reserved.

SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.spanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

/**
 * SpannerFilterConnector class is used as a filter plugin, perform filter
 * operations on the data for convert the input events into GUARDIUM object.
 */
@LogstashPlugin(name = "spanner_db_guardium_filter")
public class SpannerDBGuardiumFilter implements Filter {

	private static Logger logger = LogManager.getLogger(SpannerDBGuardiumFilter.class);

	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source",
			ApplicationConstants.MESSAGE);

	public static final String LOGSTASH_TAG_SKIP_NOT_SPANNER = "_spannerguardium_skip_not_spannerdb";
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_spannerguardium_json_parse_error";

	private String id;
	private String sourceField;

	/**
	 * SpannerFilterConnector() constructor used to set Config and context for
	 * Events
	 * 
	 * @param config
	 * @param context
	 * @return
	 */
	public SpannerDBGuardiumFilter(String id, Configuration config, Context context) {
		this.id = id;
		this.sourceField = config.get(SOURCE_CONFIG);
	}

	/**
	 * filter() method will perform operation on input, get data and some
	 * validations performed to check the input parameter and then call method which
	 * convert the input as a JSON and and convert json object into Map for Guardium
	 * Object and return response as per the requirement.
	 * 
	 * @param Collection<Event>   events
	 * @param filterMatchListener
	 * @return Collection<Event>
	 */
	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener filterMatchListener) {
		ArrayList<Event> skippedEvents = new ArrayList<>();
		for (Event e : events) {
			if (e.getField(ApplicationConstants.MESSAGE) instanceof String && e.getField(ApplicationConstants.MESSAGE)
					.toString().contains(ApplicationConstants.SPANNER_SERVICE)) {
				String messageString = e.getField(ApplicationConstants.MESSAGE).toString();
				if (!CommonUtils.isJSONValid(messageString)) {
					e.tag(LOGSTASH_TAG_SKIP_NOT_SPANNER);
					skippedEvents.add(e);
					continue;
				}
				try {
					JsonObject inputJSON = new Gson().fromJson(messageString, JsonObject.class);
					Record record = Parser.parseRecord(inputJSON);
					final GsonBuilder builder = new GsonBuilder();
					builder.serializeNulls();
					final Gson gson = builder.disableHtmlEscaping().create();
					e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
					filterMatchListener.filterMatched(e);
				} catch (Exception ex) {
					logger.error("found exception in parsing json", ex);
					e.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
				}
			} else {
				e.tag(LOGSTASH_TAG_SKIP_NOT_SPANNER);
			}
		}
		try {
			events.removeAll(skippedEvents);
		} catch (Exception ex) {
			logger.error("exception while removing skipped events: " + ex);
		}
		return events;
	}

	/**
	 * configSchema() method will get the singleton list of source config
	 * 
	 * @return Collection<PluginConfigSpec<?>>
	 */
	@Override
	public Collection<PluginConfigSpec<?>> configSchema() {
		return Collections.singletonList(SOURCE_CONFIG);
	}

	/**
	 * getId() method will get the iD
	 * 
	 * @return String
	 */
	@Override
	public String getId() {
		return this.id;
	}

}
