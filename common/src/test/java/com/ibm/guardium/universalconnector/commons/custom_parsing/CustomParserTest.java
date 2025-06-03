package com.ibm.guardium.universalconnector.commons.custom_parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.guardium.universalconnector.commons.custom_parsing.excepton.InvalidConfigurationException;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.ibm.guardium.universalconnector.commons.custom_parsing.PropertyConstant.*;
import static com.ibm.guardium.universalconnector.commons.structures.Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL;
import static org.junit.Assert.*;

public class CustomParserTest {
    private static CustomParser customParser;
    private static Map<String, String> configValues;

    @BeforeClass
    public static void setUp() throws IOException {
        // Initialize the custom parser
        customParser = new CustomParser(ParserFactory.ParserType.regex) {

            @Override
            public String getConfigFileContent() {
                // Return the path to your configuration file
                try {
                    return new String(Files.readAllBytes(Paths.get("src/test/resources/config.json")));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Record parseRecord(String payload) {
                return super.parseRecord(payload);
            }
        };

        // Load the JSON configuration
        ObjectMapper objectMapper = new ObjectMapper();
        configValues = objectMapper
                .readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/config.json"))), Map.class);
        // Initialize properties
        customParser.properties = configValues;
    }

    @Test
    public void testParseRecordValidPayload() {
        String payload = String.format(
                "[Session ID: %s] [Client Port: %s] [Server Port: %s] [DB User: %s] [Server Type: %s] [DB Protocol: %s] [Exception Type ID: %s]",
                configValues.get(PropertyConstant.SESSION_ID),
                configValues.get(PropertyConstant.CLIENT_PORT),
                configValues.get(PropertyConstant.SERVER_PORT),
                configValues.get(PropertyConstant.DB_USER),
                configValues.get(PropertyConstant.SERVER_TYPE),
                configValues.get(PropertyConstant.DB_PROTOCOL),
                configValues.get(PropertyConstant.EXCEPTION_TYPE_ID));

        Record record = customParser.parseRecord(payload);

        assertNotNull(record);
        assertEquals(configValues.get(PropertyConstant.SESSION_ID), record.getSessionId());
        assertEquals(Integer.parseInt(configValues.get(PropertyConstant.CLIENT_PORT)),
                record.getSessionLocator().getClientPort());
        assertEquals(Integer.parseInt(configValues.get(PropertyConstant.SERVER_PORT)),
                record.getSessionLocator().getServerPort());
        assertEquals(configValues.get(PropertyConstant.DB_USER), record.getAccessor().getDbUser());
        assertEquals(configValues.get(PropertyConstant.SERVER_TYPE), record.getAccessor().getServerType());
        assertEquals(configValues.get(PropertyConstant.DB_PROTOCOL), record.getAccessor().getDbProtocol());
        assertEquals(configValues.get(PropertyConstant.EXCEPTION_TYPE_ID), record.getException().getExceptionTypeId());
    }

    @Test
    public void testGetStaticValue() {
        Map<String, String> props = new HashMap<>();
        props.put(PropertyConstant.DB_USER, "{TEST}");
        CustomParser cp = new CustomParser(ParserFactory.ParserType.regex) {
            @Override
            public String getConfigFileContent() {
                return "";
            }

            @Override
            public Map<String, String> getProperties() {
                return props;
            }
        };
        cp.properties = props;
        assertEquals("TEST", cp.getValue("whatever", PropertyConstant.DB_USER));

        // Invalid regex wont cause exception
        assertNull(cp.parse("whatever", "invalid Regex"));
    }

    @Test
    public void testGetProperties() throws InvalidConfigurationException {
        Map<String, String> properties = customParser.getProperties();
        assertNotNull(properties);
        assertEquals(configValues.get(PropertyConstant.SESSION_ID), properties.get(PropertyConstant.SESSION_ID));
    }

    @Test
    public void testGetException() {
        String payload = String.format("[Exception Type ID: %s]", configValues.get(PropertyConstant.EXCEPTION_TYPE_ID));
        String sqlString = "SELECT * FROM employees";
        ExceptionRecord exceptionRecord = customParser.getException(payload, sqlString);

        assertNotNull(exceptionRecord);
        assertEquals(configValues.get(PropertyConstant.EXCEPTION_TYPE_ID), exceptionRecord.getExceptionTypeId());
        assertEquals(sqlString, exceptionRecord.getSqlString());
    }

    @Test
    public void testConvertToInt() {
        assertEquals(Integer.valueOf(53422), customParser.convertToInt(PropertyConstant.CLIENT_PORT,
                (String) configValues.get(PropertyConstant.CLIENT_PORT)));
        assertNull(customParser.convertToInt(PropertyConstant.CLIENT_PORT, "invalid"));

        assertNull(customParser.convertToInt(PropertyConstant.CLIENT_PORT, ""));
        assertNull(customParser.convertToInt(PropertyConstant.CLIENT_PORT, null));
    }

    @Test
    public void testGetValue() {
        String value = customParser.getValue(
                String.format("[Session ID: %s]", configValues.get(PropertyConstant.SESSION_ID)),
                PropertyConstant.SESSION_ID);
        assertEquals(configValues.get(PropertyConstant.SESSION_ID), value);
    }

    @Test
    public void testGetTimestamp() {
        String payload = "[Timestamp: 2024-08-23T15:22:35.876Z]";
        Time time = customParser.getTimestamp(payload);
        assertNotNull(time);
    }

    @Test
    public void testGetDataForException() {
        String sqlString = "SELECT * FROM employees";
        Data data = customParser.getDataForException(sqlString);
        assertNotNull(data);
        assertEquals(sqlString, data.getOriginalSqlCommand());
    }

    @Test
    public void testGetAccessor() {
        String payload = String.format("[DB User: %s]", configValues.get(PropertyConstant.DB_USER));
        Accessor accessor = customParser.getAccessor(payload);
        assertNotNull(accessor);
        assertEquals(configValues.get(PropertyConstant.DB_USER), accessor.getDbUser());
    }

    @Test
    public void testParseRecordWithMissingFields() {
        String payload = "[Session ID: ] [DB Name: ] [DB User: ]";
        Record record = customParser.parseRecord(payload);

        assertNotNull(record);
        assertEquals(PropertyConstant.DEFAULT_STRING, record.getSessionId());
        assertEquals(PropertyConstant.DATABASE_NOT_AVAILABLE, record.getDbName());
        assertEquals(PropertyConstant.DATABASE_NOT_AVAILABLE, record.getAppUserName());
    }

    @Test
    public void testParseTimestamp() {
        String timestamp = "2023-04-14T12:00:00Z";
        Time time = CustomParser.parseTimestamp(timestamp);

        assertNotNull(time);
        assertEquals(1681473600000L, time.getTimstamp());
        assertEquals(0, time.getMinOffsetFromGMT());
        assertEquals(0, time.getMinDst());
    }

    @Test
    public void testParseRecordWithSnifferPayload() throws IOException {
        // Load the JSON configuration from sniffer.json
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> snifferConfigValues = objectMapper.readValue(
                new File("src/test/resources/sniffer.json"), Map.class);

        // Check if the parsing type is "SNIFFER"
        String parsingType = snifferConfigValues.get("parsing_type");
        assertEquals("Expected parsing type to be SNIFFER", "SNIFFER", parsingType);

        // Check additional fields
        assertNull("Expected sql_string to be null", snifferConfigValues.get("sql_string"));
        assertNull("Expected original_sql_command to be null", snifferConfigValues.get("original_sql_command"));

        // Check if the sniffer_parser is set correctly
        String snifferParserType = snifferConfigValues.get("sniffer_parser");
        assertNotNull("Expected sniffer_parser to be defined", snifferParserType);
        assertEquals("Expected sniffer_parser to be ORACLE", "ORACLE", snifferParserType);

        // Create a payload using the values from sniffer.json
        String payload = String.format(
                "[Session ID: %s] [Client Port: %s] [Server Port: %s] [DB User: %s] [Server Type: %s] [DB Protocol: %s] [Exception Type ID: %s]",
                snifferConfigValues.get("session_id"),
                snifferConfigValues.get("client_port"),
                snifferConfigValues.get("server_port"),
                snifferConfigValues.get("db_user"),
                snifferConfigValues.get("server_type"),
                snifferConfigValues.get("db_protocol"),
                snifferConfigValues.get("exception_type_id"));

        // Parse the record using the custom parser
        customParser.properties = snifferConfigValues;
        Record record = customParser.parseRecord(payload);

        // Assertions to verify the parsed values
        assertNotNull(record);
        assertEquals(snifferConfigValues.get("session_id"), record.getSessionId());
        assertEquals(Integer.parseInt(snifferConfigValues.get("client_port")),
                record.getSessionLocator().getClientPort());
        assertEquals(Integer.parseInt(snifferConfigValues.get("server_port")),
                record.getSessionLocator().getServerPort());
        assertEquals(snifferConfigValues.get("db_user"), record.getAccessor().getDbUser());
        assertEquals(snifferConfigValues.get("server_type"), record.getAccessor().getServerType());
        assertEquals(snifferConfigValues.get("db_protocol"), record.getAccessor().getDbProtocol());
        assertEquals(snifferConfigValues.get("exception_type_id"), record.getException().getExceptionTypeId());

    }

    @Test
    public void testParseRecordWithRegexPayload() throws IOException {
        // Load the JSON configuration from config2.json
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> regexConfigValues = objectMapper.readValue(
                new File("src/test/resources/config2.json"), Map.class);

        // Check if the parsing type is "REGEX"
        String parsingType = regexConfigValues.get("parsing_type");
        assertEquals("Expected parsing type to be CUSTOM_PARSER", "CUSTOM_PARSER", parsingType);

        // Check additional fields
        assertNotNull("Expected sql_parsing_active to be defined", regexConfigValues.get("sql_parsing_active"));
        assertNotNull("Expected object to be defined", regexConfigValues.get("object"));
        assertNotNull("Expected verb to be defined", regexConfigValues.get("verb"));

        // Create a payload using the values from config2.json
        String payload = String.format(
                "[Session ID: %s] [Client Port: %s] [Server Port: %s] [DB User: %s] [Server Type: %s] [DB Protocol: %s] [Exception Type ID: %s] [Object: %s] [Verb: %s]",
                regexConfigValues.get("session_id"),
                regexConfigValues.get("client_port"),
                regexConfigValues.get("server_port"),
                regexConfigValues.get("db_user"),
                regexConfigValues.get("server_type"),
                regexConfigValues.get("db_protocol"),
                regexConfigValues.get("exception_type_id"),
                regexConfigValues.get("object"), // Add object to payload
                regexConfigValues.get("verb"));

        // Parse the record using the custom parser
        customParser.properties = regexConfigValues;
        Record record = customParser.parseRecord(payload);

        // Assertions to verify the parsed values
        assertNotNull(record);
        assertEquals(regexConfigValues.get("session_id"), record.getSessionId());
        assertEquals(Integer.parseInt(regexConfigValues.get("client_port")),
                record.getSessionLocator().getClientPort());
        assertEquals(Integer.parseInt(regexConfigValues.get("server_port")),
                record.getSessionLocator().getServerPort());
        assertEquals(regexConfigValues.get("db_user"), record.getAccessor().getDbUser());
        assertEquals(regexConfigValues.get("server_type"), record.getAccessor().getServerType());
        assertEquals(regexConfigValues.get("db_protocol"), record.getAccessor().getDbProtocol());
        assertEquals(regexConfigValues.get("exception_type_id"), record.getException().getExceptionTypeId());
    }

    @Test
    public void testIsValid() {

        // Test case 1: Valid properties
        Map<String, String> validProperties = new HashMap<>();
        validProperties.put(SQL_PARSING_ACTIVE, "true");
        validProperties.put(PARSING_TYPE, "CUSTOM_PARSER");
        validProperties.put(OBJECT, "table");
        validProperties.put(VERB, "SELECT");

        customParser.properties = validProperties;
        assertTrue("Expected isValid to return true for valid properties",
                customParser.arePropertiesValid(customParser.properties));

        // Test case 2: Invalid parsing type
        validProperties.put(PARSING_TYPE, "INVALID_TYPE");
        assertFalse("Expected isValid to return false for invalid parsing type",
                customParser.arePropertiesValid(customParser.properties));

        // Test case 3: Missing object
        validProperties.put(PARSING_TYPE, "CUSTOM_PARSER");
        validProperties.remove(OBJECT);
        assertFalse("Expected isValid to return false for missing object",
                customParser.arePropertiesValid(customParser.properties));

        // Test case 4: Missing verb
        validProperties.put(OBJECT, "table");
        validProperties.remove(VERB);
        assertFalse("Expected isValid to return false for missing verb",
                customParser.arePropertiesValid(customParser.properties));

        // Test case 5: SQL parsing inactive
        validProperties.put(SQL_PARSING_ACTIVE, "false");
        assertTrue("Expected isValid to return true when SQL parsing is inactive",
                customParser.arePropertiesValid(customParser.properties));

        // Test case 6: Properties is null
        customParser.properties = null;
        assertFalse("Expected isValid to return false when properties are null",
                customParser.arePropertiesValid(customParser.properties));
    }

    @Test
    public void testIsValidAndRelatedMethods() {
        // Test hasSqlParsing when SQL parsing is not active
        Map<String, String> properties = new HashMap<>();
        properties.put(SQL_PARSING_ACTIVE, "false");
        assertFalse(SqlParser.hasSqlParsing(properties));
        assertEquals(SqlParser.ValidityCase.VALID, SqlParser.isValid(properties, false, false, false));

        // Test hasSqlParsing when SQL parsing is active
        properties.put(SQL_PARSING_ACTIVE, "true");
        assertTrue(SqlParser.hasSqlParsing(properties));

        // Test isValid with invalid parsing type
        properties.put(PARSING_TYPE, "INVALID_TYPE");
        assertEquals(SqlParser.ValidityCase.INVALID_PARSING_TYPE, SqlParser.isValid(properties, true, false, false));

        // Test isValid with valid REGEX parsing type but null object
        properties.put(PARSING_TYPE, "CUSTOM_PARSER");
        properties.put(VERB, "SELECT");
        assertEquals(SqlParser.ValidityCase.NULL_OBJECT, SqlParser.isValid(properties, true, false, true));

        // Test isValid with valid REGEX parsing type but null verb
        properties.put(OBJECT, "table");
        properties.remove(VERB);
        assertEquals(SqlParser.ValidityCase.NULL_VERB, SqlParser.isValid(properties, true, false, true));

        // Test isValid with valid REGEX parsing type and both object and verb present
        properties.put(VERB, "SELECT");
        assertEquals(SqlParser.ValidityCase.VALID, SqlParser.isValid(properties, true, false, true));

        // Test isValid with valid SNIFFER parsing type but null sniffer parser
        properties.put(PARSING_TYPE, "SNIFFER");
        properties.remove(SNIFFER_PARSER);
        assertEquals(SqlParser.ValidityCase.INVALID_SNIFFER_PARSER, SqlParser.isValid(properties, true, true, false));

        // Test isValid with valid SNIFFER parsing type and valid sniffer parser
        properties.put(SNIFFER_PARSER, "MSSQL");
        assertEquals(SqlParser.ValidityCase.VALID, SqlParser.isValid(properties, true, true, false));
    }

    @Test
    public void testGetServerTypeAndIsSnifferParsing() {
        // Test getServerType with valid and invalid languages
        assertEquals("MS_SQL_SERVER", SqlParser.getServerType("MSSQL"));
        assertNull(SqlParser.getServerType("INVALID_LANGUAGE"));

        // Test isSnifferParsing with valid and invalid parsing types
        Map<String, String> map = new HashMap<>();
        map.put("parsing_type", "SNIFFER");
        assertTrue(SqlParser.isSnifferParsing(map));

        map.put("parsing_type", "CUSTOM_PARSER");
        assertFalse(SqlParser.isSnifferParsing(map));

        map.put("parsing_type", "JAVA");
        assertFalse(SqlParser.isSnifferParsing(map));

        map.put("parsing_type", "something else");
        assertFalse(SqlParser.isSnifferParsing(map));

        // Test getDescription for each ValidityCase
        assertEquals("The SQL Parsing is valid", SqlParser.ValidityCase.VALID.getDescription());
        assertEquals("Parsing type can only be CUSTOM_PARSER (for Regex and Json) or SNIFFER",
                SqlParser.ValidityCase.INVALID_PARSING_TYPE.getDescription());
        assertEquals("Sniffer Parser is invalid.", SqlParser.ValidityCase.INVALID_SNIFFER_PARSER.getDescription());
        assertEquals("The object field cannot be null.", SqlParser.ValidityCase.NULL_OBJECT.getDescription());
        assertEquals("The verb field cannot be null.", SqlParser.ValidityCase.NULL_VERB.getDescription());
    }

    @Test
    public void testGetServerTypeUsingSniffer() {
        Map<String, String> properties = new HashMap<>();
        properties.put(SNIFFER_PARSER, "MYSQL");
        customParser.properties = properties;
        customParser.parseUsingSniffer = true;

        String serverType = customParser.getServerType("some payload");
        assertEquals("Expected server type to be MYSQL", "MYSQL", serverType);
    }

    @Test
    public void testGetDataTypeUsingSniffer() {
        Map<String, String> properties = new HashMap<>();
        properties.put(DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL, "TEXT");
        customParser.properties = properties;
        customParser.parseUsingSniffer = true;

        String dataType = customParser.getDataType("some payload");
        assertEquals("Expected data type to be TEXT", "TEXT", dataType);
    }

    @Test
    public void testGetMinDstWithNullPayload() {
        String payload = "{}"; // Empty payload
        Integer result = customParser.getMinDst(payload);
        assertEquals(0, result.intValue());
    }

    @Test
    public void testGetMinDstWithInvalidInteger() {
        String payload = String.format("{\"%s\": \"invalid\"}", MIN_DST);
        Integer result = customParser.getMinDst(payload);
        assertEquals(0, result.intValue());
    }

    @Test
    public void testGetMinOffsetFromGMTWithNullPayload() {
        String payload = "{}"; // Empty payload
        Integer result = customParser.getMinOffsetFromGMT(payload);
        assertEquals(0, result.intValue());
    }

    @Test
    public void testGetMinOffsetFromGMTWithInvalidInteger() {
        String payload = String.format("{\"%s\": \"invalid\"}", MIN_OFFSET_FROM_GMT);
        Integer result = customParser.getMinOffsetFromGMT(payload);
        assertEquals(0, result.intValue()); // Cast to int to avoid ambiguity
    }

    @Test
    public void testGetOriginalSqlCommandWithNullPayload() {
        String payload = "{}"; // Empty payload
        String result = customParser.getOriginalSqlCommand(payload);
        assertEquals(customParser.getSqlString(payload), result);
    }

    @Test
    public void testGetServerIpv6WithNullPayload() {
        String payload = "{}"; // Empty payload
        String result = customParser.getServerIpv6(payload);
        assertEquals(DEFAULT_IPV6, result);
    }

    @Test
    public void testGetDataWithMissingObjectAndVerb() {
        // Set hasSqlParsing to true
        customParser.hasSqlParsing = true;
        customParser.parseUsingSniffer = false; // Ensure sniffer parsing is disabled

        String payload = "{}"; // No OBJECT or VERB
        String originalSQLString = "SELECT * FROM collectionA";

        Data result = customParser.getData(payload, originalSQLString);

        assertNotNull(result);
        assertNotNull(result.getConstruct());
        assertEquals(originalSQLString, result.getConstruct().getFullSql());
    }

    @Test
    public void testJson() {
        String payload = "{\"hostIdentifier\":\"7d058e67620c\",\"calendarTime\":\"Sun Apr 14 16:17:14 2019 UTC\","
                + "\"unixTime\":\"1555258634\","
                + "\"severity\":\"0\",\"filename\":\"aws_kinesis.cpp\",\"line\":\"142\",\"message\":\"Successfully sent 6 of 6 logs to Kinesis\","
                + "\"version\":\"2.7.0\",\"decorations\":{\"customer_id\":\"1\",\"host_uuid\":\"33C9A321-088E-43FE-83D7-4C6A4D134937\",\"hostname\""
                + ":\"7d058e67620c\"},\"log_type\":\"status\"}";

        CustomParser cp = new CustomParser(ParserFactory.ParserType.json) {
            @Override
            public String getConfigFileContent() {
                // Return the path to your configuration file
                try {
                    return new String(Files.readAllBytes(Paths.get("src/test/resources/jsonConfig.json")));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Record record = cp.parseRecord(payload);

        assertEquals("7d058e67620c", record.getSessionId());
        assertEquals("PostgreSQL", record.getAccessor().getServerType());
    }
}