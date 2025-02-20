/*
Copyright IBM Corp. 2021, 2025 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.bigquery;

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

@LogstashPlugin(name = "big_query_guardium_filter")
public class BigQueryGuardiumFilter implements Filter {

	private static Logger logger = LogManager.getLogger(BigQueryGuardiumFilter.class);

	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");

	private String id;
	private String sourceField;

	/**
	 * BigQueryFilterConnector() method used to set SOURCE_CONFIG and context for
	 * Events
	 * 
	 * @param config
	 * @param context
	 * @methodName SpannerFilterConnector
	 * @return
	 */
	public BigQueryGuardiumFilter(String id, Configuration config, Context context) {
		this.id = id;
		this.sourceField = config.get(SOURCE_CONFIG);
	}

	/**
	 * configSchema() method will get the iD
	 * 
	 * @methodName @configSchema
	 * @return Collection<PluginConfigSpec<?>>
	 */
	@Override
	public Collection<PluginConfigSpec<?>> configSchema() {
		return Collections.singletonList(SOURCE_CONFIG);
	}

	/**
	 * getId() method will get the iD
	 * 
	 * @methodName @getId
	 * @return String
	 */
	@Override
	public String getId() {
		return this.id;
	}

	/**
	 * filter() method will perform operation on input, get data and some
	 * validations performed to check the input parameter and then call method which
	 * convert the input as a JSON and and convert json object into Map for Guardium
	 * Object and return response as per the requirement.
	 * 
	 * @param Collection<Event>   events
	 * @param filterMatchListener
	 * @methodName @filter
	 * @return Collection<Event>
	 */
	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener filterMatchListener) {
		ArrayList<Event> skippedEvents = new ArrayList<>();
		for (Event e : events) {
			if(logger.isDebugEnabled()){
				logger.debug("Event Now: {}", e.getData());
			}
			if (e.getField(ApplicationConstants.MESSAGE) instanceof String
					&& String.valueOf(e.getField(ApplicationConstants.MESSAGE))
							.contains(ApplicationConstants.MESSAGE_CONTAINS)) {
				String messageString = e.getField(ApplicationConstants.MESSAGE).toString();
				if (!CommonUtils.isJSONValid(messageString)) {
					e.tag(ApplicationConstants.LOGSTASH_TAG_SKIP_NOT_GCP);
					skippedEvents.add(e);
					continue;
				}
				try {
					JsonObject inputJSON = new Gson().fromJson(messageString, JsonObject.class);
					Record record = Parser.parseRecord(inputJSON);
					final Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
					e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
					filterMatchListener.filterMatched(e);
				} catch (Exception ex) {
					logger.error("found exception in parsing json", ex);
					logger.error("Event that caused exception: {}", e.getData());
					e.tag(ApplicationConstants.LOGSTASH_TAG_JSON_PARSE_ERROR);
				}
			} else {
				e.tag(ApplicationConstants.LOGSTASH_TAG_SKIP_NOT_GCP);
			}
		}
		events.removeAll(skippedEvents);
		return events;

	}
}