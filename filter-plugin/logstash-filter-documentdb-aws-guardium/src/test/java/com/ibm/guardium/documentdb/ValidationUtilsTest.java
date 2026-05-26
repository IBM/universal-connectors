package com.ibm.guardium.documentdb;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationUtils class.
 */
public class ValidationUtilsTest {

    @Test
    public void testIsProperlyClosedJson_ValidObject() {
        assertTrue(ValidationUtils.isProperlyClosedJson("{\"key\":\"value\"}"));
        assertTrue(ValidationUtils.isProperlyClosedJson("{ \"key\": \"value\" }"));
        assertTrue(ValidationUtils.isProperlyClosedJson("  {\"key\":\"value\"}  "));
    }

    @Test
    public void testIsProperlyClosedJson_ValidArray() {
        assertTrue(ValidationUtils.isProperlyClosedJson("[\"value1\",\"value2\"]"));
        assertTrue(ValidationUtils.isProperlyClosedJson("[ \"value1\", \"value2\" ]"));
        assertTrue(ValidationUtils.isProperlyClosedJson("  [\"value1\"]  "));
    }

    @Test
    public void testIsProperlyClosedJson_Invalid() {
        assertFalse(ValidationUtils.isProperlyClosedJson("{\"key\":\"value\""));
        assertFalse(ValidationUtils.isProperlyClosedJson("\"key\":\"value\"}"));
        assertFalse(ValidationUtils.isProperlyClosedJson("[\"value\""));
        assertFalse(ValidationUtils.isProperlyClosedJson("\"value\"]"));
        assertFalse(ValidationUtils.isProperlyClosedJson("{]"));
        assertFalse(ValidationUtils.isProperlyClosedJson("[}"));
    }

    @Test
    public void testIsProperlyClosedJson_NullOrEmpty() {
        assertFalse(ValidationUtils.isProperlyClosedJson(null));
        assertFalse(ValidationUtils.isProperlyClosedJson(""));
        assertFalse(ValidationUtils.isProperlyClosedJson("   "));
    }

    @Test
    public void testIsValidIpAddress_ValidIPv4() {
        assertTrue(ValidationUtils.isValidIpAddress("192.168.1.1"));
        assertTrue(ValidationUtils.isValidIpAddress("127.0.0.1"));
        assertTrue(ValidationUtils.isValidIpAddress("0.0.0.0"));
        assertTrue(ValidationUtils.isValidIpAddress("255.255.255.255"));
    }

    @Test
    public void testIsValidIpAddress_ValidIPv6() {
        assertTrue(ValidationUtils.isValidIpAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertTrue(ValidationUtils.isValidIpAddress("::1"));
        assertTrue(ValidationUtils.isValidIpAddress("fe80::1"));
    }

    @Test
    public void testIsValidIpAddress_Invalid() {
        assertFalse(ValidationUtils.isValidIpAddress("256.1.1.1"));
        assertFalse(ValidationUtils.isValidIpAddress("192.168.1"));
        assertFalse(ValidationUtils.isValidIpAddress("not-an-ip"));
        assertFalse(ValidationUtils.isValidIpAddress(""));
        assertFalse(ValidationUtils.isValidIpAddress(null));
    }

    @Test
    public void testIsDocumentInternalCommandIp_LocalIPs() {
        assertTrue(ValidationUtils.isDocumentInternalCommandIp("127.0.0.1"));
        assertTrue(ValidationUtils.isDocumentInternalCommandIp("0:0:0:0:0:0:0:1"));
    }

    @Test
    public void testIsDocumentInternalCommandIp_InternalMarker() {
        assertTrue(ValidationUtils.isDocumentInternalCommandIp("(NONE)"));
        assertTrue(ValidationUtils.isDocumentInternalCommandIp("  (NONE)  "));
        assertTrue(ValidationUtils.isDocumentInternalCommandIp("(none)"));
    }

    @Test
    public void testIsDocumentInternalCommandIp_NotInternal() {
        assertFalse(ValidationUtils.isDocumentInternalCommandIp("192.168.1.1"));
        assertFalse(ValidationUtils.isDocumentInternalCommandIp("10.0.0.1"));
        assertFalse(ValidationUtils.isDocumentInternalCommandIp(""));
        assertFalse(ValidationUtils.isDocumentInternalCommandIp(null));
    }

    @Test
    public void testIsNullOrEmpty() {
        assertTrue(ValidationUtils.isNullOrEmpty(null));
        assertTrue(ValidationUtils.isNullOrEmpty(""));
        assertFalse(ValidationUtils.isNullOrEmpty(" "));
        assertFalse(ValidationUtils.isNullOrEmpty("text"));
    }

    @Test
    public void testGetValueOrDefault_WithValue() {
        assertEquals("value", ValidationUtils.getValueOrDefault("value", "default"));
        assertEquals("text", ValidationUtils.getValueOrDefault("text", "default"));
    }

    @Test
    public void testGetValueOrDefault_WithoutValue() {
        assertEquals("default", ValidationUtils.getValueOrDefault(null, "default"));
        assertEquals("default", ValidationUtils.getValueOrDefault("", "default"));
    }

    @Test
    public void testGetValueOrDefault_BothNull() {
        assertNull(ValidationUtils.getValueOrDefault(null, null));
    }
}

// Made with Bob
