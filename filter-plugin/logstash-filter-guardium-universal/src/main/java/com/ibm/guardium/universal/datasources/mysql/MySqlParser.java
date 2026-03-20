/*
 * Copyright 2024 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */
package com.ibm.guardium.universal.datasources.mysql;

import co.elastic.logstash.api.Event;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universal.parser.AbstractGuardiumParser;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Parses MySQL audit log events into Guardium Records.
 *
 * <p>Migrated from {@code MySqlFilterGuardium} — parsing logic is identical;
 * all Logstash plugin boilerplate has been removed.
 *
 * <p>Supported log formats: MySQL audit log plugin (JSON) over syslog or Filebeat.
 */
public class MySqlParser extends AbstractGuardiumParser {

    // ---- Message format constants --------------------------------------------

    private static final String AUDIT_START_SIGNAL = "mysql_audit_log: ";

    private static final String CLASS_CONNECTION  = "connection";
    private static final String CLASS_GENERAL     = "general";
    private static final String DATA_CONNECTION   = "connection_data";
    private static final String DATA_GENERAL      = "general_data";

    private static final String DB_PROTOCOL   = "MySQL";
    private static final String SERVER_TYPE   = "MySql";
    private static final String LANGUAGE      = "MYSQL";
    private static final String DATE_FORMAT   = "yyyy-MM-dd HH:mm:ss";

    // SimpleDateFormat is not thread-safe; guard with synchronized helper.
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);

    // ---- IGuardiumParser implementation -------------------------------------

    @Override
    public Record parseRecord(Event event) throws Exception {
        String message = event.getField("message").toString();

        int idx = message.indexOf(AUDIT_START_SIGNAL);
        if (idx == -1) return null;

        String jsonStr = message.substring(idx + AUDIT_START_SIGNAL.length());
        // Remove trailing comma if present (MySQL audit log quirk)
        if (jsonStr.endsWith(",")) {
            jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
        }

        JsonObject data = JsonParser.parseString(jsonStr).getAsJsonObject();
        String classType = data.get("class").getAsString();
        String timestamp = data.get("timestamp").getAsString();

        if (!CLASS_CONNECTION.equals(classType) && !CLASS_GENERAL.equals(classType)) {
            return null; // skip audit/table_access class events
        }

        Record record = new Record();
        record.setDbName(UNKNOWN);
        String eventField = data.get("event").getAsString();
        boolean validRecord = false;

        if (data.has(DATA_CONNECTION) && "connect".equals(eventField)) {
            JsonObject connData = data.getAsJsonObject(DATA_CONNECTION);
            int status = connData.get("status").getAsInt();
            String dbName = connData.get("db").getAsString();
            record.setDbName(dbName);

            if (!dbName.isEmpty() && status == 0) {
                record.setAccessor(buildAccessor(event, data));
                validRecord = true;
            } else if (status != 0) {
                ExceptionRecord ex = new ExceptionRecord();
                ex.setExceptionTypeId("LOGIN_FAILED");
                ex.setDescription("Login Failed (" + status + ")");
                ex.setSqlString(UNKNOWN);
                record.setException(ex);
                validRecord = true;
            }

        } else if (data.has(DATA_GENERAL)) {
            JsonObject genData = data.getAsJsonObject(DATA_GENERAL);
            String command = genData.get("command").getAsString();
            int status = genData.get("status").getAsInt();

            if ("Query".equals(command)) {
                String query = genData.get("query").getAsString();
                if (status != 0) {
                    ExceptionRecord ex = new ExceptionRecord();
                    ex.setExceptionTypeId("SQL_ERROR");
                    ex.setDescription("Error (" + status + ")");
                    ex.setSqlString(query);
                    record.setException(ex);
                } else if (query != null) {
                    Data d = new Data();
                    d.setOriginalSqlCommand(query);
                    record.setData(d);
                }
                validRecord = true;
            }
        }

        if (!validRecord) return null;

        record.setSessionId(data.has("connection_id") && !data.get("connection_id").isJsonNull()
                ? String.valueOf(data.get("connection_id").getAsInt())
                : UNKNOWN);
        record.setAppUserName(UNKNOWN);
        record.setTime(parseTime(timestamp));
        record.setSessionLocator(buildSessionLocator(event, data));
        record.setAccessor(buildAccessor(event, data));
        correctIPs(event, record);

        return record;
    }

    // ---- Helpers -------------------------------------------------------------

    private static synchronized Time parseTime(String ts) throws ParseException {
        Date date = DATE_FORMATTER.parse(ts);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
        return new Time(zdt.toInstant().toEpochMilli(),
                zdt.getOffset().getTotalSeconds() / 60, 0);
    }

    private static SessionLocator buildSessionLocator(Event event, JsonObject data) {
        SessionLocator loc = new SessionLocator();

        String serverIp = event.getField("server_ip") instanceof String
                ? event.getField("server_ip").toString() : "0.0.0.0";

        if (Util.isIPv6(serverIp)) {
            loc.setServerIpv6(serverIp);
            loc.setServerIp(UNKNOWN);
            loc.setIpv6(true);
        } else {
            loc.setServerIp(serverIp);
            loc.setServerIpv6(UNKNOWN);
            loc.setIpv6(false);
        }
        loc.setServerPort(SessionLocator.PORT_DEFAULT);
        loc.setClientIp(UNKNOWN);
        loc.setClientIpv6(UNKNOWN);
        loc.setClientPort(SessionLocator.PORT_DEFAULT);

        if (data.has("login")) {
            JsonObject login = data.getAsJsonObject("login");
            String addr = login.get("ip").getAsString();
            if (Util.isIPv6(addr)) {
                loc.setClientIpv6(addr);
                loc.setClientIp(UNKNOWN);
                loc.setIpv6(true);
            } else {
                loc.setClientIp(addr);
                loc.setClientIpv6(UNKNOWN);
            }
        }
        return loc;
    }

    private static Accessor buildAccessor(Event event, JsonObject data) {
        Accessor acc = new Accessor();
        acc.setDbProtocol(DB_PROTOCOL);
        acc.setServerType(SERVER_TYPE);
        acc.setLanguage(LANGUAGE);
        acc.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);

        acc.setServerHostName(event.getField("server_hostname") instanceof String
                ? event.getField("server_hostname").toString() : UNKNOWN);

        if (data.has("account")) {
            String user = data.getAsJsonObject("account").get("user").getAsString();
            acc.setDbUser(user == null || user.isEmpty() ? "NA" : user);
        }

        String osUser = UNKNOWN, osStr = UNKNOWN;
        if (data.has(DATA_CONNECTION)) {
            JsonObject connData = data.getAsJsonObject(DATA_CONNECTION);
            if (connData.has("connection_attributes")) {
                JsonObject attrs = connData.getAsJsonObject("connection_attributes");
                if (attrs.has("_os"))      osStr  = attrs.get("_os").getAsString();
                if (attrs.has("os_user"))  osUser = attrs.get("os_user").getAsString();
            }
            if ("connect".equals(data.get("event").getAsString())) {
                acc.setServiceName(connData.get("db").getAsString());
            }
        }
        if (acc.getServiceName() == null) acc.setServiceName(UNKNOWN);

        acc.setOsUser(osUser);
        acc.setServerOs(osStr);
        acc.setSourceProgram(UNKNOWN);
        acc.setClient_mac(UNKNOWN);
        acc.setClientHostName(UNKNOWN);
        acc.setClientOs(UNKNOWN);
        acc.setCommProtocol(UNKNOWN);
        acc.setDbProtocolVersion(UNKNOWN);
        acc.setServerDescription(UNKNOWN);
        return acc;
    }
}
