package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class RefreshParserTest {
  @Test
  void refreshTest() throws InvalidStatementException {
    Data data = new Data();
    String sql = "REFRESH MATERIALIZED VIEW hive.default.mv_high_value_customers;";
    assertTrue(RefreshParser.isRefresh(sql));
    RefreshParser parser = new RefreshParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    assertEquals("REFRESH MATERIALIZED VIEW", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "hive.default.mv_high_value_customers",
        data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("VIEW", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
  }
}
