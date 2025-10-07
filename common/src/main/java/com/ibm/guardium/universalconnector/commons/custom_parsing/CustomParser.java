package com.ibm.guardium.universalconnector.commons.custom_parsing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.guardium.universalconnector.commons.custom_parsing.excepton.InvalidConfigurationException;
import com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.IParser;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.*;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.ibm.guardium.universalconnector.commons.custom_parsing.PropertyConstant.*;
import static com.ibm.guardium.universalconnector.commons.structures.Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL;
import static com.ibm.guardium.universalconnector.commons.structures.Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL;
import static com.ibm.guardium.universalconnector.commons.structures.SessionLocator.PORT_DEFAULT;

public abstract class CustomParser {
    private static final Logger logger = LogManager.getLogger(CustomParser.class);
    private static final InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();
    protected Map<String, String> properties;
    private final ObjectMapper mapper;
    IParser parser;
    protected boolean parseUsingSniffer = false;
    protected boolean hasSqlParsing = false;
    protected boolean parseUsingCustomParser = false;

    protected CustomParser(ParserFactory.ParserType parserType) {
        parser = new ParserFactory().getParser(parserType);
        mapper = new ObjectMapper();

        // We only need to read the properties file once and then we validate it.
        try {
            properties = getProperties();
        } catch (InvalidConfigurationException e) {
            logger.error("The config file is corrupted so the parser cannot parse the logs: ", e);
        }
    }

    public Record parseRecord(String payload) {
        if (!isValid(payload))
            return null;

        return extractRecord(payload);
    }

    protected boolean isValid(String payload) {
        return properties != null && parser.isPayloadValid(payload);
    }

    protected Record extractRecord(String payload) {
        Record record = new Record();

        record.setSessionId(getSessionId(payload));
        record.setDbName(getDbName(payload));
        record.setAppUserName(getAppUserName(payload));
        String sqlString = getSqlString(payload);
        record.setException(getException(payload, sqlString));
        record.setAccessor(getAccessor(payload));
        record.setSessionLocator(getSessionLocator(payload));
        record.setTime(getTimestamp(payload));

        if (!record.isException())
            record.setData(getData(payload, sqlString));

        return record;
    }

    protected String getValue(String payload, String fieldName) {
        String value = properties.get(fieldName);
        if (value == null)
            return null;

        // If it is static literal we dont need custom parser
        if (value.startsWith("{") && value.endsWith("}"))
            return value.substring(1, value.indexOf("}"));

        return parse(payload, value);
    }

    protected String parse(String payload, String key) {
        return parser.parse(payload, key);
    }

    // method to handle exception type and description
    protected ExceptionRecord getException(String payload, String sqlString) {
        String exceptionTypeId = getExceptionTypeId(payload); // Get the error message
        if (exceptionTypeId.isEmpty())
            return null;

        ExceptionRecord exceptionRecord = new ExceptionRecord();
        exceptionRecord.setExceptionTypeId(exceptionTypeId);
        exceptionRecord.setDescription(getExceptionDescription(payload));
        exceptionRecord.setSqlString(sqlString);

        return exceptionRecord;
    }

    protected String getExceptionDescription(String payload) {
        return DEFAULT_STRING;
    }

    protected String getExceptionTypeId(String payload) {
        String value = getValue(payload, EXCEPTION_TYPE_ID);
        return value != null ? value : DEFAULT_STRING;
    }

    protected String getAppUserName(String payload) {
        String value = getValue(payload, APP_USER_NAME);
        return value != null ? value : DATABASE_NOT_AVAILABLE;
    }

    protected String getClientIpv6(String payload) {
        return getValue(payload, CLIENT_IPV6);
    }

    protected String getClientIp(String payload) {
        return getValue(payload, CLIENT_IP);
    }

    protected Data getDataForException(String sqlString) {
        Data data = new Data();
        data.setOriginalSqlCommand(sqlString);
        return data;
    }

    protected Data getData(String payload, String sqlString) {
        Data data = new Data();
        if (!hasSqlParsing || parseUsingSniffer) {
            data.setOriginalSqlCommand(sqlString);
            return data;
        }

        String object = getNotNullString(getValue(payload, OBJECT));
        String verb = getNotNullString(getValue(payload, VERB));
        Construct construct = new Construct();
        Sentence sentence = new Sentence(verb);
        SentenceObject sentenceObject = new SentenceObject(object);
        sentence.getObjects().add(sentenceObject);
        construct.sentences.add(sentence);
        construct.setFullSql(sqlString);

        data.setConstruct(construct);
        String originalSqlCommand = getOriginalSqlCommand(payload);
        data.setOriginalSqlCommand(
                !Objects.equals(originalSqlCommand, DEFAULT_STRING) ? originalSqlCommand : sqlString);
        return data;
    }

    protected String getNotNullString(String value) {
        return value != null ? value : DEFAULT_STRING;
    }

    protected Integer getMinDst(String payload) {
        Integer value = convertToInt(MIN_DST, getValue(payload, MIN_DST));
        return value != null ? value : ZERO;
    }

    protected Integer getMinOffsetFromGMT(String payload) {
        Integer value = convertToInt(MIN_OFFSET_FROM_GMT, getValue(payload, MIN_OFFSET_FROM_GMT));
        return value != null ? value : ZERO;
    }

    protected String getOriginalSqlCommand(String payload) {
        String value = getValue(payload, ORIGINAL_SQL_COMMAND);
        return value != null ? value : getSqlString(payload);
    }

    protected String getServerIp(String payload) {
        String value = getValue(payload, SERVER_IP);
        return value != null ? value : DEFAULT_IP;
    }

    protected String getServerIpv6(String payload) {
        String value = getValue(payload, SERVER_IPV6);
        return value != null ? value : DEFAULT_IPV6;
    }

    // method to handle the SQL command that caused the exception
    protected String getSqlString(String payload) {
        String value = getValue(payload, SQL_STRING);
        return value != null ? value : DEFAULT_STRING; // Set the SQL command that caused the exception
    }

    // In this setTimestamp method now parses the timestamp from the payload and
    // sets the timestamp, minOffsetFromGMT, and minDst fields in the Time object of
    // the Record. If the timestamp is not available, it sets default values.
    protected Time getTimestamp(String payload) {
        String value = getValue(payload, TIMESTAMP);
        if (value != null) {
            try {
                return parseTimestamp(value);
            } catch (Exception e) {
                logger.error("Time {} is invalid.", value, e);
            }
        }
        return new Time(0L, 0, 0);
    }

    protected SessionLocator getSessionLocator(String payload) {
        SessionLocator sessionLocator = new SessionLocator();

        // set default values
        sessionLocator.setIpv6(false);
        sessionLocator.setClientIpv6(DEFAULT_IPV6);
        sessionLocator.setServerIpv6(DEFAULT_IPV6);
        sessionLocator.setClientIp(DEFAULT_IP);
        sessionLocator.setServerIp(DEFAULT_IP);

        String clientIp = getClientIp(payload);
        String clientIpv6 = getClientIpv6(payload);
        if (clientIpv6 != null && clientIpv6 != DEFAULT_IPV6 && inetAddressValidator.isValidInet6Address(clientIpv6)) {
            // If client IP is IPv6, set both client and server to IPv6
            sessionLocator.setIpv6(true);
            sessionLocator.setClientIpv6(clientIpv6);
            sessionLocator.setServerIpv6(getServerIpv6(payload)); // Set server IP to default IPv6

        } else if (clientIp != null && inetAddressValidator.isValidInet4Address(clientIp)) {
            // If client IP is IPv4, set both client and server IP to IPv4
            sessionLocator.setClientIp(clientIp);
            // Cloud Databases: Set server IP to 0.0.0.0
            sessionLocator.setServerIp(getServerIp(payload));
        }

        // Set port numbers
        sessionLocator.setClientPort(getClientPort(payload));
        sessionLocator.setServerPort(getServerPort(payload));

        return sessionLocator;
    }

    public static Time parseTimestamp(String timestamp) {
        ZonedDateTime date = ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME);
        long millis = date.toInstant().toEpochMilli();
        int minOffset = date.getOffset().getTotalSeconds() / 60;
        int minDst = date.getZone().getRules().isDaylightSavings(date.toInstant()) ? 60 : 0;
        return new Time(millis, minOffset, minDst);
    }

    // Updated method to check accessor.dataType and populate original_sql_command
    // or construct
    protected Accessor getAccessor(String payload) {
        Accessor accessor = new Accessor();

        accessor.setServiceName(getServiceName(payload));
        accessor.setDbUser(getDbUser(payload));
        accessor.setDbProtocolVersion(getDbProtocolVersion(payload));
        accessor.setDbProtocol(getDbProtocol(payload));
        accessor.setServerType(getServerType(payload));
        accessor.setServerOs(getServerOs(payload));
        accessor.setServerDescription(getServerDescription(payload));
        accessor.setServerHostName(getServerHostName(payload));
        accessor.setClientHostName(getClientHostName(payload));
        accessor.setClient_mac(getClientMac(payload));
        accessor.setClientOs(getClientOs(payload));
        accessor.setCommProtocol(getCommProtocol(payload));
        accessor.setOsUser(getOsUser(payload));
        accessor.setSourceProgram(getSourceProgram(payload));
        accessor.setLanguage(getLanguage(payload));
        accessor.setDataType(getDataType(payload));

        return accessor;
    }

    protected String getServiceName(String payload) {
        String value = getValue(payload, SERVICE_NAME);
        return value != null ? value : DEFAULT_STRING;
    }

    protected String getDbUser(String payload) {
        String value = getValue(payload, DB_USER);
        return value != null ? value : DATABASE_NOT_AVAILABLE;
    }

    protected String getDbName(String payload) {
        String value = getValue(payload, DB_NAME);
        return value != null ? value : DATABASE_NOT_AVAILABLE;
    }

    protected String getDbProtocol(String payload) {
        String value = getValue(payload, DB_PROTOCOL);
        return value != null ? value : DEFAULT_STRING;
    }

    protected String getServerOs(String payload) {
        String value = getValue(payload, SERVER_OS);
        return value != null ? value : DEFAULT_STRING;
    }

    protected String getClientOs(String payload) {
        String value = getValue(payload, CLIENT_OS);
        return value != null ? value : DEFAULT_STRING;
    }

    protected String getClientHostName(String payload) {
        String value = getValue(payload, CLIENT_HOSTNAME);
        return value != null ? value : DEFAULT_STRING;
    }

    protected String getCommProtocol(String payload) {
        String value = getValue(payload, COMM_PROTOCOL);
        return value != null ? value : DEFAULT_STRING;
    }

    protected String getDbProtocolVersion(String payload) {
        String value = getValue(payload, DB_PROTOCOL_VERSION);
        return value != null ? value : DEFAULT_STRING;
    }

    protected String getOsUser(String payload) {
        String value = getValue(payload, OS_USER);
        return value != null ? value : DATABASE_NOT_AVAILABLE;
    }

    protected String getSourceProgram(String payload) {
        String value = getValue(payload, SOURCE_PROGRAM);
        return value != null ? value : DEFAULT_STRING;
    }

    protected String getClientMac(String payload) {
        String value = getValue(payload, CLIENT_MAC);
        return value != null ? value : DEFAULT_STRING;
    }

    protected String getServerDescription(String payload) {
        String value = getValue(payload, SERVER_DESCRIPTION);
        return value != null ? value : DEFAULT_STRING;
    }

    protected String getServerHostName(String payload) {
        String value = getValue(payload, SERVER_HOSTNAME);
        return value != null ? value : DEFAULT_STRING;
    }

    protected String getServerType(String payload) {
        // this has been validated before
        if (parseUsingSniffer)
            return SqlParser.getServerType(properties.get(SNIFFER_PARSER));

        String value = getValue(payload, SERVER_TYPE);
        return value != null ? value : DEFAULT_STRING;
    }

    protected String getLanguage(String payload) {
        // this has been validated before
        if (parseUsingSniffer)
            return properties.get(SNIFFER_PARSER);

        return Accessor.LANGUAGE_FREE_TEXT_STRING;
    }

    protected String getDataType(String payload) {
        if (parseUsingSniffer)
            return properties.get(DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);

        return DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL;
    }

    protected String getSessionId(String payload) {
        String value = getValue(payload, SESSION_ID);
        return value != null ? value : DEFAULT_STRING;
    }

    protected Integer getClientPort(String payload) {
        Integer value = convertToInt(CLIENT_PORT, getValue(payload, CLIENT_PORT));
        return value != null ? value : PORT_DEFAULT;
    }

    protected Integer getServerPort(String payload) {
        Integer value = convertToInt(SERVER_PORT, getValue(payload, SERVER_PORT));
        return value != null ? value : PORT_DEFAULT;
    }

    public abstract String getConfigFileContent();

    public Map<String, String> getProperties() throws InvalidConfigurationException {
        Map<String, String> props;
        try {
            String content = getConfigFileContent();
            props = mapper.readValue(content, new TypeReference<HashMap<String, String>>() {
            });
            if (!arePropertiesValid(props))
                throw new InvalidConfigurationException("The configuration file data is invalid.");
        } catch (IOException e) {
            throw new InvalidConfigurationException("Error reading or parsing the configuration file.");
        }
        return props;
    }

    protected Integer convertToInt(String fieldName, String value) {
        if (value == null || Objects.equals(value, DEFAULT_STRING)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            if (logger.isDebugEnabled())
                logger.debug("{} is not a valid value for {}.", value, fieldName);
        }
        return null;
    }

    protected boolean arePropertiesValid(Map<String, String> props) {
        if (props == null) {
            logger.error("The provided config file is invalid.");
            return false;
        }

        hasSqlParsing = SqlParser.hasSqlParsing(props);
        parseUsingSniffer = hasSqlParsing && SqlParser.isSnifferParsing(props);
        parseUsingCustomParser = hasSqlParsing && SqlParser.isCustomParsing(props);

        SqlParser.ValidityCase isValid = SqlParser.isValid(props, hasSqlParsing, parseUsingSniffer,
                parseUsingCustomParser);
        if (!isValid.equals(SqlParser.ValidityCase.VALID)) {
            logger.error(isValid.getDescription());
            return false;
        }

        return true;
    }

}