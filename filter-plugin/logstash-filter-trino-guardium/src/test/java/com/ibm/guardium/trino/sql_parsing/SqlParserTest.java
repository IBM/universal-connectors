package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SqlParserTest {
  SqlParser sqlParser = new SqlParser();

  @Test
  void testGrant() {
    String grant;
    Data data;
    int i = 0;

    grant = "INSERT INTO table (ID, FIRST_NAME, LAST_NAME) VALUES ('5', 'Steve', 'Jobs')";
    data = sqlParser.parseStatement(grant);
    assertNotNull(data.getConstruct());

    grant = "show grant on DATABASE `hive_metastore`.`default`";
    data = sqlParser.parseStatement(grant);
    assertNotNull(data);
    assertEquals("SHOW GRANT", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "`hive_metastore`.`default`",
        data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("DATABASE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);

    i = 0;
    grant = "GRANT SELECT ON employees TO admin_role;";
    data = sqlParser.parseStatement(grant);
    assertNotNull(data);
    assertEquals(grant, data.getOriginalSqlCommand());
    assertEquals("GRANT", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("employees", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("DB_OBJECT", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("SELECT", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("admin_role", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

    grant = "GRANT SELECT, INSERT ON employees TO john, manager_role;";
    data = sqlParser.parseStatement(grant);
    assertNotNull(data);
    assertEquals(grant, data.getOriginalSqlCommand());
    assertEquals("GRANT", data.getConstruct().getSentences().get(0).getVerb());
    i = 0;
    assertEquals("employees", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("DB_OBJECT", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("SELECT", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("INSERT", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("john", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals(
        "manager_role", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

    grant =
        "GRANT SELECT (employee_id, first_name, last_name) ON employees TO hr_role WITH GRANT OPTION;";
    data = sqlParser.parseStatement(grant);
    assertNotNull(data);
    assertEquals(grant, data.getOriginalSqlCommand());
    assertEquals("GRANT", data.getConstruct().getSentences().get(0).getVerb());
    i = 0;
    assertEquals("employees", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("DB_OBJECT", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
    assertEquals(
        3, data.getConstruct().getSentences().get(0).getObjects().get(i++).getFields().length);
    assertEquals("SELECT", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("hr_role", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

    grant =
        "GRANT UPDATE, INSERT (salary, bonus) ON employees, DEPARTMENTS TO payroll_role, hr_role WITH GRANT OPTION;";
    data = sqlParser.parseStatement(grant);
    assertNotNull(data.getConstruct());

    grant = "GRANT senior_role TO junior_role;";
    data = sqlParser.parseStatement(grant);
    assertNotNull(data);
    assertEquals(grant, data.getOriginalSqlCommand());
    assertEquals("GRANT", data.getConstruct().getSentences().get(0).getVerb());
    i = 0;
    assertEquals("junior_role", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("senior_role", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("ROLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

    grant = "GRANT SELECT ON departments TO alice, bob, viewer_role, analyst_role;";
    data = sqlParser.parseStatement(grant);
    assertNotNull(data);
    assertEquals(grant, data.getOriginalSqlCommand());
    assertEquals("GRANT", data.getConstruct().getSentences().get(0).getVerb());
    i = 0;
    assertEquals("departments", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("DB_OBJECT", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("SELECT", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("alice", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("bob", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("viewer_role", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals(
        "analyst_role", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
  }

  @Test
  void testRevoke() {
    String revoke;
    Data data;
    int i = 0;

    /*revoke = "REVOKE UPDATE (salary, bonus) ON payroll FROM hr_role;";
        data = sqlParser.parseStatement(revoke);
        assertNotNull(data);
        assertEquals(revoke, data.getOriginalSqlCommand());
        assertEquals("REVOKE", data.getConstruct().getSentences().get(0).getVerb());
        assertEquals("payroll", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("DB_OBJECT", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
    assertEquals(
        2, data.getConstruct().getSentences().get(0).getObjects().get(i++).getFields().length);
        assertEquals("UPDATE", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
        assertEquals("hr_role", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

        revoke = "REVOKE SELECT, INSERT ON employees FROM alice, bob;";
        data = sqlParser.parseStatement(revoke);
        assertNotNull(data);
        assertEquals(revoke, data.getOriginalSqlCommand());
        assertEquals("REVOKE", data.getConstruct().getSentences().get(0).getVerb());
        i = 0;
        assertEquals("employees", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("DB_OBJECT", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
        assertEquals("SELECT", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
        assertEquals("INSERT", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
        assertEquals("alice", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
        assertEquals("bob", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

        revoke = "REVOKE DELETE ON hr.employees FROM analyst_role;";
        data = sqlParser.parseStatement(revoke);
        assertNotNull(data);
        assertEquals(revoke, data.getOriginalSqlCommand());
        assertEquals("REVOKE", data.getConstruct().getSentences().get(0).getVerb());
        i = 0;
    assertEquals(
        "hr.employees", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("DB_OBJECT", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
        assertEquals("DELETE", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals(
        "analyst_role", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
        assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i).type);*/

    revoke =
        "REVOKE INSERT, UPDATE (first_name, last_name) ON staff FROM supervisor_role, \"Team Lead\";";
    data = sqlParser.parseStatement(revoke);
    assertNotNull(data);
    assertEquals(revoke, data.getOriginalSqlCommand());
    assertEquals("REVOKE", data.getConstruct().getSentences().get(0).getVerb());
    i = 0;
    assertEquals("staff", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("DB_OBJECT", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
    assertEquals(
        2, data.getConstruct().getSentences().get(0).getObjects().get(i++).getFields().length);
    assertEquals("INSERT", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("UPDATE", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals(
        "supervisor_role", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals(
        "\"Team Lead\"", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

    revoke = "REVOKE UPDATE ON \"finance\".\"year_end$report\" FROM cfo_role;";
    data = sqlParser.parseStatement(revoke);
    assertNotNull(data);
    assertEquals(revoke, data.getOriginalSqlCommand());
    assertEquals("REVOKE", data.getConstruct().getSentences().get(0).getVerb());
    i = 0;
    assertEquals(
        "\"finance\".\"year_end$report\"",
        data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("DB_OBJECT", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("UPDATE", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("cfo_role", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

    revoke = "REVOKE EXECUTE ON run_reports FROM \"Data Analyst\", \"Report Viewer\";";
    data = sqlParser.parseStatement(revoke);
    assertNotNull(data);
    assertEquals(revoke, data.getOriginalSqlCommand());
    assertEquals("REVOKE", data.getConstruct().getSentences().get(0).getVerb());
    i = 0;
    assertEquals("run_reports", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("DB_OBJECT", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("EXECUTE", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals(
        "\"Data Analyst\"", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals(
        "\"Report Viewer\"", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
  }

  @Test
  void testDrop() {
    String drop = "DROP TABLE IF EXISTS employees;";
    Data data = sqlParser.parseStatement(drop);
    assertNotNull(data);
    assertEquals(drop, data.getOriginalSqlCommand());
    assertEquals("DROP", data.getConstruct().getSentences().get(0).getVerb());
    int i = 0;
    assertEquals("employees", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

    drop = "DROP INDEX idx_emp_name;";
    data = sqlParser.parseStatement(drop);
    assertNotNull(data);
    assertEquals(drop, data.getOriginalSqlCommand());
    assertEquals("DROP", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "idx_emp_name", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("INDEX", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

    drop = "DROP VIEW IF EXISTS my_view";
    data = sqlParser.parseStatement(drop);
    assertNotNull(data);
    assertEquals(drop, data.getOriginalSqlCommand());
    assertEquals("DROP", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("my_view", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("VIEW", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

    drop = "DROP SCHEMA IF EXISTS hr";
    data = sqlParser.parseStatement(drop);
    assertNotNull(data);
    assertEquals(drop, data.getOriginalSqlCommand());
    assertEquals("DROP", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("hr", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("SCHEMA", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

    drop = "DROP SEQUENCE IF EXISTS emp_id_seq";
    data = sqlParser.parseStatement(drop);
    assertNotNull(data);
    assertEquals(drop, data.getOriginalSqlCommand());
    assertEquals("DROP", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("emp_id_seq", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("SEQUENCE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
  }

  @Test
  void test() {
    String sql;
    Data data;
    int i = 0;

    sql = "CREATE TABLE test_db.sample_table (id INT, name VARCHAR)";
    data = sqlParser.parseStatement(sql);
    assertNotNull(data);
    assertEquals(sql, data.getOriginalSqlCommand());
    assertEquals("CREATE TABLE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "test_db.sample_table", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
    assertEquals(2, data.getConstruct().getSentences().get(0).getObjects().get(i).fields.length);

    sql = "INSERT INTO test_db.sample_table (id, name) VALUES (1, 'Alice')";
    data = sqlParser.parseStatement(sql);
    assertNotNull(data);
    assertEquals(sql, data.getOriginalSqlCommand());
    assertEquals("INSERT", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "test_db.sample_table", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
    assertEquals(2, data.getConstruct().getSentences().get(0).getObjects().get(i).fields.length);

    sql = "UPDATE test_db.sample_table SET name = 'Bob' WHERE id = 1";
    data = sqlParser.parseStatement(sql);
    assertNotNull(data);
    assertEquals(sql, data.getOriginalSqlCommand());
    assertEquals("UPDATE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "test_db.sample_table", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
    assertEquals(2, data.getConstruct().getSentences().get(0).getObjects().get(i).fields.length);

    sql = "DROP TABLE test_db.sample_table";
    data = sqlParser.parseStatement(sql);
    assertNotNull(data);
    assertEquals(sql, data.getOriginalSqlCommand());
    assertEquals("DROP", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "test_db.sample_table", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

    sql = "SELECT id, name FROM test_db.sample_table";
    data = sqlParser.parseStatement(sql);
    assertNotNull(data);
    assertEquals(sql, data.getOriginalSqlCommand());
    assertEquals("SELECT", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "test_db.sample_table", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
    assertEquals(2, data.getConstruct().getSentences().get(0).getObjects().get(i).fields.length);

    sql = "GRANT SELECT ON test_db.sample_table TO analytics_user";
    data = sqlParser.parseStatement(sql);
    assertNotNull(data);
    assertEquals(sql, data.getOriginalSqlCommand());
    assertEquals("GRANT", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "test_db.sample_table", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("DB_OBJECT", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("SELECT", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals(
        "analytics_user", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i).type);

    sql = "REVOKE SELECT ON test_db.sample_table FROM analytics_user";
    data = sqlParser.parseStatement(sql);
    assertNotNull(data);
    i = 0;
    assertEquals(sql, data.getOriginalSqlCommand());
    assertEquals("REVOKE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "test_db.sample_table", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("DB_OBJECT", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals("SELECT", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("PRIVILEGE", data.getConstruct().getSentences().get(0).getObjects().get(i++).type);
    assertEquals(
        "analytics_user", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
  }

  @Test
  void refineSqlTest() {
    String sql =
        "CREATE TABLE delta.`/mnt/data/sales-summary` PARTITIONED BY (`region`) AS SELECT * FROM \"hive.default.sales\" JOIN \"hr.default\";";
    SqlParser parser = new SqlParser();
    Map<String, String> aliasMap = new LinkedHashMap<>();
    String newSql = parser.refineSql(sql, aliasMap);
    assertNotNull(newSql);
    assertEquals(
        "CREATE TABLE _fake_alias_1 PARTITIONED BY (_fake_alias_2) AS SELECT * FROM _fake_alias_3 JOIN _fake_alias_4;",
        newSql);
    assertEquals(4, aliasMap.size());
    assertEquals("delta.`/mnt/data/sales-summary`", aliasMap.get("_fake_alias_1"));
    assertEquals("\"hive.default.sales\"", aliasMap.get("_fake_alias_3"));
    assertEquals("`region`", aliasMap.get("_fake_alias_2"));
    assertEquals("\"hr.default\"", aliasMap.get("_fake_alias_4"));

    sql =
        "INSERT OVERWRITE DIRECTORY '/mnt/out' PARTITIONED BY (region) USING json SELECT * FROM source;";
    parser = new SqlParser();
    aliasMap = new LinkedHashMap<>();
    newSql = parser.refineSql(sql, aliasMap);
    assertNotNull(newSql);
    assertEquals(newSql, sql);
    assertEquals(0, aliasMap.size());

    sql = "REVOKE UPDATE ON \"finance\".\"year_end$report\" FROM cfo_role;";
    parser = new SqlParser();
    aliasMap = new LinkedHashMap<>();
    newSql = parser.refineSql(sql, aliasMap);
    assertNotNull(newSql);
    assertEquals("REVOKE UPDATE ON _fake_alias_1 FROM cfo_role;", newSql);
    assertEquals(1, aliasMap.size());
    assertEquals("\"finance\".\"year_end$report\"", aliasMap.get("_fake_alias_1"));

    sql = "INSERT INTO hive.testdb.sample VALUES (6,'caroz');";
    parser = new SqlParser();
    aliasMap = new LinkedHashMap<>();
    newSql = parser.refineSql(sql, aliasMap);
    assertNotNull(newSql);
    assertEquals("INSERT INTO _fake_alias_1 VALUES (6,'caroz');", newSql);
    assertEquals(1, aliasMap.size());
    assertEquals("hive.testdb.sample", aliasMap.get("_fake_alias_1"));

    sql = "INSERT INTO hive.testdb VALUES (6,'caroz');";
    parser = new SqlParser();
    aliasMap = new LinkedHashMap<>();
    newSql = parser.refineSql(sql, aliasMap);
    assertNotNull(newSql);
    assertEquals("INSERT INTO _fake_alias_1 VALUES (6,'caroz');", newSql);
    assertEquals(1, aliasMap.size());
    assertEquals("hive.testdb", aliasMap.get("_fake_alias_1"));
  }
}
