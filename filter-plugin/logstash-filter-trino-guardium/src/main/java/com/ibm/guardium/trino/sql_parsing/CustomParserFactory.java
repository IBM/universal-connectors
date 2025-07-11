package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.trino.sql_parsing.exception.InvalidStatementException;
import com.ibm.guardium.universalconnector.commons.structures.Data;

import java.util.Map;

public class CustomParserFactory {
  CustomParser getCustomParser(Data data, String sqlStatement, Map<String, String> aliasMap)
      throws InvalidStatementException {
    if (PrivilegeParser.isPrivilege(sqlStatement))
      return new PrivilegeParser(data, sqlStatement, aliasMap);
    if (ShowParser.isShowCommand(sqlStatement)) return new ShowParser(data, sqlStatement, aliasMap);
    if (ResetParser.isReset(sqlStatement)) return new ResetParser(data, sqlStatement, aliasMap);
    if (RefreshParser.isRefresh(sqlStatement))
      return new RefreshParser(data, sqlStatement, aliasMap);
    if (UseParser.isUse(sqlStatement)) return new UseParser(data, sqlStatement, aliasMap);
    if (DescribeParser.isDescribeCommand(sqlStatement))
      return new DescribeParser(data, sqlStatement, aliasMap);
    if (CreateSchemaParser.isCreateSchema(sqlStatement))
      return new CreateSchemaParser(data, sqlStatement, aliasMap);
    if (InsertOverwriteParser.isInsertOverwrite(sqlStatement))
      return new InsertOverwriteParser(data, sqlStatement, aliasMap);
    if (TransactionParser.isTransaction(sqlStatement))
      return new TransactionParser(data, sqlStatement, aliasMap);
    if (OptimizeParser.isOptimize(sqlStatement))
      return new OptimizeParser(data, sqlStatement, aliasMap);
    if (VacuumParser.isVacuum(sqlStatement)) return new VacuumParser(data, sqlStatement, aliasMap);
    if (RestoreParser.isRestore(sqlStatement))
      return new RestoreParser(data, sqlStatement, aliasMap);
    else throw new InvalidStatementException(sqlStatement);
  }
}
