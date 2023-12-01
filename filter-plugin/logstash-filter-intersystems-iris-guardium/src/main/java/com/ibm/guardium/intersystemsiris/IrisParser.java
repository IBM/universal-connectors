/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.intersystemsiris;

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

import com.ibm.guardium.intersystemsiris.parser.ParseUtils;
import com.ibm.guardium.intersystemsiris.parser.PegjsIrisParser;

public class IrisParser {

	/**
	 * Method accepts a query as an argument and returns a listofmap containing objects and the verb
	 * 
	 * @param sql
	 * @return
	 */
	public static List<Map<String, Object>> parseSQL(String sql) {
		
		ParseRunner<?> runner = ParseUtils.createParseRunner(false, PegjsIrisParser.class);

		ParsingResult<?> result = runner.run(sql);
		List<Map<String, Object>> verbObjectMapList = null;
		
		if (result.parseErrors.size() == 0) {
			 verbObjectMapList = prepareVerbObjectMap(result);
			 verbObjectMapList.forEach(IrisParser::cleanVerbs);	
		}else {
			throw new RuntimeException("not matched : " + sql);
		}
		
		return verbObjectMapList;
		
	}

	private static Map<String, String> VERB_LABEL_MAPPING = PegjsIrisParser.getVerbLabels();
	private static List<String> OBJECT_LABELS = PegjsIrisParser.getObjectLabels();

	/**
	 * Method to prepare Object and Verb
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
			 String verb = VERB_LABEL_MAPPING.get(label);

			if (verb != null) {
				if (verbLevelStack.size() == 0) {
					Map<String, Object> verbObjectMap = new HashMap<>();
					verbObjectMap.put("verb", verb);
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

			} else if ("SEMICOLON".equals(label)) {
				verbLevelStack.clear();
				verbObjectMapStack.clear();
			}
			return true;
		});
		return verbObjectMapList;
	}
    
	/**
	 * Method to get nested object and verb
	 * 
	 * @param level
	 * @param verbLevelStack
	 * @param verbObjectMapStack
	 * @return
	 */
	public static Map<String, Object> getParentMap(Integer level, Stack<Integer> verbLevelStack, Stack<Map<String, Object>> verbObjectMapStack) {
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