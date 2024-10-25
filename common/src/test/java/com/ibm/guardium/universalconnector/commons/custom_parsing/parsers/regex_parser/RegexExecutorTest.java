package com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.regex_parser;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class RegexExecutorTest {
    private RegexExecutor re = new RegexExecutor();

    @Test
    public void testBaseCases() {
        RegexResult rr = re.find(Pattern.compile(".*"), "a");
        assertTrue(rr.matched());
        assertFalse(rr.timedOut());
        assertTrue(rr.getDuration() > -1);

        rr = re.find(Pattern.compile("\\d"), "a");
        assertFalse(rr.matched());
        assertFalse(rr.timedOut());
        assertTrue(rr.getDuration() > -1);
    }

    @Test
    public void testBaseCasesNoTiming() {
        RegexResult rr = re.find(Pattern.compile(".*"), "a", 0, 0, false);
        assertTrue(rr.matched());
        assertFalse(rr.timedOut());
        assertEquals(-1, rr.getDuration());

        rr = re.find(Pattern.compile("\\d"), "a", 0, 0, false);
        assertFalse(rr.matched());
        assertFalse(rr.timedOut());
        assertEquals(-1, rr.getDuration());
    }
}