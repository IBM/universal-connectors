//
// Copyright 2021-2024 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.dynamodb;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.io.StringReader;

//class name must match plugin name
@LogstashPlugin(name = "dynamodb_guardium_plugin_filter")
public class DynamodbGuardiumPluginFilter implements Filter{

    public static final String LOG42_CONF = "log4j2uc.properties";
    static {
        try {
            String uc_etc = System.getenv("UC_ETC");
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            File file = new File(uc_etc + File.separator + LOG42_CONF);
            context.setConfigLocation(file.toURI());
        } catch (Exception e) {
            System.err.println("Failed to load log4j configuration " + e.getMessage());
            e.printStackTrace();
        }
    }

	private String id;
    public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
    private static Logger log = LogManager.getLogger(DynamodbGuardiumPluginFilter.class);

    public DynamodbGuardiumPluginFilter(String id, Configuration config, Context context) {
        // constructors should validate configuration options
        this.id = id;
    }

	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		//New Collection<Events> is created so as to avoid overwriting in case of Collection<Events>
		Collection<Event> eventsUpdated = new ArrayList<>();
		for (Event e : events) {
			//If the input plugin used is sqs, the Events received are in the form of ArrayList
			if (e.getField("message") instanceof String && e.getField("message").toString().startsWith("[")) {
				try {
					if(log.isDebugEnabled()){
						log.debug("Event Now: {}", e.getData());
					}
					String input = e.getField("message").toString();
					String trimmed = input.trim();

					//Need to extract each message from the ArrayList and fill in the logArray
					JsonReader reader = new JsonReader(new StringReader(trimmed));
					JsonElement rootElement = JsonParser.parseReader(reader);
					JsonArray logArray = rootElement.getAsJsonArray();

					//Here we iterate over each message i.e., Event to fetch the required Fields
					for (int i = 0; i < logArray.size(); i++) {
						// Extract the i-th JSON object
						JsonObject inputJSON = logArray.get(i).getAsJsonObject();
						Event newEvent = e.clone();
						newEvent.remove("message");
						inputJSON.addProperty(Constants.ACCOUNT_ID, e.getField("account_id").toString());

						//Skip the Event if the EventSource is not dynamodb.amazonaws.com
						if (!inputJSON.get("eventSource").getAsString().equals("dynamodb.amazonaws.com"))
							continue;
						try {
							String guardRec = convertEventToRecord(inputJSON);
							newEvent.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, guardRec);
							eventsUpdated.add(newEvent);
						}
						catch (ParseException pexc){
							log.error("Dynamodb filter: Error parsing dynamo event " + pexc);
							log.error("Event that caused exception: {}" +inputJSON);
							e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
						}
					}
					matchListener.filterMatched(e);
				} catch (Exception exc) {
					log.error("Dynamodb filter: Error parsing dynamo event " + exc);
					e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
				}
			}
			//If the input plugin used is cloudwatch, single Event is received
			else if (e.getField("message") instanceof String && !e.getField("message").toString().startsWith("[")) {
				String input = e.getField("message").toString();
				try {
					if(log.isDebugEnabled()){
						log.debug("Event Now: {}", e.getData());
					}
					JsonObject inputJSON = (JsonObject) JsonParser.parseString(input);
					inputJSON.addProperty(Constants.ACCOUNT_ID, e.getField("account_id").toString());
					String guardRec = convertEventToRecord(inputJSON);
					e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, guardRec);
					eventsUpdated.add(e);
					matchListener.filterMatched(e);
				} catch (Exception exception) {
					log.error("Dynamodb filter: Error parsing dynamo event " + exception);
					log.error("Event that caused exception: {}", e.getData());
					e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
				}
			} else {
				log.error("Dynamodb filter: Not a Dynamo Event so skipping it.");
				e.tag(Constants.LOGSTASH_TAG_SKIP_NOT_DYNAMO);
			}
		}
		return eventsUpdated;

	}

	private String convertEventToRecord(JsonObject inputJSON) throws ParseException {
		Record record = Parser.parseRecord(inputJSON);
		final GsonBuilder builder = new GsonBuilder();
		builder.serializeNulls();
		final Gson gson = builder.create();
		return gson.toJson(record);
	}

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        // should return a list of all configuration options for this plugin
        return Collections.singletonList(SOURCE_CONFIG);
    }

    @Override
    public String getId() {
        return this.id;
    }
}
