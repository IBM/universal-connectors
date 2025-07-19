package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class SetParserTest {
  @Test
  void setTest() throws JSQLParserException {
    String sql = "SET SESSION hive.compression_codec = 'GZIP';";
    Data data = new Data();
    SetParser parser = new SetParser(data, sql, new LinkedHashMap<>());
    parser.parse(CCJSqlParserUtil.parse(sql));
    assertEquals("SET", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "hive.compression_codec",
        data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("SESSION", data.getConstruct().getSentences().get(0).getObjects().get(0).type);

    sql = "SET TIME ZONE 'America/Toronto';";
    data = new Data();
    parser = new SetParser(data, sql, new LinkedHashMap<>());
    parser.parse(CCJSqlParserUtil.parse(sql));
    assertEquals("SET", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
    assertEquals("Time Zone", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
  }
}
