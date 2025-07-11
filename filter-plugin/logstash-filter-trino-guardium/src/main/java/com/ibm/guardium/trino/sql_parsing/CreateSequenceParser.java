package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;

import java.util.ArrayList;
import java.util.Map;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class CreateSequenceParser extends JSqlParser {
  CreateSequenceParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement statement) {
    CreateSequence create = (CreateSequence) statement;
    Construct construct = new Construct();
    construct.setFullSql(data.getOriginalSqlCommand());

    ArrayList<Sentence> sentences = new ArrayList<>();

    ArrayList<SentenceObject> objects = new ArrayList<>();
    SentenceObject object = new SentenceObject(getRealName(create.getSequence().getName()));
    object.setType(SEQUENCE);
    objects.add(object);

    Sentence sentence = new Sentence(CREATE_SEQUENCE);
    sentence.setObjects(objects);
    sentences.add(sentence);

    construct.setSentences(sentences);
    data.setConstruct(construct);
  }
}
