package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.index.CreateIndex;

import java.util.ArrayList;
import java.util.Map;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class CreateIndexParser extends JSqlParser {
  CreateIndexParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement statement) {
    Construct construct = new Construct();
    construct.setFullSql(data.getOriginalSqlCommand());

    ArrayList<Sentence> sentences = new ArrayList<>();

    CreateIndex createIndex = (CreateIndex) statement;
    ArrayList<SentenceObject> objects = new ArrayList<>();
    SentenceObject object = new SentenceObject(createIndex.getIndex().getName());
    object.setType(INDEX);
    objects.add(object);

    object = new SentenceObject(createIndex.getTable().getName());
    object.setType(TABLE);
    objects.add(object);

    Sentence sentence = new Sentence(CREATE_INDEX);
    sentence.setObjects(objects);
    sentences.add(sentence);

    construct.setSentences(sentences);
    data.setConstruct(construct);
  }
}
