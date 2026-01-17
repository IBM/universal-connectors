/*

Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0

*/
package com.ibm.guardium.firebase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;

public class ParserTest {

	final String fireBaseLogsString = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"meenakshi_b@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"223.233.72.13\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/99.0.4844.51 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-03-17T03:42:10.393226Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firebasedatabase.googleapis.com\",\"methodName\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"authorizationInfo\":[{\"resource\":\"projects\\/1065340084898\",\"permission\":\"firebasedatabase.instances.create\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/1065340084898\\/locations\\/us-central1\",\"request\":{\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.CreateDatabaseInstanceRequest\",\"databaseInstance\":{\"type\":\"USER_DATABASE\"},\"validateOnly\":true,\"databaseId\":\"project-sccd-3b9f0\",\"parent\":\"projects\\/1065340084898\\/locations\\/us-central1\"},\"response\":{\"databaseUrl\":\"https:\\/\\/project-sccd-3b9f0.firebaseio.com\",\"type\":\"USER_DATABASE\",\"name\":\"projects\\/1065340084898\\/locations\\/us-central1\\/instances\\/project-sccd-3b9f0\",\"@type\":\"type.googleapis.com\\/google.firebase.database.v1beta.DatabaseInstance\",\"project\":\"projects\\/1065340084898\"}},\"insertId\":\"d1ic85d5ibs\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"method\":\"google.firebase.database.v1beta.RealtimeDatabaseService.CreateDatabaseInstance\",\"service\":\"firebasedatabase.googleapis.com\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-03-17T03:42:09.713515Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-03-17T03:42:10.655731544Z\"}";
	final JsonObject fireBaseJson = JsonParser.parseString(fireBaseLogsString).getAsJsonObject();

	@Test
	public void testParseRecord() {
		Record record = Parser.parseRecord(fireBaseJson);
		assertEquals("1393994353", record.getSessionId());
		assertEquals("meenakshi_b@hcl.com", record.getAppUserName());
		assertNotNull(record.getSessionLocator());
		assertNotNull(record.getAccessor());
		assertNotNull(record.getDbName());
	}

	@Test
	public void testParseSessionLocator() {
		Record record = Parser.parseRecord(fireBaseJson);
		SessionLocator actual = record.getSessionLocator();
		assertNotNull(record.getSessionLocator());
		assertEquals("0.0.0.0", actual.getServerIp());
		assertEquals(-1, actual.getServerPort());
		assertEquals("223.233.72.13", actual.getClientIp());
		assertEquals(-1, actual.getClientPort());
	}

	@Test
	public void testParseSessionLocator_Ipv6() {
		final String fireStoreLogsString = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"brijeshyadav1979@gmail.com\"},\"requestMetadata\":{\"callerIp\":\"2001:0db8:85a3:0000:0000:8a2e:0370:7334\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/97.0.4692.99 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-07T18:22:40.885763Z\",\"reason\":\"8uSywAYxWi9GaXJlc3RvcmUgd2F0Y2ggZm9yIGxvbmcgcnVubmluZyBzdHJlYW1pbmcgcnBjLg\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firestore.googleapis.com\",\"methodName\":\"google.firestore.v1.Firestore.Listen\",\"authorizationInfo\":[{\"resource\":\"projects\\/guarduim\\/databases\\/\",\"permission\":\"datastore.entities.get\",\"granted\":true,\"resourceAttributes\":{}},{\"resource\":\"projects\\/guarduim\\/databases\\/\",\"permission\":\"datastore.entities.list\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/guarduim\\/databases\\/(default)\",\"request\":{\"addTarget\":{\"query\":{\"parent\":\"projects\\/guarduim\\/databases\\/(default)\\/documents\",\"structuredQuery\":{\"from\":[{\"collectionId\":\"collection55\"}],\"orderBy\":[{\"field\":{\"fieldPath\":\"__name__\"},\"direction\":\"ASCENDING\"}]}},\"targetId\":50},\"@type\":\"type.googleapis.com\\/google.firestore.v1.ListenRequest\"},\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.DatastoreServiceData\"}},\"insertId\":\"-4flla8df63i\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"service\":\"firestore.googleapis.com\",\"method\":\"google.firestore.v1.Firestore.Listen\",\"project_id\":\"guarduim\"}},\"timestamp\":\"2022-02-07T18:22:40.874854Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/guarduim\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"381d996b-ef01-4267-bbb3-0b13c7b2cdc1\",\"producer\":\"firestore.googleapis.com\"},\"receiveTimestamp\":\"2022-02-07T18:22:41.231100438Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(fireStoreLogsString).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		SessionLocator actual = record.getSessionLocator();
		assertEquals(-1, actual.getServerPort());
		assertEquals(-1, actual.getClientPort());
		assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334", actual.getClientIpv6());
		assertEquals("0000:0000:0000:0000:0000:FFFF:0000:0000", actual.getServerIpv6());
	}

	@Test
	public void testParseAccessor() {
		Record record = Parser.parseRecord(fireBaseJson);
		Accessor actual = record.getAccessor();
		assertNotNull(record.getAccessor());
		assertEquals("project-sccd:project-sccd-3b9f0", actual.getServiceName());
		assertEquals("project-sccd_firebasedatabase.googleapis.com", actual.getServerHostName());
		assertEquals("meenakshi_b@hcl.com", actual.getDbUser());
	}
}
