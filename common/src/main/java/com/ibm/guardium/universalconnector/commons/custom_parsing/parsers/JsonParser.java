package com.ibm.guardium.universalconnector.commons.custom_parsing.parsers;

import com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.json_parser.JsonUtil;

import java.util.Map;

public class JsonParser implements IParser {

    private JsonUtil util = new JsonUtil();
    Map<String, String> extractedProperties;

    @Override
    public String parse(String payload, String key) {
        if (key == null)
            return null;
        return extractedProperties.get(key);
    }

    @Override
    public boolean isPayloadValid(String payload) {
        if (payload == null)
            return false;

        extractedProperties = util.getMap(payload);
        return extractedProperties != null && !extractedProperties.isEmpty();
    }
}
