package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.update.Update;

import java.util.Map;

import static com.ibm.guardium.databricks.sql_parsing.Vocab.*;

public class UpdateParser extends JSqlParser {

  UpdateParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement update) {
    ParsedDetail parsed = new ParsedDetail();
    parseUpdateStatement((Update) update, parsed);

    extractData(data, parsed, sqlStatement, UPDATE);
  }
}