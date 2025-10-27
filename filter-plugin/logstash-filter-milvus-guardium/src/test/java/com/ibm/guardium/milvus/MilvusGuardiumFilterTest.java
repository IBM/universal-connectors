package com.ibm.guardium.milvus;

import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MilvusGuardiumFilterTest {

    FilterMatchListener matchListener = new TestMatchListener();

    String id = "1";
    Configuration config = new ConfigurationImpl(Collections.singletonMap("source", ""));
    Context context = new ContextImpl(null, null);
    MilvusGuardiumFilter filter = new MilvusGuardiumFilter(id, config, context);

    @Test
    void test() {
        String payload = "LEEF:1.0|Zilliz|Milvus|1.0|DropAlias-GrpcPermissionDenied|devTime=2025/02/06 21:38:53.557 +00:00\tdevTimeFormat=yyyy/MM/dd HH:mm:ss.SSS xxx\tuserName=zilliz\tuserAddress=tcp-172.17.0.1:47286\tdatabaseName=default\tqueryExpression=Unknown\terrorCode=65535\terrorMessage=rpc error: code = PermissionDenied desc = PrivilegeDropAlias: permission deny to zilliz in the `default` database";
        Event event = new org.logstash.Event();
        event.setField("message", payload);
        Collection<Event> actualResponse = filter.filter(Collections.singletonList(event), matchListener);

        assertNotNull(actualResponse.toArray(new Event[0])[0].getField("GuardRecord"));
    }

    @Test
    void test_filebeat(){
        String payload = "{ \"@timestamp\" : \"2025-03-24T15:23:41.631Z\", " +
                "\"@version\" : \"1\", " +
                "\"type\" : \"filebeat\", " +
                "\"service\" : \"milvus\", " +
                "\"host\" : \"{name=lima-rancher-desktop}\", " +
                "\"log\" : \"{offset=28947, file={path=/tmp/milvus_access/access.log}}\", " +
                "\"GuardRecord\" : \"{\\\"connectorName\\\":\\\"filebeat\\\",\\\"connectorId\\\":\\\"36\\\",\\\"sessionId\\\":\\\"\\\",\\\"dbName\\\":\\\"Unknown\\\",\\\"appUserName\\\":\\\"wronguser\\\",\\\"time\\\":{\\\"timestamp\\\":1742829821046,\\\"minOffsetFromGMT\\\":0,\\\"minDst\\\":0},\\\"sessionLocator\\\":{\\\"clientIp\\\":\\\"0.0.0.0\\\",\\\"clientPort\\\":0,\\\"serverIp\\\":\\\"0.0.0.0\\\",\\\"serverPort\\\":0,\\\"isIpv6\\\":false,\\\"clientIpv6\\\":\\\"0000:0000:0000:0000:0000:ffff:0000:0000\\\",\\\"serverIpv6\\\":\\\"0000:0000:0000:0000:0000:ffff:0000:0000\\\"},\\\"accessor\\\":{\\\"dbUser\\\":\\\"wronguser\\\",\\\"serverType\\\":\\\"Milvus\\\",\\\"serverOs\\\":\\\"\\\",\\\"clientOs\\\":\\\"\\\",\\\"clientHostName\\\":\\\"\\\",\\\"serverHostName\\\":\\\"\\\",\\\"commProtocol\\\":\\\"\\\",\\\"dbProtocol\\\":\\\"\\\",\\\"dbProtocolVersion\\\":\\\"\\\",\\\"osUser\\\":\\\"\\\",\\\"sourceProgram\\\":\\\"\\\",\\\"client_mac\\\":\\\"\\\",\\\"serverDescription\\\":\\\"\\\",\\\"serviceName\\\":\\\"\\\",\\\"language\\\":\\\"Milvus\\\",\\\"dataType\\\":\\\"TEXT\\\"},\\\"data\\\":null,\\\"exception\\\":{\\\"exceptionTypeId\\\":\\\"SQL_ERROR\\\",\\\"description\\\":\\\"rpc error: code = Unauthenticated desc = auth check failure, please check username and password are correct\\\",\\\"sqlString\\\":\\\"Connect-GrpcUnauthenticated\\\"},\\\"recordsAffected\\\":null}\", " +
                "\"tags\" : \"ConvertedList{delegate=[milvus, beats_input_codec_plain_applied]}\", " +
                "\"message\" : \"LEEF:1.0|Zilliz|Milvus|1.0|Connect-GrpcUnauthenticated|devTime=2025/03/24 15:23:41.046 +00:00	devTimeFormat=yyyy/MM/dd HH:mm:ss.SSS xxx	userName=wronguser	userAddress=tcp-[::1]:55046	databaseName=Unknown	queryExpression=Unknown	errorCode=65535	errorMessage=rpc error: code = Unauthenticated desc = auth check failure, please check username and password are correct\", " +
                "\"ecs\" : \"{version=8.0.0}\", " +
                "\"agent\" : \"{id=ff90b92a-0855-4352-a153-473b387006b1, type=filebeat, ephemeral_id=dc200cee-a9ce-43e7-b263-57236012a14a, name=lima-rancher-desktop, version=8.16.1}\", " +
                "\"input\" : \"{type=log}\" }";

        Event event = new org.logstash.Event();
        event.setField("message", payload);
        Collection<Event> actualResponse = filter.filter(Collections.singletonList(event), matchListener);

        assertNotNull(actualResponse.toArray(new Event[0])[0].getField("GuardRecord"));

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
}
