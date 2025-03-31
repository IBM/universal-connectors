package com.ibm.guardium.trino;

import com.ibm.guardium.universalconnector.commons.custom_parsing.ParserFactory;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    String payload = "LEEF:1.0|Trino|Trino Server|466|FINISHED|CatalogName=hive\tDatabaseName=test_db\tTableName=test_table\tTime=2025-03-10T15:42:10.438Z\tClientIP=127.0.0.1\tServerIP=172.29.0.7\tServerPort=8080\tUser=trino\tSQLCommand=\"SELECT * FROM hive.test_db.test_table LIMIT 10\"\tQueryId=20250310_154210_00000_5858r\tError=null";
    Parser parser = new Parser(ParserFactory.ParserType.leef);

    @Test
    void test() {
        Record record = parser.parseRecord(payload);
        assertNotNull(record);

        assertEquals("test_db", record.getDbName());
        assertEquals(-1, record.getSessionLocator().getClientPort());
        assertEquals("127.0.0.1", record.getSessionLocator().getClientIp());
        assertEquals("Trino", record.getAccessor().getDbProtocol());
        assertEquals("172.29.0.7", record.getSessionLocator().getServerIp());
        assertEquals(8080, record.getSessionLocator().getServerPort());
        assertEquals("trino", record.getAccessor().getDbUser());
        assertEquals("Trino", record.getAccessor().getServerType());
        assertEquals("Time{timstamp=1741621330438, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
        assertEquals("\"SELECT * FROM hive.test_db.test_table LIMIT 10\"", record.getData().getOriginalSqlCommand());
        assertNull(record.getException());
    }
}
