package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ExplainParserTest {
  @Test
  void explainTest() throws JSQLParserException {
    String sql = "EXPLAIN SELECT * FROM orders WHERE total > 100;";
    Data data = new Data();
    ExplainParser parser = new ExplainParser(data, sql, new LinkedHashMap<>());
    parser.parse(CCJSqlParserUtil.parse(sql));
    assertEquals("EXPLAIN", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("orders", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals(1, data.getConstruct().getSentences().get(0).getObjects().get(0).fields.length);

    sql = "EXPLAIN ANALYZE SELECT * FROM orders WHERE total > 100;";
    data = new Data();
    parser = new ExplainParser(data, sql, new LinkedHashMap<>());
    parser.parse(CCJSqlParserUtil.parse(sql));
    assertEquals("EXPLAIN", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("orders", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals(1, data.getConstruct().getSentences().get(0).getObjects().get(0).fields.length);
  }
}
