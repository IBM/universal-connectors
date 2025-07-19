package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.databricks.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class DescribeParserTest {
  @Test
  void describeTest() throws InvalidStatementException {
    String sql = "DESCRIBE customers;";
    Data data = new Data();
    DescribeParser parser = new DescribeParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    assertEquals("DESCRIBE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("customers", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);

    sql = "DESC hive.default.customers;";
    data = new Data();
    parser = new DescribeParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    assertEquals("DESCRIBE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "hive.default.customers",
        data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);

    sql = "DESCRIBE DATABASE my_database;";
    data = new Data();
    parser = new DescribeParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    assertEquals("DESCRIBE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("my_database", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("DATABASE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);

    sql = "DESCRIBE TABLE EXTENDED sales;";
    data = new Data();
    parser = new DescribeParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    assertEquals("DESCRIBE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("sales", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);

    sql = "DESCRIBE FUNCTION EXTENDED my_catalog.my_schema.my_func;";
    data = new Data();
    parser = new DescribeParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    assertEquals("DESCRIBE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "my_catalog.my_schema.my_func",
        data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("FUNCTION", data.getConstruct().getSentences().get(0).getObjects().get(0).type);

    sql = "DESCRIBE HISTORY delta.sales_data;";
    data = new Data();
    parser = new DescribeParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    assertEquals("DESCRIBE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "delta.sales_data", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("HISTORY", data.getConstruct().getSentences().get(0).getObjects().get(0).type);

    sql = "DESC SCHEMA EXTENDED sales;";
    data = new Data();
    parser = new DescribeParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    assertEquals("DESCRIBE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("sales", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("SCHEMA", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
  }
}
