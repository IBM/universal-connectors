//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.universalconnector.commons.structures;

public class Time {

    private long timstamp;
    private int minOffsetFromGMT;
    private int minDst;

    public Time() {
    }

    public Time(long timstamp, int minOffsetFromGMT, int minDst) {
        this.timstamp = timstamp;
        this.minOffsetFromGMT = minOffsetFromGMT;
        this.minDst = minDst;
    }

    public long getTimstamp() {
        return timstamp;
    }

    public void setTimstamp(long timstamp) {
        this.timstamp = timstamp;
    }

    public int getMinOffsetFromGMT() {
        return minOffsetFromGMT;
    }

    public void setMinOffsetFromGMT(int minOffsetFromGMT) {
        this.minOffsetFromGMT = minOffsetFromGMT;
    }

    public int getMinDst() {
        return minDst;
    }

    public void setMinDst(int minDst) {
        this.minDst = minDst;
    }
}
