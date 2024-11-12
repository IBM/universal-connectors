/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.azure.cosmos;

import java.util.Collection;
import java.util.Collections;

import com.ibm.guardium.universalconnector.commons.structures.UCRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.GuardConstants;

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
@LogstashPlugin(name = "azure_cosmos_guardium_filter")
public class AzureCosmosGuardiumFilter implements Filter {
	
	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "cosmos_json_parse_error";
	
	private String id;
	private static Logger log=LogManager.getLogger(AzureCosmosGuardiumFilter.class);;
	
	public AzureCosmosGuardiumFilter(String id, Configuration config, Context context) {
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
			   String messageString = event.getField("message").toString();
				try {
					JsonObject inputJSON = new Gson().fromJson(messageString, JsonObject.class);
					UCRecord record = Parser.parseRecord(inputJSON);
					final Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
					event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
					matchListener.filterMatched(event);
										
				} catch (Exception exception) {
					log.error("Azure filter: Error parsing event ", exception);
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


}
