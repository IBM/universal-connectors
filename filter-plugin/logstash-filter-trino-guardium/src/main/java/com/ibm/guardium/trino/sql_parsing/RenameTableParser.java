package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;

import java.util.ArrayList;
import java.util.Map;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class RenameTableParser extends JSqlParser {
  RenameTableParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement statement) {
    RenameTableStatement rename = (RenameTableStatement) statement;

    ArrayList<SentenceObject> objects = getSentenceObjects(rename);

    ArrayList<Sentence> sentences = new ArrayList<>();
    Sentence sentence = new Sentence(RENAME);
    sentence.setObjects(objects);
    sentences.add(sentence);

    Construct construct = new Construct();
    construct.setFullSql(data.getOriginalSqlCommand());
    construct.setSentences(sentences);
    data.setConstruct(construct);
  }

  ArrayList<SentenceObject> getSentenceObjects(RenameTableStatement rename) {
    ArrayList<SentenceObject> objects = new ArrayList<>();
    for (Map.Entry<Table, Table> entry : rename.getTableNames()) {
      if (entry.getKey() != null) {
        SentenceObject object = new SentenceObject(getRealName(entry.getKey().getName()));
        object.setType(TABLE);
        objects.add(object);
        if (entry.getValue() != null) {
          object = new SentenceObject(getRealName(entry.getValue().getName()));
          object.setType(TABLE);
          objects.add(object);
        }
      }
    }
    return objects;
  }
}
