/*

Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0

*/
package com.ibm.guardium.firebase;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.logstash.Event;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.PluginConfigSpec;

public class FireBaseGuardiumFilterTest {

	@Test
	public void configSchemaTest() {
		final String gcpString2 = "";
		FireBaseGuardiumFilter filter = getFireBaseFilterConnector(gcpString2);
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
		final String gcpString2 = "";
		FireBaseGuardiumFilter filter = getFireBaseFilterConnector(gcpString2);
		String id = filter.getId();
		assertNotNull(id);
	}

	@Test
	public void filterEventTest() {
		final String gcpString2 = "{\"protoPayload\": {\"@type\": \"type.googleapis.com/google.cloud.audit.AuditLog\",\"status\": {},\"authenticationInfo\": {\r\n"
				+ "        \"principalEmail\": \"javvadi.prasanthi@hcl.com\"},\r\n" + "      \"requestMetadata\": {\r\n"
				+ "        \"callerIp\": \"168.149.166.27\",\r\n"
				+ "        \"callerSuppliedUserAgent\": \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36,gzip(gfe),gzip(gfe)\",\r\n"
				+ "        \"requestAttributes\": {\r\n" + "          \"time\": \"2022-02-18T06:23:11.578188Z\",\r\n"
				+ "          \"auth\": {}\r\n" + "        },\r\n" + "        \"destinationAttributes\": {}\r\n"
				+ "      },\r\n" + "      \"serviceName\": \"firebasedatabase.googleapis.com\",\r\n"
				+ "      \"methodName\": \"google.firebase.database.v1beta.RealtimeDatabaseService.ListDatabaseInstances\",\r\n"
				+ "      \"authorizationInfo\": [\r\n" + "        {\r\n"
				+ "          \"resource\": \"projects/1065340084898\",\r\n"
				+ "          \"permission\": \"firebasedatabase.instances.list\",\r\n"
				+ "          \"granted\": true,\r\n" + "          \"resourceAttributes\": {}\r\n" + "        }\r\n"
				+ "      ],\r\n" + "      \"resourceName\": \"projects/1065340084898/locations/-\",\r\n"
				+ "      \"request\": {\r\n" + "        \"parent\": \"projects/1065340084898/locations/-\",\r\n"
				+ "        \"@type\": \"type.googleapis.com/google.firebase.database.v1beta.ListDatabaseInstancesRequest\"\r\n"
				+ "      }\r\n" + "    },\r\n" + "    \"insertId\": \"ezt8z4d1rs2\",\r\n" + "    \"resource\": {\r\n"
				+ "      \"type\": \"audited_resource\",\r\n" + "      \"labels\": {\r\n"
				+ "        \"service\": \"firebasedatabase.googleapis.com\",\r\n"
				+ "        \"project_id\": \"project-sccd\",\r\n"
				+ "        \"method\": \"google.firebase.database.v1beta.RealtimeDatabaseService.ListDatabaseInstances\"\r\n"
				+ "      }\r\n" + "    },\r\n" + "    \"timestamp\": \"2022-02-18T06:23:11.342653Z\",\r\n"
				+ "    \"severity\": \"INFO\",\r\n"
				+ "    \"logName\": \"projects/project-sccd/logs/cloudaudit.googleapis.com%2Fdata_access\",\r\n"
				+ "    \"receiveTimestamp\": \"2022-02-18T06:23:12.175300646Z\"\r\n" + "  }";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}
	@Test
	public void reenableEventTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"aayushi.palod@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"122.161.51.105\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.124 Safari\\/537.36 Edg\\/102.0.1245.44,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-22T09:37:05.834447Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firebase.database.v1beta.RealtimeDatabaseService.ReenableDatabaseInstance\",\"authorizationInfo\":[{\"resource\":\"projects\\/1065340084898\",\"permission\":\"firebasedatabase.instances.reenable\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-26ae8-aayushi\",\"request\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.ReenableDatabaseInstanceRequest\",\"name\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-26ae8-aayushi\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.DatabaseInstance\",\"type\":\"USER_DATABASE\",\"project\":\"projects\\/1065340084898\",\"databaseUrl\":\"https:\\/\\/project-sccd-26ae8-aayushi.firebaseio.com\",\"name\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-26ae8-aayushi\"}},\"insertId\":\"9fnb00d2tgw\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"service\":\"firebasedatabase.googleapis.com\",\"project_id\":\"project-sccd\",\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.ReenableDatabaseInstance\"}},\"timestamp\":\"2022-06-22T09:37:05.375458Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-06-22T09:37:06.134731204Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}
	@Test
	public void disableEventTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"aayushi.palod@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"122.161.51.105\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.124 Safari\\/537.36 Edg\\/102.0.1245.44,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-22T09:47:45.496736Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firebase.database.v1beta.RealtimeDatabaseService.DisableDatabaseInstance\",\"authorizationInfo\":[{\"resource\":\"projects\\/1065340084898\",\"permission\":\"firebasedatabase.instances.disable\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-26ae8-aayushi\",\"request\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.DisableDatabaseInstanceRequest\",\"name\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-26ae8-aayushi\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.DatabaseInstance\",\"type\":\"USER_DATABASE\",\"project\":\"projects\\/1065340084898\",\"databaseUrl\":\"https:\\/\\/project-sccd-26ae8-aayushi.firebaseio.com\",\"name\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-26ae8-aayushi\"}},\"insertId\":\"-3lt3ghd2d0i\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"service\":\"firebasedatabase.googleapis.com\",\"project_id\":\"project-sccd\",\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.DisableDatabaseInstance\"}},\"timestamp\":\"2022-06-22T09:47:45.158493Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-06-22T09:47:45.730539225Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}
	@Test
	public void createEventTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"aayushi.palod@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"122.161.51.187\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.63 Safari\\/537.36 Edg\\/102.0.1245.39,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-17T08:32:35.974458Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"authorizationInfo\":[{\"resource\":\"projects\\/1065340084898\",\"permission\":\"firebasedatabase.instances.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/1065340084898\\/locations\\/us-central1\",\"request\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.CreateDatabaseInstanceRequest\",\"validateOnly\":true,\"parent\":\"projects\\/1065340084898\\/locations\\/us-central1\",\"databaseInstance\":{\"type\":\"USER_DATABASE\"},\"databaseId\":\"project-sccd-c1084\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.DatabaseInstance\",\"project\":\"projects\\/1065340084898\",\"name\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-c1084\",\"databaseUrl\":\"https:\\/\\/project-sccd-c1084.firebaseio.com\",\"type\":\"USER_DATABASE\"}},\"insertId\":\"-4zlumed2d4u\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"service\":\"firebasedatabase.googleapis.com\",\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-06-17T08:32:35.730041Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-06-17T08:32:36.519502034Z\"}"; 
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}
	@Test
	public void deleteEventTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"aayushi.palod@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"122.161.51.105\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.124 Safari\\/537.36 Edg\\/102.0.1245.44,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-22T09:48:25.062260Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firebase.database.v1beta.RealtimeDatabaseService.DeleteDatabaseInstance\",\"authorizationInfo\":[{\"resource\":\"projects\\/1065340084898\",\"permission\":\"firebasedatabase.instances.delete\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-26ae8-aayushi\",\"request\":{\"name\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-26ae8-aayushi\",\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.DeleteDatabaseInstanceRequest\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.DatabaseInstance\",\"project\":\"projects\\/1065340084898\",\"type\":\"USER_DATABASE\",\"databaseUrl\":\"https:\\/\\/project-sccd-26ae8-aayushi.firebaseio.com\",\"name\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-26ae8-aayushi\"}},\"insertId\":\"6dvc0od2m42\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"project_id\":\"project-sccd\",\"service\":\"firebasedatabase.googleapis.com\",\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.DeleteDatabaseInstance\"}},\"timestamp\":\"2022-06-22T09:48:24.772819Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-06-22T09:48:25.351757786Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}

	@Test
	public void filterInvalidJsonEventTest() {
		final String gcpString2 = "{\"protoPayload\":\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"brijeshyadav1979@gmail.com\"},\"requestMetadata\":{\"callerIp\":\"163.53.85.108\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/97.0.4692.99 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-07T18:22:40.885763Z\",\"reason\":\"8uSywAYxWi9GaXJlc3RvcmUgd2F0Y2ggZm9yIGxvbmcgcnVubmluZyBzdHJlYW1pbmcgcnBjLg\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firestore.v1.Firestore.Listen\",\"authorizationInfo\":[{\"resource\":\"projects\\/guarduim\\/databases\\/\",\"permission\":\"datastore.entities.get\",\"granted\":true,\"resourceAttributes\":{}},{\"resource\":\"projects\\/guarduim\\/databases\\/\",\"permission\":\"datastore.entities.list\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/guarduim\\/databases\\/(default)\",\"request\":{\"addTarget\":{\"query\":{\"parent\":\"projects\\/guarduim\\/databases\\/(default)\\/documents\",\"structuredQuery\":{\"from\":[{\"collectionId\":\"collection55\"}],\"orderBy\":[{\"field\":{\"fieldPath\":\"__name__\"},\"direction\":\"ASCENDING\"}]}},\"targetId\":50},\"@type\":\"type.googleapis.com\\/google.firestore.v1.ListenRequest\"},\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.DatastoreServiceData\"}},\"insertId\":\"-4flla8df63i\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"service\":\"firestore.googleapis.com\",\"method\":\"google.firestore.v1.Firestore.Listen\",\"project_id\":\"guarduim\"}},\"timestamp\":\"2022-02-07T18:22:40.874854Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/guarduim\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"381d996b-ef01-4267-bbb3-0b13c7b2cdc1\",\"producer\":\"firestore.googleapis.com\"},\"receiveTimestamp\":\"2022-02-07T18:22:41.231100438Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}

	@Test
	public void filterNotFireStoreDBJsonEventTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"brijeshyadav1979@gmail.com\"},\"requestMetadata\":{\"callerIp\":\"163.53.85.108\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/97.0.4692.99 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-07T18:22:40.885763Z\",\"reason\":\"8uSywAYxWi9GaXJlc3RvcmUgd2F0Y2ggZm9yIGxvbmcgcnVubmluZyBzdHJlYW1pbmcgcnBjLg\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firestore.v1.Firestore.Listen\",\"authorizationInfo\":[{\"resource\":\"projects\\/guarduim\\/databases\\/\",\"permission\":\"datastore.entities.get\",\"granted\":true,\"resourceAttributes\":{}},{\"resource\":\"projects\\/guarduim\\/databases\\/\",\"permission\":\"datastore.entities.list\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/guarduim\\/databases\\/(default)\",\"request\":{\"addTarget\":{\"query\":{\"parent\":\"projects\\/guarduim\\/databases\\/(default)\\/documents\",\"structuredQuery\":{\"from\":[{\"collectionId\":\"collection55\"}],\"orderBy\":[{\"field\":{\"fieldPath\":\"__name__\"},\"direction\":\"ASCENDING\"}]}},\"targetId\":50},\"@type\":\"type.googleapis.com\\/google.firestore.v1.ListenRequest\"},\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.DatastoreServiceData\"}},\"insertId\":\"-4flla8df63i\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"service\":\"firestore123.googleapis.com\",\"method\":\"google.firestore.v1.Firestore.Listen\",\"project_id\":\"guarduim\"}},\"timestamp\":\"2022-02-07T18:22:40.874854Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/guarduim\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"381d996b-ef01-4267-bbb3-0b13c7b2cdc1\",\"producer\":\"firestore123.googleapis.com\"},\"receiveTimestamp\":\"2022-02-07T18:22:41.231100438Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}

	@Test
	public void filterErrorJsonEventTest() {
		final String gcpString2 = "{\"protoPayload\":\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{\"code\":1},\"authenticationInfo\":{\"principalEmail\":\"anubhav.kumar@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"168.149.187.116\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/97.0.4692.99 Safari\\/537.36 Edg\\/97.0.1072.69,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-08T15:32:46.433832Z\",\"reason\":\"8uSywAYxWi9GaXJlc3RvcmUgd2F0Y2ggZm9yIGxvbmcgcnVubmluZyBzdHJlYW1pbmcgcnBjLg\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firestore.googleapis.com\",\"methodName\":\"google.firestore.v1.Firestore.Listen\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/databases\\/\",\"permission\":\"datastore.entities.get\",\"granted\":true,\"resourceAttributes\":{}},{\"resource\":\"projects\\/project-sccd\\/databases\\/\",\"permission\":\"datastore.entities.list\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/databases\\/(default)\",\"request\":{\"@type\":\"type.googleapis.com\\/google.firestore.v1.ListenRequest\",\"addTarget\":{\"targetId\":10,\"query\":{\"structuredQuery\":{\"orderBy\":[{\"direction\":\"ASCENDING\",\"field\":{\"fieldPath\":\"__name__\"}}],\"from\":[{\"collectionId\":\"Feb012022\"}]},\"parent\":\"projects\\/project-sccd\\/databases\\/(default)\\/documents\"}}},\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.DatastoreServiceData\"}},\"insertId\":\"-9pka6kd65w0\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"method\":\"google.firestore.v1.Firestore.Listen\",\"service\":\"firestore.googleapis.com\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-02-08T15:32:46.422658Z\",\"severity\":\"ERROR\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"2e6d0077-a3ae-4cff-ac46-630eb1288175\",\"producer\":\"firestore.googleapis.com\",\"last\":true},\"receiveTimestamp\":\"2022-02-08T15:32:46.759519288Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}

	@Test
	public void parseExceptionTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{\"code\":3,\"message\":\"Error; please try again later.\"},\"authenticationInfo\":{\"principalEmail\":\"brijeshyadav1979@gmail.com\"},\"requestMetadata\":{\"callerIp\":\"163.53.85.108\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/97.0.4692.99 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-07T17:37:09.749129Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"authorizationInfo\":[{\"resource\":\"projects\\/493987525449\",\"permission\":\"firebasedatabase.instances.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/493987525449\\/locations\\/us-central1\",\"request\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.CreateDatabaseInstanceRequest\",\"parent\":\"projects\\/493987525449\\/locations\\/us-central1\",\"databaseId\":\"guarduim\",\"databaseInstance\":{\"type\":\"DEFAULT_DATABASE\"},\"validateOnly\":true}},\"insertId\":\"-p8vcupddj1e\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"service\":\"firebasedatabase.googleapis.com\",\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"project_id\":\"guarduim\"}},\"timestamp\":\"2022-02-07T17:37:09.731594Z\",\"severity\":\"ERROR\",\"logName\":\"projects\\/guarduim\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-02-07T17:37:10.444874877Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}

	@Test
	public void parseObjectAndVerbTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"meenakshi_b@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"223.233.72.13\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.51 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-03-17T03:42:10.393226Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"authorizationInfo\":[{\"resource\":\"projects\\/1065340084898\",\"permission\":\"firebasedatabase.instances.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/1065340084898\\/locations\\/us-central1\",\"request\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.CreateDatabaseInstanceRequest\",\"databaseInstance\":{\"type\":\"USER_DATABASE\"},\"validateOnly\":true,\"databaseId\":\"project-sccd-3b9f0\",\"parent\":\"projects\\/1065340084898\\/locations\\/us-central1\"},\"response\":{\"databaseUrl\":\"https:\\/\\/project-sccd-3b9f0.firebaseio.com\",\"type\":\"USER_DATABASE\",\"name\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-3b9f0\",\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.DatabaseInstance\",\"project\":\"projects\\/1065340084898\"}},\"insertId\":\"d1ic85d5ibs\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"service\":\"firebasedatabase.googleapis.com\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-03-17T03:42:09.713515Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-03-17T03:42:10.655731544Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}

	@Test
	public void parseHttpStatusTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"brijeshyadav1979@gmail.com\"},\"requestMetadata\":{\"callerIp\":\"163.53.85.108\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/97.0.4692.99 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-07T17:37:10.518189Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"authorizationInfo\":[{\"resource\":\"projects\\/493987525449\",\"permission\":\"firebasedatabase.instances.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/493987525449\\/locations\\/us-central1\",\"request\":{\"databaseInstance\":{\"type\":\"DEFAULT_DATABASE\"},\"parent\":\"projects\\/493987525449\\/locations\\/us-central1\",\"validateOnly\":true,\"databaseId\":\"guarduim-default-rtdb\",\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.CreateDatabaseInstanceRequest\"},\"response\":{\"type\":\"DEFAULT_DATABASE\",\"project\":\"projects\\/493987525449\",\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.DatabaseInstance\",\"databaseUrl\":\"https:\\/\\/guarduim-default-rtdb.firebaseio.com\",\"name\":\"projects\\/493987525449\\/locations\\/us-central1\\/instances\\/guarduim-default-rtdb\"}},\"insertId\":\"-p8vcupddj1g\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"project_id\":\"guarduim\",\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"service\":\"firebasedatabase.googleapis.com\"}},\"httpRequest\":{\"serverIp\":\"192.168.3.3\",\"labels\":{\"project_id\":\"guarduim\",\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"service\":\"firebasedatabase.googleapis.com\"}},\"timestamp\":\"2022-02-07T17:37:09.888653Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/guarduim\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-02-07T17:37:11.443170920Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}

	@Test
	public void parseIPV6Test() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"aayushi.palod@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"2001:0db8:85a3:0000:0000:8a2e:0370:7334\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.124 Safari\\/537.36 Edg\\/102.0.1245.44,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-22T09:37:05.834447Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firebase.database.v1beta.RealtimeDatabaseService.ReenableDatabaseInstance\",\"authorizationInfo\":[{\"resource\":\"projects\\/1065340084898\",\"permission\":\"firebasedatabase.instances.reenable\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-26ae8-aayushi\",\"request\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.ReenableDatabaseInstanceRequest\",\"name\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-26ae8-aayushi\"},\"response\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.DatabaseInstance\",\"type\":\"USER_DATABASE\",\"project\":\"projects\\/1065340084898\",\"databaseUrl\":\"https:\\/\\/project-sccd-26ae8-aayushi.firebaseio.com\",\"name\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-26ae8-aayushi\"}},\"insertId\":\"9fnb00d2tgw\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"service\":\"firebasedatabase.googleapis.com\",\"project_id\":\"project-sccd\",\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.ReenableDatabaseInstance\"}},\"timestamp\":\"2022-06-22T09:37:05.375458Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-06-22T09:37:06.134731204Z\"}";
		//final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"brijeshyadav1979@gmail.com\"},\"requestMetadata\":{\"callerIp\":\"2001:0db8:85a3:0000:0000:8a2e:0370:7334\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/97.0.4692.99 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-07T17:37:10.518189Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"authorizationInfo\":[{\"resource\":\"projects\\/493987525449\",\"permission\":\"firebasedatabase.instances.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/493987525449\\/locations\\/us-central1\",\"request\":{\"databaseInstance\":{\"type\":\"DEFAULT_DATABASE\"},\"parent\":\"projects\\/493987525449\\/locations\\/us-central1\",\"validateOnly\":true,\"databaseId\":\"guarduim-default-rtdb\",\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.CreateDatabaseInstanceRequest\"},\"response\":{\"type\":\"DEFAULT_DATABASE\",\"project\":\"projects\\/493987525449\",\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.DatabaseInstance\",\"databaseUrl\":\"https:\\/\\/guarduim-default-rtdb.firebaseio.com\",\"name\":\"projects\\/493987525449\\/locations\\/us-central1\\/instances\\/guarduim-default-rtdb\"}},\"insertId\":\"-p8vcupddj1g\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"project_id\":\"guarduim\",\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"service\":\"firebasedatabase.googleapis.com\"}},\"httpRequest\":{\"serverIp\":\"192.168.3.3\",\"labels\":{\"project_id\":\"guarduim\",\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"service\":\"firebasedatabase.googleapis.com\"}},\"timestamp\":\"2022-02-07T17:37:09.888653Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/guarduim\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-02-07T17:37:11.443170920Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}

	@Test
	public void parseRequestUnavailableTest() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"brijeshyadav1979@gmail.com\"},\"requestMetadata\":{\"callerIp\":\"163.53.85.108\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/97.0.4692.99 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-07T17:37:10.518189Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"authorizationInfo\":[{\"resource\":\"projects\\/493987525449\",\"permission\":\"firebasedatabase.instances.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/493987525449\\/locations\\/us-central1\",\"response\":{\"type\":\"DEFAULT_DATABASE\",\"project\":\"projects\\/493987525449\",\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.DatabaseInstance\",\"databaseUrl\":\"https:\\/\\/guarduim-default-rtdb.firebaseio.com\",\"name\":\"projects\\/493987525449\\/locations\\/us-central1\\/instances\\/guarduim-default-rtdb\"}},\"insertId\":\"-p8vcupddj1g\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"project_id\":\"guarduim\",\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"service\":\"firebasedatabase.googleapis.com\"}},\"httpRequest\":{\"serverIp\":\"192.168.3.3\",\"labels\":{\"project_id\":\"guarduim\",\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"service\":\"firebasedatabase.googleapis.com\"}},\"timestamp\":\"2022-02-07T17:37:09.888653Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/guarduim\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-02-07T17:37:11.443170920Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}

	@Test
	public void testNullDatabaseID() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{\"code\":3,\"message\":\"Error; please try again later.\"},\"authenticationInfo\":{\"principalEmail\":\"brijeshyadav1979@gmail.com\"},\"requestMetadata\":{\"callerIp\":\"163.53.85.108\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/97.0.4692.99 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-07T17:37:09.749129Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"authorizationInfo\":[{\"resource\":\"projects\\/493987525449\",\"permission\":\"firebasedatabase.instances.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/493987525449\\/locations\\/us-central1\",\"request\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.CreateDatabaseInstanceRequest\",\"parent\":\"projects\\/493987525449\\/locations\\/us-central1\",\"databaseInstance\":{\"type\":\"DEFAULT_DATABASE\"},\"validateOnly\":true}},\"insertId\":\"-p8vcupddj1e\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"service\":\"firebasedatabase.googleapis.com\",\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"project_id\":\"guarduim\"}},\"timestamp\":\"2022-02-07T17:37:09.731594Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/guarduim\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-02-07T17:37:10.444874877Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}

	@Test
	public void testNullStatus() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"brijeshyadav1979@gmail.com\"},\"requestMetadata\":{\"callerIp\":\"163.53.85.108\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/97.0.4692.99 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-07T17:37:09.749129Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"authorizationInfo\":[{\"resource\":\"projects\\/493987525449\",\"permission\":\"firebasedatabase.instances.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/493987525449\\/locations\\/us-central1\",\"request\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.CreateDatabaseInstanceRequest\",\"parent\":\"projects\\/493987525449\\/locations\\/us-central1\",\"databaseInstance\":{\"type\":\"DEFAULT_DATABASE\"},\"validateOnly\":true}},\"insertId\":\"-p8vcupddj1e\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"service\":\"firebasedatabase.googleapis.com\",\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"project_id\":\"guarduim\"}},\"timestamp\":\"2022-02-07T17:37:09.731594Z\",\"severity\":\"ERROR\",\"logName\":\"projects\\/guarduim\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-02-07T17:37:10.444874877Z\"}";
		FireBaseGuardiumFilter filter = getFireBaseFilterConnector(gcpString2);

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", gcpString2);
		Collection<co.elastic.logstash.api.Event> events = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(events);
	}

	@Test
	public void testNullLabel() {
		final String gcpString2 = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"brijeshyadav1979@gmail.com\"},\"requestMetadata\":{\"callerIp\":\"163.53.85.108\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/97.0.4692.99 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-07T17:37:09.749129Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"authorizationInfo\":[{\"resource\":\"projects\\/493987525449\",\"permission\":\"firebasedatabase.instances.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/493987525449\\/locations\\/us-central1\",\"request\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.CreateDatabaseInstanceRequest\",\"parent\":\"projects\\/493987525449\\/locations\\/us-central1\",\"databaseInstance\":{\"type\":\"DEFAULT_DATABASE\"},\"validateOnly\":true}},\"insertId\":\"-p8vcupddj1e\",\"resource\":{\"type\":\"audited_resource\"},\"timestamp\":\"2022-02-07T17:37:09.731594Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/guarduim\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-02-07T17:37:10.444874877Z\"}";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}

	@Test
	public void testNullCallerSupplyAgent() {
		final String gcpString2 = "{\r\n" + "    \"protoPayload\": {\r\n"
				+ "      \"@type\": \"type.googleapis.com/google.cloud.audit.AuditLog\",\r\n"
				+ "      \"status\": {},\r\n" + "      \"authenticationInfo\": {\r\n"
				+ "        \"principalEmail\": \"brijeshyadav1979@gmail.com\"\r\n" + "      },\r\n"
				+ "      \"requestMetadata\": {\r\n" + "        \"callerIp\": \"163.53.85.108\",\r\n"
				+ "        \"requestAttributes\": {\r\n" + "          \"time\": \"2022-02-07T17:37:09.749129Z\",\r\n"
				+ "          \"auth\": {}\r\n" + "        },\r\n" + "        \"destinationAttributes\": {}\r\n"
				+ "      },\r\n" + "      \"serviceName\": \"firebasedatabase.googleapis.com\",\r\n"
				+ "      \"methodName\": \"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\r\n"
				+ "      \"authorizationInfo\": [\r\n" + "        {\r\n"
				+ "          \"resource\": \"projects/493987525449\",\r\n"
				+ "          \"permission\": \"firebasedatabase.instances.create\",\r\n"
				+ "          \"granted\": true,\r\n" + "          \"resourceAttributes\": {}\r\n" + "        }\r\n"
				+ "      ],\r\n" + "      \"resourceName\": \"projects/493987525449/locations/us-central1\",\r\n"
				+ "      \"request\": {\r\n"
				+ "        \"@type\": \"type.googleapis.com/google.firebase.database.v1beta.CreateDatabaseInstanceRequest\",\r\n"
				+ "        \"parent\": \"projects/493987525449/locations/us-central1\",\r\n"
				+ "        \"databaseInstance\": {\r\n" + "          \"type\": \"DEFAULT_DATABASE\"\r\n"
				+ "        },\r\n" + "        \"validateOnly\": true\r\n" + "      }\r\n" + "    },\r\n"
				+ "    \"insertId\": \"-p8vcupddj1e\",\r\n" + "    \"resource\": {\r\n"
				+ "      \"type\": \"audited_resource\",\r\n" + "      \"labels\": {\r\n"
				+ "        \"service\": \"firebasedatabase.googleapis.com\",\r\n"
				+ "        \"method\": \"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\"\r\n"
				+ "      }\r\n" + "    },\r\n" + "    \"timestamp\": \"2022-02-07T17:37:09.731594Z\",\r\n"
				+ "    \"severity\": \"INFO\",\r\n"
				+ "    \"logName\": \"projects/guarduim/logs/cloudaudit.googleapis.com%2Factivity\",\r\n"
				+ "    \"receiveTimestamp\": \"2022-02-07T17:37:10.444874877Z\"\r\n" + "  }";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}

	@Test
	public void testNullProjectId() {
		final String gcpString2 = "{\r\n" + "    \"protoPayload\": {\r\n"
				+ "      \"@type\": \"type.googleapis.com/google.cloud.audit.AuditLog\",\r\n"
				+ "      \"status\": {},\r\n" + "      \"authenticationInfo\": {\r\n"
				+ "        \"principalEmail\": \"brijeshyadav1979@gmail.com\"\r\n" + "      },\r\n"
				+ "      \"requestMetadata\": {\r\n" + "        \"callerIp\": \"163.53.85.108\",\r\n"
				+ "        \"callerSuppliedUserAgent\": \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.99 Safari/537.36,gzip(gfe),gzip(gfe)\",\r\n"
				+ "        \"requestAttributes\": {\r\n" + "          \"time\": \"2022-02-07T17:37:09.749129Z\",\r\n"
				+ "          \"auth\": {}\r\n" + "        },\r\n" + "        \"destinationAttributes\": {}\r\n"
				+ "      },\r\n" + "      \"serviceName\": \"firebasedatabase.googleapis.com\",\r\n"
				+ "      \"methodName\": \"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\r\n"
				+ "      \"authorizationInfo\": [\r\n" + "        {\r\n"
				+ "          \"resource\": \"projects/493987525449\",\r\n"
				+ "          \"permission\": \"firebasedatabase.instances.create\",\r\n"
				+ "          \"granted\": true,\r\n" + "          \"resourceAttributes\": {}\r\n" + "        }\r\n"
				+ "      ],\r\n" + "      \"resourceName\": \"projects/493987525449/locations/us-central1\",\r\n"
				+ "      \"request\": {\r\n"
				+ "        \"@type\": \"type.googleapis.com/google.firebase.database.v1beta.CreateDatabaseInstanceRequest\",\r\n"
				+ "        \"parent\": \"projects/493987525449/locations/us-central1\",\r\n"
				+ "        \"databaseInstance\": {\r\n" + "          \"type\": \"DEFAULT_DATABASE\"\r\n"
				+ "        },\r\n" + "        \"validateOnly\": true\r\n" + "      }\r\n" + "    },\r\n"
				+ "    \"insertId\": \"-p8vcupddj1e\",\r\n" + "    \"resource\": {\r\n"
				+ "      \"type\": \"audited_resource\",\r\n" + "      \"labels\": {\r\n"
				+ "        \"service\": \"firebasedatabase.googleapis.com\",\r\n"
				+ "        \"method\": \"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\"\r\n"
				+ "      }\r\n" + "    },\r\n" + "    \"timestamp\": \"2022-02-07T17:37:09.731594Z\",\r\n"
				+ "    \"severity\": \"INFO\",\r\n"
				+ "    \"logName\": \"projects/guarduim/logs/cloudaudit.googleapis.com%2Factivity\",\r\n"
				+ "    \"receiveTimestamp\": \"2022-02-07T17:37:10.444874877Z\"\r\n" + "  }";
		Collection<co.elastic.logstash.api.Event> events = getEvents(gcpString2);
		assertNotNull(events);
	}

	@Test
	public void testValidateKey() throws Exception {
		final String fireStoreLogsString = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"brijeshyadav1979@gmail.com\"},\"requestMetadata\":{\"callerIp\":\"163.53.85.108\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/97.0.4692.99 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-07T18:22:40.885763Z\",\"reason\":\"8uSywAYxWi9GaXJlc3RvcmUgd2F0Y2ggZm9yIGxvbmcgcnVubmluZyBzdHJlYW1pbmcgcnBjLg\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firestore.v1.Firestore.Listen\",\"authorizationInfo\":[{\"resource\":\"projects\\/guarduim\\/databases\\/\",\"permission\":\"datastore.entities.get\",\"granted\":true,\"resourceAttributes\":{}},{\"resource\":\"projects\\/guarduim\\/databases\\/\",\"permission\":\"datastore.entities.list\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/guarduim\\/databases\\/(default)\",\"request\":{\"addTarget\":{\"query\":{\"parent\":\"projects\\/guarduim\\/databases\\/(default)\\/documents\",\"structuredQuery\":{\"from\":[{\"collectionId\":\"collection55\"}],\"orderBy\":[{\"field\":{\"fieldPath\":\"__name__\"},\"direction\":\"ASCENDING\"}]}},\"targetId\":50},\"@type\":\"type.googleapis.com\\/google.firestore.v1.ListenRequest\"},\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.DatastoreServiceData\"}},\"insertId\":\"-4flla8df63i\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"service\":\"firestore.googleapis.com\",\"method\":\"google.firestore.v1.Firestore.Listen\",\"project_id\":\"guarduim\"}},\"timestamp\":\"2022-02-07T18:22:40.874854Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/guarduim\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"381d996b-ef01-4267-bbb3-0b13c7b2cdc1\",\"producer\":\"firestore.googleapis.com\"},\"receiveTimestamp\":\"2022-02-07T18:22:41.231100438Z\"}";
		final JsonObject fbj = JsonParser.parseString(fireStoreLogsString).getAsJsonObject();
		JsonObject record = Parser.validateKeyExistance(fbj, "databaseName");
		assertNotNull(record);
	}

	public Collection<co.elastic.logstash.api.Event> getEvents(String gcpStringRequest) {
		FireBaseGuardiumFilter filter = getFireBaseFilterConnector(gcpStringRequest);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", gcpStringRequest);
		Collection<co.elastic.logstash.api.Event> events = filter.filter(Collections.singletonList(e), matchListener);
		return events;
	}

	private FireBaseGuardiumFilter getFireBaseFilterConnector(String jsonString) {
		Configuration config = new ConfigurationImpl(Collections.singletonMap("source", jsonString));
		Context context = new ContextImpl(null, null);
		FireBaseGuardiumFilter filter = new FireBaseGuardiumFilter("spn-id", config, context);
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
