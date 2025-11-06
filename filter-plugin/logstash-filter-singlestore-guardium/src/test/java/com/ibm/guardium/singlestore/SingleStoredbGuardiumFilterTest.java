//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.singlestore;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.universalconnector.commons.GuardConstants;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleStoredbGuardiumFilterTest {


    /**
     * To feed Guardium universal connector, a "GuardRecord" fields must exist.
     * Filter should add field "GuardRecord" to the Event, which Universal connector then inserts into Guardium.
     */
    @Test
    public void testFieldGuardRecord_singlestoredb() {
        System.out.println("                                        ================================");
        System.out.println("========================================||testFieldGuardRecord_singlestoredb||========================================");
        System.out.println("                                        ================================");

        String singlestoreString = "133855,2024-06-24 07:16:16.901,UTC,53b9ae806c1c:3306,agg,1,100000,root,vector_db,,1308432953418920798,CREATE DATABASE `uc_vector_db`";
        //String singlestoreString= "133865,2024-06-24 07:17:27.705,UTC,53b9ae806c1c:3306,agg,1,100000,root,uc_vector_db,,9259882968147880232,CREATE TABLE uc_vector_table (AND SCHEMA_NAME LIKE 'uc_vector_db' AND TABLE_SCHEMA LIKE 'uc_vector_db' WHERE t.table_schema like 'uc_vector_db'";
        //String singlestoreString = "21596,2024-06-10 10:42:43.236,UTC,53b9ae806c1c:3306,agg,1,99995,root,information_schema,temp_1_5015_0,694459544968825767,SELECT IP_ADDR\\, PORT\\, MEMSQL_DIR\\, DISK_USED_B\\ FROM information_schema.mv_disk_usage JOIN information_schema.mv_nodes\\ ON mv_disk_usage.NODE_ID = mv_nodes.ID\\ ORDER BY IP_ADDR\\, PORT";
        //String singlestoreString="21622,2024-06-10 10:47:43.235,UTC,53b9ae806c1c:3306,agg,USER_LOGIN,99995,root,localhost,root@%,password,SUCCESS";
        //String singlestoreString="152310,2024-06-26 13:54:39.421,UTC,53b9ae806c1c:3306,agg,USER_LOGIN,99997,ayoub,10.3.220.131,,authentication_none,FAILURE: Access denied";
        //String singlestoreString="133899,2024-06-24 07:21:24.876,UTC,53b9ae806c1c:3306,agg,1,100000,amine,uc_vector_db,temp_1_31066_6,5822492499834975395,update uc_vector_table set id=999 where id=122";
        //String singlestoreString="133894,2024-06-24 07:20:50.842,UTC,53b9ae806c1c:3306,agg,1,100000,root,uc_vector_db,temp_1_31066_5,17413739249988095469,UPDATE employees SET salary = 80000.00 WHERE id = 1";
        //String singlestoreString="133894,2024-06-24 07:20:50.842,UTC,53b9ae806c1c:3306,agg,1,100000,root,uc_vector_db,temp_1_31066_5,17413739249988095469,DELETE FROM employees WHERE id = 3";
        //String singlestoreString="133894,2024-06-24 07:20:50.842,UTC,53b9ae806c1c:3306,agg,1,100000,root,uc_vector_db,temp_1_31066_5,17413739249988095469,INSERT INTO high_earners (id, name, salary) SELECT id, name, salary FROM employees WHERE salary > 100000";
        //String singlestoreString="133894,2024-06-24 07:20:50.842,UTC,53b9ae806c1c:3306,agg,1,100000,root,uc_vector_db,temp_1_31066_5,17413739249988095469,INSERT INTO employees VALUES (1, 'John Doe', 'Engineer', 75000.00)";
        //String singlestoreString="21881,2024-06-10 11:30:23.310,UTC,53b9ae806c1c:3306,agg,1,99995,root,information_schema,temp_1_5079_5,14799535652165267194,select * from information_schema.schemata where schema_name not in ('cluster', 'memsql') order by schema_name";
        //String singlestoreString="21869,2024-06-10 11:30:19.227,UTC,53b9ae806c1c:3306,agg,1,99995,root,information_schema,temp_1_5078_1,11951698597271110224,SELECT @@max_allowed_packet\\, @@aggregator_id";
        //String singlestoreString="133872,2024-06-24 07:17:33.287,UTC,53b9ae806c1c:3306,agg,1,99993,distributed,uc_vector_db,,6408909967012182963,SHOW TABLE STATUS FROM `uc_vector_db`";
        //String singlestoreString="21866,2024-06-10 11:29:58.831,UTC,53b9ae806c1c:3306,agg,1,99993,distributed,information_schema,temp_1_5077_3,4952243803313514788,/*!90621 OBJECT()*/ SELECT WITH(binary_serialization=1\\, binary_serialization_internal=1) `_MV_QUERY_PROSPECTIVE_HISTOGRAMS`.`DATABASE_NAME` AS `DATABASE_NAME`\\, `_MV_QUERY_PROSPECTIVE_HISTOGRAMS`.`TABLE_NAME` AS `TABLE_NAME`\\, `_MV_QUERY_PROSPECTIVE_HISTOGRAMS`.`COLUMN_NAME` AS `COLUMN_NAME`\\, `_MV_QUERY_PROSPECTIVE_HISTOGRAMS`.`JSON_KEY` AS `JSON_KEY`\\, MAX(`_MV_QUERY_PROSPECTIVE_HISTOGRAMS`.`USAGE_COUNT`) AS `USAGE_COUNT`\\, `_MV_QUERY_PROSPECTIVE_HISTOGRAMS`.`ACTIVITY_NAME` AS `ACTIVITY_NAME` FROM `_MV_QUERY_PROSPECTIVE_HISTOGRAMS` as `_MV_QUERY_PROSPECTIVE_HISTOGRAMS` WITH (disable_encoded_joins = TRUE) WHERE (NOT 0) GROUP BY 6\\, 1\\, 2\\, 3\\, 4 /*!90623 OPTION(NO_QUERY_REWRITE=1\\, INTERPRETER_MODE=INTERPRET_FIRST)*/";
        //String singlestoreString="21866,2024-06-10 11:29:58.831,UTC,53b9ae806c1c:3306,agg,1,99993,distributed,information_schema,temp_1_5077_3,4952243803313514788,SELECT first_name, (SELECT department_name FROM departments WHERE departments.department_id = employees.department_id) AS department_name FROM employees";
        //String singlestoreString="21866,2024-06-10 11:29:58.831,UTC,53b9ae806c1c:3306,agg,1,99993,distributed,information_schema,temp_1_5077_3,4952243803313514788,SELECT first_name FROM employees WHERE department_id IN (SELECT department_id FROM departments WHERE location_id>1500)";


        Context context = new ContextImpl(null, null);
        SingleStoredbGuardiumFilter filter = new SingleStoredbGuardiumFilter("test-id", null, context);

        Event e = ParserTest.getParsedEvent(singlestoreString);

        TestMatchListener matchListener = new TestMatchListener();

        if (e != null) {
            e.setField(Constants.SERVER_IP, "1.1.1.1");
            e.setField(Constants.SERVER_HOSTNAME, "singlestore.server.com");
            Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
            Assert.assertEquals(1, results.size());
            Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        }

    }

    @Test
    public void TestCleanQuery() {
        String originalQuery = "/* ApplicationName=DBeaver 25.1.0 - SQLEditor <Script-13.sql> */ SELECT * FROM customers WHERE city = \"Pune\"";
        String expectedCleanedQuery = "SELECT * FROM customers WHERE city = \"Pune\"";

        Parser parser = new Parser();
        String actualCleanedQuery = parser.cleanQuery(originalQuery);

        Assert.assertEquals(expectedCleanedQuery, actualCleanedQuery);
    }

}

class TestMatchListener implements FilterMatchListener {

    private final AtomicInteger matchCount = new AtomicInteger(0);

    @Override
    public void filterMatched(Event event) {
        matchCount.incrementAndGet();
    }
}

