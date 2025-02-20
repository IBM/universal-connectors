/*
Copyright IBM Corp. 2021, 2025 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.bigquery;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.PluginConfigSpec;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import co.elastic.logstash.api.Configuration;
public class BigQueryGuardiumFilterTest {

	@Test
	public void configSchemaTest() {
		final String gcpString2 = "";
		BigQueryGuardiumFilter filter = getBigQueryFilterConnector(gcpString2);
		Collection<PluginConfigSpec<?>> events = filter.configSchema();
		assertNotNull(events);
	}

	@Test
	public void commonUtilsTest() {
		CommonUtils commonUtils = new CommonUtils();
		assertNotNull(commonUtils);
	}

	@Test
	public void commonUtilsCatchTest() {
		Boolean status = CommonUtils.isJSONValid("test");
		assertNotNull(status);

	}

	@Test
	public void getIdTest() {
		final String gcpString2 = "";
		BigQueryGuardiumFilter filter = getBigQueryFilterConnector(gcpString2);
		String id = filter.getId();
		assertEquals("spn-id", id);
	}

	@Test
	public void filterHTTPPrototypeTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.86\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.84 Safari\\/537.36,gzip(gfe),gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_787f3164_1804a2c3db5\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_787f3164_1804a2c3db5\",\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"query\":\"create table  `project-sccd.BigQueryQADS.testOne`  (\\r\\n  SingerId   INT64 NOT NULL,\\r\\n  FirstName  STRING(1024),\\r\\n  LastName   STRING(1024),\\r\\n  BirthDate  DATE,\\r\\n);\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/BigQueryQADS\\/tables\\/testOne\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\",\"priority\":\"QUERY_INTERACTIVE\",\"statementType\":\"CREATE_TABLE\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-04-21T03:32:06.081Z\",\"startTime\":\"2022-04-21T03:32:06.185Z\",\"endTime\":\"2022-04-21T03:32:06.363Z\",\"queryStats\":{}}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-wq3ig5dhf0x\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"location\":\"US\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-04-21T03:32:06.371066Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1650511926081-project-sccd:bquxjob_787f3164_1804a2c3db5\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"httpRequest\":{\"serverIp\":\"168.149.184.86\"},\"receiveTimestamp\":\"2022-04-21T03:32:06.413809027Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void filterCreateSchemaTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.7.55\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/94.0.4606.81 Safari\\/537.36 Edg\\/94.0.992.50,gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.DatasetService.InsertDataset\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.datasets.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/datasets\\/babynames\",\"metadata\":{\"datasetCreation\":{\"reason\":\"CREATE\",\"dataset\":{\"createTime\":\"2021-10-22T08:23:25.239Z\",\"acl\":{\"policy\":{\"bindings\":[{\"role\":\"roles\\/bigquery.dataEditor\",\"members\":[\"projectEditor:project-sccd\"]},{\"role\":\"roles\\/bigquery.dataOwner\",\"members\":[\"projectOwner:project-sccd\",\"user:user@test.com\"]},{\"members\":[\"projectViewer:project-sccd\"],\"role\":\"roles\\/bigquery.dataViewer\"}]}},\"datasetName\":\"projects\\/project-sccd\\/datasets\\/babynames\",\"updateTime\":\"2021-10-22T08:23:25.239Z\"}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"vmzwgld2yry\",\"resource\":{\"type\":\"bigquery_dataset\",\"labels\":{\"project_id\":\"project-sccd\",\"dataset_id\":\"babynames\"}},\"timestamp\":\"2021-10-22T08:23:25.306328Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2021-10-22T08:23:25.690839506Z\"}";
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("create schema babynames", sqlquery.getData().getConstruct().fullSql);
		assertEquals("project-sccd:babynames", sqlquery.getDbName());
		assertEquals("create schema", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("babynames", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
	}

	@Test
	public void filterOuterProtoPayloadDataTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.7.55\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/94.0.4606.81 Safari\\/537.36 Edg\\/94.0.992.50,gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.TableService.InsertTable\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/datasets\\/babynames\",\"permission\":\"bigquery.tables.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/datasets\\/babynames\\/tables\\/person\",\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\",\"tableCreation\":{\"table\":{\"createTime\":\"2021-10-22T08:27:29.578Z\",\"updateTime\":\"2021-10-22T08:27:29.622Z\",\"schemaJson\":\"{\\n  \\\"fields\\\": [{\\n    \\\"name\\\": \\\"id\\\",\\n    \\\"type\\\": \\\"BIGNUMERIC\\\",\\n    \\\"mode\\\": \\\"REQUIRED\\\"\\n  }, {\\n    \\\"name\\\": \\\"first_name\\\",\\n    \\\"type\\\": \\\"STRING\\\",\\n    \\\"mode\\\": \\\"NULLABLE\\\"\\n  }, {\\n    \\\"name\\\": \\\"last_name\\\",\\n    \\\"type\\\": \\\"STRING\\\",\\n    \\\"mode\\\": \\\"NULLABLE\\\"\\n  }, {\\n    \\\"name\\\": \\\"age\\\",\\n    \\\"type\\\": \\\"INTEGER\\\",\\n    \\\"mode\\\": \\\"NULLABLE\\\"\\n  }]\\n}\",\"tableName\":\"projects\\/project-sccd\\/datasets\\/babynames\\/tables\\/person\"},\"reason\":\"TABLE_INSERT_REQUEST\"}}},\"insertId\":\"yp5msqd3d7i\",\"resource\":{\"type\":\"bigquery_dataset\",\"labels\":{\"project_id\":\"project-sccd\",\"dataset_id\":\"babynames\"}},\"timestamp\":\"2021-10-22T08:27:29.659395Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2021-10-22T08:27:30.126085723Z\"}";
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("create table babynames.person", sqlquery.getData().getConstruct().fullSql);
		assertEquals("project-sccd:babynames", sqlquery.getDbName());
		assertEquals("user@test.com", sqlquery.getAppUserName());
		assertEquals("create table", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("person", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
	}

	@Test
	public void filterProtoPayloadDbNameTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{},\"requestMetadata\":{\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/97.0.4692.71 Safari\\/537.36 Edg\\/97.0.1072.62,gzip(gfe),gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_8e8a6ad_17e901567d3\",\"metadata\":{\"jobInsertion\":{\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_8e8a6ad_17e901567d3\",\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"query\":\"alter table `project-sccd.BigQueryQADS.userdetail` ADD COLUMN phone_num varchar(50);\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/BigQueryQADS\\/tables\\/userdetail\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\",\"priority\":\"QUERY_INTERACTIVE\",\"statementType\":\"ALTER_TABLE\"}},\"jobStatus\":{\"jobState\":\"RUNNING\"},\"jobStats\":{\"createTime\":\"2022-01-25T07:13:49.369Z\",\"startTime\":\"2022-01-25T07:13:49.472Z\",\"queryStats\":{}}},\"reason\":\"JOB_INSERT_REQUEST\"},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"kb670e1rdar\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-01-25T07:13:49.519016Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1643094829369-project-sccd:bquxjob_8e8a6ad_17e901567d3\",\"producer\":\"bigquery.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-01-25T07:13:50.116494531Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:BigQueryQADS", sqlquery.getDbName());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void filterProtoPayloadEmptyMetadataTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.7.55\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/94.0.4606.81 Safari\\/537.36 Edg\\/94.0.992.50,gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.TableService.InsertTable\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/datasets\\/babynames\",\"permission\":\"bigquery.tables.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/datasets\\/babynames\\/tables\\/person\",\"metadata\":{}},\"insertId\":\"yp5msqd3d7i\",\"resource\":{\"type\":\"bigquery_dataset\",\"labels\":{\"project_id\":\"project-sccd\",\"dataset_id\":\"babynames\"}},\"timestamp\":\"2021-10-22T08:27:29.659395Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2021-10-22T08:27:30.126085723Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:", sqlquery.getDbName());
		assertEquals("user@test.com", sqlquery.getAppUserName());
		assertEquals("", sqlquery.getData().getConstruct().fullSql);
		assertEquals("", sqlquery.getData().getConstruct().redactedSensitiveDataSql);
		assertEquals("select", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void filterFieldsForTableCreationTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.166.30\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/94.0.4606.81 Safari\\/537.36,gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.TableService.InsertTable\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/datasets\\/big_Query_Data_Set\",\"permission\":\"bigquery.tables.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/datasets\\/big_Query_Data_Set\\/tables\\/person\",\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\",\"tableCreation\":{\"table\":{\"schemaJson\":\"{\\n  \\\"fields\\\": [{\\n    \\\"name\\\": \\\"id\\\",\\n    \\\"type\\\": \\\"STRING\\\",\\n    \\\"mode\\\": \\\"NULLABLE\\\"\\n  }, {\\n    \\\"name\\\": \\\"first_name\\\",\\n    \\\"type\\\": \\\"STRING\\\",\\n    \\\"mode\\\": \\\"NULLABLE\\\"\\n  }, {\\n    \\\"name\\\": \\\"last_name\\\",\\n    \\\"type\\\": \\\"STRING\\\",\\n    \\\"mode\\\": \\\"NULLABLE\\\"\\n  }, {\\n    \\\"name\\\": \\\"email\\\",\\n    \\\"type\\\": \\\"STRING\\\",\\n    \\\"mode\\\": \\\"NULLABLE\\\"\\n  }]\\n}\",\"updateTime\":\"2021-11-02T11:40:57.440Z\",\"createTime\":\"2021-11-02T11:40:57.327Z\",\"tableName\":\"projects\\/project-sccd\\/datasets\\/big_Query_Data_Set\\/tables\\/person\"},\"reason\":\"TABLE_INSERT_REQUEST\"}}},\"insertId\":\"-iayvihdbijr\",\"resource\":{\"type\":\"bigquery_dataset\",\"labels\":{\"project_id\":\"project-sccd\",\"dataset_id\":\"big_Query_Data_Set\"}},\"timestamp\":\"2021-11-02T11:40:57.468544Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2021-11-02T11:40:58.012452163Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:big_Query_Data_Set", sqlquery.getDbName());
		assertEquals("144348902", sqlquery.getSessionId());
		assertEquals("create table", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("person", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void parseJobTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.166.30\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/94.0.4606.81 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_109c8442_17ce074b669\",\"metadata\":{\"jobInsertion\":{\"reason\":\"JOB_INSERT_REQUEST\",\"job\":{\"jobStats\":{\"createTime\":\"2021-11-02T11:42:21.929Z\",\"startTime\":\"2021-11-02T11:42:22.027Z\",\"queryStats\":{}},\"jobStatus\":{\"jobState\":\"RUNNING\"},\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_109c8442_17ce074b669\",\"jobConfig\":{\"queryConfig\":{\"statementType\":\"INSERT\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"priority\":\"QUERY_INTERACTIVE\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/big_Query_Data_Set\\/tables\\/person\",\"writeDisposition\":\"WRITE_EMPTY\",\"query\":\"insert into project-sccd.big_Query_Data_Set.person (id, first_name, last_name, email)\\r\\nVALUES('3','Anand', 'Kumar', 'user@test.com'),\\r\\n      ('4','John', 'Cena', 'user@test.com'),\\r\\n      ('5','Brock', 'Lacner', 'user@test.com'),\\r\\n      ('6','Rock', '', 'user@test.com')\"},\"type\":\"QUERY\"}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-eyzoa2e23bel\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2021-11-02T11:42:22.065732Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1635853341929-project-sccd:bquxjob_109c8442_17ce074b669\",\"producer\":\"bigquery.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2021-11-02T11:42:22.783211184Z\"}";
		BigQueryGuardiumFilter filter = getBigQueryFilterConnector(gcpString2);
		Event e = new org.logstash.Event(); 
		TestMatchListener matchListener = new TestMatchListener(); 
		List<co.elastic.logstash.api.Event> events = new ArrayList<>(); 
		events.add(e); 
		e.setField("message", gcpString2); 
		Collection<co.elastic.logstash.api.Event> result = filter.filter(events, matchListener); 
		assertEquals(1, matchListener.getMatchCount()); 
		assertEquals(1, result.size());
	}

	@Test
	public void parseNullResourceJsonForMetaDataTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"112.23.00.11\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/94.0.4606.81 Safari\\/537.36,gzip(gfe),gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/datasets\\/big_Query_Data_Set\\/tables\\/person\",\"permission\":\"bigquery.tables.getData\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/datasets\\/big_Query_Data_Set\\/tables\\/person\",\"metadata\":{\"tableDataRead\":{\"reason\":\"JOB\",\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_77ace2e_17ce89f4992\"},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-aj7t5te11j51\",\"resource\":{},\"timestamp\":\"2021-11-04T01:45:52.859567Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"receiveTimestamp\":\"2021-11-04T01:45:52.924677259Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals(null, sqlquery.getData().getOriginalSqlCommand());
		assertEquals("112.23.00.11", sqlquery.getSessionLocator().getClientIp());
		assertEquals(-1, sqlquery.getSessionLocator().getClientPort());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void eventObjectAndVerbTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"136.185.149.223\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.51 Safari\\/537.36 Edg\\/99.0.1150.39,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_2efa9cd5_17fcfe7b26f\",\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\",\"jobChange\":{\"job\":{\"jobStatus\":{\"jobState\":\"DONE\"},\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_2efa9cd5_17fcfe7b26f\",\"jobConfig\":{\"queryConfig\":{\"statementType\":\"SELECT\",\"priority\":\"QUERY_INTERACTIVE\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/_1b12d4043516e7fe556c67c37894adb7c0c3588b\\/tables\\/anon0d36a80a5698fbaec071061444ad0e4a4d2d2e24\",\"writeDisposition\":\"WRITE_TRUNCATE\",\"query\":\"select * from project-sccd.Newdatademo.demoibm LIMIT 1000;\"},\"type\":\"QUERY\"},\"jobStats\":{\"queryStats\":{\"referencedTables\":[\"projects\\/project-sccd\\/datasets\\/Newdatademo\\/tables\\/demoibm\"],\"billingTier\":1,\"totalProcessedBytes\":\"28\",\"outputRowCount\":\"2\",\"totalBilledBytes\":\"10485760\"},\"endTime\":\"2022-03-28T09:42:23.627Z\",\"createTime\":\"2022-03-28T09:42:23.237Z\",\"startTime\":\"2022-03-28T09:42:23.380Z\",\"totalSlotMs\":\"56\"}},\"after\":\"DONE\"}}},\"insertId\":\"-uso33ze1h99r\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-03-28T09:42:23.630852Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1648460543237-project-sccd:bquxjob_2efa9cd5_17fcfe7b26f\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-03-28T09:42:23.997320883Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:Newdatademo", sqlquery.getDbName());
		assertEquals("user@test.com", sqlquery.getAppUserName());
		assertEquals("select", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("demoibm", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testSpecialChars() {
		final String gcpString2 = "{\\\"protoPayload\\\":{\\\"@type\\\":\\\"type.googleapis.com\\\\/google.cloud.audit.AuditLog\\\",\\\"status\\\":{},\\\"authenticationInfo\\\":{\\\"principalEmail\\\":\\\"user@test.com\\\"},\\\"requestMetadata\\\":{\\\"callerIp\\\":\\\"136.185.149.223\\\",\\\"callerSuppliedUserAgent\\\":\\\"Mozilla\\\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\\\/537.36 (KHTML, like Gecko) Chrome\\\\/99.0.4844.51 Safari\\\\/537.36 Edg\\\\/99.0.1150.39,gzip(gfe),gzip(gfe)\\\",\\\"requestAttributes\\\":{},\\\"destinationAttributes\\\":{}},\\\"serviceName\\\":\\\"bigquery.googleapis.com\\\",\\\"methodName\\\":\\\"google.cloud.bigquery.v2.JobService.InsertJob\\\",\\\"authorizationInfo\\\":[{\\\"resource\\\":\\\"projects\\\\/project-sccd\\\",\\\"permission\\\":\\\"bigquery.jobs.create\\\",\\\"granted\\\":true,\\\"resourceAttributes\\\":{}}],\\\"resourceName\\\":\\\"projects\\\\/project-sccd\\\\/jobs\\\\/bquxjob_2efa9cd5_17fcfe7b26f\\\",\\\"metadata\\\":{\\\"@type\\\":\\\"type.googleapis.com\\\\/google.cloud.audit.BigQueryAuditMetadata\\\",\\\"jobChange\\\":{\\\"job\\\":{\\\"jobStatus\\\":{\\\"jobState\\\":\\\"DONE\\\"},\\\"jobName\\\":\\\"projects\\\\/project-sccd\\\\/jobs\\\\/bquxjob_2efa9cd5_17fcfe7b26f\\\",\\\"jobConfig\\\":{\\\"queryConfig\\\":{\\\"statementType\\\":\\\"SELECT\\\",\\\"priority\\\":\\\"QUERY_INTERACTIVE\\\",\\\"createDisposition\\\":\\\"CREATE_IF_NEEDED\\\",\\\"destinationTable\\\":\\\"projects\\\\/project-sccd\\\\/datasets\\\\/_1b12d4043516e7fe556c67c37894adb7c0c3588b\\\\/tables\\\\/anon0d36a80a5698fbaec071061444ad0e4a4d2d2e24\\\",\\\"writeDisposition\\\":\\\"WRITE_TRUNCATE\\\",\\\"query\\\":\\\"WITH example AS   (SELECT 'абвгд' AS characters) SELECT   characters,   CHAR_LENGTH(characters) AS  char_length_example FROM example;\\\"},\\\"type\\\":\\\"QUERY\\\"},\\\"jobStats\\\":{\\\"queryStats\\\":{\\\"referencedTables\\\":[\\\"projects\\\\/project-sccd\\\\/datasets\\\\/Newdatademo\\\\/tables\\\\/demoibm\\\"],\\\"billingTier\\\":1,\\\"totalProcessedBytes\\\":\\\"28\\\",\\\"outputRowCount\\\":\\\"2\\\",\\\"totalBilledBytes\\\":\\\"10485760\\\"},\\\"endTime\\\":\\\"2022-03-28T09:42:23.627Z\\\",\\\"createTime\\\":\\\"2022-03-28T09:42:23.237Z\\\",\\\"startTime\\\":\\\"2022-03-28T09:42:23.380Z\\\",\\\"totalSlotMs\\\":\\\"56\\\"}},\\\"after\\\":\\\"DONE\\\"}}},\\\"insertId\\\":\\\"-uso33ze1h99r\\\",\\\"resource\\\":{\\\"type\\\":\\\"bigquery_project\\\",\\\"labels\\\":{\\\"project_id\\\":\\\"project-sccd\\\",\\\"location\\\":\\\"US\\\"}},\\\"timestamp\\\":\\\"2022-03-28T09:42:23.630852Z\\\",\\\"severity\\\":\\\"INFO\\\",\\\"logName\\\":\\\"projects\\\\/project-sccd\\\\/logs\\\\/cloudaudit.googleapis.com%2Fdata_access\\\",\\\"operation\\\":{\\\"id\\\":\\\"1648460543237-project-sccd:bquxjob_2efa9cd5_17fcfe7b26f\\\",\\\"producer\\\":\\\"bigquery.googleapis.com\\\",\\\"last\\\":true},\\\"receiveTimestamp\\\":\\\"2022-03-28T09:42:23.997320883Z\\\"}";
		BigQueryGuardiumFilter filter = getBigQueryFilterConnector(gcpString2);
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
	public void eventObjectAndVerb1Test() {
		final String gcpString2 = "{\\\"protoPayload\\\":{\\\"@type\\\":\\\"type.googleapis.com\\\\/google.cloud.audit.AuditLog\\\",\\\"status\\\":{},\\\"authenticationInfo\\\":{\\\"principalEmail\\\":\\\"user@test.com\\\"},\\\"requestMetadata\\\":{\\\"callerIp\\\":\\\"168.149.184.86\\\",\\\"callerSuppliedUserAgent\\\":\\\"Mozilla\\\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\\\/537.36 (KHTML, like Gecko) Chrome\\\\/99.0.4844.84 Safari\\\\/537.36,gzip(gfe),gzip(gfe)\\\",\\\"requestAttributes\\\":{},\\\"destinationAttributes\\\":{}},\\\"serviceName\\\":\\\"bigquery.googleapis.com\\\",\\\"methodName\\\":\\\"google.cloud.bigquery.v2.JobService.InsertJob\\\",\\\"authorizationInfo\\\":[{\\\"resource\\\":\\\"projects\\\\/project-sccd\\\",\\\"permission\\\":\\\"bigquery.jobs.create\\\",\\\"granted\\\":true,\\\"resourceAttributes\\\":{}}],\\\"resourceName\\\":\\\"projects\\\\/project-sccd\\\\/jobs\\\\/bquxjob_787f3164_1804a2c3db5\\\",\\\"metadata\\\":{\\\"jobInsertion\\\":{\\\"reason\\\":\\\"JOB_INSERT_REQUEST\\\",\\\"job\\\":{\\\"jobStats\\\":{\\\"createTime\\\":\\\"2022-04-21T03:32:06.081Z\\\",\\\"startTime\\\":\\\"2022-04-21T03:32:06.185Z\\\",\\\"queryStats\\\":{}},\\\"jobConfig\\\":{\\\"queryConfig\\\":{\\\"priority\\\":\\\"QUERY_INTERACTIVE\\\",\\\"destinationTable\\\":\\\"projects\\\\/project-sccd\\\\/datasets\\\\/BigQueryQADS\\\\/tables\\\\/testOne\\\",\\\"createDisposition\\\":\\\"CREATE_IF_NEEDED\\\",\\\"query\\\":\\\"create table  `project-sccd.BigQueryQADS.testOne`  (\\\\r\\\\n  SingerId   INT64 NOT NULL,\\\\r\\\\n  FirstName  STRING(1024),\\\\r\\\\n  LastName   STRING(1024),\\\\r\\\\n  BirthDate  DATE,\\\\r\\\\n);\\\",\\\"writeDisposition\\\":\\\"WRITE_EMPTY\\\",\\\"statementType\\\":\\\"CREATE_TABLE\\\"},\\\"type\\\":\\\"QUERY\\\"},\\\"jobStatus\\\":{\\\"jobState\\\":\\\"RUNNING\\\"},\\\"jobName\\\":\\\"projects\\\\/project-sccd\\\\/jobs\\\\/bquxjob_787f3164_1804a2c3db5\\\"}},\\\"@type\\\":\\\"type.googleapis.com\\\\/google.cloud.audit.BigQueryAuditMetadata\\\"}},\\\"insertId\\\":\\\"-wq3ig5dhf0p\\\",\\\"resource\\\":{\\\"type\\\":\\\"bigquery_project\\\",\\\"labels\\\":{\\\"project_id\\\":\\\"project-sccd\\\",\\\"location\\\":\\\"US\\\"}},\\\"timestamp\\\":\\\"2022-04-21T03:32:06.236284Z\\\",\\\"severity\\\":\\\"INFO\\\",\\\"logName\\\":\\\"projects\\\\/project-sccd\\\\/logs\\\\/cloudaudit.googleapis.com%2Fdata_access\\\",\\\"operation\\\":{\\\"id\\\":\\\"1650511926081-project-sccd:bquxjob_787f3164_1804a2c3db5\\\",\\\"producer\\\":\\\"bigquery.googleapis.com\\\",\\\"first\\\":true},\\\"receiveTimestamp\\\":\\\"2022-04-21T03:32:06.413809027Z\\\"}";
		BigQueryGuardiumFilter filter = getBigQueryFilterConnector(gcpString2);
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
	public void eventObjectAndVerbCreateTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.184.86\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.84 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_787f3164_1804a2c3db5\",\"metadata\":{\"jobInsertion\":{\"reason\":\"JOB_INSERT_REQUEST\",\"job\":{\"jobStats\":{\"createTime\":\"2022-04-21T03:32:06.081Z\",\"startTime\":\"2022-04-21T03:32:06.185Z\",\"queryStats\":{}},\"jobConfig\":{\"queryConfig\":{\"priority\":\"QUERY_INTERACTIVE\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/BigQueryQADS\\/tables\\/testOne\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"query\":\"create table  `project-sccd.BigQueryQADS.testOne`  (\\r\\n  SingerId   INT64 NOT NULL,\\r\\n  FirstName  STRING(1024),\\r\\n  LastName   STRING(1024),\\r\\n  BirthDate  DATE,\\r\\n);\",\"writeDisposition\":\"WRITE_EMPTY\",\"statementType\":\"CREATE_TABLE\"},\"type\":\"QUERY\"},\"jobStatus\":{\"jobState\":\"RUNNING\"},\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_787f3164_1804a2c3db5\"}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-wq3ig5dhf0p\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-04-21T03:32:06.236284Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1650511926081-project-sccd:bquxjob_787f3164_1804a2c3db5\",\"producer\":\"bigquery.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-21T03:32:06.413809027Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:BigQueryQADS", sqlquery.getDbName());
		assertEquals("user@test.com", sqlquery.getAppUserName());
		assertEquals("create table", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("testOne", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertNotNull(events);
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	private BigQueryGuardiumFilter getBigQueryFilterConnector(String jsonString) {
		Configuration config = new ConfigurationImpl(Collections.singletonMap("source", jsonString));
		Context context = new ContextImpl(null, null);
		BigQueryGuardiumFilter filter = new BigQueryGuardiumFilter("spn-id", config, context);
		return filter;
	}

	@Test
	public void event() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.12.41\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.75 Safari\\/537.36 Edg\\/100.0.1185.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_59173dd3_1803b96a7ba\",\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\",\"jobInsertion\":{\"reason\":\"JOB_INSERT_REQUEST\",\"job\":{\"jobStatus\":{\"jobState\":\"RUNNING\"},\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_59173dd3_1803b96a7ba\",\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"writeDisposition\":\"WRITE_TRUNCATE\",\"statementType\":\"SELECT\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/_1f1c36c3ea0388eeb7a982fdeb3f5cbaed5b427a\\/tables\\/anonafe3be657fddb83f57bf753a081c1535d35a9c85\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"query\":\"select * from `project-sccd.BigQueryE2E.Employee` LIMIT 1000\",\"priority\":\"QUERY_INTERACTIVE\"}},\"jobStats\":{\"createTime\":\"2022-04-18T07:35:47.545Z\",\"queryStats\":{},\"startTime\":\"2022-04-18T07:35:47.722Z\"}}}}},\"insertId\":\"-wq87age2wvch\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-04-18T07:35:47.961913Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1650267347545-project-sccd:bquxjob_59173dd3_1803b96a7ba\",\"producer\":\"bigquery.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-18T07:35:48.786537635Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:BigQueryE2E", sqlquery.getDbName());
		assertEquals("user@test.com", sqlquery.getAppUserName());
		assertEquals("-1198440991", sqlquery.getSessionId());
		assertEquals("select", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("Employee", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void filterProtoPayloadJobChangeRequestTest() {
		final String gcpString2 = "{\\\"protoPayload\\\":{\\\"@type\\\":\\\"type.googleapis.com\\\\/google.cloud.audit.AuditLog\\\",\\\"status\\\":{},\\\"authenticationInfo\\\":{\\\"principalEmail\\\":\\\"user@test.com\\\"},\\\"requestMetadata\\\":{\\\"callerIp\\\":\\\"148.64.5.27\\\",\\\"callerSuppliedUserAgent\\\":\\\"Mozilla\\\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\\\/537.36 (KHTML, like Gecko) Chrome\\\\/94.0.4606.81 Safari\\\\/537.36,gzip(gfe)\\\",\\\"requestAttributes\\\":{},\\\"destinationAttributes\\\":{}},\\\"serviceName\\\":\\\"bigquery.googleapis.com\\\",\\\"methodName\\\":\\\"tableservice.insert\\\",\\\"authorizationInfo\\\":[{\\\"resource\\\":\\\"projects\\\\/bigqueryproject-330206\\\\/datasets\\\\/bigQuery_DataSet\\\",\\\"permission\\\":\\\"bigquery.tables.create\\\",\\\"granted\\\":true,\\\"resourceAttributes\\\":{}}],\\\"resourceName\\\":\\\"projects\\\\/bigqueryproject-330206\\\\/datasets\\\\/bigQuery_DataSet\\\\/tables\\\",\\\"metadata\\\":{\\\"@type\\\":\\\"type.googleapis.com\\\\/google.cloud.bigquery.logging.v1.AuditData\\\",\\\"tableChange\\\":{\\\"resource\\\":{\\\"tableName\\\":{\\\"projectId\\\":\\\"bigqueryproject-330206\\\",\\\"datasetId\\\":\\\"bigQuery_DataSet\\\",\\\"tableId\\\":\\\"person\\\"},\\\"info\\\":{},\\\"view\\\":{\\\"query\\\":\\\"select * from tableName\\\"},\\\"expireTime\\\":\\\"2021-12-25T06:39:34.754Z\\\",\\\"createTime\\\":\\\"2021-10-26T06:39:34.754Z\\\",\\\"schemaJson\\\":\\\"{\\\\n  \\\\\\\"fields\\\\\\\": [{\\\\n    \\\\\\\"name\\\\\\\": \\\\\\\"id\\\\\\\",\\\\n    \\\\\\\"type\\\\\\\": \\\\\\\"NUMERIC\\\\\\\",\\\\n    \\\\\\\"mode\\\\\\\": \\\\\\\"NULLABLE\\\\\\\"\\\\n  }, {\\\\n    \\\\\\\"name\\\\\\\": \\\\\\\"first_name\\\\\\\",\\\\n    \\\\\\\"type\\\\\\\": \\\\\\\"STRING\\\\\\\",\\\\n    \\\\\\\"mode\\\\\\\": \\\\\\\"NULLABLE\\\\\\\"\\\\n  }, {\\\\n    \\\\\\\"name\\\\\\\": \\\\\\\"last_name\\\\\\\",\\\\n    \\\\\\\"type\\\\\\\": \\\\\\\"STRING\\\\\\\",\\\\n    \\\\\\\"mode\\\\\\\": \\\\\\\"NULLABLE\\\\\\\"\\\\n  }, {\\\\n    \\\\\\\"name\\\\\\\": \\\\\\\"age\\\\\\\",\\\\n    \\\\\\\"type\\\\\\\": \\\\\\\"NUMERIC\\\\\\\",\\\\n    \\\\\\\"mode\\\\\\\": \\\\\\\"NULLABLE\\\\\\\"\\\\n  }]\\\\n}\\\",\\\"updateTime\\\":\\\"2021-10-26T06:39:34.794Z\\\"}}}},\\\"insertId\\\":\\\"-w646o2dacdo\\\",\\\"resource\\\":{\\\"type\\\":\\\"bigquery_resource\\\",\\\"labels\\\":{\\\"project_id\\\":\\\"bigqueryproject-330206\\\"}},\\\"timestamp\\\":\\\"2021-10-26T06:39:34.836392Z\\\",\\\"severity\\\":\\\"NOTICE\\\",\\\"logName\\\":\\\"projects\\\\/bigqueryproject-330206\\\\/logs\\\\/cloudaudit.googleapis.com%2Factivity\\\",\\\"receiveTimestamp\\\":\\\"2021-10-26T06:39:35.675223426Z\\\"}";
		BigQueryGuardiumFilter filter = getBigQueryFilterConnector(gcpString2);
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
	public void filterProtoPayloadTableChangeTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.5.27\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/94.0.4606.81 Safari\\/537.36,gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"tableservice.insert\",\"authorizationInfo\":[{\"resource\":\"projects\\/bigqueryproject-330206\\/datasets\\/bigQuery_DataSet\",\"permission\":\"bigquery.tables.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/bigqueryproject-330206\\/datasets\\/bigQuery_DataSet\\/tables\",\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.bigquery.logging.v1.AuditData\",\"tableChange\":{\"resource\":{\"tableName\":{\"projectId\":\"bigqueryproject-330206\",\"datasetId\":\"bigQuery_DataSet\",\"tableId\":\"person\"},\"info\":{},\"view\":{\"query\":\"select * from tableName\"},\"expireTime\":\"2021-12-25T06:39:34.754Z\",\"createTime\":\"2021-10-26T06:39:34.754Z\",\"schemaJson\":\"{\\n  \\\"fields\\\": [{\\n    \\\"name\\\": \\\"id\\\",\\n    \\\"type\\\": \\\"NUMERIC\\\",\\n    \\\"mode\\\": \\\"NULLABLE\\\"\\n  }, {\\n    \\\"name\\\": \\\"first_name\\\",\\n    \\\"type\\\": \\\"STRING\\\",\\n    \\\"mode\\\": \\\"NULLABLE\\\"\\n  }, {\\n    \\\"name\\\": \\\"last_name\\\",\\n    \\\"type\\\": \\\"STRING\\\",\\n    \\\"mode\\\": \\\"NULLABLE\\\"\\n  }, {\\n    \\\"name\\\": \\\"age\\\",\\n    \\\"type\\\": \\\"NUMERIC\\\",\\n    \\\"mode\\\": \\\"NULLABLE\\\"\\n  }]\\n}\",\"updateTime\":\"2021-10-26T06:39:34.794Z\"}}}},\"insertId\":\"-w646o2dacdo\",\"resource\":{\"type\":\"bigquery_resource\",\"labels\":{\"project_id\":\"bigqueryproject-330206\"}},\"timestamp\":\"2021-10-26T06:39:34.836392Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/bigqueryproject-330206\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2021-10-26T06:39:35.675223426Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("bigqueryproject-330206:", sqlquery.getDbName());
		assertEquals("user@test.com", sqlquery.getAppUserName());
		assertEquals("select", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void filterProtoPayloadErrorwithoutDescriptionTEST() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.187.11\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.84 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_68d3c23e_18049d458fc\",\"metadata\":{\"jobInsertion\":{\"job\":{\"jobConfig\":{\"queryConfig\":{\"query\":\"create schema  `BigQueryQADS`;\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"priority\":\"QUERY_INTERACTIVE\",\"writeDisposition\":\"WRITE_EMPTY\"},\"type\":\"QUERY\"},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"queryStats\":{},\"endTime\":\"2022-04-21T01:56:06.823Z\",\"createTime\":\"2022-04-21T01:56:06.803Z\",\"startTime\":\"2022-04-21T01:56:06.823Z\"},\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_68d3c23e_18049d458fc\"},\"reason\":\"JOB_INSERT_REQUEST\"},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-oomtnhddawf\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-04-21T01:56:06.875742Z\",\"severity\":\"ERROR\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1650506166803-project-sccd:bquxjob_68d3c23e_18049d458fc\",\"producer\":\"bigquery.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-21T01:56:06.916288378Z\"}";
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:", sqlquery.getDbName());
		assertEquals("-955314875", sqlquery.getSessionId());
		assertEquals("SQL_ERROR", sqlquery.getException().getExceptionTypeId());
		assertEquals("", sqlquery.getException().getDescription());
		assertEquals(null, sqlquery.getData());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void filterProtoPayloadErrorwithDescriptionTEST() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.187.11\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.84 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_68d3c23e_18049d458fc\",\"metadata\":{\"jobChange\":{\"job\":{\"jobConfig\":{\"queryConfig\":{\"query\":\"create schema  `project-sccd.BigQueryQADS.userdetailQW`;\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"priority\":\"QUERY_INTERACTIVE\",\"writeDisposition\":\"WRITE_EMPTY\"},\"type\":\"QUERY\"},\"jobStatus\":{\"errors\":[{\"code\":3,\"message\":\"Invalid project ID 'project-sccd.BigQueryQADS'. Project IDs must contain 6-63 lowercase letters, digits, or dashes. Some project IDs also include domain name separated by a colon. IDs must start with a letter and may not end with a dash.\"}],\"errorResult\":{\"message\":\"Invalid project ID 'project-sccd.BigQueryQADS'. Project IDs must contain 6-63 lowercase letters, digits, or dashes. Some project IDs also include domain name separated by a colon. IDs must start with a letter and may not end with a dash.\",\"code\":3},\"jobState\":\"DONE\"},\"jobStats\":{\"queryStats\":{},\"endTime\":\"2022-04-21T01:56:06.823Z\",\"createTime\":\"2022-04-21T01:56:06.803Z\",\"startTime\":\"2022-04-21T01:56:06.823Z\"},\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_68d3c23e_18049d458fc\"},\"reason\":\"JOB_INSERT_REQUEST\"},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-oomtnhddawf\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-04-21T01:56:06.875742Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1650506166803-project-sccd:bquxjob_68d3c23e_18049d458fc\",\"producer\":\"bigquery.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-21T01:56:06.916288378Z\"}";
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:", sqlquery.getDbName());
		assertEquals(Long.parseLong("1650506166875"), sqlquery.getTime().getTimstamp());
		assertEquals(0, sqlquery.getTime().getMinOffsetFromGMT());
		assertEquals("SQL_ERROR", sqlquery.getException().getExceptionTypeId());
		assertEquals("Invalid project ID 'project-sccd.BigQueryQADS'. Project IDs must contain 6-63 lowercase letters, digits, or dashes. Some project IDs also include domain name separated by a colon. IDs must start with a letter and may not end with a dash.", sqlquery.getException().getDescription());
		assertEquals(null, sqlquery.getData());
		assertEquals("create schema  `project-sccd.BigQueryQADS.userdetailQW` ", sqlquery.getException().getSqlString());
	}

	@Test
	public void filterProtoPayloadErrorTEST() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.187.11\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.84 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_68d3c23e_18049d458fc\",\"metadata\":{\"jobChange\":{\"job\":{\"jobConfig\":{\"queryConfig\":{\"query\":\"create schema  `project-sccd.BigQueryQADS.userdetailQW`;\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"priority\":\"QUERY_INTERACTIVE\",\"writeDisposition\":\"WRITE_EMPTY\"},\"type\":\"QUERY\"},\"jobStatus\":{\"errors\":[{\"code\":3,\"message\":\"Invalid project ID 'project-sccd.BigQueryQADS'. Project IDs must contain 6-63 lowercase letters, digits, or dashes. Some project IDs also include domain name separated by a colon. IDs must start with a letter and may not end with a dash.\"}],\"errorResult\":{},\"jobState\":\"DONE\"},\"jobStats\":{\"queryStats\":{},\"endTime\":\"2022-04-21T01:56:06.823Z\",\"createTime\":\"2022-04-21T01:56:06.803Z\",\"startTime\":\"2022-04-21T01:56:06.823Z\"},\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_68d3c23e_18049d458fc\"},\"reason\":\"JOB_INSERT_REQUEST\"},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-oomtnhddawf\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-04-21T01:56:06.875742Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1650506166803-project-sccd:bquxjob_68d3c23e_18049d458fc\",\"producer\":\"bigquery.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2022-04-21T01:56:06.916288378Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:", sqlquery.getDbName());
		assertEquals("user@test.com", sqlquery.getAppUserName());
		assertEquals("SQL_ERROR", sqlquery.getException().getExceptionTypeId());
		assertEquals("create schema  `project-sccd.BigQueryQADS.userdetailQW` ", sqlquery.getException().getSqlString());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void filterSQLWithSlashTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"122.177.51.80\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.51 Safari\\/537.36 Edg\\/99.0.1150.39,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_3a9bcd2e_17fdef9a82a\",\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\",\"jobChange\":{\"job\":{\"jobStats\":{\"queryStats\":{\"cacheHit\":true},\"createTime\":\"2022-03-31T07:56:16.896Z\",\"endTime\":\"2022-03-31T07:56:17.088Z\",\"startTime\":\"2022-03-31T07:56:17.035Z\"},\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_3a9bcd2e_17fdef9a82a\",\"jobStatus\":{\"jobState\":\"DONE\"},\"jobConfig\":{\"queryConfig\":{\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/_1b12d4043516e7fe556c67c37894adb7c0c3588b\\/tables\\/anon8a04e12e525797da397d67d2135c12d9cc4f325c\",\"writeDisposition\":\"WRITE_TRUNCATE\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"priority\":\"QUERY_INTERACTIVE\",\"query\":\"select * from  `project-sccd.BigQueryQADS.empDetails` where name =\\\"sandeep\\\";\\r\\n\",\"statementType\":\"SELECT\"},\"type\":\"QUERY\"}},\"after\":\"DONE\"}}},\"insertId\":\"-bn7puve3k0vp\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-03-31T07:56:17.091042Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1648713376896-project-sccd:bquxjob_3a9bcd2e_17fdef9a82a\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-03-31T07:56:17.339111864Z\"}";
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:BigQueryQADS", sqlquery.getDbName());
		assertEquals("select", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("empDetails", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("select * from  `project-sccd.BigQueryQADS.empDetails` where name = \"sandeep \"   ", sqlquery.getData().getConstruct().fullSql);
		assertEquals("select * from  `project-sccd.BigQueryQADS.empDetails` where name = ?   ", sqlquery.getData().getConstruct().redactedSensitiveDataSql);
	}

	@Test
	public void testParseRecordMetaDataWithOperators() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.7.241\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.41 Safari\\/537.36 Edg\\/101.0.1210.32,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_5121cbab_180ac7c8144\",\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\",\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobStats\":{\"createTime\":\"2022-05-10T05:44:39.493Z\",\"totalSlotMs\":\"13\",\"startTime\":\"2022-05-10T05:44:39.559Z\",\"queryStats\":{\"billingTier\":1,\"totalProcessedBytes\":\"80\",\"outputRowCount\":\"4\",\"totalBilledBytes\":\"10485760\",\"referencedTables\":[\"projects\\/project-sccd\\/datasets\\/SampleBigquery\\/tables\\/regions\"]},\"endTime\":\"2022-05-10T05:44:39.737Z\"},\"jobConfig\":{\"queryConfig\":{\"query\":\"SELECT * FROM `project-sccd.SampleBigquery.regions` TABLESAMPLE SYSTEM (10 PERCENT)\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/_1f1c36c3ea0388eeb7a982fdeb3f5cbaed5b427a\\/tables\\/anon07bece15_8015_4321_bfa1_0688770bb158\",\"statementType\":\"SELECT\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_TRUNCATE\",\"priority\":\"QUERY_INTERACTIVE\"},\"type\":\"QUERY\"},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_5121cbab_180ac7c8144\"}}}},\"insertId\":\"opa31yet3qtg\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"location\":\"US\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-10T05:44:39.779888Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1652161479493-project-sccd:bquxjob_5121cbab_180ac7c8144\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-05-10T05:44:40.356367737Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:SampleBigquery", sqlquery.getDbName());
		assertEquals("-994385949", sqlquery.getSessionId());
		assertEquals("select", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("regions", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("SELECT * FROM `project-sccd.SampleBigquery.regions` TABLESAMPLE SYSTEM (10 PERCENT)", sqlquery.getData().getConstruct().fullSql);
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordMetaDataWithUnionAll() {
		final String gcpString2 = "{\\\"protoPayload\\\":{\\\"@type\\\":\\\"type.googleapis.com\\\\/google.cloud.audit.AuditLog\\\",\\\"status\\\":{},\\\"authenticationInfo\\\":{\\\"principalEmail\\\":\\\"user@test.com\\\"},\\\"requestMetadata\\\":{\\\"callerIp\\\":\\\"148.64.7.241\\\",\\\"callerSuppliedUserAgent\\\":\\\"Mozilla\\\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\\\/537.36 (KHTML, like Gecko) Chrome\\\\/101.0.4951.41 Safari\\\\/537.36 Edg\\\\/101.0.1210.32,gzip(gfe),gzip(gfe)\\\",\\\"requestAttributes\\\":{},\\\"destinationAttributes\\\":{}},\\\"serviceName\\\":\\\"bigquery.googleapis.com\\\",\\\"methodName\\\":\\\"google.cloud.bigquery.v2.JobService.InsertJob\\\",\\\"authorizationInfo\\\":[{\\\"resource\\\":\\\"projects\\\\/project-sccd\\\",\\\"permission\\\":\\\"bigquery.jobs.create\\\",\\\"granted\\\":true,\\\"resourceAttributes\\\":{}}],\\\"resourceName\\\":\\\"projects\\\\/project-sccd\\\\/jobs\\\\/bquxjob_640bae2b_180ac9c199e\\\",\\\"metadata\\\":{\\\"jobInsertion\\\":{\\\"job\\\":{\\\"jobConfig\\\":{\\\"queryConfig\\\":{\\\"destinationTable\\\":\\\"projects\\\\/project-sccd\\\\/datasets\\\\/_1f1c36c3ea0388eeb7a982fdeb3f5cbaed5b427a\\\\/tables\\\\/anon01aade025de4cd3104ed641ecf9597ae186c7267\\\",\\\"createDisposition\\\":\\\"CREATE_IF_NEEDED\\\",\\\"query\\\":\\\"SELECT region_id FROM `project-sccd.SampleBigquery.regions` \\\\r\\\\nUNION ALL\\\\r\\\\nSELECT region_id FROM `project-sccd.SampleBigquery.countries`\\\",\\\"priority\\\":\\\"QUERY_INTERACTIVE\\\",\\\"writeDisposition\\\":\\\"WRITE_TRUNCATE\\\",\\\"statementType\\\":\\\"SELECT\\\"},\\\"type\\\":\\\"QUERY\\\"},\\\"jobStats\\\":{\\\"endTime\\\":\\\"2022-05-10T06:19:10.211Z\\\",\\\"totalSlotMs\\\":\\\"42\\\",\\\"queryStats\\\":{\\\"referencedTables\\\":[\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/countries\\\",\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/Mvied\\\",\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/regions\\\",\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/Mviddded\\\",\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/Mviewed\\\"],\\\"totalProcessedBytes\\\":\\\"224\\\",\\\"billingTier\\\":1,\\\"outputRowCount\\\":\\\"28\\\",\\\"totalBilledBytes\\\":\\\"20971520\\\",\\\"referencedViews\\\":[\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/Mvied\\\",\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/Mviddded\\\",\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/Mviewed\\\"]},\\\"startTime\\\":\\\"2022-05-10T06:19:09.986Z\\\",\\\"createTime\\\":\\\"2022-05-10T06:19:09.874Z\\\"},\\\"jobStatus\\\":{\\\"jobState\\\":\\\"DONE\\\"},\\\"jobName\\\":\\\"projects\\\\/project-sccd\\\\/jobs\\\\/bquxjob_640bae2b_180ac9c199e\\\"},\\\"reason\\\":\\\"JOB_INSERT_REQUEST\\\"},\\\"@type\\\":\\\"type.googleapis.com\\\\/google.cloud.audit.BigQueryAuditMetadata\\\"}},\\\"insertId\\\":\\\"45ubbie7nv1o\\\",\\\"resource\\\":{\\\"type\\\":\\\"bigquery_project\\\",\\\"labels\\\":{\\\"location\\\":\\\"US\\\",\\\"project_id\\\":\\\"project-sccd\\\"}},\\\"timestamp\\\":\\\"2022-05-10T06:19:10.261330Z\\\",\\\"severity\\\":\\\"INFO\\\",\\\"logName\\\":\\\"projects\\\\/project-sccd\\\\/logs\\\\/cloudaudit.googleapis.com%2Fdata_access\\\",\\\"operation\\\":{\\\"id\\\":\\\"1652163549874-project-sccd:bquxjob_640bae2b_180ac9c199e\\\",\\\"producer\\\":\\\"bigquery.googleapis.com\\\",\\\"first\\\":true},\\\"receiveTimestamp\\\":\\\"2022-05-10T06:19:11.077461046Z\\\"}";
		BigQueryGuardiumFilter filter = getBigQueryFilterConnector(gcpString2);
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
	public void testParseRecordMetaDataWithUnionDistinct() {
		final String gcpString2 = "{\\\"protoPayload\\\":{\\\"@type\\\":\\\"type.googleapis.com\\\\/google.cloud.audit.AuditLog\\\",\\\"status\\\":{},\\\"authenticationInfo\\\":{\\\"principalEmail\\\":\\\"user@test.com\\\"},\\\"requestMetadata\\\":{\\\"callerIp\\\":\\\"148.64.7.241\\\",\\\"callerSuppliedUserAgent\\\":\\\"Mozilla\\\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\\\/537.36 (KHTML, like Gecko) Chrome\\\\/101.0.4951.41 Safari\\\\/537.36 Edg\\\\/101.0.1210.32,gzip(gfe),gzip(gfe)\\\",\\\"requestAttributes\\\":{},\\\"destinationAttributes\\\":{}},\\\"serviceName\\\":\\\"bigquery.googleapis.com\\\",\\\"methodName\\\":\\\"google.cloud.bigquery.v2.JobService.InsertJob\\\",\\\"authorizationInfo\\\":[{\\\"resource\\\":\\\"projects\\\\/project-sccd\\\",\\\"permission\\\":\\\"bigquery.jobs.create\\\",\\\"granted\\\":true,\\\"resourceAttributes\\\":{}}],\\\"resourceName\\\":\\\"projects\\\\/project-sccd\\\\/jobs\\\\/bquxjob_33666de5_180ac9c054b\\\",\\\"metadata\\\":{\\\"jobInsertion\\\":{\\\"reason\\\":\\\"JOB_INSERT_REQUEST\\\",\\\"job\\\":{\\\"jobStatus\\\":{\\\"jobState\\\":\\\"DONE\\\"},\\\"jobStats\\\":{\\\"startTime\\\":\\\"2022-05-10T06:19:04.705Z\\\",\\\"createTime\\\":\\\"2022-05-10T06:19:04.584Z\\\",\\\"endTime\\\":\\\"2022-05-10T06:19:05.094Z\\\",\\\"totalSlotMs\\\":\\\"154\\\",\\\"queryStats\\\":{\\\"totalBilledBytes\\\":\\\"20971520\\\",\\\"outputRowCount\\\":\\\"4\\\",\\\"billingTier\\\":1,\\\"referencedTables\\\":[\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/Mviewed\\\",\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/countries\\\",\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/Mvied\\\",\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/Mviddded\\\",\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/regions\\\"],\\\"totalProcessedBytes\\\":\\\"224\\\",\\\"referencedViews\\\":[\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/Mviewed\\\",\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/Mvied\\\",\\\"projects\\\\/project-sccd\\\\/datasets\\\\/SampleBigquery\\\\/tables\\\\/Mviddded\\\"]}},\\\"jobConfig\\\":{\\\"queryConfig\\\":{\\\"statementType\\\":\\\"SELECT\\\",\\\"destinationTable\\\":\\\"projects\\\\/project-sccd\\\\/datasets\\\\/_1f1c36c3ea0388eeb7a982fdeb3f5cbaed5b427a\\\\/tables\\\\/anon067e1388cf48e32b30f87e94f8102ea0c3a7be49\\\",\\\"writeDisposition\\\":\\\"WRITE_TRUNCATE\\\",\\\"createDisposition\\\":\\\"CREATE_IF_NEEDED\\\",\\\"query\\\":\\\"SELECT region_id FROM `project-sccd.SampleBigquery.regions` \\\\r\\\\nUNION DISTINCT \\\\r\\\\nSELECT region_id FROM `project-sccd.SampleBigquery.countries`\\\",\\\"priority\\\":\\\"QUERY_INTERACTIVE\\\"},\\\"type\\\":\\\"QUERY\\\"},\\\"jobName\\\":\\\"projects\\\\/project-sccd\\\\/jobs\\\\/bquxjob_33666de5_180ac9c054b\\\"}},\\\"@type\\\":\\\"type.googleapis.com\\\\/google.cloud.audit.BigQueryAuditMetadata\\\"}},\\\"insertId\\\":\\\"yp3mlqf14vf1j\\\",\\\"resource\\\":{\\\"type\\\":\\\"bigquery_project\\\",\\\"labels\\\":{\\\"location\\\":\\\"US\\\",\\\"project_id\\\":\\\"project-sccd\\\"}},\\\"timestamp\\\":\\\"2022-05-10T06:19:05.145936Z\\\",\\\"severity\\\":\\\"INFO\\\",\\\"logName\\\":\\\"projects\\\\/project-sccd\\\\/logs\\\\/cloudaudit.googleapis.com%2Fdata_access\\\",\\\"operation\\\":{\\\"id\\\":\\\"1652163544584-project-sccd:bquxjob_33666de5_180ac9c054b\\\",\\\"producer\\\":\\\"bigquery.googleapis.com\\\",\\\"first\\\":true},\\\"receiveTimestamp\\\":\\\"2022-05-10T06:19:06.141381920Z\\\"}";
		BigQueryGuardiumFilter filter = getBigQueryFilterConnector(gcpString2);
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
	public void testParseRecordMetaDataWithIntersectDistinct() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.7.26\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.64 Safari\\/537.36 Edg\\/101.0.1210.47,gzip(gfe),gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_aaffd9_180e10a87dd\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_aaffd9_180e10a87dd\",\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"query\":\"CREATE TABLE BigQueryE2E.Student (id INT64, first_name STRING(8), last_name STRING(9));\\r\\nCREATE TABLE BigQueryE2E.Course (id INT64, name STRING(16), teacher_id INT64);\\r\\nCREATE TABLE BigQueryE2E.Student_Course (id INT64, student_id INT64,ccourse_id INT64);\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\",\"priority\":\"QUERY_INTERACTIVE\",\"statementType\":\"SCRIPT\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-05-20T10:40:11.275Z\",\"startTime\":\"2022-05-20T10:40:11.335Z\",\"endTime\":\"2022-05-20T10:40:13.551Z\",\"queryStats\":{}}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"ldhm7ve6dr9p\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"location\":\"US\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-20T10:40:13.560531Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1653043211275-project-sccd:bquxjob_aaffd9_180e10a87dd\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-05-20T10:40:14.417906660Z\"}";
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:BigQueryE2E", sqlquery.getDbName());
		assertEquals("1437565344", sqlquery.getSessionId());
		assertEquals("user@test.com", sqlquery.getAppUserName());
		assertEquals("create table", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("Student", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("CREATE TABLE BigQueryE2E.Student (id INT64, first_name STRING(8), last_name STRING(9))   CREATE TABLE BigQueryE2E.Course (id INT64, name STRING(16), teacher_id INT64)   CREATE TABLE BigQueryE2E.Student_Course (id INT64, student_id INT64,ccourse_id INT64) ", sqlquery.getData().getConstruct().fullSql);
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordMetaDataWithTable() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.5.202\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.41 Safari\\/537.36 Edg\\/101.0.1210.32,gzip(gfe),gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_66d45f65_180bb95d0e0\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_66d45f65_180bb95d0e0\",\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"query\":\"CREATE table testInfo.Customertables\\r\\n(\\r\\nx INT64 OPTIONS(description=\\\"An optional INTEGER field\\\"),\\r\\ny STRUCT<\\r\\nb BOOL\\r\\n>\\r\\n)\\r\\nOPTIONS(\\r\\nexpiration_timestamp=TIMESTAMP \\\"2023-01-01 00:00:00 UTC\\\",\\r\\ndescription=\\\"a table that expires in 2023\\\",\\r\\nlabels=[(\\\"org_unit\\\", \\\"development\\\")]\\r\\n)\\r\\n\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/testInfo\\/tables\\/CustomerRegion\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\",\"priority\":\"QUERY_INTERACTIVE\",\"statementType\":\"CREATE_TABLE\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-05-13T04:06:40.926Z\",\"startTime\":\"2022-05-13T04:06:41.018Z\",\"endTime\":\"2022-05-13T04:06:41.184Z\",\"queryStats\":{}}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-wql3brdxnoq\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-05-13T04:06:41.192811Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1652414800926-project-sccd:bquxjob_66d45f65_180bb95d0e0\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-05-13T04:06:41.959826021Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:testInfo", sqlquery.getDbName());
		assertEquals("create table", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("Customertables", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("CREATE table testInfo.Customertables  (  x INT64 OPTIONS(description= \"An optional INTEGER field \"),  y STRUCT<  b BOOL  >  )  OPTIONS(  expiration_timestamp=TIMESTAMP  \"2023-01-01 00:00:00 UTC \",  description= \"a table that expires in 2023 \",  labels=[( \"org_unit \",  \"development \")]  )  ", sqlquery.getData().getConstruct().fullSql);
		assertEquals("CREATE table testInfo.Customertables  (  x INT64 OPTIONS(description= \"An optional INTEGER field \"),  y STRUCT<  b BOOL  >  )  OPTIONS(  expiration_timestamp=TIMESTAMP  \"2023-01-01 00:00:00 UTC \",  description= \"a table that expires in 2023 \",  labels=[( \"org_unit \",  \"development \")]  )  ", sqlquery.getData().getConstruct().redactedSensitiveDataSql);
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordMetaDataWithFunction() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.7.102\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_3365572a_180796b3fe1\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_3365572a_180796b3fe1\",\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"query\":\"CREATE FUNCTION `project-sccd.SampleBigquery.regions`(arr ANY TYPE) AS (\\r\\n  (\\r\\n    SELECT\\r\\n      IF(\\r\\n        MOD(ARRAY_LENGTH(arr), 2) = 0,\\r\\n        (arr[OFFSET(DIV(ARRAY_LENGTH(arr), 2) - 1)] + arr[OFFSET(DIV(ARRAY_LENGTH(arr), 2))]) \\/ 2,\\r\\n        arr[OFFSET(DIV(ARRAY_LENGTH(arr), 2))]\\r\\n      )\\r\\n    FROM (SELECT ARRAY_AGG(x ORDER BY x) AS arr FROM UNNEST(arr) AS x)\\r\\n  )\\r\\n);\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\",\"priority\":\"QUERY_INTERACTIVE\",\"statementType\":\"CREATE_FUNCTION\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-04-30T07:45:02.421Z\",\"startTime\":\"2022-04-30T07:45:02.525Z\",\"endTime\":\"2022-04-30T07:45:02.622Z\",\"queryStats\":{}}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-iayzcie3ivrt\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"location\":\"US\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-04-30T07:45:02.631459Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1651304702421-project-sccd:bquxjob_3365572a_180796b3fe1\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-04-30T07:45:03.355019668Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:SampleBigquery", sqlquery.getDbName());
		assertEquals("user@test.com", sqlquery.getAppUserName());
		assertEquals("create function", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("regions", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordMetaDataWithProcedure() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.7.102\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/script_job_a68c7cdc16ba1b476fd55a442d3a1448_0\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/script_job_a68c7cdc16ba1b476fd55a442d3a1448_0\",\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"query\":\"CREATE PROCEDURE `project-sccd.SampleBigquery.myproc`()\\r\\nBEGIN\\r\\n SELECT\\r\\n  fileName AS unique_id,\\r\\n  historyStatus AS latest_status,\\r\\n  lastUpdatedTimestamp AS last_update,\\r\\n  ROW_NUMBER() OVER (PARTITION BY fileName ORDER BY lastUpdatedTimestamp DESC) AS row_number\\r\\nFROM `bigquery-public-data.fcc_political_ads.file_history`\\r\\nORDER BY\\r\\n  1,\\r\\n  3 DESC;\\r\\nEND\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\",\"priority\":\"QUERY_INTERACTIVE\",\"statementType\":\"CREATE_PROCEDURE\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-04-30T08:27:36.137Z\",\"startTime\":\"2022-04-30T08:27:36.204Z\",\"endTime\":\"2022-04-30T08:27:36.356Z\",\"queryStats\":{},\"parentJobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_6ae18a5d_18079923909\"}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-skdehtehvnj2\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"location\":\"US\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-04-30T08:27:36.372161Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1651307256137-project-sccd:script_job_a68c7cdc16ba1b476fd55a442d3a1448_0\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-04-30T08:27:36.713485373Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:SampleBigquery", sqlquery.getDbName());
		assertEquals("-376165729", sqlquery.getSessionId());
		assertEquals("create procedure", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("myproc", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordMetaDataWithMaterialized() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.7.102\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_aae3c3b_180791373f6\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_aae3c3b_180791373f6\",\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"query\":\"CREATE      MATERIALIZED        VIEW  `SampleBigquery.       Mview` AS   SELECT country_id,country_name, COUNT(region_id) as count\\r\\nFROM `project-sccd.SampleBigquery.countries` group by country_id, country_name;\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/SampleBigquery\\/tables\\/Mview\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\",\"priority\":\"QUERY_INTERACTIVE\",\"statementType\":\"CREATE_MATERIALIZED_VIEW\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-04-30T06:09:07.675Z\",\"startTime\":\"2022-04-30T06:09:07.783Z\",\"endTime\":\"2022-04-30T06:09:08.134Z\",\"queryStats\":{\"referencedTables\":[\"projects\\/project-sccd\\/datasets\\/SampleBigquery\\/tables\\/countries\"]}}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-mqr2tve3vkkk\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"location\":\"US\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-04-30T06:09:08.143896Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1651298947675-project-sccd:bquxjob_aae3c3b_180791373f6\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-04-30T06:09:08.934935711Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:SampleBigquery", sqlquery.getDbName());
		assertEquals("create materialized view", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("       Mview", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("CREATE      MATERIALIZED        VIEW  `SampleBigquery.       Mview` AS   SELECT country_id,country_name, COUNT(region_id) as count  FROM `project-sccd.SampleBigquery.countries` group by country_id, country_name ", sqlquery.getData().getConstruct().fullSql);
		assertEquals("CREATE      MATERIALIZED        VIEW  `SampleBigquery.       Mview` AS   SELECT country_id,country_name, COUNT(region_id) as count  FROM `project-sccd.SampleBigquery.countries` group by country_id, country_name ", sqlquery.getData().getConstruct().redactedSensitiveDataSql);
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordMetaDataWithAlterMaterialized() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.7.102\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_aae3c3b_180791373f6\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_aae3c3b_180791373f6\",\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"query\":\"ALTER            MATERIALIZED          VIEW    `SampleBigquery.       Mview` AS   SELECT country_id,country_name, COUNT(region_id) as count\\r\\nFROM `project-sccd.SampleBigquery.countries` group by country_id, country_name;\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/SampleBigquery\\/tables\\/Mview\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\",\"priority\":\"QUERY_INTERACTIVE\",\"statementType\":\"CREATE_MATERIALIZED_VIEW\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-04-30T06:09:07.675Z\",\"startTime\":\"2022-04-30T06:09:07.783Z\",\"endTime\":\"2022-04-30T06:09:08.134Z\",\"queryStats\":{\"referencedTables\":[\"projects\\/project-sccd\\/datasets\\/SampleBigquery\\/tables\\/countries\"]}}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-mqr2tve3vkkk\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"location\":\"US\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-04-30T06:09:08.143896Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1651298947675-project-sccd:bquxjob_aae3c3b_180791373f6\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-04-30T06:09:08.934935711Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:SampleBigquery", sqlquery.getDbName());
		assertEquals("-376165729", sqlquery.getSessionId());
		assertEquals("alter materialized view", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("       Mview", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordMetaDataWithView() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.7.102\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_547cee3a_180791126ca\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_547cee3a_180791126ca\",\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"query\":\"CREATE VIEW `project-sccd.SampleBigquery.groups`(name, count) AS SELECT country_name, COUNT(*) as count\\r\\nFROM `project-sccd.SampleBigquery.countries`\\r\\ngroup by country_name having count > 1;\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/SampleBigquery\\/tables\\/groups\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\",\"priority\":\"QUERY_INTERACTIVE\",\"statementType\":\"CREATE_VIEW\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-04-30T06:06:37.551Z\",\"startTime\":\"2022-04-30T06:06:37.651Z\",\"endTime\":\"2022-04-30T06:06:37.816Z\",\"queryStats\":{\"referencedTables\":[\"projects\\/project-sccd\\/datasets\\/SampleBigquery\\/tables\\/countries\"]}}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"ajoyg3e2y8sn\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"location\":\"US\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-04-30T06:06:37.823543Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1651298797551-project-sccd:bquxjob_547cee3a_180791126ca\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-04-30T06:06:38.183637403Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:SampleBigquery", sqlquery.getDbName());
		assertEquals("create view", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("groups", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("CREATE VIEW `project-sccd.SampleBigquery.groups`(name, count) AS SELECT country_name, COUNT(*) as count  FROM `project-sccd.SampleBigquery.countries`  group by country_name having count > 1 ", sqlquery.getData().getConstruct().fullSql);
		assertEquals("CREATE VIEW `project-sccd.SampleBigquery.groups`(name, count) AS SELECT country_name, COUNT(*) as count  FROM `project-sccd.SampleBigquery.countries`  group by country_name having count > ? ", sqlquery.getData().getConstruct().redactedSensitiveDataSql);
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordMetaDataWithTruncate() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.7.11\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_266c3f83_180740997f2\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_266c3f83_180740997f2\",\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"query\":\"truncate table BigQueryQADS.TestPrf\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/BigQueryQADS\\/tables\\/TestPrf\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\",\"priority\":\"QUERY_INTERACTIVE\",\"statementType\":\"TRUNCATE_TABLE\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-04-29T06:40:13.884Z\",\"startTime\":\"2022-04-29T06:40:14.148Z\",\"endTime\":\"2022-04-29T06:40:15.057Z\",\"queryStats\":{\"referencedTables\":[\"projects\\/project-sccd\\/datasets\\/BigQueryQADS\\/tables\\/TestPrf\"]}}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"i1qxmudzz1z\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"location\":\"US\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-04-29T06:40:15.062826Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1651214413884-project-sccd:bquxjob_266c3f83_180740997f2\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-04-29T06:40:15.950274800Z\"}";
		BigQueryGuardiumFilter filter = getBigQueryFilterConnector(gcpString2);
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
	public void testParseRecordMetaDataWithSnapshot() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.166.232\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/100.0.4896.127 Safari\\/537.36 Edg\\/100.0.1185.50,gzip(gfe),gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_7d79c6a8_18079a45a9d\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_7d79c6a8_18079a45a9d\",\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"query\":\"DROP        SNAPSHOT   TABLE          SampleBigquery.mytablesnapshot\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/SampleBigquery\\/tables\\/mytablesnapshot\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\",\"priority\":\"QUERY_INTERACTIVE\",\"statementType\":\"DROP_SNAPSHOT_TABLE\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-04-30T08:47:24.588Z\",\"startTime\":\"2022-04-30T08:47:24.695Z\",\"endTime\":\"2022-04-30T08:47:24.861Z\",\"queryStats\":{}}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"j5lj0ue29qxt\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-04-30T08:47:24.867896Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1651308444588-project-sccd:bquxjob_7d79c6a8_18079a45a9d\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-04-30T08:47:25.055265124Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:SampleBigquery", sqlquery.getDbName());
		assertEquals("drop snapshot table", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("mytablesnapshot", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("DROP        SNAPSHOT   TABLE          SampleBigquery.mytablesnapshot", sqlquery.getData().getConstruct().fullSql);
		assertEquals("DROP        SNAPSHOT   TABLE          SampleBigquery.mytablesnapshot", sqlquery.getData().getConstruct().redactedSensitiveDataSql);
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordMetaDataWithSchema() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.5.202\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/101.0.4951.41 Safari\\/537.36 Edg\\/101.0.1210.32,gzip(gfe),gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_66d45f65_180bb95d0e0\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_66d45f65_180bb95d0e0\",\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"query\":\"CREATE    schema    testInfo.Customertables\\r\\n(\\r\\nx INT64 OPTIONS(description=\\\"An optional INTEGER field\\\"),\\r\\ny STRUCT<\\r\\nb BOOL\\r\\n>\\r\\n)\\r\\nOPTIONS(\\r\\nexpiration_timestamp=TIMESTAMP \\\"2023-01-01 00:00:00 UTC\\\",\\r\\ndescription=\\\"a table that expires in 2023\\\",\\r\\nlabels=[(\\\"org_unit\\\", \\\"development\\\")]\\r\\n)\\r\\n\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/testInfo\\/tables\\/CustomerRegion\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\",\"priority\":\"QUERY_INTERACTIVE\",\"statementType\":\"CREATE_TABLE\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-05-13T04:06:40.926Z\",\"startTime\":\"2022-05-13T04:06:41.018Z\",\"endTime\":\"2022-05-13T04:06:41.184Z\",\"queryStats\":{}}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-wql3brdxnoq\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-05-13T04:06:41.192811Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1652414800926-project-sccd:bquxjob_66d45f65_180bb95d0e0\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-05-13T04:06:41.959826021Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:Customertables", sqlquery.getDbName());
		assertEquals("create schema", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("Customertables", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordMetaDataWithJoin() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"148.64.12.11\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.63 Safari\\/537.36 Edg\\/102.0.1245.30,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_453d23dc_1812cf30b49\",\"metadata\":{\"jobChange\":{\"job\":{\"jobConfig\":{\"queryConfig\":{\"query\":\"WITH Words AS (\\r\\nSELECT\\r\\nCOLLATE('a', 'und:ci') AS char1,\\r\\nCOLLATE('Z', 'und:ci') AS char2\\r\\n)\\r\\nSELECT ( Words.char1 < Words.char2 ) AS a_less_than_Z\\r\\nFROM Words;\",\"priority\":\"QUERY_INTERACTIVE\",\"writeDisposition\":\"WRITE_TRUNCATE\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/_1f1c36c3ea0388eeb7a982fdeb3f5cbaed5b427a\\/tables\\/anon6fee11202d41720b3bda37fa7375724de3fb0721\",\"statementType\":\"SELECT\"},\"type\":\"QUERY\"},\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_453d23dc_1812cf30b49\",\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"queryStats\":{\"cacheHit\":true},\"endTime\":\"2022-06-04T04:25:56.667Z\",\"createTime\":\"2022-06-04T04:25:56.589Z\",\"startTime\":\"2022-06-04T04:25:56.604Z\"}},\"after\":\"DONE\"},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-w6nbcpe3seb0\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"location\":\"US\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-06-04T04:25:56.704461Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1654316756589-project-sccd:bquxjob_453d23dc_1812cf30b49\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-06-04T04:25:56.833681466Z\"}";
		BigQueryGuardiumFilter filter = getBigQueryFilterConnector(gcpString2);
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
	public void testParseRecordMetaDataWithNot() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{\"code\":5,\"message\":\"Not found: Table project-sccd:BigQueryE2E.Customer1\"},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.166.41\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.63 Safari\\/537.36 Edg\\/102.0.1245.39,gzip(gfe),gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_6cc6c6a7_1816192f2d4\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_6cc6c6a7_1816192f2d4\",\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"query\":\"Drop table BigQueryE2E.Customer1\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/BigQueryE2E\\/tables\\/Customer1\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\",\"priority\":\"QUERY_INTERACTIVE\",\"statementType\":\"DROP_TABLE\"}},\"jobStatus\":{\"jobState\":\"DONE\",\"errorResult\":{\"code\":5,\"message\":\"Not found: Table project-sccd:BigQueryE2E.Customer1\"},\"errors\":[{\"code\":5,\"message\":\"Not found: Table project-sccd:BigQueryE2E.Customer1\"}]},\"jobStats\":{\"createTime\":\"2022-06-14T09:40:55.903Z\",\"startTime\":\"2022-06-14T09:40:55.981Z\",\"endTime\":\"2022-06-14T09:40:56.084Z\",\"queryStats\":{}}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-ef7z5ee4unnu\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-06-14T09:40:56.089285Z\",\"severity\":\"ERROR\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1655199655903-project-sccd:bquxjob_6cc6c6a7_1816192f2d4\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-06-14T09:40:56.337931474Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:", sqlquery.getDbName());
		assertEquals("SQL_ERROR", sqlquery.getException().getExceptionTypeId());
		assertEquals("Not found: Table project-sccd:BigQueryE2E.Customer1", sqlquery.getException().getDescription());
		assertEquals("Drop table BigQueryE2E.Customer1", sqlquery.getException().getSqlString());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordMetaDataWith() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"136.185.149.223\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.51 Safari\\/537.36 Edg\\/99.0.1150.39,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_2efa9cd5_17fcfe7b26f\",\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\",\"jobChange\":{\"job\":{\"jobStatus\":{\"jobState\":\"DONE\"},\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_2efa9cd5_17fcfe7b26f\",\"jobConfig\":{\"queryConfig\":{\"statementType\":\"SELECT\",\"priority\":\"QUERY_INTERACTIVE\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/_1b12d4043516e7fe556c67c37894adb7c0c3588b\\/tables\\/anon0d36a80a5698fbaec071061444ad0e4a4d2d2e24\",\"writeDisposition\":\"WRITE_TRUNCATE\",\"query\":\"WITH Values AS ( SELECT 1 x, 'a' y UNION ALL SELECT 1 x, 'b' y UNION ALL SELECT 2 x, 'a' y UNION ALL SELECT 2 x, 'c' y ) SELECT x, ARRAY_AGG(y) as array_agg FROM Values GROUP BY x\"},\"type\":\"QUERY\"},\"jobStats\":{\"queryStats\":{\"referencedTables\":[\"projects\\/project-sccd\\/datasets\\/Newdatademo\\/tables\\/demoibm\"],\"billingTier\":1,\"totalProcessedBytes\":\"28\",\"outputRowCount\":\"2\",\"totalBilledBytes\":\"10485760\"},\"endTime\":\"2022-03-28T09:42:23.627Z\",\"createTime\":\"2022-03-28T09:42:23.237Z\",\"startTime\":\"2022-03-28T09:42:23.380Z\",\"totalSlotMs\":\"56\"}},\"after\":\"DONE\"}}},\"insertId\":\"-uso33ze1h99r\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-03-28T09:42:23.630852Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1648460543237-project-sccd:bquxjob_2efa9cd5_17fcfe7b26f\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-03-28T09:42:23.997320883Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:", sqlquery.getDbName());
		assertEquals("with ", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("Values", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("WITH Values AS ( SELECT 1 x, 'a' y UNION ALL SELECT 1 x, 'b' y UNION ALL SELECT 2 x, 'a' y UNION ALL SELECT 2 x, 'c' y ) SELECT x, ARRAY_AGG(y) as array_agg FROM Values GROUP BY x" , sqlquery.getData().getConstruct().fullSql);
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordUNNEST() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"136.185.149.223\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.51 Safari\\/537.36 Edg\\/99.0.1150.39,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_2efa9cd5_17fcfe7b26f\",\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\",\"jobChange\":{\"job\":{\"jobStatus\":{\"jobState\":\"DONE\"},\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_2efa9cd5_17fcfe7b26f\",\"jobConfig\":{\"queryConfig\":{\"statementType\":\"SELECT\",\"priority\":\"QUERY_INTERACTIVE\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/_1b12d4043516e7fe556c67c37894adb7c0c3588b\\/tables\\/anon0d36a80a5698fbaec071061444ad0e4a4d2d2e24\",\"writeDisposition\":\"WRITE_TRUNCATE\",\"query\":\"Select ANY_VALUE(fruit) as any_value from UNNEST([\\\"apple\\\",\\\"banana\\\",\\\"pear\\\"]) as fruit\"},\"type\":\"QUERY\"},\"jobStats\":{\"queryStats\":{\"referencedTables\":[\"projects\\/project-sccd\\/datasets\\/Newdatademo\\/tables\\/demoibm\"],\"billingTier\":1,\"totalProcessedBytes\":\"28\",\"outputRowCount\":\"2\",\"totalBilledBytes\":\"10485760\"},\"endTime\":\"2022-03-28T09:42:23.627Z\",\"createTime\":\"2022-03-28T09:42:23.237Z\",\"startTime\":\"2022-03-28T09:42:23.380Z\",\"totalSlotMs\":\"56\"}},\"after\":\"DONE\"}}},\"insertId\":\"-uso33ze1h99r\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-03-28T09:42:23.630852Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1648460543237-project-sccd:bquxjob_2efa9cd5_17fcfe7b26f\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-03-28T09:42:23.997320883Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:", sqlquery.getDbName());
		assertEquals("user@test.com", sqlquery.getAppUserName());
		assertEquals("Select ANY_VALUE(fruit) as any_value from UNNEST([ \"apple \", \"banana \", \"pear \"]) as fruit", sqlquery.getData().getConstruct().fullSql);
		assertEquals("select", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordCreateSchema() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"136.185.149.223\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.51 Safari\\/537.36 Edg\\/99.0.1150.39,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_2efa9cd5_17fcfe7b26f\",\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\",\"jobChange\":{\"job\":{\"jobStatus\":{\"jobState\":\"DONE\"},\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_2efa9cd5_17fcfe7b26f\",\"jobConfig\":{\"queryConfig\":{\"statementType\":\"SELECT\",\"priority\":\"QUERY_INTERACTIVE\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/_1b12d4043516e7fe556c67c37894adb7c0c3588b\\/tables\\/anon0d36a80a5698fbaec071061444ad0e4a4d2d2e24\",\"writeDisposition\":\"WRITE_TRUNCATE\",\"query\":\"CREATE SCHEMA `project-sccd.my9dataset` OPTIONS( location= \\\"us \\\", default_table_expiration_days=3.75, labels=[( \\\"label1 \\\", \\\"value1 \\\"),(\\\"label2 \\\", \\\"value2\\\")])\"},\"type\":\"QUERY\"},\"jobStats\":{\"queryStats\":{\"referencedTables\":[\"projects\\/project-sccd\\/datasets\\/Newdatademo\\/tables\\/demoibm\"],\"billingTier\":1,\"totalProcessedBytes\":\"28\",\"outputRowCount\":\"2\",\"totalBilledBytes\":\"10485760\"},\"endTime\":\"2022-03-28T09:42:23.627Z\",\"createTime\":\"2022-03-28T09:42:23.237Z\",\"startTime\":\"2022-03-28T09:42:23.380Z\",\"totalSlotMs\":\"56\"}},\"after\":\"DONE\"}}},\"insertId\":\"-uso33ze1h99r\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"US\"}},\"timestamp\":\"2022-03-28T09:42:23.630852Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1648460543237-project-sccd:bquxjob_2efa9cd5_17fcfe7b26f\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-03-28T09:42:23.997320883Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("CREATE SCHEMA `project-sccd.my9dataset` OPTIONS( location=  \"us  \", default_table_expiration_days=3.75, labels=[(  \"label1  \",  \"value1  \"),( \"label2  \",  \"value2 \")])", sqlquery.getData().getConstruct().fullSql);
		assertEquals("CREATE SCHEMA `project-sccd.my9dataset` OPTIONS( location=  ?, default_table_expiration_days=?.?, labels=[(  ?,  ?),( ?,  ?)])", sqlquery.getData().getConstruct().redactedSensitiveDataSql);
		assertEquals("project-sccd:my9dataset", sqlquery.getDbName());
		assertEquals("create schema", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("my9dataset", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	public static JsonObject parseGuardiumObject(JsonObject inputJson) {
		final JsonObject protoPayload = inputJson.get(ApplicationConstants.PROTO_PAYLOAD).getAsJsonObject();
		return protoPayload;
	}

	@Test
	public void testParseRecordSelectObjectVerbBlank() {
		final String gcpString2 = "{\\\"protoPayload\\\":{\\\"@type\\\":\\\"type.googleapis.com\\\\/google.cloud.audit.AuditLog\\\",\\\"status\\\":{},\\\"authenticationInfo\\\":{\\\"principalEmail\\\":\\\"user@test.com\\\"},\\\"requestMetadata\\\":{\\\"callerIp\\\":\\\"168.149.187.11\\\",\\\"callerSuppliedUserAgent\\\":\\\"Mozilla\\\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\\\/537.36 (KHTML, like Gecko) Chrome\\\\/99.0.4844.84 Safari\\\\/537.36,gzip(gfe),gzip(gfe)\\\",\\\"requestAttributes\\\":{},\\\"destinationAttributes\\\":{}},\\\"serviceName\\\":\\\"bigquery.googleapis.com\\\",\\\"methodName\\\":\\\"google.cloud.bigquery.v2.JobService.InsertJob\\\",\\\"authorizationInfo\\\":[{\\\"resource\\\":\\\"projects\\\\/project-sccd\\\",\\\"permission\\\":\\\"bigquery.jobs.create\\\",\\\"granted\\\":true,\\\"resourceAttributes\\\":{}}],\\\"resourceName\\\":\\\"projects\\\\/project-sccd\\\\/jobs\\\\/bquxjob_68d3c23e_18049d458fc\\\",\\\"metadata\\\":{\\\"jobChange\\\":{\\\"job\\\":{\\\"jobConfig\\\":{\\\"queryConfig\\\":{\\\"query\\\":\\\"create schema  `project-sccd.BigQueryQADS.userdetailQW`;\\\",\\\"createDisposition\\\":\\\"CREATE_IF_NEEDED\\\",\\\"priority\\\":\\\"QUERY_INTERACTIVE\\\",\\\"writeDisposition\\\":\\\"WRITE_EMPTY\\\"},\\\"type\\\":\\\"QUERY\\\"},\\\"jobStatus\\\":{\\\"errors\\\":[{\\\"code\\\":3,\\\"message\\\":\\\"Invalid project ID 'project-sccd.BigQueryQADS'. Project IDs must contain 6-63 lowercase letters, digits, or dashes. Some project IDs also include domain name separated by a colon. IDs must start with a letter and may not end with a dash.\\\"}],\\\"errorResult\\\":{},\\\"jobState\\\":\\\"DONE\\\"},\\\"jobStats\\\":{\\\"queryStats\\\":{},\\\"endTime\\\":\\\"2022-04-21T01:56:06.823Z\\\",\\\"createTime\\\":\\\"2022-04-21T01:56:06.803Z\\\",\\\"startTime\\\":\\\"2022-04-21T01:56:06.823Z\\\"},\\\"jobName\\\":\\\"projects\\\\/project-sccd\\\\/jobs\\\\/bquxjob_68d3c23e_18049d458fc\\\"},\\\"reason\\\":\\\"JOB_INSERT_REQUEST\\\"},\\\"@type\\\":\\\"type.googleapis.com\\\\/google.cloud.audit.BigQueryAuditMetadata\\\"}},\\\"insertId\\\":\\\"-oomtnhddawf\\\",\\\"resource\\\":{\\\"type\\\":\\\"bigquery_project\\\",\\\"labels\\\":{\\\"project_id\\\":\\\"project-sccd\\\",\\\"location\\\":\\\"US\\\"}},\\\"timestamp\\\":\\\"2022-04-21T01:56:06.875742Z\\\",\\\"severity\\\":\\\"INFO\\\",\\\"logName\\\":\\\"projects\\\\/project-sccd\\\\/logs\\\\/cloudaudit.googleapis.com%2Fdata_access\\\",\\\"operation\\\":{\\\"id\\\":\\\"1650506166803-project-sccd:bquxjob_68d3c23e_18049d458fc\\\",\\\"producer\\\":\\\"bigquery.googleapis.com\\\",\\\"first\\\":true},\\\"receiveTimestamp\\\":\\\"2022-04-21T01:56:06.916288378Z\\\"}";
		BigQueryGuardiumFilter filter = getBigQueryFilterConnector(gcpString2);
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
	public void testParseRecordMetaDataWithTableInsertrequest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"103.165.15.173\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/105.0.0.0 Safari\\/537.36,gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.TableService.InsertTable\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/datasets\\/9622Dataset\",\"permission\":\"bigquery.tables.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/datasets\\/9622Dataset\\/tables\\/9622Table\",\"metadata\":{\"tableCreation\":{\"table\":{\"schemaJson\":\"{\\n}\",\"updateTime\":\"2022-09-06T09:33:48.043Z\",\"tableName\":\"projects\\/project-sccd\\/datasets\\/9622Dataset\\/tables\\/9622Table\",\"createTime\":\"2022-09-06T09:33:47.931Z\"},\"reason\":\"TABLE_INSERT_REQUEST\"},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-iv71qld7oer\",\"resource\":{\"type\":\"bigquery_dataset\",\"labels\":{\"dataset_id\":\"9622Dataset\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-09-06T09:33:48.071213Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-09-06T09:33:48.937954238Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:9622Dataset", sqlquery.getDbName());
		assertEquals("create table", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("9622Table", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("create table 9622Dataset.9622Table", sqlquery.getData().getConstruct().fullSql);
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordMetaDataWithDeleteTableRequest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"103.165.15.173\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/105.0.0.0 Safari\\/537.36,gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.TableService.DeleteTable\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/datasets\\/9622Dataset\\/tables\\/9622Table\",\"permission\":\"bigquery.tables.delete\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/datasets\\/9622Dataset\\/tables\\/9622Table\",\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\",\"tableDeletion\":{\"reason\":\"TABLE_DELETE_REQUEST\"}}},\"insertId\":\"-naw5i4d7vsk\",\"resource\":{\"type\":\"bigquery_dataset\",\"labels\":{\"dataset_id\":\"9622Dataset\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-09-06T09:34:01.912415Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-09-06T09:34:02.837090160Z\"}";
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:9622Dataset", sqlquery.getDbName());
		assertEquals("drop table", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("9622Table", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("drop table 9622Dataset.9622Table", sqlquery.getData().getConstruct().fullSql);
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testParseRecordUploadCsvCreateTableLog() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"103.165.15.80\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/105.0.0.0 Safari\\/537.36,gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_29acf1c6_18311523980\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_29acf1c6_18311523980\",\"jobConfig\":{\"type\":\"IMPORT\",\"loadConfig\":{\"schemaJson\":\"{\\n}\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/mydataset\\/tables\\/oldtable\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-09-06T05:42:28.990Z\",\"startTime\":\"2022-09-06T05:42:29.270Z\",\"endTime\":\"2022-09-06T05:42:30.621Z\",\"loadStats\":{\"totalOutputBytes\":\"216\"},\"totalSlotMs\":\"103\",\"reservationUsage\":[{\"name\":\"default-pipeline\",\"slotMs\":\"103\"}]}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-eovwo0dsdv8\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"us-central1\"}},\"timestamp\":\"2022-09-06T05:42:30.670452Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1662442948990-project-sccd:bquxjob_29acf1c6_18311523980\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-09-06T05:42:31.078574953Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("project-sccd:mydataset", sqlquery.getDbName());
		assertEquals("create table", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("oldtable", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		assertEquals("create table mydataset.oldtable", sqlquery.getData().getConstruct().fullSql);
		assertEquals("create table mydataset.oldtable", sqlquery.getData().getConstruct().redactedSensitiveDataSql);
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testfilterSessionLocator() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"112.155.11.12\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/105.0.0.0 Safari\\/537.36,gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_29acf1c6_18311523980\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_29acf1c6_18311523980\",\"jobConfig\":{\"type\":\"IMPORT\",\"loadConfig\":{\"schemaJson\":\"{\\n}\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/mydataset\\/tables\\/oldtable\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-09-06T05:42:28.990Z\",\"startTime\":\"2022-09-06T05:42:29.270Z\",\"endTime\":\"2022-09-06T05:42:30.621Z\",\"loadStats\":{\"totalOutputBytes\":\"216\"},\"totalSlotMs\":\"103\",\"reservationUsage\":[{\"name\":\"default-pipeline\",\"slotMs\":\"103\"}]}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-eovwo0dsdv8\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"us-central1\"}},\"timestamp\":\"2022-09-06T05:42:30.670452Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1662442948990-project-sccd:bquxjob_29acf1c6_18311523980\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-09-06T05:42:31.078574953Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("112.155.11.12", sqlquery.getSessionLocator().getClientIp());
		assertEquals(-1, sqlquery.getSessionLocator().getClientPort());
		assertEquals("0.0.0.0", sqlquery.getSessionLocator().getServerIp());
		assertEquals(-1, sqlquery.getSessionLocator().getServerPort());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testfilterSessionLocatorwithIpv6() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"2409:4064:2291:455c:e189:21f3:2d20:6ec5\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/94.0.4606.81 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{},\"destinationAttributes\":{}},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_77ace2e_17ce89f4992\",\"metadata\":{\"jobChange\":{\"reason\":\"JOB_INSERT_REQUEST\",\"job\":{\"jobConfig\":{\"type\":\"QUERY\",\"queryConfig\":{\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/big_Query_Data_Set\\/tables\\/person\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\",\"statementType\":\"INSERT\",\"query\":\"insert into project-sccd.big_Query_Data_Set.person (id, first_name, last_name, email)\\r\\nVALUES('36','Anand', 'Kumar', 'user@test.com')\",\"priority\":\"QUERY_INTERACTIVE\"}},\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_77ace2e_17ce89f4992\",\"jobStatus\":{\"jobState\":\"RUNNING\"},\"jobStats\":{\"queryStats\":{},\"createTime\":\"2021-11-04T01:45:51.435Z\",\"startTime\":\"2021-11-04T01:45:51.532Z\"}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-aj7t5te11j3t\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"location\":\"US\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2021-11-04T01:45:51.570238Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1635990351435-project-sccd:bquxjob_77ace2e_17ce89f4992\",\"producer\":\"bigquery.googleapis.com\",\"first\":true},\"receiveTimestamp\":\"2021-11-04T01:45:51.713484313Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals(true, sqlquery.getSessionLocator().isIpv6());
		assertEquals("2409:4064:2291:455c:e189:21f3:2d20:6ec5", sqlquery.getSessionLocator().getClientIpv6());
		assertEquals("0000:0000:0000:0000:0000:FFFF:0000:0000", sqlquery.getSessionLocator().getServerIpv6());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testfilterAccessor() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"103.165.15.80\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/105.0.0.0 Safari\\/537.36,gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_29acf1c6_18311523980\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_29acf1c6_18311523980\",\"jobConfig\":{\"type\":\"IMPORT\",\"loadConfig\":{\"schemaJson\":\"{\\n}\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/mydataset\\/tables\\/oldtable\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-09-06T05:42:28.990Z\",\"startTime\":\"2022-09-06T05:42:29.270Z\",\"endTime\":\"2022-09-06T05:42:30.621Z\",\"loadStats\":{\"totalOutputBytes\":\"216\"},\"totalSlotMs\":\"103\",\"reservationUsage\":[{\"name\":\"default-pipeline\",\"slotMs\":\"103\"}]}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-eovwo0dsdv8\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"us-central1\"}},\"timestamp\":\"2022-09-06T05:42:30.670452Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1662442948990-project-sccd:bquxjob_29acf1c6_18311523980\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-09-06T05:42:31.078574953Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("user@test.com", sqlquery.getAccessor().getDbUser());
		assertEquals("project-sccd_bigquery.googleapis.com", sqlquery.getAccessor().getServerHostName());
		assertEquals("BigQuery(GCP)", sqlquery.getAccessor().getDbProtocol());
		assertEquals("BigQuery", sqlquery.getAccessor().getServerType());
		assertEquals("project-sccd:mydataset", sqlquery.getAccessor().getServiceName());
		assertEquals("CONSTRUCT", sqlquery.getAccessor().getDataType());
		assertEquals("FREE_TEXT", sqlquery.getAccessor().getLanguage());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testfilterTimstamp() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"103.165.15.80\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/105.0.0.0 Safari\\/537.36,gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_29acf1c6_18311523980\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_29acf1c6_18311523980\",\"jobConfig\":{\"type\":\"IMPORT\",\"loadConfig\":{\"schemaJson\":\"{\\n}\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/mydataset\\/tables\\/oldtable\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-09-06T05:42:28.990Z\",\"startTime\":\"2022-09-06T05:42:29.270Z\",\"endTime\":\"2022-09-06T05:42:30.621Z\",\"loadStats\":{\"totalOutputBytes\":\"216\"},\"totalSlotMs\":\"103\",\"reservationUsage\":[{\"name\":\"default-pipeline\",\"slotMs\":\"103\"}]}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-eovwo0dsdv8\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"us-central1\"}},\"timestamp\":\"2022-09-06T05:42:30.670452Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1662442948990-project-sccd:bquxjob_29acf1c6_18311523980\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-09-06T05:42:31.078574953Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals(Long.parseLong("1662442950670"), sqlquery.getTime().getTimstamp());
		assertEquals(0, sqlquery.getTime().getMinDst());
		assertEquals(0, sqlquery.getTime().getMinOffsetFromGMT());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	@Test
	public void testfilterDataConstruct() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"user@test.com\"},\"requestMetadata\":{\"callerIp\":\"103.165.15.80\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/105.0.0.0 Safari\\/537.36,gzip(gfe)\"},\"serviceName\":\"bigquery.googleapis.com\",\"methodName\":\"google.cloud.bigquery.v2.JobService.InsertJob\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\",\"permission\":\"bigquery.jobs.create\",\"granted\":true}],\"resourceName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_29acf1c6_18311523980\",\"metadata\":{\"jobChange\":{\"after\":\"DONE\",\"job\":{\"jobName\":\"projects\\/project-sccd\\/jobs\\/bquxjob_29acf1c6_18311523980\",\"jobConfig\":{\"type\":\"IMPORT\",\"loadConfig\":{\"schemaJson\":\"{\\n}\",\"destinationTable\":\"projects\\/project-sccd\\/datasets\\/mydataset\\/tables\\/oldtable\",\"createDisposition\":\"CREATE_IF_NEEDED\",\"writeDisposition\":\"WRITE_EMPTY\"}},\"jobStatus\":{\"jobState\":\"DONE\"},\"jobStats\":{\"createTime\":\"2022-09-06T05:42:28.990Z\",\"startTime\":\"2022-09-06T05:42:29.270Z\",\"endTime\":\"2022-09-06T05:42:30.621Z\",\"loadStats\":{\"totalOutputBytes\":\"216\"},\"totalSlotMs\":\"103\",\"reservationUsage\":[{\"name\":\"default-pipeline\",\"slotMs\":\"103\"}]}}},\"@type\":\"type.googleapis.com\\/google.cloud.audit.BigQueryAuditMetadata\"}},\"insertId\":\"-eovwo0dsdv8\",\"resource\":{\"type\":\"bigquery_project\",\"labels\":{\"project_id\":\"project-sccd\",\"location\":\"us-central1\"}},\"timestamp\":\"2022-09-06T05:42:30.670452Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"1662442948990-project-sccd:bquxjob_29acf1c6_18311523980\",\"producer\":\"bigquery.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-09-06T05:42:31.078574953Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		final JsonObject BigqueryJson = JsonParser.parseString(gcpString2).getAsJsonObject();
		Record sqlquery = Parser.parseRecord(BigqueryJson);
		assertEquals("create table", sqlquery.getData().getConstruct().sentences.get(0).getVerb());
		assertEquals("collection", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());
		assertEquals("oldtable", sqlquery.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
		TestMatchListener matchListener = new TestMatchListener();
		assertEquals(0, matchListener.getMatchCount());
		assertEquals(1, events.size());
	}

	public Collection<co.elastic.logstash.api.Event> getEvents(String gcpStringRequest) {
		BigQueryGuardiumFilter filter = getBigQueryFilterConnector(gcpStringRequest);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", gcpStringRequest);
		Collection<co.elastic.logstash.api.Event> events = filter.filter(Collections.singletonList(e), matchListener);
		return events;
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