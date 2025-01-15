/*
*Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.neptune.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.query.MalformedQueryException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.neptune.connector.constant.ApplicationConstants;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

@LogstashPlugin(name = "neptune_guardium_filter")
public class NeptuneGuardiumFilter implements Filter {
	private static Logger log;

	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec
			.stringSetting(ApplicationConstants.SOURCE, ApplicationConstants.MESSAGE);
	private static final String NEPTUNE_PARSE_FAILURE_TAG = " neptune-parse-failure_error ";
	private static final String LOGSTASH_SKIP_NOT_NEPTUNE = " neptune_skip_not_neptune ";

	private String id;
	private String sourceField;

	public NeptuneGuardiumFilter(String id, Configuration config, Context context) {
		this.id = id;
		this.sourceField = config.get(SOURCE_CONFIG);
	}

	/**
	 * This method is invoked by the logstash,where the custom logic can be applied
	 * to process the data.
	 */
	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		ArrayList<Event> skippedEvents = new ArrayList<>();
		if (log == null) {
			log = LogManager.getLogger(NeptuneGuardiumFilter.class);
		}
		Record record = null;
		for (Event event : events) {
			if(log.isDebugEnabled()){
				log.debug("Event Now: {}", event.getField(ApplicationConstants.MESSAGE));
			}
//			log.error("Message received => " + event.getField(ApplicationConstants.MESSAGE));
			if (event.getField(ApplicationConstants.MESSAGE).toString()
					.contains(ApplicationConstants.QUERY_TYPE_GREMLIN)
					|| event.getField(ApplicationConstants.MESSAGE).toString()
							.contains(ApplicationConstants.QUERY_TYPE_SPARQL)) {

				record = new Record();
				try {

					if (event.getField(ApplicationConstants.MESSAGE) instanceof String) {
						record = ParserHelper.parseRecord(event, record);
						final GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
						builder.serializeNulls();
						final Gson gson = builder.create();
						event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
//						log.error("Guard record => " + gson.toJson(record));
						matchListener.filterMatched(event);
					}

				} catch (Exception e) {

					if (e instanceof ScriptException) {
						log.error("Invalid Query, Unable to parse: "
								+ event.getField(ApplicationConstants.PAYLOAD).toString());
					} else if (e instanceof MalformedQueryException) {
						log.error("Invalid Query, Unable to parse: " + ParserHelper
								.decodeContent(event.getField(ApplicationConstants.PAYLOAD).toString()
										.split(ApplicationConstants.EQUAL, 2)[1])
								.replaceAll("\n|\r", ApplicationConstants.SPACE));
					} else {
						log.error(e.getMessage());
					}

					event.tag(NEPTUNE_PARSE_FAILURE_TAG);

					/*
					 * we can afford skipping the failing event from further processing by Logstash
					 * pipeline because in case of Exception event should not reach to Guardium. The
					 * details of message are logged for investigation.
					 * 
					 */
					skippedEvents.add(event);
				}
			} else {
//				log.error("Not a NeptuneDB Log");
				event.tag(LOGSTASH_SKIP_NOT_NEPTUNE);
				skippedEvents.add(event);
			}
		}
		events.removeAll(skippedEvents);
		return events;
	}

	@Override
	public Collection<PluginConfigSpec<?>> configSchema() {

		return Collections.singletonList(SOURCE_CONFIG);
	}

	@Override
	public String getId() {
		return this.id;
	}

	public String getSourceField() {
		return sourceField;
	}

}
