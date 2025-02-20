/*
Copyright IBM Corp. 2021, 2025 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.bigquery;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;

import org.parboiled.Action;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.DontLabel;
import org.parboiled.json.PegParser;

@BuildParseTree
public class PegBigQueryParser extends PegParser {
	final static Set<String> KEYWORDS = new HashSet<>();
	static {

		KEYWORDS.add("ARRAY");
		KEYWORDS.add("ALTER");
		KEYWORDS.add("ALL");
		KEYWORDS.add("ADD");
		KEYWORDS.add("AND");
		KEYWORDS.add("AS");
		KEYWORDS.add("ASC");
		KEYWORDS.add("BETWEEN");
		KEYWORDS.add("BY");
		KEYWORDS.add("CALL");
		KEYWORDS.add("CASE");
		KEYWORDS.add("CREATE");
		KEYWORDS.add("CROSS");
		KEYWORDS.add("CONTAINS");
		KEYWORDS.add("CURRENT_DATE");
		KEYWORDS.add("CURRENT_TIME");
		KEYWORDS.add("CURRENT_TIMESTAMP");
		KEYWORDS.add("CURRENT_USER");
		KEYWORDS.add("DELETE");
		KEYWORDS.add("DESC");
		KEYWORDS.add("DISTINCT");
		KEYWORDS.add("DROP");
		KEYWORDS.add("ELSE");
		KEYWORDS.add("END");
		KEYWORDS.add("EXCEPT");
		KEYWORDS.add("EXISTS");
		KEYWORDS.add("EXPLAIN");
		KEYWORDS.add("FALSE");
		KEYWORDS.add("FROM");
		KEYWORDS.add("FULL");
		KEYWORDS.add("FOR");
		KEYWORDS.add("GROUP");
		KEYWORDS.add("HAVING");
		KEYWORDS.add("IN");
		KEYWORDS.add("INNER");
		KEYWORDS.add("INSERT");
		KEYWORDS.add("INTO");
		KEYWORDS.add("INTERSECT");
		KEYWORDS.add("IS");
		KEYWORDS.add("JOIN");
		KEYWORDS.add("JSON");
		KEYWORDS.add("KEY");
		KEYWORDS.add("LEFT");
		KEYWORDS.add("LIKE");
		KEYWORDS.add("LIMIT");
		KEYWORDS.add("LOW_PRIORITY");
		KEYWORDS.add("NOT");
		KEYWORDS.add("NULL");
		KEYWORDS.add("ON");
		KEYWORDS.add("OR");
		KEYWORDS.add("ORDER");
		KEYWORDS.add("OUTER");
		KEYWORDS.add("PARTITION");
		KEYWORDS.add("PIVOT");
		KEYWORDS.add("RECURSIVE");
		KEYWORDS.add("RENAME");
		KEYWORDS.add("READ");
		KEYWORDS.add("RIGHT");
		KEYWORDS.add("SELECT");
		KEYWORDS.add("SESSION_USER");
		KEYWORDS.add("SET");
		KEYWORDS.add("SHOW");
		KEYWORDS.add("SYSTEM_USER");
		KEYWORDS.add("TABLE");
		KEYWORDS.add("THEN");
		KEYWORDS.add("TRUE");
		KEYWORDS.add("TRUNCATE");
		KEYWORDS.add("TYPE");
		KEYWORDS.add("UNION");
		KEYWORDS.add("UPDATE");
		KEYWORDS.add("USING");
		KEYWORDS.add("VALUES");
		KEYWORDS.add("WINDOW");
		KEYWORDS.add("WITH");
		KEYWORDS.add("WHEN");
		KEYWORDS.add("WHERE");
		KEYWORDS.add("WRITE");
		KEYWORDS.add("GLOBAL");
		KEYWORDS.add("SESSION");
		KEYWORDS.add("LOCAL");
		KEYWORDS.add("PERSIST");
		KEYWORDS.add("PERSIST_ONLY");
		KEYWORDS.add("UNNEST");

		// DATA TYPES
		KEYWORDS.add("BOOL");
		KEYWORDS.add("BYTE");
		KEYWORDS.add("DATE");
		KEYWORDS.add("DATETIME");
		KEYWORDS.add("FLOAT64");
		KEYWORDS.add("INT64");
		KEYWORDS.add("NUMERIC");
		KEYWORDS.add("STRING");
		KEYWORDS.add("TIME");
		KEYWORDS.add("TIMESTAMP");
		KEYWORDS.add("ARRAY");
		KEYWORDS.add("STRUCT");
	}

	private static Map<String, String> VERB_LABELS_MAP = new HashMap<>();
	static {

		VERB_LABELS_MAP.put("with_clause", "WITH");
		VERB_LABELS_MAP.put("select_stmt_nake", "SELECT");
		VERB_LABELS_MAP.put("update_stmt", "UPDATE");
		VERB_LABELS_MAP.put("delete_stmt", "DELETE");

		VERB_LABELS_MAP.put("replace_insert_stmt", "INSERT");
		VERB_LABELS_MAP.put("insert_no_columns_stmt", "INSERT");
		VERB_LABELS_MAP.put("insert_into_set", "INSERT");

		VERB_LABELS_MAP.put("analyze_stmt", "ANALYZE");
		VERB_LABELS_MAP.put("attach_stmt", "ATTACH DATABASE");
		VERB_LABELS_MAP.put("drop_stmt", "DROP");
		VERB_LABELS_MAP.put("truncate_stmt", "TRUNCATE");
		VERB_LABELS_MAP.put("rename_stmt", "RENAME");
		VERB_LABELS_MAP.put("use_stmt", "USE");

		VERB_LABELS_MAP.put("create_table_stmt", "CREATE TABLE");
		VERB_LABELS_MAP.put("alter_table_stmt", "ALTER TABLE");
		VERB_LABELS_MAP.put("lock_table", "LOCK TABLE");

		VERB_LABELS_MAP.put("create_db_stmt", "CREATE SCHEMA");

	}

	private static List<String> OBJECT_LABELS = new LinkedList<>();
	static {
		OBJECT_LABELS.add("table_name");
		OBJECT_LABELS.add("cte_name");
	}

	public static Map<String, String> getVerbLabels() {
		return Collections.unmodifiableMap(VERB_LABELS_MAP);
	}

	public static List<String> getObjectLabels() {
		return Collections.unmodifiableList(OBJECT_LABELS);
	}

	private static List<String> SUPRESS_LABELS = new LinkedList<>();
	static {
		SUPRESS_LABELS.add("__");
		SUPRESS_LABELS.add("___");
		SUPRESS_LABELS.add("ident_start");
		SUPRESS_LABELS.add("ident_part");
		SUPRESS_LABELS.add("column_start");
		SUPRESS_LABELS.add("column_part");
		SUPRESS_LABELS.add("digit");
	}

	private static List<String> SUPRESS_SUB_LABELS = new LinkedList<>();
	static {
		SUPRESS_SUB_LABELS.add("literal_string");
		SUPRESS_SUB_LABELS.add("single_quoted_ident");
		SUPRESS_SUB_LABELS.add("backticks_quoted_ident");
		SUPRESS_SUB_LABELS.add("double_quoted_ident");
		SUPRESS_SUB_LABELS.add("table_name");
		SUPRESS_SUB_LABELS.add("column_name");
	}

	static Rule startRule;

	public Rule start() {
		if (startRule == null) {
			InputStream resourceAsStream = PegBigQueryParser.class.getClassLoader()
					.getResourceAsStream("bigquery.peg.json");
			JsonObject json = Json.createReader(resourceAsStream).readObject();
			startRule = super.start("start", json);
		}
		return startRule;
	}

	@DontLabel
	protected Rule parseRule(JsonObject jsonObj) {

		Rule rule = super.parseRule(jsonObj);

		String type = jsonObj.getString("type");
		if ("rule".equals(type)) {

			String name = jsonObj.getString("name");
			if ("ident_name".equals(name) || "column_name".equals(name)) {
				rule = Sequence(rule, (Action<?>) context -> !KEYWORDS.contains(context.getMatch().toUpperCase()));
				RULE_CACHE.put(name, rule);
			}

		} else if ("rule_ref".equals(type)) {

			String name = jsonObj.getString("name");
			if (SUPRESS_LABELS.contains(name)) {
				rule.suppressNode();
			} else if (name.startsWith("KW") || SUPRESS_SUB_LABELS.contains(name)) {
				rule.suppressSubnodes();
			}

		}

		return rule;
	}

}