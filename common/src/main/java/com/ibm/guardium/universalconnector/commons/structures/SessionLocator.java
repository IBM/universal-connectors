//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.universalconnector.commons.structures;

/**
 * Describes location details about the data source connection/session: 
 * Who connected, from which client IP and port, to what server IP and port.
 * <p>
 * Uses either IPv4 or IPv6 addresses.
 */
public class SessionLocator {

    public static final int PORT_DEFAULT = -1;
    private String clientIp;
    private int clientPort=PORT_DEFAULT;
    private String serverIp;
    private int    serverPort=PORT_DEFAULT;
    private boolean isIpv6;
    private String clientIpv6;
    private String serverIpv6;

    public int getClientPort() {
        return clientPort;
    }

    /**
     * Sets the [listening] port of the client (user).
     * 
     * @param clientPort    The port the data source uses during the connection with the client.
     */
    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public String getClientIp() {
        return clientIp;
    }

    /**
     * Sets the IPv4 address of the or data source client (user).
     * 
     * @param clientIp Server IPv4; If not available, populate with client IP.
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getServerIp() {
        return serverIp;
    }

    /**
     * Sets the data source server IPv4 address.
     * 
     * @param serverIp Server IPv4; If not available, populate with client IP.
     */
    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }
    
    /**
     * Sets the data source server [listening] port.
     * 
     * @param serverPort Set to 0 if not available. For data sources on AWS, set to
     *                   -1.
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public boolean isIpv6() {
        return isIpv6;
    }
    
    /**
     * (Optional) Flags to look at IPv6 addresses. When true, Guardium will use
     * getClientIPv6() and getServerIPv6().
     * 
     * @param ipv6 If set to true (default: false), no need to use setClientIP() and
     *             setServerIP().
     */
    public void setIpv6(boolean ipv6) {
        isIpv6 = ipv6;
    }

    public String getClientIpv6() {
        return clientIpv6;
    }

    /**
     * Sets the IPv6 address of the or data source client (user).
     * 
     * @param clientIpv6
     */
    public void setClientIpv6(String clientIpv6) {
        this.clientIpv6 = clientIpv6;
    }

    public String getServerIpv6() {
        return serverIpv6;
    }
    
    /**
     * Sets the data source server IPv6 address.
     * 
     * @param serverIpv6 If not available, populate with client IP. For data sources
     *                   on AWS, set to "0.0.0.0".
     */
    public void setServerIpv6(String serverIpv6) {
        this.serverIpv6 = serverIpv6;
    }
}
