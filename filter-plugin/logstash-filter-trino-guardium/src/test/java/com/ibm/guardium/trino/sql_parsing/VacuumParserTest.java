package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class VacuumParserTest {
  @Test
  void test() throws InvalidStatementException {
    String sql;
    Data data;
    VacuumParser parser;

    sql = "VACUUM my_table RETAIN 168 HOURS;";
    data = new Data();
    parser = new VacuumParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    int i = 0;
    assertEquals("VACUUM", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("my_table", data.getConstruct().getSentences().get(i).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(i).getObjects().get(0).type);

    sql = "VACUUM schema.my_table;";
    data = new Data();
    parser = new VacuumParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    i = 0;
    assertEquals("VACUUM", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "schema.my_table", data.getConstruct().getSentences().get(i).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(i).getObjects().get(0).type);
  }
}
