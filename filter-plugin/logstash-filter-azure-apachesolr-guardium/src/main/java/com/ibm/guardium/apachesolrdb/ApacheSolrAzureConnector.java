/*
  Copyright IBM Corp. 2021, 2022 All rights reserved.
  SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.guardium.apachesolrdb;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
 * This class is used to convert log events into guardium object as per
 * requirement
 */
@LogstashPlugin(name = "apache_solr_azure_connector")
public class ApacheSolrAzureConnector implements Filter {
	private static Logger log = LogManager.getLogger(ApacheSolrAzureConnector.class);
	private String id;
	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "mes***REMOVED***ge");
	public static final String LOGSTASH_TAG_SKIP_NOT_SOLR = "_solrguardium_skip_not_solr";
	public static final String LOGSTASH_TAG_SOLR_PARSE_ERROR = "_solr_parse_error";

	public ApacheSolrAzureConnector(String id, Configuration config, Context context) {
		this.id = id;
	}

	@Override
	public Collection<PluginConfigSpec<?>> configSchema() {
		return Collections.singletonList(SOURCE_CONFIG);
	}

	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		List<Event> skippedEvents = new ArrayList<Event>();
		for (Event event : events) {
			if (isQtpEvent(event)) {
				try {
					Record rec = Parser.parseQtpRecord(event);
					final GsonBuilder builder = new GsonBuilder();
					builder.serializeNulls();
					final Gson gson = builder.di***REMOVED***bleHtmlEscaping().create();
					event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(rec));
					matchListener.filterMatched(event);
				} catch (Exception exception) {
					log.error("Apachesolrqtp filter: Error parsing event ", exception);
					event.tag(LOGSTASH_TAG_SOLR_PARSE_ERROR);
				}
			} else {
				event.tag(LOGSTASH_TAG_SKIP_NOT_SOLR);
				skippedEvents.add(event);
			}
		}
		events.removeAll(skippedEvents);
		return events;
	}

	// this method validates QtP log events and return true if it gets validated
	public boolean isQtpEvent(Event event) {
		if (event.getField("mes***REMOVED***ge") instanceof String) {
			Boolean qtp_event = (event.getField("mes***REMOVED***ge").toString().contains(ApplicationConstant.SOLR_QTP_LOG_UPDATE)
					&& validateQtpPost(event))
					|| (event.getField("mes***REMOVED***ge").toString().contains(ApplicationConstant.SOLR_QTP_REQUEST)
							&& event.getField("mes***REMOVED***ge").toString().contains(ApplicationConstant.STATUS_CHECK))
					|| event.getField("mes***REMOVED***ge").toString().contains(ApplicationConstant.ERROR)
					|| (event.getField("mes***REMOVED***ge").toString().contains(ApplicationConstant.SOLR_QTP_OVERSEER_STRING)
							&& event.getField("mes***REMOVED***ge").toString().contains(ApplicationConstant.ERROR))
					|| (event.getField("mes***REMOVED***ge").toString().contains(ApplicationConstant.HTTP_SOLR_CALL)
							&& event.getField("mes***REMOVED***ge").toString().contains(ApplicationConstant.STATUS_CHECK)
							&& validateCoreAction(event));
			return qtp_event;
		}
		return false;
	}

	/**
	 * this method validates QtpPost requests and returns true if it is successfully
	 * validated
	 * 
	 * @param event
	 * @return status
	 */
	public static Boolean validateQtpPost(Event event) {
		boolean status = Boolean.FALSE;
		status = event.getField(ApplicationConstant.QUERY_STRING).toString().contains(ApplicationConstant.ADD)
				|| event.getField(ApplicationConstant.QUERY_STRING).toString().contains(ApplicationConstant.DELETE);
		return status;

	}

	/**
	 * this method validates core creation, insertion, deletion and returns true if
	 * it is successfully validated
	 * 
	 * @param event
	 * @return status
	 */
	public static Boolean validateCoreAction(Event event) {
		boolean status = Boolean.FALSE;
		String params = ApplicationConstant.UNKNOWN_STRING;
		if (event.getField(ApplicationConstant.QUERY_STRING) != null) {
			params = event.getField(ApplicationConstant.QUERY_STRING).toString();
		}
		status = CommonUtils.ManageCollection(params) || CommonUtils.ManageCluster(params)
				|| CommonUtils.ManageShard(params) || CommonUtils.ManageReplica(params)
				|| CommonUtils.ManageAlias(params);
		return status;
	}

	@Override
	public String getId() {
		return this.id;
	}

}
