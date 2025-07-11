package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class ShowParser extends CustomParser {

  private static final String IDENTIFIER = "(?:[a-zA-Z_][a-zA-Z0-9_`\"$]*|`[^`]+`|\"[^\"]+\")";
  private static final String QUALIFIED_NAME =
      "(?:" + IDENTIFIER + "(?:\\." + IDENTIFIER + "){0,2})";

  private static final String SHOW_REGEX =
      "(?i)^\\s*SHOW\\s+"
          + "((?:EXTENDED\\s+)?(?:TABLES?|VIEWS?|MATERIALIZED\\s+VIEWS?|DATABASES|SCHEMAS|CATALOGS|COLUMNS|TBLPROPERTIES|FUNCTIONS?|GRANTS|SESSION|CURRENT\\s+ROLES|PARTITIONS|GRANT|REVOKE|"
          + "CREATE\\s+(?:TABLE|VIEW|MATERIALIZED\\s+VIEW|FUNCTION|SCHEMA)|VERSION|VOLUMES?|TRANSACTIONS|COMPACTIONS|CONF|LOCK|INDEXES|ROLES|STATS|QUERIES))"
          + // Group 1: SHOW command
          "(?:\\s+(?:FROM|IN)\\s+("
          + QUALIFIED_NAME
          + "))?"
          + // Group 2: FROM/IN object
          "(?:\\s+FROM\\s+("
          + QUALIFIED_NAME
          + "))?"
          + // Group 3: Second FROM
          "(?:\\s+LIKE\\s+(['\"][^'\"]+['\"]))?"
          + // LIKE clause (not captured)
          "(?:\\s+ON\\s+(?:(?:TABLE|VIEW|MATERIALIZED\\s+VIEW|FUNCTION|SCHEMA|DATABASE)\\s+)?"
          + "("
          + QUALIFIED_NAME
          + "))?"
          + // Group 4: ON object
          "(?:\\s+FOR\\s+([a-zA-Z_][a-zA-Z0-9_@-]*))?"
          + // Group 5: FOR user
          "(?:\\s+("
          + QUALIFIED_NAME
          + "))?"
          + // Group 6: trailing object (e.g., SHOW CREATE TABLE my_table)
          "(?:\\s+WHERE\\s+[^;]+)?";
  Pattern pattern = Pattern.compile(SHOW_REGEX);

  ShowParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse() throws InvalidStatementException {
    sqlStatement = sqlStatement.trim().replaceAll(";+\\s*$", "");
    Matcher m = pattern.matcher(sqlStatement);
    if (m.find()) {
      String keyword = m.group(1).toUpperCase().replaceAll("\\s+", " ").trim();
      String objectName = firstNonNullGroup(m);

      Construct construct = getConstruct(objectName, keyword, getType(keyword.toLowerCase()));
      data.setConstruct(construct);
      return;
    }
    throw new InvalidStatementException(
        "The SQL statement [" + sqlStatement + "] is invalid and cannot be parsed ");
  }

  private static String firstNonNullGroup(Matcher m) {
    for (int group = 2; group <= m.groupCount(); group++) {
      String value = m.group(group);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  String getType(String keyword) {
    if (keyword.contains("column")
        || keyword.contains("create table")
        || keyword.contains("tblproperties")) {
      return TABLE;
    } else if (keyword.contains("database")
        || keyword.contains("schema")
        || keyword.contains("catalog")
        || keyword.contains("table")
        || keyword.contains("view")) {
      return DATABASE;
    } else if (keyword.contains("role")) {
      return ROLE;
    } else if (keyword.contains("function")) {
      return FUNCTION;
    } else if (keyword.contains("session")) {
      return SESSION;
    } else if (keyword.contains("user") || keyword.contains("current role")) {
      return USER;
    } else if (keyword.contains("grant") || keyword.contains("revoke")) {
      String sql = this.sqlStatement.toUpperCase();
      if (sql.contains("ON")) {
        if (sql.contains(TABLE)) return TABLE;
        if (sql.contains(VIEW)) return VIEW;
        if (sql.contains(DATABASE)) return DATABASE;
        return DB_OBJECT;
      }
      return ROLE_USER;
    }
    return DB_OBJECT;
  }

  private Construct getConstruct(String objectName, String keyword, String type) {
    ArrayList<SentenceObject> objects = new ArrayList<>();

    if (objectName != null) {
      SentenceObject object = new SentenceObject(getRealName(objectName));
      object.setType(type);
      objects.add(object);
    }

    ArrayList<Sentence> sentences = new ArrayList<>();
    Sentence sentence = new Sentence(SHOW + " " + keyword);
    sentence.setObjects(objects);
    sentences.add(sentence);

    Construct construct = new Construct();
    construct.setFullSql(data.getOriginalSqlCommand());
    construct.setSentences(sentences);
    return construct;
  }

  static boolean isShowCommand(String sql) {
    return sql.toUpperCase().startsWith(SHOW);
  }
}
