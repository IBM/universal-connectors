/*
© Copyright IBM Corp. 2021, 2022, 2023 All rights reserved.

SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.spanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	 * 
	 */
	private static String customDDlRegex = "((?i)create\\s*table)\\s+([\\w]+)|((?i)drop\\s*table)\\s+([\\w]+)|((?i)alter\\s*table)\\s+([\\w]+)|((?i)create\\s*or\\s*replace\\s*table)\\s+([\\w]+)|((?i)create\\s*role)\\s+([\\w]+)|((?i)drop\\s*role)\\s+([\\w]+)|((?i)alter\\s*role)\\s+([\\w]+)|(\\w+\\s+(?i)index)\\s+([\\w]+)|(\\w+\\s+(?i)view)\\s+([\\w]+)|(\\w+\\s+\\w+\\s+\\w+\\s+(?i)view)\\s+([\\w]+)|(\\w+\\s+(?i)database)\\s+([\\w-]+)|(\\w+\\s+(?i)model)\\s+([\\w]+)|(\\w+\\s+\\w+\\s+\\w+\\s+(?i)model)\\s+([\\w]+)|((?i)grant)\\s+\\w+[(\\s\\w,]*?(?:[\\(\\s\\w\\s,\\)])*[\\s,]*?\\s*\\w+[(\\s\\w\\)]*?\\w+[)]?\\s*\\w*\\s*(?:(?i)table)\\s+([\\,\\w\\s]*)?(?:(?i)to\\s*role)|((?i)grant\\s*role)\\s+([\\w]+)|((?i)revoke)\\s+\\w+[(\\s\\w,]*?(?:[\\(\\s\\w\\s,\\)])*[\\s,]*?\\s*\\w+[(\\s\\w\\)]*?\\w+[)]?\\s*\\w*\\s*(?:(?i)table)\\s+([\\,\\w\\s]*)?(?:(?i)from\\s*role)|((?i)revoke\\s*role)\\s+([\\w]+)";

	/**
	 * To ignore TIMESTAMP AND UNNEST as an object because these are keywords in
	 * SQL.
	 * 
	 */
	private static String customSelectRegex = "(?i)FROM\\s*(?!(?i)\\s+(TIMESTAMP\\s+|UNNEST))\\s+(\\w+)";

	/**
	 * DDL queries and its Optional parts as UNIQUE or NULL_FILTERED are not
	 * supported Due to parser limitations,Hence handling in regex to get object and
	 * verb values
	 * 
	 */
	private static String replaceDDLRegex = "((?i)UNIQUE)|((?i)NULL_FILTERED)";

	private static Pattern customDDlRegexPattern = Pattern.compile(customDDlRegex);
	private static Pattern customSelectRegexPattern = Pattern.compile(customSelectRegex);
	private static Pattern replaceAllRegexPattern = Pattern.compile(replaceDDLRegex);

	/**
	 * This method accepts a query as an argument and returns a map containing
	 * objects and the verb that populates sentenceobject.ï¿½
	 * 
	 * @param sql
	 * @return Map<String, Object> map
	 *
	 */
	public static Map<String, Object> runJSEngine(String sql) throws Exception {

		Map<String, Object> map = new HashMap<>();
		List<Object> actionLst = new ArrayList<>();
		List<Object> objList = new ArrayList<>();
		List<String> objVerbLst;

		try {

			map = parseSQL(sql);
			if (map.isEmpty()) {
				objVerbLst = ddLParser(sql);
				map = objVerbLst.isEmpty() ? getMap(sql, map, actionLst, objList) : getObjectVerb(objVerbLst);
			}
		} catch (Exception e) {
			// In case of parser failure we are checking with DDL regular expression.
			objVerbLst = ddLParser(sql);
			map = objVerbLst.isEmpty() ? getMap(sql, map, actionLst, objList) : getObjectVerb(objVerbLst);
		}
		return map;
	}

	/**
	 * This method takes the query as a parameter and returns the map with object
	 * and verb values if the parser successfully parses the query.
	 * 
	 * @param sql
	 * @return Map<String, Object> objVerbMap
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseSQL(String sql) throws Exception {
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
		sql = sql.replaceAll("\\\\n", "\n").replaceAll("\\\\r", "\r").replaceAll("\\\\t", "\t").replaceAll("\\\\|@p",
				" ");

		Map<String, Object> objVerbMap = new HashMap<>();
		List<Object> actionLst = new ArrayList<>();
		List<Object> objList = new ArrayList<>();

		if (sql.toLowerCase().trim().startsWith(ApplicationConstants.CREATE)
				|| sql.toLowerCase().trim().startsWith(ApplicationConstants.DROP)
				|| sql.toLowerCase().trim().startsWith(ApplicationConstants.ALTER)) {
			return objVerbMap;
		}

		ParseRunner<?> runner = ParseUtils.createParseRunner(false, PegBigQueryParser.class);
		ParsingResult<?> result = runner.run(sql);
		Object objectcheck;
		Object verbcheck;

		if (result.parseErrors.size() == 0) {
			Map<?, ?> verbObjectMap = prepareVerbObjectMap(result);
			verbObjectMap = cleanVerbs(verbObjectMap);

			if (!verbObjectMap.isEmpty()) {
				verbcheck = verbObjectMap.get(ApplicationConstants.VERB);
				verbcheck = verbcheck != null ? verbcheck : StringUtils.EMPTY;
				actionLst.add(verbcheck);
				objectcheck = verbObjectMap.get(ApplicationConstants.OBJECTS);
				objList.add(objectcheck != null ? ((HashSet<Object>) extractObject(objectcheck)) : new HashSet<>());
				if (verbObjectMap.get(ApplicationConstants.DESCENDANTS) != null) {
					recursiveDescendentCheck(actionLst, objList,
							(LinkedList<Object>) verbObjectMap.get(ApplicationConstants.DESCENDANTS));
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
	 * This method checks Object and Verb recursively .
	 * 
	 * @param actionLst - List having parent verb.
	 * @param objList   - List is having parent object.
	 */
	@SuppressWarnings("unchecked")
	private static void recursiveDescendentCheck(List<Object> actionLst, List<Object> objList,
			LinkedList<Object> argDescendentList) {
		if (argDescendentList != null) {
			for (Object object : argDescendentList) {
				Map<String, Object> map = (Map<String, Object>) object;
				actionLst.add(map.get(ApplicationConstants.VERB) != null ? map.get(ApplicationConstants.VERB)
						: StringUtils.EMPTY);
				objList.add(map.get(ApplicationConstants.OBJECTS) != null
						? ((HashSet<Object>) extractObject(map.get(ApplicationConstants.OBJECTS)))
						: new HashSet<>());
				if (map.get(ApplicationConstants.DESCENDANTS) != null) {
					recursiveDescendentCheck(actionLst, objList,
							(LinkedList<Object>) (map.get(ApplicationConstants.DESCENDANTS)));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static HashSet<Object> extractObject(Object objectcheck) {
		HashSet<Object> hashSet = new HashSet<>();
		for (Object ob : (HashSet<Object>) objectcheck) {
			ob = ob.toString().replaceAll("`", StringUtils.EMPTY).trim();
			if (ob.toString().contains(".")) {
				String[] split = ob.toString().split("\\.");
				hashSet.add(split[split.length - 1]);
			} else
				hashSet.add(ob);
		}
		return hashSet;
	}

	/**
	 * This method takes List<String> as input and extracts object & verb value from
	 * objVerbLst and returns map
	 * 
	 * @param objVerbLst
	 * @return Map<String, Object> map
	 */

	private static Map<String, Object> getObjectVerb(List<String> objVerbLst) {
		Map<String, Object> map = new HashMap<>();
		List<Object> actionLst = new ArrayList<>();
		List<Object> objList = new ArrayList<>();
		for (int i = 0; i < objVerbLst.size(); i++) {
			if (i % 2 != 0) {
				objList.add(Collections.singleton(objVerbLst.get(i)));
			} else {
				actionLst.add(objVerbLst.get(i).toLowerCase());
			}
		}
		map.put(ApplicationConstants.VERBS, actionLst);
		map.put(ApplicationConstants.OBJECTS, objList);
		return map;
	}

	/**
	 * This method takes query as a parameter and extracts the object value for the
	 * query, which the parser is unable to parse for select (DML) queries.
	 * 
	 * @param sql
	 * @return String object
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
	 * This method takes query as input and extracts the object & verb value from
	 * DDL regex pattern as parser doesn't support for DDL queries
	 * 
	 * @param sqlQuery
	 * @return List<String> lst
	 */
	private static List<String> ddLParser(String sqlQuery) {
		List<String> lst = new ArrayList<>();
		String[] split = sqlQuery.split(";");
		for (int j = 0; j < split.length; j++) {
			final Matcher matcher = replaceAllRegexPattern.matcher(split[j]);
			final String sql = matcher.replaceAll(StringUtils.EMPTY);
			final Matcher matcher2 = customDDlRegexPattern.matcher(sql.replaceAll("`", StringUtils.EMPTY).trim());
			while (matcher2.find()) {
				for (int i = 1; i <= matcher2.groupCount(); i++) {
					if (matcher2.group(i) != null) {
						String[] group = matcher2.group(i).split(",");
						List<String> newlst = new ArrayList<>();
						if (group.length > 1) {
							int size = lst.size();
							String verb = lst.get(size - 1);
							for (int k = 0; k < group.length; k++) {
								newlst.add(group[k].trim());
							}
							if (!newlst.isEmpty()) {
								for (int i1 = 0; i1 < newlst.size(); i1++) {
									if (i1 % 2 != 0) {
										lst.add(verb);
										lst.add(newlst.get(i1));
									} else {
										if (i1 == 0) {
											lst.add(newlst.get(i1));
										} else {
											lst.add(verb);
											lst.add(newlst.get(i1));
										}
									}
								}

							}

						} else {
							lst.add(matcher2.group(i).trim());
						}
					}
				}
			}
		}
		return lst;

	}

	/**
	 * This method calls customSelectParser and extracts the object value for the
	 * query, which the parser is unable to parse for select (DML) queries.
	 * 
	 * @param sql, map, verblist, objectlist
	 * @return map
	 */

	private static Map<String, Object> getMap(String sql, Map<String, Object> map, List<Object> actionLst,
			List<Object> objList) {
		String obj = customSelectParser(sql);
		if (!obj.isEmpty()) {
			objList.add(Collections.singleton(customSelectParser(sql)));
			map.put(ApplicationConstants.OBJECTS, objList);
		} else {
			map.put(ApplicationConstants.OBJECTS, Arrays.asList());
		}
		actionLst.add(ApplicationConstants.SELECT.toLowerCase());
		map.put(ApplicationConstants.VERBS, actionLst);
		return map;

	}

	/**
	 * This method cleans the verbs not having object.
	 *
	 * @param verbObjectMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Map<?, ?> cleanVerbs(Map<?, ?> verbObjectMap) {

		List<Map<?, ?>> descMapList = (List<Map<?, ?>>) verbObjectMap.get(ApplicationConstants.DESCENDANTS);
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
			verbObjectMap.remove(ApplicationConstants.DESCENDANTS);
			Set<String> objectList = (Set<String>) verbObjectMap.get(ApplicationConstants.OBJECTS);
			if (objectList == null) {
				return null;
			}
		}
		return verbObjectMap;
	}

	/**
	 * This method prepares the map of object and verb with descendants.
	 * 
	 * @param result
	 * @return verbObjectMap
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
					verbObjectMap.put(ApplicationConstants.VERB, verb.toLowerCase());
					verbObjectMapStack.push(verbObjectMap);
				} else {
					Map<String, Object> parentVerbObjectMap = getParentMap(level, verbLevelStack, verbObjectMapStack);
					List<Map<?, ?>> descMapList = (List<Map<?, ?>>) parentVerbObjectMap
							.get(ApplicationConstants.DESCENDANTS);
					if (descMapList == null) {
						descMapList = new LinkedList<>();
						parentVerbObjectMap.put(ApplicationConstants.DESCENDANTS, descMapList);
					}
					Map<String, Object> descMap = new HashMap<>();
					descMap.put(ApplicationConstants.VERB, verb.toLowerCase());
					verbObjectMapStack.push(descMap);
					descMapList.add(descMap);
				}
				verbLevelStack.push(level);

			} else if (OBJECT_LABELS.contains(label)) {
				String object = ParseTreeUtils.getNodeText(node, result.inputBuffer);

				Map<String, Object> parentVerbObjectMap = getParentMap(level, verbLevelStack, verbObjectMapStack);
				Set<String> objectList = (Set<String>) parentVerbObjectMap.get(ApplicationConstants.OBJECTS);
				if (objectList == null) {
					objectList = new HashSet<>();
					parentVerbObjectMap.put(ApplicationConstants.OBJECTS, objectList);
				}
				objectList.add(object);

			}
			return true;
		});
		return verbObjectMap;
	}

	/**
	 * Method to prepare Parent and descendant relationship in the map. This method
	 * uses stack for maintaining nested descendant.
	 * 
	 * @param level
	 * @param verbLevelStack
	 * @param verbObjectMapStack
	 * @return parentVerbObjectMap
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
