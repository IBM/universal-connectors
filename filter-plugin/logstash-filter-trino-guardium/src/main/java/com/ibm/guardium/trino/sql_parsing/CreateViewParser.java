package com.ibm.guardium.trino.sql_parsing;

import com.ibm.guardium.universalconnector.commons.structures.Data;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.view.CreateView;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ibm.guardium.trino.sql_parsing.Vocab.*;

public class CreateViewParser extends JSqlParser {
  Pattern pattern = Pattern.compile("(?i)\\bCREATE.*?VIEW");

  CreateViewParser(Data data, String sqlStatement, Map<String, String> aliasMap) {
    super(data, sqlStatement, aliasMap);
  }

  @Override
  void parse(Statement create) {
    ParsedDetail parsed = new ParsedDetail();
    parseCreateView((CreateView) create, parsed);

    Matcher matcher = pattern.matcher(sqlStatement);
    String action = CREATE_VIEW;
    if (matcher.find()) {
      action = matcher.group();
    }
    extractData(data, parsed, sqlStatement, action);
  }

  void parseCreateView(CreateView create, ParsedDetail parsed) {
    Table view = create.getView();
    parsed.extractTableDetails(view, VIEW);

    if (create.getColumnNames() != null)
      parsed.extractFieldDetails(create.getColumnNames(), view.getName());

    if (create.getSelect() != null) parseInnerSelect(create.getSelect(), parsed, view.getName());
  }
}
