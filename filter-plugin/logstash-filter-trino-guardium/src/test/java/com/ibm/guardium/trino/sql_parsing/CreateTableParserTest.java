package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CreateTableParserTest {
  @Test
  void testCreateTable() throws JSQLParserException {
    String sql;
    Data data;
    CreateTableParser parser;
    Map<String, String> aliasMap = new LinkedHashMap<>();
    sql =
        "CREATE OR REPLACE TABLE sales (\n"
            + "  id INT,\n"
            + "  product STRING,\n"
            + "  quantity INT,\n"
            + "  price DOUBLE,\n"
            + "  sale_date DATE\n"
            + ")\n"
            + "USING DELTA;";
    data = new Data();
    parser = new CreateTableParser(data, sql, aliasMap);
    parser.parse(CCJSqlParserUtil.parse(sql));

    assertNotNull(data.getConstruct());
    assertEquals(1, data.getConstruct().getSentences().get(0).getObjects().size());
    assertEquals("CREATE OR REPLACE TABLE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("sales", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals(5, data.getConstruct().getSentences().get(0).getObjects().get(0).fields.length);

    sql =
        "CREATE OR REPLACE TABLE top_products\n"
            + "USING DELTA AS\n"
            + "SELECT product, SUM(quantity) AS total_sold\n"
            + "FROM sales\n"
            + "GROUP BY product\n"
            + "ORDER BY total_sold DESC;";
    data = new Data();
    aliasMap = new LinkedHashMap<>();
    parser = new CreateTableParser(data, sql, aliasMap);
    parser.parse(CCJSqlParserUtil.parse(sql));

    assertNotNull(data.getConstruct());
    assertEquals(2, data.getConstruct().getSentences().get(0).getObjects().size());
    assertEquals("CREATE OR REPLACE TABLE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("sales", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals(2, data.getConstruct().getSentences().get(0).getObjects().get(0).fields.length);
    assertEquals(
        "top_products", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals(0, data.getConstruct().getSentences().get(0).getObjects().get(1).fields.length);
  }
}
