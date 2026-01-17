/*

Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0

*/
package com.ibm.guardium.firestore;

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

	final String fireStoreLogsString = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"authenticationInfo\":{\"principalEmail\":\"aabbcc.abc@hcl.com\"},\"requestMetadata\":{\"callerIp\":\"122.161.51.105\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/102.0.5005.124 Safari\\/537.36 Edg\\/102.0.1245.44,gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-06-22T07:11:09.274182Z\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firestore.googleapis.com\",\"methodName\":\"google.firestore.admin.v1.FirestoreAdmin.DeleteIndex\",\"authorizationInfo\":[{\"resource\":\"projects\\/project-sccd\\/databases\\/(default)\\/collectionGroups\\/09march-coll\\/indexes\\/CICAgLiIkYMK\",\"permission\":\"datastore.indexes.delete\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/project-sccd\\/databases\\/(default)\\/collectionGroups\\/09march-coll\\/indexes\\/CICAgLiIkYMK\",\"request\":{\"@type\":\"type.googleapis.com\\/google.firestore.admin.v1.DeleteIndexRequest\",\"name\":\"projects\\/project-sccd\\/databases\\/(default)\\/collectionGroups\\/09march-coll\\/indexes\\/CICAgLiIkYMK\"}},\"insertId\":\"1iy4kipbco\",\"resource\":{\"type\":\"datastore_index\",\"labels\":{\"database_id\":\"(default)\",\"index_id\":\"CICAgLiIkYMK\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-06-22T07:11:09.064713Z\",\"severity\":\"NOTICE\",\"logName\":\"projects\\/project-sccd\\/logs\\/cloudaudit.googleapis.com%2Factivity\",\"receiveTimestamp\":\"2022-06-22T07:11:09.582401147Z\"}";
	final JsonObject fireStoreJson = JsonParser.parseString(fireStoreLogsString).getAsJsonObject();

	@Test
	public void testParseRecord() throws Exception {
		Record record = Parser.parseRecord(fireStoreJson);
		assertEquals("1iy4kipbco", record.getSessionId());
		// assertEquals("(default)", record.getDbName());
		assertEquals("aabbcc.abc@hcl.com", record.getAppUserName());
		assertNotNull(record.getSessionLocator());
		assertNotNull(record.getAccessor());
		assertNotNull(record.getDbName());
	}

	@Test
	public void testParseSessionLocator() throws Exception {
		Record record = Parser.parseRecord(fireStoreJson);
		SessionLocator actual = record.getSessionLocator();
		assertNotNull(record.getSessionLocator());
		assertEquals("0.0.0.0", actual.getServerIp());
		assertEquals(-1, actual.getServerPort());
		assertEquals("122.161.51.105", actual.getClientIp());
		assertEquals(-1, actual.getClientPort());
	}

	@Test
	public void testParseSessionLocator_Ipv6() throws Exception {
		final String fireStoreLogsString = "{\"protoPayload\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.AuditLog\",\"status\":{},\"authenticationInfo\":{\"principalEmail\":\"brijeshyadav1979@gmail.com\"},\"requestMetadata\":{\"callerIp\":\"2001:0db8:85a3:0000:0000:8a2e:0370:7334\",\"callerSuppliedUserAgent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/97.0.4692.99 Safari\\/537.36,gzip(gfe),gzip(gfe)\",\"requestAttributes\":{\"time\":\"2022-02-07T18:22:40.885763Z\",\"reason\":\"8uSywAYxWi9GaXJlc3RvcmUgd2F0Y2ggZm9yIGxvbmcgcnVubmluZyBzdHJlYW1pbmcgcnBjLg\",\"auth\":{}},\"destinationAttributes\":{}},\"serviceName\":\"firestore.googleapis.com\",\"methodName\":\"google.firestore.v1.Firestore.Listen\",\"authorizationInfo\":[{\"resource\":\"projects\\/guarduim\\/databases\\/\",\"permission\":\"datastore.entities.get\",\"granted\":true,\"resourceAttributes\":{}},{\"resource\":\"projects\\/guarduim\\/databases\\/\",\"permission\":\"datastore.entities.list\",\"granted\":true,\"resourceAttributes\":{}}],\"resourceName\":\"projects\\/guarduim\\/databases\\/(default)\",\"request\":{\"addTarget\":{\"query\":{\"parent\":\"projects\\/guarduim\\/databases\\/(default)\\/documents\",\"structuredQuery\":{\"from\":[{\"collectionId\":\"collection55\"}],\"orderBy\":[{\"field\":{\"fieldPath\":\"__name__\"},\"direction\":\"ASCENDING\"}]}},\"targetId\":50},\"@type\":\"type.googleapis.com\\/google.firestore.v1.ListenRequest\"},\"metadata\":{\"@type\":\"type.googleapis.com\\/google.cloud.audit.DatastoreServiceData\"}},\"insertId\":\"-4flla8df63i\",\"resource\":{\"type\":\"audited_resource\",\"labels\":{\"service\":\"firestore.googleapis.com\",\"method\":\"google.firestore.v1.Firestore.Listen\",\"project_id\":\"guarduim\"}},\"timestamp\":\"2022-02-07T18:22:40.874854Z\",\"severity\":\"INFO\",\"logName\":\"projects\\/guarduim\\/logs\\/cloudaudit.googleapis.com%2Fdata_access\",\"operation\":{\"id\":\"381d996b-ef01-4267-bbb3-0b13c7b2cdc1\",\"producer\":\"firestore.googleapis.com\"},\"receiveTimestamp\":\"2022-02-07T18:22:41.231100438Z\"}";
		final JsonObject spannerJson = JsonParser.parseString(fireStoreLogsString).getAsJsonObject();
		Record record = Parser.parseRecord(spannerJson);
		SessionLocator actual = record.getSessionLocator();
		assertEquals("0000:0000:0000:0000:0000:FFFF:0000:0000", actual.getServerIpv6());
	}

	@Test
	public void testParseAccessor() throws Exception {
		Record record = Parser.parseRecord(fireStoreJson);
		Accessor actual = record.getAccessor();
		assertNotNull(record.getAccessor());
		assertEquals("project-sccd-(default)", actual.getServiceName());
		assertEquals("project-sccd_firestore.googleapis.com", actual.getServerHostName());
		assertEquals("aabbcc.abc@hcl.com", actual.getDbUser());
	}
	
	

}
