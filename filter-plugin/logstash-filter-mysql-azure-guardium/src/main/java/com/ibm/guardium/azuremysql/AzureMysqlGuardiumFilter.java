/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.azuremysql;

import co.elastic.logstash.api.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.UCRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;

/**
 * This class is used to convert log events into guardium object as per
 * requirement
 */

@LogstashPlugin(name = "azure_mysql_guardium_filter")
public class AzureMysqlGuardiumFilter implements Filter {

	private static Logger log = LogManager.getLogger(AzureMysqlGuardiumFilter.class);

	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "azure_mysql_json_parse_error";
	private String id;

	public AzureMysqlGuardiumFilter(String id, Configuration config, Context context) {
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
				String messagevalue = event.getField("message").toString();
				try {
					JsonObject inputJSON = new Gson().fromJson(messagevalue, JsonObject.class);
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
