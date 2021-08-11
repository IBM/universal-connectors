//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.universalconnector.commons;

/**
 * A utility class for creating a Guardium {@link com.ibm.guardium.universalconnector.commons.structures.Record Record}.
 */
public class Util {
    /**
     * Returns true if address is in IPv6 format. 
     * 
     * Example IPv6 addresses: 
     *      2001:0db8:85a3:0000:0000:8a2e:0370:7334
     *      fe80::a00:27ff:fee0:1fcf%enp0s3
     * 
     * @param address
     * @return
     */
    static public boolean isIPv6(String address) {
        return address.contains(":");
    }

    /**
     * Gets the time in seconds.
     * 
     * @param time  A representation of time in milliseconds, usually from {@link java.time.Instant#toEpochMilli()} 
     * @return
     */
    public static int getTimeUnixTime(Long time) {
        return (int) (time/1000);
    }

    public static int getTimeMicroseconds(Long time) {
        return (int) (time%1000 * 1000);
    }

}