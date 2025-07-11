package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class ResetParser extends CustomParser {
  Pattern resetPattern =
      Pattern.compile("RESET\\s+SESSION\\s+([a-zA-Z0-9_.]+);?", Pattern.CASE_INSENSITIVE);

  ResetParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse() throws InvalidStatementException {
    Matcher matcher = resetPattern.matcher(this.sqlStatement);
    if (matcher.find()) {
      String property = matcher.group(1);
      ParsedDetail.extractSimpleData(data, sqlStatement, property, SESSION, RESET, this.aliasMap);
      return;
    }
    throw new InvalidStatementException(
        "The SQL statement [" + sqlStatement + "] is invalid and cannot be parsed ");
  }

  static boolean isReset(String sql) {
    return sql.startsWith("RESET");
  }
}
