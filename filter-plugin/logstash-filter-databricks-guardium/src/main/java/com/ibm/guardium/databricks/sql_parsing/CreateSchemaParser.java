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

public class CreateSchemaParser extends CustomParser {
  Pattern pattern =
          Pattern.compile(
                  "(?i)^CREATE\\s+SCHEMA\\s+(IF\\s+NOT\\s+EXISTS\\s+)?([a-zA-Z0-9_\\.]+)"
                          + "(?:\\s+AUTHORIZATION\\s+([a-zA-Z0-9_]+))?"
                          + "(?:\\s+COMMENT\\s+'([^']*)')?"
                          + "(?:\\s+WITH\\s*\\(([^)]*)\\))?",
                  Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  CreateSchemaParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  static boolean isCreateSchema(String sql) {
    return sql.toUpperCase().startsWith(CREATE_SCHEMA);
  }

  @Override
  void parse() throws InvalidStatementException {
    sqlStatement = sqlStatement.trim().replaceAll(";+\\s*$", "");
    Matcher matcher = pattern.matcher(sqlStatement);
    if (matcher.find()) {
      String schemaName = matcher.group(2);
      String authorizationUser = matcher.group(3);
      String comment = matcher.group(4);
      ArrayList<SentenceObject> objects = new ArrayList<>();
      if (schemaName != null) objects.add(getObject(schemaName, SCHEMA));

      if (authorizationUser != null) objects.add(getObject(authorizationUser, USER));

      if (comment != null) objects.add(getObject(comment, COMMENT));

      String propertiesString = matcher.group(5);
      if (propertiesString != null) objects.addAll(getPropertyObjects(propertiesString));

      ArrayList<Sentence> sentences = new ArrayList<>();
      Sentence sentence = new Sentence(CREATE_SCHEMA);
      sentence.setObjects(objects);
      sentences.add(sentence);

      Construct construct = new Construct();
      construct.setFullSql(data.getOriginalSqlCommand());
      construct.setSentences(sentences);
      data.setConstruct(construct);
      return;
    }
    throw new InvalidStatementException(
            "The SQL statement [" + sqlStatement + "] is invalid and cannot be parsed ");
  }

  SentenceObject getObject(String name, String type) {
    SentenceObject object = new SentenceObject(name);
    object.setType(type);
    return object;
  }

  ArrayList<SentenceObject> getPropertyObjects(String propertiesString) {
    ArrayList<SentenceObject> objects = new ArrayList<>();
    String[] pairs = propertiesString.split(",");
    for (String pair : pairs) {
      String[] kv = pair.trim().split("=");
      if (kv.length == 2) {
        String key = kv[0].trim();
        objects.add(getObject(key, PROPERTY));
      }
    }
    return objects;
  }
}