package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;

import java.util.Map;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class AlterSequenceParser extends JSqlParser {
  AlterSequenceParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement statement) {
    AlterSequence alter = (AlterSequence) statement;
    ParsedDetail.extractSimpleData(
        data, sqlStatement, alter.getSequence().getName(), SEQUENCE, ALTER, this.aliasMap);
  }
}
