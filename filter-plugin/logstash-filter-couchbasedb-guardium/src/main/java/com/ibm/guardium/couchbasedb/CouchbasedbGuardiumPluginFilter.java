package com.ibm.guardium.couchbasedb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.GuardConstants;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Record;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

//class name must match plugin name
@LogstashPlugin(name = "couchbasedb_guardium_plugin_filter")
public class CouchbasedbGuardiumPluginFilter implements Filter {

	public static final String LOG42_CONF = "log4j2uc.properties";
	static {
		try {
			String uc_etc = System.getenv("UC_ETC");
			LoggerContext context = (LoggerContext) LogManager.getContext(false);
			File file = new File(uc_etc + File.separator + LOG42_CONF);
			context.setConfigLocation(file.toURI());
		} catch (Exception e) {
			System.err.println("Failed to load log4j configuration " + e.getMes***REMOVED***ge());
			e.printStackTrace();
		}
	}

	private String id;
	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "mes***REMOVED***ge");
	private static Logger log = LogManager.getLogger(CouchbasedbGuardiumPluginFilter.class);

	public CouchbasedbGuardiumPluginFilter(String id, Configuration config, Context context) {
		// constructors should validate configuration options
		this.id = id;
	}

	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		ArrayList<Event> skippedEvents = new ArrayList<>();
		for (Event e : events) {
			if (e.getField(Constants.MESSAGE) instanceof String) {
				String input = e.getField(Constants.MESSAGE).toString();
				try {
					JsonObject inputJSON = (JsonObject) JsonParser.parseString(input);

					Boolean isUIGeneratedQuery = Parser.checkForUIGeneratedQueries(inputJSON);
					if (!isUIGeneratedQuery) {
						if(e.getField(Constants.SERVER_IP) instanceof String) {
							inputJSON.addProperty(Constants.SERVER_IP, e.getField(Constants.SERVER_IP).toString());
						}
						if(e.getField(Constants.SERVER_HOSTNAME) instanceof String) {
							inputJSON.addProperty(Constants.SERVER_HOSTNAME, e.getField(Constants.SERVER_HOSTNAME).toString());
						}
						Record record = Parser.parseRecord(inputJSON);
						final GsonBuilder builder = new GsonBuilder();
						builder.serializeNulls();
						final Gson gson = builder.create();
						e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
					} else {
						log.debug("Couchbasedb Event With RequestID {} Generated By UI Has Been Skipped",
								inputJSON.get(Constants.REQUEST_ID));
						e.tag(Constants.LOGSTASH_TAG_SKIP);
						skippedEvents.add(e);

					}
					matchListener.filterMatched(e);
				} catch (Exception exception) {
					log.error("Couchbasedb Filter: Error Parsing Couchbase Event " + exception);
					e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
				}
			} else {
				log.error("Given Event Is Not An Instance Of String " + e.getField(Constants.MESSAGE));
				e.tag(Constants.LOGSTASH_TAG_SKIP_NOT_COUCHBASE_DB);
			}
		}
		events.removeAll(skippedEvents);
		return events;
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