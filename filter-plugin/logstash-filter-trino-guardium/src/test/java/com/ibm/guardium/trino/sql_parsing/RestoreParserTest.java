package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class RestoreParserTest {
  @Test
  void test() throws InvalidStatementException {
    String sql;
    Data data;
    RestoreParser parser;

    sql = "RESTORE TABLE 'sales-data' TO TIMESTAMP AS OF '2024-04-15T18:30:00';";
    data = new Data();
    parser = new RestoreParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    int i = 0;
    assertEquals("RESTORE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("sales-data", data.getConstruct().getSentences().get(i).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(i).getObjects().get(0).type);

    sql = "RESTORE TABLE my_db.my_table TO VERSION AS OF 5;";
    data = new Data();
    parser = new RestoreParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    assertEquals("RESTORE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "my_db.my_table", data.getConstruct().getSentences().get(i).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(i).getObjects().get(0).type);

    sql = "RESTORE TABLE my_table TO TIMESTAMP AS OF '2024-05-01T10:00:00';";
    data = new Data();
    parser = new RestoreParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    assertEquals("RESTORE", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("my_table", data.getConstruct().getSentences().get(i).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(i).getObjects().get(0).type);
  }
}
