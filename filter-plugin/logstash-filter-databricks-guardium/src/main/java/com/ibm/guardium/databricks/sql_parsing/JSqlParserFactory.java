package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.databricks.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.util.Map;

public class JSqlParserFactory {
  JSqlParser getParser(
          Statement statement, Data data, String sqlStatement, Map<String, String> aliasMap)
          throws InvalidStatementException {
    if (statement instanceof Select) return new SelectParser(data, sqlStatement, aliasMap);
    if (statement instanceof Insert) return new InsertParser(data, sqlStatement, aliasMap);
    if (statement instanceof Update) return new UpdateParser(data, sqlStatement, aliasMap);
    if (statement instanceof Delete) return new DeleteParser(data, sqlStatement, aliasMap);
    if (statement instanceof Drop) return new DropParser(data, sqlStatement, aliasMap);
    if (statement instanceof Merge) return new MergeParser(data, sqlStatement, aliasMap);

    if (statement instanceof CreateTable)
      return new CreateTableParser(data, sqlStatement, aliasMap);
    if (statement instanceof CreateView) return new CreateViewParser(data, sqlStatement, aliasMap);
    if (statement instanceof CreateIndex)
      return new CreateIndexParser(data, sqlStatement, aliasMap);
    if (statement instanceof CreateSequence)
      return new CreateSequenceParser(data, sqlStatement, aliasMap);

    if (statement instanceof Grant) return new GrantParser(data, sqlStatement, aliasMap);

    if (statement instanceof Alter) return new AlterParser(data, sqlStatement, aliasMap);
    if (statement instanceof AlterView) return new AlterViewParser(data, sqlStatement, aliasMap);
    if (statement instanceof AlterSequence)
      return new AlterSequenceParser(data, sqlStatement, aliasMap);

    if (statement instanceof Comment) return new CommentParser(data, sqlStatement, aliasMap);
    if (statement instanceof ExplainStatement)
      return new ExplainParser(data, sqlStatement, aliasMap);
    if (statement instanceof RenameTableStatement)
      return new RenameTableParser(data, sqlStatement, aliasMap);
    if (statement instanceof SetStatement) return new SetParser(data, sqlStatement, aliasMap);

    throw new InvalidStatementException(statement.toString());
  }
}