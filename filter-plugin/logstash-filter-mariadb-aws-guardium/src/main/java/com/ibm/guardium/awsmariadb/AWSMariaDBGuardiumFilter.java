/*
 Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.awsmariadb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.guardium.awsmariadb.constant.ApplicationConstant;
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
 * AWSMariaDBGuardiumFilter Class will perform operation on Logstash filter
 * plug-in for the univer***REMOVED***l connector It parses events and mes***REMOVED***ges from the
 * MariaDB audit logs into a Guardium record instance
 *
 * @annotation @LogstashPlugin(name = "awsmariadb_guardium_filter")
 * @className @AWSMariaDBGuardiumFilter
 *
 */

@LogstashPlugin(name = "awsmariadb_guardium_filter")
public class AWSMariaDBGuardiumFilter implements Filter {

	private static Logger log = LogManager.getLogger(AWSMariaDBGuardiumFilter.class);

	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "mes***REMOVED***ge");
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "mariadb_json_parse_error";
	public static final String LOGSTASH_TAG_JSON_PARSE_SKIP = "mariadb_skip_json";
	private String id;

	public AWSMariaDBGuardiumFilter(String id, Configuration config, Context context) {
		this.id = id;
	}

	@Override
	public Collection<PluginConfigSpec<?>> configSchema() {
		return Collections.singletonList(SOURCE_CONFIG);
	}

	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		ArrayList<Event> skippedEvents = new ArrayList<>();
		Record rec = null;
		GsonBuilder builder = null;
		Gson gson = null;
		for (Event event : events) {
			if (isParseableEvent(event)) {
				try {
					rec = ParserHelper.parseRecord(event);
					gson = new GsonBuilder().di***REMOVED***bleHtmlEscaping().serializeNulls().create();
					event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(rec));
					matchListener.filterMatched(event);
				} catch (Exception exception) {
					event.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
					skippedEvents.add(event);
				}
			} else {
				event.tag(LOGSTASH_TAG_JSON_PARSE_SKIP);
				skippedEvents.add(event);
			}
		}
		return events;
	}

	@Override
	public String getId() {
		return this.id;
	}

	/**
	 * isParseableEvent() method will perform operation on events and verifying the
	 * excepted events and return's true if condition's ***REMOVED***tisfied
	 * 
	 *
	 * @param event
	 * @methodName @isParseableEvent
	 * @return true
	 *
	 */
	private boolean isParseableEvent(Event event) {
		if (event.getField(ApplicationConstant.OPERATION_KEY) == null
				|| event.getField(ApplicationConstant.OPERATION_KEY).toString().equals("")
				|| event.getField(ApplicationConstant.OPERATION_KEY).toString().equals("CONNECT")
				|| event.getField(ApplicationConstant.OPERATION_KEY).toString().equals("DISCONNECT"))
			return false;

		if (event.getField(ApplicationConstant.RETCODE_KEY) == null
				|| event.getField(ApplicationConstant.RETCODE_KEY).toString().equals(""))
			return false;

		return true;

	}
}
