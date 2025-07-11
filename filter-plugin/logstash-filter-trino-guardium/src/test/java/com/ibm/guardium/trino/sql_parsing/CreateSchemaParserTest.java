package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CreateSchemaParserTest {
  @Test
  void test() throws InvalidStatementException {
    Data data = new Data();
    String sql;
    CreateSchemaParser parser;
    Map<String, String> aliasMap = new LinkedHashMap<>();

    sql = "CREATE SCHEMA hr.sales AUTHORIZATION alice;";
    parser = new CreateSchemaParser(data, sql, aliasMap);
    parser.parse();
    assertEquals("CREATE SCHEMA", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("hr.sales", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("SCHEMA", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals("alice", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals("USER", data.getConstruct().getSentences().get(0).getObjects().get(1).type);

    aliasMap = new LinkedHashMap<>();
    sql =
        "CREATE SCHEMA hive.finance WITH (location = 's3a://warehouse/finance/', external_location = 's3a://rawdata/finance/', managed_location = 's3a://managed/finance/');";
    parser = new CreateSchemaParser(data, sql, aliasMap);
    parser.parse();
    assertEquals("CREATE SCHEMA", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "hive.finance", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("SCHEMA", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals("location", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals("PROPERTY", data.getConstruct().getSentences().get(0).getObjects().get(1).type);
    assertEquals(
        "external_location", data.getConstruct().getSentences().get(0).getObjects().get(2).name);
    assertEquals("PROPERTY", data.getConstruct().getSentences().get(0).getObjects().get(2).type);
    assertEquals(
        "managed_location", data.getConstruct().getSentences().get(0).getObjects().get(3).name);
    assertEquals("PROPERTY", data.getConstruct().getSentences().get(0).getObjects().get(3).type);
  }
}
