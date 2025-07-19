package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.update.Update;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class UpdateParserTest {
  String update1 =
      "UPDATE employees SET salary = 60000, department = 'Marketing' WHERE employee_id = 101";
  String update2 =
      "UPDATE employees SET salary = (SELECT AVG(salary) FROM employees WHERE department = 'Sales') WHERE department = 'Marketing';";

  @Test
  void parseUpdateTest1() throws JSQLParserException {
    Update statement = (Update) CCJSqlParserUtil.parse(update1);
    ParsedDetail parsedDetail = new ParsedDetail();
    UpdateParser updateParser = new UpdateParser(new Data(), update1, new LinkedHashMap<>());
    updateParser.parseUpdateStatement(statement, parsedDetail);
    assertEquals(1, parsedDetail.tableToFields.size());
    assertTrue(parsedDetail.tableToFields.containsKey("employees"));
    Iterator<String> iterator = parsedDetail.tableToFields.get("employees").iterator();
    assertEquals("employee_id", iterator.next());
    assertEquals("salary", iterator.next());
    assertEquals("department", iterator.next());
  }

  @Test
  void parseUpdateTest2() throws JSQLParserException {
    Update statement = (Update) CCJSqlParserUtil.parse(update2);
    ParsedDetail parsedDetail = new ParsedDetail();
    UpdateParser updateParser = new UpdateParser(new Data(), update2, new LinkedHashMap<>());
    updateParser.parseUpdateStatement(statement, parsedDetail);
    assertEquals(1, parsedDetail.tableToFields.size());
    assertTrue(parsedDetail.tableToFields.containsKey("employees"));
    Iterator<String> iterator = parsedDetail.tableToFields.get("employees").iterator();
    assertEquals("salary", iterator.next());
    assertEquals("department", iterator.next());
  }
}
