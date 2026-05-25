package com.ibm.guardium.documentdb;

import co.elastic.logstash.api.Event;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EventUtils class.
 */
public class EventUtilsTest {

    @Test
    public void testLogEvent_ValidEvent() {
        Event event = new org.logstash.Event();
        event.setField("key1", "value1");
        event.setField("key2", "value2");
        
        String result = EventUtils.logEvent(event);
        
        assertNotNull(result);
        assertTrue(result.contains("key1"));
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("key2"));
        assertTrue(result.contains("value2"));
        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    public void testLogEvent_EmptyEvent() {
        Event event = new org.logstash.Event();
        
        String result = EventUtils.logEvent(event);
        
        assertNotNull(result);
        assertTrue(result.contains("{"));
        assertTrue(result.contains("}"));
    }

    @Test
    public void testGetStringField_ValidString() {
        Event event = new org.logstash.Event();
        event.setField("testField", "testValue");
        
        String result = EventUtils.getStringField(event, "testField");
        
        assertEquals("testValue", result);
    }

    @Test
    public void testGetStringField_NonExistentField() {
        Event event = new org.logstash.Event();
        
        String result = EventUtils.getStringField(event, "nonExistent");
        
        assertNull(result);
    }

    @Test
    public void testGetStringField_NonStringField() {
        Event event = new org.logstash.Event();
        event.setField("numberField", 123);
        
        String result = EventUtils.getStringField(event, "numberField");
        
        // Should return string representation or null depending on implementation
        assertNotNull(result);
    }

    @Test
    public void testGetValidatedEventServerIp_ValidIPv4() {
        Event event = new org.logstash.Event();
        event.setField(Constants.EVENT_FIELD_SERVER_IP, "192.168.1.1");
        
        String result = EventUtils.getValidatedEventServerIp(event);
        
        assertEquals("192.168.1.1", result);
    }

    @Test
    public void testGetValidatedEventServerIp_ValidIPv6() {
        Event event = new org.logstash.Event();
        event.setField(Constants.EVENT_FIELD_SERVER_IP, "::1");
        
        String result = EventUtils.getValidatedEventServerIp(event);
        
        assertEquals("::1", result);
    }

    @Test
    public void testGetValidatedEventServerIp_InvalidIP() {
        Event event = new org.logstash.Event();
        event.setField(Constants.EVENT_FIELD_SERVER_IP, "not-an-ip");
        
        String result = EventUtils.getValidatedEventServerIp(event);
        
        assertNull(result);
    }

    @Test
    public void testGetValidatedEventServerIp_NoField() {
        Event event = new org.logstash.Event();
        
        String result = EventUtils.getValidatedEventServerIp(event);
        
        assertNull(result);
    }

    @Test
    public void testGetMessageField_ValidMessage() {
        Event event = new org.logstash.Event();
        event.setField(Constants.EVENT_FIELD_MESSAGE, "test message");
        
        String result = EventUtils.getMessageField(event);
        
        assertEquals("test message", result);
    }

    @Test
    public void testGetMessageField_NoMessage() {
        Event event = new org.logstash.Event();
        
        String result = EventUtils.getMessageField(event);
        
        assertNull(result);
    }

    @Test
    public void testGetServerHostnamePrefix_ValidPrefix() {
        Event event = new org.logstash.Event();
        event.setField(Constants.EVENT_FIELD_SERVER_HOSTNAME_PREFIX, "test-server");
        
        String result = EventUtils.getServerHostnamePrefix(event);
        
        assertEquals("test-server", result);
    }

    @Test
    public void testGetServerHostnamePrefix_NoPrefix() {
        Event event = new org.logstash.Event();
        
        String result = EventUtils.getServerHostnamePrefix(event);
        
        assertNull(result);
    }

    @Test
    public void testGetStringField_NullFieldName() {
        Event event = new org.logstash.Event();
        event.setField("test", "value");
        
        String result = EventUtils.getStringField(event, null);
        
        assertNull(result);
    }

    @Test
    public void testLogEvent_WithSpecialCharacters() {
        Event event = new org.logstash.Event();
        event.setField("key", "value with \"quotes\" and \n newlines");
        
        String result = EventUtils.logEvent(event);
        
        assertNotNull(result);
        assertTrue(result.contains("key"));
    }

    @Test
    public void testGetStringField_EmptyString() {
        Event event = new org.logstash.Event();
        event.setField("emptyField", "");
        
        String result = EventUtils.getStringField(event, "emptyField");
        
        assertEquals("", result);
    }
}

// Made with Bob
