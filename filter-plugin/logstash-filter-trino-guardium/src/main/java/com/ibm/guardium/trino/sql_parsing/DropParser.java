package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.drop.Drop;

import java.util.ArrayList;
import java.util.Map;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class DropParser extends JSqlParser {

  DropParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement statement) {
    ArrayList<SentenceObject> objects = getSentenceObjects((Drop) statement);

    Sentence sentence = new Sentence(DROP);
    sentence.setObjects(objects);
    ArrayList<Sentence> sentences = new ArrayList<>();
    sentences.add(sentence);

    Construct construct = new Construct();
    construct.setFullSql(data.getOriginalSqlCommand());
    construct.setSentences(sentences);
    data.setConstruct(construct);
  }

  private ArrayList<SentenceObject> getSentenceObjects(Drop statement) {
    Drop drop = statement;

    ArrayList<SentenceObject> objects = new ArrayList<>();
    SentenceObject object = new SentenceObject(getRealName(drop.getName().getName()));
    object.setType(drop.getType());
    objects.add(object);

    if (drop.getParameters() != null) {
      for (String parameter : drop.getParameters()) {
        object = new SentenceObject(getRealName(parameter));
        object.setType(drop.getType());
        objects.add(object);
      }
    }
    return objects;
  }
}
