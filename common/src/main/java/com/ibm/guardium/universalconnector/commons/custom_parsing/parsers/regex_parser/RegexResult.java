/*
 * Licensed Materials - Property of IBM
 * 5725I71-CC011829
 * (C) Copyright IBM Corp. 2021. All Rights Reserved.
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.regex_parser;

import java.util.regex.Matcher;

/**
 * Contains all relevant information from attempting to match a Regex against a source String.
 */
public class RegexResult {
    private Matcher matcher;
    private boolean matched = false;
    private long duration = -1L;
    private boolean timedOut = false;

    public RegexResult(Matcher m, boolean matched) {
        this.setMatcher(m);
        this.setMatched(matched);
    }

    public RegexResult(Matcher m, boolean matched, long duration) {
        this(m, matched);
        this.setDuration(duration);
    }

    public RegexResult(long duration, boolean timedOut) {
        this.setDuration(duration);
        this.setTimedOut(timedOut);
    }

    RegexResult(boolean timedOut) {
        this.setTimedOut(timedOut);
    }

    public Matcher getMatcher() {
        return matcher;
    }

    private void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public boolean matched() {
        return matched;
    }

    private void setMatched(boolean matched) {
        this.matched = matched;
    }

    /**
     * Returns how long, in nanoseconds, the Regex took to match (or be interrupted). Note that if
     * timing was disabled during execution the duration will be -1
     *
     * @return The duration of the regex match
     */
    public long getDuration() {
        return duration;
    }

    private void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean timedOut() {
        return timedOut;
    }

    private void setTimedOut(boolean timedOut) {
        this.timedOut = timedOut;
    }
}