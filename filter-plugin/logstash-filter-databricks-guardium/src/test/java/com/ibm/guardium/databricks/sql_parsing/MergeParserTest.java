package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class MergeParserTest {
  @Test
  void testMerge() throws JSQLParserException {
    String sql;
    Data data;
    MergeParser parser;

    sql =
        "MERGE INTO target_table AS target\n"
            + "USING source_table AS source\n"
            + "ON target.id = source.id\n"
            + "WHEN MATCHED THEN\n"
            + "  UPDATE SET target.name = source.name, target.age = source.age\n"
            + "WHEN NOT MATCHED THEN\n"
            + "  INSERT (id, name, age) VALUES (source.id, source.name, source.age);";
    data = new Data();
    parser = new MergeParser(data, sql, new LinkedHashMap<>());
    parser.parse(CCJSqlParserUtil.parse(sql));

    assertNotNull(data.getConstruct());
    assertEquals(2, data.getConstruct().getSentences().get(0).getObjects().size());
    assertEquals(
        "target_table", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals(3, data.getConstruct().getSentences().get(0).getObjects().get(0).fields.length);
    assertEquals(
        "source_table", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals(3, data.getConstruct().getSentences().get(0).getObjects().get(1).fields.length);

    sql =
        "MERGE INTO customers AS target\n"
            + "USING updates AS source\n"
            + "ON target.customer_id = source.customer_id\n"
            + "WHEN MATCHED AND source.status = 'inactive' THEN\n"
            + "  DELETE\n"
            + "WHEN MATCHED THEN\n"
            + "  UPDATE SET\n"
            + "    target.name = source.name,\n"
            + "    target.email = source.email,\n"
            + "    target.status = source.status\n"
            + "WHEN NOT MATCHED THEN\n"
            + "  INSERT (customer_id, name, email, status)\n"
            + "  VALUES (source.customer_id, source.name, source.email, source.status);";

    data = new Data();
    parser = new MergeParser(data, sql, new LinkedHashMap<>());
    parser.parse(CCJSqlParserUtil.parse(sql));

    assertNotNull(data.getConstruct());
    assertEquals(2, data.getConstruct().getSentences().get(0).getObjects().size());
    assertEquals("customers", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals(4, data.getConstruct().getSentences().get(0).getObjects().get(0).fields.length);
    assertEquals("updates", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
    assertEquals(4, data.getConstruct().getSentences().get(0).getObjects().get(1).fields.length);
  }
}
