package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class SelectParserTest {

  String select1 =
      "SELECT a.field1, b.field2, c.field3 FROM table1 a JOIN table2 b ON a.id = b.id JOIN table3 c ON b.id = c.id WHERE a.field4 = 'value';";
  String select2 =
      "SELECT field1, field2 FROM table1 WHERE EXISTS (SELECT 1 FROM table2 WHERE table2.field3 = table1.field3);";
  String select3 = "SELECT field1, field2 FROM table1 WHERE field3 BETWEEN 100 AND 200";
  String select4 =
      "SELECT p.product_name, SUM(s.quantity) AS total_quantity FROM products p JOIN sales s ON p.product_id = s.product_id GROUP BY p.product_name;";
  String select5 =
      "WITH top_customers AS (SELECT customer_id, SUM(total_amount) AS total_spent FROM orders GROUP BY customer_id HAVING SUM(total_amount) > 1000) SELECT c.customer_id, c.name, t.total_spent FROM customers c JOIN top_customers t ON c.customer_id = t.customer_id;";

  @Test
  void selectTest() throws JSQLParserException {
    Data data;
    Select statement;
    SelectParser parser;

    data = new Data();
    statement = (Select) CCJSqlParserUtil.parse(select1);
    parser = new SelectParser(data, select1, new LinkedHashMap<>());
    parser.parse(statement);
    assertEquals("SELECT", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("table3", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals(
        2, data.getConstruct().getSentences().get(0).getObjects().get(0).getFields().length);
    assertEquals("table2", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(1).type);
    assertEquals(
        2, data.getConstruct().getSentences().get(0).getObjects().get(1).getFields().length);
    assertEquals("table1", data.getConstruct().getSentences().get(0).getObjects().get(2).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(2).type);
    assertEquals(
        3, data.getConstruct().getSentences().get(0).getObjects().get(2).getFields().length);

    data = new Data();
    statement = (Select) CCJSqlParserUtil.parse(select2);
    parser = new SelectParser(data, select2, new LinkedHashMap<>());
    parser.parse(statement);
    assertEquals("SELECT", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("table2", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals(
        1, data.getConstruct().getSentences().get(0).getObjects().get(0).getFields().length);
    assertEquals("table1", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(1).type);
    assertEquals(
        3, data.getConstruct().getSentences().get(0).getObjects().get(1).getFields().length);

    data = new Data();
    statement = (Select) CCJSqlParserUtil.parse(select3);
    parser = new SelectParser(data, select3, new LinkedHashMap<>());
    parser.parse(statement);
    assertEquals("SELECT", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("table1", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals(
        3, data.getConstruct().getSentences().get(0).getObjects().get(0).getFields().length);

    data = new Data();
    statement = (Select) CCJSqlParserUtil.parse(select4);
    parser = new SelectParser(data, select4, new LinkedHashMap<>());
    parser.parse(statement);
    assertEquals("SELECT", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("sales", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals(
        2, data.getConstruct().getSentences().get(0).getObjects().get(0).getFields().length);
    assertEquals("products", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(1).type);
    assertEquals(
        2, data.getConstruct().getSentences().get(0).getObjects().get(1).getFields().length);

    data = new Data();
    statement = (Select) CCJSqlParserUtil.parse(select5);
    parser = new SelectParser(data, select5, new LinkedHashMap<>());
    parser.parse(statement);
    assertEquals("SELECT", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "top_customers", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals(
        2, data.getConstruct().getSentences().get(0).getObjects().get(0).getFields().length);
    assertEquals("orders", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(1).type);
    assertEquals(
        2, data.getConstruct().getSentences().get(0).getObjects().get(1).getFields().length);
    assertEquals("customers", data.getConstruct().getSentences().get(0).getObjects().get(2).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(2).type);
    assertEquals(
        2, data.getConstruct().getSentences().get(0).getObjects().get(2).getFields().length);
  }
}
