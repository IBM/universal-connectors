/*
Copyright IBM Corp. 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.guardium.icd.postgresql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class MultilineEventParser {
	/**
	 * Regex to parse the Error statement.
	 */
	private static String MAIN_EVENT_REGEX_ERROR = "(?:\\d{4}-[01]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d:[0-5]\\d\\.\\d+([+-][0-2]\\d:[0-5]\\d|Z)\\s[A-Za-z]+\\s\\w{1}\\s\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\s[A-Z]+\\s\\[[A-Za-z]+\\]\\s\\[[0-9]+\\]\\s\\[[0-9]+\\]:\\s\\[[0-9-]+\\]\\s[a-zA-Z=,0-9.\\s]+:)\\s+";
	private static String MAIN_EVENT_REGEX_ERROR_DUMMY = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d+Z\\sstderr\\s\\w{1}\\s\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\s\\w+\\s[A-Za-z\\[\\]\\s\\d:\\-]+\\s\\w+=\\w+,\\w+=\\w+,\\w+=\\w\\d+.\\d+.\\d+.\\d+\\sSTATEMENT:\\s+";
	/*
	 * There is a specific pattern in the first event which we are parsing using the
	 * MAIN_EVENT_REGEX
	 */
	private static String MAIN_EVENT_REGEX = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d+Z\\sstderr\\s\\w{1}\\s\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\s\\w+\\s[A-Za-z\\[\\]\\s\\d:\\-]+\\s[A-Za-z\\=,0-9\\.\\s:\\\\]+[a-z0-9\\\\\\s]+";

	/*
	 * 
	 * Events merged using Multiline Codec will be having different format, this has
	 * been captured using SUB_EVENT_REGEX
	 */

	private static String SUB_EVENT_REGEX = "\\d{4}-[01]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d:[0-5]\\d\\.\\d+([+-][0-2]\\d:[0-5]\\d|Z)\\s[A-Za-z]+\\s\\w{1}";

	/**
	 * Method takes event object and array of event.
	 * 
	 * @param event
	 */

	public void prepareEventForMultiLineLogs(Event event) {
		StringBuilder queryBuilder = new StringBuilder();
		JsonArray messageArray = getJSONObject(event.getField(ApplicationConstant.MESSAGE).toString());
		JsonObject objectPartOne = null;
		boolean isFirst = true;
		String messagePart = "";
		for (int i = 0; i < messageArray.size(); i++) {
			objectPartOne = messageArray.get(i).getAsJsonObject();
			String str = objectPartOne.get("message").toString();
			if (isFirst) {
				if (str.contains("STATEMENT")) {
					messagePart = objectPartOne.get(ApplicationConstant.MESSAGE).toString().replaceAll(MAIN_EVENT_REGEX_ERROR,
							"");
					messagePart = messagePart.replaceAll(MAIN_EVENT_REGEX_ERROR_DUMMY, "");

					queryBuilder.append(messagePart.substring(1, messagePart.length() - 1));
				} else {
					messagePart = objectPartOne.get(ApplicationConstant.MESSAGE).toString().replaceAll(MAIN_EVENT_REGEX, "");
					queryBuilder.append(messagePart.substring(1, messagePart.length() - 1));

				}
				// All other information is same, this check will populate event filed with
				// required values.
				// Later on we only have to parse and join the message.
				prepareEventObject(event, objectPartOne);
				isFirst = false;
			} else {
				messagePart = objectPartOne.get(ApplicationConstant.MESSAGE).toString().replaceAll(SUB_EVENT_REGEX, "");
				queryBuilder.append(messagePart.substring(1, messagePart.length() - 1));
			}
			// if its no the last event then add a space after each parsed line.
			if (i < (messageArray.size() - 1)) {
				queryBuilder.append(ApplicationConstant.SPACE);
			}
		}
		JsonObject objectPartTwo = messageArray.get(0).getAsJsonObject();
		String str = objectPartTwo.get("message").toString();
		if (str.contains("STATEMENT")) {
			event.setField(ApplicationConstant.STATUS, refactorFinalQueryString(queryBuilder.toString()));
			event.setField(ApplicationConstant.STATEMENT, "STATEMENT");
		} else {
			event.setField(ApplicationConstant.SQL_QUERY, refactorFinalQueryString(queryBuilder.toString()));
		}
	}

	/**
	 * Method to fetch the required fields from event parts (Multiline event).
	 * 
	 * @param event
	 * @param inputJson
	 */
	private void prepareEventObject(Event event, JsonObject inputJson) {
		String[] message = inputJson.get("message").toString().split(ApplicationConstant.SPACE);
		String otherDetail = inputJson.get("logSourceCRN").toString().substring(1,
				inputJson.get("logSourceCRN").toString().length() - 3);
		String[] otherDetailArray = otherDetail.split(":");
		String[] guardFields = message[10].split(",");
		String errorcode = message[7].replace("[", "").replace("]", "");
		event.setField(ApplicationConstant.TIMESTAMP, message[0].substring(1));
		event.setField(ApplicationConstant.ERROR_CODE, errorcode);
		event.setField(ApplicationConstant.ACCOUNT_ID, otherDetailArray[6].substring(2));
		event.setField(ApplicationConstant.DETAILS, ":" + otherDetailArray[7]);
		event.setField(ApplicationConstant.USER_NAME, guardFields[0].replace(ApplicationConstant.USERNAME_KEY, ""));
		event.setField(ApplicationConstant.DB_NAME, guardFields[1].replace(ApplicationConstant.DBNAME_KEY, ""));
		event.setField(ApplicationConstant.CLIENT_IP, guardFields[2].replace(ApplicationConstant.CLIENTIP_KEY, ""));
		event.setField(ApplicationConstant.TIMEZONE,
				event.getField("M_TIMEZONE") != null ? event.getField("M_TIMEZONE").toString()
						: ApplicationConstant.DEFAULT_TIMEZONE);
	}

	/**
	 * Method to - the final query by removing extra quotes and tabspace
	 * 
	 * @param query
	 * @return
	 */
	private String refactorFinalQueryString(String query) {
		return query.replaceAll("\\\\t", " ").replaceAll("\"", "").replaceAll("    ", " ").replaceAll("   ", " ")
				.replaceAll("\\\\", "").replace(",<not logged>", "");
	}

	/**
	 * Converts the String event object into JSONObject.
	 * 
	 * @param message
	 * @return
	 */
	private JsonArray getJSONObject(String message) {
		return new Gson().fromJson(message, JsonArray.class);
	}

}
