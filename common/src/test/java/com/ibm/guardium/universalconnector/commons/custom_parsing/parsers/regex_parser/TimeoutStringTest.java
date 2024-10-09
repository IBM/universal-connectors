package com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.regex_parser;

import org.junit.Test;

import static org.junit.Assert.*;

public class TimeoutStringTest {
    @Test
    public void testTimeoutString() {
        TimeoutString ts  = new TimeoutString("", -1, 1);

        try {
            ts.charAt(0);
            fail("Expected TimeoutException to be thrown");
        } catch (RegexTimeoutException e) {
            // expected
        }

        ts = new TimeoutString("123", System.nanoTime() - (1000));

        for (int i = 0; i < TimeoutString.DEFAULT_CHECK_ITERATIONS - 1; i++) {
            ts.charAt(0);
        }

        // This should be the call into charAt() that actually triggers the timeout
        try {
            ts.charAt(0);
            fail("Expected TimeoutException to be thrown");
        } catch (RegexTimeoutException e) {
            // expected
        }
    }
}