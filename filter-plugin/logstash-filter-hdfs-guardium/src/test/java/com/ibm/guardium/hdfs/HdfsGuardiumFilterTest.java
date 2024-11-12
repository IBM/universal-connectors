/*
 *
 * Copyright 2020-2021 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 *
 */
package com.ibm.guardium.hdfs;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class HdfsGuardiumFilterTest {
    @Test
    public void testParseOpen() {
		final String audit = "2020-08-15 04:08:44,462 INFO FSNamesystem.audit: allowed=true	ugi=hdfs/cdp711-11.fyre.ibm.com@DBANET4.ROOT (auth:KERBEROS)	ip=/9.46.88.114	cmd=open	src=/ranger/audit/kafka/kafka/20200815/kafka_ranger_audit_cdp711-11.fyre.ibm.com.log	dst=null	perm=null	proto=rpc";

		final String allowed = "true";
		final String timestamp = "2020-08-15 04:08:44,462";
		final String ugi = "hdfs/cdp711-11.fyre.ibm.com@DBANET4.ROOT (auth:KERBEROS)";
		final String ip = "9.46.88.114";
		final String cmd = "open";
		final String src = "/ranger/audit/kafka/kafka/20200815/kafka_ranger_audit_cdp711-11.fyre.ibm.com.log";
		final String dst = HdfsGuardiumFilter.NULL_STRING;
		final String perm = HdfsGuardiumFilter.NULL_STRING;

        Context context = new ContextImpl(null, null);
        HdfsGuardiumFilter filter = new HdfsGuardiumFilter("test-id", null, context);
        TestMatchListener matchListener = new TestMatchListener();
        Event e = new org.logstash.Event();
        
        e.setField("message", audit);
        e.setField(HdfsGuardiumFilter.ALLOW_TAG_STRING, allowed);
        e.setField(HdfsGuardiumFilter.TIME_TAG_STRING, timestamp);
        e.setField(HdfsGuardiumFilter.USER_TAG_STRING, ugi);
        e.setField(HdfsGuardiumFilter.IP_TAG_STRING, ip);
        e.setField(HdfsGuardiumFilter.CMD_TAG_STRING, cmd);
        e.setField(HdfsGuardiumFilter.SRC_TAG_STRING, src);
        e.setField(HdfsGuardiumFilter.DST_TAG_STRING, dst);
        e.setField(HdfsGuardiumFilter.PERM_TAG_STRING, perm);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
    }
    @Test
    public void testParseRename() {
	  final String audit = "2020-08-15 04:27:17,637 INFO FSNamesystem.audit: allowed=true	ugi=hbase/cdp711-51.fyre.ibm.com@DBANET4.ROOT (auth:KERBEROS)	ip=/9.46.89.150	cmd=rename	src=/hbase/WALs/cdp711-51.fyre.ibm.com,16020,1597181114113/cdp711-51.fyre.ibm.com%2C16020%2C1597181114113.cdp711-51.fyre.ibm.com%2C16020%2C1597181114113.regiongroup-0.1597487237491	dst=/hbase/oldWALs/cdp711-51.fyre.ibm.com%2C16020%2C1597181114113.cdp711-51.fyre.ibm.com%2C16020%2C1597181114113.regiongroup-0.1597487237491	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI";

		final String allowed = "true";
		final String timestamp = "2020-08-15 04:27:17,637";
		final String ugi = "hbase/cdp711-51.fyre.ibm.com@DBANET4.ROOT (auth:KERBEROS)";
		final String ip = "9.46.89.150";
		final String cmd = "rename";
		final String src = "/hbase/WALs/cdp711-51.fyre.ibm.com,16020,1597181114113/cdp711-51.fyre.ibm.com%2C16020%2C1597181114113.cdp711-51.fyre.ibm.com%2C16020%2C1597181114113.regiongroup-0.1597487237491";
		final String dst = "/hbase/oldWALs/cdp711-51.fyre.ibm.com%2C16020%2C1597181114113.cdp711-51.fyre.ibm.com%2C16020%2C1597181114113.regiongroup-0.1597487237491";
		final String perm = "hbase:hbase:rw-r--r--";

        Context context = new ContextImpl(null, null);
        HdfsGuardiumFilter filter = new HdfsGuardiumFilter("test-id", null, context);
        TestMatchListener matchListener = new TestMatchListener();
        Event e = new org.logstash.Event();
        
        e.setField("message", audit);
        e.setField(HdfsGuardiumFilter.ALLOW_TAG_STRING, allowed);
        e.setField(HdfsGuardiumFilter.TIME_TAG_STRING, timestamp);
        e.setField(HdfsGuardiumFilter.USER_TAG_STRING, ugi);
        e.setField(HdfsGuardiumFilter.IP_TAG_STRING, ip);
        e.setField(HdfsGuardiumFilter.CMD_TAG_STRING, cmd);
        e.setField(HdfsGuardiumFilter.SRC_TAG_STRING, src);
        e.setField(HdfsGuardiumFilter.DST_TAG_STRING, dst);
        e.setField(HdfsGuardiumFilter.PERM_TAG_STRING, perm);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
    }
    @Test
    public void testParseUserProxy() {
	  final String audit = "2020-08-15 04:13:25,273 INFO FSNamesystem.audit: allowed=true	ugi=hue/cdp711-11.fyre.ibm.com@DBANET4.ROOT (auth:PROXY) via hive/cdp711-11.fyre.ibm.com@DBANET4.ROOT (auth:KERBEROS)	ip=/9.46.88.114	cmd=rename (options=[TO_TRASH])	src=/user/hue/.cloudera_manager_hive_metastore_canary/cloudera_manager_metastore_canary_test_catalog_hive_HIVEMETASTORE_b833a1435460949959526c72f67e2d0f/hive_HIVEMETASTORE_b833a1435460949959526c72f67e2d0f	dst=/user/hue/.Trash/Current/user/hue/.cloudera_manager_hive_metastore_canary/cloudera_manager_metastore_canary_test_catalog_hive_HIVEMETASTORE_b833a1435460949959526c72f67e2d0f/hive_HIVEMETASTORE_b833a1435460949959526c72f67e2d0f1597490005271	perm=hue:hue:rwxr-xr-x	proto=rpc";

		final String allowed = "true";
		final String timestamp = "2020-08-15 04:13:25,273";
		final String ugi = "hue/cdp711-11.fyre.ibm.com@DBANET4.ROOT (auth:PROXY) via hive/cdp711-11.fyre.ibm.com@DBANET4.ROOT (auth:KERBEROS)";
		final String ip = "9.46.88.114";
		final String cmd = "rename (options=[TO_TRASH])";
		final String src = "/user/hue/.cloudera_manager_hive_metastore_canary/cloudera_manager_metastore_canary_test_catalog_hive_HIVEMETASTORE_b833a1435460949959526c72f67e2d0f/hive_HIVEMETASTORE_b833a1435460949959526c72f67e2d0f";
		final String dst = "/user/hue/.Trash/Current/user/hue/.cloudera_manager_hive_metastore_canary/cloudera_manager_metastore_canary_test_catalog_hive_HIVEMETASTORE_b833a1435460949959526c72f67e2d0f/hive_HIVEMETASTORE_b833a1435460949959526c72f67e2d0f1597490005271";
		final String perm = "hue:hue:rwxr-xr-x";

        Context context = new ContextImpl(null, null);
        HdfsGuardiumFilter filter = new HdfsGuardiumFilter("test-id", null, context);
        TestMatchListener matchListener = new TestMatchListener();
        Event e = new org.logstash.Event();
        
        e.setField("message", audit);
        e.setField(HdfsGuardiumFilter.ALLOW_TAG_STRING, allowed);
        e.setField(HdfsGuardiumFilter.TIME_TAG_STRING, timestamp);
        e.setField(HdfsGuardiumFilter.USER_TAG_STRING, ugi);
        e.setField(HdfsGuardiumFilter.IP_TAG_STRING, ip);
        e.setField(HdfsGuardiumFilter.CMD_TAG_STRING, cmd);
        e.setField(HdfsGuardiumFilter.SRC_TAG_STRING, src);
        e.setField(HdfsGuardiumFilter.DST_TAG_STRING, dst);
        e.setField(HdfsGuardiumFilter.PERM_TAG_STRING, perm);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
    }
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
