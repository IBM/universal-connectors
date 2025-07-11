package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;

import java.util.Map;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public abstract class CustomParser extends ANSISqlParser {
  static final String[] CUSTOM_COMMANDS = {
    SHOW,
    USE,
    RESET,
    REFRESH,
    CREATE_SCHEMA,
    REVOKE,
    DESCRIBE,
    INSERT_OVERWRITE,
    BEGIN_TRANSACTION,
    OPTIMIZE,
    VACUUM,
    RESTORE,
    SHOW
  };

  CustomParserFactory factory;

  CustomParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
    factory = new CustomParserFactory();
  }

  abstract void parse() throws InvalidStatementException;

  static boolean unsupportedJSql(String sqlStatement) {
    for (String custom : CUSTOM_COMMANDS)
      if (sqlStatement.toUpperCase().startsWith(custom)) return true;

    return PrivilegeParser.isPrivilege(sqlStatement);
  }
}
