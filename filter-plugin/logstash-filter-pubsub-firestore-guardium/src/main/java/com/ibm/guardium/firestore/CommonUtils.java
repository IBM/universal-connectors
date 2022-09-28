/*

© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0

*/
package com.ibm.guardium.firestore;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonElement;

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
	 * @param String value
	 * @methodName @isJSONValid
	 * @return Boolean value TRUE/FALSE
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
	 * @param JsonElement jsonElement
	 * @methodName @convertIntoString
	 * @return String value
	 * 
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
}
