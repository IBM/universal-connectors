package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;

import java.util.Map;

import static com.ibm.guardium.databricks.sql_parsing.Vocab.*;

public class DeleteParser extends JSqlParser {

  DeleteParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement delete) {
    ParsedDetail parsed = new ParsedDetail();
    parseDeleteStatement((Delete) delete, parsed);

    extractData(data, parsed, sqlStatement, DELETE);
  }
}