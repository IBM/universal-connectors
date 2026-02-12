/*
Copyright IBM Corp. 2021, 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.cockroachdb;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import org.junit.jupiter.api.Test;

import static com.ibm.guardium.cockroachdb.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private final Parser parser = new Parser();

    @Test
    void testParseRegularQuery() {
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT * FROM customers WHERE city = 'New York'\","
                + "\"Tag\":\"SELECT\","
                + "\"User\":\"guardium_qa\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"DatabaseName\":\"testdb\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\","
                + "\"ServerHostname\":\"cockroach-node1\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertEquals("guardium_qa", record.getAppUserName());
        assertEquals("testdb", record.getDbName());
        assertEquals("127.0.0.1", record.getSessionLocator().getClientIp());
        assertEquals(49696, record.getSessionLocator().getClientPort());
        assertEquals(DB_PROTOCOL, record.getAccessor().getDbProtocol());
        assertEquals("guardium_qa", record.getAccessor().getDbUser());
        assertEquals(SERVER_TYPE, record.getAccessor().getServerType());
        assertEquals("SELECT * FROM customers WHERE city = 'New York'",
                record.getData().getOriginalSqlCommand());
        assertEquals(1768332558964L, record.getTime().getTimstamp());
        assertEquals("testdb", record.getAccessor().getServiceName());
        assertEquals(record.getDbName(), record.getAccessor().getServiceName());
        assertNull(record.getException());
    }

    @Test
    void testParseSQLError() {
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"CREATE TABLE customers (id INT8 PRIMARY KEY, name STRING)\","
                + "\"Tag\":\"CREATE TABLE\","
                + "\"User\":\"guardium_qa\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"SQLSTATE\":\"42P07\","
                + "\"ErrorText\":\"relation \\\"customers\\\" already exists\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertEquals("guardium_qa", record.getAppUserName());
        assertNull(record.getData());
        assertNotNull(record.getException());
        assertEquals(EXCEPTION_TYPE_SQL_ERROR_STRING, record.getException().getExceptionTypeId());
        assertTrue(record.getException().getDescription().contains("already exists"));
        assertTrue(record.getException().getDescription().contains("[SQLSTATE: 42P07]"));
        assertEquals("CREATE TABLE customers (id INT8 PRIMARY KEY, name STRING)",
                record.getException().getSqlString());
    }

    @Test
    void testParseAuthenticationFailure() {
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"client_authentication_failed\","
                + "\"User\":\"invalid_user\","
                + "\"Reason\":\"USER_NOT_FOUND\","
                + "\"ClientIP\":\"192.168.1.100\","
                + "\"ClientPort\":\"54321\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertEquals("invalid_user", record.getAppUserName());
        assertNull(record.getData());
        assertNotNull(record.getException());
        assertEquals(EXCEPTION_TYPE_LOGIN_FAILED_STRING, record.getException().getExceptionTypeId());
        assertTrue(record.getException().getDescription().contains("invalid_user"));
        assertTrue(record.getException().getDescription().contains("USER_NOT_FOUND"));
    }

    @Test
    void testCleanSpecialCharacters() {
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT 1\","
                + "\"User\":\"‹guardium_qa›\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertEquals("guardium_qa", record.getAppUserName());
        assertFalse(record.getAppUserName().contains("‹"));
        assertFalse(record.getAppUserName().contains("›"));
        assertEquals("guardium_qa", record.getAccessor().getDbUser());
    }

    @Test
    void testSessionLocatorEmpty() {
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT 1\","
                + "\"User\":\"test_user\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        SessionLocator sessionLocator = parser.getSessionLocator(data);

        assertEquals(DEFAULT_IP, sessionLocator.getClientIp());
        assertEquals(DEFAULT_IP, sessionLocator.getServerIp());
        assertEquals(DEFAULT_PORT, sessionLocator.getClientPort());
        assertEquals(DEFAULT_PORT, sessionLocator.getServerPort());
    }

    @Test
    void testAccessorWithMissingUser() {
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT 1\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Accessor accessor = parser.getAccessor(data, NOT_AVAILABLE);

        assertEquals(NOT_AVAILABLE, accessor.getDbUser());
        assertEquals(SERVER_TYPE, accessor.getServerType());
        assertEquals(DB_PROTOCOL, accessor.getDbProtocol());
        assertEquals(LANGUAGE_COCKROACHDB, accessor.getLanguage());
    }

    @Test
    void testMultipleErrors() {
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"INSERT INTO test VALUES (1, 'test')\","
                + "\"User\":\"test_user\","
                + "\"ErrorText\":\"duplicate key value violates unique constraint\","
                + "\"SQLSTATE\":\"23505\","
                + "\"ClientIP\":\"10.0.0.5\","
                + "\"ClientPort\":\"12345\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNull(record.getData());
        assertNotNull(record.getException());
        assertEquals(EXCEPTION_TYPE_SQL_ERROR_STRING, record.getException().getExceptionTypeId());
        assertTrue(record.getException().getDescription().contains("duplicate key"));
        assertTrue(record.getException().getDescription().contains("[SQLSTATE: 23505]"));
        assertEquals("INSERT INTO test VALUES (1, 'test')", record.getException().getSqlString());
    }

    @Test
    void testSQLErrorWithoutSQLSTATE() {
        // Test error without SQLSTATE field
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT * FROM products WHERE category = 'Electronics'\","
                + "\"User\":\"test_user\","
                + "\"ErrorText\":\"column \\\"category\\\" does not exist\","
                + "\"ClientIP\":\"10.0.0.5\","
                + "\"ClientPort\":\"12345\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNull(record.getData());
        assertNotNull(record.getException());
        assertEquals(EXCEPTION_TYPE_SQL_ERROR_STRING, record.getException().getExceptionTypeId());
        assertEquals("column \"category\" does not exist", record.getException().getDescription());
        assertFalse(record.getException().getDescription().contains("SQLSTATE"));
        assertEquals("SELECT * FROM products WHERE category = 'Electronics'", record.getException().getSqlString());
    }

    @Test
    void testTimestampConversion() {
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT NOW()\","
                + "\"User\":\"test_user\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNotNull(record.getTime());
        assertEquals(1768332558964L, record.getTime().getTimstamp());
        assertEquals(0, record.getTime().getMinOffsetFromGMT());
        assertEquals(0, record.getTime().getMinDst());
    }

    @Test
    void testComplexJoinQuery() {
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT u.name, o.order_date, o.total FROM users u INNER JOIN orders o ON u.id = o.user_id WHERE o.total > 500\","
                + "\"Tag\":\"SELECT\","
                + "\"User\":\"guardium_qa\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"DatabaseName\":\"testdb\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertEquals("guardium_qa", record.getAppUserName());
        assertEquals("testdb", record.getDbName());
        assertNotNull(record.getData());
        assertTrue(record.getData().getOriginalSqlCommand().contains("INNER JOIN"));
        assertTrue(record.getData().getOriginalSqlCommand().contains("WHERE"));
        assertNull(record.getException());
    }

    @Test
    void testMultiLineQuery() {
        String multiLineQuery = "SELECT u.id, u.name, u.email, c.name as customer_name, p.product_name, o.total, o.status, o.order_date\\n"
                + "FROM users u\\n"
                + "LEFT JOIN orders o ON u.id = o.user_id\\n"
                + "LEFT JOIN customers c ON o.customer_id = c.id\\n"
                + "WHERE u.active = true AND o.status IN ('pending', 'completed')\\n"
                + "ORDER BY o.order_date DESC";

        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"" + multiLineQuery + "\","
                + "\"Tag\":\"SELECT\","
                + "\"User\":\"guardium_qa\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"DatabaseName\":\"testdb\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNotNull(record.getData());
        assertTrue(record.getData().getOriginalSqlCommand().contains("LEFT JOIN"));
        assertTrue(record.getData().getOriginalSqlCommand().contains("ORDER BY"));
        assertNull(record.getException());
    }

    @Test
    void testQueryWithSubquery() {
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT * FROM users WHERE id IN (SELECT user_id FROM orders WHERE total > 500)\","
                + "\"Tag\":\"SELECT\","
                + "\"User\":\"guardium_qa\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"DatabaseName\":\"testdb\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNotNull(record.getData());
        assertTrue(record.getData().getOriginalSqlCommand().contains("SELECT user_id FROM orders"));
        assertNull(record.getException());
    }

    @Test
    void testQueryWithCaseStatement() {
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT name, CASE WHEN salary < 50000 THEN 'Junior' WHEN salary < 100000 THEN 'Mid-level' ELSE 'Senior' END as level FROM employees\","
                + "\"Tag\":\"SELECT\","
                + "\"User\":\"guardium_qa\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"DatabaseName\":\"testdb\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNotNull(record.getData());
        assertTrue(record.getData().getOriginalSqlCommand().contains("CASE WHEN"));
        assertTrue(record.getData().getOriginalSqlCommand().contains("ELSE"));
        assertNull(record.getException());
    }

    @Test
    void testVeryLongQuery() {
        String longQuery = "SELECT u.id, u.name, u.email, u.active, u.last_login, u.created_at, "
                + "c.name as customer_name, c.email as customer_email, c.phone, "
                + "p.product_name, p.price, p.category, p.stock, "
                + "o.total, o.status, o.order_date, oi.quantity, oi.price as item_price "
                + "FROM users u "
                + "LEFT JOIN orders o ON u.id = o.user_id "
                + "LEFT JOIN customers c ON o.customer_id = c.id "
                + "LEFT JOIN order_items oi ON o.id = oi.order_id "
                + "LEFT JOIN products p ON oi.product_id = p.id "
                + "WHERE u.active = true AND o.status IN ('pending', 'completed', 'shipped') "
                + "AND p.category IN ('Electronics', 'Furniture') "
                + "ORDER BY o.order_date DESC, u.name ASC LIMIT 100";

        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"" + longQuery + "\","
                + "\"Tag\":\"SELECT\","
                + "\"User\":\"guardium_qa\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"DatabaseName\":\"testdb\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNotNull(record.getData());
        assertEquals(longQuery, record.getData().getOriginalSqlCommand());
        assertTrue(record.getData().getOriginalSqlCommand().length() > 400);
        assertNull(record.getException());
    }

    @Test
    void testQueryWithSpecialCharactersInString() {
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"INSERT INTO users (name, email) VALUES ('O''Brien', 'test@example.com')\","
                + "\"Tag\":\"INSERT\","
                + "\"User\":\"guardium_qa\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"DatabaseName\":\"testdb\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNotNull(record.getData());
        assertTrue(record.getData().getOriginalSqlCommand().contains("O''Brien"));
        assertNull(record.getException());
    }

    @Test
    void testComplexErrorWithMultiLineQuery() {
        String multiLineQuery = "SELECT u.id, u.name\\nFROM users u\\nINNER JOIN orders o\\nWHERE invalid_column = 1";
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"" + multiLineQuery + "\","
                + "\"Tag\":\"SELECT\","
                + "\"User\":\"guardium_qa\","
                + "\"ErrorText\":\"column \\\"invalid_column\\\" does not exist\","
                + "\"SQLSTATE\":\"42703\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNull(record.getData());
        assertNotNull(record.getException());
        assertEquals(EXCEPTION_TYPE_SQL_ERROR_STRING, record.getException().getExceptionTypeId());
        assertTrue(record.getException().getDescription().contains("does not exist"));
        assertTrue(record.getException().getSqlString().contains("INNER JOIN"));
    }

    @Test
    void testQueryWithNullValues() {
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT * FROM users WHERE email IS NULL OR last_login IS NOT NULL\","
                + "\"Tag\":\"SELECT\","
                + "\"User\":\"guardium_qa\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"DatabaseName\":\"testdb\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNotNull(record.getData());
        assertTrue(record.getData().getOriginalSqlCommand().contains("IS NULL"));
        assertTrue(record.getData().getOriginalSqlCommand().contains("IS NOT NULL"));
        assertNull(record.getException());
    }

    // ============================================================================
    // Tests for cleanExtraChars method
    // ============================================================================

    @Test
    void testCleanRedactionMarkersFromUsername() {
        // Test that CockroachDB extra characters are removed from username
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT 1\","
                + "\"User\":\"‹guardium_qa›\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertEquals("guardium_qa", record.getAppUserName());
        assertEquals("guardium_qa", record.getAccessor().getDbUser());
        assertFalse(record.getAppUserName().contains("‹"));
        assertFalse(record.getAppUserName().contains("›"));
    }

    @Test
    void testPreserveLessThanGreaterThanOperators() {
        // Test that SQL comparison operators < and > are NOT removed
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT * FROM users WHERE age > 18 AND salary < 100000\","
                + "\"Tag\":\"SELECT\","
                + "\"User\":\"guardium_qa\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"DatabaseName\":\"testdb\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNotNull(record.getData());
        String sql = record.getData().getOriginalSqlCommand();

        // Verify < and > operators are preserved
        assertTrue(sql.contains(">"), "Greater than operator should be preserved");
        assertTrue(sql.contains("<"), "Less than operator should be preserved");
        assertEquals("SELECT * FROM users WHERE age > 18 AND salary < 100000", sql);
    }

    @Test
    void testPreserveLessThanEqualGreaterThanEqual() {
        // Test that <= and >= operators are preserved
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT * FROM products WHERE price >= 10.00 AND stock <= 100\","
                + "\"Tag\":\"SELECT\","
                + "\"User\":\"guardium_qa\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"DatabaseName\":\"testdb\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNotNull(record.getData());
        String sql = record.getData().getOriginalSqlCommand();

        assertTrue(sql.contains(">="), "Greater than or equal operator should be preserved");
        assertTrue(sql.contains("<="), "Less than or equal operator should be preserved");
        assertEquals("SELECT * FROM products WHERE price >= 10.00 AND stock <= 100", sql);
    }

    @Test
    void testPreserveNotEqualOperator() {
        // Test that <> (not equal) operator is preserved
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT * FROM users WHERE status <> 'deleted'\","
                + "\"Tag\":\"SELECT\","
                + "\"User\":\"guardium_qa\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"DatabaseName\":\"testdb\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNotNull(record.getData());
        String sql = record.getData().getOriginalSqlCommand();

        assertTrue(sql.contains("<>"), "Not equal operator <> should be preserved");
        assertEquals("SELECT * FROM users WHERE status <> 'deleted'", sql);
    }

    @Test
    void testMixedRedactionMarkersAndSQLOperators() {
        // Test query with both extra characters AND SQL operators
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT * FROM users WHERE age > ‹18› AND salary < ‹100000› AND status <> ‹'inactive'›\","
                + "\"Tag\":\"SELECT\","
                + "\"User\":\"‹guardium_qa›\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"DatabaseName\":\"testdb\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNotNull(record.getData());
        String sql = record.getData().getOriginalSqlCommand();

        // Verify extra characters are removed
        assertFalse(sql.contains("‹"), "Extra character ‹ should be removed");
        assertFalse(sql.contains("›"), "Extra character › should be removed");

        // Verify SQL operators are preserved
        assertTrue(sql.contains(">"), "Greater than operator should be preserved");
        assertTrue(sql.contains("<"), "Less than operator should be preserved");
        assertTrue(sql.contains("<>"), "Not equal operator should be preserved");

        // Verify final result
        assertEquals("SELECT * FROM users WHERE age > 18 AND salary < 100000 AND status <> 'inactive'", sql);

        // Verify username is also cleaned
        assertEquals("guardium_qa", record.getAppUserName());
    }

    @Test
    void testMultipleRedactionMarkersInValues() {
        // Test multiple values with extra characters
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"INSERT INTO products (id, name, price) VALUES (‹1›, ‹'Product A'›, ‹19.99›), (‹2›, ‹'Product B'›, ‹29.99›)\","
                + "\"Tag\":\"INSERT\","
                + "\"User\":\"guardium_qa\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"DatabaseName\":\"testdb\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNotNull(record.getData());
        String sql = record.getData().getOriginalSqlCommand();

        assertFalse(sql.contains("‹"));
        assertFalse(sql.contains("›"));
        assertEquals("INSERT INTO products (id, name, price) VALUES (1, 'Product A', 19.99), (2, 'Product B', 29.99)", sql);
    }

    @Test
    void testComplexQueryWithBothMarkerTypes() {
        // Test complex query with BETWEEN, IN, and extra characters
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"SELECT * FROM orders WHERE total BETWEEN ‹100› AND ‹500› AND status IN (‹'pending'›, ‹'completed'›) AND created_at > ‹'2024-01-01'›\","
                + "\"Tag\":\"SELECT\","
                + "\"User\":\"guardium_qa\","
                + "\"ApplicationName\":\"$ cockroach sql\","
                + "\"DatabaseName\":\"testdb\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNotNull(record.getData());
        String sql = record.getData().getOriginalSqlCommand();

        // Verify extra characters removed
        assertFalse(sql.contains("‹"));
        assertFalse(sql.contains("›"));

        // Verify > operator preserved
        assertTrue(sql.contains(">"));

        // Verify final result
        assertEquals("SELECT * FROM orders WHERE total BETWEEN 100 AND 500 AND status IN ('pending', 'completed') AND created_at > '2024-01-01'", sql);
    }

    @Test
    void testOnlyRedactionMarkers() {
        // Test string with only extra characters
        String payload = "{"
                + "\"Timestamp\":1768332558964138753,"
                + "\"EventType\":\"query_execute\","
                + "\"Statement\":\"‹›‹›‹›\","
                + "\"Tag\":\"SELECT\","
                + "\"User\":\"‹›\","
                + "\"ClientIP\":\"127.0.0.1\","
                + "\"ClientPort\":\"49696\""
                + "}";

        final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
        Record record = parser.parseRecord(data);

        assertNotNull(record);
        assertNotNull(record.getData());
        assertEquals("", record.getData().getOriginalSqlCommand());
        assertEquals("", record.getAppUserName());
    }
}