package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.Statement;

import java.util.Map;

import static com.ibm.guardium.databricks.sql_parsing.Vocab.*;

public class SetParser extends JSqlParser {
  SetParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement statement) {
    SetStatement setStatement = (SetStatement) statement;

    ParsedDetail.extractSimpleData(
            data,
            sqlStatement,
            setStatement.getName().toString(),
            setStatement.getEffectParameter() != null ? setStatement.getEffectParameter() : "",
            SET,
            this.aliasMap);
  }
}