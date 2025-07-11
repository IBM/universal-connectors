package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.delete.Delete;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class DeleteParserTest {
  String delete1 =
      "DELETE FROM employees WHERE department_id IN (SELECT department_id FROM departments WHERE location = 'New York');";
  String delete2 =
      "DELETE employees FROM employees JOIN departments ON employees.department_id = departments.department_id WHERE departments.location = 'New York';";

  @Test
  void parseDeleteTest1() throws JSQLParserException {
    Delete statement = (Delete) CCJSqlParserUtil.parse(delete1);
    ParsedDetail parsedDetail = new ParsedDetail();
    DeleteParser deleteParser = new DeleteParser(new Data(), delete1, new LinkedHashMap<>());
    deleteParser.parseDeleteStatement(statement, parsedDetail);
    assertEquals(2, parsedDetail.tableToFields.size());
    assertTrue(parsedDetail.tableToFields.containsKey("employees"));
    assertTrue(parsedDetail.tableToFields.containsKey("departments"));
    Iterator<String> iterator = parsedDetail.tableToFields.get("employees").iterator();
    assertEquals("department_id", iterator.next());

    iterator = parsedDetail.tableToFields.get("departments").iterator();
    assertEquals("department_id", iterator.next());
    assertEquals("location", iterator.next());
  }

  @Test
  void parseDeleteTest2() throws JSQLParserException {
    Delete statement = (Delete) CCJSqlParserUtil.parse(delete2);
    ParsedDetail parsedDetail = new ParsedDetail();
    DeleteParser deleteParser = new DeleteParser(new Data(), delete2, new LinkedHashMap<>());
    deleteParser.parseDeleteStatement(statement, parsedDetail);
    assertEquals(2, parsedDetail.tableToFields.size());
    assertTrue(parsedDetail.tableToFields.containsKey("employees"));
    assertTrue(parsedDetail.tableToFields.containsKey("departments"));
    Iterator<String> iterator = parsedDetail.tableToFields.get("employees").iterator();
    assertEquals("department_id", iterator.next());

    iterator = parsedDetail.tableToFields.get("departments").iterator();
    assertEquals("department_id", iterator.next());
    assertEquals("location", iterator.next());
  }
}
