/*
 * Copyright © 2025 Software GmbH, Darmstadt, Germany and/or its licensors
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.softwareag.adabas.auditing.logstash;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.Time;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

// class name must match plugin name
@LogstashPlugin(name = "adabas_auditing_filter")
public class AdabasAuditingFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(AdabasAuditingFilter.class);

    public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source",
            "adabas-auditing");

    public static final String LOGSTASH_TAG_SKIP_NOT_COMMAND = "_adabasguardium_skip_not_command";

    private static final String DATA_PROTOCOL_STRING = "Adabas native audit";
    private static final String SERVER_TYPE_STRING = "Adabas";
    private static final String UNKOWN_STRING = "";

    // hashmap mappings
    private static final String RECORD_SESSION_ID = "CMDID";
    private static final String RECORD_DB_NAME = "UABIDBID";
    private static final String RECORD_TIME = "UABHTIME";
    private static final String RECORD_APP_USER_NAME = "TPUSERID";

    private static final String ACCESSOR_DB_USER = "NATUID";
    private static final String ACCESSOR_SERVER_HOST_NAME = "LPARNAME";
    private static final String ACCESSOR_SOURCE_PROGRAM = "NATPROG";

    private final String id;
    private final String sourceField;

    public AdabasAuditingFilter(final String id, final Configuration config, final Context context) {
        // constructors should validate configuration options
        this.id = id;
        this.sourceField = config.get(SOURCE_CONFIG);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Event> filter(final Collection<Event> events, final FilterMatchListener matchListener) {
        for (final Event e : events) {
            final Object f = e.getField(sourceField);
            // System.out.println("Field: " + f);
            final Record record = new Record();
            Accessor accessor = null;
            Data data = null;
            if (f instanceof HashMap<?, ?>) {
                final HashMap<String, Object> map = (HashMap<String, Object>) f;
                logger.debug("Event map: {}", map);
                if (map.containsKey("UABI_ITEMS")) {
                    // extract data from the map
                    final HashMap<String, Object> items = (HashMap<String, Object>) map.get("UABI_ITEMS");
                    // data for the record
                    if (items.containsKey(RECORD_DB_NAME)) {
                        record.setDbName(items.get(RECORD_DB_NAME).toString());
                    }
                    // extract data from ACBX or CLNT
                    if (items.containsKey("UABD_ITEMS")) {
                        final Object item = items.get("UABD_ITEMS");
                        if (item instanceof ArrayList<?>) {
                            final ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) item;
                            HashMap<String, Object> clnt = null;
                            HashMap<String, Object> acbx = null;
                            HashMap<String, Object> fbuf = null;
                            for (final HashMap<String, Object> itemMap : list) {
                                itemMap.get("UABDTY");
                                final String type = itemMap.get("UABDTY").toString();
                                if (type.equals("ACBX") || type.equals("CLNT") || type.equals("FBUF")) {
                                    if (itemMap.containsKey("PAYLOAD_CLNT")) {
                                        clnt = (HashMap<String, Object>) itemMap.get("PAYLOAD_CLNT");
                                        // cnlt data for the record
                                        if (clnt.containsKey(RECORD_APP_USER_NAME)) {
                                            record.setAppUserName(clnt.get(RECORD_APP_USER_NAME).toString());
                                        }
                                        // fill accessor data
                                        accessor = parseAccessor(clnt);
                                    }
                                    if (itemMap.containsKey("PAYLOAD_ACBX")) {
                                        acbx = (HashMap<String, Object>) itemMap.get("PAYLOAD_ACBX");
                                        // acbx data for the record
                                        if (acbx.containsKey(RECORD_SESSION_ID)) {
                                            record.setSessionId(acbx.get(RECORD_SESSION_ID).toString());
                                        }
                                    }
                                    if (itemMap.containsKey("PAYLOAD_FBUF")) {
                                        fbuf = (HashMap<String, Object>) itemMap.get("PAYLOAD_FBUF");
                                    }
                                }
                            }
                            data = parseData(acbx, clnt, fbuf);
                        }
                    }
                }
                // extract time
                if (map.containsKey(RECORD_TIME)) {
                    record.setTime(getTime(map.get(RECORD_TIME).toString()));
                }

                if (accessor != null) {
                    record.setAccessor(accessor);
                    record.setData(data);
                    final GsonBuilder builder = new GsonBuilder();
                    builder.serializeNulls();
                    final Gson gson = builder.create();
                    e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
                    e.setField("type", sourceField);
                    matchListener.filterMatched(e);
                } else {
                    e.tag(LOGSTASH_TAG_SKIP_NOT_COMMAND);
                }
            }
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

    private Accessor parseAccessor(final HashMap<String, Object> clnt) {
        logger.debug("Client info: {}", clnt);

        final Accessor accessor = new Accessor();

        accessor.setDbProtocol(DATA_PROTOCOL_STRING);
        accessor.setServerType(SERVER_TYPE_STRING);

        accessor.setDbUser(clnt.containsKey(ACCESSOR_DB_USER) ? clnt.get(ACCESSOR_DB_USER).toString() : UNKOWN_STRING);
        accessor.setServerHostName(
                clnt.containsKey(ACCESSOR_SERVER_HOST_NAME) ? clnt.get(ACCESSOR_SERVER_HOST_NAME).toString()
                        : UNKOWN_STRING);
        accessor.setSourceProgram(
                clnt.containsKey(ACCESSOR_SOURCE_PROGRAM) ? clnt.get(ACCESSOR_SOURCE_PROGRAM).toString()
                        : UNKOWN_STRING);
        accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);

        accessor.setClient_mac(UNKOWN_STRING);
        accessor.setClientHostName(UNKOWN_STRING);
        accessor.setClientOs(UNKOWN_STRING);
        accessor.setCommProtocol(UNKOWN_STRING);
        accessor.setDbProtocolVersion(UNKOWN_STRING);
        accessor.setOsUser(UNKOWN_STRING);
        accessor.setServerDescription(UNKOWN_STRING);
        accessor.setServerOs(UNKOWN_STRING);
        accessor.setServiceName(UNKOWN_STRING);
        return accessor;
    }

    private Data parseData(final HashMap<String, Object> acbx, final HashMap<String, Object> clnt,
            final HashMap<String, Object> fbuf) {
        final Data data = new Data();
        data.setConstruct(parseConstruct(acbx, fbuf));

        return data;
    }

    private Construct parseConstruct(HashMap<String, Object> acbx, HashMap<String, Object> fbuf) {
        final Construct construct = new Construct();
        construct.sentences.add(parseSentence(acbx, fbuf));
        return construct;
    }

    private Sentence parseSentence(HashMap<String, Object> acbx, HashMap<String, Object> fbuf) {
        if (acbx == null) {
            return null;
        }
        final Sentence sentence = new Sentence(acbx.get("CMDCODE").toString());
        //
        if (fbuf != null) {
            final String s = fbuf.get("FORMATBUFFER").toString();
            if (s != null) {
                final String[] fb = s.substring(0, s.length() - 1).split(",");
                // very simple formatbuffer parser
                ArrayList<String> fields = new ArrayList<String>();
                int i = 0;
                for (String f : fb) {
                    if (i == 0) {
                        fields.add(f);
                    }
                    i++;
                    if (i == 3) {
                        i = 0;
                    }
                }
                sentence.setFields(fields);
            }
        }
        return sentence;
    }

    private Time getTime(final String dateString) {
        final ZonedDateTime date = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        final long millis = date.toInstant().toEpochMilli();
        final int minOffset = date.getOffset().getTotalSeconds() / 60;
        return new Time(millis, minOffset, 0);
    }
}
