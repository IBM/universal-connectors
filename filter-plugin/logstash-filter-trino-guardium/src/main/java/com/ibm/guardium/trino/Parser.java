/*
Copyright IBM Corp. 2021, 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.trino;

import com.ibm.guardium.universalconnector.commons.custom_parsing.CustomParser;
import com.ibm.guardium.universalconnector.commons.custom_parsing.ParserFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ibm.guardium.universalconnector.commons.structures.Data;

import static com.ibm.guardium.trino.Constants.*;
import static com.ibm.guardium.universalconnector.commons.custom_parsing.PropertyConstant.DEFAULT_STRING;
import static com.ibm.guardium.universalconnector.commons.custom_parsing.PropertyConstant.*;
import static com.ibm.guardium.universalconnector.commons.structures.Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL;

/**
 * Parser Class will perform operation on parsing events and messages from the
 * Trino audit logs into a Guardium record instance Guardium records include
 * the accessor, the sessionLocator, data, and exceptions. If there are no
 * errors, the data contains details about the query "construct"
 *
 * @className Parser
 */
public class Parser extends CustomParser {
    private static Logger logger = LogManager.getLogger(Parser.class);

    public Parser(ParserFactory.ParserType parserType) {
        super(parserType);
    }

    @Override
    protected String getExceptionTypeId(String payload) {
        String value = getValue(payload, EXCEPTION_TYPE_ID);
        if (value == null || value.equalsIgnoreCase(NULL))
            return DEFAULT_STRING;
        return SQL_ERROR;
    }

    @Override
    protected String getExceptionDescription(String payload) {
        String value = getValue(payload, ERROR_MSG);
        return value != null ? value : DEFAULT_STRING;
    }

    @Override
    protected Data getData(String payload, String sqlString) {
        Data data = new Data();
        data.setOriginalSqlCommand(sqlString);
        return data;
    }

    @Override
    protected String getLanguage(String payload) {
        return MY_SQL_SNIF;
    }

    @Override
    protected String getDataType(String payload) {
        return DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL;
    }

    @Override
    public String getConfigFileContent() {
        return "{" +
                " \"db_name\": \"DatabaseName\",\n" +
                " \"db_user\": \"User\",\n" +
                " \"app_user_name\": \"User\",\n" +
                " \"client_ip\": \"ClientIP\",\n" +
                " \"client_port\": \"ClientPort\",\n" +
                " \"exception_type_id\": \"Error\",\n" +
                " \"exception_desc\": \"Error\",\n" +
                " \"db_protocol\": \"{Trino}\",\n" +
                " \"object\": \"\",\n" +
                " \"timstamp\": \"Time\",\n" +
                " \"server_ip\": \"ServerIP\",\n" +
                " \"server_port\": \"ServerPort\",\n" +
                " \"server_type\": \"$vendor$\",\n" +
                " \"session_id\": \"\",\n" +
                " \"event_id\": \"$eventid$\",\n" +
                " \"status\": \"$eventid$\",\n" +
                " \"sniffer_parser\": \"FREE_TEXT\",\n" +
                " \"sql_string\": \"SQLCommand\"" +
                "}";
    }


}
