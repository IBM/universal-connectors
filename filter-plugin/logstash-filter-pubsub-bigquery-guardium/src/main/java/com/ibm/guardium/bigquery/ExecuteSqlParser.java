/*
Copyright IBM Corp. 2021, 2025 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.bigquery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;
import org.parboiled.util.ParseUtils;

public class ExecuteSqlParser {

	private static Map<String, String> VERB_LABEL_MAPPING = PegBigQueryParser.getVerbLabels();
	private static List<String> OBJECT_LABELS = PegBigQueryParser.getObjectLabels();

	/**
	 * DDL queries are not supported Due to parser limitations,Hence handling in
	 * regex to get object and verb values
         * For Example
	 * Create Table -
	 *	Query -	CREATE TABLE Example ( x INT64, y STRING);
	 *	Output - Object -> Example
    	 *		 Verb   -> CREATE TABLE
	 *  Create Function -
	 *	Query - CREATE FUNCTION `bigdataset.other_function`(x INT64, y INT64)  AS (x * y * 2);
	 *	Output - Object -> other_function
    	 *	 	Verb    -> CREATE FUNCTION
	 *  Create View -
	 *	Query - CREATE VIEW `mydataset.new_view` AS WITH RECURSIVE T1 AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM T1 WHERE n < 3) SELECT * FROM T1
	 *	Output - Object -> new_view
    	 *		 Verb   -> CREATE VIEW
	 *  Create View -
	 *	Query -	CREATE PROCEDURE mdataset.SelectFromTablesAndAppend(target_date DATE, OUT rows_added INT64) BEGIN .....
	 *	Output - Object -> SelectFromTablesAndAppend
    	 *		 Verb   -> CREATE PROCEDURE
	 * 
	 */
	final static String customDDlRegex = "(\\w+\\s+(?i)table)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)table)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)function)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)function)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)snapshot\\s+table)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)snapshot\\s+table)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)MATERIALIZED\\s+VIEW)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)MATERIALIZED\\s+VIEW)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)VIEW)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)VIEW)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)procedure)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)procedure)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)schema)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)schema)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)search\\s+index)\\s+([\\w-]+[\\w-]+)|(\\w+\\s+(?i)table\\s+function)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)table\\s+function)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)external\\s+table)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)external\\s+table)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)capacity)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)capacity)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)ASSIGNMENT)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)ASSIGNMENT)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)RESERVATION)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)RESERVATION)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)schema)\\s+([\\w]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+TABLE)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+TABLE)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+VIEW)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+VIEW)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+MATERIALIZED\\s+view)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+MATERIALIZED\\s+view)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+TABLE\\s+FUNCTION)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+TABLE\\s+FUNCTION)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+FUNCTION)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+FUNCTION)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+PROCEDURE)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+PROCEDURE)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+EXTERNAL\\s+TABLE)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+EXTERNAL\\s+TABLE)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)TEMP\\s+TABLE)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)TEMP\\s+TABLE)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)TEMP\\s+FUNCTION)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)TEMP\\s+FUNCTION)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)TEMPORARY\\s+TABLE)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)TEMPORARY\\s+TABLE)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)TEMPORARY\\s+FUNCTION)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)TEMPORARY\\s+FUNCTION)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+)|(\\w+\\s+(?i)TEMP\\s+TABLE)\\s+([\\w-]+)|(\\w+\\s+(?i)TEMPORARY\\s+TABLE)\\s+([\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+TEMP\\s+TABLE)\\s+([\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+TEMPORARY\\s+TABLE)\\s+([\\w-]+)|(\\w+\\s+(?i)TEMP\\s+FUNCTION)\\s+([\\w-]+)|(\\w+\\s+(?i)TEMPORARY\\s+FUNCTION)\\s+([\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+TEMP\\s+FUNCTION)\\s+([\\w-]+)|(\\w+\\s+(?i)OR\\s+REPLACE\\s+TEMPORARY\\s+FUNCTION)\\s+([\\w-]+)|(^WITH\\s+)(\\w+[a-zA-Z0-9.]*\\s+)(?:AS\\s*\\()(\\s*\\w+\\s+)(?:.*\\s+)(?:FROM\\s+)(\\w+[a-zA-Z0-9.]*)";

	final static Pattern customDDlRegexPattern = Pattern.compile(customDDlRegex);
	/**
	 * DCL queries are not supported Due to parser limitations,Hence handling in
	 * regex to get object and verb values
	 * 
	 */
	final static String grantRevokeRegex = "((?i)grant)\\s+(\\w+\\/\\w+\\.?\\w+\\s+\\w+\\s+\\w+)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|((?i)revoke)\\s+(\\w+\\/\\w+\\.?\\w+\\s+\\w+\\s+\\w+)\\s+([\\w-]+\\s*\\.\\s*[\\w-]+\\s*\\.\\s*[\\w-]+)|((?i)GRANT)\\s+(\\w+\\/\\w+\\.?\\w+\\s+\\w+\\s+\\w+)\\s+((\\w+\\-\\w+\\s*\\.)?\\s*\\w+)|((?i)GRANT)\\s+(\\w+\\/\\w+\\.?\\w+\\s+\\w+\\s+\\w+)\\s+((\\w+\\s*\\.)?\\s*\\w+)|((?i)REVOKE)\\s+(\\w+\\/\\w+\\.?\\w+\\s+\\w+\\s+\\w+)\\s+((\\w+\\-\\w+\\s*\\.)?\\s*\\w+)|((?i)REVOKE)\\s+(\\w+\\/\\w+\\.?\\w+\\s+\\w+\\s+\\w+)\\s+((\\w+\\s*\\.)?\\s*\\w+)";
	final static Pattern customGrantRevokeRegex = Pattern.compile(grantRevokeRegex);
	
	/**
	 * To ignore TIMESTAMP and UNNEST from object map as these are keywords in SQL.
	 * 
	 */
	//(?i)FROM\s*(?!(?i)\s+(\bTIMESTAMP\b\s*|\bUNNEST\b))\s+(\w+)
	final static String customSelectRegex = "(?i)FROM\\s*(?!(?i)\\s+(\\bTIMESTAMP\\b\\s*|\\bUNNEST\\b))\\s+([a-zA-z0-9.-]*)";
	final static Pattern customSelectRegexPattern = Pattern.compile(customSelectRegex);
	/**
	 * For all DDL queries, If exists and if not exist are optional,Hence handling
	 * regex to replace with an empty string is necessary
	 * 
	 */
	final static String testRegex = "((?i)if\\s+exists)|((?i)if\\s+(?i)not\\s+exists)";
	final static Pattern customTestPattern = Pattern.compile(testRegex);

	/**
	 * MERGE queries are not supported Due to parser limitations,Hence handling in
	 * regex to get object and verb values
	 * 
	 */
	final static String mergeregex = "(?i)(MERGE)\\s+(?:INTO)?\\s*((?:\\S+\\s*\\.)?(?:\\s*\\S+\\s*\\.)?\\s*\\w+)\\s+(?:AS)?\\s*(?:\\w+\\s+)?USING\\s+((?:\\S+\\s*\\.)?(?:\\s*\\S+\\s*\\.)?\\s*\\w+|[(]\\s*SELECT)|(^WITH\\s+)(\\w+\\s+)(?:AS\\s*)|(\\s*SELECT\\s+)(?:.*FROM)(\\s+\\w+)";
	final static Pattern customMergeRegex = Pattern.compile(mergeregex);

	/**
	 * Method to initiate parsing of SQL queries. This method will be using parser
	 * and regular expression to fetch object and verb from a SQL query.
	 * 
	 * @param sql
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> runJSEngine(String sql) {
		Map<String, Object> map = new HashMap<>();
		List<String> objVerbLst;
		try {
			// Check for merge query.
			if (sql.toUpperCase().contains(ApplicationConstants.MERGE)) {
				map = customMergeParser(sql);
				return map;
			} else if (sql.toUpperCase().contains(ApplicationConstants.GRANT) // Checking Grand/Revoke query
					|| sql.toUpperCase().contains(ApplicationConstants.REVOKE)) {
				map = customGrantRevokeParser(sql);
				return map;
			} else {
				// Replacing RECURSIVE keyword with space as this is optional in with query.
				if ( !sql.isEmpty() && sql.matches("((?i)WITH\\s+RECURSIVE[\\s\\n\\r\\S]*)")) {
					sql = sql.replaceAll("((?i)RECURSIVE)", " ");
				}
				map = parseSQL(sql);
				if (map.isEmpty() || (map.get("objects") != null && map.get("objects").toString().isEmpty())
						|| sql.matches("((?i)CREATE\\s+TABLE\\s+.*)")) {
					objVerbLst = ddLParser(sql);
					map = objVerbLst.isEmpty() ? getMap(sql) : getObjectVerb(objVerbLst);
				} else {
					map.put("objects", (List<HashSet<String>>) map.get("objects"));
				}
			}
			return map;
		} catch (Exception ex) {
			// In case of parser failure we are checking with DDL regular expression.
			objVerbLst = ddLParser(sql);
			return map = objVerbLst.isEmpty() ? getMap(sql) : getObjectVerb(objVerbLst);
		}
	}

	/**
	 * Utility method to prepare final map having object and verb.
	 * 
	 * @param sql
	 * @return
	 */
	private static Map<String, Object> getMap(final String sql) {
		final Map<String, Object> map = new HashMap<>();
		final List<String> actionLst = new ArrayList<>();
		final List<String> objList = new ArrayList<>();
		String obj = customSelectParser(sql);
		if (!obj.isEmpty()) {
			objList.add(customSelectParser(sql));
			map.put(ApplicationConstants.OBJECTS, objList);
		}
		actionLst.add(ApplicationConstants.SELECT);
		map.put(ApplicationConstants.VERBS, actionLst);
		return map;

	}

	/**
	 * Method to parse Grant/Revoke queries using regular expression..
	 * 
	 * @param sql
	 * @return
	 */
	private static Map<String, Object> customGrantRevokeParser(String sql) {
		String sqlQuery = sql.replaceAll(Pattern.quote("`"), "");
		sqlQuery = CommonUtils.removeEscapeSequence(sqlQuery);
		Map<String, Object> map = new HashMap<>();
		List<String> verbLst = new ArrayList<>();
		List<String> objLst = new ArrayList<>();
		final Matcher matcher = customGrantRevokeRegex.matcher(sqlQuery);
		while (matcher.find()) {
			matcher.group(0).toString();
			for (int i = 1; i <= matcher.groupCount(); i++) {
					if (matcher.group(i) != null) {
						verbLst.add(matcher.group(i));
						objLst.add(matcher.group(i + 2).trim());
						break;
					}
			}
			map.put(ApplicationConstants.VERBS, verbLst);
			map.put(ApplicationConstants.OBJECTS, objLst);
		}
		return map;
	}

	/**
	 * Method to parse Merge queries using regular expression.
	 * 
	 * @param sql
	 * @return
	 */
	private static Map<String, Object> customMergeParser(String sql) {
		String sqlQuery1 = sql.replaceAll(Pattern.quote("`"), "");
		String sqlQuery = CommonUtils.removeEscapeSequence(sqlQuery1);
		String commonInputs = "INSERT,UPDATE,DELETE";
		Map<String, Object> map = new HashMap<>();
		List<String> verbLst = new ArrayList<>();
		List<String> objLst = new ArrayList<>();
		final Matcher matcher = customMergeRegex.matcher(sqlQuery);
		while (matcher.find()) {
			matcher.group(0).toString();
			for (int i = 1; i <= matcher.groupCount(); i++) {
				if (i % 10 != 0) {
					if (matcher.group(i) != null) {
						String group = matcher.group(i + 1);
						if(matcher.groupCount()<i+2) {
							break;
						}
						String group1 = matcher.group(i + 2);
						List<String> collect = Arrays.asList(StringUtils.split(commonInputs, ",")).stream()
								.filter(s -> StringUtils.containsIgnoreCase(sqlQuery, s)).collect(Collectors.toList());
						if (matcher.group(i + 2).contains(ApplicationConstants.OPENB) && collect.isEmpty()) {
							verbLst.add(matcher.group(i));
							objLst.add(group.trim());
						} else if (collect.isEmpty()) {
							verbLst.add(matcher.group(i));
							verbLst.add(matcher.group(i));
							objLst.add(group.trim());
							objLst.add(group1.trim());
						} else if (matcher.group(i + 2).contains(ApplicationConstants.OPENB)) {
							verbLst.add(matcher.group(i));
							verbLst.addAll(collect);
							objLst.add(group.trim());
							collect.stream().forEach(v -> objLst.add(group.trim()));
						} else {
							verbLst.add(matcher.group(i));
							verbLst.add(matcher.group(i));
							verbLst.addAll(collect);
							objLst.add(group.trim());
							objLst.add(group1.trim());
							collect.stream().forEach(v -> objLst.add(group.trim()));
						}
						break;
					}
				}
			}
			map.put(ApplicationConstants.VERBS, verbLst);
			map.put(ApplicationConstants.OBJECTS, objLst);
		}
		return map;
	}

	/**
	 * Method to prepare map of object and verb.
	 * 
	 * @param objVerbLst
	 * @return
	 */
	private static Map<String, Object> getObjectVerb(List<String> objVerbLst) {
		Map<String, Object> map = new HashMap<>();
		List<String> actionLst = new ArrayList<>();
		List<String> objList = new ArrayList<>();
		for (int i = 0; i < objVerbLst.size(); i++) {
			if (i % 2 != 0) {
				objList.add(objVerbLst.get(i).toString().trim());
			} else {
				actionLst.add(objVerbLst.get(i).replaceAll("\\s+", " "));
			}
		}
		map.put(ApplicationConstants.VERBS, actionLst);
		map.put(ApplicationConstants.OBJECTS, objList);
		return map;
	}

	/**
	 * Method to ignore Timestamp and UNNEST selection as object using regular
	 * expression..
	 * 
	 * @param sql
	 * @return
	 */
	private static String customSelectParser(String sql) {
		String object = StringUtils.EMPTY;
		final Matcher matcher = customSelectRegexPattern.matcher(sql);
		while (matcher.find()) {
			object = matcher.group(2).toString();
		}
		return object;
	}

	/**
	 * Method to parse DDL queries using regular expression.
	 * 
	 * @param sqlQuery
	 * @return
	 */
	private static List<String> ddLParser(String sqlQuery) {
		String sql = sqlQuery.replaceAll(Pattern.quote("`"), "");
		sql = CommonUtils.removeEscapeSequence(sql);
		final Matcher matcher2 = customTestPattern.matcher(sql);
		sql = matcher2.replaceAll(StringUtils.EMPTY).trim();
		final Matcher matcher = customDDlRegexPattern.matcher(sql);
		List<String> lst = new ArrayList<>();
		while (matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				if (matcher.group(i) != null) {
					lst.add(matcher.group(i));
				}
			}
		}
		return lst;
	}

	/**
	 * Method to parse SQL using bigquery parser.
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseSQL(String sql) throws Exception {

		sql = handleSQLComment(sql);

		sql = CommonUtils.replaceNewLineChars(sql);
		ParseRunner<?> runner = ParseUtils.createParseRunner(false, PegBigQueryParser.class);
		ParsingResult<?> result = runner.run(sql);
		Map<String, Object> objVerbMap = new HashMap<>();
		List<Object> actionLst = new ArrayList<>();
		List<Object> objList = new ArrayList<>();
		Object objectcheck;
		Object verbcheck;
		if (result.parseErrors.size() == 0) {
			Map<?, ?> verbObjectMap = prepareVerbObjectMap(result);
			verbObjectMap = cleanVerbs(verbObjectMap);
			if (!verbObjectMap.isEmpty()) {
				verbcheck = verbObjectMap.get("verb");
				verbcheck = verbcheck != null ? verbcheck : StringUtils.EMPTY;
				if (verbcheck.toString().equalsIgnoreCase(ApplicationConstants.CREATE)
						|| verbcheck.toString().equalsIgnoreCase(ApplicationConstants.DROP)
						|| verbcheck.toString().equalsIgnoreCase(ApplicationConstants.ALTER)) {
					return objVerbMap;
				}
				actionLst.add(verbcheck);
				objectcheck = verbObjectMap.get("objects");
				objList.add(objectcheck != null ? ((HashSet<Object>) objectcheck) : new HashSet<>());
				if (verbObjectMap.get("descs") != null) {
					recursiveDesendentheck(actionLst, objList, (LinkedList<Object>) verbObjectMap.get("descs"));
				}
				objVerbMap.put(ApplicationConstants.VERBS, actionLst);
				objVerbMap.put(ApplicationConstants.OBJECTS, objList);
			}
		} else {
			throw new RuntimeException("not matched : " + sql);
		}
		return objVerbMap;
	}

	/**
	 * Method to handle comment '--' in sql string by adding new line '\n\ char in
	 * it.
	 * 
	 * @param sql
	 * @return
	 */
	private static String handleSQLComment(String sql) {
		if (sql.contains("--")) {
			char[] ch = sql.toCharArray();
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < ch.length; i++) {
				if (ch[i] == '\\') {
					if (i + 1 < ch.length) {
						if (ch[i + 1] == 'n' || ch[i + 1] == 'r' || ch[i + 1] == 't') {
							result.append('\n');
							i++;
						}
					} else
						result.append(' ');

				} else
					result.append(ch[i]);
			}
			sql = result.toString();
		}
		return sql;
	}

	/**
	 * Method to check Object and Verb recursively .
	 * 
	 * @param actionLst - List having parent verb.
	 * @param objList   - List is having parent object.
	 */
	@SuppressWarnings("unchecked")
	private static void recursiveDesendentheck(List<Object> actionLst, List<Object> objList,
			LinkedList<Object> argDecendenttList) {
		if (argDecendenttList != null) {
			for (Object object : argDecendenttList) {
				Map<String, Object> map = (Map<String, Object>) object;
				actionLst.add(map.get("verb") != null ? map.get("verb") : StringUtils.EMPTY);
				objList.add(map.get("objects") != null ? ((HashSet<Object>) map.get("objects")) : new HashSet<>());
				if (map.get("descs") != null) {
					recursiveDesendentheck(actionLst, objList, (LinkedList<Object>) (map.get("descs")));
				}
			}
		}
	}

	/**
	 * Method to clean verbs for which objects are not available in parsed result.
	 * 
	 * @param verbObjectMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Map<?, ?> cleanVerbs(Map<?, ?> verbObjectMap) {

		List<Map<?, ?>> descMapList = (List<Map<?, ?>>) verbObjectMap.get("descs");
		if (descMapList != null) {
			for (Iterator<?> iterator = descMapList.iterator(); iterator.hasNext();) {
				Map<?, ?> descMap = (Map<?, ?>) iterator.next();

				descMap = cleanVerbs(descMap);
				if (descMap == null) {
					iterator.remove();
				}

			}
		}
		if (descMapList == null || descMapList.isEmpty()) {
			verbObjectMap.remove("descs");
			Set<String> objectList = (Set<String>) verbObjectMap.get("objects");
			if (objectList == null) {
				return null;
			}
		}

		return verbObjectMap;

	}

	/**
	 * Method to prepare object and verb from parsed tree.
	 * 
	 * @param result
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> prepareVerbObjectMap(ParsingResult<?> result) {

		Map<String, Object> verbObjectMap = new HashMap<>();

		Stack<Map<String, Object>> verbObjectMapStack = new Stack<>();
		Stack<Integer> verbLevelStack = new Stack<>();

		ParseUtils.visitTree(result.parseTreeRoot, (node, level) -> {

			String label = node.getLabel();
			String verb = VERB_LABEL_MAPPING.get(label);

			if (verb != null) {
				if (verbLevelStack.size() == 0) {
					verbObjectMap.put("verb", verb);
					verbObjectMapStack.push(verbObjectMap);
				} else {
					Map<String, Object> parentVerbObjectMap = getParentMap(level, verbLevelStack, verbObjectMapStack);
					List<Map<?, ?>> descMapList = (List<Map<?, ?>>) parentVerbObjectMap.get("descs");
					if (descMapList == null) {
						descMapList = new LinkedList<>();
						parentVerbObjectMap.put("descs", descMapList);
					}
					Map<String, Object> descMap = new HashMap<>();
					descMap.put("verb", verb);
					verbObjectMapStack.push(descMap);
					descMapList.add(descMap);
				}
				verbLevelStack.push(level);

			} else if (OBJECT_LABELS.contains(label)) {
				String object = ParseTreeUtils.getNodeText(node, result.inputBuffer);

				Map<String, Object> parentVerbObjectMap = getParentMap(level, verbLevelStack, verbObjectMapStack);
				Set<String> objectList = (Set<String>) parentVerbObjectMap.get("objects");
				if (objectList == null) {
					objectList = new HashSet<>();
					parentVerbObjectMap.put("objects", objectList);
				}
				objectList.add(object);

			}
			return true;
		});
		return verbObjectMap;
	}
	   

	/**
	 * Method to prepare Parent and descendant relationship in the map. 
	 * This method use stack for maintaining nested descendant.
	 * @param level
	 * @param verbLevelStack
	 * @param verbObjectMapStack
	 * @return
	 */
	private static Map<String, Object> getParentMap(Integer level, Stack<Integer> verbLevelStack,
			Stack<Map<String, Object>> verbObjectMapStack) {
		int parentVerbLevel = verbLevelStack.peek();
		Map<String, Object> parentVerbObjectMap = verbObjectMapStack.peek();

		if (level < parentVerbLevel) {
			while (level < parentVerbLevel) {
				parentVerbLevel = verbLevelStack.pop();
				parentVerbObjectMap = verbObjectMapStack.pop();
			}
			verbLevelStack.push(parentVerbLevel);
			verbObjectMapStack.push(parentVerbObjectMap);
		}
		return parentVerbObjectMap;
	}

}
