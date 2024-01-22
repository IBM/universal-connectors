/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.intersystemsiris.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.parboiled.Action;
import org.parboiled.Node;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.support.ParsingResult;

@BuildParseTree
public class PegjsIrisParser extends BasePegjsParser {

	final static Set<String> KEYWORDS = new HashSet<>();
	static {


		KEYWORDS.add("ALTER");
        KEYWORDS.add("ALL");
        KEYWORDS.add("ADD");
        KEYWORDS.add("AND");
        KEYWORDS.add("AS");
        KEYWORDS.add("ASC");
        KEYWORDS.add("ANALYZE");
        KEYWORDS.add("ACCESSIBLE");
        KEYWORDS.add("ABSENT");
      

        KEYWORDS.add("BEFORE");
        KEYWORDS.add("BETWEEN");
        KEYWORDS.add("BIGINT");
        KEYWORDS.add("BLOB");
        KEYWORDS.add("BOTH");
        KEYWORDS.add("BY");
        KEYWORDS.add("BOOLEAN");

        KEYWORDS.add("CALL");
        KEYWORDS.add("CASCADE");
        KEYWORDS.add("CASE");
        KEYWORDS.add("CHECK");
        KEYWORDS.add("COLLATE");
        KEYWORDS.add("COLUMN");
        KEYWORDS.add("CONDITION");
        KEYWORDS.add("CONSTRAINT");
        KEYWORDS.add("CONTINUE");
        KEYWORDS.add("CREATE");
        KEYWORDS.add("CROSS");
        KEYWORDS.add("CURRENT_DATE");
        KEYWORDS.add("CURRENT_TIME");
        KEYWORDS.add("CURRENT_TIMESTAMP");
        KEYWORDS.add("CURRENT_USER");
        KEYWORDS.add("CURSOR");
        KEYWORDS.add("COUNT");
        

        KEYWORDS.add("DATABASE");
        KEYWORDS.add("DATABASES");
        KEYWORDS.add("DAY_HOUR");
        KEYWORDS.add("DAY_MICROSECOND");
        KEYWORDS.add("DAY_MINUTE");
        KEYWORDS.add("DAY_SECOND");
        KEYWORDS.add("DEC");
        KEYWORDS.add("DECIMAL");
        KEYWORDS.add("DECLARE");
        KEYWORDS.add("DEFAULT");
        KEYWORDS.add("DELAYED");
        KEYWORDS.add("DELETE");
        KEYWORDS.add("DESC");
        KEYWORDS.add("DESCRIBE");
        KEYWORDS.add("DETERMINISTIC");
        KEYWORDS.add("DISTINCT");
        KEYWORDS.add("DISTINCTROW");
        KEYWORDS.add("DIV");
        KEYWORDS.add("DROP");
        KEYWORDS.add("DOUBLE");
        KEYWORDS.add("DUAL");

        KEYWORDS.add("ELSE");
        KEYWORDS.add("EACH");
        KEYWORDS.add("ELSEIF");
        KEYWORDS.add("ENCLOSED");
        KEYWORDS.add("ESCAPED");
        KEYWORDS.add("EXCEPT");
        KEYWORDS.add("EXISTS");
        KEYWORDS.add("EXIT");
        KEYWORDS.add("EXPLAIN");

        KEYWORDS.add("FALSE");
        KEYWORDS.add("FULL");
        KEYWORDS.add("FROM");
        KEYWORDS.add("FETCH");
        KEYWORDS.add("FLOAT");
        KEYWORDS.add("FLOAT4");
        KEYWORDS.add("FLOAT8");
        KEYWORDS.add("FOR");
        KEYWORDS.add("FORCE");
        KEYWORDS.add("FOREIGN");
        KEYWORDS.add("FULLTEXT");
        KEYWORDS.add("FUNCTION");
        

        KEYWORDS.add("GENERATED");
        KEYWORDS.add("GET");
        KEYWORDS.add("GO");
        KEYWORDS.add("GRANT");
        KEYWORDS.add("GROUP");
        KEYWORDS.add("GROUPING");
        KEYWORDS.add("GROUPS");

        KEYWORDS.add("HAVING");
        KEYWORDS.add("HIGH_PRIORITY");
        KEYWORDS.add("HOUR_MICROSECOND");
        KEYWORDS.add("HOUR_MINUTE");
        KEYWORDS.add("HOUR_SECOND");

        KEYWORDS.add("IF");
        KEYWORDS.add("IGNORE");
        KEYWORDS.add("IN");
        KEYWORDS.add("INNER");
        KEYWORDS.add("INFILE");
        KEYWORDS.add("INLIST");
        KEYWORDS.add("INOUT");
        KEYWORDS.add("INSENSITIVE");
        KEYWORDS.add("INSERT");
        KEYWORDS.add("INTERSECT");
        KEYWORDS.add("INT");
        KEYWORDS.add("INT1");
        KEYWORDS.add("INT2");
        KEYWORDS.add("INT3");
        KEYWORDS.add("INT4");
        KEYWORDS.add("INT8");
        KEYWORDS.add("INTEGER");
        KEYWORDS.add("INTERVAL");
        KEYWORDS.add("INTO");
        KEYWORDS.add("IO_AFTER_GTIDS");
        KEYWORDS.add("IO_BEFORE_GTIDS");
        KEYWORDS.add("IS");
        KEYWORDS.add("ITERATE");

        KEYWORDS.add("JOIN");
        KEYWORDS.add("JSON_TABLE");
        KEYWORDS.add("JSON_ARRAY");
        KEYWORDS.add("JSON_OBJECT");
        
        
        KEYWORDS.add("KEY");
        KEYWORDS.add("KEYS");
        KEYWORDS.add("KILL");

 

        KEYWORDS.add("LATERAL");
        KEYWORDS.add("LEADING");
        KEYWORDS.add("LEAVE");
        KEYWORDS.add("LEFT");
        KEYWORDS.add("LIKE");
        KEYWORDS.add("LIMIT");
        KEYWORDS.add("LINEAR");
        KEYWORDS.add("LINES");
        KEYWORDS.add("LOAD");
        KEYWORDS.add("LOCALTIME");
        KEYWORDS.add("LOCALTIMESTAMP");
        KEYWORDS.add("LOCK");
        KEYWORDS.add("LONG");
        KEYWORDS.add("LONGBLOB");
        KEYWORDS.add("LONGTEXT");
        KEYWORDS.add("LOOP");
        KEYWORDS.add("LOW_PRIORITY");

 

        KEYWORDS.add("MASTER_BIND");
        KEYWORDS.add("MATCH");
        KEYWORDS.add("MAXVALUE");
        KEYWORDS.add("MEDIUMBLOB");
        KEYWORDS.add("MEDIUMINT");
        KEYWORDS.add("MEDIUMTEXT");
        KEYWORDS.add("MIDDLEINT");
        KEYWORDS.add("MINUTE_MICROSECOND");
        KEYWORDS.add("MINUTE_SECOND");
        KEYWORDS.add("MODIFIES");
        KEYWORDS.add("MATCHES");

 

        KEYWORDS.add("NATURAL");
        KEYWORDS.add("NOT");
        KEYWORDS.add("NO_WRITE_TO_BINLOG");
        KEYWORDS.add("NULL");
        KEYWORDS.add("NUMERIC");

 

        KEYWORDS.add("OF");
        KEYWORDS.add("ON");
        KEYWORDS.add("OPTIMIZE");
        KEYWORDS.add("OPTIMIZER_COSTS");
        KEYWORDS.add("OPTION");
        KEYWORDS.add("OPTIONALLY");
        KEYWORDS.add("OR");
        KEYWORDS.add("ORDER");
        KEYWORDS.add("OUT");
        KEYWORDS.add("OUTER");
        KEYWORDS.add("OUTFILE");
        KEYWORDS.add("OVER");

 

        KEYWORDS.add("PARTITION");
        KEYWORDS.add("PRECISION");
        KEYWORDS.add("PRIMARY");
        KEYWORDS.add("PROCEDURE");
        KEYWORDS.add("PURGE");
        KEYWORDS.add("PATTERN");

 

        KEYWORDS.add("RANGE");
        KEYWORDS.add("READ");
        KEYWORDS.add("READS");
        KEYWORDS.add("READ_WRITE");
        KEYWORDS.add("REAL");
        KEYWORDS.add("RECURSIVE");
        KEYWORDS.add("REFERENCES");
        KEYWORDS.add("REGEXP");
        KEYWORDS.add("RELEASE");
        KEYWORDS.add("RENAME");
        KEYWORDS.add("REQUIRE");
        KEYWORDS.add("RESIGNAL");
        KEYWORDS.add("RESTRICT");
        KEYWORDS.add("RETURN");
        KEYWORDS.add("REVOKE");
        KEYWORDS.add("RIGHT");
        KEYWORDS.add("RLIKE");
        KEYWORDS.add("ROW");
        KEYWORDS.add("ROWS");

 

        KEYWORDS.add("SCHEMAS");
        KEYWORDS.add("SELECT");
        KEYWORDS.add("SENSITIVE");
        KEYWORDS.add("SEPARATOR");
        KEYWORDS.add("SET");
        KEYWORDS.add("SHOW");
        KEYWORDS.add("SIGNAL");
        KEYWORDS.add("SMALLINT");
        KEYWORDS.add("SPATIAL");
        KEYWORDS.add("SPECIFIC");
        KEYWORDS.add("SQL");
        KEYWORDS.add("SQLEXCEPTION");
        KEYWORDS.add("SQLSTATE");
        KEYWORDS.add("SQLWARNING");
        KEYWORDS.add("SQL_BIG_RESULT");
        KEYWORDS.add("SQL_CALC_FOUND_ROWS");
        KEYWORDS.add("SQL_SMALL_RESULT");
        KEYWORDS.add("SSL");
        KEYWORDS.add("STARTING");
        KEYWORDS.add("STORED");
        KEYWORDS.add("STRAIGHT_JOIN");
        KEYWORDS.add("SUM");
        KEYWORDS.add("STARTSWITH");
        
 

        KEYWORDS.add("TABLE");
        KEYWORDS.add("TERMINATED");
        KEYWORDS.add("THEN");
        KEYWORDS.add("TINYBLOB");
        KEYWORDS.add("TINYINT");
        KEYWORDS.add("TINYTEXT");
        KEYWORDS.add("TO");
        KEYWORDS.add("TRAILING");
        KEYWORDS.add("TRIGGER");
        KEYWORDS.add("TRUE");

 

        KEYWORDS.add("UNION");
        KEYWORDS.add("UNIQUE");
        KEYWORDS.add("UNLOCK");
        KEYWORDS.add("UNSIGNED");
        KEYWORDS.add("UPDATE");
        KEYWORDS.add("USAGE");
        KEYWORDS.add("USE");
        KEYWORDS.add("USING");
        KEYWORDS.add("UTC_DATE");
        KEYWORDS.add("UTC_TIME");
        KEYWORDS.add("UTC_TIMESTAMP");
        

 

        KEYWORDS.add("VALUES");
        KEYWORDS.add("VARBINARY");
        KEYWORDS.add("VARCHAR");
        KEYWORDS.add("VARCHARACTER");
        KEYWORDS.add("VARYING");
        KEYWORDS.add("VIRTUAL");

 

        KEYWORDS.add("WHEN");
        KEYWORDS.add("WHERE");
        KEYWORDS.add("WHILE");
        KEYWORDS.add("WINDOW");
        KEYWORDS.add("WITH");
        KEYWORDS.add("WRITE");

 

        KEYWORDS.add("XOR");

 

        KEYWORDS.add("YEAR_MONTH");

 

        KEYWORDS.add("ZEROFILL");
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
		VERB_LABELS_MAP.put("drop_table_stmt", "DROP TABLE");
		VERB_LABELS_MAP.put("drop_view_stmt", "DROP VIEW");
		VERB_LABELS_MAP.put("drop_index_stmt", "DROP INDEX");
		VERB_LABELS_MAP.put("drop_database_stmt", "DROP DATABASE");
		VERB_LABELS_MAP.put("drop_schema_stmt", "DROP SCHEMA");
		VERB_LABELS_MAP.put("drop_trigger_stmt", "DROP TRIGGER");
		VERB_LABELS_MAP.put("drop_user_stmt", "DROP USER");
		VERB_LABELS_MAP.put("drop_role_stmt", "DROP ROLE");
		VERB_LABELS_MAP.put("drop_procedure_stmt", "DROP PROCEDURE");
		VERB_LABELS_MAP.put("drop_function_stmt", "DROP FUNCTION");
		VERB_LABELS_MAP.put("drop_aggregate_stmt", "DROP AGGREGATE");
		VERB_LABELS_MAP.put("truncate_stmt", "TRUNCATE TABLE");
		VERB_LABELS_MAP.put("rename_stmt", "RENAME");
		VERB_LABELS_MAP.put("use_stmt", "USE");
		VERB_LABELS_MAP.put("create_table_stmt", "CREATE TABLE");
		VERB_LABELS_MAP.put("create_temporary_table_stmt", "CREATE TEMPORARY TABLE");
		VERB_LABELS_MAP.put("create_global_temporary_table_stmt", "CREATE GLOBAL TEMPORARY TABLE");
		VERB_LABELS_MAP.put("create_user_stmt", "CREATE USER");
		VERB_LABELS_MAP.put("modify_user_stmt", "MODIFY USER");
		VERB_LABELS_MAP.put("delete_user_stmt", "DELETE USER");
		VERB_LABELS_MAP.put("create_role_stmt", "CREATE ROLE");
		VERB_LABELS_MAP.put("modify_role_stmt", "MODIFY ROLE");
		VERB_LABELS_MAP.put("delete_role_stmt", "DELETE ROLE");
		VERB_LABELS_MAP.put("create_resource_stmt", "CREATE RESOURCE");
		VERB_LABELS_MAP.put("modify_resource_stmt", "MODIFY RESOURCE");
		VERB_LABELS_MAP.put("delete_resource_stmt", "DELETE RESOURCE");
		VERB_LABELS_MAP.put("create_namespace_stmt", "CREATE SECTION NAMESPACE");
		VERB_LABELS_MAP.put("modify_namespace_stmt", "MODIFY SECTION NAMESPACE");
		VERB_LABELS_MAP.put("delete_namespace_stmt", "DELETE SECTION NAMESPACE");
		VERB_LABELS_MAP.put("create_section_db_stmt", "CREATE SECTION DATABASE");
		VERB_LABELS_MAP.put("modify_section_db_stmt", "MODIFY SECTION DATABASE");
		VERB_LABELS_MAP.put("delete_section_db_stmt", "DELETE SECTION DATABASE");
		VERB_LABELS_MAP.put("alter_table_stmt", "ALTER TABLE");
		VERB_LABELS_MAP.put("alter_view_stmt", "ALTER VIEW");
		VERB_LABELS_MAP.put("alter_user_stmt", "ALTER USER");
		VERB_LABELS_MAP.put("lock_table", "LOCK TABLE");
		VERB_LABELS_MAP.put("create_db_stmt", "CREATE DATABASE");
		VERB_LABELS_MAP.put("create_schema_stmt", "CREATE SCHEMA");
        VERB_LABELS_MAP.put("savepoint_stmt", "SAVEPOINT");
		VERB_LABELS_MAP.put("rollback_stmt", "ROLLBACK");
		VERB_LABELS_MAP.put("grant_stmt", "GRANT");
		VERB_LABELS_MAP.put("grant_privilege_stmt", "GRANT PRIVILEGE");
		VERB_LABELS_MAP.put("create_trigger_stmt", "CREATE TRIGGER");
		VERB_LABELS_MAP.put("create_or_replace_trigger_stmt", "CREATE OR REPLACE TRIGGER");
		VERB_LABELS_MAP.put("create_procedure_stmt", "CREATE PROCEDURE");
		VERB_LABELS_MAP.put("create_or_replace_procedure_stmt", "CREATE OR REPLACE PROCEDURE");
		VERB_LABELS_MAP.put("create_func_stmt", "CREATE FUNCTION");
		VERB_LABELS_MAP.put("create_aggregate_stmt", "CREATE AGGREGATE");
		VERB_LABELS_MAP.put("create_or_replace_aggregate_stmt", "CREATE OR REPLACE AGGREGATE");
		VERB_LABELS_MAP.put("load_data_stmt", "LOAD DATA");
		VERB_LABELS_MAP.put("lock_stmt", "LOCK TABLE");
		VERB_LABELS_MAP.put("unlock_stmt", "UNLOCK TABLE");
		VERB_LABELS_MAP.put("create_index_stmt","CREATE INDEX");
		VERB_LABELS_MAP.put("create_view_stmt","CREATE VIEW");
		VERB_LABELS_MAP.put("create_or_replace_view_stmt","CREATE OR REPLACE VIEW");
		VERB_LABELS_MAP.put("purge_queries_stmt", "PURGE QUERIES");
		VERB_LABELS_MAP.put("purge_cached_queries_stmt", "PURGE CACHED QUERIES");
		VERB_LABELS_MAP.put("create_model_stmt", "CREATE MODEL");
		VERB_LABELS_MAP.put("train_model_stmt", "TRAIN MODEL");
		VERB_LABELS_MAP.put("tune_table_stmt", "TUNE TABLE");
		VERB_LABELS_MAP.put("create_query_stmt", "CREATE QUERY");
		VERB_LABELS_MAP.put("create_or_replace_query_stmt", "CREATE OR REPLACE QUERY");
		VERB_LABELS_MAP.put("drop_query_stmt", "DROP QUERY");
		VERB_LABELS_MAP.put("validate_model_stmt", "VALIDATE MODEL");
		VERB_LABELS_MAP.put("alter_model_stmt", "ALTER MODEL");
		VERB_LABELS_MAP.put("purge_model_stmt", "PURGE");
		VERB_LABELS_MAP.put("drop_model_stmt", "DROP MODEL");
		VERB_LABELS_MAP.put("create_mlconfig_stmt", "CREATE ML CONFIGURATION");
		VERB_LABELS_MAP.put("create_or_replace_mlconfig_stmt", "CREATE OR REPLACE ML CONFIGURATION");
		VERB_LABELS_MAP.put("set_mlconfig_stmt", "SET ML CONFIGURATION");
		VERB_LABELS_MAP.put("alter_mlconfig_stmt", "ALTER ML CONFIGURATION");
		VERB_LABELS_MAP.put("drop_mlconfig_stmt", "DROP ML CONFIGURATION");
		VERB_LABELS_MAP.put("create_method_stmt", "CREATE METHOD");
		VERB_LABELS_MAP.put("drop_method_stmt", "DROP METHOD");
		VERB_LABELS_MAP.put("build_index_stmt", "BUILD INDEX");
		VERB_LABELS_MAP.put("create_foreignserver_stmt", "CREATE FOREIGN SERVER");
		VERB_LABELS_MAP.put("alter_foreignserver_stmt", "ALTER FOREIGN SERVER");
		VERB_LABELS_MAP.put("drop_foreignserver_stmt", "DROP FOREIGN SERVER");
		VERB_LABELS_MAP.put("alter_host_stmt", "ALTER HOST");
		VERB_LABELS_MAP.put("alter_connection_stmt", "ALTER CONNECTION");
		VERB_LABELS_MAP.put("modify_host_stmt", "MODIFY HOST");
		VERB_LABELS_MAP.put("modify_connection_stmt", "MODIFY CONNECTION");
		VERB_LABELS_MAP.put("create_foreigntable_stmt", "CREATE FOREIGN TABLE");
		VERB_LABELS_MAP.put("alter_foreigntable_stmt", "ALTER FOREIGN TABLE");
		VERB_LABELS_MAP.put("drop_foreigntable_stmt", "DROP FOREIGN TABLE");
		VERB_LABELS_MAP.put("insert_or_update_stmt", "INSERT OR UPDATE");
		VERB_LABELS_MAP.put("revoke_stmt", "REVOKE");
		VERB_LABELS_MAP.put("transaction_stmt", "START TRANSACTION");
		VERB_LABELS_MAP.put("set_transaction_stmt", "SET TRANSACTION");
		VERB_LABELS_MAP.put("commit_stmt", "COMMIT");
		VERB_LABELS_MAP.put("explain_stmt", "EXPLAIN");
		VERB_LABELS_MAP.put("freeze_stmt", "FREEZE PLANS");
		VERB_LABELS_MAP.put("unfreeze_stmt", "UNFREEZE PLANS");
		VERB_LABELS_MAP.put("intransaction_stmt", "INTRANSACTION");
		VERB_LABELS_MAP.put("intrans_stmt", "INTRANS");
		VERB_LABELS_MAP.put("call_func_stmt", "CALL");
		VERB_LABELS_MAP.put("set_option_stmt", "SET OPTION");
	}


	private static List<String> OBJECT_LABELS = new LinkedList<>();
	static {
		OBJECT_LABELS.add("table_name");
		OBJECT_LABELS.add("model_name");
		OBJECT_LABELS.add("method_name");
		OBJECT_LABELS.add("mlconfig_name");
		OBJECT_LABELS.add("query_name");
		OBJECT_LABELS.add("server_name");
		OBJECT_LABELS.add("class_name");
		OBJECT_LABELS.add("schema_name");
		OBJECT_LABELS.add("user_name");
		OBJECT_LABELS.add("role_name");
		OBJECT_LABELS.add("savepoint_name");
		OBJECT_LABELS.add("procedure_name");
		OBJECT_LABELS.add("func_name");
		OBJECT_LABELS.add("database_name");
		OBJECT_LABELS.add("trigger_name");
		OBJECT_LABELS.add("index_name");
		OBJECT_LABELS.add("grantor_name");
		OBJECT_LABELS.add("grantee_name");
		OBJECT_LABELS.add("resource_name");
		OBJECT_LABELS.add("namespace_name");
	}

	public static Map<String, String> getVerbLabels() {
		return Collections.unmodifiableMap(VERB_LABELS_MAP);
	}

	public static List<String> getObjectLabels() {
		return Collections.unmodifiableList(OBJECT_LABELS);
	}

	private static List<String> SKIP_LABELS = new LinkedList<>();
	static {

		SKIP_LABELS.add("crud_stmt");
		SKIP_LABELS.add("union_stmt");
		SKIP_LABELS.add("select_stmt");

		SKIP_LABELS.add("STAR");
	}

	private static List<String> SUPRESS_LABELS = new LinkedList<>();
	static {
		SUPRESS_LABELS.add("__");
		SUPRESS_LABELS.add("___");
		SUPRESS_LABELS.add("ident_start");
		SUPRESS_LABELS.add("ident_part");
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

	private static Rule startRule;

	@Override
	public Rule start() {
		if (startRule == null) {
			String pegjs = ParseUtils.read(PegjsIrisParser.class.getClassLoader().getResourceAsStream("iris.pegjs"));
			ParsingResult<?> result = ParseUtils.parse(pegjs, PegPegjsParser.class, false);
			startRule = start("start", result);
		}
		return startRule;
	}

	@Override
	protected Rule activateRule(String label, Rule rule) {
		if ("ident_name".equals(label) || "column_name".equals(label)) {
			rule = Sequence(rule, (Action<?>) context -> !KEYWORDS.contains(context.getMatch().toUpperCase()));
		}
		return rule;
	}

	protected Rule parseRule(ParsingResult<?> result, Node<?> parent) {
		Rule rule = super.parseRule(result, parent);

		if (rule != null) {
			String value = result.inputBuffer.extract(parent.getStartIndex(), parent.getEndIndex());
			if (SUPRESS_LABELS.contains(value)) {
				rule.suppressNode();
			} else if (value.startsWith("\"") || value.startsWith("'") || SUPRESS_SUB_LABELS.contains(value)) {
				rule.suppressSubnodes();
			} else if (SKIP_LABELS.contains(value)) {
				rule.skipNode();
			}
		}

		return rule;
	}

	public static ParsingResult<?> parse(String script) throws Exception {
		System.out.println("script : " + script);
		ParsingResult<?> result = ParseUtils.parse(script, PegjsIrisParser.class, false);
		ParseUtils.printTree(result);
		return result;
	}

	public static void main(String[] args) throws Exception {

		parse("SELECT * FROM employee");

	}

}