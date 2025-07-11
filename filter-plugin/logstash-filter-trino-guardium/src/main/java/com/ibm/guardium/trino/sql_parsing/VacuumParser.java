package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;

import java.util.ArrayList;
import java.util.Map;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class VacuumParser extends CustomParser {
  VacuumParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse() throws InvalidStatementException {
    String[] parts = sqlStatement.split(" ");
    if (parts.length < 2)
      throw new InvalidStatementException(
          "The SQL statement [" + sqlStatement + "] is invalid and cannot be parsed ");

    String table = parts[1];

    SentenceObject object =
        new SentenceObject(getRealName(table.replace(";", "").replace("'", "")));
    object.setType(TABLE);
    ArrayList<SentenceObject> objects = new ArrayList<>();
    objects.add(object);
    Sentence sentence = new Sentence(VACUUM);
    sentence.setObjects(objects);
    ArrayList<Sentence> sentences = new ArrayList<>();
    sentences.add(sentence);
    Construct construct = new Construct();
    construct.setSentences(sentences);
    construct.setFullSql(data.getOriginalSqlCommand());

    data.setConstruct(construct);
  }

  static boolean isVacuum(String sql) {
    return sql.startsWith(VACUUM);
  }
}
