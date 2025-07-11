package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommentParserTest {
  @Test
  void commentTest() throws JSQLParserException {
    Data data = new Data();
    Map<String, String> aliasMap = new LinkedHashMap<>();
    String sql =
        "COMMENT ON TABLE sales.orders IS 'Stores customer order data including order date and total amount';";
    CommentParser parser = new CommentParser(data, sql, aliasMap);
    parser.parse(CCJSqlParserUtil.parse(sql));
    assertEquals("COMMENT", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "Stores customer order data including order date and total amount",
        data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("COMMENT", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals("orders", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(1).type);

    data = new Data();
    aliasMap = new LinkedHashMap<>();
    sql = "COMMENT ON COLUMN sales.orders.order_date IS 'The date when the order was placed';";
    parser = new CommentParser(data, sql, aliasMap);
    parser.parse(CCJSqlParserUtil.parse(sql));
    assertEquals("COMMENT", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "The date when the order was placed",
        data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("COMMENT", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals("order_date", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals("COLUMN", data.getConstruct().getSentences().get(0).getObjects().get(1).type);

    data = new Data();
    aliasMap = new LinkedHashMap<>();
    sql = "COMMENT ON VIEW sales.active_orders IS 'View of orders that are currently active';";
    parser = new CommentParser(data, sql, aliasMap);
    parser.parse(CCJSqlParserUtil.parse(sql));
    assertEquals("COMMENT", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "View of orders that are currently active",
        data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("COMMENT", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals(
        "active_orders", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals("VIEW", data.getConstruct().getSentences().get(0).getObjects().get(1).type);
  }
}
