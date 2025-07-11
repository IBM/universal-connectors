package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AlterSequenceParserTest {

  @Test
  void parseAlterStatement() throws JSQLParserException {
    Data data = new Data();
    String alter = "ALTER SEQUENCE emp_seq MINVALUE 100 MAXVALUE 9999;";
    Statement statement = CCJSqlParserUtil.parse(alter);
    Map<String, String> aliasMap = new LinkedHashMap<>();
    AlterSequenceParser parser = new AlterSequenceParser(data, alter, aliasMap);
    parser.parse(statement);
    int i = 0;
    assertEquals("ALTER", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("emp_seq", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("SEQUENCE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
  }
}
