package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AlterParserTest {

  @Test
  void parseAlterStatement() throws JSQLParserException {
    Data data = new Data();
    String alter = "ALTER TABLE employees RENAME COLUMN birthdate TO date_of_birth;";
    Map<String, String> aliasMap = new LinkedHashMap<>();
    AlterParser parser = new AlterParser(data, alter, aliasMap);
    Statement statement = CCJSqlParserUtil.parse(alter);
    parser.parse(statement);
    assertEquals("ALTER", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("employees", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals(
        2, data.getConstruct().getSentences().get(0).getObjects().get(0).getFields().length);

    alter = "ALTER TABLE employees ADD CONSTRAINT chk_salary CHECK (salary > 0);";
    data = new Data();
    aliasMap = new LinkedHashMap<>();
    parser = new AlterParser(data, alter, aliasMap);
    statement = (Alter) CCJSqlParserUtil.parse(alter);
    parser.parse(statement);
    assertEquals("ALTER", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("employees", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals(
        1, data.getConstruct().getSentences().get(0).getObjects().get(0).getFields().length);

    data = new Data();
    aliasMap = new LinkedHashMap<>();
    alter =
        "ALTER TABLE employees ADD CONSTRAINT fk_dept FOREIGN KEY (department_id) REFERENCES departments(dep_id);";
    parser = new AlterParser(data, alter, aliasMap);
    statement = (Alter) CCJSqlParserUtil.parse(alter);
    parser.parse(statement);
    int i = 0;
    assertEquals("ALTER", data.getConstruct().getSentences().get(0).getVerb());

    assertEquals("departments", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
    assertEquals(
        1, data.getConstruct().getSentences().get(0).getObjects().get(i++).getFields().length);
    assertEquals("employees", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
    assertEquals(
        1, data.getConstruct().getSentences().get(0).getObjects().get(i).getFields().length);

    data = new Data();
    aliasMap = new LinkedHashMap<>();
    alter = "ALTER TABLE employees ADD CONSTRAINT emp_pk PRIMARY KEY (employee_id);";
    parser = new AlterParser(data, alter, aliasMap);
    statement = (Alter) CCJSqlParserUtil.parse(alter);
    parser.parse(statement);
    i = 0;
    assertEquals("ALTER", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("employees", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
    assertEquals(
        1, data.getConstruct().getSentences().get(0).getObjects().get(i).getFields().length);
  }
}
