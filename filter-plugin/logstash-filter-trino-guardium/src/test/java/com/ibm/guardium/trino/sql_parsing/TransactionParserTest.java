package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class TransactionParserTest {
  @Test
  void test() throws InvalidStatementException {
    String sql;
    Data data;
    TransactionParser parser;
    int i;
    sql =
        "BEGIN TRANSACTION;\n"
            + "INSERT INTO sales (id, product, price)\n"
            + "VALUES (1, 'Widget', 9.99);\n"
            + "\n"
            + "UPDATE inventory\n"
            + "SET stock = stock - 1\n"
            + "WHERE product_id = 1;\n"
            + "\n"
            + "COMMIT;\n";
    data = new Data();
    parser = new TransactionParser(data, sql, new LinkedHashMap<>());
    parser.parse();

    i = 0;
    assertEquals("TRANSACTION", data.getConstruct().getSentences().get(i++).getVerb());
    assertEquals("INSERT", data.getConstruct().getSentences().get(i).getVerb());
    assertEquals("sales", data.getConstruct().getSentences().get(i).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(i).getObjects().get(0).type);
    assertEquals(3, data.getConstruct().getSentences().get(i++).getObjects().get(0).fields.length);
    assertEquals("UPDATE", data.getConstruct().getSentences().get(i).getVerb());
    assertEquals("inventory", data.getConstruct().getSentences().get(i).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(i).getObjects().get(0).type);
    assertEquals(2, data.getConstruct().getSentences().get(i).getObjects().get(0).fields.length);

    sql = "BEGIN;\nDELETE FROM logs\nWHERE created_at < '2024-01-01';\nROLLBACK;";
    data = new Data();
    parser = new TransactionParser(data, sql, new LinkedHashMap<>());
    parser.parse();

    i = 0;
    assertEquals("TRANSACTION", data.getConstruct().getSentences().get(i++).getVerb());
    assertEquals("DELETE", data.getConstruct().getSentences().get(i).getVerb());
    assertEquals("logs", data.getConstruct().getSentences().get(i).getObjects().get(0).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(i).getObjects().get(0).type);
    assertEquals(1, data.getConstruct().getSentences().get(i).getObjects().get(0).fields.length);
  }
}
