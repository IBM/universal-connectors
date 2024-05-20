/*
Copyright IBM Corp. 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universalconnector.commons.structures.Record;

public class ParserTest {

	@Test
	public void parseRecordTest() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"117.98.4.93:1811\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,"+330");
		assertNotNull(record);
		assertEquals("-1109026561", record.getSessionId());
		assertEquals("", record.getAppUserName());
		assertEquals("", record.getDbName());
		assertNotNull(record.getTime());
		assertNotNull(record.getSessionLocator());
		assertNotNull(record.getAccessor());
		assertNotNull(record.getData());
		assertNull(record.getException());
	}
	@Test
	public void parseRecordTestException() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_failed\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"117.98.4.93:1811\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,"+330");
		assertNotNull(record);
		assertEquals("-1109026561", record.getSessionId());
		assertEquals("", record.getAppUserName());
		assertEquals("", record.getDbName());
		assertNotNull(record.getTime());
		assertNotNull(record.getSessionLocator());
		assertNotNull(record.getAccessor());
		assertNull(record.getData());
		assertNotNull(record.getException());
	}

	@Test
	public void parseTimeTest() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-12-22T15:54:24,534+0530\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"117.98.4.93:1811\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,"+330");
		assertNotNull(record);
		assertNotNull(record.getTime());
		assertEquals(Long.parseLong("1703240664534"), record.getTime().getTimstamp());
		assertEquals(0, record.getTime().getMinDst());
		assertEquals(0, record.getTime().getMinOffsetFromGMT());

	}

	@Test
	public void parseSessionLocatorTest() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"117.98.4.93:1811\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,0);
		assertNotNull(record);
		assertNotNull(record.getSessionLocator());
		assertEquals("117.98.4.93", record.getSessionLocator().getClientIp());
		assertEquals(1811, record.getSessionLocator().getClientPort());
		assertEquals("127.0.0.1", record.getSessionLocator().getServerIp());
		assertEquals(-1, record.getSessionLocator().getServerPort());
		assertEquals("", record.getSessionLocator().getClientIpv6());
		assertEquals("", record.getSessionLocator().getServerIpv6());

	}

	@Test
	public void parseAccessorTest() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"117.98.4.93:1811\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,0);
		assertNotNull(record);
		assertNotNull(record.getAccessor());
		assertEquals("elastic", record.getAccessor().getDbUser());
		assertEquals("Elasticsearch", record.getAccessor().getServerType());
		assertEquals("", record.getAccessor().getServerOs());
		assertEquals("", record.getAccessor().getClientOs());
		assertEquals("", record.getAccessor().getClientHostName());
		assertEquals("127.0.0.1", record.getAccessor().getServerHostName());
		assertEquals("", record.getAccessor().getCommProtocol());
		assertEquals("EL_SEARCH", record.getAccessor().getDbProtocol());
		assertEquals("", record.getAccessor().getDbProtocolVersion());
		assertEquals("", record.getAccessor().getOsUser());
		assertEquals("", record.getAccessor().getSourceProgram());
		assertEquals("", record.getAccessor().getClient_mac());
		assertEquals("", record.getAccessor().getServerDescription());
		assertEquals("", record.getAccessor().getServiceName());
		assertEquals("EL_SEARCH", record.getAccessor().getLanguage());
		assertEquals("TEXT", record.getAccessor().getDataType());

	}

	@Test
	public void parseDataTest() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"117.98.4.93:1811\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,"+330");
		assertNotNull(record);
		assertNotNull(record.getData());
		assertNull(record.getData().getConstruct());
		assertEquals("__AWS \"session_info\":{\"app_user_name\":\"\",\"server_host\":\"127.0.0.1\"} POST /_sql?format=txt#  {   \"query\": \"SELECT * FROM hcltesting1\" } ",
				record.getData().getOriginalSqlCommand());
	}

	@Test
	public void parseExceptionTest() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_failed\", \"user.name\":\"elastic\",\"origin.type\":\"rest\", \"origin.address\":\"117.98.4.93:1811\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,"+330");
		assertNotNull(record);
		assertNotNull(record.getException());
		assertEquals("LOGIN_FAILED", record.getException().getExceptionTypeId());
		assertEquals("authentication failed", record.getException().getDescription());
		assertEquals("NA", record.getException().getSqlString());
	}

	@Test
	public void testParseSessionId() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"117.98.4.93:1811\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,"+330");
		assertNotNull(record);
		assertEquals("-1109026561", record.getSessionId());

	}

	@Test
	public void testParseNoIp() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\",\"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"[::1]:1811\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,"+330");
		assertNotNull(record);
		assertNotNull(record.getSessionLocator());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("0.0.0.0", record.getSessionLocator().getServerIp());
	}

	@Test
	public void testParseNoPort() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\",\"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,"+330");
		assertNotNull(record);
		assertNotNull(record.getSessionLocator());
		assertEquals(-1, record.getSessionLocator().getClientPort());
		assertEquals(-1, record.getSessionLocator().getServerPort());
	}

	@Test
	public void testParseNoUser() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\",\"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\",\"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,"+330");
		assertNotNull(record);
		assertNotNull(record.getAccessor());
		assertEquals("NA", record.getAccessor().getDbUser());
	}

	@Test
	public void testParseNoServerHostName() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\",\"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\",\"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,"+330");
		assertNotNull(record);
		assertNotNull(record.getAccessor());
		assertEquals("", record.getAccessor().getServerHostName());
	}

	@Test
	public void testParseNoUrlQueryParameter() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-27T12:54:01,123+0000\", \"cluster.uuid\":\"B5MsxOf_SJOxilTmVUV_Tg\", \"node.name\":\"node-1\", \"node.id\":\"7OaddIpERFWnIP9Nu16KGA\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"[::1]:47554\", \"realm\":\"reserved\", \"url.path\":\"/_transform/ecommerce_transform3/_schedule_now\", \"request.method\":\"POST\", \"request.id\":\"H1xZXEJQQ0CH2xh-C_GzTQ\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		assertEquals("", Parser.getUrlQueryParameter(object));
	}
	@Test
	public void testParseUrlQueryParameter() throws Exception {
		final String message ="{\"type\":\"audit\", \"timestamp\":\"2023-10-03T11:47:05,748+0000\", \"cluster.uuid\":\"B5MsxOf_SJOxilTmVUV_Tg\", \"node.name\":\"node-1\", \"node.id\":\"7OaddIpERFWnIP9Nu16KGA\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"[::1]:42980\", \"realm\":\"reserved\", \"url.path\":\"/_application/search_application/my-app\", \"url.query\":\"create&dslapi&test&pretty\", \"request.method\":\"PUT\", \"request.body\":\"\\n{\\n  \\\"indices\\\": [ \\\"index1\\\", \\\"index2\\\" ],\\n  \\\"template\\\": {\\n    \\\"script\\\": {\\n      \\\"source\\\": {\\n        \\\"query\\\": {\\n          \\\"query_string\\\": {\\n            \\\"query\\\": \\\"{{query_string}}\\\",\\n            \\\"default_field\\\": \\\"{{default_field}}\\\"\\n          }\\n        }\\n      },\\n      \\\"params\\\": {\\n        \\\"query_string\\\": \\\"*\\\",\\n        \\\"default_field\\\": \\\"*\\\"\\n      }\\n    },\\n    \\\"dictionary\\\": {\\n      \\\"properties\\\": {\\n        \\\"query_string\\\": {\\n          \\\"type\\\": \\\"string\\\"\\n        },\\n        \\\"default_field\\\": {\\n          \\\"type\\\": \\\"string\\\",\\n          \\\"enum\\\": [\\n            \\\"title\\\",\\n            \\\"description\\\"\\n          ]\\n        },\\n        \\\"additionalProperties\\\": false\\n      },\\n      \\\"required\\\": [\\n        \\\"query_string\\\"\\n      ]\\n    }\\n  }\\n}\\n\", \"request.id\":\"NpDzlB6hRGSkLy8dheXkUw\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		assertEquals("?create&&dslapi&&test", Parser.getUrlQueryParameter(object));
	}
	@Test
	public void testParseUrlQueryParameterWithoutPrettyKeyword() throws Exception {
		final String message ="{\"type\":\"audit\", \"timestamp\":\"2023-10-03T11:47:05,748+0000\", \"cluster.uuid\":\"B5MsxOf_SJOxilTmVUV_Tg\", \"node.name\":\"node-1\", \"node.id\":\"7OaddIpERFWnIP9Nu16KGA\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"[::1]:42980\", \"realm\":\"reserved\", \"url.path\":\"/_application/search_application/my-app\", \"url.query\":\"create&dslapi&test\", \"request.method\":\"PUT\", \"request.body\":\"\\n{\\n  \\\"indices\\\": [ \\\"index1\\\", \\\"index2\\\" ],\\n  \\\"template\\\": {\\n    \\\"script\\\": {\\n      \\\"source\\\": {\\n        \\\"query\\\": {\\n          \\\"query_string\\\": {\\n            \\\"query\\\": \\\"{{query_string}}\\\",\\n            \\\"default_field\\\": \\\"{{default_field}}\\\"\\n          }\\n        }\\n      },\\n      \\\"params\\\": {\\n        \\\"query_string\\\": \\\"*\\\",\\n        \\\"default_field\\\": \\\"*\\\"\\n      }\\n    },\\n    \\\"dictionary\\\": {\\n      \\\"properties\\\": {\\n        \\\"query_string\\\": {\\n          \\\"type\\\": \\\"string\\\"\\n        },\\n        \\\"default_field\\\": {\\n          \\\"type\\\": \\\"string\\\",\\n          \\\"enum\\\": [\\n            \\\"title\\\",\\n            \\\"description\\\"\\n          ]\\n        },\\n        \\\"additionalProperties\\\": false\\n      },\\n      \\\"required\\\": [\\n        \\\"query_string\\\"\\n      ]\\n    }\\n  }\\n}\\n\", \"request.id\":\"NpDzlB6hRGSkLy8dheXkUw\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		assertEquals("?create&&dslapi&&test", Parser.getUrlQueryParameter(object));
	}

	@Test
	public void testParseNoRequestBody() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-27T12:54:01,123+0000\", \"cluster.uuid\":\"B5MsxOf_SJOxilTmVUV_Tg\", \"node.name\":\"node-1\", \"node.id\":\"7OaddIpERFWnIP9Nu16KGA\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"[::1]:47554\", \"realm\":\"reserved\", \"url.path\":\"/_transform/ecommerce_transform3/_schedule_now\", \"request.method\":\"POST\", \"request.id\":\"H1xZXEJQQ0CH2xh-C_GzTQ\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		assertEquals("", Parser.getRequestBody(object));
	}

	@Test
	public void testParseClientIpPort() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"[::1]:1811\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,"+330");
		assertNotNull(record);
		assertNotNull(record.getSessionLocator());
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals(1811, record.getSessionLocator().getClientPort());
	}

	@Test
	public void testParseServerIpPort() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"117.98.4.93:1811\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,"+330");
		assertNotNull(record);
		assertNotNull(record.getSessionLocator());
		assertEquals("127.0.0.1", record.getSessionLocator().getServerIp());
		assertEquals(-1, record.getSessionLocator().getServerPort());
	}
	@Test
	public void testParseServerClientIpv6() throws Exception {
		final String message = "{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"1233:0dc8:33a3:0000:0000:9a2e:0440:8834\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"[2001:0db8:85a3:0000:0000:8a2e:0370:7334]:8080\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		final JsonObject object = JsonParser.parseString(message).getAsJsonObject();
		Record record = Parser.parseRecord(object,"+330");
		assertNotNull(record);
		assertNotNull(record.getSessionLocator());
		assertEquals("1233:0dc8:33a3:0000:0000:9a2e:0440:8834", record.getSessionLocator().getServerIpv6());
		assertEquals("[2001:0db8:85a3:0000:0000:8a2e:0370:7334]", record.getSessionLocator().getClientIpv6());
		assertEquals(-1, record.getSessionLocator().getServerPort());
		assertEquals(8080, record.getSessionLocator().getClientPort());
		assertEquals("", record.getSessionLocator().getServerIp());
		assertEquals("", record.getSessionLocator().getClientIp());
		assertNotNull(record);
	}

}
