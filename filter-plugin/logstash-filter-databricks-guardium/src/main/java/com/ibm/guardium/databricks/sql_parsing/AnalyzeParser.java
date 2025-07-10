package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.analyze.Analyze;

import java.util.Map;

import static com.ibm.guardium.databricks.sql_parsing.Vocab.*;

public class AnalyzeParser extends JSqlParser {
  AnalyzeParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement statement) {
    Analyze analyze = (Analyze) statement;

    ParsedDetail.extractSimpleData(
            data, sqlStatement, analyze.getTable().getName(), TABLE, ANALYZE, this.aliasMap);
  }
}