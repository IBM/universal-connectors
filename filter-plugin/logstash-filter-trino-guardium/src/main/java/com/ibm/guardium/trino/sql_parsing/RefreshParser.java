package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class RefreshParser extends CustomParser {
  Pattern pattern =
      Pattern.compile(
          "(?i)(REFRESH|CREATE)\\s+MATERIALIZED\\s+VIEW\\s+([a-zA-Z0-9_$.]+)",
          Pattern.CASE_INSENSITIVE);

  RefreshParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse() throws InvalidStatementException {
    sqlStatement = sqlStatement.trim().replaceAll(";+\\s*$", "");
    Matcher matcher = pattern.matcher(this.sqlStatement);
    if (matcher.find()) {
      String property = matcher.group(2);
      ParsedDetail.extractSimpleData(data, sqlStatement, property, VIEW, REFRESH, this.aliasMap);
      return;
    }
    throw new InvalidStatementException(
        "The SQL statement [" + sqlStatement + "] is invalid and cannot be parsed ");
  }

  static boolean isRefresh(String sql) {
    return sql.startsWith(REFRESH);
  }
}
