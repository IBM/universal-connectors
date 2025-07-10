package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.OutputClause;
import net.sf.jsqlparser.statement.ParenthesedStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.delete.ParenthesedDelete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.insert.ParenthesedInsert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ibm.guardium.databricks.sql_parsing.ParsedDetail.UNKNOWN;

public abstract class JSqlParser extends ANSISqlParser {
  JSqlParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  abstract void parse(Statement statement);

  void extractData(Data data, ParsedDetail parsed, String sqlStatement, String action) {
    ArrayList<Sentence> sentences = new ArrayList<>();
    Sentence sentence = new Sentence(action);
    sentence.setObjects(parsed.getParsedObjects(this.aliasMap));
    sentences.add(sentence);

    Construct construct = new Construct();
    construct.setFullSql(data.getOriginalSqlCommand());
    construct.setSentences(sentences);
    data.setConstruct(construct);
  }

  void parseSelectStatement(PlainSelect plainSelect, ParsedDetail parsed) {
    // Process FROM clause
    String tableName = processFromItem(plainSelect.getFromItem(), parsed);

    // Process JOIN clauses
    if (plainSelect.getJoins() != null) {
      processJoins(plainSelect.getJoins(), parsed);
    }

    // Process WHERE clause
    if (plainSelect.getWhere() != null) {
      processExpression(plainSelect.getWhere(), parsed, tableName);
    }

    // Process SELECT items
    for (SelectItem selectItem : plainSelect.getSelectItems()) {
      processSelectItem(selectItem, parsed, tableName);
    }

    // Group by
    if (plainSelect.getGroupBy() != null
            && plainSelect.getGroupBy().getGroupByExpressionList() != null) {
      for (Object expression : plainSelect.getGroupBy().getGroupByExpressionList())
        if (expression instanceof Expression)
          processExpression((Expression) expression, parsed, tableName);
    }

    // Having by
    if (plainSelect.getHaving() != null)
      processExpression(plainSelect.getHaving(), parsed, tableName);

    if (plainSelect.getWithItemsList() != null) {
      for (WithItem item : plainSelect.getWithItemsList()) {
        ProcessWithItem(item, parsed);
      }
    }
  }

  void ProcessWithItem(WithItem withItem, ParsedDetail parsed) {
    if (withItem.getParenthesedStatement() != null)
      parseParenthesedStatements(withItem.getParenthesedStatement(), parsed);
  }

  String processFromItem(FromItem fromItem, ParsedDetail parsed) {
    if (fromItem instanceof Table) {
      Table table = (Table) fromItem;
      parsed.addUnknownAlias(table);
      parsed.extractTableDetails(table);
      return table.getName();
    } else if (fromItem instanceof ParenthesedFromItem) {
      ParenthesedFromItem pfi = (ParenthesedFromItem) fromItem;
      FromItem innerItem = pfi.getFromItem();
      return processFromItem(innerItem, parsed);
    }
    return UNKNOWN;
  }

  void processExpression(Expression expression, ParsedDetail parsed, String tableName) {
    if (expression == null) {
      return;
    }

    if (expression instanceof Column) {
      Column column = (Column) expression;
      String tableAlias = column.getTable() != null ? column.getTable().getName() : null;
      String realTable =
              (tableAlias != null)
                      ? parsed.aliasToTable.getOrDefault(tableAlias, tableAlias)
                      : tableName;
      parsed.addField(realTable, column.getColumnName());
    } else if (expression instanceof BinaryExpression) {
      // Binary expressions like =, <>, >, <, AND, OR
      BinaryExpression binary = (BinaryExpression) expression;
      processExpression(binary.getLeftExpression(), parsed, tableName);
      processExpression(binary.getRightExpression(), parsed, tableName);
    } else if (expression instanceof ExistsExpression) {
      ExistsExpression existsExpr = (ExistsExpression) expression;
      if (existsExpr.getRightExpression() != null) {
        processExpression(existsExpr.getRightExpression(), parsed, tableName);
      }
    } else if (expression instanceof InExpression) {
      InExpression inExpr = (InExpression) expression;
      if (inExpr.getLeftExpression() != null) {
        processExpression(inExpr.getLeftExpression(), parsed, tableName);
      }
      if (inExpr.getRightExpression() != null) {
        processExpression(inExpr.getRightExpression(), parsed, tableName);
      }
    } else if (expression instanceof Function) {
      Function function = (Function) expression;
      if (function.getParameters() != null) {
        for (Expression param : function.getParameters().getExpressions()) {
          processExpression(param, parsed, tableName);
        }
      }
    } else if (expression instanceof ParenthesedStatement) {
      parseParenthesedStatements((ParenthesedStatement) expression, parsed);
    } else if (expression instanceof ExpressionList) {
      ExpressionList expressionList = (ExpressionList) expression;
      for (Object expr : expressionList) {
        if (expr instanceof Expression) processExpression((Expression) expr, parsed, tableName);
      }
    } else if (expression instanceof CaseExpression) {
      CaseExpression caseExpr = (CaseExpression) expression;
      if (caseExpr.getSwitchExpression() != null) {
        processExpression(caseExpr.getSwitchExpression(), parsed, tableName);
      }
      for (WhenClause when : caseExpr.getWhenClauses()) {
        processExpression(when.getWhenExpression(), parsed, tableName);
        processExpression(when.getThenExpression(), parsed, tableName);
      }
      if (caseExpr.getElseExpression() != null) {
        processExpression(caseExpr.getElseExpression(), parsed, tableName);
      }
    } else if (expression instanceof Between) {
      Between between = (Between) expression;
      processExpression(between.getLeftExpression(), parsed, tableName);
      processExpression(between.getBetweenExpressionEnd(), parsed, tableName);
      processExpression(between.getBetweenExpressionStart(), parsed, tableName);
    }
  }

  void processSelectItem(SelectItem selectItem, ParsedDetail parsed, String tableName) {
    Expression expression = selectItem.getExpression();
    processExpression(expression, parsed, tableName);
  }

  void processJoins(List<Join> joins, ParsedDetail parsed) {
    for (Join join : joins) {
      String tableName = processFromItem(join.getRightItem(), parsed);
      if (join.getOnExpressions() != null) {
        for (Expression exp : join.getOnExpressions()) processExpression(exp, parsed, tableName);
      }
    }
  }

  void parseUpdateStatement(Update update, ParsedDetail parsed) {
    Table table = update.getTable();
    parsed.extractTableDetails(table);
    parsed.addUnknownAlias(table);
    String tableName = table.getName();

    if (update.getUpdateSets() != null) {
      for (UpdateSet updateSet : update.getUpdateSets()) {
        for (Column column : updateSet.getColumns()) {
          parsed.extractColumn(column, tableName);
        }

        for (Expression expr : updateSet.getValues()) {
          processExpression(expr, parsed, tableName);
        }
      }
    }

    // From clause
    if (update.getFromItem() != null) tableName = processFromItem(update.getFromItem(), parsed);

    // Process Joins
    if (update.getJoins() != null) processJoins(update.getJoins(), parsed);

    // where clause
    if (update.getWhere() != null) processExpression(update.getWhere(), parsed, tableName);
  }

  void parseInsertStatement(Insert insert, ParsedDetail parsed) {
    Table table = insert.getTable();
    parsed.extractTableDetails(table);
    parsed.extractFieldDetails(insert.getColumns(), table.getName());

    if (insert.getValues() != null && insert.getValues().getExpressions() != null) {
      for (Expression expr : insert.getValues().getExpressions()) {
        processExpression(expr, parsed, table.getName());
      }
    }
  }

  void parseDeleteStatement(Delete delete, ParsedDetail parsed) {
    Table table = delete.getTable();
    parsed.extractTableDetails(table);

    // Delete joins
    if (delete.getJoins() != null) processJoins(delete.getJoins(), parsed);

    if (delete.getWhere() != null) processExpression(delete.getWhere(), parsed, table.getName());
  }

  void parseParenthesedStatements(ParenthesedStatement statement, ParsedDetail parsed) {
    if (statement instanceof ParenthesedSelect) {
      ParenthesedSelect select = (ParenthesedSelect) statement;
      parseSelectStatement(select.getPlainSelect(), parsed);
    } else if (statement instanceof ParenthesedInsert) {
      ParenthesedInsert insert = (ParenthesedInsert) statement;
      parseInsertStatement(insert, parsed);
    } else if (statement instanceof ParenthesedDelete) {
      ParenthesedDelete delete = (ParenthesedDelete) statement;
      parseDeleteStatement(delete, parsed);
    } else if (statement instanceof ParenthesedUpdate) {
      ParenthesedUpdate update = (ParenthesedUpdate) statement;
      parseUpdateStatement(update, parsed);
    }
  }

  void processOutputClause(OutputClause outputClause, ParsedDetail parsed) {
    Table table = outputClause.getOutputTable();
    parsed.extractTableDetails(table);

    parsed.extractFieldDetails(outputClause.getColumnList(), table.getName());
  }

  void parseInnerSelect(Select select, ParsedDetail parsed, String tableName) {
    if (select instanceof PlainSelect && select.getPlainSelect() != null) {
      parseSelectStatement(select.getPlainSelect(), parsed);
    } else if (select instanceof Values) {
      for (Expression expression : ((Values) select).getExpressions()) {
        processExpression(expression, parsed, tableName);
      }
    } else if (select instanceof ParenthesedSelect) {
      parseParenthesedStatements((ParenthesedSelect) select, parsed);
    }
  }
}