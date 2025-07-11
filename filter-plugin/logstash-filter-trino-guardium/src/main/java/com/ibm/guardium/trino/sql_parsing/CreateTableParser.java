package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class CreateTableParser extends JSqlParser {
  Pattern pattern = Pattern.compile("(?i)\\bCREATE.*?TABLE");

  CreateTableParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement create) {
    ParsedDetail parsed = new ParsedDetail();
    parseCreateTable((CreateTable) create, parsed);

    String action = CREATE_TABLE;
    Matcher matcher = pattern.matcher(sqlStatement);
    if (matcher.find()) {
      action = matcher.group();
    }
    extractData(data, parsed, sqlStatement, action);
  }

  void parseCreateTable(CreateTable create, ParsedDetail parsed) {
    Table table = create.getTable();
    parsed.extractTableDetails(table);

    if (create.getColumnDefinitions() != null) {
      for (ColumnDefinition colDef : create.getColumnDefinitions()) {
        parsed.addField(table.getName(), colDef.getColumnName());
      }
    }

    if (create.getSelect() != null) parseInnerSelect(create.getSelect(), parsed, table.getName());

    if (create.getIndexes() != null) {
      for (Index index : create.getIndexes()) {
        if (index instanceof CheckConstraint) {
          Expression expr = ((CheckConstraint) index).getExpression();
          processExpression(expr, parsed, table.getName());
        }
      }
    }
  }
}
