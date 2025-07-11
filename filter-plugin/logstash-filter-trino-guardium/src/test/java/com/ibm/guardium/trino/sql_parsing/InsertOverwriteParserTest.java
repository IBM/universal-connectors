package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InsertOverwriteParserTest {
  @Test
  void test() throws InvalidStatementException {
    String sql;
    Data data;
    InsertOverwriteParser parser;

    sql =
        "INSERT OVERWRITE TABLE sales_summary\n"
            + "SELECT region, SUM(amount) AS total_sales\n"
            + "FROM sales\n"
            + "GROUP BY region;";
    data = new Data();
    Map<String, String> alias = new LinkedHashMap<>();
    parser = new InsertOverwriteParser(data, SqlParser.refineSql(sql, alias), alias);
    parser.parse();

    assertNotNull(data.getConstruct());
    assertEquals("INSERT OVERWRITE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(2, data.getConstruct().getSentences().get(0).getObjects().size());
    assertEquals("sales", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals(2, data.getConstruct().getSentences().get(0).getObjects().get(0).fields.length);
    assertEquals(
        "sales_summary", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(1).type);
    assertEquals(0, data.getConstruct().getSentences().get(0).getObjects().get(1).fields.length);

    sql =
        "INSERT OVERWRITE DIRECTORY '/mnt/out' PARTITIONED BY (region) USING json SELECT * FROM source;";
    data = new Data();
    alias = new LinkedHashMap<>();
    parser = new InsertOverwriteParser(data, SqlParser.refineSql(sql, alias), alias);
    parser.parse();

    assertNotNull(data.getConstruct());
    assertEquals("INSERT OVERWRITE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(2, data.getConstruct().getSentences().get(0).getObjects().size());
    assertEquals("source", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals(0, data.getConstruct().getSentences().get(0).getObjects().get(0).fields.length);
    assertEquals("/mnt/out", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals("DIRECTORY", data.getConstruct().getSentences().get(0).getObjects().get(1).type);
  }
}
