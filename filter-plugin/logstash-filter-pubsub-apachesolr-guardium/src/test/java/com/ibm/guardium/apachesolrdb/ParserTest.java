/*
Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.apachesolrdb;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.logstash.Event;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
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

	Event event = new Event();

	@Test
	public void testParseTimestamp() throws Exception {
		String dateStr = "2022-02-10";
		Exception thrown = Assertions.assertThrows(Exception.class, () -> Parser.parseTime(dateStr));
		Assertions.assertEquals("Incorrect Time Format", thrown.getMessage());
	}

	@Test
	public void testParseQtpConstruct_update() throws Exception {
		final String solrString = "{\"insertId\":\"epzzqmg1c6000v\",\"jsonPayload\":{\"message\":\"2022-02-10 12:42:05.050 INFO  (qtp672313607-16) [   x:first_core] o.a.s.u.p.LogUpdateProcessorFactory [first_core]  webapp=\\/solr path=\\/update params={commitWithin=1000&overwrite=true&wt=json&_=1644496890256}{add=[7698 (1724380007675985920)]} 0 6\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\"}},\"timestamp\":\"2022-02-10T12:42:05.050746475Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:42:05.438339552Z\"}";
		final JsonObject solrJson = JsonParser.parseString(solrString).getAsJsonObject();
		Record record = Parser.parseQtpRecord(solrJson);
		Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		Assert.assertNotNull(sentence);
		Assert.assertNotNull(record);
		Assert.assertEquals("update", sentence.getVerb());
		Assert.assertEquals("first_core", sentence.getObjects().get(0).name);
		Assert.assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void testParseQtpConstruct_Find() throws Exception {
		final String solrString = "{\"insertId\":\"epzzqmg1c6dwlg\",\"jsonPayload\":{\"message\":\"2022-02-10 12:44:20.243 INFO  (qtp672313607-18) [   x:first_core] o.a.s.c.S.Request [first_core]  webapp=\\/solr path=\\/select params={q=id:7698&_=1644497065596} hits=1 status=0 QTime=1\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_id\":\"2639899388249855782\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-02-10T12:44:20.244030006Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:44:20.439608402Z\"}";
		final JsonObject solrJson = JsonParser.parseString(solrString).getAsJsonObject();
		Record record = Parser.parseQtpRecord(solrJson);
		final Sentence sentence = record.getData().getConstruct().getSentences().get(0);
		Assert.assertNotNull(sentence);
		Assert.assertEquals("select", sentence.getVerb());
		Assert.assertEquals("first_core", sentence.getObjects().get(0).name);
		Assert.assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void testParseAccessor() throws Exception {
		final String solrString = "{\"insertId\":\"epzzqmg1c6dwlg\",\"jsonPayload\":{\"message\":\"2022-02-10 12:44:20.243 INFO  (qtp672313607-18) [   x:first_core] o.a.s.c.S.Request [first_core]  webapp=\\/solr path=\\/select params={q=id:7698&_=1644497065596} hits=1 status=0 QTime=1\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_id\":\"2639899388249855782\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-02-10T12:44:20.244030006Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:44:20.439608402Z\"}";
		final JsonObject solrJson = JsonParser.parseString(solrString).getAsJsonObject();
		Record record = Parser.parseQtpRecord(solrJson);
		final Accessor accessor = record.getAccessor();
		Assert.assertNotNull(accessor);
		Assert.assertEquals("ApacheSolrGCP", accessor.getDbProtocol().toString());
		Assert.assertEquals("SolrDB", accessor.getServerType().toString());
		Assert.assertEquals("NA", accessor.getDbUser().toString());
		Assert.assertEquals("FREE_TEXT", accessor.getLanguage().toString());
		Assert.assertEquals("project-sccd:2639899388249855782:first_core", accessor.getServiceName().toString());
	}

	@Test
	public void testParseSessionLocator() throws Exception {
		final String solrString = "{\"insertId\":\"epzzqmg1c6dwlg\",\"jsonPayload\":{\"message\":\"2022-02-10 12:44:20.243 INFO  (qtp672313607-18) [   x:first_core] o.a.s.c.S.Request [first_core]  webapp=\\/solr path=\\/select params={q=id:7698&_=1644497065596} hits=1 status=0 QTime=1\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_id\":\"2639899388249855782\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-02-10T12:44:20.244030006Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:44:20.439608402Z\"}";
		final JsonObject solrJson = JsonParser.parseString(solrString).getAsJsonObject();
		Record record = Parser.parseQtpRecord(solrJson);
		final SessionLocator sessionLocator = record.getSessionLocator();
		Assert.assertNotNull(sessionLocator);
		Assert.assertEquals("0.0.0.0", sessionLocator.getClientIp().toString());
		Assert.assertEquals(-1, sessionLocator.getClientPort());
		Assert.assertEquals("0.0.0.0", sessionLocator.getServerIp().toString());
		Assert.assertEquals(-1, sessionLocator.getServerPort());
	}

	@Test
	public void testParseException() throws Exception {
		final String solrString = "{\"insertId\":\"17u90yhf9nmdte\",\"jsonPayload\":{\"message\":\"2022-02-10 14:04:38.542 ERROR  (qtp672313607-17) [   x:first_core] o.a.s.h.RequestHandlerBase org.apache.solr.common.SolrException: Cannot parse provided JSON: Unexpected EOF: char=(EOF),position=20 AFTER=''\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"2639899388249855782\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-02-10T14:04:38.543314031Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T14:04:39.447195015Z\"}";
		final JsonObject solrJson = JsonParser.parseString(solrString).getAsJsonObject();
		Record record = Parser.parseQtpRecord(solrJson);
		final ExceptionRecord exceptionRecord = record.getException();
		Assert.assertNotNull(exceptionRecord);
		Assert.assertEquals(ApplicationConstant.EXCEPTION_TYPE_AUTHORIZATION_STRING,
				exceptionRecord.getExceptionTypeId().toString());
		Assert.assertEquals(
				"o.a.s.h.RequestHandlerBase org.apache.solr.common.SolrException: Cannot parse provided JSON: Unexpected EOF: char=(EOF),position=20 AFTER=''",
				exceptionRecord.getDescription().toString());
	}
	
	@Test
	public void testRedacted() throws Exception {
		final String solrString = "{\"insertId\":\"1lzqgspg20hyho1\",\"jsonPayload\":{\"message\":\"2022-06-03 11:27:38.004 INFO  (qtp2005169944-19) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={core=firstCore&other=secondCore&action=SWAP} status=0 QTime=207\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"zone\":\"asia-south2-a\",\"instance_id\":\"6290282768426383618\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-30T08:50:36.941752806Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"apachesolrqa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-30T08:50:37.641620487Z\"}";
		final JsonObject solrJson = JsonParser.parseString(solrString).getAsJsonObject();
		Record record = Parser.parseQtpRecord(solrJson);
		String redacted = record.getData().getConstruct().getRedactedSensitiveDataSql();
		Assert.assertNotNull(redacted);
		Assert.assertNotNull(record);
		Assert.assertEquals("o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/cores params={core=?&other=?&action=?}", redacted);
	}
}