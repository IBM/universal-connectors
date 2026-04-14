//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.generic;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;
import com.google.gson.*;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.util.*;

// class name must match plugin name
@LogstashPlugin(name = "generic_guardium_filter")
public class GenericGuardiumFilter implements Filter {
	
	public static final String LOG42_CONF = "log4j2uc.properties";
	static {
		try {
			String uc_etc = System.getenv("UC_ETC");
			LoggerContext context = (LoggerContext) LogManager.getContext(false);
			File file = new File(uc_etc + File.separator + LOG42_CONF);
			context.setConfigLocation(file.toURI());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private static Logger log = LogManager.getLogger("GenericGuardiumFilter.class");

    public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");

    private String id;
    
    public GenericGuardiumFilter(String id, Configuration config, Context context) {
        // constructors should validate configuration options
        this.id = id;
    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
        
    	for (Event e : events) {
    		//if (e.getField(Constants.MESSAGE) instanceof String) {
    			try {
    				JsonObject inputData = inputData(e);
    				//log.error("Actual message "+inputData.getAsString());
    				
    				Record record = Parser.parseRecord(inputData);
            		final GsonBuilder builder = new GsonBuilder();
	            	builder.serializeNulls();
	            	final Gson gson = builder.create();
	            	e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));	
	            	//log.error("GuardRec "+e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	            	
    			} catch (Exception exception) {
    				log.error("Generic Filter: Error parsing event " + e.getField(Constants.MESSAGE).toString(),
    						exception);
    				e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
    			}
    		//}
    		/*else {
				log.error("Given Event Is Not An Instance Of String "+e.getField(Constants.MESSAGE) );
				e.tag(Constants.LOGSTASH_TAG_SKIP_NOT_RDB);
				}*/
	        }
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
    
    private JsonObject prepareData(Event e, ArrayList<String> fieldArray)
    {
		JsonObject data = new JsonObject();

    	for(String fieldName : fieldArray){
    	if(e.includes(fieldName)) {
			if (e.getField(fieldName).toString() != null
					&& !e.getField(fieldName).toString().isEmpty()) {
				data.addProperty(fieldName, e.getField(fieldName).toString());
			}
			else data.addProperty(fieldName, "");
		}
    	}
		return data;
    	
    }
    private JsonObject inputData(Event e) {
		JsonObject data = new JsonObject();
		ArrayList<String> inputConstants = new ArrayList<>();
		inputConstants.add(Constants.DBNAME);
		inputConstants.add(Constants.ORIGINALSQLCOMMAND);
		inputConstants.add(Constants.TIMESTAMP);
		inputConstants.add(Constants.APPUSERNAME);
		inputConstants.add(Constants.SESSIONID);
		inputConstants.add(Constants.SERVERTYPE);
		inputConstants.add(Constants.COMMPROTO);
		inputConstants.add(Constants.SOURCEPROGRAM);
		inputConstants.add(Constants.OSUSER);
		inputConstants.add(Constants.SERVERHOSTNAME);
		inputConstants.add(Constants.CLIENTMAC);
		inputConstants.add(Constants.SERVEROS);
		inputConstants.add(Constants.SERVICENAME);
		inputConstants.add(Constants.DBPROTOCOL);
		inputConstants.add(Constants.CLIENTHOSTNAME);
		inputConstants.add(Constants.DBUSER);
		inputConstants.add(Constants.CLIENTOS);
		inputConstants.add(Constants.DATATYPE);
		inputConstants.add(Constants.SERVERDESC);
		inputConstants.add(Constants.LANGUAGE);
		inputConstants.add(Constants.DBPROTOCOLVERSION);
		inputConstants.add(Constants.SQLSTATE);
		inputConstants.add(Constants.DESCRIPTION);
		inputConstants.add(Constants.SQLSTRING);
		inputConstants.add(Constants.SERVERPORT);
		inputConstants.add(Constants.CLIENTPORT);
		inputConstants.add(Constants.SERVERIP);
		inputConstants.add(Constants.CLIENTIP);
		inputConstants.add(Constants.ISIPV6);
		inputConstants.add(Constants.CLIENTIPV6);
		inputConstants.add(Constants.SERVERIPV6);
		data = prepareData(e, inputConstants);		
		return data;
	}

}
