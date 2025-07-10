package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.databricks.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.Statement;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ibm.guardium.databricks.sql_parsing.Vocab.*;

public class DescribeParser extends CustomParser {
  Pattern pattern =
          Pattern.compile(
                  "(?i)^\\s*DESC(?:RIBE)?(?:\\s+(DATABASE|SCHEMA|TABLE|FUNCTION|HISTORY))?(?:\\s+(EXTENDED|FORMATTED))?(?:\\s+IF\\s+EXISTS)?\\s+((?:[\"`]?[\\w$]+[\"`]?\\.)*(?:[\"`]?[\\w$]+[\"`]?\\s*(?:\\([^)]*\\))?))");

  DescribeParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse() throws InvalidStatementException {
    Matcher matcher = pattern.matcher(sqlStatement);

    if (matcher.find()) {
      String objectType = matcher.group(1);
      String name = matcher.group(3);
      if (objectType == null) objectType = TABLE;
      if (name == null)
        throw new InvalidStatementException(
                "The SQL statement [\" + sqlStatement + \"] is invalid and cannot be parsed \");");

      ParsedDetail.extractSimpleData(data, sqlStatement, name, objectType, DESCRIBE, this.aliasMap);
      return;
    }
    throw new InvalidStatementException(
            "The SQL statement [\" + sqlStatement + \"] is invalid and cannot be parsed \");");
  }

  static boolean isDescribeCommand(String sql) {
    return sql.toUpperCase().startsWith(DESC);
  }
}