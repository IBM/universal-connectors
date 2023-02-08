/*
© Copyright IBM Corp. 2021, 2022, 2023 All rights reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.parboiled.json;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.parboiled.Rule;
import org.parboiled.annotations.DontLabel;
import org.parboiled.matchers.ProxyMatcher;

public abstract class PegParser extends BaseJsonParser {

	protected Rule start(String start, JsonObject json) {
		parseRules(json.getJsonArray("rules"));
		Rule startRule = RULE_CACHE.get(start);
		initProxies();
		return startRule;
	}

	protected Rule[] parseRules(JsonArray jsonArr) {
		Rule[] childRules = new Rule[jsonArr.size()];
		for (int i = 0; i < jsonArr.size(); i++) {
			JsonValue jsonItem = jsonArr.get(i);
			childRules[i] = parseRule((JsonObject) jsonItem);
		}
		return childRules;
	}

	@DontLabel
	protected Rule parseRule(JsonObject jsonObj) {

		Rule rule = null;

		String type = jsonObj.getString("type");
		if ("rule".equals(type)) {

			String name = jsonObj.getString("name");
			JsonObject expr = jsonObj.getJsonObject("expression");
			rule = parseRule(expr);
			rule.label(name);
			RULE_CACHE.put(name, rule);

		} else if ("sequence".equals(type)) {

			JsonArray elems = jsonObj.getJsonArray("elements");
			rule = Sequence(parseRules(elems));

		} else if ("choice".equals(type)) {

			JsonArray alts = jsonObj.getJsonArray("alternatives");
			rule = FirstOf(parseRules(alts));

		} else if ("labeled".equals(type)) {

			JsonObject expr = jsonObj.getJsonObject("expression");
			rule = parseRule(expr);

		} else if ("rule_ref".equals(type)) {

			String proxyName = jsonObj.getString("name");
			ProxyMatcher proxy = new ProxyMatcher();
			List<ProxyMatcher> proxies = PROXY_CACHE.get(proxyName);
			if (proxies == null) {
				proxies = new ArrayList<>();
				PROXY_CACHE.put(proxyName, proxies);
			}
			proxies.add(proxy);
			rule = proxy;

		} else if ("optional".equals(type)) {

			JsonObject expr = jsonObj.getJsonObject("expression");
			Rule childRule = parseRule(expr);
			rule = Optional(childRule).skipNode();

		} else if ("semantic_and".equals(type) || "semantic_not".equals(type)) {
			rule = EMPTY;
		} else if ("simple_not".equals(type)) {

			JsonObject expr = jsonObj.getJsonObject("expression");
			rule = TestNot(parseRule(expr)).skipNode();

		} else if ("zero_or_more".equals(type)) {

			JsonObject expr = jsonObj.getJsonObject("expression");
			Rule childRule = parseRule(expr);
			rule = ZeroOrMore(childRule).skipNode();

		} else if ("one_or_more".equals(type)) {

			JsonObject expr = jsonObj.getJsonObject("expression");
			Rule childRule = parseRule(expr);
			rule = OneOrMore(childRule);

		} else if ("group".equals(type)) {

			JsonObject expr = jsonObj.getJsonObject("expression");
			rule = parseRule(expr).skipNode();

		} else if ("action".equals(type)) {

			JsonObject expr = jsonObj.getJsonObject("expression");
			rule = parseRule(expr);

		} else if ("literal".equals(type)) {

			String value = jsonObj.getString("value");
			boolean ic = jsonObj.getBoolean("ignoreCase");
			if (ic) {
				rule = IgnoreCase(value);
			} else {
				rule = String(value);
			}

		} else if ("class".equals(type)) {

			JsonArray parts = jsonObj.getJsonArray("parts");
			StringBuilder chars = new StringBuilder();

			for (int i = 0; i < parts.size(); i++) {
				JsonValue jsonItem = parts.get(i);
				if (jsonItem instanceof JsonString) {
					chars.append(((JsonString) jsonItem).getChars().charAt(0));
				} else {
					JsonArray range = (JsonArray) jsonItem;
					char start = ((JsonString) range.get(0)).getChars().charAt(0);
					char end = ((JsonString) range.get(1)).getChars().charAt(0);
					for (char j = start; j <= end; j++) {
						chars.append(j);
					}
				}
			}

			boolean inverted = jsonObj.getBoolean("inverted");
			if (inverted) {
				rule = NoneOf(chars.toString());
			} else {
				rule = AnyOf(chars.toString());
			}

		} else if ("any".equals(type)) {
			rule = ANY;

		} else {
			throw new RuntimeException("not handled : " + type + " : " + jsonObj);
		}

		return rule;

	}

}