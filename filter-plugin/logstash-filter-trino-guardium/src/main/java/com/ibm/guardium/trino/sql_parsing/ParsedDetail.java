package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.*;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class ParsedDetail {
  static final String UNKNOWN = "(unknown)";
  Map<String, String> aliasToTable = new HashMap<>(); // alias -> table
  Map<String, String> tableToSchema = new HashMap<>();
  Map<String, HashSet<String>> tableToFields = new HashMap<>();
  Map<String, String> tableToType = new HashMap<>();

  void addField(String tableName, String fieldName) {
    tableToFields.computeIfAbsent(tableName, k -> new HashSet<>()).add(fieldName);
  }

  ArrayList<SentenceObject> getParsedObjects(Map<String, String> aliasMap) {
    Map<String, HashSet<String>> result = new HashMap<>();
    ArrayList<SentenceObject> objects = new ArrayList<>();
    if (tableToFields.containsKey(UNKNOWN) && aliasToTable.containsKey(UNKNOWN)) {
      tableToFields.get(aliasToTable.get(UNKNOWN)).addAll(tableToFields.get(UNKNOWN));
    }

    for (Map.Entry<String, HashSet<String>> entry : tableToFields.entrySet()) {
      String realTableName =
          aliasToTable.get(entry.getKey()) != null
              ? aliasToTable.get(entry.getKey())
              : entry.getKey();
      result.computeIfAbsent(realTableName, k -> new HashSet<>()).addAll(entry.getValue());
    }

    for (Map.Entry<String, HashSet<String>> entry : result.entrySet()) {
      String table = entry.getKey();
      HashSet<String> fields = entry.getValue();

      String tableName = table;
      if (aliasMap.containsKey(tableName)) tableName = aliasMap.get(tableName);

      SentenceObject object = new SentenceObject(tableName);
      String type = tableToType.get(tableName);
      if (type == null) type = TABLE;
      object.setType(type);
      object.setSchema(tableToSchema.get(table));
      object.setFields(fields.toArray(new String[0]));
      objects.add(object);
    }

    for (Map.Entry<String, String> entry : tableToSchema.entrySet()) {
      if (!result.containsKey(entry.getKey())) {
        SentenceObject object = new SentenceObject(entry.getKey());
        object.setType(tableToType.get(entry.getKey()));
        objects.add(object);
      }
    }

    return objects;
  }

  void extractTableDetails(Table table) {
    extractTableDetails(table, TABLE);
  }

  void extractTableDetails(Table table, String type) {
    tableToSchema.put(table.getName(), table.getSchemaName());
    tableToType.put(table.getName(), type);
    String alias = (table.getAlias() != null) ? table.getAlias().getName() : table.getName();
    aliasToTable.put(alias, table.getName());
  }

  void extractColumn(Column column, String tableName) {
    String fieldName = column.getColumnName();
    tableToFields.computeIfAbsent(tableName, k -> new HashSet<>()).add(fieldName);
  }

  void addUnknownAlias(Table table) {
    aliasToTable.putIfAbsent(UNKNOWN, table.getName());
  }

  void extractFieldDetails(ExpressionList<Column> columns, String tableName) {
    if (columns != null) {
      for (Column column : columns) extractColumn(column, tableName);
    }
  }

  void extractFieldDetails(List<String> columns, String tableName) {
    if (columns != null) {
      for (String column : columns)
        tableToFields.computeIfAbsent(tableName, k -> new HashSet<>()).add(column);
    }
  }

  static void extractSimpleData(
      Data data,
      String sqlStatement,
      String name,
      String type,
      String action,
      Map<String, String> aliasMap) {

    if (aliasMap.containsKey(name)) name = aliasMap.get(name);
    SentenceObject object = new SentenceObject(name);
    object.setType(type);
    ArrayList<SentenceObject> objects = new ArrayList<>();
    objects.add(object);

    ArrayList<Sentence> sentences = new ArrayList<>();
    Sentence sentence = new Sentence(action);
    sentence.setObjects(objects);
    sentences.add(sentence);

    Construct construct = new Construct();
    construct.setFullSql(data.getOriginalSqlCommand());
    construct.setSentences(sentences);
    data.setConstruct(construct);
  }
}
