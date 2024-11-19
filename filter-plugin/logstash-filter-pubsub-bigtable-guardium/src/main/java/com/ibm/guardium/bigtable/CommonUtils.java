/*
Copyright IBM Corp. 2021, 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.bigtable;

import com.google.gson.JsonElement;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class CommonUtils {

    /**
     *
     */
    public CommonUtils() {
        super();
    }

    /**
     * isJSONValid() method is used to validate input string is valid JSON or NOT
     *
     * @return Boolean value TRUE/FALSE
     * @methodName @isJSONValid
     */
    public static boolean isJSONValid(String value) {
        try {
            new JSONObject(value);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    /**
     * convertIntoString() method is used to retrieve any value from JsonElement
     *
     * @return String value
     * @methodName @convertIntoString
     */
    public static String convertIntoString(JsonElement jsonElement) {
        String value = StringUtils.EMPTY;
        if (jsonElement != null) {
            value = String.valueOf(jsonElement);
        }
        if (!StringUtils.isBlank(value) && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }

        return value;
    }

    static int convertToInt(JsonElement jsonElement) {
        String value = convertIntoString(jsonElement);
        try{
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
