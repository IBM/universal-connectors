/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.icd.postgresql;

import java.util.Collection;
import java.util.Collections;
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

/**
 * ICDPostgresqlGuardiumFilter class is used as a filter plugin, perform filter
 * operations on the data for convert the input events into GUARDIUM object.
 *
 * @className @ICDPostgresqlGuardiumFilter
 *
 */
@LogstashPlugin(name = "icd_postgresql_guardium_filter")
public class ICDPostgresqlGuardiumFilter implements Filter {
	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "icdpostgresql_json_parse_error";
	public static final String LOGSTASH_TAG_SKIP_NOT_ICD = "icdpostgresql_Invalid_Event";
	private String id;
	private Logger log;
	private MultilineEventParser multilineEventParser = new MultilineEventParser();

	public ICDPostgresqlGuardiumFilter(String id, Configuration config, Context context) {
		this.id = id;
	}

	@Override
	public Collection<PluginConfigSpec<?>> configSchema() {
		return Collections.singletonList(SOURCE_CONFIG);
	}

	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		if (log == null) {
			log = LogManager.getLogger(ICDPostgresqlGuardiumFilter.class);
		}
		for (Event event : events) {
			try {
				if(log.isDebugEnabled()){
					log.debug("Event Now: {}", event.getData());
				}
				multilineEventParser.prepareEventForMultiLineLogs(event);
				if (event.getField(ApplicationConstant.EVENT_TYPE) != null
						&& event.getField(ApplicationConstant.EVENT_TYPE).toString().equalsIgnoreCase(ApplicationConstant.PROCESS_EVENT)) {
					Record rec = Parser.parseRecord(event);
					final GsonBuilder builder = new GsonBuilder();
					builder.serializeNulls();
					final Gson gson = builder.disableHtmlEscaping().create();
					event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(rec));
					matchListener.filterMatched(event);
				}
				else {
					log.debug("ICD Postgres filter: Not a valid Postgres Event so skipping it.");
					event.tag(LOGSTASH_TAG_SKIP_NOT_ICD);
				}
			} catch (Exception exception) {
				log.error("Postgres filter: Error parsing event ", exception);
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