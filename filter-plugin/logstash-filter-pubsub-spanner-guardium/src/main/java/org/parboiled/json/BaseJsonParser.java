/*
© Copyright IBM Corp. 2021, 2022, 2023 All rights reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.parboiled.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.ProxyMatcher;

public abstract class BaseJsonParser extends BaseParser<Object> {

	protected Map<String, Rule> RULE_CACHE = new HashMap<>();
	protected Map<String, List<ProxyMatcher>> PROXY_CACHE = new HashMap<>();

	protected void initProxies() {
		PROXY_CACHE.entrySet().forEach(e -> e.getValue().forEach(p -> {
			p.label(e.getKey());
			p.arm((Matcher) RULE_CACHE.get(e.getKey()));
		}));
		PROXY_CACHE.clear();
		RULE_CACHE.clear();
	}

	public abstract Rule start();

}