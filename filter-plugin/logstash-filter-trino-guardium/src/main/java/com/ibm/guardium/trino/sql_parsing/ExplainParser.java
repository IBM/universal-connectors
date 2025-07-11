package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.statement.ExplainStatement;
import net.sf.jsqlparser.statement.Statement;

import java.util.Map;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class ExplainParser extends JSqlParser {
  ExplainParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement statement) {
    ExplainStatement explainStatement = (ExplainStatement) statement;
    ParsedDetail parsedDetail = new ParsedDetail();
    if (explainStatement.getStatement() != null) {
      parseSelectStatement(explainStatement.getStatement().getPlainSelect(), parsedDetail);
    }

    extractData(data, parsedDetail, sqlStatement, EXPLAIN);
  }
}
