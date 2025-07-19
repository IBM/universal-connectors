package com.ibm.guardium.databricks.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;

import java.util.Map;

public class ANSISqlParser {
  Data data;
  String sqlStatement;
  Map<String, String> aliasMap;

  ANSISqlParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    this.data = data;
    this.sqlStatement = sqlStatement;
    this.aliasMap = aliasMap;
  }

  String getRealName(String alias) {
    if (aliasMap.containsKey(alias)) return aliasMap.get(alias);
    return alias;
  }
}