/*
© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.apachesolrdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
 * this class is used to convert log events into guardium object as per
 * requirement
 * 
 *
 */
@LogstashPlugin(name = "apache_solr_gcp_connector")
public class ApacheSolrGcpConnector implements Filter {
	private static Logger LOGGER = LogManager.getLogger(ApacheSolrGcpConnector.class);
	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	private String id;
	public static final String LOGSTASH_TAG_SKIP_NOT_SOLR = "_solrguardium_skip_not_solr";
	public static final String LOGSTASH_TAG_SOLR_PARSE_ERROR = "_solr_parse_error";

	@Override
	public Collection<PluginConfigSpec<?>> configSchema() {
		return Collections.singletonList(SOURCE_CONFIG);
	}

	public ApacheSolrGcpConnector(String id, Configuration config, Context context) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}

	/**
	 * this method takes inputs as event and matchlistner and then converts the log
	 * events into JsonObject and calls the parser method for extracting fields from
	 * logs and set the required fields into guardium object as per the requirement.
	 * 
	 * @param Collection<Event>   events
	 * @param filterMatchListener
	 * @return Collection<Event>
	 */
	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		List<Event> skippedEvents = new ArrayList<Event>();
		for (Event e : events) {
			if (isQtpEvent(e)) {
				String messageString = e.getField("message").toString();
				if (!CommonUtils.isJSONValid(messageString)) {
					e.tag(LOGSTASH_TAG_SKIP_NOT_SOLR);
					skippedEvents.add(e);
					continue;
				}
				try {
					JsonObject inputJSON = new Gson().fromJson(messageString, JsonObject.class);
					Record record = Parser.parseQtpRecord(inputJSON);
					final GsonBuilder builder = new GsonBuilder();
					builder.serializeNulls();
					final Gson gson = builder.disableHtmlEscaping().create();
					e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
					matchListener.filterMatched(e);
				} catch (Exception exception) {
					LOGGER.error("ApacheSolrGcpConnector Qtp filter: Error parsing Qtp event " + exception);
					e.tag(LOGSTASH_TAG_SOLR_PARSE_ERROR);
				}
			} else {
				e.tag(LOGSTASH_TAG_SKIP_NOT_SOLR);
				skippedEvents.add(e);
			}
		}
		events.removeAll(skippedEvents);
		return events;
	}

	/**
	 * this method validates QtpPost requests and returns true if it is successfully
	 * validated
	 * 
	 * @param message
	 * @return status
	 */
	public static Boolean validateQtpPost(String message) {
		boolean status = Boolean.FALSE;
		try {
			if (!StringUtils.isEmpty(message) && message.contains(ApplicationConstant.PARAMS)) {
				if (message.split(ApplicationConstant.PARAMS).length == 2) {
					String params = message.split(ApplicationConstant.PARAMS)[1];
					status = params.contains(ApplicationConstant.ADD_EQUAL)
							|| params.contains(ApplicationConstant.DELETE) ? Boolean.TRUE : Boolean.FALSE;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Inside validateQtpPost: Error parsing qtp post event " + e);
		}
		return status;
	}

	// this method validates collection actions and returns
	// true if it gets validated
	public static Boolean validateCoreAction(String message) {
		boolean status = Boolean.FALSE;
		try {
			if (!StringUtils.isEmpty(message) && message.contains(ApplicationConstant.PARAMS)) {
				if (message.split(ApplicationConstant.PARAMS).length == 2) {
					String params = message.split(ApplicationConstant.PARAMS)[1];
					status = CommonUtils.ManageCollection(params) || CommonUtils.ManageCluster(params)
							|| CommonUtils.ManageShard(params) || CommonUtils.ManageReplica(params)
							|| CommonUtils.ManageAlias(params);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Inside validateCoreAction: Error parsing solr event " + e);
		}
		return status;
	}

	/**
	 * this method validates QTP logs events and returns true if it is successfully
	 * validated
	 * 
	 * @param e
	 * @return
	 */

	public boolean isQtpEvent(Event e) {
		if (e.getField("message") instanceof String) {
			Boolean qtp_event = e.getField("message").toString().contains(ApplicationConstant.SOLR_POST_QTP_MARK_STRING)
					&& validateQtpPost(String.valueOf(e.getField("message")))
					|| e.getField("message").toString().contains(ApplicationConstant.SOLR_REQUEST_QTP_MARK_STRING)
					|| (e.getField("message").toString()
							.contains(ApplicationConstant.SOLR_COLLECTION_ERROR_QTP_MARK_STRING)
							&& e.getField("message").toString().contains(ApplicationConstant.ERROR))
					|| e.getField("message").toString().contains(ApplicationConstant.ERROR)
					|| e.getField("message").toString().contains(ApplicationConstant.HTTP_SOLR_CALL)
							&& validateCoreAction(String.valueOf(e.getField("message")));
			return qtp_event;
		}
		return false;
	}
}
