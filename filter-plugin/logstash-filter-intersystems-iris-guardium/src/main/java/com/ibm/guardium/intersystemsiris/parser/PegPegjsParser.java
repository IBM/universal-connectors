/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.intersystemsiris.parser;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.DontLabel;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;

@BuildParseTree
public class PegPegjsParser extends PegParser {

	private static List<String> HIDE_LABELS = new LinkedList<>();
	static {
		HIDE_LABELS.add("start");

		HIDE_LABELS.add("OneOrMore");
		HIDE_LABELS.add("FirstOf");
		HIDE_LABELS.add("Sequence");

		HIDE_LABELS.add("WhiteSpace");
		HIDE_LABELS.add("LineTerminatorSequence");
		HIDE_LABELS.add("EOS");
	}

	private static List<String> SUPRESS_SUB_LABELS = new LinkedList<>();
	static {

		SUPRESS_SUB_LABELS.add("WhiteSpace");
		SUPRESS_SUB_LABELS.add("LineTerminatorSequence");
		SUPRESS_SUB_LABELS.add("Comment");
		SUPRESS_SUB_LABELS.add("CodeBlock");

		SUPRESS_SUB_LABELS.add("IdentifierName");

		SUPRESS_SUB_LABELS.add("ClassCharacterRange");
		SUPRESS_SUB_LABELS.add("ClassCharacter");
		SUPRESS_SUB_LABELS.add("StringLiteral");
	}

	private static Rule startRule;

	@Override
	public Rule start() {
		if (startRule == null) {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream("pegjs.peg.json");
			JsonObject json = Json.createReader(inputStream).readObject();
			startRule = start("Grammar", json);
		}
		return startRule;
	}

	@DontLabel
	protected Rule parseRule(JsonObject jsonObj) {
		Rule rule = super.parseRule(jsonObj);
		String type = jsonObj.getString("type");
		if ("rule_ref".equals(type)) {
			String name = jsonObj.getString("name");
			if (SUPRESS_SUB_LABELS.contains(name)) {
				rule.suppressSubnodes();
			}

		}

		return rule;
	}

	public static ParsingResult<?> parse(String input) throws Exception {
		return ParseUtils.parse(input, PegPegjsParser.class, false);
	}
}