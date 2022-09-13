/*

© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0

*/
package com.ibm.guardium.firestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.GuardConstants;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Record;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

/**
 * FireStoreGuardiumFilter class is used as a filter plugin, perform filter
 * operations on the data for convert the input events into GUARDIUM object.
 *
 * @className @FireStoreGuardiumFilter
 * 
 */
@LogstashPlugin(name = "fire_store_guardium_filter")
public class FireStoreGuardiumFilter implements Filter {

	private static Logger logger = LogManager.getLogger(FireStoreGuardiumFilter.class);

	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source",
			ApplicationConstants.MESSAGE);

	public static final String LOGSTASH_TAG_SKIP_NOT_FIRESTORE = "_firestoreguardium_skip_not_firestore";
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_firestoreguardium_json_parse_error";

	private String id;
	private String sourceField;

	/**
	 * FireStoreGuardiumFilter() method used to set Config and context for Events
	 * 
	 * @param config
	 * @param context
	 */
	public FireStoreGuardiumFilter(String id, Configuration config, Context context) {
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
					.toString().contains(ApplicationConstants.FIRE_STORE_SERVICE)) {
				String mes***REMOVED***geString = e.getField(ApplicationConstants.MESSAGE).toString();
				if (!CommonUtils.isJSONValid(mes***REMOVED***geString)) {
					logger.info("Invalid JSON");
					e.tag(LOGSTASH_TAG_SKIP_NOT_FIRESTORE);
					skippedEvents.add(e);
					continue;
				}
				try {
					JsonObject inputJSON = new Gson().fromJson(mes***REMOVED***geString, JsonObject.class);
					Record record = Parser.parseRecord(inputJSON);
					final GsonBuilder builder = new GsonBuilder();
					builder.serializeNulls();
					final Gson gson = builder.di***REMOVED***bleHtmlEscaping().create();
					e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
					filterMatchListener.filterMatched(e);
				} catch (Exception ex) {
					logger.error("unable to parse the JSON, exception: ", ex);
					e.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
				}
			} else {
				e.tag(LOGSTASH_TAG_SKIP_NOT_FIRESTORE);
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