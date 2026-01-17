/*
Copyright IBM Corp. 2021, 2022, 2023 All rights reserved.

SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.spanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;

public class ParserTest {

	final String spannerString = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"111.44.22.999\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/98.0.4758.102 Safari\\/537.36 Edg\\/98.0.1108.62,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-03-07T04:17:51.801797793Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8SgFA0zcqVGRh4iari_Ve9m88l6J-w1F-Gk4AHOWy7JHBekHq_eloh2w\",\"request\":{\"queryMode\":\"PROFILE\",\"sql\":\"select Firstname,DOB From Users where id=2\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8SgFA0zcqVGRh4iari_Ve9m88l6J-w1F-Gk4AHOWy7JHBekHq_eloh2w\"}},\"insertId\":\"1snxb5rdfzg7\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"location\":\"us-central1\",\"project_id\":\"project-sccd\",\"instance_id\":\"spanner-test\"}},\"timestamp\":\"2022-03-07T04:17:51.801593049Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"7945483215916040071\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-03-07T04:17:53.543048234Z\"}";
	final JsonObject spannerJson = JsonParser.parseString(spannerString).getAsJsonObject();

	@Test
	public void testParseRecord() {
		Record record = Parser.parseRecord(spannerJson);
		assertEquals("2097373799", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertNotNull(record.getSessionLocator());
		assertNotNull(record.getAccessor());
	}

	@Test
	public void testParseSessionLocator() {
		Record record = Parser.parseRecord(spannerJson);
		SessionLocator actual = record.getSessionLocator();
		assertNotNull(record.getSessionLocator());
		assertEquals("0.0.0.0", actual.getServerIp());
		assertEquals(-1, actual.getServerPort());
		assertEquals("111.44.22.999", actual.getClientIp());
		assertEquals(-1, actual.getClientPort());
	}
	
	@Test
	public void testParseSessionLocator_Ipv6() {
		final String spannerString = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"pankaj-g766@hcl.com\",\"principalSubject\":\"user:pankaj-g766@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"2222:a00:222:4444:b0:222:db5c:f555\",\"callerSuppliedUserAgent\":\"grpc-c++\\/1.45.0-dev grpc-c\\/22.0.0 (linux; chttp2),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-25T09:35:01.505018272Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x94O_jIlXA2ed_pbzYxdsI071emhpt0lrXzUqcnftd0BLjqnmR_wRX-3w\",\"request\":{\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x94O_jIlXA2ed_pbzYxdsI071emhpt0lrXzUqcnftd0BLjqnmR_wRX-3w\",\"sql\":\"SELECT t0.LastName FROM (select Info,LastName from Users) AS t0 GROUP BY t0.LastName LIMIT 100;\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.spanner.v1.ResultSet\"}},\"insertId\":\"14inpo5e34435\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"location\":\"asia-south1\",\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-02-25T09:35:01.497225106Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"receiveTimestamp\":\"2022-02-25T09:35:03.181856942Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(spannerString).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		SessionLocator actual = record.getSessionLocator();
		assertNotNull(record.getSessionLocator());
		assertEquals("0000:0000:0000:0000:0000:FFFF:0000:0000", actual.getServerIpv6());
		assertTrue(actual.isIpv6());
		assertEquals(-1, actual.getServerPort());
		assertEquals(-1, actual.getClientPort());
		assertEquals("2222:a00:222:4444:b0:222:db5c:f555", actual.getClientIpv6());
	}

	@Test
	public void testParseAccessor() {
		Record record = Parser.parseRecord(spannerJson);
		Accessor actual = record.getAccessor();
		assertNotNull(record.getAccessor());
		assertEquals("project-sccd:spanner-test:test", actual.getServiceName());
		assertEquals("project-sccd_spanner-test_spanner.googleapis.com", actual.getServerHostName());
		assertEquals("user@test.com", actual.getDbUser());
	}

	@Test
	public void testParseConstruct() {
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertNotNull(sentence);
		assertNotNull(record);
		assertEquals("select", sentence.getVerb());
		assertEquals("Users", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

}
