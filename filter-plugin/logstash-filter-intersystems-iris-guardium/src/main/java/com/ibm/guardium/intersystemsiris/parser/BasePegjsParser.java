/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.intersystemsiris.parser;

import java.util.LinkedList;
import java.util.List;

import org.parboiled.Node;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.DontLabel;
import org.parboiled.support.ParsingResult;

@BuildParseTree
public abstract class BasePegjsParser extends BaseJsonParser {
	
	final static List<String> HIDE_LABELS = new LinkedList<>();
	static {
		HIDE_LABELS.add("start");
		HIDE_LABELS.add("OneOrMore");
		HIDE_LABELS.add("Sequence");
		HIDE_LABELS.add("FirstOf");

		HIDE_LABELS.add("WhiteSpace");
		HIDE_LABELS.add("LineTerminatorSequence");
		HIDE_LABELS.add("Comment");
		HIDE_LABELS.add("EOS");
		HIDE_LABELS.add("Initializer");
		HIDE_LABELS.add("CodeBlock");

		HIDE_LABELS.add("Rule");
		HIDE_LABELS.add("ChoiceExpression");
		HIDE_LABELS.add("ActionExpression");
		HIDE_LABELS.add("SequenceExpression");
		HIDE_LABELS.add("LabeledExpression");
		HIDE_LABELS.add("LabelColon");
		HIDE_LABELS.add("PrefixedExpression");
		HIDE_LABELS.add("SuffixedExpression");
		HIDE_LABELS.add("PrimaryExpression");
		HIDE_LABELS.add("LiteralMatcher");
		HIDE_LABELS.add("CharacterClassMatcher");
		HIDE_LABELS.add("AnyMatcher");
		HIDE_LABELS.add("RuleReferenceExpression");
	}

	public Rule start(String start, ParsingResult<?> result) {
		parseRule(result, result.parseTreeRoot);
		Rule startRule = RULE_CACHE.get(start);
		initProxies();
		return startRule;
	}
	
	@DontLabel
	protected Rule activateRule(String label, Rule rule) {
		return rule;
	}

	protected int level;

	@DontLabel
	@SuppressWarnings({ "rawtypes" })
	protected Rule parseRule(ParsingResult<?> result, Node<?> parent) {
		Rule rule = null;
		level++;

		if (!HIDE_LABELS.contains(parent.getLabel()) && !parent.getLabel().startsWith("'")) {
			ParseUtils.printNode(result, parent, level);
		}

		if ("Rule".equals(parent.getLabel())) {

			String label = null;
			Rule expression = null;
			for (Node child : parent.getChildren()) {
				if ("IdentifierName".equals(child.getLabel())) {
					label = result.inputBuffer.extract(child.getStartIndex(), child.getEndIndex());
				} else if ("ChoiceExpression".equals(child.getLabel())) {
					expression = parseRule(result, child);
				}
			}

			rule = activateRule(label, expression);
			RULE_CACHE.put(label, rule);
		}

		else if ("ChoiceExpression".equals(parent.getLabel())) {
			rule = FirstOf(parseRules(result, parent.getChildren()));
		}

		else if ("SequenceExpression".equals(parent.getLabel())) {
			rule = Sequence(parseRules(result, parent.getChildren()));
		}

		else if ("LabelColon".equals(parent.getLabel())) {
			// NoOps
		}

		else if ("PrefixedExpression".equals(parent.getLabel())) {

			Node<?> firstChild = parent.getChildren().get(0);
			if ("SuffixedExpression".equals(firstChild.getLabel())) {
				rule = parseRule(result, firstChild);
			} else {
				char prefix = 0;
				Rule expression = null;
				for (Node child : firstChild.getChildren()) {
					if ("PrefixedOperator".equals(child.getLabel())) {
						prefix = result.inputBuffer.charAt(child.getStartIndex());
					} else if ("SuffixedExpression".equals(child.getLabel())) {
						expression = parseRule(result, child);
					}
				}

				rule = prefixExpression(prefix, expression);
			}

		} else if ("SuffixedExpression".equals(parent.getLabel())) {

			Node<?> firstChild = parent.getChildren().get(0);
			if ("PrimaryExpression".equals(firstChild.getLabel())) {
				rule = parseRule(result, firstChild);
			} else {
				char suffix = 0;
				Rule expression = null;
				for (Node child : firstChild.getChildren()) {
					if ("SuffixedOperator".equals(child.getLabel())) {
						suffix = result.inputBuffer.charAt(child.getStartIndex());
					} else if ("PrimaryExpression".equals(child.getLabel())) {
						expression = parseRule(result, child);
					}
				}

				rule = suffixExpression(suffix, expression);
			}

		}

		else if ("LiteralMatcher".equals(parent.getLabel())) {
			boolean ic = false;
			String value = null;
			for (Node child : parent.getChildren()) {
				if ("StringLiteral".equals(child.getLabel())) {
					value = result.inputBuffer.extract(child.getStartIndex() + 1, child.getEndIndex() - 1);
				} else if ("'i'".equals(child.getLabel())) {
					ic = true;
				}
			}

			if (ic) {
				rule = IgnoreCase(value);
			} else {
				rule = String(value);
			}
		}

		else if ("CharacterClassMatcher".equals(parent.getLabel())) {

			boolean invert = false;
			StringBuilder chars = new StringBuilder();

			for (Node child : parent.getChildren()) {

				if ("FirstOf".equals(child.getLabel())) {
					String value = result.inputBuffer.extract(child.getStartIndex(), child.getEndIndex());
					if (value.length() < 3) {
						char char0 = value.charAt(0);
						if (char0 == '\\') {
							chars.append(escapeChar(value.charAt(1)));
						} else {
							chars.append(char0);
						}
					} else {
						char start = value.charAt(0);
						char end = value.charAt(2);
						for (char j = start; j <= end; j++) {
							chars.append(j);
						}
					}
				} else if ("'^'".equals(child.getLabel())) {
					invert = true;
				} else if ("'i'".equals(child.getLabel())) {
					// TODO handle ignore case
				}
			}

			if (invert) {
				rule = NoneOf(chars.toString()).skipNode();
			} else {
				rule = AnyOf(chars.toString()).skipNode();
			}
		}

		else if ("AnyMatcher".equals(parent.getLabel())) {
			rule = ANY;
		}

		else if ("RuleReferenceExpression".equals(parent.getLabel())) {
			String proxyName = null;
			for (Node child : parent.getChildren()) {
				if ("IdentifierName".equals(child.getLabel())) {
					proxyName = result.inputBuffer.extract(child.getStartIndex(), child.getEndIndex());
				}
			}
			rule = createProxy(proxyName);
		}

		else {

			if (!parent.getChildren().isEmpty()) {
				Rule[] childRules = parseRules(result, parent.getChildren());
				if (childRules.length == 1) {
					rule = childRules[0];
				}
			}

		}

		if (!HIDE_LABELS.contains(parent.getLabel()) && !parent.getLabel().startsWith("'")) {
			System.out.println(ParseUtils.indent(level) + " : " + rule);
		}

		level--;
		return rule;

	}

	@DontLabel
	protected char escapeChar(char char1) {
		switch (char1) {
		case 'b':
			return '\b';
		case 'f':
			return '\f';
		case 'n':
			return '\n';
		case 'r':
			return '\r';
		case 't':
			return '\t';
		default:
			return char1;
		}
	}

	@DontLabel
	protected Rule prefixExpression(char prefix, Rule expression) {
		switch (prefix) {
		case '&':
			return Test(expression);
		case '!':
			return TestNot(expression);
		default:
			return expression;
		}
	}

	@DontLabel
	protected Rule suffixExpression(char suffix, Rule expression) {
		switch (suffix) {
		case '?':
			return Optional(expression);
		case '*':
			return ZeroOrMore(expression);
		case '+':
			return OneOrMore(expression);
		default:
			return expression;
		}
	}

	protected Rule[] parseRules(ParsingResult<?> result, List<?> children) {
		List<Rule> childRules = new LinkedList<>();
		for (Object sub : children) {
			Rule childRule = parseRule(result, (Node<?>) sub);
			if (childRule != null) {
				childRules.add(childRule);
			}
		}
		return childRules.toArray(new Rule[0]);
	}

}