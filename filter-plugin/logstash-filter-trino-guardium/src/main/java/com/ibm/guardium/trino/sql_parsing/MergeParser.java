package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.merge.MergeInsert;
import net.sf.jsqlparser.statement.merge.MergeUpdate;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.Map;

import static com.ibm.guardium.trino.sql_parsing.Vocab.MERGE;

public class MergeParser extends JSqlParser {

  MergeParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement statement) {
    ParsedDetail parsed = new ParsedDetail();
    Merge merge = (Merge) statement;
    Table table = merge.getTable();
    parsed.extractTableDetails(table);

    if (merge.getMergeInsert() != null) {
      parseMergeInsert(merge.getMergeInsert(), parsed, table.getName());
    }

    if (merge.getMergeUpdate() != null) {
      parseMergeUpdate(merge.getMergeUpdate(), parsed, table.getName());
    }

    if (merge.getFromItem() != null) {
      processFromItem(merge.getFromItem(), parsed);
    }

    if (merge.getOnCondition() != null) {
      processExpression(merge.getOnCondition(), parsed, table.getName());
    }

    if (merge.getWithItemsList() != null) {
      for (WithItem withItem : merge.getWithItemsList()) {
        ProcessWithItem(withItem, parsed);
      }
    }

    if (merge.getOutputClause() != null) processOutputClause(merge.getOutputClause(), parsed);

    extractData(data, parsed, sqlStatement, MERGE);
  }

  void parseMergeInsert(MergeInsert mergeInsert, ParsedDetail parsed, String tableName) {
    if (mergeInsert.getColumns() != null) {
      for (Expression expression : mergeInsert.getColumns())
        processExpression(expression, parsed, tableName);
    }
    if (mergeInsert.getValues() != null) {
      for (Expression expression : mergeInsert.getValues())
        processExpression(expression, parsed, tableName);
    }
    if (mergeInsert.getAndPredicate() != null)
      processExpression(mergeInsert.getAndPredicate(), parsed, tableName);
    if (mergeInsert.getWhereCondition() != null)
      processExpression(mergeInsert.getWhereCondition(), parsed, tableName);
  }

  void parseMergeUpdate(MergeUpdate mergeUpdate, ParsedDetail parsed, String tableName) {
    if (mergeUpdate.getUpdateSets() != null) {
      for (UpdateSet updateSet : mergeUpdate.getUpdateSets()) {
        if (updateSet.getColumns() != null) {
          for (Expression expression : updateSet.getColumns())
            processExpression(expression, parsed, tableName);
        }
        if (updateSet.getValues() != null) {
          for (Expression expression : updateSet.getValues())
            processExpression(expression, parsed, tableName);
        }
      }
    }
    if (mergeUpdate.getAndPredicate() != null)
      processExpression(mergeUpdate.getAndPredicate(), parsed, tableName);
    if (mergeUpdate.getWhereCondition() != null)
      processExpression(mergeUpdate.getWhereCondition(), parsed, tableName);
    if (mergeUpdate.getDeleteWhereCondition() != null)
      processExpression(mergeUpdate.getDeleteWhereCondition(), parsed, tableName);
  }
}
