package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.comment.Comment;

import java.util.ArrayList;
import java.util.Map;

import static com.ibm.guardium.databricks.sql_parsing.Vocab.*;

public class CommentParser extends JSqlParser {
  CommentParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement statement) {
    ArrayList<SentenceObject> objects = getSentenceObjects((Comment) statement);

    ArrayList<Sentence> sentences = new ArrayList<>();
    Sentence sentence = new Sentence(COMMENT);
    sentence.setObjects(objects);
    sentences.add(sentence);

    Construct construct = new Construct();
    construct.setFullSql(data.getOriginalSqlCommand());
    construct.setSentences(sentences);
    data.setConstruct(construct);
  }

  private ArrayList<SentenceObject> getSentenceObjects(Comment statement) {
    Comment comment = statement;

    ArrayList<SentenceObject> objects = new ArrayList<>();
    if (comment.getComment() != null) {
      SentenceObject object = new SentenceObject(comment.getComment().getValue());
      object.setType(COMMENT);
      objects.add(object);
    }

    if (comment.getTable() != null) {
      SentenceObject object = new SentenceObject(comment.getTable().getName());
      object.setType(TABLE);
      objects.add(object);
    }
    if (comment.getColumn() != null) {
      SentenceObject object = new SentenceObject(comment.getColumn().getColumnName());
      object.setType(COLUMN);
      objects.add(object);
    }
    if (comment.getView() != null) {
      SentenceObject object = new SentenceObject(comment.getView().getName());
      object.setType(VIEW);
      objects.add(object);
    }

    return objects;
  }
}