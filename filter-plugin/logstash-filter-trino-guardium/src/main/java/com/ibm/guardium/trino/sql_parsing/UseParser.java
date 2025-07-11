package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class UseParser extends CustomParser {
  Pattern usePattern = Pattern.compile("(?i)^USE\\s+([a-zA-Z0-9_\\.]+)\\s*;?");

  UseParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  static boolean isUse(String sql) {
    return sql.toUpperCase().startsWith("USE");
  }

  @Override
  void parse() throws InvalidStatementException {
    Matcher matcher = usePattern.matcher(sqlStatement);
    if (matcher.find()) {
      ParsedDetail.extractSimpleData(
          data, sqlStatement, matcher.group(1), DATABASE, USE, this.aliasMap);
      return;
    }
    throw new InvalidStatementException(
        "The SQL statement [" + sqlStatement + "] is invalid and cannot be parsed ");
  }
}
