/*
Copyright IBM Corp. 2021, 2022, 2023 All rights reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.parboiled.util;

import java.util.function.BiFunction;

import org.parboiled.Node;
import org.parboiled.Parboiled;
import org.parboiled.json.BaseJsonParser;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.TracingParseRunner;

public class ParseUtils {

	public static ParseRunner<?> createParseRunner(boolean trace, Class<? extends BaseJsonParser> parserClass) {
		BaseJsonParser parser = Parboiled.createParser(parserClass);
		ParseRunner<?> runner;
		if (trace) {
			runner = new TracingParseRunner<>(parser.start());
		} else {
			runner = new BasicParseRunner<>(parser.start());
		}
		return runner;
	}

	public static void visitTree(Node<?> node, BiFunction<Node<?>, Integer, ?> visitor) {
		visitTree(1, node, visitor);
	}

	public static void visitTree(int level, Node<?> node, BiFunction<Node<?>, Integer, ?> visitor) {
		visitor.apply(node, level);
		level++;
		for (Node<?> sub : node.getChildren()) {
			visitTree(level, sub, visitor);
		}
	}

}