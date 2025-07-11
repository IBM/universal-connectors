package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class OptimizeParserTest {
  @Test
  void test() throws InvalidStatementException {
    String sql;
    Data data;
    OptimizeParser parser;

    sql =
        "OPTIMIZE TABLE sales_data WHERE region = 'west' AND year = 2024 ZORDER BY (customer_id, order_date);";
    data = new Data();
    parser = new OptimizeParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    int i = 0;
    assertEquals("OPTIMIZE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("sales_data", data.getConstruct().getSentences().get(i).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(i).getObjects().get(0).type);
    assertEquals(4, data.getConstruct().getSentences().get(i).getObjects().get(0).fields.length);

    sql = "OPTIMIZE TABLE sales_data;";
    data = new Data();
    parser = new OptimizeParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    assertEquals("OPTIMIZE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("sales_data", data.getConstruct().getSentences().get(i).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(i).getObjects().get(0).type);
    assertEquals(0, data.getConstruct().getSentences().get(i).getObjects().get(0).fields.length);

    sql = "OPTIMIZE sales_data;";
    data = new Data();
    parser = new OptimizeParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    assertEquals("OPTIMIZE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("sales_data", data.getConstruct().getSentences().get(i).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(i).getObjects().get(0).type);
    assertEquals(0, data.getConstruct().getSentences().get(i).getObjects().get(0).fields.length);
  }
}
