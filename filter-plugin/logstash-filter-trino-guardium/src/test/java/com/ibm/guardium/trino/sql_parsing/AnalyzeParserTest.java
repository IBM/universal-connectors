package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnalyzeParserTest {
  @Test
  void analyzeTest() throws JSQLParserException {
    String sql = "ANALYZE hive.default.orders;";
    Data data = new Data();
    Map<String, String> aliasMap = new LinkedHashMap<>();
    AnalyzeParser parser = new AnalyzeParser(data, sql, aliasMap);
    parser.parse(CCJSqlParserUtil.parse(sql));
    assertEquals("ANALYZE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("orders", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
  }
}
