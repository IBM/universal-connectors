package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class InsertOverwriteParser extends CustomParser {
  static final String REGEX =
      "(?is)^\\s*INSERT\\s+OVERWRITE\\s+"
          + "(?:"
          + "TABLE\\s+([a-zA-Z0-9_]+)"
          + "|"
          + "(?:DIRECTORY\\s+['\"]([^'\"]+)['\"])"
          + ")"
          + "(?:\\s+(?:PARTITION|PARTITIONED\\s+BY)\\s*\\(([^)]*)\\))?"
          + "(?:\\s+USING\\s+(\\w+))?"
          + "\\s+(SELECT\\b.*)";
  Pattern pattern = Pattern.compile(REGEX);

  // Group 6: SELECT query
  InsertOverwriteParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse() throws InvalidStatementException {
    Matcher m = pattern.matcher(sqlStatement);
    if (m.find()) {
      String name = null;
      if (m.group(1) != null) name = m.group(1);
      if (m.group(2) != null && name == null) name = m.group(2);
      if (m.group(3) != null && name == null) name = m.group(3);

      if (name == null)
        throw new InvalidStatementException(
            "The SQL statement [" + sqlStatement + "] is invalid and cannot be parsed ");
      if (m.group(5) != null) {
        String innerSql = m.group(5);

        try {
          Statement statement = CCJSqlParserUtil.parse(innerSql);
          SelectParser selectParser = new SelectParser(data, sqlStatement, aliasMap);
          selectParser.parse(statement);
        } catch (JSQLParserException e) {
          throw new InvalidStatementException(
              "The SQL statement [" + sqlStatement + "] is invalid and cannot be parsed ");
        }
      }

      Construct construct = getConstruct(name);
      data.setConstruct(construct);
      return;
    }

    throw new InvalidStatementException(
        "The SQL statement [" + sqlStatement + "] is invalid and cannot be parsed ");
  }

  private Construct getConstruct(String name) {
    Construct construct = data.getConstruct();
    if (construct == null) {
      construct = new Construct();
    }

    ArrayList<Sentence> sentences = construct.getSentences();
    if (sentences == null) {
      sentences = new ArrayList<>();
    }

    ArrayList<SentenceObject> objects = null;
    if (sentences.get(0) != null) objects = sentences.get(0).getObjects();
    if (objects == null) objects = new ArrayList<>();

    SentenceObject object = new SentenceObject(getRealName(name));
    String type = sqlStatement.startsWith(INSERT_OVERWRITE_DIRECTORY) ? DIRECTORY : TABLE;
    object.setType(type);
    objects.add(object);
    Sentence sentence = new Sentence(INSERT_OVERWRITE);
    sentences = new ArrayList<>();
    sentence.setObjects(objects);
    sentences.add(sentence);
    construct.setSentences(sentences);
    construct.setFullSql(data.getOriginalSqlCommand());

    return construct;
  }

  static boolean isInsertOverwrite(String sql) {
    return sql.toUpperCase().startsWith(INSERT_OVERWRITE);
  }
}
