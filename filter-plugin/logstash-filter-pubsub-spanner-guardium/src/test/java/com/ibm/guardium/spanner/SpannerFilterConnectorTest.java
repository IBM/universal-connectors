/*
Copyright IBM Corp. 2021, 2022, 2023 All rights reserved.

SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.spanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.logstash.Event;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
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

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.PluginConfigSpec;

public class SpannerFilterConnectorTest {

	@Test
	public void configSchemaTest() {
		final String gcpString2 = "";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Collection<PluginConfigSpec<?>> events = filter.configSchema();
		assertNotNull(events);
	}

	@Test
	public void commonUtilsTest() {
		CommonUtils commonUtils = new CommonUtils();
		assertNotNull(commonUtils);
	}

	@Test
	public void getIdTest() {
		final String gcpString2 = "{\"protoPayload\":\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"pankaj-g766@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.73\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-04-26T12:02:34.853241708Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0ZRnChs6K4dbngNt3ICWHWlM7PEK_0zGralREXja09C-tbna2rqiPl9yQ\",\"request\":{\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0ZRnChs6K4dbngNt3ICWHWlM7PEK_0zGralREXja09C-tbna2rqiPl9yQ\",\"sql\":\"select * from singers\"}},\"insertId\"\"1lpovsaf2u0n15\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\",\"instance_config\":\"\",\"location\":\"us-central1\"}},\"timestamp\":\"2022-04-26T12:02:34.852924678Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"12631762209399029108\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\"\"2022-04-26T12:02:36.143153614Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		String id = filter.getId();
		assertEquals("spn-id", id);
   }

	@Test
	public void filterEmptyJSONTest() {
		final String gcpString2 = "";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		Collection<co.elastic.logstash.api.Event> events = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
		assertNotNull(events);
	}
	@Test
	public void filterInvalidJSONTest() {
		final String gcpString2 = "{\"protoPayload\":\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.73\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-04-26T12:02:34.853241708Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0ZRnChs6K4dbngNt3ICWHWlM7PEK_0zGralREXja09C-tbna2rqiPl9yQ\",\"request\":{\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0ZRnChs6K4dbngNt3ICWHWlM7PEK_0zGralREXja09C-tbna2rqiPl9yQ\",\"sql\":\"select * from singers\"}},\"insertId\"\"1lpovsaf2u0n15\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\",\"instance_config\":\"\",\"location\":\"us-central1\"}},\"timestamp\":\"2022-04-26T12:02:34.852924678Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"12631762209399029108\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\"\"2022-04-26T12:02:36.143153614Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(0, result.size());
	}

	@Test
	public void filterTestCheck() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.188.229\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/98.0.4758.102 Safari\\/537.36 Edg\\/98.0.1108.62,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-03-06T10:03:43.817387774Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x_3xb7nwA0H7S6B8hLV3AzHNPVKl7PzemyrtB21HA3RoJOaQxccdeqoAw\",\"request\":{\"sql\":\"WITH Values AS (\\n SELECT 1 x, 'a' y UNION ALL \\n SELECT 1 x, #getting offset value \\n 'b' y UNION ALL \\n SELECT 2 x, 'a' y UNION ALL \\n SELECT 2 x, 'c' y \\n ) SELECT x, ARRAY_AGG(y) as array_agg FROM Values GROUP BY x;\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"queryMode\":\"PROFILE\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x_3xb7nwA0H7S6B8hLV3AzHNPVKl7PzemyrtB21HA3RoJOaQxccdeqoAw\"}},\"insertId\":\"1snxb5rdflup\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"location\":\"us-central1\",\"project_id\":\"project-sccd\",\"instance_id\":\"spanner-test\"}},\"timestamp\":\"2022-03-06T10:03:43.817102127Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"13710611471873755832\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-03-06T10:03:44.673334332Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("1875025146", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("select", sentence.getVerb());
		assertEquals("Values", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}
	
	@Test
	public void filterTestRecord() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.12.209\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/98.0.4758.102 Safari\\/537.36 Edg\\/98.0.1108.62,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-03-07T04:17:51.801797793Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8SgFA0zcqVGRh4iari_Ve9m88l6J-w1F-Gk4AHOWy7JHBekHq_eloh2w\",\"request\":{\"queryMode\":\"PROFILE\",\"sql\":\"select Firstname,DOB From Users where id=2\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8SgFA0zcqVGRh4iari_Ve9m88l6J-w1F-Gk4AHOWy7JHBekHq_eloh2w\"}},\"insertId\":\"1snxb5rdfzg7\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"location\":\"us-central1\",\"project_id\":\"project-sccd\",\"instance_id\":\"spanner-test\"}},\"timestamp\":\"2022-03-07T04:17:51.801593049Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"7945483215916040071\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-03-07T04:17:53.543048234Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("-1979925154", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("select", sentence.getVerb());
		assertEquals("Users", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
		assertNotNull(record.getSessionLocator());
		assertNotNull(record.getAccessor());
		assertNotNull(record.getData());
		assertNotNull(record.getTime());
	}

	@Test
	public void filterTestGrant() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:vejalla.indrani@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"117.209.13.187\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/108.0.0.0 Safari\\/537.36,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-12-15T15:17:10.764878109Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.UpdateDatabaseDdl\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/test-span\\/databases\\/test-database\",\"permission\":\"spanner.databases.updateDdl\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/test-span\\/databases\\/test-database\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/test-span\\/databases\\/test-database\",\"request\":{\"database\":\"projects\\/project-sccd\\/instances\\/test-span\\/databases\\/test-database\",\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest\",\"statements\":[\"GRANT INSERT(SingerId, FirstName, LastName, SingerInfo) ON TABLE Singers TO ROLE span_qa\",\"GRANT SELECT(name, level, location), UPDATE(location) ON TABLE employees, contractors TO ROLE hr_manager\"]},\"response\":{\"@type\":\"type.googleapis.com\\/google.longrunning.Operation\"}},\"insertId\":\"kskofjncebo\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"location\":\"asia-south1\",\"instance_config\":\"\",\"instance_id\":\"test-span\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-12-15T15:17:10.759430837Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"id\":\"projects\\/project-sccd\\/instances\\/test-span\\/databases\\/test-database\\/operations\\/_auto_op_36c1cf6cc65bd52f\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-12-15T15:17:14.475070376Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("1716037752", record.getSessionId());
		assertEquals("project-sccd:test-span:test-database", record.getDbName());
		assertEquals("grant", sentence.getVerb());
		assertEquals("Singers", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
		}

	@Test
	public void filterTestRevoke() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:vejalla.indrani@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"117.209.13.187\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/108.0.0.0 Safari\\/537.36,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-12-15T15:17:10.764878109Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.UpdateDatabaseDdl\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/test-span\\/databases\\/test-database\",\"permission\":\"spanner.databases.updateDdl\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/test-span\\/databases\\/test-database\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/test-span\\/databases\\/test-database\",\"request\":{\"database\":\"projects\\/project-sccd\\/instances\\/test-span\\/databases\\/test-database\",\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest\",\"statements\":[\"REVOKE SELECT ON TABLE Albums FROM ROLE hr_rep\",\"DROP TABLE TEST\"]},\"response\":{\"@type\":\"type.googleapis.com\\/google.longrunning.Operation\"}},\"insertId\":\"kskofjncebo\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"location\":\"asia-south1\",\"instance_config\":\"\",\"instance_id\":\"test-span\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-12-15T15:17:10.759430837Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"id\":\"projects\\/project-sccd\\/instances\\/test-span\\/databases\\/test-database\\/operations\\/_auto_op_36c1cf6cc65bd52f\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-12-15T15:17:14.475070376Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("1716037752", record.getSessionId());
		assertEquals("project-sccd:test-span:test-database", record.getDbName());
		assertEquals("revoke", sentence.getVerb());
		assertEquals("Albums", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterNoRquestEntryInJsonTest() {
		final String gcpString2 = "{\"insertId\":\"1xxqxsxcnww\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"first\":true,\"id\":\"projects\\/project-sccd\\/instances\\/spanner-plugin\\/databases\\/spanner-db\\/operations\\/_auto_op_9076614e440f9535\",\"last\":true,\"producer\":\"spanner.googleapis.com\"},\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:anandkumar.saini778@hcl.com\"},\"authorizationInfo\":[{\"granted\":true,\"permission\":\"spanner.databases.create\",\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-plugin\",\"resourceAttributes\":{\"name\":\"projects\\/project-sccd\\/instances\\/spanner-plugin\\/databases\\/spanner-db\",\"service\":\"spanner\",\"type\":\"spanner.databases\"}}],\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.CreateDatabase\",\"request\":{},\"requestMetadata\":{\"callerIp\":\"148.64.5.69\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/93.0.4577.82 Safari\\/537.36,gzip(gfe)\",\"destinationAttributes\":{},\"requestAttributes\":{\"auth\":{},\"time\":\"2021-09-22T05:18:43.475279952Z\"}},\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-plugin\\/databases\\/spanner-db\",\"serviceName\":\"spanner.googleapis.com\",\"status\":{}},\"receiveTimestamp\":\"2021-09-22T05:18:50.888616258Z\",\"resource\":{\"labels\":{\"instance_config\":\"\",\"instance_id\":\"spanner-plugin\",\"location\":\"asia-southeast1\",\"project_id\":\"project-sccd\"},\"type\":\"spanner_instance\"},\"severity\":\"NOTICE\",\"timestamp\":\"2021-09-22T05:18:43.470573031Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterAuthenticationInfoNegTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"requestMetadata\":{\"callerIp\":\"000.00.33.00\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.74 Safari\\/537.36 Edg\\/99.0.1150.55,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-04-06T05:18:49.347990923Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.write\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8dQcMTHwxehFLsffoKBmyRKRikyGbUm252KGy3UjQQa_CVTzqp4Czayw\",\"request\":{\"sql\":\"UPDATE Concerts SET TicketPrices = [25, 50, 100] WHERE VenueId = 1;\",\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8dQcMTHwxehFLsffoKBmyRKRikyGbUm252KGy3UjQQa_CVTzqp4Czayw\"}},\"insertId\":\"8bxmqjejo40t\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"location\":\"us-central1\"}},\"timestamp\":\"2022-04-06T05:18:49.347497042Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"253676221933612491\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-06T05:18:49.568984479Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterAuthorizationInfoNegTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"180.94.33.60\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.74 Safari\\/537.36 Edg\\/99.0.1150.55,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-04-06T05:18:49.347990923Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8dQcMTHwxehFLsffoKBmyRKRikyGbUm252KGy3UjQQa_CVTzqp4Czayw\",\"request\":{\"sql\":\"UPDATE Concerts SET TicketPrices = [25, 50, 100] WHERE VenueId = 1;\",\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8dQcMTHwxehFLsffoKBmyRKRikyGbUm252KGy3UjQQa_CVTzqp4Czayw\"}},\"insertId\":\"8bxmqjejo40t\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"location\":\"us-central1\"}},\"timestamp\":\"2022-04-06T05:18:49.347497042Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"253676221933612491\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-06T05:18:49.568984479Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterInstanceNameNegTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"180.94.33.60\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.74 Safari\\/537.36 Edg\\/99.0.1150.55,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-04-06T05:18:49.347990923Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.write\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8dQcMTHwxehFLsffoKBmyRKRikyGbUm252KGy3UjQQa_CVTzqp4Czayw\",\"request\":{\"sql\":\"UPDATE Concerts SET TicketPrices = [25, 50, 100] WHERE VenueId = 1;\",\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8dQcMTHwxehFLsffoKBmyRKRikyGbUm252KGy3UjQQa_CVTzqp4Czayw\"}},\"insertId\":\"8bxmqjejo40t\",\"resource\":{\"type\":\"spanner_instance\"},\"timestamp\":\"2022-04-06T05:18:49.347497042Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"253676221933612491\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-06T05:18:49.568984479Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> events = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, events.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterRequestMetadataNegTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.write\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8dQcMTHwxehFLsffoKBmyRKRikyGbUm252KGy3UjQQa_CVTzqp4Czayw\",\"request\":{\"sql\":\"UPDATE Concerts SET TicketPrices = [25, 50, 100] WHERE VenueId = 1;\",\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8dQcMTHwxehFLsffoKBmyRKRikyGbUm252KGy3UjQQa_CVTzqp4Czayw\"}},\"insertId\":\"8bxmqjejo40t\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"location\":\"us-central1\"}},\"timestamp\":\"2022-04-06T05:18:49.347497042Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"253676221933612491\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-06T05:18:49.568984479Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterInstanceAndProjectIdNegTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.43\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.64 Safari\\/537.36 Edg\\/101.0.1210.53,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-26T08:22:55.134992665Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ8vg7Vm3kk4tcbM2541S-7e2wGqwfu6tXg20TUTKsL9Gwdae1KW2ULmFw\",\"request\":{\"queryMode\":\"PROFILE\",\"sql\":\"SELECT a.SingerId FROM Albums AS a WHERE STARTS_WITH(a.AlbumTitle, \\\"T\\\") AND a.MarketingBudget <= 5000;\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ8vg7Vm3kk4tcbM2541S-7e2wGqwfu6tXg20TUTKsL9Gwdae1KW2ULmFw\"}},\"insertId\":\"4zyb0zd80dn\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"location\":\"asia-south1\"}},\"timestamp\":\"2022-05-26T08:22:55.129354923Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"10361143198256964072\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-26T08:22:56.043074091Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("-1050474491", record.getSessionId());
		assertEquals(":test", record.getDbName());
		assertEquals("select", sentence.getVerb());
		assertEquals("Albums", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
		}
	
	@Test
	public void filterSessionLocatorTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"111.11.11.111\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/98.0.4758.102 Safari\\/537.36 Edg\\/98.0.1108.62,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-03-07T04:17:51.801797793Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8SgFA0zcqVGRh4iari_Ve9m88l6J-w1F-Gk4AHOWy7JHBekHq_eloh2w\",\"request\":{\"queryMode\":\"PROFILE\",\"sql\":\"select Firstname,DOB From Users where id=2\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8SgFA0zcqVGRh4iari_Ve9m88l6J-w1F-Gk4AHOWy7JHBekHq_eloh2w\"}},\"insertId\":\"1snxb5rdfzg7\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"location\":\"us-central1\",\"project_id\":\"project-sccd\",\"instance_id\":\"spanner-test\"}},\"timestamp\":\"2022-03-07T04:17:51.801593049Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"7945483215916040071\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-03-07T04:17:53.543048234Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		SessionLocator actual = record.getSessionLocator();
		assertNotNull(record.getSessionLocator());
		assertEquals("0.0.0.0", actual.getServerIp());
		assertEquals(-1, actual.getServerPort());
		assertEquals("111.11.11.111", actual.getClientIp());
		assertEquals(-1, actual.getClientPort());
	}
	
	@Test
	public void filterIpv6Test() {
		final String spannerString = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:pankaj-g766@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"1111:a11:222:4444:b0:111:db0c:f111\",\"callerSuppliedUserAgent\":\"grpc-c++\\/1.45.0-dev grpc-c\\/22.0.0 (linux; chttp2),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-25T09:35:01.505018272Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x94O_jIlXA2ed_pbzYxdsI071emhpt0lrXzUqcnftd0BLjqnmR_wRX-3w\",\"request\":{\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x94O_jIlXA2ed_pbzYxdsI071emhpt0lrXzUqcnftd0BLjqnmR_wRX-3w\",\"sql\":\"SELECT t0.LastName FROM (select Info,LastName from Users) AS t0 GROUP BY t0.LastName LIMIT 100;\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.spanner.v1.ResultSet\"}},\"insertId\":\"14inpo5e34435\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"location\":\"asia-south1\",\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-02-25T09:35:01.497225106Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"receiveTimestamp\":\"2022-02-25T09:35:03.181856942Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(spannerString).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		SessionLocator actual = record.getSessionLocator();
		assertNotNull(record.getSessionLocator());
		assertEquals("0000:0000:0000:0000:0000:FFFF:0000:0000", actual.getServerIpv6());
		assertTrue(actual.isIpv6());
		assertEquals(-1, actual.getServerPort());
		assertEquals(-1, actual.getClientPort());
		assertEquals("1111:a11:222:4444:b0:111:db0c:f111", actual.getClientIpv6());
	}

	@Test
public void filterAccessorTest() {
	final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.12.209\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/98.0.4758.102 Safari\\/537.36 Edg\\/98.0.1108.62,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-03-07T04:17:51.801797793Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8SgFA0zcqVGRh4iari_Ve9m88l6J-w1F-Gk4AHOWy7JHBekHq_eloh2w\",\"request\":{\"queryMode\":\"PROFILE\",\"sql\":\"select Firstname,DOB From Users where id=2\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8SgFA0zcqVGRh4iari_Ve9m88l6J-w1F-Gk4AHOWy7JHBekHq_eloh2w\"}},\"insertId\":\"1snxb5rdfzg7\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"location\":\"us-central1\",\"project_id\":\"project-sccd\",\"instance_id\":\"spanner-test\"}},\"timestamp\":\"2022-03-07T04:17:51.801593049Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"7945483215916040071\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-03-07T04:17:53.543048234Z\"}";
	final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
	Record record = Parser.parseRecord(spannerJson);
	Accessor actual = record.getAccessor();
	assertNotNull(record.getAccessor());
	assertEquals("project-sccd:spanner-test:test", actual.getServiceName());
	assertEquals("project-sccd_spanner-test_spanner.googleapis.com", actual.getServerHostName());
	assertEquals("user@test.com", actual.getDbUser());
	assertEquals("SpannerDB", actual.getServerType());
	assertEquals("Spanner", actual.getDbProtocol());
}		

	@Test
	public void filterSqlParserTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.12.209\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/98.0.4758.102 Safari\\/537.36 Edg\\/98.0.1108.62,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-03-07T04:50:54.292832567Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x-sZNP9yGownes7w4LpVxd8RuobRkz5lFKTxwOSy2O_mZ4lz4c5Ms0bvA\",\"request\":{\"sql\":\"select * from Employee;\",\"queryMode\":\"PROFILE\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x-sZNP9yGownes7w4LpVxd8RuobRkz5lFKTxwOSy2O_mZ4lz4c5Ms0bvA\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\"}},\"insertId\":\"1snxb5rdfzrm\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"location\":\"us-central1\",\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-03-07T04:50:54.292645291Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"16835646549553829817\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-03-07T04:50:55.393220752Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("-1979925154", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("select", sentence.getVerb());
		assertEquals("Employee", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterSqlParserTest2() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.187.236\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/98.0.4758.102 Safari\\/537.36 Edg\\/98.0.1108.56,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-26T05:35:42.788279327Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8SJYc6IAbaap8lU2dpW4Oe9oFmdK7KZQCT9eVMIMXIGjwNsy3zoaY49Q\",\"request\":{\"queryMode\":\"PROFILE\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x8SJYc6IAbaap8lU2dpW4Oe9oFmdK7KZQCT9eVMIMXIGjwNsy3zoaY49Q\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"sql\":\"SELECt * from users where LastName=\\\"Gupta\\\"\"}},\"insertId\":\"1uvaxeyd10pp\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"location\":\"us-central1\",\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-02-26T05:35:42.778801583Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"3271617462799267193\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-02-26T05:35:44.996622082Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterCreateDbTest() {
		final String gcpString2 = "{\"insertId\":\"1xxqxsxcnww\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"first\":true,\"id\":\"projects\\/project-sccd\\/instances\\/spanner-plugin\\/databases\\/spanner-db\\/operations\\/_auto_op_9076614e440f9535\",\"last\":true,\"producer\":\"spanner.googleapis.com\"},\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"anandkumar.saini778@hcl.com\",\"principalSubject\":\"user:anandkumar.saini778@hcl.com\"},\"authorizationInfo\":[{\"granted\":true,\"permission\":\"spanner.databases.create\",\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-plugin\",\"resourceAttributes\":{\"name\":\"projects\\/project-sccd\\/instances\\/spanner-plugin\\/databases\\/spanner-db\",\"service\":\"spanner\",\"type\":\"spanner.databases\"}}],\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.CreateDatabase\",\"request\":{\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.CreateDatabaseRequest\",\"createStatement\":\"CREATE DATABASE `spanner-db`\",\"parent\":\"projects\\/project-sccd\\/instances\\/spanner-plugin\"},\"requestMetadata\":{\"callerIp\":\"148.64.5.69\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/93.0.4577.82 Safari\\/537.36,gzip(gfe)\",\"destinationAttributes\":{},\"requestAttributes\":{\"auth\":{},\"time\":\"2021-09-22T05:18:43.475279952Z\"}},\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-plugin\\/databases\\/spanner-db\",\"response\":{\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.Database\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-plugin\\/databases\\/spanner-db\"},\"serviceName\":\"spanner.googleapis.com\",\"status\":{}},\"receiveTimestamp\":\"2021-09-22T05:18:50.888616258Z\",\"resource\":{\"labels\":{\"instance_config\":\"\",\"instance_id\":\"spanner-plugin\",\"location\":\"asia-southeast1\",\"project_id\":\"project-sccd\"},\"type\":\"spanner_instance\"},\"severity\":\"NOTICE\",\"timestamp\":\"2021-09-22T05:18:43.470573031Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("51684807", record.getSessionId());
		assertEquals("project-sccd:spanner-plugin:spanner-db", record.getDbName());
		assertEquals("create database", sentence.getVerb());
		assertEquals("spanner-db", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterCreateTableParserTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:pankaj-g766@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.189.18\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.74 Safari\\/537.36 Edg\\/99.0.1150.55,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-04-06T05:04:40.500711551Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.UpdateDatabaseDdl\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.updateDdl\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"request\":{\"database\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"statements\":[\"CREATE TABLE Singers (\\r\\n  SingerId   INT64 NOT NULL,  FirstName  STRING(1024),\\r\\n  LastName   STRING(1024),\\r\\n  BirthDate  DATE,\\r\\n  LastUpdated TIMESTAMP \\r\\n) PRIMARY KEY(SingerId)\"],\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.longrunning.Operation\"}},\"insertId\":\"1u1ppuad9hfv\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"location\":\"us-central1\",\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-04-06T05:04:40.500451180Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"id\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/operations\\/_auto_op_f5cc4877dc787659\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-06T05:04:44.336426139Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("1479311085", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("create table", sentence.getVerb());
		assertEquals("Singers", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterCreateTable2Test() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:pankaj-g766@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.195\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.124 Safari\\/537.36 Edg\\/102.0.1245.44,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-23T05:55:47.407763840Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.UpdateDatabaseDdl\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\",\"permission\":\"spanner.databases.updateDdl\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\",\"request\":{\"database\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\",\"statements\":[\"CREATE TABLE Singers_Team (\\r\\n  SingerId   INT64 NOT NULL,  FirstName  STRING(1024),\\r\\n  LastName   STRING(1024),\\r\\n  BirthDate  DATE,\\r\\n  LastUpdated TIMESTAMP \\r\\n) PRIMARY KEY(SingerId)\"],\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.longrunning.Operation\"}},\"insertId\":\"au5jc4d6s4k\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_id\":\"spanner-dev\",\"instance_config\":\"\",\"location\":\"asia-south1\"}},\"timestamp\":\"2022-06-23T05:55:47.404640801Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"id\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\\/operations\\/_auto_op_9a261dd88d683be1\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-06-23T05:55:53.168781846Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterCreateTableMulitipleQueriesTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:pankaj-g766@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.18\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.64 Safari\\/537.36 Edg\\/101.0.1210.47,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-20T06:34:40.726294409Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.UpdateDatabaseDdl\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.updateDdl\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"request\":{\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest\",\"database\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"statements\":[\"CREATE TABLE Student2 (id INT64, first_name STRING(8), last_name STRING(9)) PRIMARY KEY(id)\",\"CREATE TABLE Course2 (id INT64, name STRING(16), teacher_id INT64) PRIMARY KEY(id)\",\"CREATE TABLE Student_Course2 (id INT64, student_id INT64,ccourse_id INT64) PRIMARY KEY(id)\"]},\"response\":{\"@type\":\"type.googleapis.com\\/google.longrunning.Operation\"}},\"insertId\":\"4q77ttd2tkm\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"location\":\"asia-south1\"}},\"timestamp\":\"2022-05-20T06:34:40.721072488Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"id\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/operations\\/_auto_op_f170d20887b0fab1\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-20T06:34:45.338515375Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("214548392", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("create table", sentence.getVerb());
		assertEquals("Student2", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterDropTableMulitipleQueriesTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:pankaj-g766@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.33\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.64 Safari\\/537.36 Edg\\/101.0.1210.47,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-21T05:54:41.851790255Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.UpdateDatabaseDdl\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.updateDdl\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"request\":{\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest\",\"statements\":[\"DROP TABLE Student3\",\"DROP TABLE Course3\",\"DROP TABLE Student_Course3\"],\"database\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.longrunning.Operation\"}},\"insertId\":\"1o7uzcod8220\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_id\":\"spanner-test\",\"instance_config\":\"\",\"location\":\"asia-south1\"}},\"timestamp\":\"2022-05-21T05:54:41.847332270Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"id\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/operations\\/_auto_op_347090d341a0617c\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-21T05:54:45.060197542Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("1936537039", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("drop table", sentence.getVerb());
		assertEquals("Student3", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterDropTableTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:pankaj-g766@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.39\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-06T08:46:06.985225527Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.UpdateDatabaseDdl\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.updateDdl\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"request\":{\"database\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest\",\"statements\":[\"DROP TABLE Songs2\"]},\"response\":{\"@type\":\"type.googleapis.com\\/google.longrunning.Operation\"}},\"insertId\":\"of21i9ddpjz\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"project_id\":\"project-sccd\",\"instance_id\":\"spanner-test\",\"location\":\"asia-south1\"}},\"timestamp\":\"2022-05-06T08:46:06.981176953Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"id\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/operations\\/_auto_op_083d1418844a9074\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-06T08:46:11.839441524Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("83340809", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("drop table", sentence.getVerb());
		assertEquals("Songs2", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterNestedDeleteSqlParserTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"180.94.33.60\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.74 Safari\\/537.36 Edg\\/99.0.1150.55,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-04-06T05:17:50.607150044Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.write\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x-eCIatSQLyyGLOvVLCesV99Btg_xgRcyQc1ikvElA9bZG4fWtnQ7eShw\",\"request\":{\"queryMode\":\"PROFILE\",\"sql\":\"DELETE FROM Singers\\r\\nWHERE FirstName NOT IN (SELECT FirstName from AckworthSingers);\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x-eCIatSQLyyGLOvVLCesV99Btg_xgRcyQc1ikvElA9bZG4fWtnQ7eShw\"}},\"insertId\":\"10cuvs7ednx1p\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\",\"location\":\"us-central1\"}},\"timestamp\":\"2022-04-06T05:17:50.598353614Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"12742530131834499424\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-06T05:17:51.634813306Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterInsertIntoSqlTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.27\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-04-25T08:05:04.535434281Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.write\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0bwu-qpw4qT8xi7yk-1zSH-fVfseUsjTDdy2NFzckzC-1cg_dAgIGYQ8w\",\"request\":{\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0bwu-qpw4qT8xi7yk-1zSH-fVfseUsjTDdy2NFzckzC-1cg_dAgIGYQ8w\",\"sql\":\"INSERT INTO Singers (SingerId, FirstName)\\r\\nVALUES (30, (SELECT FirstName FROM AckworthSingers WHERE SingerId = 30));\\r\",\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\"}},\"insertId\":\"1f28nsuf2l2h1p\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"instance_config\":\"\",\"location\":\"us-central1\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-04-25T08:05:04.526390442Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"12389987077963291915\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-25T08:05:05.315364492Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterArrayOffsetQueryTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.95\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-04-26T05:49:49.780115953Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0b07GRMs7SnmWT5D9topXYuUr0nXLcYjmvp26p_Emr-P-JWu4K9W4ZNNQ\",\"request\":{\"sql\":\"WITH locations AS       \\r\\n  (SELECT ARRAY<STRUCT<city STRING, state STRING>>[(\\\"Seattle\\\", \\\"Washington\\\"),\\r\\n    (\\\"Phoenix\\\", \\\"Arizona\\\")] AS location)\\r\\nSELECT l.LOCATION[offset(0)].*\\r\\nFROM locations l;\\r\",\"queryMode\":\"PROFILE\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0b07GRMs7SnmWT5D9topXYuUr0nXLcYjmvp26p_Emr-P-JWu4K9W4ZNNQ\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\"}},\"insertId\":\"hhmp6wf3ega34\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\",\"location\":\"us-central1\"}},\"timestamp\":\"2022-04-26T05:49:49.769468382Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"6999545450751889435\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-26T05:49:50.521242310Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterSqlWithoutObjTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.72\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-04-28T13:34:45.077353389Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0ZFAPOXwtKnclvjk1Swg66Tk1vgFrGihf-FeLu3hFiK70C0_wlg_ZxewQ\",\"request\":{\"sql\":\"Select AS VALUE STRUCT(1 AS x, 2, 3)\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0ZFAPOXwtKnclvjk1Swg66Tk1vgFrGihf-FeLu3hFiK70C0_wlg_ZxewQ\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"queryMode\":\"PROFILE\"}},\"insertId\":\"1yrdinvf4irdv7\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"location\":\"us-central1\",\"project_id\":\"project-sccd\",\"instance_id\":\"spanner-test\"}},\"timestamp\":\"2022-04-28T13:34:45.070698654Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"8047635221861181473\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-28T13:34:45.658815677Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterJoinSqlTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.43\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.64 Safari\\/537.36 Edg\\/101.0.1210.53,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-26T08:59:22.712893673Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ_VBi3iJps_wB62vfVcUecmWo42wksR6UwdBjpYyQD7WnmXdlKQRucDkA\",\"request\":{\"queryMode\":\"PROFILE\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ_VBi3iJps_wB62vfVcUecmWo42wksR6UwdBjpYyQD7WnmXdlKQRucDkA\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"sql\":\"SELECT s.FirstName, a.MarketingBudget\\nFROM Singers AS s JOIN Albums AS a ON s.SingerId = a.SingerId;\"}},\"insertId\":\"10mu3rld3j2z\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"location\":\"asia-south1\",\"instance_config\":\"\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-26T08:59:22.708364199Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"7805286887883402045\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-26T08:59:23.605988325Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("235796560", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("select", sentence.getVerb());
		assertEquals("Singers", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterInnerJoinSqlTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.18\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-04T07:46:30.260464604Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0ajRzXJkOqs6YmKFH_lQve72ysAKd9DPLNCZf3sLh61zOiH2f5NugyESQ\",\"request\":{\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0ajRzXJkOqs6YmKFH_lQve72ysAKd9DPLNCZf3sLh61zOiH2f5NugyESQ\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"sql\":\"SELECT Singers.FirstName,AckworthSingers.LastName\\nFROM Singers JOIN AckworthSingers ON Singers.SingerId = AckworthSingers.SingerId;\",\"queryMode\":\"PROFILE\"}},\"insertId\":\"1typdnsz0\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\",\"location\":\"asia-south1\"}},\"timestamp\":\"2022-05-04T07:46:30.255993168Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"16343214564427054759\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-04T07:46:30.668678045Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("214548392", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("select", sentence.getVerb());
		assertEquals("Singers", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterBlanlkSqlTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.95\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-04-26T05:49:49.780115953Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0b07GRMs7SnmWT5D9topXYuUr0nXLcYjmvp26p_Emr-P-JWu4K9W4ZNNQ\",\"request\":{\"sql\":\"\",\"queryMode\":\"PROFILE\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0b07GRMs7SnmWT5D9topXYuUr0nXLcYjmvp26p_Emr-P-JWu4K9W4ZNNQ\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\"}},\"insertId\":\"hhmp6wf3ega34\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\",\"location\":\"us-central1\"}},\"timestamp\":\"2022-04-26T05:49:49.769468382Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"6999545450751889435\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-26T05:49:50.521242310Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterAlterTableTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:pankaj-g766@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.182.37\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-11T05:17:18.770782310Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.UpdateDatabaseDdl\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.updateDdl\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"request\":{\"statements\":[\"ALTER TABLE Singers ADD column Email STRING(1024)\"],\"database\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.longrunning.Operation\"}},\"insertId\":\"1b67srne128md\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\",\"location\":\"asia-south1\",\"instance_config\":\"\"}},\"timestamp\":\"2022-05-11T05:17:18.765979068Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"id\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/operations\\/_auto_op_d2c83ca9d0c89699\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-11T05:17:24.055109071Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("-1809150263", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("alter table", sentence.getVerb());
		assertEquals("Singers", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterCreateIndexDDlTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:pankaj-g766@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.39\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-06T07:27:44.431316422Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.UpdateDatabaseDdl\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.updateDdl\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"request\":{\"statements\":[\"CREATE INDEX SingersByFirstLastName4 ON singers(FirstName, LastName)\"],\"database\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.longrunning.Operation\"}},\"insertId\":\"1rtjo6hda5jn\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"project_id\":\"project-sccd\",\"location\":\"asia-south1\",\"instance_id\":\"spanner-test\"}},\"timestamp\":\"2022-05-06T07:27:44.427428445Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"id\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/operations\\/_auto_op_59d518eec4e446db\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-06T07:27:48.124308859Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("83340809", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("create index", sentence.getVerb());
		assertEquals("SingersByFirstLastName4", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterIndexWithOptionalDDlTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:pankaj-g766@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.127\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.124 Safari\\/537.36 Edg\\/102.0.1245.44,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-23T09:55:03.065385347Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.UpdateDatabaseDdl\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\",\"permission\":\"spanner.databases.updateDdl\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\",\"request\":{\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest\",\"statements\":[\"CREATE UNIQUE  NULL_FILTERED INDEX SingersByFirstLastName2 ON singers(FirstName, LastName)\"],\"database\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.longrunning.Operation\"}},\"insertId\":\"xky932cgab\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-dev\",\"project_id\":\"project-sccd\",\"instance_config\":\"\",\"location\":\"asia-south1\"}},\"timestamp\":\"2022-06-23T09:55:03.062462909Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"id\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\\/operations\\/_auto_op_8f26e22fddd24b91\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-06-23T09:55:06.632922036Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("619635321", record.getSessionId());
		assertEquals("project-sccd:spanner-dev:first-db", record.getDbName());
		assertEquals("create    index", sentence.getVerb());
		assertEquals("SingersByFirstLastName2", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterCreateViewDDlTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:pankaj-g766@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.39\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-06T07:30:27.886132419Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.UpdateDatabaseDdl\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.updateDdl\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"request\":{\"database\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"statements\":[\"Create VIEW ByDiscountSale SQL SECURITY INVOKER AS Select O.customerNumber, O.orderNumber from QAOrders O where O.discounT=20\"],\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.longrunning.Operation\"}},\"insertId\":\"1rtjo6hda5kx\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"location\":\"asia-south1\",\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-06T07:30:27.883542030Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"id\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/operations\\/_auto_op_87e6488fec576fea\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-06T07:30:31.139264583Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("83340809", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("create view", sentence.getVerb());
		assertEquals("ByDiscountSale", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterCreateOrReplaceViewDDlTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:pankaj-g766@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.127\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.124 Safari\\/537.36 Edg\\/102.0.1245.44,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-23T11:53:07.175897019Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.UpdateDatabaseDdl\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\",\"permission\":\"spanner.databases.updateDdl\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\",\"request\":{\"database\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\",\"statements\":[\"CREATE OR REPLACE   view ByDiscount SQL SECURITY INVOKER AS Select O.customerNumber, O.orderNumber from QAOrders O where O.discounT=20\"],\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.longrunning.Operation\"}},\"insertId\":\"au5jc4d74cz\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"project_id\":\"project-sccd\",\"location\":\"asia-south1\",\"instance_id\":\"spanner-dev\"}},\"timestamp\":\"2022-06-23T11:53:07.173119353Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"id\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\\/operations\\/_auto_op_bd874c7f73dd6a31\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-06-23T11:53:12.171737967Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("619635321", record.getSessionId());
		assertEquals("project-sccd:spanner-dev:first-db", record.getDbName());
		assertEquals("create or replace   view", sentence.getVerb());
		assertEquals("ByDiscount", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterCollateQueriesTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.82\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-10T07:13:10.687396097Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0aEcFvL2eHfBo2Xb1x8L-VHynaYYxVJ440BJGefVAXFwwsBhKf5EHvoYw\",\"request\":{\"sql\":\"SELECT FirstName FROM Singers ORDER BY FirstName COLLATE \\\"en_CA\\\"\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0aEcFvL2eHfBo2Xb1x8L-VHynaYYxVJ440BJGefVAXFwwsBhKf5EHvoYw\",\"queryMode\":\"PROFILE\"}},\"insertId\":\"1vzd1fidlwiu\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"location\":\"asia-south1\",\"project_id\":\"project-sccd\",\"instance_id\":\"spanner-test\",\"instance_config\":\"\"}},\"timestamp\":\"2022-05-10T07:13:10.687149481Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"9563794440567017670\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-10T07:13:10.769721099Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("-1247504139", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("select", sentence.getVerb());
		assertEquals("Singers", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterComplexJoinTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.82\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-10T07:14:13.807389078Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0ZDyXOHK8anCp1ja7KAXeyhJ7iXj2YK54SCVMmtuGRRmFbRm4zuQJHArQ\",\"request\":{\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0ZDyXOHK8anCp1ja7KAXeyhJ7iXj2YK54SCVMmtuGRRmFbRm4zuQJHArQ\",\"queryMode\":\"PROFILE\",\"sql\":\"SELECT A.name, item, ARRAY_LENGTH(A.items) item_count_for_name\\nFROM\\nUNNEST(\\n[\\nSTRUCT(\\n'first' AS name,\\n[1, 2, 3, 4] AS items),\\nSTRUCT(\\n'second' AS name,\\n[] AS items)]) AS A\\nLEFT JOIN\\nA.items AS item;\"}},\"insertId\":\"1r9i4b4d9s1g\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"location\":\"asia-south1\",\"instance_id\":\"spanner-test\",\"instance_config\":\"\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-10T07:14:13.801723756Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"13871882488328723698\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-10T07:14:14.096583771Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterDropViewDDlTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\",\"principalSubject\":\"user:pankaj-g766@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.39\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-06T07:31:18.063748131Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.admin.database.v1.DatabaseAdmin.UpdateDatabaseDdl\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.updateDdl\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"request\":{\"statements\":[\"DROP VIEW ByDiscountSale\"],\"database\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"@type\":\"type.googleapis.com\\/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.longrunning.Operation\"}},\"insertId\":\"1rtjo6hda5l7\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"asia-south1\",\"instance_id\":\"spanner-test\",\"instance_config\":\"\"}},\"timestamp\":\"2022-05-06T07:31:18.059626861Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"operation\":{\"id\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/operations\\/_auto_op_f0022701e52b7ade\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-06T07:31:21.142940626Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("83340809", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("drop view", sentence.getVerb());
		assertEquals("ByDiscountSale", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterIntersectAllTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.82\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-10T06:01:26.610664705Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0bZVEdgvU--omqa6xJqgGYfE-AoA5SPE-WWo2lDFaqP7nhyYipBbKeCmw\",\"request\":{\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"sql\":\"SELECT LastName\\nFROM Singers\\nINTERSECT ALL\\nSELECT LastName\\nFROM AckworthSingers;\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0bZVEdgvU--omqa6xJqgGYfE-AoA5SPE-WWo2lDFaqP7nhyYipBbKeCmw\",\"queryMode\":\"PROFILE\"}},\"insertId\":\"1vzd1fidlv30\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"instance_config\":\"\",\"project_id\":\"project-sccd\",\"location\":\"asia-south1\"}},\"timestamp\":\"2022-05-10T06:01:26.610524859Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"11885810735845302677\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-10T06:01:27.515790449Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterTableSampleTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.82\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-10T07:11:03.695302378Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0Z7s9pt2thmUipqygXVXNeBscdpoPEctPim8HT_melLsI_Gn0XBXw8XJA\",\"request\":{\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0Z7s9pt2thmUipqygXVXNeBscdpoPEctPim8HT_melLsI_Gn0XBXw8XJA\",\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"sql\":\"SELECT SingerId FROM Singers TABLESAMPLE BERNOULLI (0.1 PERCENT)\"}},\"insertId\":\"1vzd1fidlwh4\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_config\":\"\",\"location\":\"asia-south1\",\"instance_id\":\"spanner-test\"}},\"timestamp\":\"2022-05-10T07:11:03.695081824Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"9581259756418334896\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-10T07:11:04.027739630Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("-1247504139", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("select", sentence.getVerb());
		assertEquals("Singers", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterExceptDistinctTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.82\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-10T06:02:45.140072455Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0aAY99QzenBar-NwLulwbc9BV_eCdJ9OrRQuJzSJfceYGH97tdFFF9GUg\",\"request\":{\"sql\":\"SELECT LastName\\nFROM Singers\\nEXCEPT DISTINCT\\nSELECT LastName\\nFROM AckworthSingers\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0aAY99QzenBar-NwLulwbc9BV_eCdJ9OrRQuJzSJfceYGH97tdFFF9GUg\",\"queryMode\":\"PROFILE\"}},\"insertId\":\"ci03zdd2qlo\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"location\":\"asia-south1\"}},\"timestamp\":\"2022-05-10T06:02:45.135716283Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"9189157196098373739\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-10T06:02:46.488530165Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterUnionAllTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.82\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-10T06:03:05.229887569Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0aiPL9U6L__jfIhdUlAyd6C_8Mm-J436ta9sxoe3TVLo0apsR1ZV3FTdg\",\"request\":{\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0aiPL9U6L__jfIhdUlAyd6C_8Mm-J436ta9sxoe3TVLo0apsR1ZV3FTdg\",\"sql\":\"SELECT LastName\\nFROM Singers\\nUNION   ALL\\nSELECT LastName\\nFROM AckworthSingers\"}},\"insertId\":\"1qfj7sre1monh\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"location\":\"asia-south1\"}},\"timestamp\":\"2022-05-10T06:03:05.225873245Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"14842267127113430954\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-10T06:03:05.714574079Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterResourceNegTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.73\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-04-26T12:07:16.637073526Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0Z7r44HCOUxMM5W8EgTtcowsBHIr9oDGPdPVkw1tjo1ONolsK3x2aWR9w\",\"request\":{\"sql\":\"select * from Employee where id=1\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"queryMode\":\"PROFILE\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0Z7r44HCOUxMM5W8EgTtcowsBHIr9oDGPdPVkw1tjo1ONolsK3x2aWR9w\"}},\"insertId\":\"ozfvxyf3agh5k\",\"timestamp\":\"2022-04-26T12:07:16.627683177Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"13557322266984096341\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-26T12:07:16.857627297Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, result.size());
	}

	@Test
	public void filterTimestampTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.204\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.54 Safari\\/537.36 Edg\\/101.0.1210.39,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-17T07:13:53.235480843Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0ZObapw5zlKdLGQlNk4bAyVxm_tPTBA46nSB84rtYOymypusB-DsDo2rw\",\"request\":{\"sql\":\"SELECT CURRENT_TIMESTAMP() as now\",\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0ZObapw5zlKdLGQlNk4bAyVxm_tPTBA46nSB84rtYOymypusB-DsDo2rw\"}},\"insertId\":\"dvvnd2bn1\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"location\":\"asia-south1\",\"instance_config\":\"\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-17T07:13:53.235237769Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"9444581525320325246\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-17T07:13:54.326077047Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterSelectCustomParserTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.73\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-04-26T12:02:34.853241708Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0ZRnChs6K4dbngNt3ICWHWlM7PEK_0zGralREXja09C-tbna2rqiPl9yQ\",\"request\":{\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AH-i_0ZRnChs6K4dbngNt3ICWHWlM7PEK_0zGralREXja09C-tbna2rqiPl9yQ\",\"sql\":\"SELECT SingerId FROM Singers MATERIALIZED BERNOULLI (0.1 PERCENT)\"}},\"insertId\":\"1lpovsaf2u0n15\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\",\"instance_config\":\"\",\"location\":\"us-central1\"}},\"timestamp\":\"2022-04-26T12:02:34.852924678Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"12631762209399029108\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-26T12:02:36.143153614Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("-571457581", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("select", sentence.getVerb());
		assertEquals("Singers", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}
	@Test
	public void filterSelectCustomParserTest2() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.188.229\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/98.0.4758.102 Safari\\/537.36 Edg\\/98.0.1108.62,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-03-06T10:03:43.817387774Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x_3xb7nwA0H7S6B8hLV3AzHNPVKl7PzemyrtB21HA3RoJOaQxccdeqoAw\",\"request\":{\"sql\":\"SELECT * FROM Singers AS s JOIN@{FORCE_JOIN_ORDER=TRUE} Albums AS a ON s.SingerId = a.Singerid WHERE s.LastName LIKE '%a' AND a.AlbumTitle LIKE 'G%'\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"queryMode\":\"PROFILE\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x_3xb7nwA0H7S6B8hLV3AzHNPVKl7PzemyrtB21HA3RoJOaQxccdeqoAw\"}},\"insertId\":\"1snxb5rdflup\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"location\":\"us-central1\",\"project_id\":\"project-sccd\",\"instance_id\":\"spanner-test\"}},\"timestamp\":\"2022-03-06T10:03:43.817102127Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"13710611471873755832\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-03-06T10:03:44.673334332Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterLikeOperatorTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.188.229\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/98.0.4758.102 Safari\\/537.36 Edg\\/98.0.1108.62,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-03-06T10:03:43.817387774Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x_3xb7nwA0H7S6B8hLV3AzHNPVKl7PzemyrtB21HA3RoJOaQxccdeqoAw\",\"request\":{\"sql\":\"select * from Employees where LastName 'LIKE A%'\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"queryMode\":\"PROFILE\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AN4G3x_3xb7nwA0H7S6B8hLV3AzHNPVKl7PzemyrtB21HA3RoJOaQxccdeqoAw\"}},\"insertId\":\"1snxb5rdflup\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"location\":\"us-central1\",\"project_id\":\"project-sccd\",\"instance_id\":\"spanner-test\"}},\"timestamp\":\"2022-03-06T10:03:43.817102127Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"13710611471873755832\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-03-06T10:03:44.673334332Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("1875025146", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("select", sentence.getVerb());
		assertEquals("Employees", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterInsertIntoTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.7.7\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.67 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-21T15:24:25.072010335Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test-spadb\",\"permission\":\"spanner.databases.write\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test-spadb\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test-spadb\\/sessions\\/AP1odZ_QexQUjvDoJWYsE851T-90BHjpEeDqmSNjgdA0QTpKTyUlxXg-SNiQCg\",\"request\":{\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"sql\":\"\\t   \\nINSERT INTO Songs (SingerId, AlbumId, TrackId, SongName, Duration, SongGenre)\\nVALUES (2, 1, 1, \\\"Let's Get Back Together\\\", 182, \\\"COUNTRY\\\"),\\n       (2, 1, 2, \\\"Starting Again\\\", 156, \\\"ROCK\\\"),\\n       (2, 1, 3, \\\"I Knew You Were Magic\\\", 294, \\\"BLUES\\\"),\\n       (2, 1, 4, \\\"42\\\", 185, \\\"CLASSICAL\\\"),\\n       (2, 1, 5, \\\"Blue\\\", 238, \\\"BLUES\\\"),\\n       (2, 1, 6, \\\"Nothing Is The Same\\\", 303, \\\"BLUES\\\"),\\n       (2, 1, 7, \\\"The Second Time\\\", 255, \\\"ROCK\\\"),\\n       (2, 3, 1, \\\"Fight Story\\\", 194, \\\"ROCK\\\"),\\n       (3, 1, 1, \\\"Not About The Guitar\\\", 278, \\\"BLUES\\\");\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test-spadb\\/sessions\\/AP1odZ_QexQUjvDoJWYsE851T-90BHjpEeDqmSNjgdA0QTpKTyUlxXg-SNiQCg\"}},\"insertId\":\"p9kem8d1jvy\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"location\":\"asia-south1\",\"instance_config\":\"\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-21T15:24:25.071777511Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"5206100289408760473\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-21T15:24:25.418575044Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterUnnestTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"117.216.191.151\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.67 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-24T08:36:09.712743635Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test_spanner\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test_spanner\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test_spanner\\/sessions\\/AP1odZ-7j5OlE33VATwhpBW5VyiA_phe1BJuZsz-8CPDwabV6IunlR4lzT2OEA\",\"request\":{\"sql\":\"SELECT * FROM UNNEST ([1, 2, 3])\",\"queryMode\":\"PROFILE\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test_spanner\\/sessions\\/AP1odZ-7j5OlE33VATwhpBW5VyiA_phe1BJuZsz-8CPDwabV6IunlR4lzT2OEA\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\"}},\"insertId\":\"1c084t9csul\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"instance_config\":\"\",\"location\":\"asia-south1\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-24T08:36:09.712544108Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"6978075329756489822\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-24T08:36:10.676926312Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

	@Test
	public void filterUnnestTest2() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"117.216.191.151\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.67 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-24T08:36:09.712743635Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test_spanner\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test_spanner\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test_spanner\\/sessions\\/AP1odZ-7j5OlE33VATwhpBW5VyiA_phe1BJuZsz-8CPDwabV6IunlR4lzT2OEA\",\"request\":{\"sql\":\"SELECT * FROM UNNEST ([1, 2, 3])\",\"queryMode\":\"PROFILE\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test_spanner\\/sessions\\/AP1odZ-7j5OlE33VATwhpBW5VyiA_phe1BJuZsz-8CPDwabV6IunlR4lzT2OEA\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\"}},\"insertId\":\"1c084t9csul\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"instance_config\":\"\",\"location\":\"asia-south1\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-24T08:36:09.712544108Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"6978075329756489822\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-24T08:36:10.676926312Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterUnnestTestNeg() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.7\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.64 Safari\\/537.36 Edg\\/101.0.1210.53,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-27T04:52:17.560725325Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ84iU4RFPW-xw5Jzge-PNHzZ3geaodFs8J4MPUxVMjTn-dQ5S3s1VwMvg\",\"request\":{\"sql\":\"SELECT ARRAY_AGG(DISTINCT x) AS array_agg FROM UNNEST([2, 1, -2, 3, -2, 1, 2]) AS x;\",\"queryMode\":\"PROFILE\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ84iU4RFPW-xw5Jzge-PNHzZ3geaodFs8J4MPUxVMjTn-dQ5S3s1VwMvg\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\"}},\"insertId\":\"10mu3rld3xx9\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"location\":\"asia-south1\",\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-27T04:52:17.560581046Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"9615730345181986298\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-27T04:52:18.212161956Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterUnnestTest2Neg() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.7\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.64 Safari\\/537.36 Edg\\/101.0.1210.53,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-27T04:54:05.871373724Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ9Xe3okitfV6P2Hyd9czr-UnqoK1PhETnz74NTUZyEb8KTmV-uV6HObFg\",\"request\":{\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ9Xe3okitfV6P2Hyd9czr-UnqoK1PhETnz74NTUZyEb8KTmV-uV6HObFg\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"queryMode\":\"PROFILE\",\"sql\":\"SELECT SUM(DISTINCT x) AS sum FROM UNNEST([1, 2, 3, 4, 5, 4, 3, 2, 1]) AS x;\"}},\"insertId\":\"10mu3rld3xxq\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"instance_config\":\"\",\"location\":\"asia-south1\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-27T04:54:05.866550897Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"3061561076673982531\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-27T04:54:06.221777470Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("1877432704", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("select", sentence.getVerb());
	}

	@Test
	public void filterJoinOrderTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.43\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.64 Safari\\/537.36 Edg\\/101.0.1210.53,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-26T10:08:10.144162364Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ-H3z0ZH6QfH0ejMJ2Nj-WMPZikwGeEckIOkWuAIa8bEg4XVbPUDTyvrA\",\"request\":{\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ-H3z0ZH6QfH0ejMJ2Nj-WMPZikwGeEckIOkWuAIa8bEg4XVbPUDTyvrA\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"sql\":\"SELECT *\\nFROM Singers AS s JOIN@{FORCE_JOIN_ORDER=TRUE} Albums AS a\\nON s.SingerId = a.Singerid\\nWHERE s.LastName LIKE '%a' AND a.AlbumTitle LIKE 'G%';\",\"queryMode\":\"PROFILE\"}},\"insertId\":\"y4nt9od21zm\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"location\":\"asia-south1\",\"project_id\":\"project-sccd\",\"instance_config\":\"\"}},\"timestamp\":\"2022-05-26T10:08:10.137901588Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"2738706054634786023\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-26T10:08:11.757949748Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("235796560", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("select", sentence.getVerb());
		assertEquals("Singers", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterJoinOrderTest2() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.43\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.64 Safari\\/537.36 Edg\\/101.0.1210.53,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-26T10:14:36.383561567Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ9cxUq5KRWT5MGroUsmnAmCbJXpu1LufpmacLrq5UR4mJ8UYMTgfXzwXQ\",\"request\":{\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ9cxUq5KRWT5MGroUsmnAmCbJXpu1LufpmacLrq5UR4mJ8UYMTgfXzwXQ\",\"queryMode\":\"PROFILE\",\"sql\":\"SELECT DISTINCT s.LastName\\nFROM Singers AS s JOIN@{FORCE_JOIN_ORDER=TRUE} Albums AS a\\nON s.SingerId = a.Singerid;\"}},\"insertId\":\"170qlond28wx\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"instance_id\":\"spanner-test\",\"location\":\"asia-south1\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-26T10:14:36.379124751Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"6579346296759115856\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-26T10:14:38.144304253Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}
	@Test
	public void filterSelectWithTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.43\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.64 Safari\\/537.36 Edg\\/101.0.1210.53,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-05-26T10:14:36.383561567Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ9cxUq5KRWT5MGroUsmnAmCbJXpu1LufpmacLrq5UR4mJ8UYMTgfXzwXQ\",\"request\":{\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ9cxUq5KRWT5MGroUsmnAmCbJXpu1LufpmacLrq5UR4mJ8UYMTgfXzwXQ\",\"queryMode\":\"PROFILE\",\"sql\":\"WITH orders AS ( SELECT STRUCT(STRUCT('Yonge Street' AS street, 'Canada' AS country) AS address) AS customer ) SELECT t.customer.address.country FROM orders AS t\"}},\"insertId\":\"170qlond28wx\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"location\":\"asia-south1\",\"project_id\":\"project-sccd\",\"instance_config\":\"\",\"instance_id\":\"spanner-test\"}},\"timestamp\":\"2022-05-26T10:14:36.379124751Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"6579346296759115856\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-05-26T10:14:38.144304253Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		}

	@Test
	public void filterinsertWithSingleLineCommentTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.7\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.124 Safari\\/537.36 Edg\\/102.0.1245.44,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-24T08:26:01.696462967Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\",\"permission\":\"spanner.databases.write\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\\/sessions\\/AP1odZ8VuVS1fmUb_yz9SsDuAEBB73d2JTTdaUTooQhS5jiBRv1aSUeEnoJPRA\",\"request\":{\"sql\":\"INSERT INTO \\nSingers (SingerId, FirstName,\\nLastName)\\nVALUES\\n(1000,\\n'Pankaj', ------- type: INT64\\n'Gupta' -------- type: INT64\\n)\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-dev\\/databases\\/first-db\\/sessions\\/AP1odZ8VuVS1fmUb_yz9SsDuAEBB73d2JTTdaUTooQhS5jiBRv1aSUeEnoJPRA\",\"queryMode\":\"PROFILE\"}},\"insertId\":\"1qpwz6gf2nqn3p\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_config\":\"\",\"location\":\"asia-south1\",\"instance_id\":\"spanner-dev\"}},\"timestamp\":\"2022-06-24T08:26:01.691345574Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"3470808783754897035\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-06-24T08:26:02.762027351Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterSqlWithSingleLineCommentTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.5.12\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.63 Safari\\/537.36 Edg\\/102.0.1245.33,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-13T05:43:37.083408134Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ_2kgWCr9w5pG7plvvT1dqSSoNhl8qRmOANgvjlyVR2AhuAegCTVNdAOQ\",\"request\":{\"sql\":\"select FirstName, ----------it will display first name.\\nLastName from singers\\n\",\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ_2kgWCr9w5pG7plvvT1dqSSoNhl8qRmOANgvjlyVR2AhuAegCTVNdAOQ\"}},\"insertId\":\"1lzp1jmf15241v\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"location\":\"asia-south1\",\"project_id\":\"project-sccd\",\"instance_config\":\"\"}},\"timestamp\":\"2022-06-13T05:43:37.083245617Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"7720251317467415618\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-06-13T05:43:37.152844729Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}

	@Test
	public void filterSqlWithMultiLineCommentTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.5.12\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.63 Safari\\/537.36 Edg\\/102.0.1245.33,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-13T05:43:37.083408134Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.select\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ_2kgWCr9w5pG7plvvT1dqSSoNhl8qRmOANgvjlyVR2AhuAegCTVNdAOQ\",\"request\":{\"sql\":\"select FirstName, ----------it will display first name.\\nLastName from singers\\n\",\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ_2kgWCr9w5pG7plvvT1dqSSoNhl8qRmOANgvjlyVR2AhuAegCTVNdAOQ\"}},\"insertId\":\"1lzp1jmf15241v\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"location\":\"asia-south1\",\"project_id\":\"project-sccd\",\"instance_config\":\"\"}},\"timestamp\":\"2022-06-13T05:43:37.083245617Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"7720251317467415618\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-06-13T05:43:37.152844729Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		assertEquals("-2082082286", record.getSessionId());
		assertEquals("project-sccd:spanner-test:test", record.getDbName());
		assertEquals("select", sentence.getVerb());
		assertEquals("singers", sentence.getObjects().get(0).name);
		assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void filterInsertWithoutIntoSqlTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.26\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.63 Safari\\/537.36 Edg\\/102.0.1245.39,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-14T06:10:52.283166139Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.write\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ8bkGH-2-NoRRZNomAzSKjH98v2D3US7zoY-gHRcKXJkZ3QaJe93Y0KfA\",\"request\":{\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"queryMode\":\"PROFILE\",\"sql\":\"INSERT Singers (SingerId, FirstName, LastName) values(111,\\\"Pankaj\\\",\\\"Gupta\\\")\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ8bkGH-2-NoRRZNomAzSKjH98v2D3US7zoY-gHRcKXJkZ3QaJe93Y0KfA\"}},\"insertId\":\"1qpoobaf4b9swc\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"project_id\":\"project-sccd\",\"instance_config\":\"\",\"location\":\"asia-south1\"}},\"timestamp\":\"2022-06-14T06:10:52.279695230Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1055527040076551020\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-06-14T06:10:52.376165035Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		}

	@Test
	public void filterDeleteWithoutFromTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.34\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.63 Safari\\/537.36 Edg\\/102.0.1245.39,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-14T04:44:26.949047537Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteStreamingSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"permission\":\"spanner.databases.write\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ8MLZiUefN3s3Q9WrNCdplLbype2XGuBAXUYM1uVt0sfdZa9JqHhKdLiw\",\"request\":{\"sql\":\"DELETE Singers WHERE FirstName = 'Pankaj'\",\"queryMode\":\"PROFILE\",\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-test\\/databases\\/test\\/sessions\\/AP1odZ8MLZiUefN3s3Q9WrNCdplLbype2XGuBAXUYM1uVt0sfdZa9JqHhKdLiw\"}},\"insertId\":\"1qpoobaf43cn14\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_id\":\"spanner-test\",\"instance_config\":\"\",\"location\":\"asia-south1\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-06-14T04:44:26.948876631Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"8475578497266804882\",\"producer\":\"spanner.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-06-14T04:44:27.021597707Z\"}";
		SpannerDBGuardiumFilter filter = getSpannerFilterConnector(gcpString2);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		List<co.elastic.logstash.api.Event> events = new ArrayList<>();
		events.add(e);
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener);
		assertEquals(1, matchListener.getMatchCount());
		assertEquals(1, result.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}
	
	@Test
		public void filterAutomationInsertTest() {
			final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"automation-spanner@project-sccd.iam.gserviceaccount.com\",\"serviceAccountKeyName\":\"\\/\\/iam.googleapis.com\\/projects\\/project-sccd\\/serviceAccounts\\/automation-spanner@project-sccd.iam.gserviceaccount.com\\/keys\\/14c38b258dc0bca0a5bb16ea5cfaba33c2186e94\",\"principalSubject\":\"serviceAccount:automation-spanner@project-sccd.iam.gserviceaccount.com\"},\"requestMetadata\":{\"callerIp\":\"111.222.333.444\",\"callerSuppliedUserAgent\":\"spanner-java\\/2.0.0 grpc-java-netty\\/1.44.1,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-14T14:17:23.622208179Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"spanner.googleapis.com\",\"methodName\":\"google.spanner.v1.Spanner.ExecuteSql\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-divya\\/databases\\/spanner-auto5\",\"permission\":\"spanner.databases.beginOrRollbackReadWriteTransaction\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-divya\\/databases\\/spanner-auto5\",\"type\":\"spanner.databases\"}},{\"resource\":\"projects\\/project-sccd\\/instances\\/spanner-divya\\/databases\\/spanner-auto5\",\"permission\":\"spanner.databases.write\",\"granted\":true,\"resourceAttributes\":{\"service\":\"spanner\",\"name\":\"projects\\/project-sccd\\/instances\\/spanner-divya\\/databases\\/spanner-auto5\",\"type\":\"spanner.databases\"}}],\"resourceName\":\"projects\\/project-sccd\\/instances\\/spanner-divya\\/databases\\/spanner-auto5\\/sessions\\/AP1odZ_CFFGhflVVEDixe447b4y4AoTvh3exiNPS2DYpf7mNLFHh8tZEf4amIQ\",\"request\":{\"requestOptions\":{},\"sql\":\"insert into `ACCOUNT_MASTER` (`AccountName`, `groupID`, `account_id`) values (@p1, @p2, @p3)\",\"queryOptions\":{},\"@type\":\"type.googleapis.com\\/google.spanner.v1.ExecuteSqlRequest\",\"session\":\"projects\\/project-sccd\\/instances\\/spanner-divya\\/databases\\/spanner-auto5\\/sessions\\/AP1odZ_CFFGhflVVEDixe447b4y4AoTvh3exiNPS2DYpf7mNLFHh8tZEf4amIQ\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.spanner.v1.ResultSet\"}},\"insertId\":\"1b6c67md3n1u\",\"resource\":{\"type\":\"spanner_instance\",\"labels\":{\"instance_config\":\"\",\"project_id\":\"project-sccd\",\"location\":\"asia-south1\",\"instance_id\":\"spanner-divya\"}},\"timestamp\":\"2022-06-14T14:17:23.617454797Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"receiveTimestamp\":\"2022-06-14T14:17:24.534688004Z\"}";
			final JsonObject spannerJson = JsonParser.parseString(gcpString2).getAsJsonObject();
			Record record = Parser.parseRecord(spannerJson);
			SessionLocator actual = record.getSessionLocator();
			assertEquals("0.0.0.0", actual.getServerIp());
			assertEquals(-1, actual.getServerPort());
			assertEquals("111.222.333.444", actual.getClientIp());
			assertEquals(-1, actual.getClientPort());
			assertEquals("64276589", record.getSessionId());
			assertEquals("project-sccd:spanner-divya:spanner-auto5", record.getDbName());
		}
		
	
	private SpannerDBGuardiumFilter getSpannerFilterConnector(String jsonString) {
		Configuration config = new ConfigurationImpl(Collections.singletonMap("source", jsonString));
		Context context = new ContextImpl(null, null);
		SpannerDBGuardiumFilter filter = new SpannerDBGuardiumFilter("spn-id", config, context);
		return filter;
	
	}	
}

class TestMatchListener implements FilterMatchListener {
	private AtomicInteger matchCount = new AtomicInteger(0);

	public int getMatchCount() {
		return matchCount.get(); 
	}

	@Override
	public void filterMatched(co.elastic.logstash.api.Event arg0) {
		matchCount.incrementAndGet();

	}
}
