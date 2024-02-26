/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.intersystemsiris.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.DontLabel;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.ProxyMatcher;

public abstract class BaseJsonParser extends BaseParser<Object> {

	protected Map<String, Rule> RULE_CACHE = new HashMap<>();
	protected Map<String, List<ProxyMatcher>> PROXY_CACHE = new HashMap<>();

	@DontLabel
	protected Rule createProxy(String proxyName) {
		ProxyMatcher proxy = new ProxyMatcher();
		List<ProxyMatcher> proxies = PROXY_CACHE.get(proxyName);
		if (proxies == null) {
			proxies = new ArrayList<>();
			PROXY_CACHE.put(proxyName, proxies);
		}
		proxies.add(proxy);
		return proxy;
	}
	
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