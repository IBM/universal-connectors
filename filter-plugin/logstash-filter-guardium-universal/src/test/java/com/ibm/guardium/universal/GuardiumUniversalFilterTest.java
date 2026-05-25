/*
 * Copyright 2024 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */
package com.ibm.guardium.universal;

import co.elastic.logstash.api.Event;
import com.ibm.guardium.universal.parser.IGuardiumParser;
import com.ibm.guardium.universal.parser.ParserRegistry;
import com.ibm.guardium.universal.datasources.mysql.MySqlParser;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

/**
 * Framework-level tests for {@link GuardiumUniversalFilter} and {@link ParserRegistry}.
 *
 * <p>Datasource-specific parsing is tested in the parser's own test class
 * (e.g., {@code MySqlParserTest}), keeping these tests focused on the
 * generic plugin lifecycle.
 */
public class GuardiumUniversalFilterTest {

    // ---- ParserRegistry tests ------------------------------------------------

    @Test
    public void testRegistry_mySqlIsRegistered() {
        IGuardiumParser parser = ParserRegistry.getParser("mysql");
        Assert.assertNotNull(parser);
        Assert.assertTrue(parser instanceof MySqlParser);
    }

    @Test
    public void testRegistry_caseInsensitive() {
        IGuardiumParser p1 = ParserRegistry.getParser("MySQL");
        IGuardiumParser p2 = ParserRegistry.getParser("mysql");
        IGuardiumParser p3 = ParserRegistry.getParser("MYSQL");
        Assert.assertSame(p1.getClass(), p2.getClass());
        Assert.assertSame(p2.getClass(), p3.getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegistry_unknownDatasourceThrows() {
        ParserRegistry.getParser("nonexistent-db-12345");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegistry_emptyDatasourceThrows() {
        ParserRegistry.getParser("");
    }

    @Test
    public void testRegistry_customParserCanBeRegistered() {
        IGuardiumParser stub = event -> null;
        ParserRegistry.register("test-stub", stub);
        Assert.assertSame(stub, ParserRegistry.getParser("test-stub"));
    }

    // ---- MySqlParser unit tests ----------------------------------------------

    private static final String MYSQL_QUERY_EVENT =
            "Mar 10 12:00:00 db-host mysqld: mysql_audit_log: "
            + "{\"timestamp\":\"2024-03-10 12:00:00\",\"id\":1,"
            + "\"class\":\"general\",\"event\":\"query\","
            + "\"connection_id\":42,"
            + "\"account\":{\"user\":\"admin\",\"host\":\"localhost\"},"
            + "\"login\":{\"user\":\"admin\",\"os\":\"\",\"ip\":\"10.0.0.1\",\"proxy\":\"\"},"
            + "\"general_data\":{\"command\":\"Query\",\"sql_command\":\"select\","
            + "\"query\":\"SELECT 1\",\"status\":0}}";

    private static final String MYSQL_LOGIN_FAILED_EVENT =
            "mysql_audit_log: "
            + "{\"timestamp\":\"2024-03-10 12:00:00\",\"id\":2,"
            + "\"class\":\"connection\",\"event\":\"connect\","
            + "\"connection_id\":43,"
            + "\"account\":{\"user\":\"baduser\",\"host\":\"localhost\"},"
            + "\"login\":{\"user\":\"baduser\",\"os\":\"\",\"ip\":\"10.0.0.2\",\"proxy\":\"\"},"
            + "\"connection_data\":{\"connection_type\":\"ssl\",\"status\":1,"
            + "\"db\":\"testdb\",\"connection_attributes\":{}}}";

    @Test
    public void testMySqlParser_successfulQuery() throws Exception {
        MySqlParser parser = new MySqlParser();
        Event event = new org.logstash.Event();
        event.setField("message", MYSQL_QUERY_EVENT);
        event.setField("server_ip", "192.168.1.10");
        event.setField("server_hostname", "db-host");

        Record record = parser.parseRecord(event);

        Assert.assertNotNull("Parser should return a Record for a valid query event", record);
        Assert.assertNotNull("Data should be set for a successful query", record.getData());
        Assert.assertEquals("SELECT 1", record.getData().getOriginalSqlCommand());
        Assert.assertEquals("42", record.getSessionId());
        Assert.assertEquals("admin", record.getAccessor().getDbUser());
    }

    @Test
    public void testMySqlParser_loginFailed() throws Exception {
        MySqlParser parser = new MySqlParser();
        Event event = new org.logstash.Event();
        event.setField("message", MYSQL_LOGIN_FAILED_EVENT);
        event.setField("server_ip", "192.168.1.10");

        Record record = parser.parseRecord(event);

        Assert.assertNotNull(record);
        Assert.assertNotNull("Exception should be set for login failure", record.getException());
        Assert.assertEquals("LOGIN_FAILED", record.getException().getExceptionTypeId());
    }

    @Test
    public void testMySqlParser_irrelevantMessage_returnsNull() throws Exception {
        MySqlParser parser = new MySqlParser();
        Event event = new org.logstash.Event();
        event.setField("message", "some unrelated syslog message without mysql prefix");

        Record record = parser.parseRecord(event);

        Assert.assertNull("Parser should return null for non-MySQL messages", record);
    }
}
