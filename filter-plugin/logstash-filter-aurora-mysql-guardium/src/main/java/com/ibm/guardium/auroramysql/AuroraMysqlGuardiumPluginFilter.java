package com.ibm.guardium.auroramysql;

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
@LogstashPlugin(name = "auroramysqlguardiumpluginfilter")
public class AuroraMysqlGuardiumPluginFilter implements Filter {

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
	private static Logger logger = LogManager.getLogger(AuroraMysqlGuardiumPluginFilter.class);

	public AuroraMysqlGuardiumPluginFilter(String id, Configuration config, Context context) {
		// constructors should validate configuration options
		this.id = id;
	}

	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {

		for (Event e : events) {

			if(logger.isDebugEnabled()){
				logger.debug("Event now {}:",e.getData());
			}
			if (e.getField("message") instanceof String && e.getField("message") != null) {
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
					logger.error("AWS_AURORA_MYSQL filter: Error parsing MYSQL event " + exception);
					logger.error("Event that caused exception: {}",e.getData());
					e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
				}

			} else {
				logger.error("AWS_AURORA_MYSQL filter: Event has been skipped: " + e.getField("message"));
				e.tag("_guardium_skip_not_AWS_AURORA_MYSQ");
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
		if (e.getField(Constants.SESSION_ID) != null) {
			data.addProperty(Constants.SESSION_ID, e.getField(Constants.SESSION_ID).toString());
		}
		if (e.getField(Constants.ACTION_STATUS) != null) {
			data.addProperty(Constants.ACTION_STATUS, e.getField(Constants.ACTION_STATUS).toString());
		}
		if (e.getField(Constants.EXEC_STATEMENT) != null) {
			data.addProperty(Constants.EXEC_STATEMENT, e.getField(Constants.EXEC_STATEMENT).toString());
		}
		if (e.getField(Constants.AUDIT_ACTION) != null) {
			data.addProperty(Constants.AUDIT_ACTION, e.getField(Constants.AUDIT_ACTION).toString());
		}
		if (e.getField(Constants.DB_USER) != null) {
			data.addProperty(Constants.DB_USER, e.getField(Constants.DB_USER).toString());
		}
		if (e.getField(Constants.DB_NAME) != null) {
			data.addProperty(Constants.DB_NAME, e.getField(Constants.DB_NAME).toString());
		}
		if (e.getField(Constants.SERVER_INSTANCE) != null) {
			data.addProperty(Constants.SERVER_INSTANCE, e.getField(Constants.SERVER_INSTANCE).toString());
		}
		if (e.getField(Constants.SERVERHOSTNAME) != null) {
			data.addProperty(Constants.SERVERHOSTNAME, e.getField(Constants.SERVERHOSTNAME).toString());
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
