package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CreateViewParserTest {
  @Test
  void refreshTest() throws JSQLParserException {
    Data data = new Data();
    Map<String, String> aliasMap = new LinkedHashMap<>();
    String sql =
        "CREATE MATERIALIZED VIEW hive.default.mv_top_orders AS SELECT * FROM orders WHERE total > 1000;";
    CreateViewParser parser = new CreateViewParser(data, sql, aliasMap);

    parser.parse(CCJSqlParserUtil.parse(sql));
    assertEquals("CREATE MATERIALIZED VIEW", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("orders", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals(1, data.getConstruct().getSentences().get(0).getObjects().get(0).fields.length);
    assertEquals(
        "mv_top_orders", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals("VIEW", data.getConstruct().getSentences().get(0).getObjects().get(1).type);
    sql =
            "CREATE TEMPORARY VIEW test(id, name) AS\n"
                    + "  VALUES ( 1, 'Lisa'),\n"
                    + "         ( 2, 'Mary'),\n"
                    + "         ( 3, 'Evan'),\n"
                    + "         ( 4, 'Fred'),\n"
                    + "         ( 5, 'Alex'),\n"
                    + "         ( 6, 'Mark'),\n"
                    + "         ( 7, 'Lily')";
    data = new Data();
    aliasMap = new LinkedHashMap<>();
    parser = new CreateViewParser(data, sql, aliasMap);

    parser.parse(CCJSqlParserUtil.parse(sql));
    assertEquals("CREATE TEMPORARY VIEW", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("test", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("VIEW", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals(2, data.getConstruct().getSentences().get(0).getObjects().get(0).fields.length);

    sql =
            "CREATE OR REPLACE TEMP VIEW my_temp_view AS\n"
                    + "SELECT * FROM some_table WHERE condition = 'value';\n";
    data = new Data();
    aliasMap = new LinkedHashMap<>();
    parser = new CreateViewParser(data, sql, aliasMap);

    parser.parse(CCJSqlParserUtil.parse(sql));
    assertEquals(
            "CREATE OR REPLACE TEMP VIEW", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("some_table", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals(1, data.getConstruct().getSentences().get(0).getObjects().get(0).fields.length);
    assertEquals(
            "my_temp_view", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals("VIEW", data.getConstruct().getSentences().get(0).getObjects().get(1).type);
    assertEquals(0, data.getConstruct().getSentences().get(0).getObjects().get(1).fields.length);
  }
}
