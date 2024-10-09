package com.ibm.guardium.universalconnector.commons.custom_parsing;

import com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.IParser;
import com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.RegexParser;

public class ParserFactory {
    IParser parser;

    public ParserFactory() {
    }

    public IParser getParser(ParserType parserType) {
        //For now we only support Regex
        //if(parserType.equals(ParserType.regex))
        return new RegexParser();
    }

    public enum ParserType {
        regex,
        json,
        xml
    }
}
