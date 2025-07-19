package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.databricks.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ibm.guardium.databricks.sql_parsing.Vocab.*;

public class PrivilegeParser extends CustomParser {
  Pattern grantPattern =
          Pattern.compile(
                  "(?i)GRANT\\s+(\\w+)\\s*\\(([^)]+)\\)\\s+ON\\s+(\\w+)\\s+TO\\s+(\\w+)(?:\\s+WITH\\s+GRANT\\s+OPTION)?\\s*;",
                  Pattern.CASE_INSENSITIVE);
  Pattern revokePattern =
          Pattern.compile(
                  "(?i)REVOKE\\s+([A-Z ,]+)"
                          + // Privileges
                          "(?:\\s*\\(([^)]+)\\))?"
                          + // Optional column list
                          "\\s+ON\\s+((?:"
                          + "\"[^\"]+\""
                          + // Quoted identifier
                          "|[a-zA-Z0-9_$]+"
                          + // Or unquoted identifier
                          ")(?:\\.(?:\"[^\"]+\"|[a-zA-Z0-9_$]+))*)"
                          + // Dot-separated
                          "\\s+FROM\\s+((?:\"[^\"]+\"|[a-zA-Z0-9_]+)(?:\\s*,\\s*(?:\"[^\"]+\"|[a-zA-Z0-9_]+))*)",
                  Pattern.CASE_INSENSITIVE);

  static boolean isPrivilege(String statement) {
    return (statement.toUpperCase().startsWith(GRANT) && statement.contains("("))
            || statement.toUpperCase().startsWith(REVOKE);
  }

  PrivilegeParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse() throws InvalidStatementException {
    if (sqlStatement.startsWith(GRANT)) {
      Matcher matcher = grantPattern.matcher(sqlStatement);
      if (matcher.matches()) {
        parseGrant(data, sqlStatement, matcher);
        return;
      }
    } else if (sqlStatement.startsWith(REVOKE)) {
      sqlStatement = sqlStatement.trim().replaceAll(";$", "");
      Matcher matcher = revokePattern.matcher(sqlStatement);
      if (matcher.matches()) {
        parseRevoke(data, sqlStatement, matcher);
        return;
      }
    }

    throw new InvalidStatementException(
            "The SQL statement [" + sqlStatement + "] is invalid and cannot be parsed ");
  }

  void parseGrant(Data data, String sqlStatement, Matcher matcher) {
    String privileges = matcher.group(1);
    String columns = matcher.group(2);
    String table = matcher.group(3);
    String users = matcher.group(4);
    ArrayList<SentenceObject> objects = new ArrayList<>();

    if (table != null) {
      SentenceObject object = new SentenceObject(getRealName(table));
      object.setType(DB_OBJECT);

      if (columns != null) {
        String[] columnList = columns.split(",");
        String[] trimmedColumns = new String[columnList.length];
        int i = 0;
        for (String column : columnList) {
          trimmedColumns[i++] = column.trim();
        }
        object.setFields(trimmedColumns);
      }
      objects.add(object);
    }

    if (privileges != null) {
      for (String privilege : privileges.split(",")) {
        SentenceObject object = new SentenceObject(getRealName(privilege.trim()));
        object.setType(PRIVILEGE);
        objects.add(object);
      }
    }

    if (users != null) {
      for (String user : users.split(",")) {
        SentenceObject object = new SentenceObject(getRealName(user.trim()));
        object.setType(USER);
        objects.add(object);
      }
    }

    Sentence sentence = new Sentence(GRANT);
    sentence.setObjects(objects);
    ArrayList<Sentence> sentences = new ArrayList<>();
    sentences.add(sentence);

    Construct construct = new Construct();
    construct.setFullSql(data.getOriginalSqlCommand());
    construct.setSentences(sentences);
    data.setConstruct(construct);
  }

  void parseRevoke(Data data, String sqlStatement, Matcher matcher) {
    String privileges = matcher.group(1);
    String columns = matcher.group(2);
    String table = matcher.group(3);
    String users = matcher.group(4);
    ArrayList<SentenceObject> objects = new ArrayList<>();

    if (table != null) {
      SentenceObject object = new SentenceObject(getRealName(table));
      object.setType(DB_OBJECT);

      if (columns != null) {
        String[] columnList = columns.split(",");
        String[] trimmedColumns = new String[columnList.length];
        int i = 0;
        for (String column : columnList) {
          trimmedColumns[i++] = column.trim();
        }
        object.setFields(trimmedColumns);
      }
      objects.add(object);
    }

    if (privileges != null) {
      for (String privilege : privileges.split(",")) {
        SentenceObject object = new SentenceObject(getRealName(privilege.trim()));
        object.setType(PRIVILEGE);
        objects.add(object);
      }
    }

    if (users != null) {
      for (String user : users.split(",")) {
        SentenceObject object = new SentenceObject(getRealName(user.trim()));
        object.setType(USER);
        objects.add(object);
      }
    }

    Sentence sentence = new Sentence(REVOKE);
    sentence.setObjects(objects);
    ArrayList<Sentence> sentences = new ArrayList<>();
    sentences.add(sentence);

    Construct construct = new Construct();
    construct.setFullSql(data.getOriginalSqlCommand());
    construct.setSentences(sentences);
    data.setConstruct(construct);
  }
}