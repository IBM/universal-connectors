package com.ibm.guardium.redshift;

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

@LogstashPlugin(name = "redshift_guardium_connector")
public class RedShiftGuardiumConnector implements Filter {
	private static Logger log = LogManager.getLogger(RedShiftGuardiumConnector.class);
	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	private String id;
	private String sourceField;

	public RedShiftGuardiumConnector(String id, Configuration config, Context context) {
		this.id = id;
	}

	@Override
	public Collection<PluginConfigSpec<?>> configSchema() {
		return Collections.singletonList(SOURCE_CONFIG);
	}

	@Override
	public String getId() {
		return this.id;
	}

	public String getSourceField() {
		return sourceField;
	}

	/**
	 * This filter method invoked by the Logstash execution engine.
	 *
	 * @param events , matchListener
	 * @return events
	 */
	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		ArrayList<Event> skippedEvents = new ArrayList<>();
		Parser parser = new Parser();

		for (Event e : events) {
			if (log.isDebugEnabled()) {
				log.debug("Event Now: {}", e.getData());
			}
			if ((e.getField(RedShiftTags.SQLQUERY) != null && e.getField(RedShiftTags.U_IDENTIFIER) != null
					&& e.getField(RedShiftTags.SQLQUERY).toString() instanceof String
					&& e.getField(RedShiftTags.SQLQUERY).toString().contains("LOG")
					&& !(e.getField(RedShiftTags.U_IDENTIFIER).toString().equalsIgnoreCase("RDSDB")))
					|| (e.getField(RedShiftTags.USER_NAME) != null
					&& !e.getField(RedShiftTags.USER_NAME).toString().equalsIgnoreCase("RDSDB")
					&& e.getField(RedShiftTags.STATUS).toString().equalsIgnoreCase("authentication failure"))) {
				try {

					Record record = parser.parseRecord(e);
					final GsonBuilder builder = new GsonBuilder();
					builder.serializeNulls();
					final Gson gson = builder.create();
					e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
					matchListener.filterMatched(e);
				} catch (Exception ex) {
					log.error("Error Occured while parsing the audit log : " + ex);
					e.tag(RedShiftTags.LOGSTASH_TAG_JSON_PARSE_ERROR);
				}
			} else {
				skippedEvents.add(e);
			}
		}
		events.removeAll(skippedEvents);
		return events;
	}
}
