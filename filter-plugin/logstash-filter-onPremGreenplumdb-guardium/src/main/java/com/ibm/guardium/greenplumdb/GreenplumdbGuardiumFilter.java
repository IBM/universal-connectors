/*
Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.greenplumdb;

import java.util.ArrayList;
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
 * 
 * @author shivam_agarwal
 *
 */
@LogstashPlugin(name = "greenplumdb_guardium_filter")
public class GreenplumdbGuardiumFilter implements Filter {

	private static Logger LOGGER;
	private static final String GREENPLUMDB_PARSE_FAILURE_TAG = "greenplumdb-parse-failure_error";
	private static final String LOGSTASH_SKIP_NOT_GREENPLUMDB = "logstash_skip_not_greenplumdb";
	private String id;
	private String sourceField;

	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec
			.stringSetting(ApplicationConstant.SOURCE, ApplicationConstant.MESSAGE);

	/**
	 * 
	 * @param id
	 * @param config
	 * @param context
	 */
	public GreenplumdbGuardiumFilter(String id, Configuration config, Context context) {
		this.id = id;
		this.sourceField = config.get(SOURCE_CONFIG);
	}

	@Override
	public Collection<PluginConfigSpec<?>> configSchema() {

		return Collections.singletonList(SOURCE_CONFIG);
	}

	@Override
	public String getId() {
		return this.id;
	}

	/**
	 * 
	 * @return
	 */
	public String getSourceField() {
		return sourceField;
	}

	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {

		ArrayList<Event> skippedEvents = new ArrayList<>();
		if (LOGGER == null) {
			LOGGER = LogManager.getLogger(GreenplumdbGuardiumFilter.class);
		}
		Record record = null;
		for (Event event : events) {
			if (event.getField(ApplicationConstant.MESSAGE) != null && (event.getField(ApplicationConstant.MESSAGE)
					.toString().contains(ApplicationConstant.POSTGRES)
					|| event.getField(ApplicationConstant.MESSAGE).toString().contains(ApplicationConstant.ELOG)
					|| event.getField(ApplicationConstant.MESSAGE).toString().contains(ApplicationConstant.AUTH))) {

				try {
					if (isParse(event)) {
						record = Parser.parseRecord(event);
						final GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
						builder.serializeNulls();
						final Gson gson = builder.create();
						event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
						matchListener.filterMatched(event);
					} else {

						skippedEvents.add(event);
					}

				} catch (Exception e) {

					LOGGER.error(e.getMessage());

					event.tag(GREENPLUMDB_PARSE_FAILURE_TAG);

					/*
					 * We are taking care of the events which are specific to greenplumdb using
					 * input plugin wherein we are defining the "type" as "greenplumdb" and in the
					 * filter this value is checked again to filter the greenplumdb specific events.
					 * beat { type => "greenplumdb" We are checking this type in Filter plugin to
					 * filter the events. if [type] == "greenplumdb"{}
					 */
					skippedEvents.add(event);
				}
			} else {
				LOGGER.error("Not a GreenplumDB Log");
				event.tag(LOGSTASH_SKIP_NOT_GREENPLUMDB);
				skippedEvents.add(event);
			}
		}
		events.removeAll(skippedEvents);
		return events;

	}

	private boolean isParse(Event event) {

		final String trnsactionID = event.getField(ApplicationConstant.TRANSACTION_ID) != null
				? event.getField(ApplicationConstant.TRANSACTION_ID).toString()
				: ApplicationConstant.EMPTY;
		final String sliceID = event.getField(ApplicationConstant.SLICE_ID) != null
				? event.getField(ApplicationConstant.SLICE_ID).toString()
				: ApplicationConstant.EMPTY;
		final String fileName = event.getField(ApplicationConstant.FILE_NAME) != null
				? event.getField(ApplicationConstant.FILE_NAME).toString()
				: ApplicationConstant.EMPTY;
		boolean flag = false;

		if (trnsactionID.equals("0") && !sliceID.isEmpty()) {
			flag = sliceID.equals(ApplicationConstant.SLICE) ? true : false;

		} else if (trnsactionID.equals("0") || fileName.equals(ApplicationConstant.ELOG)
				|| fileName.equals(ApplicationConstant.AUTH)) {
			flag = true;
		}
		return flag;
	}

}