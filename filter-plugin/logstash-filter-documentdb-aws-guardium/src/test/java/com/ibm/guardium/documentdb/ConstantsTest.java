package com.ibm.guardium.documentdb;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Constants class.
 * Verifies that all constants are properly defined and accessible.
 */
public class ConstantsTest {

    @Test
    public void testProtocolConstants() {
        assertEquals("DocumentDB", Constants.DATA_PROTOCOL);
        assertEquals("DocumentDB", Constants.SERVER_TYPE);
    }

    @Test
    public void testDefaultValues() {
        assertEquals("", Constants.UNKNOWN_STRING);
        assertEquals("N.A.", Constants.NOT_AVAILABLE);
        assertEquals("0.0.0.0", Constants.DEFAULT_IP);
        assertEquals("0000:0000:0000:0000:0000:FFFF:0000:0000", Constants.DEFAULT_IPV6);
        assertEquals("[json-object]", Constants.COMPOUND_OBJECT);
        assertEquals("(NONE)", Constants.DOCUMENT_INTERNAL_API_IP);
    }

    @Test
    public void testExceptionTypes() {
        assertEquals("SQL_ERROR", Constants.EXCEPTION_TYPE_AUTHORIZATION);
        assertEquals("LOGIN_FAILED", Constants.EXCEPTION_TYPE_AUTHENTICATION);
        assertEquals("UC_PARSER_ERROR", Constants.UC_PARSER_ERROR);
        assertEquals("UC_AUDIT_ERROR", Constants.UC_AUDIT_ERROR);
    }

    @Test
    public void testLogstashTags() {
        assertEquals("_documentdbguardium_json_parse_error", Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
        assertEquals("_documentdbguardium_json_depth_error", Constants.LOGSTASH_TAG_JSON_DEPTH_ERROR);
        assertEquals("_documentdbguardium_skip", Constants.LOGSTASH_TAG_SKIP);
    }

    @Test
    public void testDocumentDBSignals() {
        assertEquals("\"atype\"", Constants.DOCUMENTDB_AUDIT_SIGNAL);
        assertEquals("command", Constants.DOCUMENTDB_PROFILER_SIGNAL);
    }

    @Test
    public void testOperationKeys() {
        assertEquals("aggregate", Constants.AGGR_KEY);
        assertEquals("count", Constants.COUNT_KEY);
        assertEquals("remove", Constants.DELETE_KEY);
        assertEquals("insert", Constants.INSERT_KEY);
        assertEquals("update", Constants.UPDATE_KEY);
        assertEquals("distinct", Constants.DISTINCT_KEY);
        assertEquals("find", Constants.FIND_KEY);
        assertEquals("findAndModify", Constants.FINDANDMODIFY_KEY);
    }

    @Test
    public void testProfilerKeysSet() {
        assertNotNull(Constants.PROFILER_KEYS);
        assertEquals(8, Constants.PROFILER_KEYS.size());
        assertTrue(Constants.PROFILER_KEYS.contains("aggregate"));
        assertTrue(Constants.PROFILER_KEYS.contains("count"));
        assertTrue(Constants.PROFILER_KEYS.contains("remove"));
        assertTrue(Constants.PROFILER_KEYS.contains("insert"));
        assertTrue(Constants.PROFILER_KEYS.contains("update"));
        assertTrue(Constants.PROFILER_KEYS.contains("distinct"));
        assertTrue(Constants.PROFILER_KEYS.contains("find"));
        assertTrue(Constants.PROFILER_KEYS.contains("findAndModify"));
    }

    @Test
    public void testLocalIPList() {
        assertNotNull(Constants.LOCAL_IP_LIST);
        assertEquals(2, Constants.LOCAL_IP_LIST.size());
        assertTrue(Constants.LOCAL_IP_LIST.contains("127.0.0.1"));
        assertTrue(Constants.LOCAL_IP_LIST.contains("0:0:0:0:0:0:0:1"));
    }

    @Test
    public void testRedactionIgnoreStrings() {
        assertNotNull(Constants.REDACTION_IGNORE_STRINGS);
        assertEquals(6, Constants.REDACTION_IGNORE_STRINGS.size());
        assertTrue(Constants.REDACTION_IGNORE_STRINGS.contains("from"));
        assertTrue(Constants.REDACTION_IGNORE_STRINGS.contains("localField"));
        assertTrue(Constants.REDACTION_IGNORE_STRINGS.contains("foreignField"));
        assertTrue(Constants.REDACTION_IGNORE_STRINGS.contains("as"));
        assertTrue(Constants.REDACTION_IGNORE_STRINGS.contains("connectFromField"));
        assertTrue(Constants.REDACTION_IGNORE_STRINGS.contains("connectToField"));
    }

    @Test
    public void testFieldNames() {
        assertEquals("atype", Constants.FIELD_ATYPE);
        assertEquals("param", Constants.FIELD_PARAM);
        assertEquals("ns", Constants.FIELD_NS);
        assertEquals("user", Constants.FIELD_USER);
        assertEquals("error", Constants.FIELD_ERROR);
        assertEquals("message", Constants.FIELD_MESSAGE);
        assertEquals("command", Constants.FIELD_COMMAND);
        assertEquals("ts", Constants.FIELD_TS);
        assertEquals("client", Constants.FIELD_CLIENT);
        assertEquals("remote_ip", Constants.FIELD_REMOTE_IP);
        assertEquals("appName", Constants.FIELD_APP_NAME);
        assertEquals("op", Constants.FIELD_OP);
    }

    @Test
    public void testEventFieldNames() {
        assertEquals("message", Constants.EVENT_FIELD_MESSAGE);
        assertEquals("server_ip", Constants.EVENT_FIELD_SERVER_IP);
        assertEquals("serverHostnamePrefix", Constants.EVENT_FIELD_SERVER_HOSTNAME_PREFIX);
    }

    @Test
    public void testAuthenticationTypes() {
        assertEquals("authenticate", Constants.AUTH_TYPE_AUTHENTICATE);
        assertEquals("createRole", Constants.AUTH_TYPE_CREATE_ROLE);
        assertEquals("dropRole", Constants.AUTH_TYPE_DROP_ROLE);
    }

    @Test
    public void testErrorCodes() {
        assertEquals("0", Constants.ERROR_CODE_SUCCESS);
        assertEquals("13", Constants.ERROR_CODE_UNAUTHORIZED);
        assertEquals("18", Constants.ERROR_CODE_AUTH_FAILED);
    }

    @Test
    public void testServerConfiguration() {
        assertEquals(".aws.com", Constants.SERVER_HOSTNAME_SUFFIX);
        assertEquals("documentdb.amazonaws.com", Constants.DEFAULT_SERVER_HOSTNAME);
    }

    @Test
    public void testLimitsAndThresholds() {
        assertEquals(10000, Constants.MAX_SQL_STRING_LENGTH);
        assertEquals(256, Constants.STRING_BUILDER_INITIAL_CAPACITY);
    }

    @Test
    public void testErrorMessages() {
        assertNotNull(Constants.ERROR_JSON_VALIDATION_FAILED);
        assertNotNull(Constants.ERROR_INVALID_AUTHENTICATE_LOG);
        assertNotNull(Constants.ERROR_JSON_NESTING_TOO_DEEP);
        assertNotNull(Constants.ERROR_INSUFFICIENT_MEMORY);
        assertNotNull(Constants.ERROR_PARSING_AUDIT_EVENT);
        assertNotNull(Constants.ERROR_PARSING_PROFILER_EVENT);
        assertNotNull(Constants.ERROR_MISSING_DB_NAME);
        assertNotNull(Constants.ERROR_FAILED_TO_SERIALIZE_EVENT);
        
        assertTrue(Constants.ERROR_JSON_VALIDATION_FAILED.contains("JSON"));
        assertTrue(Constants.ERROR_INVALID_AUTHENTICATE_LOG.contains("authenticate"));
        assertTrue(Constants.ERROR_JSON_NESTING_TOO_DEEP.contains("nesting"));
        assertTrue(Constants.ERROR_INSUFFICIENT_MEMORY.contains("memory"));
        assertTrue(Constants.ERROR_PARSING_AUDIT_EVENT.contains("audit"));
        assertTrue(Constants.ERROR_PARSING_PROFILER_EVENT.contains("profiler"));
        assertTrue(Constants.ERROR_MISSING_DB_NAME.contains("DB name"));
    }

    @Test
    public void testConstantsClassCannotBeInstantiated() {
        // Verify that the Constants class cannot be instantiated
        // This is a design pattern test to ensure utility class is properly designed
        try {
            java.lang.reflect.Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
            fail("Expected AssertionError to be thrown");
        } catch (Exception e) {
            // Expected - constructor should throw AssertionError
            assertTrue(e.getCause() instanceof AssertionError);
        }
    }

    @Test
    public void testSetsAreImmutable() {
        // Verify that the sets cannot be modified
        try {
            Constants.PROFILER_KEYS.add("newKey");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected - sets should be immutable
        }

        try {
            Constants.LOCAL_IP_LIST.add("192.168.1.1");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected - sets should be immutable
        }

        try {
            Constants.REDACTION_IGNORE_STRINGS.add("newField");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected - sets should be immutable
        }
    }
}

// Made with Bob
