package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;

import java.util.List;
import java.util.Map;

import static com.ibm.guardium.databricks.sql_parsing.Vocab.*;

public class AlterParser extends JSqlParser {

  AlterParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  static String removeWithCheckOption(String sql) {
    if (sql.toUpperCase().startsWith(ALTER))
      return sql.replaceAll("(?i)\\s+WITH\\s+(CASCADED\\s+|LOCAL\\s+)?CHECK\\s+OPTION\\s*;?", "");

    return sql;
  }

  @Override
  void parse(Statement statement) {
    ParsedDetail parsed = new ParsedDetail();
    parseAlter((Alter) statement, parsed);

    extractData(data, parsed, sqlStatement, ALTER);
  }

  void parseAlter(Alter alter, ParsedDetail parsed) {
    Table table = alter.getTable();
    parsed.extractTableDetails(table, TABLE);

    // Process each ALTER operation
    for (AlterExpression alterExpression : alter.getAlterExpressions()) {
      if (alterExpression.getColumnName() != null)
        parsed.addField(table.getName(), alterExpression.getColumnName());
      if (alterExpression.getColumnOldName() != null)
        parsed.addField(table.getName(), alterExpression.getColumnOldName());
      if (alterExpression.getIndex() != null) {
        Index index = alterExpression.getIndex();
        if (index instanceof CheckConstraint) {
          Expression expr = ((CheckConstraint) alterExpression.getIndex()).getExpression();
          processExpression(expr, parsed, table.getName());
        } else if (index instanceof ForeignKeyIndex) {
          ForeignKeyIndex foreignKeyIndex = (ForeignKeyIndex) alterExpression.getIndex();
          List<String> columnNames = foreignKeyIndex.getReferencedColumnNames();
          parsed.extractFieldDetails(columnNames, foreignKeyIndex.getTable().getName());
          parsed.extractFieldDetails(foreignKeyIndex.getColumnsNames(), table.getName());
        } else if (index instanceof NamedConstraint) {
          parsed.extractFieldDetails(index.getColumnsNames(), table.getName());
        }
      }
    }
  }
}