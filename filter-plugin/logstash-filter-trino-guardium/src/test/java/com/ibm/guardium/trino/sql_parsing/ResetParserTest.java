package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ResetParserTest {
  @Test
  void resetTest() throws InvalidStatementException {
    Data data = new Data();
    String sql = "RESET SESSION hive.insert_existing_partitions_behavior;";
    assertTrue(ResetParser.isReset(sql));
    ResetParser parser = new ResetParser(data, sql, new LinkedHashMap<>());
    parser.parse();
    assertEquals("RESET", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "hive.insert_existing_partitions_behavior",
        data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("SESSION", data.getConstruct().getSentences().get(0).getObjects().get(0).type);
  }
}
