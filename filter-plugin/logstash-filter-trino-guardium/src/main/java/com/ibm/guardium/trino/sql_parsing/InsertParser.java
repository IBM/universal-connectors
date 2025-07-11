package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;

import java.util.Map;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class InsertParser extends JSqlParser {

  InsertParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement insert) {
    ParsedDetail parsed = new ParsedDetail();
    parseInsertStatement((Insert) insert, parsed);

    extractData(data, parsed, sqlStatement, INSERT);
  }
}
