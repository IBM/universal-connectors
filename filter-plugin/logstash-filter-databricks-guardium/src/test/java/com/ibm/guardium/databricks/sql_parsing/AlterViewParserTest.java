package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AlterViewParserTest {

  @Test
  void parseAlterStatement() throws JSQLParserException {
    Data data = new Data();
    String alter =
        "ALTER VIEW employee_view AS SELECT employee_id, department_id FROM employees WITH CHECK OPTION;";
    alter = AlterParser.removeWithCheckOption(alter);
    Statement statement = CCJSqlParserUtil.parse(alter);
    Map<String, String> aliasMap = new LinkedHashMap<>();
    AlterViewParser parser = new AlterViewParser(data, alter, aliasMap);
    parser.parse(statement);
    int i = 0;
    assertEquals("ALTER", data.getConstruct().getSentences().get(0).getVerb());
    assertEquals("employees", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("TABLE", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
    assertEquals(
        2, data.getConstruct().getSentences().get(0).getObjects().get(i++).getFields().length);
    assertEquals(
        "employee_view", data.getConstruct().getSentences().get(0).getObjects().get(i).name);
    assertEquals("VIEW", data.getConstruct().getSentences().get(0).getObjects().get(i).type);
  }
}
