/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.azure.cosmos.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.BiFunction;

import org.parboiled.Node;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParsingResult;

public class ParseUtils {

    public static String read(InputStream inputStream) {
    	StringBuilder sb=new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"))) {
            while (reader.ready()) {
                String line = reader.readLine();
                sb.append(line).append("\n");
            }
            return sb.toString(); 
            } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
	}

    public static ParsingResult<?> parse(String input, Class<? extends BaseJsonParser> parserClass, boolean trace) {
		ParseRunner<?> runner = ParseUtils.createParseRunner(trace, parserClass);
		ParsingResult<?> result = runner.run(input);
		return result;
	}

    public static void visitLeafs(Node<?> node, BiFunction<Node<?>, Integer, Boolean> fn) {
		visitLeafs(node, 0, fn);
	}

	public static void visitLeafs(Node<?> node, int level, BiFunction<Node<?>, Integer, Boolean> fn) {
		if (node.getChildren().isEmpty()) {
			fn.apply(node, level);
			return;
		}
		level++;
		for (Node<?> sub : node.getChildren()) {
			visitLeafs(sub, level, fn);
		}
	}

	public static String indent(int level) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < level; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}

	public static void printNode(ParsingResult<?> result, Node<?> node, int level) {
		System.out.println();
		System.out.print(ParseUtils.indent(level));
		System.out.print(node.getLabel());
		String value = result.inputBuffer.extract(node.getStartIndex(), node.getEndIndex());
		System.out.print(" : " + value);
	}
	
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
	
	public static void visitTree(Node<?> node, BiFunction<Node<?>, Integer, Boolean> visitor) {
		visitTree(node, 1, visitor);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void visitTree(Node<?> node, int level, BiFunction<Node<?>, Integer, Boolean> visitor) {
		boolean proceed = visitor.apply(node, level);
		level++;
		if (proceed) {
			visitTree((List) node.getChildren(), level, visitor);
		}
	}

	public static void visitTree(List<Node<?>> children, int level, BiFunction<Node<?>, Integer, Boolean> visitor) {
		for (Node<?> sub : children) {
			visitTree(sub, level, visitor);
		}
	}

	public static void applyChildren(List<Node<?>> children, int level, BiFunction<Node<?>, Integer, Boolean> fn) {
		for (Node<?> sub : children) {
			fn.apply(sub, level);
		}
	}


}
