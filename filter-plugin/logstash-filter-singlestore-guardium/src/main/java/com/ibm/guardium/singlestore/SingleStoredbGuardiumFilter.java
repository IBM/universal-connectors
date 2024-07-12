//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.singlestore;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
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

@LogstashPlugin(name = "singlestoredb_guardium_filter")
public class SingleStoredbGuardiumFilter implements Filter {

	public static final String LOG42_CONF = "log4j2uc.properties";
	static {
		try {
			String uc_etc = System.getenv("UC_ETC");
			LoggerContext context = (LoggerContext) LogManager.getContext(false);
			File file = new File(uc_etc + File.separator + LOG42_CONF);
			context.setConfigLocation(file.toURI());
		} catch (Exception e) {
			System.out.println("====: "+e.getMessage());
		}
	}

	final private String id;
	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	final private static Logger log = LogManager.getLogger(SingleStoredbGuardiumFilter.class);

	public SingleStoredbGuardiumFilter(String id, Configuration config, Context context) {
		this.id = id;
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

	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {

		for (Event e : events) {
			if(log.isDebugEnabled()){
				log.debug("Event Now: {}", e.getData());
			}
			try {
				Record record;

				final GsonBuilder builder = new GsonBuilder();
				final Gson gson = builder.create();
				builder.serializeNulls();

				getParsedEvent(e.getField("message").toString(),e);

                JsonObject inputData = inputData(e);
                if(isFailedLogin(inputData)){

                    record = Parser.parseExceptionRecord(inputData);

                }else{

                    record = Parser.parseRecord(inputData);
                }
                e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
                matchListener.filterMatched(e);
				log.info("==========>Final JSON to be send to Guardium: {}", gson.toJson(record));

				System.out.println("==========>Final JSON to be send to Guardium: "+gson.toJson(record));
			} catch (Exception exception) {
				log.error("SingleStore filter: Error parsing SingleStore event {}", e.getField(Constants.MESSAGE).toString(), exception);
				e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
			}
		}
		return events;
	}

	private boolean isFailedLogin(JsonObject inputData) {
        return inputData.get("message").getAsString().split(",")[5].equals("USER_LOGIN") && inputData.get("message").getAsString().split(",")[11].equals("FAILURE: Access denied");
	}

	private JsonObject inputData(Event e) {
		JsonObject data = new JsonObject();

		if(e.getField(Constants.TIMESTAMP).toString() != null && !e.getField(Constants.TIMESTAMP).toString().isEmpty()){
			String datestring=e.getField(Constants.TIMESTAMP).toString();
			datestring = datestring.replace("UTC", "Z");
			data.addProperty(Constants.TIMESTAMP, datestring);
		}
		if(e.getField(Constants.CLIENT_IP).toString() != null && !e.getField(Constants.CLIENT_IP).toString().isEmpty()){
			data.addProperty(Constants.CLIENT_IP, e.getField(Constants.CLIENT_IP).toString());
		}
		if(e.getField(Constants.SERVER_IP).toString() != null && !e.getField(Constants.SERVER_IP).toString().isEmpty()){
			data.addProperty(Constants.SERVER_IP, e.getField(Constants.SERVER_IP).toString());
		}
		if(e.getField(Constants.SERVER_HOSTNAME).toString() != null && !e.getField(Constants.SERVER_HOSTNAME).toString().isEmpty()){
			data.addProperty(Constants.SERVER_HOSTNAME, e.getField(Constants.SERVER_HOSTNAME).toString());
		}
		if(e.getField(Constants.SERVER_PORT).toString() != null && !e.getField(Constants.SERVER_PORT).toString().isEmpty()){
			data.addProperty(Constants.SERVER_PORT, e.getField(Constants.SERVER_PORT).toString());
		}
		if(e.getField(Constants.DB_USER).toString() != null && !e.getField(Constants.DB_USER).toString().isEmpty()){
			data.addProperty(Constants.DB_USER, e.getField(Constants.DB_USER).toString());
		}
		if(e.getField(Constants.DB_NAME).toString() != null && !e.getField(Constants.DB_NAME).toString().isEmpty()){
			data.addProperty(Constants.DB_NAME, e.getField(Constants.DB_NAME).toString());
		}
		if(e.getField(Constants.QUERY_STATEMENT).toString() != null && !e.getField(Constants.QUERY_STATEMENT).toString().isEmpty()){
			data.addProperty(Constants.QUERY_STATEMENT, e.getField(Constants.QUERY_STATEMENT).toString());
		}
		if(e.getField(Constants.MESSAGE).toString() != null && !e.getField(Constants.MESSAGE).toString().isEmpty()){
			data.addProperty(Constants.MESSAGE, e.getField(Constants.MESSAGE).toString());
		}
		return data;
	}

	public static void getParsedEvent(String logEvent,Event event) {
		try {

			String[] values=logEvent.split(",");

			String server_port=values[3].split(":")[1];

			event.setField("message", logEvent);
			event.setField(Constants.DB_NAME, values[8]);
			event.setField(Constants.CLIENT_IP, Constants.CLIENT_IP_VALUE);
			if(values[5].equals("USER_LOGIN")){
				event.setField(Constants.CLIENT_IP, values[8]);
				event.setField(Constants.DB_NAME, "");
			}

			if(event.getData().containsKey("serverIP")){
				event.setField(Constants.SERVER_IP, event.getField("serverIP").toString());
			}

			if(event.getData().containsKey("serverHostname")){
				event.setField(Constants.SERVER_HOSTNAME,event.getField("serverHostname").toString());
			}

			event.setField(Constants.SERVER_PORT,server_port);
			event.setField(Constants.TIMESTAMP, values[1]+values[2]);
			event.setField(Constants.DB_USER, values[7]);

			if(values.length>12){
				for (int i=12;i<values.length;i++){
					values[11]=values[11]+","+values[i];
				}

			}
			values[11]=values[11].replace("\\","");
			event.setField(Constants.QUERY_STATEMENT, values[11]);
		}
		catch (Exception e){
            log.error("getParsedEvent Function {}", e.getMessage());
		}

	}


}