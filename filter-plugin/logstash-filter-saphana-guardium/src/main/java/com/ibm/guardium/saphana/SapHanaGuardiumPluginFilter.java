package com.ibm.guardium.saphana;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;
import com.google.gson.*;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.util.*;

//class name must match plugin name
@LogstashPlugin(name = "saphana_guardium_plugin_filter")
public class SapHanaGuardiumPluginFilter implements Filter {

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
	private static Logger log = LogManager.getLogger(SapHanaGuardiumPluginFilter.class);

	public SapHanaGuardiumPluginFilter(String id, Configuration config, Context context) {
		// constructors should validate configuration options
		this.id = id;
	}

	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		Set<String> setOfFingerprintID = new HashSet<>();

		for (Event e : events) {
			if (e.getField(Constants.EXEC_STATEMENT) instanceof String || e.getField(Constants.ACTION_STATUS) instanceof String) {

				if (setOfFingerprintID.contains(e.getField(Constants.FINGERPRINT))) {
					e.tag("_guardium_skip_duplicate_records");
				} else {
					setOfFingerprintID.add(e.getField("fingerprint").toString());
					JsonObject data = new JsonObject();
					data = inputData(e);
					try {
						Record record = Parser.parseRecord(data);
						final GsonBuilder builder = new GsonBuilder();
						builder.serializeNulls();
						final Gson gson = builder.create();
						e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));

						matchListener.filterMatched(e);
					} catch (Exception exception) {
						log.error("SAPHANA filter: Error parsing saphana event " + exception);

						if(log.isDebugEnabled()){
							log.debug("Event Now: ",data);
							exception.printStackTrace();
						}
						e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
					}
				}
			} else {
				log.error("SAPHANA filter: Event has been skipped: " + e.getField("message")+ " originalSQL"+e.getField(Constants.EXEC_STATEMENT));
				e.tag("_guardium_skip_not_saphana");
			}
		}

		return events;
	}

	private JsonObject inputData(Event e) {

		JsonObject data = new JsonObject();

		if (e.getField(Constants.CLIENT_IP) != null) {
			data.addProperty(Constants.CLIENT_IP, e.getField(Constants.CLIENT_IP).toString());
		}
		if (e.getField(Constants.TIMESTAMP) != null) {
			data.addProperty(Constants.TIMESTAMP, e.getField(Constants.TIMESTAMP).toString());
		}
		if (e.getField(Constants.APP_USER) != null) {
			data.addProperty(Constants.APP_USER, e.getField(Constants.APP_USER).toString());
		}
		if (e.getField(Constants.SESSION_ID) != null) {
			data.addProperty(Constants.SESSION_ID, e.getField(Constants.SESSION_ID).toString());
		}
		if (e.getField(Constants.ACTION_STATUS) != null) {
			data.addProperty(Constants.ACTION_STATUS, e.getField(Constants.ACTION_STATUS).toString());
		}
		if (e.getField(Constants.EXEC_STATEMENT) != null) {
			data.addProperty(Constants.EXEC_STATEMENT, e.getField(Constants.EXEC_STATEMENT).toString());
		}
		if (e.getField(Constants.CLIENT_PORT) != null) {
			data.addProperty(Constants.CLIENT_PORT, e.getField(Constants.CLIENT_PORT).toString());
		}
		if (e.getField(Constants.AUDIT_ACTION) != null) {
			data.addProperty(Constants.AUDIT_ACTION, e.getField(Constants.AUDIT_ACTION).toString());
		}
		if (e.getField(Constants.SERVICE_NAME) != null) {
			data.addProperty(Constants.SERVICE_NAME, e.getField(Constants.SERVICE_NAME).toString());
		}
		if (e.getField(Constants.SERVER_HOST) != null) {
			data.addProperty(Constants.SERVER_HOST, e.getField(Constants.SERVER_HOST).toString());
		}
		
		if (e.getField(Constants.SOURCE_PROGRAM) != null) {
			data.addProperty(Constants.SOURCE_PROGRAM, e.getField(Constants.SOURCE_PROGRAM).toString());
		}
		if (e.getField(Constants.SERVER_PORT) != null) {
			data.addProperty(Constants.SERVER_PORT, e.getField(Constants.SERVER_PORT).toString());
		}
		if (e.getField(Constants.DB_USER) != null) {
			data.addProperty(Constants.DB_USER, e.getField(Constants.DB_USER).toString());
		}
		if (e.getField(Constants.SCHEMA_NAME) != null) {
			data.addProperty(Constants.SCHEMA_NAME, e.getField(Constants.SCHEMA_NAME).toString());
		}
		if (e.getField(Constants.SERVER_IP) != null) {
			data.addProperty(Constants.SERVER_IP, e.getField(Constants.SERVER_IP).toString());
		}
		if (e.getField(Constants.CLIENT_HOST) != null) {
			data.addProperty(Constants.CLIENT_HOST, e.getField(Constants.CLIENT_HOST).toString());
		}
		if (e.getField(Constants.OFFSET) != null) {
			data.addProperty(Constants.OFFSET, e.getField(Constants.OFFSET).toString());
		}

		return data;
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