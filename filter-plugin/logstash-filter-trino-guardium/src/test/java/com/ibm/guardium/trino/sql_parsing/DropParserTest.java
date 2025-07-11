package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.drop.Drop;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class DropParserTest {
  @Test
  void dropTest() throws JSQLParserException {
    String sql = "DROP TABLE employees;";
    Data data = new Data();
    Drop drop = (Drop) CCJSqlParserUtil.parse(sql);
    DropParser parser = new DropParser(data, sql, new LinkedHashMap<>());
    parser.parse(drop);
    assertEquals("DROP", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("employees", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);

    sql = "DROP TABLE IF EXISTS employees;";
    data = new Data();
    drop = (Drop) CCJSqlParserUtil.parse(sql);
    parser = new DropParser(data, sql, new LinkedHashMap<>());
    parser.parse(drop);
    assertEquals("DROP", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("employees", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);

    sql = "DROP VIEW IF EXISTS active_employees;";
    data = new Data();
    drop = (Drop) CCJSqlParserUtil.parse(sql);
    parser = new DropParser(data, sql, new LinkedHashMap<>());
    parser.parse(drop);
    assertEquals("DROP", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "active_employees", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("VIEW", data.getConstruct().getSentences().get(0).getObjects().get(0).type);

    sql = "DROP SEQUENCE IF EXISTS emp_id_seq;";
    data = new Data();
    drop = (Drop) CCJSqlParserUtil.parse(sql);
    parser = new DropParser(data, sql, new LinkedHashMap<>());
    parser.parse(drop);
    assertEquals("DROP", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("emp_id_seq", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("SEQUENCE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);

    sql = "DROP SCHEMA IF EXISTS hr CASCADE;";
    data = new Data();
    drop = (Drop) CCJSqlParserUtil.parse(sql);
    parser = new DropParser(data, sql, new LinkedHashMap<>());
    parser.parse(drop);
    assertEquals("DROP", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("hr", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("SCHEMA", data.getConstruct().getSentences().get(0).getObjects().get(0).type);

    sql = "DROP ROLE analyst;";
    data = new Data();
    drop = (Drop) CCJSqlParserUtil.parse(sql);
    parser = new DropParser(data, sql, new LinkedHashMap<>());
    parser.parse(drop);
    assertEquals("DROP", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("analyst", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("ROLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);

    sql = "DROP INDEX idx_employees_lastname;";
    data = new Data();
    drop = (Drop) CCJSqlParserUtil.parse(sql);
    parser = new DropParser(data, sql, new LinkedHashMap<>());
    parser.parse(drop);
    assertEquals("DROP", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "idx_employees_lastname",
        data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("INDEX", data.getConstruct().getSentences().get(0).getObjects().get(0).type);

    sql = "DROP CATALOG IF EXISTS my_catalog;";
    data = new Data();
    drop = (Drop) CCJSqlParserUtil.parse(sql);
    parser = new DropParser(data, sql, new LinkedHashMap<>());
    parser.parse(drop);
    assertEquals("DROP", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("my_catalog", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("CATALOG", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
  }
}
