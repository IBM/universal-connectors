/*
 Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.awsmariadb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.logstash.plugins.ConfigurationImpl;

import com.ibm.guardium.awsmariadb.constant.ApplicationConstant;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class AWSMariaDBGuardiumFilterTest {
	FilterMatchListener matchListener = new TestMatchListener();
	Configuration config = new ConfigurationImpl(
			Collections.singletonMap(ApplicationConstantTest.CONFIG_KEY, ApplicationConstantTest.CONFIG_VALUE));
	AWSMariaDBGuardiumFilter mariadbGuardiumFilter = new AWSMariaDBGuardiumFilter(ApplicationConstantTest.ID, config,
			null);

	@Test
	public void filterTest() {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, ApplicationConstantTest.LOG_MESSAGE);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, ApplicationConstantTest.CONNECTIONID_VALUE);
		event.setField(ApplicationConstantTest.USERNAME_KEY, ApplicationConstantTest.USERNAME_VALUE);
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:Connectors");
		event.setField(ApplicationConstantTest.RETCODE_KEY, ApplicationConstantTest.RETCODE_VALUE);
		event.setField(ApplicationConstantTest.OBJECT_KEY, ApplicationConstantTest.OBJECT_VALUE);
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, ApplicationConstantTest.HOSTNAME_VALUE);
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, ApplicationConstantTest.OPERATION_VALUE);
		event.setField("timestamp", "20220107 09:11:51");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(Collections.singletonList(event),
				matchListener);
		assertNotNull(actualResponse);
		assertEquals(ApplicationConstantTest.CONNECTIONID_VALUE, actualResponse.toArray(new org.logstash.Event[] {})[0]
				.getField(ApplicationConstantTest.CONNECTIONID_KEY));
		assertEquals("979326520502-database-mariadbtest", actualResponse.toArray(new org.logstash.Event[] {})[0]
				.getField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY));
		assertEquals(ApplicationConstantTest.USERNAME_VALUE,
				actualResponse.toArray(new org.logstash.Event[] {})[0].getField(ApplicationConstantTest.USERNAME_KEY));
		assertEquals(ApplicationConstantTest.HOSTNAME_VALUE,
				actualResponse.toArray(new org.logstash.Event[] {})[0].getField(ApplicationConstantTest.HOSTNAME_KEY));
		assertEquals("979326520502:database-mariadbtest:Connectors",
				actualResponse.toArray(new org.logstash.Event[] {})[0].getField(ApplicationConstant.DBNAMEPREFIX_KEY));
		assertEquals(ApplicationConstantTest.RETCODE_VALUE,
				actualResponse.toArray(new org.logstash.Event[] {})[0].getField(ApplicationConstantTest.RETCODE_KEY));
		assertEquals(ApplicationConstantTest.OBJECT_VALUE,
				actualResponse.toArray(new org.logstash.Event[] {})[0].getField(ApplicationConstantTest.OBJECT_KEY));

	}

	@Test
	public void retcodeTest() {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, ApplicationConstantTest.LOG_MESSAGE);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, ApplicationConstantTest.CONNECTIONID_VALUE);
		event.setField(ApplicationConstantTest.USERNAME_KEY, ApplicationConstantTest.USERNAME_VALUE);
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:Connectors");
		event.setField(ApplicationConstantTest.OBJECT_KEY, ApplicationConstantTest.OBJECT_VALUE);
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, ApplicationConstantTest.HOSTNAME_VALUE);
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, ApplicationConstantTest.OPERATION_VALUE);
		event.setField("timestamp", "20220107 09:11:51");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(Collections.singletonList(event),
				matchListener);
		assertNotNull(actualResponse);
		assertEquals(ApplicationConstantTest.CONNECTIONID_VALUE, actualResponse.toArray(new org.logstash.Event[] {})[0]
				.getField(ApplicationConstantTest.CONNECTIONID_KEY));
		assertEquals("979326520502-database-mariadbtest", actualResponse.toArray(new org.logstash.Event[] {})[0]
				.getField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY));
		assertEquals(ApplicationConstantTest.USERNAME_VALUE,
				actualResponse.toArray(new org.logstash.Event[] {})[0].getField(ApplicationConstantTest.USERNAME_KEY));
		assertEquals(ApplicationConstantTest.HOSTNAME_VALUE,
				actualResponse.toArray(new org.logstash.Event[] {})[0].getField(ApplicationConstantTest.HOSTNAME_KEY));
		assertEquals("979326520502:database-mariadbtest:Connectors",
				actualResponse.toArray(new org.logstash.Event[] {})[0].getField(ApplicationConstant.DBNAMEPREFIX_KEY));
		assertEquals(ApplicationConstantTest.OBJECT_VALUE,
				actualResponse.toArray(new org.logstash.Event[] {})[0].getField(ApplicationConstantTest.OBJECT_KEY));

	}

	@Test
	public void ExceptionTest() {
		final String auditString = "20220524 03:23:47,ip-10-19-0-161,admin,223.186.31.18,231,42427,QUERY,Connectors,'select * from ***REMOVED***le\nLIMIT 0, 1000',1146";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "231");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "admin");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:Connectors");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "1146");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'select * from ***REMOVED***le\nLIMIT 0, 1000'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "223.186.31.18");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220524 03:23:47");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void CONNECTIONTest() {
		final String auditString = "20220527 05:51:28,ip-10-19-0-161,rd***REMOVED***dmin,localhost,1554,0,CONNECT,,,0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "1554");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "rd***REMOVED***dmin");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "CONNECT");
		event.setField("timestamp", "20220527 05:51:28");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void DISCONNECTTest() {
		final String auditString = "20220527 06:17:43,ip-10-19-0-161,rd***REMOVED***dmin,localhost,1559,0,DISCONNECT,,,0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "1559");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "rd***REMOVED***dmin");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "DISCONNECT");
		event.setField("timestamp", "20220527 06:17:43");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void FAILED_CONNECTTest() {
		final String auditString = "20220527 11:46:07,ip-10-19-0-161,,167.94.138.120,1632,0,FAILED_CONNECT,,,1158";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "1632");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "1158");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "167.94.138.120");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "FAILED_CONNECT");
		event.setField("timestamp", "20220527 11:46:07");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void FAILED_CONNECT2Test() {
		final String auditString = "20220527 11:46:07,ip-10-19-0-161,,167.94.138.120,1632,0,FAILED_CONNECT,,,1159";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "1632");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "1159");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "167.94.138.120");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "FAILED_CONNECT");
		event.setField("timestamp", "20220527 11:46:07");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void FAILED_CONNECT1Test() {
		final String auditString = "20220527 12:21:14,ip-10-19-0-161,admin,223.226.69.23,1647,0,FAILED_CONNECT,,,1045";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "1632");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "admin");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "1045");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "223.226.69.23");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "FAILED_CONNECT");
		event.setField("timestamp", "20220527 12:21:14");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void INSERTTest() {
		final String auditString = "20220530 10:07:16,ip-10-19-0-187,admin,223.226.69.23,49,7524,QUERY,MariaDB,'INSERT INTO Complxes_Details VALUES(1,\\'reddy***REMOVED***\\')',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "49");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "admin");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:MariaDB");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'INSERT INTO Complxes_Details VALUES(1,\\'reddy***REMOVED***\\')'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "223.226.69.23");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220527 12:21:14");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void GROUPBYTest() {
		final String auditString = "20220531 08:17:22,ip-10-19-0-190,admin,136.185.152.250,29,12623,QUERY,ORG,'SELECT \\n    fruits\\nFROM \\n    Works\\nGROUP BY \\n    fruits\\nLIMIT 0, 1000',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "29");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "admin");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:ORG");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'SELECT \\n    fruits\\nFROM \\n    Works\\nGROUP BY \\n    fruits\\nLIMIT 0, 1000'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "136.185.152.250");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220531 08:17:22");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void ORDERBYTest() {
		final String auditString = "20220531 08:19:50,ip-10-19-0-190,admin,136.185.152.250,29,12724,QUERY,ORG,'SELECT \\n   DISTINCT id\\nFROM \\n   Works\\nORDER BY \\n   id\\nLIMIT 0, 1000',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "29");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "admin");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:ORG");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'SELECT \\n   DISTINCT id\\nFROM \\n   Works\\nORDER BY \\n   id\\nLIMIT 0, 1000'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "136.185.152.250");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220531 08:19:50");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void HAVINGSTMTTest() {
		final String auditString = "20220531 08:51:54,ip-10-19-0-190,admin,136.185.152.250,29,14075,QUERY,ORG,'SELECT DEPARTMENT, COUNT(WORKER_ID) as \\'Number of Workers\\' FROM Worker GROUP BY DEPARTMENT HAVING COUNT(WORKER_ID) < 5\\nLIMIT 0, 1000',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "29");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "admin");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:ORG");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'SELECT DEPARTMENT, COUNT(WORKER_ID) as \\'Number of Workers\\' FROM Worker GROUP BY DEPARTMENT HAVING COUNT(WORKER_ID) < 5\\nLIMIT 0, 1000'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "136.185.152.250");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220531 08:51:54");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void INSERT1Test() {
		final String auditString = "20220531 06:18:52,ip-10-19-0-190,admin,136.185.152.250,29,7647,QUERY,ORG,'INSERT INTO Bonus \\n\\t(WORKER_REF_ID, BONUS_AMOUNT, BONUS_DATE) VALUES\\n\\t\\t(001, 5000, \\'16-02-20\\'),\\n\\t\\t(002, 3000, \\'16-06-11\\'),\\n\\t\\t(003, 4000, \\'16-02-20\\'),\\n\\t\\t(001, 4500, \\'16-02-20\\'),\\n\\t\\t(002, 3500, \\'16-06-11\\')',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "29");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "admin");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:ORG");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'INSERT INTO Bonus \\n\\t(WORKER_REF_ID, BONUS_AMOUNT, BONUS_DATE) VALUES\\n\\t\\t(001, 5000, \\'16-02-20\\'),\\n\\t\\t(002, 3000, \\'16-06-11\\'),\\n\\t\\t(003, 4000, \\'16-02-20\\'),\\n\\t\\t(001, 4500, \\'16-02-20\\'),\\n\\t\\t(002, 3500, \\'16-06-11\\')'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "136.185.152.250");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220531 06:18:52");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void PROCEDURETest() {
		final String auditString = "20220526 10:06:23,ip-10-19-0-161,admin,223.186.133.89,1244,180262,QUERY,Connectors,'CREATE PROCEDURE Connectors.searchThesisByFilters(\\n    IN year_in SMALLINT,\\n    IN title_in VARCHAR(255),\\n    IN limit_in TINYINT,\\n    IN offset_in TINYINT\\n)\\nBEGIN\\n    DECLARE first BIT DEFAULT 0;\\n\\n    SET @sql = \\'SELECT TD.title AS title,\\' ||\\n               \\'TD.year AS year,\\' ||\\n               \\'TD.file AS path,\\' ||\\n               \\'TD.abstract AS abstract,\\' ||\\n               \\'TD.thesis_id AS thesis_id \\' ||\\n               \\'FROM Thesis_Detail TD \\';\\n\\n    IF NOT ISNULL(title_in) THEN\\n        SET first = 1;\\n        SET @sql = @sql + \\' WHERE MATCH(title) AGAINST(? IN NATURAL LANGUAGE MODE)\\';\\n    END IF;\\n\\n    IF NOT ISNULL(year_in) THEN\\n        IF first THEN\\n            SET @sql = @sql + \\' WHERE\\';\\n        ELSE\\n            SET @sql = @sql + \\' AND\\';\\n        END IF;\\n        SET @sql = @sql + \\' TD.year = ?\\';\\n    END IF;\\n\\n    SET @sql = @sql + \\' LIMIT ?  OFFSET  ?\\';\\n\\n    PREPARE stmt FROM @sql;\\n    DEALLOCATE PREPARE stmt;\\n\\nEND',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "1244");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "admin");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:Connectors");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'CREATE PROCEDURE Connectors.searchThesisByFilters(\\n    IN year_in SMALLINT,\\n    IN title_in VARCHAR(255),\\n    IN limit_in TINYINT,\\n    IN offset_in TINYINT\\n)\\nBEGIN\\n    DECLARE first BIT DEFAULT 0;\\n\\n    SET @sql = \\'SELECT TD.title AS title,\\' ||\\n               \\'TD.year AS year,\\' ||\\n               \\'TD.file AS path,\\' ||\\n               \\'TD.abstract AS abstract,\\' ||\\n               \\'TD.thesis_id AS thesis_id \\' ||\\n               \\'FROM Thesis_Detail TD \\';\\n\\n    IF NOT ISNULL(title_in) THEN\\n        SET first = 1;\\n        SET @sql = @sql + \\' WHERE MATCH(title) AGAINST(? IN NATURAL LANGUAGE MODE)\\';\\n    END IF;\\n\\n    IF NOT ISNULL(year_in) THEN\\n        IF first THEN\\n            SET @sql = @sql + \\' WHERE\\';\\n        ELSE\\n            SET @sql = @sql + \\' AND\\';\\n        END IF;\\n        SET @sql = @sql + \\' TD.year = ?\\';\\n    END IF;\\n\\n    SET @sql = @sql + \\' LIMIT ?  OFFSET  ?\\';\\n\\n    PREPARE stmt FROM @sql;\\n    DEALLOCATE PREPARE stmt;\\n\\nEND'");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "223.186.133.89");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220526 10:06:23");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void ALTERUSERTest() {
		final String auditString = "20220628 13:54:30,ip-10-19-2-44,admin,160.238.72.86,483,23276,QUERY,adept,'ALTER USER foo2@test IDENTIFIED BY *****',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "483");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "admin");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:adept");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'ALTER USER foo2@test IDENTIFIED BY *****'");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "160.238.72.86");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220628 13:54:30");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void INSERTJOINTest() {
		final String auditString = "20220630 13:42:59,ip-10-19-2-122,admin,160.238.73.85,152,22748,QUERY,adept,'INSERT INTO `Employee` VALUES (\\'Veniam\\',\\'1\\',\\'640 Damon Junction\\\\nEast Mathew, NY 68818\\',\\'3\\'),\\n(\\'Molestiae\\',\\'2\\',\\'6658 Hollis Club\\\\nErnamouth, TX 19743\\',\\'10\\'),\\n(\\'Officiis\\',\\'3\\',\\'59965 Mason Neck Apt. 985\\\\nKareemborough, NV 85535\\',\\'9\\'),\\n(\\'Rerum\\',\\'4\\',\\'91067 Geovany Fort\\\\nHanefort, WA 92863\\',\\'6\\'),\\n(\\'Et\\',\\'5\\',\\'7647 Reva Shores Suite 970\\\\nNew Audrafort, OH 17846-5397\\',\\'2\\'),\\n(\\'Et\\',\\'6\\',\\'9419 Carmela Burg Apt. 687\\\\nAimeebury, SD 32389-4489\\',\\'8\\'),\\n(\\'Laborum\\',\\'7\\',\\'6961 Weissnat Drive\\\\nDonnellyfort, MT 53947\\',\\'6\\'),\\n(\\'Cupiditate\\',\\'8\\',\\'117 Nellie Summit Suite 982\\\\nSouth Heavenfurt, CA 45675\\',\\'8\\'),\\n(\\'Eveniet\\',\\'9\\',\\'9086 Mariam Square Suite 698\\\\nSouth Dulce, MT 82861-3079\\',\\'2\\'),\\n(\\'Rerum\\',\\'10\\',\\'783 Goodwin Burgs Apt. 429\\\\nWillmsfort, UT 42820-1019\\',\\'9\\'),\\n(\\'Quis\\',\\'11\\',\\'42928 Ernesto Trail\\\\nEast Jules, WV 87169-2851\\',\\'1\\'),\\n(\\'Esse\\',\\'12\\',\\'161 Kassulke Stravenue Apt. 937\\\\nWilliamsonton, MS 62622\\',\\'7\\'),\\n(\\'Dolores\\',',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "152");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "admin");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:adept");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'INSERT INTO `Employee` VALUES (\\'Veniam\\',\\'1\\',\\'640 Damon Junction\\\\nEast Mathew, NY 68818\\',\\'3\\'),\\n(\\'Molestiae\\',\\'2\\',\\'6658 Hollis Club\\\\nErnamouth, TX 19743\\',\\'10\\'),\\n(\\'Officiis\\',\\'3\\',\\'59965 Mason Neck Apt. 985\\\\nKareemborough, NV 85535\\',\\'9\\'),\\n(\\'Rerum\\',\\'4\\',\\'91067 Geovany Fort\\\\nHanefort, WA 92863\\',\\'6\\'),\\n(\\'Et\\',\\'5\\',\\'7647 Reva Shores Suite 970\\\\nNew Audrafort, OH 17846-5397\\',\\'2\\'),\\n(\\'Et\\',\\'6\\',\\'9419 Carmela Burg Apt. 687\\\\nAimeebury, SD 32389-4489\\',\\'8\\'),\\n(\\'Laborum\\',\\'7\\',\\'6961 Weissnat Drive\\\\nDonnellyfort, MT 53947\\',\\'6\\'),\\n(\\'Cupiditate\\',\\'8\\',\\'117 Nellie Summit Suite 982\\\\nSouth Heavenfurt, CA 45675\\',\\'8\\'),\\n(\\'Eveniet\\',\\'9\\',\\'9086 Mariam Square Suite 698\\\\nSouth Dulce, MT 82861-3079\\',\\'2\\'),\\n(\\'Rerum\\',\\'10\\',\\'783 Goodwin Burgs Apt. 429\\\\nWillmsfort, UT 42820-1019\\',\\'9\\'),\\n(\\'Quis\\',\\'11\\',\\'42928 Ernesto Trail\\\\nEast Jules, WV 87169-2851\\',\\'1\\'),\\n(\\'Esse\\',\\'12\\',\\'161 Kassulke Stravenue Apt. 937\\\\nWilliamsonton, MS 62622\\',\\'7\\'),\\n(\\'Dolores\\','");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-adept");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "160.238.73.85");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220628 13:54:30");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void PARSABLEVENT_OPERATIONTest() {
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "29");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "admin");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:adept");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'Select RTRIM(FIRST_NAME) from Worker\\\\nLIMIT 0, 1000'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "223.186.133.89");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "");
		event.setField("timestamp", "20220526 10:06:23");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void PARSABLEVENT_RETCODETest() {
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "250");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "ORG");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:Workers");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'Select RTRIM(FIRST_NAME) from Worker\\\\nLIMIT 0, 1000'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "223.186.133.89");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "0");
		event.setField("timestamp", "20220526 10:06:23");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	class TestMatchListener implements FilterMatchListener {

		private AtomicInteger matchCount = new AtomicInteger(0);

		@Override
		public void filterMatched(Event event) {
			matchCount.incrementAndGet();
		}

		public int getMatchCount() {
			return matchCount.get();
		}
	}
}
