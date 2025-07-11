package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ibm.guardium.trino.sql_parsing.Vocab.ALIAS;
import static com.ibm.guardium.trino.sql_parsing.Vocab.DB_OBJECT;

/**
 * This class is used to parse ANSI SQL Statements used in Trino Hive DB/Databricks. We used JSql
 * Parser to parse them. However, Some of the statement do not exist in JSql Parser like REVOKE or
 * for some the format is different. In those cases there is a custom code written for parsing them.
 */
public class SqlParser {
  private static Logger logger = LogManager.getLogger(SqlParser.class);
  private static final Pattern PATTERN =
      Pattern.compile(
          "("
              + "("
              + "(?:`[^`]+`|\"[^\"]+\"|[a-zA-Z_][a-zA-Z0-9_]*)"
              + "(?:\\s*\\.\\s*"
              + "(?:`[^`]+`|\"[^\"]+\"|[a-zA-Z_][a-zA-Z0-9_]*)"
              + ")+"
              + ")"
              + "|"
              + "(?:`[^`]+`|\"[^\"]+\")"
              + ")");
  private static final Pattern TABLE_PATTERN =
      Pattern.compile("(?i)(?<=\\bFROM\\b|\\bINTO\\b|\\bTABLE\\b)\\s+([a-zA-Z_][a-zA-Z0-9_]*)");

  JSqlParserFactory sqlParserFactory;
  CustomParserFactory customParserFactory;
  private static final Set<String> JSQL_RESERVED =
      new HashSet<>(
          Arrays.asList(
              "SAMPLE", "TABLE", "VALUES", "GROUP", "ORDER" // Add more as needed
              ));

  public SqlParser() {
    this.sqlParserFactory = new JSqlParserFactory();
    this.customParserFactory = new CustomParserFactory();
  }

  // Assisted by watsonx Code Assistant
  /**
   * Parses a given SQL statement and returns a Data object containing parsed information.
   *
   * <p>This method takes an SQL string as input and processes it to extract relevant information.
   * It first trims and normalizes the input SQL statement, then refines it using a custom
   * refinement process that may introduce aliases for tables and columns.
   *
   * <p>The method handles both standard SQL statements and custom statements that JSQLParser cannot
   * parse. For unsupported statements, it delegates parsing to a custom parser. For supported
   * statements, it uses JSQLParser to create an abstract syntax tree (AST) and then processes the
   * AST using a parser factory that generates appropriate parsers based on the statement type.
   *
   * <p>The parsed information is stored in a Data object, which includes the original SQL command,
   * any introduced aliases, and the parsed result. If an error occurs during parsing, the method
   * logs the error and returns the Data object with the original SQL statement.
   *
   * @param sqlStatement the SQL string to be parsed
   * @return a Data object containing the parsed information
   */
  public Data parseStatement(String sqlStatement) {
    Data data = new Data();
    data.setOriginalSqlCommand(sqlStatement);

    if (sqlStatement == null || sqlStatement.isEmpty() || sqlStatement.trim().isEmpty()) {
      logger.error("The sql statement cannot be null or empty.");
      fixData(data, sqlStatement);
      return data;
    }

    sqlStatement = sqlStatement.trim();
    sqlStatement = sqlStatement.replaceAll(" +", " ");

    Map<String, String> aliasMap = new LinkedHashMap<>();
    sqlStatement = refineSql(sqlStatement, aliasMap);

    try {
      // There are statements that JSqlParser cannot parse. We parse them separately.
      if (CustomParser.unsupportedJSql(sqlStatement)) {
        customParserFactory.getCustomParser(data, sqlStatement, aliasMap).parse();
      } else {
        sqlStatement = AlterParser.removeWithCheckOption(sqlStatement);
        Statement statement = CCJSqlParserUtil.parse(sqlStatement);
        this.sqlParserFactory.getParser(statement, data, sqlStatement, aliasMap).parse(statement);
      }
    } catch (JSQLParserException e) {
      // In some rare cases if the table name is one of the keywords this could happen. To improve
      // the performance, we prefer not to add this case to be checked all the time. Only when an
      // exception happens we check this
      sqlStatement = fixEdgeCases(sqlStatement, aliasMap);
      try {
        Statement statement = CCJSqlParserUtil.parse(sqlStatement);
        this.sqlParserFactory.getParser(statement, data, sqlStatement, aliasMap).parse(statement);
      } catch (Exception ex) {
        fixData(data, sqlStatement);
        logger.error(
            "An error occurred during parsing the sql statement: ",
            new InvalidStatementException(sqlStatement));
      }
    } catch (Exception e) {
      fixData(data, sqlStatement);
      logger.error(
          "An error occurred during parsing the sql statement: ",
          new InvalidStatementException(sqlStatement));
    }

    return data;
  }

  // Assisted by watsonx Code Assistant

  /**
   * Refines the given SQL statement by introducing aliases for tables and columns, ensuring that
   * each alias is unique within the statement. The reason is that we use JSQL parser to parse sql
   * statements and it doesnt support non-standard namings
   *
   * <p>This static method takes an SQL string and a map of aliases as input. It uses a regular
   * expression pattern to identify table and column references in the SQL statement. For each
   * reference, it checks if an alias already exists in the provided alias map. If not, it generates
   * a unique alias and adds it to both the alias map and a reverse map. The method then replaces
   * the original references with their corresponding aliases in the SQL statement, returning the
   * refined SQL string.
   *
   * @param sql the SQL string to be refined
   * @param aliasMap a map to store the original-to-alias mappings
   * @return the refined SQL string with unique aliases for tables and columns
   */
  static String refineSql(String sql, Map<String, String> aliasMap) {
    Matcher matcher = PATTERN.matcher(sql);
    StringBuffer sb = new StringBuffer();
    int counter = 1;

    Map<String, String> reverseMap = new LinkedHashMap<>();
    while (matcher.find()) {
      String original = matcher.group();
      String alias;
      if (!reverseMap.containsKey(original)) {
        alias = ALIAS + counter++;
        aliasMap.put(alias, original);
        reverseMap.put(original, alias);
      } else {
        alias = reverseMap.get(original);
      }
      matcher.appendReplacement(sb, Matcher.quoteReplacement(alias));
    }
    matcher.appendTail(sb);

    return sb.toString();
  }

  private String fixEdgeCases(String sql, Map<String, String> aliasMap) {
    Matcher matcher = TABLE_PATTERN.matcher(sql);
    StringBuffer sb = new StringBuffer();
    int counter = aliasMap.size() + 1;

    while (matcher.find()) {
      String tableName = matcher.group(1);
      if (JSQL_RESERVED.contains(tableName.toUpperCase())) {
        String alias = ALIAS + counter++;
        aliasMap.put(alias, tableName);
        matcher.appendReplacement(sb, matcher.group(0).replace(tableName, alias));
      } else {
        matcher.appendReplacement(sb, matcher.group(0));
      }
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  private void fixData(Data data, String sql) {
    String action = "N.A.";
    if (sql != null && !sql.isEmpty()) action = sql.split("\\s+")[0];
    SentenceObject object = new SentenceObject(DB_OBJECT);
    object.setType(DB_OBJECT);
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
