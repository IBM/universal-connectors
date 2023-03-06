package com.infoinsightsllc.guardium;

import java.io.IOException;
import java.util.Map;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.*;
import co.elastic.logstash.api.Event;

public class Parser {
    public static final String DATA_PROTOCOL_STRING = "Snowflake native audit";
    public static final String UNKOWN_STRING = "";
    public static final String SERVER_TYPE_STRING = "SNOWFLAKE";
    public static final String EXCEPTION_TYPE_AUTHORIZATION_STRING = "SQL_ERROR";
    public static final String EXCEPTION_TYPE_AUTHENTICATION_STRING = "LOGIN_FAILED";


    public Parser() {
	}


	/**
     * Parses a Snowflake event sent via JDBC input plugin query.
     * 
     * @param event A logstash event containing Snowflake key/value pairs
     * @return
     */
    public Record parseRecord(final Event event) throws IOException{
        MappableGuardiumRecord rec = new MappableGuardiumRecord();
        if(event == null){
            return rec.getGuardRecord();
        }

        Map<String, Object> eventMap = event.toMap();

        for (Map.Entry<String, Object> entry : eventMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            rec.setMappedField(key, value);
        }

        return rec.getGuardRecord();
    }

}
