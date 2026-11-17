/*
*Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.neptune.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private static final int MAX_LOGGED_PAYLOAD_LENGTH = 2000;
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
		for (Event event : events) {
			try {
				Object message = event.getField(ApplicationConstants.MESSAGE);
				if (log.isDebugEnabled()) {
					log.debug("Event Now: {}", message);
				}
				if (!(message instanceof String)) {
					throw new IllegalArgumentException("Event has no message field or it is not a string");
				}
				String messageText = (String) message;
				if (messageText.contains(ApplicationConstants.QUERY_TYPE_GREMLIN)
						|| messageText.contains(ApplicationConstants.QUERY_TYPE_SPARQL)) {

					Record record = ParserHelper.parseRecord(event, new Record());
					final GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
					builder.serializeNulls();
					final Gson gson = builder.create();
					event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
					matchListener.filterMatched(event);
				} else {
					event.tag(LOGSTASH_SKIP_NOT_NEPTUNE);
					skippedEvents.add(event);
				}
			} catch (Exception | StackOverflowError e) {
				/*
				 * A malformed event must never stop the Logstash pipeline. Log the reason and
				 * the payload for investigation, tag the event and skip it, so it does not
				 * reach Guardium.
				 */
				logSkippedEvent(event, e);
				try {
					event.tag(NEPTUNE_PARSE_FAILURE_TAG);
				} catch (Exception ignored) {
					// tagging is best effort, the event is skipped anyway
				}
				skippedEvents.add(event);
			}
		}
		events.removeAll(skippedEvents);
		return events;
	}

	/**
	 * Logs why an event is skipped. This method must never throw, as it runs while
	 * handling a failure.
	 */
	private static void logSkippedEvent(Event event, Throwable cause) {
		try {
			Object payload = event.getField(ApplicationConstants.PAYLOAD);
			String payloadText = payload == null ? "<no payload>"
					: ParserHelper.decodeContent(payload.toString()).replaceAll("\n|\r", ApplicationConstants.SPACE);
			if (payloadText.length() > MAX_LOGGED_PAYLOAD_LENGTH) {
				payloadText = payloadText.substring(0, MAX_LOGGED_PAYLOAD_LENGTH) + "...";
			}
			String reason = String.valueOf(cause.getMessage()).replaceAll("\n|\r", ApplicationConstants.SPACE);
			log.error("Skipping malformed Neptune event, Unable to parse. Reason: {}: {}. Payload: {}",
					cause.getClass().getSimpleName(), reason, payloadText);
		} catch (Exception | StackOverflowError e) {
			log.error("Skipping malformed Neptune event, Unable to parse. Reason: {}", String.valueOf(cause));
		}
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
