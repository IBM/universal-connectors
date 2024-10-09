/*
 * Licensed Materials - Property of IBM
 * 5725I71-CC011829
 * (C) Copyright IBM Corp. 2021-2022. All Rights Reserved.
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.regex_parser;

/**
 * Implementation of a CharSequence that is interruptible. The point here is that we can use this to
 * back Strings that are being run through Regular Expression matching. Because the default
 * implementation of Matcher will walk the String, we hijack the charAt() method to occasionally
 * check to see if we're taking too long to parse this regex. <br>
 * <br>
 * Any usage of this String implementation should be wrapped in a try/catch expecting a
 * RegexTimeoutException.
 */
public class TimeoutString implements CharSequence {
    static final long DEFAULT_CHECK_ITERATIONS = 500;

    private final CharSequence source;
    private final long endTime;
    private long timeoutCheckIterations = DEFAULT_CHECK_ITERATIONS;
    private long count = 0;

    public TimeoutString(CharSequence source, long timeoutNano) {
        this.source = source;
        this.endTime = timeoutNano;
    }

    public TimeoutString(CharSequence source, long timeoutNano, int timeoutCheckIterations) {
        this(source, timeoutNano);
        this.timeoutCheckIterations = timeoutCheckIterations;
    }

    @Override
    public int length() {
        return source.length();
    }

    @Override
    public char charAt(int index) {
        count++;
        if (count % timeoutCheckIterations == 0 && System.nanoTime() > this.endTime) {
            throw new RegexTimeoutException(
                    String.format("Regex match timed out against source-string %s", this.source));
        }
        return source.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return source.subSequence(start, end);
    }
}