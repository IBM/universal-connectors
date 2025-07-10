package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.databricks.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ibm.guardium.databricks.sql_parsing.Vocab.OPTIMIZE;
import static com.ibm.guardium.databricks.sql_parsing.Vocab.TABLE;

public class OptimizeParser extends CustomParser {
  private static final String OPTIMIZE_REGEX =
          "(?is)^\\s*OPTIMIZE\\s+(?:TABLE\\s+)?"
                  + "(?<table>[a-zA-Z_][a-zA-Z0-9_$.]*)"
                  + "(?:\\s+WHERE\\s+(?<where>.*?))?"
                  + "(?:\\s+ZORDER\\s+BY\\s*\\((?<zorder>[^)]+)\\))?"
                  + "\\s*;?\\s*$";

  // Fixed pattern with proper quantifier and escaping
  Pattern wherePattern = Pattern.compile("(?i)(?<!['\"])\\b([a-zA-Z_][a-zA-Z0-9_$.]*)\\b(?!['\"])");

  Pattern pattern = Pattern.compile(OPTIMIZE_REGEX);
  private static final ArrayList<String> KEYWORDS =
          new ArrayList<String>() {
            {
              add("AND");
              add("OR");
              add("NOT");
              add("IN");
              add("IS");
              add("BETWEEN");
              add("LIKE");
            }
          };

  OptimizeParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse() throws InvalidStatementException {
    Matcher matcher = pattern.matcher(sqlStatement);

    if (!matcher.matches())
      throw new InvalidStatementException(
              "The SQL statement [" + sqlStatement + "] is invalid and cannot be parsed ");

    SentenceObject object = new SentenceObject(getRealName(matcher.group("table")));
    object.setType(TABLE);
    HashSet<String> fields = getFields(matcher);

    object.setFields(fields.toArray(new String[0]));
    ArrayList<SentenceObject> objects = new ArrayList<>();
    objects.add(object);
    Sentence sentence = new Sentence(OPTIMIZE);
    sentence.setObjects(objects);
    ArrayList<Sentence> sentences = new ArrayList<>();
    sentences.add(sentence);
    Construct construct = new Construct();
    construct.setSentences(sentences);
    construct.setFullSql(data.getOriginalSqlCommand());
    data.setConstruct(construct);
  }

  private HashSet<String> getFields(Matcher matcher) {
    HashSet<String> fields = new HashSet<>();
    String whereClause = matcher.group("where");
    if (whereClause != null) {
      Matcher colMatcher = wherePattern.matcher(whereClause);
      while (colMatcher.find()) {
        String field = colMatcher.group(1).trim();
        if (!KEYWORDS.contains(field.toUpperCase())) fields.add(field);
      }
    }

    String zorderClause = matcher.group("zorder");
    if (zorderClause != null) {
      for (String col : zorderClause.split(",")) {
        fields.add(col.trim());
      }
    }
    return fields;
  }

  static boolean isOptimize(String sql) {
    return sql.toUpperCase().startsWith(OPTIMIZE);
  }
}