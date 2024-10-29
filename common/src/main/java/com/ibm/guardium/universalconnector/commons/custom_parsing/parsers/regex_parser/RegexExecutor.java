/*
 * Licensed Materials - Property of IBM
 * 5725I71-CC011829
 * (C) Copyright IBM Corp. 2021. All Rights Reserved.
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.regex_parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Executes a Pattern (regular-expression) against a source String. Enforces a timeout with respect
 * to how long we will attempt to match the Regex. After the timeout expires, the Regex match wil
 * bail.
 */
public class RegexExecutor {
    static final long DEFAULT_TIMEOUT_MICRO = 1000;
    private static final boolean DEFAULT_TIMING = true;

    private static final Logger logger = LogManager.getLogger(RegexExecutor.class);

    /**
     * Creates a Matcher object from the supplied Pattern and source String, and then calls
     * matcher.find(). Limits the amount of processing time the Regex can take.
     *
     * @param p      The Pattern to attempt to match.
     * @param source The String to attempt the match on.
     * @return A RegexResult containing all relevant attributes of the regex match
     */
    public RegexResult find(Pattern p, String source) {
        return find(p, source, 0, DEFAULT_TIMEOUT_MICRO, DEFAULT_TIMING);
    }


    /**
     * Creates a Matcher object from the supplied Pattern and source String, and then calls
     * matcher.find(). Limits the amount of processing time the Regex can take.
     *
     * @param p          The Pattern to attempt to match.
     * @param source     The String to attempt the match on.
     * @param startIndex The index within the source String to begin the find() from.
     * @param timeout    The amount of time (in microseconds) the regex will be allowed to take during
     *                   matching
     * @param timing     Whether or not to record how long it took to match.
     * @return A RegexResult containing all relevant attributes of the regex match
     */
    public RegexResult find(Pattern p, String source, int startIndex, long timeout, boolean timing) {
        long current = System.nanoTime();
        Matcher m = p.matcher(new TimeoutString(source, current + (timeout * 1000)));
        try {
            boolean matched = m.find(startIndex);
            if (timing) {
                return new RegexResult(m, matched, System.nanoTime() - current);
            } else {
                return new RegexResult(m, matched);
            }
        } catch (RegexTimeoutException e) {
            logger.debug(
                    "Regex [{}] took >={} microseconds to match against String [{}]",
                    p.pattern(),
                    timeout,
                    source,
                    e);
            if (timing) {
                return new RegexResult(timeout, true);
            } else {
                return new RegexResult(true);
            }
        }
    }
}