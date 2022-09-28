/*
 Copyright IBM Corp. 2021, 2022 All rights reserved.

SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.couchdb;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.guardium.couchdb.CouchdbGuardiumFilter;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

@LogstashPlugin(name = "couchdb_guardium_filter")
public class CouchdbGuardiumFilter implements Filter {
	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "couchdb_json_parse_error";
	public static final String LOGSTASH_TAG_JSON_PARSE_SKIP = "couchdb_skip_json";
	private final static String COUCHDB_SIGNAL = "couchdb";
	private String id;
	private static Logger log;


	public CouchdbGuardiumFilter(String id, Configuration config, Context context) {
		// constructors should validate configuration options
		this.id = id;
	}

	@Override
	public Collection<PluginConfigSpec<?>> configSchema() {
		return Collections.singletonList(SOURCE_CONFIG);
	}

	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		if (log == null) {
			// instantiate default logger, as other Context can not coexsit alongside
			// Guardium Universal Connector Logger context.
			log = LogManager.getLogger(CouchdbGuardiumFilter.class);
		}
		ArrayList<Event> skippedEvents = new ArrayList<>();
		for (Event event : events) {
			if(event.getField("application") !=null && event.getField("application").toString().contains(COUCHDB_SIGNAL))
			{
				if (isParseableEvent(event)) 
				{
					try {
						Record rec = Parser.parseRecord(event);
						final GsonBuilder builder = new GsonBuilder();
						builder.serializeNulls();
						final Gson gson = builder.disableHtmlEscaping().create();
						event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(rec));
						matchListener.filterMatched(event);
					} catch (Exception exception) {
						log.error("Exception occurred while parsing the audit log:  ", event);
						event.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
					}
				} 
				else {
					skippedEvents.add(event);
				} 
			}
		}
		events.removeAll(skippedEvents);
		return events;
	}



	@Override
	public String getId() {
		return this.id;
	}

	/**
	 * isParseableEvent() method will perform operation on events and verifying the
	 * excepted events and return's true if condition's satisfied
	 * 
	 *
	 * @param event
	 * @methodName @isParseableEvent
	 * @return true
	 *
	 */
	private boolean isParseableEvent(Event event) {

		if (event.getField(ApplicationConstant.DB_NAME) == null		
				||(event.getField(ApplicationConstant.DB_NAME).toString().equals("_session") && (event.getField(ApplicationConstant.STATUS)!=null && event.getField(ApplicationConstant.STATUS).equals("200")))
				|| event.getField(ApplicationConstant.DB_NAME).toString().equals("_uuids")
				|| event.getField(ApplicationConstant.DB_NAME).toString().equals("_utils")
				|| event.getField(ApplicationConstant.DB_NAME).toString().equals("verifytestdb")
				|| event.getField(ApplicationConstant.DB_NAME).toString().equals("verifytestdb_replicate")
				|| event.getField(ApplicationConstant.DB_NAME).toString().equals("_membership")
				|| event.getField(ApplicationConstant.DB_NAME).toString().equals("/_cluster_setup")
				|| event.getField(ApplicationConstant.DB_NAME).toString().equals("_node")
				|| event.getField(ApplicationConstant.DB_NAME).toString().equals("_scheduler")
				|| event.getField(ApplicationConstant.DB_NAME).toString().equals("favicon.ico")
				|| event.getField(ApplicationConstant.DB_NAME).toString().equals("_config")
				|| event.getField(ApplicationConstant.DB_NAME).toString().equals("_active_tasks"))

			return false;

		return true;

	}

}
