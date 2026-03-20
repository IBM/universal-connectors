/*
 * Copyright 2024 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */
package com.ibm.guardium.universal.parser;

import co.elastic.logstash.api.Event;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Optional base class providing utilities shared across many datasource parsers.
 *
 * <p>Parsers may extend this class to avoid duplicating IP-correction and
 * other common logic, but implementing {@link IGuardiumParser} directly is
 * also fine.
 */
public abstract class AbstractGuardiumParser implements IGuardiumParser {

    protected static final String UNKNOWN = "";
    protected static final int DEFAULT_PORT = SessionLocator.PORT_DEFAULT;

    private static final Set<String> LOCAL_IPS = new HashSet<>(
            Arrays.asList("127.0.0.1", "0:0:0:0:0:0:0:1", "::1", "(NONE)"));

    /**
     * Overrides local/internal server and client IPs using the Logstash event's
     * {@code server_ip} field when the session-level IPs are empty or loopback.
     * This mirrors the identical logic previously duplicated in every filter plugin.
     */
    protected void correctIPs(Event event, Record record) {
        SessionLocator loc = record.getSessionLocator();
        if (loc == null) return;

        String serverIp = loc.isIpv6() ? loc.getServerIpv6() : loc.getServerIp();
        if (isLocalOrEmpty(serverIp) && event.getField("server_ip") instanceof String) {
            String ip = event.getField("server_ip").toString();
            if (Util.isIPv6(ip)) {
                loc.setServerIpv6(ip);
                loc.setServerIp(UNKNOWN);
                loc.setIpv6(true);
            } else {
                loc.setServerIp(ip);
                loc.setServerIpv6(UNKNOWN);
                loc.setIpv6(false);
            }
        }

        String clientIp = loc.isIpv6() ? loc.getClientIpv6() : loc.getClientIp();
        if (isLocalOrEmpty(clientIp)) {
            if (loc.isIpv6()) {
                loc.setClientIpv6(loc.getServerIpv6());
                loc.setClientIp(UNKNOWN);
            } else {
                loc.setClientIp(loc.getServerIp());
                loc.setClientIpv6(UNKNOWN);
            }
        }
    }

    protected boolean isLocalOrEmpty(String ip) {
        return ip == null || ip.isEmpty() || LOCAL_IPS.contains(ip);
    }
}
