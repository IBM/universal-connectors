package com.ibm.guardium.intersystemsiris;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universalconnector.commons.structures.UCRecord;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ParserTest {

	@Test
	public void testparseTime() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"vaishnavi.g\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals(Long.parseLong("1686209101250"), record.getTime().getTimstamp());
		assertEquals(0, record.getTime().getMinDst());
		assertEquals(0, record.getTime().getMinOffsetFromGMT());
		assertNotNull(record);
	}
	// problem
	@Test
	public void testparseNullClientIp() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"DynamicStatement\",\"namespace\":\"%SYS\",\"clientipaddress\":null,\"@timestamp\":\"2023-06-08T10:35:06.410Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"UnknownUser\",\"mytimestamp\":\"2023-06-08 10:07:53.452\",\"eventdata\":\" INSERT INTO samples1 VALUES ( ? , ? , ? ) /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10,1,1\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=1\\r\\n%CallArgs(2)=\\\"Vaishu\\\"\\r\\n%CallArgs(3)=\\\"Gopi\\\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertNotNull(record);
	}

	@Test
	public void testparseSessionLocator() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"DynamicStatement\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"@timestamp\":\"2023-06-08T10:35:06.410Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"UnknownUser\",\"mytimestamp\":\"2023-06-08 10:07:53.452\",\"eventdata\":\" INSERT INTO samples1 VALUES ( ? , ? , ? ) /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10,1,1\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=1\\r\\n%CallArgs(2)=\\\"Vaishu\\\"\\r\\n%CallArgs(3)=\\\"Gopi\\\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("", record.getSessionLocator().getClientIpv6());
		assertEquals("", record.getSessionLocator().getServerIpv6());
		assertEquals(-1, record.getSessionLocator().getServerPort());
		assertEquals(-1, record.getSessionLocator().getClientPort());
		assertNotNull(record);
	}

	@Test
	public void testparseSessionId() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"vaishnavi.g\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("-1211128616", record.getSessionId());
		assertNotNull(record);
	}

	@Test
	public void testparseClientIp() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"vaishnavi.g\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("20.204.110.141", record.getSessionLocator().getClientIp());
		assertNotNull(record);
	}

	@Test
	public void testparseEmptyClientIp() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"vaishnavi.g\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("0.0.0.0", record.getSessionLocator().getServerIp());
		assertNotNull(record);
	}

	@Test
	public void testparseNoIp() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"vaishnavi.g\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("0.0.0.0", record.getSessionLocator().getServerIp());
		assertNotNull(record);
	}

	@Test
	public void testparseServerIp() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"DynamicStatement\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"Server_ip\":\"80.104.110.181\",\"@timestamp\":\"2023-06-08T10:35:06.410Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"UnknownUser\",\"mytimestamp\":\"2023-06-08 10:07:53.452\",\"eventdata\":\" INSERT INTO samples1 VALUES ( ? , ? , ? ) /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10,1,1\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=1\\r\\n%CallArgs(2)=\\\"Vaishu\\\"\\r\\n%CallArgs(3)=\\\"Gopi\\\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("80.104.110.181", record.getSessionLocator().getServerIp());
		assertNotNull(record);
	}
	
	@Test
	public void testparseNullServerIp() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"DynamicStatement\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"serveripaddress\":null,\"@timestamp\":\"2023-06-08T10:35:06.410Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"UnknownUser\",\"mytimestamp\":\"2023-06-08 10:07:53.452\",\"eventdata\":\" INSERT INTO samples1 VALUES ( ? , ? , ? ) /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10,1,1\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=1\\r\\n%CallArgs(2)=\\\"Vaishu\\\"\\r\\n%CallArgs(3)=\\\"Gopi\\\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("0.0.0.0", record.getSessionLocator().getServerIp());
		assertNotNull(record);
	}

	@Test
	public void testparseEmptyServerIp() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"DynamicStatement\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"serveripaddress\":\"\",\"@timestamp\":\"2023-06-08T10:35:06.410Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"UnknownUser\",\"mytimestamp\":\"2023-06-08 10:07:53.452\",\"eventdata\":\" INSERT INTO samples1 VALUES ( ? , ? , ? ) /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10,1,1\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=1\\r\\n%CallArgs(2)=\\\"Vaishu\\\"\\r\\n%CallArgs(3)=\\\"Gopi\\\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("0.0.0.0", record.getSessionLocator().getServerIp());
		assertNotNull(record);
	}

	@Test
	public void testparseNoServerIp() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"DynamicStatement\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"@timestamp\":\"2023-06-08T10:35:06.410Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"UnknownUser\",\"mytimestamp\":\"2023-06-08 10:07:53.452\",\"eventdata\":\" INSERT INTO samples1 VALUES ( ? , ? , ? ) /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10,1,1\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=1\\r\\n%CallArgs(2)=\\\"Vaishu\\\"\\r\\n%CallArgs(3)=\\\"Gopi\\\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("0.0.0.0", record.getSessionLocator().getServerIp());
		assertNotNull(record);
	}

	@Test
	public void testServerClientIpv6() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"DynamicStatement\",\"namespace\":\"%SYS\",\"clientipaddress\":\"2001:0db8:85a3:0000:0000:8a2e:0370:7334\",\"Server_ip\":\"1233:0dc8:33a3:0000:0000:9a2e:0440:8834\",\"@timestamp\":\"2023-06-08T10:35:06.410Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"UnknownUser\",\"mytimestamp\":\"2023-06-08 10:07:53.452\",\"eventdata\":\" INSERT INTO samples1 VALUES ( ? , ? , ? ) /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10,1,1\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=1\\r\\n%CallArgs(2)=\\\"Vaishu\\\"\\r\\n%CallArgs(3)=\\\"Gopi\\\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334", record.getSessionLocator().getClientIpv6());
		assertEquals("1233:0dc8:33a3:0000:0000:9a2e:0440:8834", record.getSessionLocator().getServerIpv6());
		assertEquals(-1, record.getSessionLocator().getServerPort());
		assertEquals(-1, record.getSessionLocator().getClientPort());
		assertNotNull(record);
	}

	@Test
	public void testparseDbname() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"DynamicStatement\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"@timestamp\":\"2023-06-08T10:35:06.410Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"UnknownUser\",\"mytimestamp\":\"2023-06-08 10:07:53.452\",\"eventdata\":\" INSERT INTO samples1 VALUES ( ? , ? , ? ) /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10,1,1\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=1\\r\\n%CallArgs(2)=\\\"Vaishu\\\"\\r\\n%CallArgs(3)=\\\"Gopi\\\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("%SYS", record.getDbName());
		assertNotNull(record);
	}

	@Test
	public void testparseEmptyDbname() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"DynamicStatement\",\"namespace\":\"\",\"clientipaddress\":\"20.204.110.141\",\"@timestamp\":\"2023-06-08T10:35:06.410Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"UnknownUser\",\"mytimestamp\":\"2023-06-08 10:07:53.452\",\"eventdata\":\" INSERT INTO samples1 VALUES ( ? , ? , ? ) /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10,1,1\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=1\\r\\n%CallArgs(2)=\\\"Vaishu\\\"\\r\\n%CallArgs(3)=\\\"Gopi\\\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("", record.getDbName());
		assertNotNull(record);
	}

	@Test
	public void testparseNoDbname() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"DynamicStatement\",\"clientipaddress\":\"20.204.110.141\",\"@timestamp\":\"2023-06-08T10:35:06.410Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"UnknownUser\",\"mytimestamp\":\"2023-06-08 10:07:53.452\",\"eventdata\":\" INSERT INTO samples1 VALUES ( ? , ? , ? ) /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10,1,1\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=1\\r\\n%CallArgs(2)=\\\"Vaishu\\\"\\r\\n%CallArgs(3)=\\\"Gopi\\\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("", record.getDbName());
		assertNotNull(record);
	}

	@Test
	public void testparseDbuser() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"DynamicStatement\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"@timestamp\":\"2023-06-08T10:35:06.410Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"UnknownUser\",\"mytimestamp\":\"2023-06-08 10:07:53.452\",\"eventdata\":\" INSERT INTO samples1 VALUES ( ? , ? , ? ) /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10,1,1\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=1\\r\\n%CallArgs(2)=\\\"Vaishu\\\"\\r\\n%CallArgs(3)=\\\"Gopi\\\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("UnknownUser", record.getAccessor().getDbUser());
		assertNotNull(record);
	}

	@Test
	public void testparseEmptyDbuser() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"DynamicStatement\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"@timestamp\":\"2023-06-08T10:35:06.410Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"\",\"mytimestamp\":\"2023-06-08 10:07:53.452\",\"eventdata\":\" INSERT INTO samples1 VALUES ( ? , ? , ? ) /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10,1,1\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=1\\r\\n%CallArgs(2)=\\\"Vaishu\\\"\\r\\n%CallArgs(3)=\\\"Gopi\\\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("NA", record.getAccessor().getDbUser());
		assertNotNull(record);
	}

	@Test
	public void testparseNoDbuser() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"DynamicStatement\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"@timestamp\":\"2023-06-08T10:35:06.410Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"mytimestamp\":\"2023-06-08 10:07:53.452\",\"eventdata\":\" INSERT INTO samples1 VALUES ( ? , ? , ? ) /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10,1,1\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=1\\r\\n%CallArgs(2)=\\\"Vaishu\\\"\\r\\n%CallArgs(3)=\\\"Gopi\\\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("NA", record.getAccessor().getDbUser());
		assertNotNull(record);
	}

	@Test
	public void testparseServerhostname() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"vaishnavi.g\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("ip-172-31-91-96:IRIS", record.getAccessor().getServerHostName());
		assertNotNull(record);
	}

	@Test
	public void testparseEmptyServerhostname() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"vaishnavi.g\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("", record.getAccessor().getServerHostName());
		assertNotNull(record);
	}

	@Test
	public void testparseNoServerhostname() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"vaishnavi.g\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("", record.getAccessor().getServerHostName());
		assertNotNull(record);
	}

	@Test
	public void testparseOsUsername() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"vaishnavi.g\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("vaishnavi.g", record.getAccessor().getOsUser());
		assertNotNull(record);
	}

	@Test
	public void testparseEmptyOsUsername() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("", record.getAccessor().getOsUser());
		assertNotNull(record);
	}

	@Test
	public void testparseNoOsUsername() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("", record.getAccessor().getOsUser());
		assertNotNull(record);
	}

	@Test
	public void testparseProtocolServer() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"vaishnavi.g\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("IRIS", record.getAccessor().getDbProtocol());
		assertEquals("IRIS", record.getAccessor().getServerType());
		assertNotNull(record);
	}

	@Test
	public void testparseAccesor() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"vaishnavi.g\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("", record.getAccessor().getClientHostName());
		assertEquals("", record.getAccessor().getClientOs());
		assertEquals("", record.getAccessor().getSourceProgram());
		assertEquals("", record.getAccessor().getClient_mac());
		assertEquals("", record.getAccessor().getCommProtocol());
		assertEquals("", record.getAccessor().getClientOs());
		assertEquals("", record.getAccessor().getServerOs());
		assertEquals("", record.getAccessor().getServerDescription());
		assertEquals("", record.getAccessor().getDbProtocolVersion());
		assertNotNull(record);
	}

	@Test
	public void testparseLanguage() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"vaishnavi.g\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertNotNull(record);
	}

	@Test
	public void testparseDatatype() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"vaishnavi.g\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertNotNull(record);
	}

	@Test
	public void testparseException() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"LoginFailure\",\"namespace\":\"%SYS\",\"clientipaddress\":\"103.149.70.62\",\"@timestamp\":\"2023-06-08T10:39:02.992Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"guardium_dev\",\"mytimestamp\":\"2023-06-08 10:41:11.842\",\"eventdata\":\"Error message: ERROR #798: Password login failed\\r\\nERROR #952: Invalid password\\r\\nWeb Application: /csp/sys\\r\\n$I: |TCP|1972|1689\\r\\n$P: |TCP|1972|1689\\r\\n\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("Error message: ERROR #798: Password login failed\r\nERROR #952: Invalid password\r\nWeb Application: /csp/sys\r\n$I: |TCP|1972|1689\r\n$P: |TCP|1972|1689\r\n",record.getException().getDescription());
		assertEquals("LOGIN_FAILED", record.getException().getExceptionTypeId());
		assertEquals("NA", record.getException().getSqlString());
		assertNotNull(record);
	}

	@Test
	public void testparseEmptyExceptionMessage() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"LoginFailure\",\"namespace\":\"%SYS\",\"clientipaddress\":\"103.149.70.62\",\"@timestamp\":\"2023-06-08T10:39:02.992Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"guardium_dev\",\"mytimestamp\":\"2023-06-08 10:41:11.842\",\"eventdata\":\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("NA", record.getException().getDescription());
		assertEquals("LOGIN_FAILED", record.getException().getExceptionTypeId());
		assertEquals("NA", record.getException().getSqlString());
		assertNotNull(record);
	}

	@Test
	public void testparseNoExceptionMessage() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"LoginFailure\",\"namespace\":\"%SYS\",\"clientipaddress\":\"103.149.70.62\",\"@timestamp\":\"2023-06-08T10:39:02.992Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"guardium_dev\",\"mytimestamp\":\"2023-06-08 10:41:11.842\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("NA", record.getException().getDescription());
		assertEquals("LOGIN_FAILED", record.getException().getExceptionTypeId());
		assertEquals("NA", record.getException().getSqlString());
		assertNotNull(record);
	}

	@Test
	public void testparseNoEventtype() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"namespace\":\"%SYS\",\"clientipaddress\":\"103.149.70.62\",\"event\":\"DynamicStatement\",\"@timestamp\":\"2023-06-08T10:39:02.992Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"guardium_dev\",\"mytimestamp\":\"2023-06-08 10:41:11.842\",\"eventdata\":\" \\r\\nDROP TABLE soumalya\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("DROP TABLE", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("soumalya", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
		assertNotNull(record);
	}

	@Test
	public void testparseCreateTable() throws Exception {
		final String message = "{\"@timestamp\":\"2023-06-08T07:23:04.607Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"@version\":\"1\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"username\":\"guardium_dev\",\"eventdata\":\"CREATE TABLE sample_Tab(ID int, FirstName varchar(255), LastName varchar(255)) /*#OPTIONS {\\\"xDBC\\\":1} */\",\"event\":\"XDBCStatement\",\"mytimestamp\":\"2023-06-08 07:25:01.250\",\"type\":\"test\",\"osusername\":\"vaishnavi.g\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("CREATE TABLE", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("sample_Tab", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}

	@Test
	public void testparseInsertTable() throws Exception {
		final String message = "{\"@version\":\"1\",\"osusername\":\"CSP Gateway\",\"event\":\"DynamicStatement\",\"namespace\":\"%SYS\",\"clientipaddress\":\"20.204.110.141\",\"@timestamp\":\"2023-06-08T10:35:06.410Z\",\"systemid\":\"ip-172-31-91-96:IRIS\",\"username\":\"UnknownUser\",\"mytimestamp\":\"2023-06-08 10:07:53.452\",\"eventdata\":\" INSERT INTO samples1 VALUES ( ? , ? , ? ) /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10,1,1\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=1\\r\\n%CallArgs(2)=\\\"Vaishu\\\"\\r\\n%CallArgs(3)=\\\"Gopi\\\"\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("INSERT", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("samples1", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}

	@Test
	public void testparseDropTable() throws Exception {
		final String message = "{\"osusername\":\"CSP Gateway\",\"@timestamp\":\"2023-06-29T08:11:07.314Z\",\"mytimestamp\":\"2023-06-29 08:09:13.953\",\"username\":\"UnknownUser\",\"namespace\":\"IRISDB2605\",\"type\":\"test\",\"eventdata\":\" \\r\\nDROP TABLE soumalya\",\"@version\":\"1\",\"clientipaddress\":\"20.204.110.141\",\"event\":\"DynamicStatement\",\"systemid\":\"ip-172-31-33-219:IRIS\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("DROP TABLE", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("soumalya", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}

	@Test
	public void testparseUpdateTable() throws Exception {
		final String message = "{\"clientipaddress\":\"20.204.110.141\",\"event\":\"DynamicStatement\",\"@timestamp\":\"2023-06-29T08:32:07.068Z\",\"mytimestamp\":\"2023-06-29 08:29:59.104\",\"username\":\"UnknownUser\",\"eventdata\":\" UPDATE soumalya SET FirstName = ? WHERE ID = ? /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"1,10\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=\\\"Nitin\\\"\\r\\n%CallArgs(2)=1\",\"type\":\"test\",\"systemid\":\"ip-172-31-33-219:IRIS\",\"namespace\":\"IRISDB2605\",\"@version\":\"1\",\"osusername\":\"CSP Gateway\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("UPDATE", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("soumalya", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseSelect() throws Exception {
		final String message = "{\"eventdata\":\" SELECT * FROM demotable4\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\",\"event\":\"DynamicStatement\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"namespace\":\"IRISDB2605\",\"mytimestamp\":\"2023-07-04 05:45:37.822\",\"@timestamp\":\"2023-07-04T05:45:11.664Z\",\"systemid\":\"ip-172-31-33-219:IRIS\",\"clientipaddress\":null,\"username\":\"UnknownUser\",\"@version\":\"1\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("SELECT", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("demotable4", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseNestedSelect() throws Exception {
		final String message = "{\"eventdata\":\"SELECT Name,Salary,Home_State FROM Sample.Employee WHERE Salary > 75000 AND Home_State = ANY (SELECT State FROM Sample.USZipCode  WHERE Longitude < -93) ORDER BY Home_State\",\"event\":\"DynamicStatement\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"namespace\":\"IRISDB2605\",\"mytimestamp\":\"2023-07-04 05:45:37.822\",\"@timestamp\":\"2023-07-04T05:45:11.664Z\",\"systemid\":\"ip-172-31-33-219:IRIS\",\"clientipaddress\":null,\"username\":\"UnknownUser\",\"@version\":\"1\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("SELECT", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("Sample.Employee", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseDelete() throws Exception {
		final String message = "{\"eventdata\":\" DELETE FROM Employee1 WHERE EmpNum = ? /*#OPTIONS {\\\"DynamicSQLTypeList\\\":\\\"10\\\"} */\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\\r\\nParameter values:\\r\\n%CallArgs(1)=12\",\"event\":\"DynamicStatement\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"namespace\":\"IRISDB2605\",\"mytimestamp\":\"2023-07-04 05:54:37.940\",\"@timestamp\":\"2023-07-04T05:54:06.877Z\",\"systemid\":\"ip-172-31-33-219:IRIS\",\"clientipaddress\":null,\"username\":\"UnknownUser\",\"@version\":\"1\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("DELETE", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("Employee1", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseTruncate() throws Exception {
		final String message = "{\"eventdata\":\" TRUNCATE TABLE Employee1\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\",\"event\":\"DynamicStatement\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"namespace\":\"IRISDB2605\",\"mytimestamp\":\"2023-07-04 05:57:28.458\",\"@timestamp\":\"2023-07-04T05:57:05.222Z\",\"systemid\":\"ip-172-31-33-219:IRIS\",\"clientipaddress\":null,\"username\":\"UnknownUser\",\"@version\":\"1\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("TRUNCATE TABLE", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("Employee1", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseCreateProcedure() throws Exception {
		final String message ="{\"eventdata\":\"CREATE PROCEDURE UpdatePaySP (IN Salary INTEGER DEFAULT 0, IN Name VARCHAR(50), INOUT PayBracket VARCHAR(50) DEFAULT 'NULL') BEGIN UPDATE Sample.Person SET Salary = :Salary WHERE Name=:Name ; END\",\"event\":\"DynamicStatement\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"namespace\":\"%SYS\",\"mytimestamp\":\"2023-07-04 06:32:25.931\",\"@timestamp\":\"2023-07-04T06:32:02.378Z\",\"systemid\":\"ip-172-31-33-219:IRIS\",\"clientipaddress\":null,\"username\":\"UnknownUser\",\"@version\":\"1\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("CREATE PROCEDURE", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("UpdatePaySP", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseExplain() throws Exception {
		final String message = "{\"clientipaddress\":null,\"mytimestamp\":\"2023-07-04 09:42:03.016\",\"@timestamp\":\"2023-07-04T09:42:01.401Z\",\"osusername\":\"CSP Gateway\",\"username\":\"UnknownUser\",\"type\":\"test\",\"systemid\":\"ip-172-31-33-219:IRIS\",\"event\":\"DynamicStatement\",\"namespace\":\"%SYS\",\"eventdata\":\"SELECT %SYSTEM.QUERY_PLAN('SELECT * FROM Emp  ',0,'ShowPlan') AS Plan\\r\\n /*#OPTIONS { \\\"IsolationLevel\\\":0 } */\",\"@version\":\"1\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
//		assertEquals("ShowPlan", record.getData().getConstruct().getSentences().get(0).getVerb());
//		assertEquals("Emp", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseLockTable() throws Exception {
		final String message = "{\"eventdata\":\" \\r\\nLOCK TABLE Emp IN SHARE MODE \\r\\n\",\"event\":\"DynamicStatement\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"namespace\":\"%SYS\",\"mytimestamp\":\"2023-07-04 06:43:34.577\",\"@timestamp\":\"2023-07-04T06:43:02.496Z\",\"systemid\":\"ip-172-31-33-219:IRIS\",\"clientipaddress\":null,\"username\":\"UnknownUser\",\"@version\":\"1\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
//		assertEquals("LOCK", record.getData().getConstruct().getSentences().get(0).getVerb());
//		assertEquals("Emp", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseCreateUser() throws Exception {
		final String  message = "{\"eventdata\":\" \\r\\nCREATE USER DEMOUSER identified by 12345 \",\"event\":\"DynamicStatement\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"namespace\":\"%SYS\",\"mytimestamp\":\"2023-07-04 07:02:19.129\",\"@timestamp\":\"2023-07-04T07:02:01.681Z\",\"systemid\":\"ip-172-31-33-219:IRIS\",\"clientipaddress\":null,\"username\":\"UnknownUser\",\"@version\":\"1\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("CREATE USER", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("DEMOUSER", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseCreateRole() throws Exception {
		final String message = "{\"eventdata\":\" \\r\\nCREATE ROLE role1\",\"event\":\"DynamicStatement\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"namespace\":\"%SYS\",\"mytimestamp\":\"2023-07-04 07:03:41.324\",\"@timestamp\":\"2023-07-04T07:04:04.079Z\",\"systemid\":\"ip-172-31-33-219:IRIS\",\"clientipaddress\":null,\"username\":\"UnknownUser\",\"@version\":\"1\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("CREATE ROLE", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("role1", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseGrant() throws Exception {
		final String message = "{\"eventdata\":\" \\r\\nGRANT role1 to DEMOUSER\",\"event\":\"DynamicStatement\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"namespace\":\"%SYS\",\"mytimestamp\":\"2023-07-04 07:05:34.261\",\"@timestamp\":\"2023-07-04T07:05:02.506Z\",\"systemid\":\"ip-172-31-33-219:IRIS\",\"clientipaddress\":null,\"username\":\"UnknownUser\",\"@version\":\"1\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("GRANT", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("role1", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseRevoke() throws Exception {
		final String message = "{\"eventdata\":\" \\r\\nREVOKE role1 from DEMOUSER\",\"event\":\"DynamicStatement\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"namespace\":\"%SYS\",\"mytimestamp\":\"2023-07-04 07:07:15.311\",\"@timestamp\":\"2023-07-04T07:07:01.432Z\",\"systemid\":\"ip-172-31-33-219:IRIS\",\"clientipaddress\":null,\"username\":\"UnknownUser\",\"@version\":\"1\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
//		assertEquals("REVOKE", record.getData().getConstruct().getSentences().get(0).getVerb());
//		assertEquals("", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseCreateMLConfig() throws Exception {
		final String message = "{\"namespace\":\"%SYS\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"eventdata\":\" \\r\\nCREATE ML CONFIGURATION autoML_config PROVIDER AutoML %DESCRIPTION 'my AutoML configuration!'\",\"mytimestamp\":\"2023-07-05 06:13:32.013\",\"username\":\"UnknownUser\",\"@timestamp\":\"2023-07-05T06:26:05.660Z\",\"@version\":\"1\",\"clientipaddress\":null,\"systemid\":\"ip-172-31-33-219:IRIS\",\"event\":\"DynamicStatement\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("CREATE ML CONFIGURATION", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("autoML_config", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseCreateSchema() throws Exception {
		final String  message = "{\"namespace\":\"%SYS\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"eventdata\":\" \\r\\nCREATE SCHEMA DEMOSCHEMA\",\"mytimestamp\":\"2023-07-05 06:27:26.516\",\"username\":\"UnknownUser\",\"@timestamp\":\"2023-07-05T06:27:05.317Z\",\"@version\":\"1\",\"clientipaddress\":null,\"systemid\":\"ip-172-31-33-219:IRIS\",\"event\":\"DynamicStatement\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("CREATE SCHEMA", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("DEMOSCHEMA", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseAlterUser() throws Exception {
		final String message = "{\"namespace\":\"%SYS\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"eventdata\":\" \\r\\nALTER USER DEMOUSER IDENTIFY BY 54321\",\"mytimestamp\":\"2023-07-05 06:31:57.318\",\"username\":\"UnknownUser\",\"@timestamp\":\"2023-07-05T06:32:03.628Z\",\"@version\":\"1\",\"clientipaddress\":null,\"systemid\":\"ip-172-31-33-219:IRIS\",\"event\":\"DynamicStatement\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("ALTER USER", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("DEMOUSER", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseCreateAggregateFunc() throws Exception {
		final String message = "{\"namespace\":\"%SYS\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"eventdata\":\" \\r\\nCREATE AGGREGATE Sample.SumAddSub(arg NUMERIC(4,1)) ITERATE WITH Sample.AddSub\",\"mytimestamp\":\"2023-07-05 06:38:50.424\",\"username\":\"UnknownUser\",\"@timestamp\":\"2023-07-05T06:39:02.071Z\",\"@version\":\"1\",\"clientipaddress\":null,\"systemid\":\"ip-172-31-33-219:IRIS\",\"event\":\"DynamicStatement\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("CREATE AGGREGATE", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("Sample.SumAddSub", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseDropAggregateFunc() throws Exception {
		final String message = "{\"namespace\":\"%SYS\",\"type\":\"test\",\"osusername\":\"CSP Gateway\",\"eventdata\":\" \\r\\nDROP AGGREGATE Sample.SumAddSub\",\"mytimestamp\":\"2023-07-05 06:44:52.656\",\"username\":\"UnknownUser\",\"@timestamp\":\"2023-07-05T06:44:28.219Z\",\"@version\":\"1\",\"clientipaddress\":null,\"systemid\":\"ip-172-31-33-219:IRIS\",\"event\":\"DynamicStatement\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("DROP AGGREGATE", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("Sample.SumAddSub", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
	
	@Test
	public void testparseCreateModel() throws Exception {
		final String message ="{\"eventdata\":\" \\r\\nCREATE MODEL SpamFilter PREDICTING (IsSpam) WITH (email_length int, subject_title varchar) \\r\\n\",\"mytimestamp\":\"2023-07-05 07:27:00.858\",\"namespace\":\"%SYS\",\"osusername\":\"CSP Gateway\",\"clientipaddress\":null,\"username\":\"UnknownUser\",\"event\":\"DynamicStatement\",\"systemid\":\"ip-172-31-33-219:IRIS\",\"@timestamp\":\"2023-07-05T07:29:04.424Z\",\"@version\":\"1\",\"type\":\"test\"}";
		final JsonObject irisJson = JsonParser.parseString(message).getAsJsonObject();
		UCRecord record = Parser.parseRecord(irisJson);
		assertEquals("CONSTRUCT", record.getAccessor().getDataType());
		assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("CREATE MODEL", record.getData().getConstruct().getSentences().get(0).getVerb());
		assertEquals("SpamFilter", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
	}
}
