/*
Copyright IBM Corp. 2021, 2025 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.aws.opensearch;

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
}
