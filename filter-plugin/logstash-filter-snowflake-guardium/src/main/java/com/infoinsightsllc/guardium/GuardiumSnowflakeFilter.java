package com.infoinsightsllc.guardium;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.*;


// class name must match plugin name
@LogstashPlugin(name = "guardium_snowflake_filter")
public class GuardiumSnowflakeFilter implements Filter {

    private static Logger log = LogManager.getLogger(GuardiumSnowflakeFilter.class);
    public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_snowflakeguardium_json_parse_error";
    public static final PluginConfigSpec<String> SOURCE_CONFIG =
            PluginConfigSpec.stringSetting("source", "message");
    
    public static final String LOGSTASH_TAG_SKIP_NOT_SNOWFLAKE = "_not_snowflake_or_malformed";

    private String id;

    public GuardiumSnowflakeFilter(String id, Configuration config, Context context) {
        this.id = id;
    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
        final GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        final Gson gson = builder.create();

        Parser parser = new Parser();
        ArrayList<Event> skippedEvents = new ArrayList<>();
        for (Event event : events) {
            try {
                Record rec = parser.parseRecord(event);
                if(rec.getAccessor().getDbUser() == null || rec.getAccessor().getDbUser().equals("")){
                    event.tag(LOGSTASH_TAG_SKIP_NOT_SNOWFLAKE);
                    skippedEvents.add(event);
                }
                else{
                    event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(rec));
                }
            } catch (Exception exception) {
                log.error("Snowflake filter: Error parsing event ", exception);
                event.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
            }
        }
        events.removeAll(skippedEvents);
        return events;
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return Collections.singletonList(SOURCE_CONFIG);
    }

    @Override
    public String getId() {
        return this.id;
    }
}
