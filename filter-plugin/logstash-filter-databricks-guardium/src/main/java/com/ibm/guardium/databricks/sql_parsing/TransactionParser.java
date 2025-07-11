package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.databricks.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ibm.guardium.databricks.sql_parsing.Vocab.*;

public class TransactionParser extends CustomParser {
  private static Logger logger = LogManager.getLogger(TransactionParser.class);

  JSqlParserFactory sqlParserFactory;
  CustomParserFactory customParserFactory;

  TransactionParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
    this.sqlParserFactory = new JSqlParserFactory();
    this.customParserFactory = new CustomParserFactory();
  }

  @Override
  void parse() throws InvalidStatementException {
    sqlStatement = sqlStatement.replaceAll("\n", " ").trim();
    if (!isValid())
      throw new InvalidStatementException(
              "The SQL statement [" + sqlStatement + "] is invalid and cannot be parsed ");

    // add Transaction related part
    ArrayList<Sentence> sentences = new ArrayList<>();
    Sentence sentence = new Sentence(TRANSACTION);
    sentences.add(sentence);
    List<String> transactions = parseTransactionBlocks(sqlStatement);

    for (String tx : transactions) {
      sentence = parseStatement(tx);
      if (sentence != null) sentences.add(sentence);
    }

    Construct construct = new Construct();
    construct.setFullSql(data.getOriginalSqlCommand());
    construct.setSentences(sentences);
    data.setConstruct(construct);
  }

  Sentence parseStatement(String statement) {
    try {
      Data statementData = new Data();
      // There are statements that JSqlParser cannot parse. We parse them separately.
      if (CustomParser.unsupportedJSql(statement)) {
        customParserFactory.getCustomParser(statementData, statement, aliasMap).parse();
      } else {
        statement = AlterParser.removeWithCheckOption(statement);
        Statement jsqlStatement = CCJSqlParserUtil.parse(statement);
        this.sqlParserFactory
                .getParser(jsqlStatement, statementData, statement, aliasMap)
                .parse(jsqlStatement);
      }

      if (statementData.getConstruct() != null
              && statementData.getConstruct().getSentences() != null
              && !statementData.getConstruct().getSentences().isEmpty())
        return statementData.getConstruct().getSentences().get(0);
    } catch (Exception e) {
      logger.error("An error occurred during parsing the sql statement: ", e);
    }
    return null;
  }

  boolean isValid() {
    return sqlStatement.startsWith(BEGIN_TRANSACTION)
            && (sqlStatement.endsWith(COMMIT + ";") || sqlStatement.endsWith(ROLLBACK + ";"));
  }

  static boolean isTransaction(String sql) {
    return sql.startsWith(BEGIN_TRANSACTION);
  }

  List<String> parseTransactionBlocks(String sqlScript) {
    List<String> currentStatements = new ArrayList<>();

    String[] rawStatements = sqlScript.split("(?<=;)");
    for (String raw : rawStatements) {
      String stmt = raw.trim();
      if (stmt.isEmpty()) continue;
      String noSemicolon = stmt.replaceAll(";$", "").trim();
      if (!noSemicolon.startsWith(BEGIN_TRANSACTION)
              && !noSemicolon.startsWith(COMMIT)
              && !noSemicolon.startsWith(ROLLBACK)) {
        currentStatements.add(stmt);
      }
    }
    return new ArrayList<>(currentStatements);
  }
}