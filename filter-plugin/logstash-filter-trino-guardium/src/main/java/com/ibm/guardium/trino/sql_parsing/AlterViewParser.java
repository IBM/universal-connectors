package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.select.Select;

import java.util.Map;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class AlterViewParser extends JSqlParser {
  AlterViewParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement statement) {
    ParsedDetail parsed = new ParsedDetail();
    parseAlter((AlterView) statement, parsed);

    extractData(data, parsed, sqlStatement, ALTER);
  }

  void parseAlter(AlterView alter, ParsedDetail parsed) {
    Table table = alter.getView();
    parsed.extractTableDetails(table, VIEW);

    Select select = alter.getSelect();
    if (select != null && select.getPlainSelect() != null)
      parseSelectStatement(select.getPlainSelect(), parsed);
  }
}
