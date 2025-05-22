/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.guardium.icd.postgresql;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import co.elastic.logstash.api.Event;

public class MultilineEventParser {
	/**
	 * Regex to parse the Error statement.
	 */

	private Logger log = LogManager.getLogger(ICDPostgresqlGuardiumFilter.class);
	/*
	 * There is a specific pattern in the first event which we are parsing using the
	 * MAIN_EVENT_REGEX and MAIN_EVENT_REGEX_SINGLELINE_EVENT
	 */
	private static String MAIN_EVENT_REGEX = "(?<timestamp>\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})\\s+(?<timezone>[A-Z]{3})\\s+\\[(?<sourceProgram>.*)\\]\\s+\\[(?<sqlstate>[0-9A-Za-z]+)\\]\\s+\\[(?<sessionid>[0-9A-Za-z]+)\\]:\\s+\\[[0-9A-Za-z\\-]*\\]\\s+(.*=(?<username>.*)),(.*=(?<dbname>.*)),(.*=(?<clientip>.*))\\s+LOG:\\s+AUDIT:\\s+(?<data1>[^,]*),(?<data2>[^,]*),(?<data3>[^,]*),(?<data4>[^,]*),(?<data5>[^,]*),(?<data6>[^,]*),(?<data7>[^,]*),(?<query>(.*))";

	private static String MAIN_EVENT_REGEX_SINGLELINE_EVENT = "(?<timestamp>\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})\\s+(?<timezone>[A-Z]{3})\\s+\\[(?<sourceProgram>[^\\]]+)\\]\\s+\\[(?<sqlstate>[0-9A-Za-z]+)\\]\\s+\\[(?<sessionid>[0-9A-Za-z]+)\\]:\\s+\\[[0-9A-Za-z\\-]*\\]\\s+(user=(?<username>[^,]+)),(db=(?<dbname>[^,]+)),(client=(?<clientip>[^,]+))\\s+LOG:\\s+AUDIT:\\s+(?<data1>[^,]*),(?<data2>[^,]*),(?<data3>[^,]*),(?<data4>[^,]*),(?<data5>[^,]*),(?<data6>[^,]*),(?<data7>[^,]*),(?<query>(.*)),<not logged>";

	private static final String EVENT_STATEMENT_REGEX = "(?<timestamp>\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})\\s+(?<timezone>[A-Z]{3})\\s+\\[(?<sourceProgram>.*)\\]\\s+\\[(?<sqlstate>[0-9A-Za-z]+)\\]\\s+\\[(?<sessionid>[0-9A-Za-z]+)\\]:\\s+\\[[0-9A-Za-z\\-]*\\]\\s+(.*=(?<username>.*)),(.*=(?<dbname>.*)),(.*=(?<clientip>.*))\\s+STATEMENT:\\s+(?<query>(.*))";

	private static final String FATAL_STATEMENT_REGEX = "(?<timestamp>\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})\\s+(?<timezone>[A-Z]{3})\\s+\\[(?<sourceProgram>.*)\\]\\s+\\[(?<sqlstate>[0-9A-Za-z]+)\\]\\s+\\[(?<sessionid>[0-9A-Za-z]+)\\]:\\s+\\[(?<messageid>.*)\\]\\s+(.*=(?<username>.*)),(.*=(?<dbname>.*)),(.*=(?<clientip>.*))\\s+FATAL:\\s+(?<query>(.*))";

	/**
	 * Method takes event object and array of event.
	 *
	 * @param event
	 */

	public void prepareEventForMultiLineLogs(Event event) {
		//JsonArray messageArray = new JsonArray();
		StringBuilder query = new StringBuilder();
		String message = "";
		//JsonObject objectPartOne = new JsonObject();
		String logSourceCRN = "";
		String includeAccountIdInHost = event.getField(ApplicationConstant.INCLUDE_ACCOUNTID_IN_HOST).toString();
		//boolean isMultilineEvent = isMultilineEvent(event);
		String eventType = ApplicationConstant.SKIP_EVENT;
		String messageFromEvent = event.getField(ApplicationConstant.MESSAGE).toString();
		if (messageFromEvent.contains(ApplicationConstant.INFO_EVENT) || messageFromEvent.contains("MISC")) {
			event.setField(ApplicationConstant.EVENT_TYPE, ApplicationConstant.SKIP_EVENT);
		} else {
			String singleLineMessage = event.getField(ApplicationConstant.MESSAGE).toString();
			JsonObject jsonObject = JsonParser.parseString(singleLineMessage).getAsJsonObject();
			JsonObject jsonMessage = jsonObject;
			if(jsonObject.has("data")) {
				String data = jsonObject.get("data").toString();
				JsonObject dataObject = JsonParser.parseString(data).getAsJsonObject();
				jsonMessage = dataObject.getAsJsonObject(ApplicationConstant.MESSAGE).getAsJsonObject();
			}

			message = jsonMessage.get(ApplicationConstant.MESSAGE).getAsString();
			logSourceCRN = jsonMessage.get(ApplicationConstant.LOG_SOURCE_CRN).toString();
			Matcher matcher;
			if (message.contains(ApplicationConstant.STATEMENT_EVENT)) {
				matcher = Pattern.compile(EVENT_STATEMENT_REGEX).matcher(message);
			} else if (message.contains(ApplicationConstant.FATAL_EVENT)){
				matcher = Pattern.compile(FATAL_STATEMENT_REGEX).matcher(message);
			} else if (message.endsWith("<not logged>")){
				matcher = Pattern.compile(MAIN_EVENT_REGEX_SINGLELINE_EVENT).matcher(message);
			} else {
				matcher = Pattern.compile(MAIN_EVENT_REGEX).matcher(message);
			}

			if (matcher.find()) {
				event.setField(ApplicationConstant.TIMESTAMP, matcher.group(ApplicationConstant.TIMESTAMP));
				event.setField(ApplicationConstant.ERROR_CODE, matcher.group(ApplicationConstant.SQL_STATE));
				event.setField(ApplicationConstant.SESSION_ID, matcher.group(ApplicationConstant.SESSION_ID));
				if (matcher.group(ApplicationConstant.SQL_STATE).equalsIgnoreCase(ApplicationConstant.SUCCESS_STATE)) {
					event.setField(ApplicationConstant.STATEMENT, "S");
				} else {
					event.setField(ApplicationConstant.STATEMENT, "F");
				}
				event.setField(ApplicationConstant.USER_NAME, matcher.group(ApplicationConstant.USERNAME_KEY));
				event.setField(ApplicationConstant.DB_NAME, matcher.group(ApplicationConstant.DB_NAME));
				event.setField(ApplicationConstant.CLIENT_IP, matcher.group(ApplicationConstant.CLIENTIP_KEY));
				event.setField(ApplicationConstant.TIMEZONE, matcher.group(ApplicationConstant.TIMEZONE));
				String sourceProgram = matcher.group(ApplicationConstant.SOURCE_PROGRAM);
				if (!sourceProgram.isEmpty() && !sourceProgram.equalsIgnoreCase("[unknown]"))
					event.setField(ApplicationConstant.SOURCE_PROGRAM, matcher.group(ApplicationConstant.SOURCE_PROGRAM));
				else event.setField(ApplicationConstant.SOURCE_PROGRAM, ApplicationConstant.NOT_AVAILABLE);
				if (!matcher.group(ApplicationConstant.QUERY).isEmpty() || !matcher.group(ApplicationConstant.QUERY).equalsIgnoreCase("BEGIN")) {
					eventType = ApplicationConstant.PROCESS_EVENT;
					query = query.append(refactorFinalQueryString(matcher.group(ApplicationConstant.QUERY)));
				}
			}
			else
				eventType = ApplicationConstant.SKIP_EVENT;
			if (!logSourceCRN.isEmpty()) {
				String updatedCRN = logSourceCRN.replace("\"", "");
				String logSourceString = "";
				if(event.includes("logSource") && null != event.getField("logSource")){
					eventType = setLogSourceCRN(event, updatedCRN, logSourceCRN, eventType);
				}else{
					setSourceCRNAndAccountId(event, logSourceCRN);
				}
			}

			event.setField(ApplicationConstant.SQL_QUERY, refactorFinalQueryString(query.toString()));
			event.setField(ApplicationConstant.EVENT_TYPE, eventType);
			event.setField(ApplicationConstant.INCLUDE_ACCOUNTID_IN_HOST, includeAccountIdInHost);
		}
	}

	private String setLogSourceCRN(Event event, String updatedCRN, String logSourceCRN, String eventType) {
		String logSourceString;
		String logSource = (event.getField("logSource").toString());
		List logSourceArray = new ArrayList();

		if(logSource.contains(",")){
			logSource = logSource.substring(1, logSource.length() - 1);
			logSourceArray = Arrays.asList(logSource.split(",\\s*"));
			if(logSourceArray.contains(updatedCRN)){
				setSourceCRNAndAccountId(event, logSourceCRN);
			}else{
				eventType = ApplicationConstant.SKIP_EVENT;
			}
		}else {
			if(logSource.equalsIgnoreCase(updatedCRN)){
				setSourceCRNAndAccountId(event, logSourceCRN);
			}else{
				eventType = ApplicationConstant.SKIP_EVENT;
			}
		}
		return eventType;
	}

	private void setSourceCRNAndAccountId(Event event, String logSourceCRN) {
		logSourceCRN = logSourceCRN.substring(1, logSourceCRN.length() - 3);
		String[] logSourceCRNArray = logSourceCRN.split(ApplicationConstant.COLON);
		event.setField(ApplicationConstant.DETAILS, ApplicationConstant.COLON + logSourceCRNArray[7]);
		String[] accountIdArray = logSourceCRNArray[6].split("/");
		event.setField(ApplicationConstant.ACCOUNT_ID, accountIdArray[1]);
	}


	/**
	 * Method to - the final query by removing extra quotes and tabspace
	 *
	 * @param query
	 * @return
	 */
	private String refactorFinalQueryString(String query) {
		// Step 1: Remove the first and last double quotes and \t and extra spaces if they exist
		query = query.replaceAll("\\\\t", " ")
				.replaceAll("    ", " ")
				.replaceAll("   ", " ")
				.replace(",<not logged>\"", "");
		query = query.replace("\\","");
		if (query.startsWith("\"")){
			query = query.substring(1);
		}
		if (query.endsWith("\"")){
			query = query.substring(0, query.length()-1);
		}

		// Step 2: Replace double double-quotes around the table name with single double-quotes
		query = query.replaceAll("\"{2}([^\"]+?)\"{2}", "\"$1\"");

		// Step 3: Replace tabs with spaces and reduce multiple space

		return query;
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