/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.azure.cosmos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.parboiled.parserunners.ParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;
import com.ibm.guardium.azure.cosmos.parser.ParseUtils;
import com.ibm.guardium.azure.cosmos.parser.PegjsCosmosParser;



public class SQLParser {
    
	/**
	 * Method accepts a query as an argument and returns a listofmap containing objects and the verb
	 * 
	 * @param sql
	 * @return
	 */
	public static List<Map<String, Object>> parseSQL(String sql) {
		
		ParseRunner<?> runner = ParseUtils.createParseRunner(false, PegjsCosmosParser.class);

		ParsingResult<?> result = runner.run(sql);
		List<Map<String, Object>> verbObjectMapList = null;
		
		if (result.parseErrors.size() == 0) {
			 verbObjectMapList = prepareVerbObjectMap(result);
			 verbObjectMapList.forEach(SQLParser::cleanVerbs);	
		}else {
			throw new RuntimeException("not matched : " + sql);
		}
		
		return verbObjectMapList;
		
	}
	
	/**
	 * Method to prepare object and verb
	 * 
	 * @param result
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> prepareVerbObjectMap(ParsingResult<?> result) {

		List<Map<String, Object>> verbObjectMapList = new ArrayList<>();
		Stack<Integer> verbLevelStack = new Stack<>();
		Stack<Map<String, Object>> verbObjectMapStack = new Stack<>();

		ParseUtils.visitTree(result.parseTreeRoot, (node, level) -> {

			String label = node.getLabel();

			if ("select".equals(label)) {
				if (verbLevelStack.size() == 0) {
					Map<String, Object> verbObjectMap = new HashMap<>();
					verbObjectMap.put("verb", label);
					verbObjectMapStack.push(verbObjectMap);
					verbObjectMapList.add(verbObjectMap);
				} else {
					Map<String, Object> parentVerbObjectMap = getParentMap(level, verbLevelStack, verbObjectMapStack);
					List<Map<?, ?>> descMapList = (List<Map<?, ?>>) parentVerbObjectMap.get("descs");
					if (descMapList == null) {
						descMapList = new LinkedList<>();
						parentVerbObjectMap.put("descs", descMapList);
					}
					Map<String, Object> descMap = new HashMap<>();
					descMap.put("verb", label);
					verbObjectMapStack.push(descMap);
					descMapList.add(descMap);
				}
				verbLevelStack.push(level);

			} else if ("collection_primary_expression".equals(label) || "collection_member_expression".equals(label)) {
				String object = ParseTreeUtils.getNodeText(node, result.inputBuffer);

				Map<String, Object> parentVerbObjectMap = getParentMap(level, verbLevelStack, verbObjectMapStack);
				Set<String> objectList = (Set<String>) parentVerbObjectMap.get("objects");
				if (objectList == null) {
					objectList = new HashSet<>();
					parentVerbObjectMap.put("objects", objectList);
				}
				objectList.add(object);

			} else if ("SEMICOLON".equals(label)) {
				verbLevelStack.clear();
				verbObjectMapStack.clear();
			}
			return true;
		});
		return verbObjectMapList;
	}

	/**
	 * Method to get the parent verb and object
	 * 
	 * @param level
	 * @param verbLevelStack
	 * @param verbObjectMapStack
	 * @return
	 */
	public static Map<String, Object> getParentMap(Integer level, Stack<Integer> verbLevelStack,
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
    
	/**
	 * Method to clean descendants verb
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

}
