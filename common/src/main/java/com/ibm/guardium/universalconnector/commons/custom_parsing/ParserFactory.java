package com.ibm.guardium.universalconnector.commons.custom_parsing;

import com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.IParser;
import com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.JsonParser;
import com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.LeefParser;
import com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.RegexParser;

public class ParserFactory {
    public ParserFactory() {
    }

    public IParser getParser(ParserType parserType) {
        if (parserType.equals(ParserType.json))
            return new JsonParser();
        else if (parserType.equals(ParserType.leef))
            return new LeefParser();

        return new RegexParser();
    }

    public enum ParserType {
        regex,
        json,
        leef
    }
}
