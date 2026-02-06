/*
Copyright IBM Corp. 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.DATASOURCE_PLACEHOLDER;

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
 * This class is the main filter plugin for [DATASOURCE_NAME].
 * It receives log events from Logstash, validates them, parses them into
 * Guardium Record format, and returns the processed events.
 * 
 * INSTRUCTIONS:
 * 1. Replace DATASOURCE_PLACEHOLDER with your data source name (e.g., mongodb, postgresql)
 * 2. Replace DataSourceGuardiumFilter with your actual class name (e.g., MongoDbGuardiumFilter)
 * 3. Update the @LogstashPlugin name to match your plugin name
 * 4. Implement the validation logic specific to your data source
 * 5. Update the error tag constants
 */
@LogstashPlugin(name = "DATASOURCE_PLACEHOLDER_guardium_filter")
public class DataSourceGuardiumFilter implements Filter {
	
	// Configuration for the source field (usually "message")
	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	
	// Error tag for JSON parsing errors - customize for your data source
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "DATASOURCE_PLACEHOLDER_json_parse_error";
	
	private String id;
	private static Logger log = LogManager.getLogger(DataSourceGuardiumFilter.class);
	
	/**
	 * Constructor for the filter plugin
	 * 
	 * @param id Unique identifier for this filter instance
	 * @param config Configuration object
	 * @param context Logstash context
	 */
	public DataSourceGuardiumFilter(String id, Configuration config, Context context) {
		this.id = id;
	}
	
	@Override
	public Collection<PluginConfigSpec<?>> configSchema() {
		return Collections.singletonList(SOURCE_CONFIG);
	}

	/**
	 * Main filter method that processes events
	 * 
	 * @param events Collection of events to process
	 * @param matchListener Listener to notify when events match
	 * @return Collection of processed events
	 */
	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		
		for (Event event : events) {
			// Get the message field from the event
			if (event.getField("message") instanceof String && event.getField("message") != null) {
				String messageString = event.getField("message").toString();
				
				try {
					// TODO: Add validation logic specific to your data source
					// Example: Check if message contains specific keywords or patterns
					// if (!isValidDataSourceMessage(messageString)) {
					//     event.tag("DATASOURCE_PLACEHOLDER_skip_not_datasource");
					//     continue;
					// }
					
					// Parse the JSON message if it is in the JSON format
					JsonObject inputJSON = new Gson().fromJson(messageString, JsonObject.class);
					
					// TODO: Add additional validation on the parsed JSON
					// Example: Check for required fields
					// if (!inputJSON.has("required_field")) {
					//     event.tag("DATASOURCE_PLACEHOLDER_missing_required_field");
					//     continue;
					// }
					
					// Parse the record using the Parser class. Pass the JSO object or the message itself
					Record record = Parser.parseRecord(inputJSON);
					
					// Convert the record to JSON and add it to the event
					final Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
					event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
					
					// Notify that this event matched
					matchListener.filterMatched(event);
					
				} catch (Exception exception) {
					log.error("[DATASOURCE_NAME] filter: Error parsing event ", exception);
					event.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
				}
			}
		}
		return events;
	}

	@Override
	public String getId() {
		return this.id;
	}
	
	/**
	 * TODO: Implement validation logic for your data source
	 * This method should check if the message is from your data source
	 * 
	 * @param message The message string to validate
	 * @return true if the message is from your data source, false otherwise
	 */
	private boolean isValidDataSourceMessage(String message) {
		// Example implementation:
		// return message.contains("your_datasource_identifier");
		return true; // Replace with actual validation
	}
}

