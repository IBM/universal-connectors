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
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.Time;

public class Parser {

    private static final Logger logger = LogManager.getLogger(Parser.class);

    @SuppressWarnings("unchecked")
    public Record parseRecord(HashMap<String, Object> map) {
        final Record record = new Record();
        Accessor accessor = null;
        Data data = null;

        if (map.containsKey("UABI_ITEMS")) {
            // extract data from the map
            final HashMap<String, Object> items = (HashMap<String, Object>) map.get("UABI_ITEMS");
            // data for the record
            if (items.containsKey(Constants.RECORD_DB_NAME)) {
                record.setDbName(items.get(Constants.RECORD_DB_NAME).toString());
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
                        final String type = itemMap.get("UABDTY").toString();
                        if (type.equals("ACBX") || type.equals("CLNT") || type.equals("FBUF")) {
                            if (itemMap.containsKey("PAYLOAD_CLNT")) {
                                clnt = (HashMap<String, Object>) itemMap.get("PAYLOAD_CLNT");
                                // cnlt data for the record
                                if (clnt.containsKey(Constants.RECORD_APP_USER_NAME)) {
                                    record.setAppUserName(clnt.get(Constants.RECORD_APP_USER_NAME).toString());
                                }
                                // fill accessor data
                                accessor = parseAccessor(clnt);
                            }
                            if (itemMap.containsKey("PAYLOAD_ACBX")) {
                                acbx = (HashMap<String, Object>) itemMap.get("PAYLOAD_ACBX");
                                // acbx data for the record
                                if (acbx.containsKey(Constants.RECORD_SESSION_ID)) {
                                    record.setSessionId(acbx.get(Constants.RECORD_SESSION_ID).toString());
                                }
                                if (acbx.containsKey(Constants.ACBX_RSP_CODE)) {
                                    int rspCode = (int) acbx.get(Constants.ACBX_RSP_CODE);
                                    if (rspCode != 0) {
                                        ExceptionRecord exceptionRecord = new ExceptionRecord();
                                        exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);
                                        exceptionRecord
                                                .setDescription("Response Code "
                                                        + acbx.get(Constants.ACBX_RSP_CODE).toString() + "("
                                                        + acbx.get(Constants.ACBX_RSP_SUB_CODE).toString()
                                                        + ") received.");
                                        exceptionRecord.setSqlString(
                                                acbx.get(Constants.ACBX_CMD_CODE).toString() + " with ISN "
                                                        + acbx.get(Constants.ACBX_ISN).toString());
                                        record.setException(exceptionRecord);
                                    }
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
        if (map.containsKey(Constants.RECORD_TIME)) {
            record.setTime(getTime(map.get(Constants.RECORD_TIME).toString()));
        }
        record.setAccessor(accessor);
        record.setData(data);
        return record;
    }

    private Accessor parseAccessor(final HashMap<String, Object> clnt) {
        logger.debug("Client info: {}", clnt);

        final Accessor accessor = new Accessor();
        accessor.setDbProtocol(Constants.DATA_PROTOCOL_STRING);
        accessor.setServerType(Constants.SERVER_TYPE_STRING);

        accessor.setDbUser(
                clnt.containsKey(Constants.ACCESSOR_DB_USER) ? clnt.get(Constants.ACCESSOR_DB_USER).toString()
                        : Constants.UNKNOWN_STRING);
        accessor.setServerHostName(
                clnt.containsKey(Constants.ACCESSOR_SERVER_HOST_NAME)
                        ? clnt.get(Constants.ACCESSOR_SERVER_HOST_NAME).toString()
                        : Constants.UNKNOWN_STRING);
        accessor.setSourceProgram(
                clnt.containsKey(Constants.ACCESSOR_SOURCE_PROGRAM)
                        ? clnt.get(Constants.ACCESSOR_SOURCE_PROGRAM).toString()
                        : Constants.UNKNOWN_STRING);
        accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);

        accessor.setClient_mac(Constants.UNKNOWN_STRING);
        accessor.setClientHostName(Constants.UNKNOWN_STRING);
        accessor.setClientOs(Constants.UNKNOWN_STRING);
        accessor.setCommProtocol(Constants.UNKNOWN_STRING);
        accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
        accessor.setOsUser(Constants.UNKNOWN_STRING);
        accessor.setServerDescription(Constants.UNKNOWN_STRING);
        accessor.setServerOs(Constants.UNKNOWN_STRING);
        accessor.setServiceName(Constants.UNKNOWN_STRING);
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