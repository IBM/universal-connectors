package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.grant.Grant;

import java.util.ArrayList;
import java.util.Map;

import static com.ibm.guardium.databricks.sql_parsing.Vocab.*;

public class GrantParser extends JSqlParser {

  GrantParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement statement) {
    Grant grant = (Grant) statement;
    ArrayList<SentenceObject> objects = new ArrayList<>();

    if (grant.getObjectName() != null) {
      SentenceObject object = new SentenceObject(getRealName(grant.getObjectName()));
      object.setType(DB_OBJECT);
      objects.add(object);
    }

    if (grant.getPrivileges() != null) {
      for (String privilege : grant.getPrivileges()) {
        SentenceObject object = new SentenceObject(getRealName(privilege));
        object.setType(PRIVILEGE);
        objects.add(object);
      }
    }

    if (grant.getUsers() != null) {
      for (String user : grant.getUsers()) {
        SentenceObject object = new SentenceObject(getRealName(user));
        object.setType(USER);
        objects.add(object);
      }
    }

    if (grant.getRole() != null) {
      SentenceObject object = new SentenceObject(getRealName(grant.getRole()));
      object.setType(ROLE);
      objects.add(object);
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
}