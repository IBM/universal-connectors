package com.ibm.guardium.universalconnector.commons.custom_parsing.parsers;

import com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.regex_parser.RegexExecutor;
import com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.regex_parser.RegexResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexParser implements IParser {
    private static final Logger logger = LogManager.getLogger(RegexParser.class);

    private static final RegexExecutor executor = new RegexExecutor();

    @Override
    public String parse(String payload, String regexString) {
        if (regexString == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(regexString);
        RegexResult rr = executor.find(pattern, payload);
        if (rr.matched()) {
            Matcher m = rr.getMatcher();
            return m.groupCount() > 0 ? m.group(1) : m.group();
        } else {
            if (rr.timedOut() && logger.isDebugEnabled()) {
                logger.debug("Regex parse aborted due to taking too long to match -- regex: {}, event-payload: {}", pattern, payload);
            }
            return null;
        }
    }

}
