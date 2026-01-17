/*
 Copyright IBM Corp. 2021, 2022 All rights reserved.
 SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.apachesolrdb;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.logstash.Event;

import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
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
		event.setField(ApplicationConstant.TIMESTAMP, "2022-02-10");
		Exception thrown = Assertions.assertThrows(Exception.class, () -> Parser.parseTime(event));
		Assertions.assertEquals("Incorrect Time Format", thrown.getMessage());
	}

	@Test
	public void testParseQtpAsConstruct_Find() throws Exception {
		event.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_SELECT_VALUE);
		event.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		event.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SELECT_VALUE);
		Construct result = Parser.parseQtpAsConstruct(event);
		final Sentence sentence = result.sentences.get(0);
		Assert.assertNotNull(sentence);
		Assert.assertEquals(ApplicationConstantTest.EXPECTED_SELECT_VALUE, sentence.getVerb());
		Assert.assertEquals(ApplicationConstantTest.EXPECTED_COLLECTION_VALUE, sentence.getObjects().get(0).name);
		Assert.assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void testParseQtpAsConstruct_update() throws Exception {
		event.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_UPDATE_VALUE);
		event.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		event.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_ADD_VALUE);
		Construct result = Parser.parseQtpAsConstruct(event);
		final Sentence sentence = result.sentences.get(0);
		Assert.assertNotNull(sentence);
		Assert.assertEquals(ApplicationConstantTest.EXPECTED_UPDATE_VALUE, sentence.getVerb());
		Assert.assertEquals(ApplicationConstantTest.EXPECTED_COLLECTION_VALUE, sentence.getObjects().get(0).name);
		Assert.assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void testParseAccessor() {
		event.setField(ApplicationConstant.CLASS, ApplicationConstantTest.UPDATE_CLASS_VALUE);
		event.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		event.setField(ApplicationConstant.SERVER_TYPE, ApplicationConstantTest.SERVER_TYPE_VALUE);
		final Accessor accessor = Parser.parseAccessor(event);
		Assert.assertNotNull(accessor);
		Assert.assertEquals(ApplicationConstant.DATA_PROTOCOL, accessor.getDbProtocol().toString());
		Assert.assertEquals(ApplicationConstantTest.SERVER_TYPE_VALUE, accessor.getServerType().toString());
		Assert.assertEquals(ApplicationConstant.NOT_AVAILABLE, accessor.getDbUser().toString());
		Assert.assertEquals(Accessor.LANGUAGE_FREE_TEXT_STRING, accessor.getLanguage().toString());
		Assert.assertEquals(ApplicationConstantTest.EXPECTED_COLLECTION_VALUE, accessor.getServiceName().toString());
	}

	@Test
	public void testParseSessionLocator() {
		event.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		final SessionLocator sessionLocator = Parser.parseSessionLocator(event);
		Assert.assertNotNull(sessionLocator);
		Assert.assertEquals(ApplicationConstant.DEFAULT_IP, sessionLocator.getClientIp().toString());
		Assert.assertEquals(-1, sessionLocator.getClientPort());
		Assert.assertEquals(ApplicationConstantTest.SERVERIP_VALUE, sessionLocator.getServerIp().toString());
		Assert.assertEquals(-1, sessionLocator.getServerPort());
	}

	@Test
	public void testParseSentence() throws Exception {
		event.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_SELECT_VALUE);
		event.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		event.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SELECT_VALUE);
		Sentence sentence = Parser.parseQtpSentence(event);
		Assert.assertNotNull(sentence);
		Assert.assertEquals(ApplicationConstantTest.EXPECTED_SELECT_VALUE, sentence.getVerb());
		Assert.assertEquals(ApplicationConstantTest.EXPECTED_COLLECTION_VALUE, sentence.getObjects().get(0).name);
		Assert.assertEquals("collection", sentence.getObjects().get(0).type);
	}

	@Test
	public void testParseException() {
		event.setField(ApplicationConstant.ERROR_MSG, ApplicationConstantTest.ERROR_DESCRIPTION);
		event.setField(ApplicationConstant.CLASS, ApplicationConstantTest.SOLR_QTP_ERROR_STRING_VALUE);
		final ExceptionRecord exceptionRecord = Parser.parseQtpExceptionRecord(event);
		Assert.assertNotNull(exceptionRecord);
		Assert.assertEquals(ApplicationConstant.EXCEPTION_TYPE_AUTHORIZATION_STRING,
				exceptionRecord.getExceptionTypeId().toString());
		Assert.assertEquals(ApplicationConstantTest.ERROR_DESCRIPTION, exceptionRecord.getDescription().toString());
	}
	
	@Test
	public void testRedacted() throws Exception {
		event.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SWAP_CORE_VALUE);
		event.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		event.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_CORE_VALUE);
		event.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		event.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_ADMIN_VALUE);
		event.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		event.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		event.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Record record = Parser.parseQtpRecord(event);
		String redacted = record.getData().getConstruct().getRedactedSensitiveDataSql();
		Assert.assertNotNull(redacted);
		Assert.assertEquals(ApplicationConstantTest.REDACTED_VALUE, redacted);
	}

}
