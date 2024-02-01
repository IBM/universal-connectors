/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.azure.cosmos.parser;

import java.util.LinkedList;
import java.util.List;

import org.parboiled.Node;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;


@BuildParseTree
public class PegjsCosmosParser extends BasePegjsParser {

	private static List<String> SKIP_LABELS = new LinkedList<>();
	static {
		SKIP_LABELS.add("select_specification");
		SKIP_LABELS.add("from_specification");

		SKIP_LABELS.add("object_property_list");

		SKIP_LABELS.add("scalar_expression");
		SKIP_LABELS.add("scalar_binary_or_expression");
		SKIP_LABELS.add("scalar_binary_and_expression");
		SKIP_LABELS.add("scalar_binary_equality_expression");
		SKIP_LABELS.add("scalar_binary_relational_expression");
		SKIP_LABELS.add("scalar_in_expression");
		SKIP_LABELS.add("scalar_between_expression");
		SKIP_LABELS.add("scalar_binary_bitwise_or_expression");
		SKIP_LABELS.add("scalar_binary_bitwise_xor_expression");
		SKIP_LABELS.add("scalar_binary_bitwise_and_expression");
		SKIP_LABELS.add("scalar_binary_shift_expression");
		SKIP_LABELS.add("scalar_binary_additive_expression");
		SKIP_LABELS.add("scalar_binary_multiplicative_expression");
		SKIP_LABELS.add("scalar_unary_expression");
	}

	private static List<String> SUPRESS_LABELS = new LinkedList<>();
	static {
		SUPRESS_LABELS.add("_");
	}

	private static List<String> SUPRESS_SUB_LABELS = new LinkedList<>();
	static {
		SUPRESS_SUB_LABELS.add("select");
		SUPRESS_SUB_LABELS.add("top");
		SUPRESS_SUB_LABELS.add("distinct");
		SUPRESS_SUB_LABELS.add("value");
		
		SUPRESS_SUB_LABELS.add("from");
		SUPRESS_SUB_LABELS.add("collection_primary_expression");
		SUPRESS_SUB_LABELS.add("collection_member_expression");
		SUPRESS_SUB_LABELS.add("as");

		SUPRESS_SUB_LABELS.add("join");
		SUPRESS_SUB_LABELS.add("in");

		SUPRESS_SUB_LABELS.add("where");
		SUPRESS_SUB_LABELS.add("exists");
		
		SUPRESS_SUB_LABELS.add("order");
		SUPRESS_SUB_LABELS.add("by");
		SUPRESS_SUB_LABELS.add("sort_specification");

		SUPRESS_SUB_LABELS.add("identifier");
		SUPRESS_SUB_LABELS.add("identifier_name");
		SUPRESS_SUB_LABELS.add("constant");
	}

	private static Rule startRule;

	@Override
	public Rule start() {
		if (startRule == null) {
			String pegjs = ParseUtils.read(PegjsCosmosParser.class.getClassLoader().getResourceAsStream("cosmos.pegjs"));
			ParsingResult<?> result = ParseUtils.parse(pegjs, PegPegjsParser.class, false);
			startRule = start("sql", result);
		}
		return startRule;
	}

	protected Rule parseRule(ParsingResult<?> result, Node<?> parent) {
		Rule rule = super.parseRule(result, parent);

		if (rule != null) {
			String value = result.inputBuffer.extract(parent.getStartIndex(), parent.getEndIndex());
			if (SUPRESS_LABELS.contains(value)) {
				rule.suppressNode();
			} else if (SUPRESS_SUB_LABELS.contains(value)) {
				rule.suppressSubnodes();
			} else if (SKIP_LABELS.contains(value)) {
				rule.skipNode();
			}
		}

		return rule;
	}

	public static ParsingResult<?> parse(String script) throws Exception {
		System.out.println("script : " + script);
		ParsingResult<?> result = ParseUtils.parse(script, PegjsCosmosParser.class, false);
//		ParsingResult<?> result = ParseUtils.parse(script, PegjsCosmosParser.class, true);
		String parseTreePrintOut = ParseTreeUtils.printNodeTree(result);
		System.out.println("tree : " + parseTreePrintOut);
		return result;
	}

	public static void main(String[] args) throws Exception {

//		parse("SELECT * FROM employee");
		
//		PegjsCosmosParserTest.main(args);
		
	}

}
