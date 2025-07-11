package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class UseParserTest {
  @Test
  void useTest() throws InvalidStatementException {
    Data data = new Data();
    String sql = "USE hive.hr;";
    UseParser parser = new UseParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    assertEquals("USE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("hive.hr", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("DATABASE", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
  }
}
