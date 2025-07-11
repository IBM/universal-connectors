package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.Map;

import static com.ibm.guardium.databricks.sql_parsing.Vocab.*;

public class SelectParser extends JSqlParser {

  SelectParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement select) {
    ParsedDetail parsed = new ParsedDetail();
    parseSelectStatement(((Select) select).getPlainSelect(), parsed);

    extractData(data, parsed, sqlStatement, SELECT);
  }
}