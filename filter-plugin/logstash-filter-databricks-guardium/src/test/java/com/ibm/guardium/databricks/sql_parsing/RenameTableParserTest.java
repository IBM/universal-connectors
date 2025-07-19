package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class RenameTableParserTest {
  @Test
  void renameTest() throws JSQLParserException {
    Data data = new Data();
    String sql = "RENAME TABLE old_table TO new_table;";
    RenameTableParser parser = new RenameTableParser(data, sql, new LinkedHashMap<>());
    parser.parse(CCJSqlParserUtil.parse(sql));

    assertEquals("RENAME", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("old_table", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("new_table", data.getConstruct().getSentences().get(0).getObjects().get(1).name);

    data = new Data();
    sql = "RENAME TABLE hive.default.customers TO hive.default.clients;";
    parser = new RenameTableParser(data, sql, new LinkedHashMap<>());
    parser.parse(CCJSqlParserUtil.parse(sql));

    assertEquals("RENAME", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("customers", data.getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals("clients", data.getConstruct().getSentences().get(0).getObjects().get(1).name);
  }
}
