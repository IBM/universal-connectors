package org.logstashplugins;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.softwareag.adabas.auditing.logstash.AdabasAuditingFilter;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class AdabasAuditingFilterTest {

    @Test
    public void testAdabasReadFilter() throws ParseException {
        HashMap<String, Object> testData = getTestData();

        String sourceField = "adabas_auditing";
        Configuration config = new ConfigurationImpl(Collections.singletonMap("source", sourceField));

        Context context = new ContextImpl(null, null);
        AdabasAuditingFilter filter = new AdabasAuditingFilter("test-id", config, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField(sourceField, testData);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        // Assert.assertEquals(
        // "{\"sessionId\":\"14800101\",\"dbName\":\"22131\",\"appUserName\":\"GER\",\"time\":{\"timstamp\":1738675401420,\"minOffsetFromGMT\":0,\"minDst\":0},\"sessionLocator\":null,\"accessor\":{\"dbUser\":\"GER\",\"serverType\":\"Adabas\",\"serverOs\":\"\",\"clientOs\":\"\",\"clientHostName\":\"\",\"serverHostName\":\"DA3F\",\"commProtocol\":\"\",\"dbProtocol\":\"Adabas
        // native
        // audit\",\"dbProtocolVersion\":\"\",\"osUser\":\"\",\"sourceProgram\":\"EMPLAPP\",\"client_mac\":\"\",\"serverDescription\":\"\",\"serviceName\":\"\",\"language\":\"FREE_TEXT\",\"dataType\":\"CONSTRUCT\"},\"data\":null,\"exception\":null}",
        // e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString());
        System.out.println(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString());
        Assert.assertEquals(1, matchListener.getMatchCount());
    }

    private HashMap<String, Object> getTestData() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        HashMap<String, Object> testData = new HashMap<>();

        HashMap<String, Object> uabiItems = new HashMap<>();
        uabiItems.put("UABIDBID", 22131);
        uabiItems.put("messageType", "CMD");
        uabiItems.put("UABIITIM", formatter.parse("2025-02-04T14:23:16.176Z"));
        uabiItems.put("UABISNAM", "EMPL");
        uabiItems.put("UABINUCI", 0);
        uabiItems.put("UABITY", "CMD");

        HashMap<String, Object> uaicm = new HashMap<>();
        uaicm.put("UAICMAUD", "DE00AC140A6D2607");
        uaicm.put("UAICMSID", "GER");
        uaicm.put("UAICMISN", 109425);
        uaicm.put("UAICMGID",
                "000498E885620001404040404040404000F72580C7C5D940404040F1");
        uaicm.put("UAICMFNR", 1);
        uaicm.put("UAICMISG", 0);
        uaicm.put("UAICMCMD", "S1");

        uabiItems.put("UAICM", uaicm);

        // // "UABD_ITEMS" => [
        ArrayList<HashMap<String, Object>> uabdItems = new ArrayList<>();
        HashMap<String, Object> payloadData = new HashMap<>();
        payloadData.put("AREACODE", "");
        payloadData.put("JOBTITLE", "");
        payloadData.put("MIDDLE_NAME", "W.");
        payloadData.put("CITY", "Auckland");
        payloadData.put("SEX", "");
        payloadData.put("LEAVE_TAKEN", 0);
        payloadData.put("COUNTRY", "NZ");
        payloadData.put("LEAVE_DUE", 0);
        payloadData.put("MARSTAT", "");
        ArrayList<String> addressLine = new ArrayList<>();
        addressLine.add("Line 1");
        addressLine.add("Line 2");
        addressLine.add("Line 3");
        addressLine.add("Line 4");
        addressLine.add("Line 5");
        payloadData.put("ADDRESS_LINE", addressLine);
        payloadData.put("LANG", new ArrayList<>());
        payloadData.put("PERSONNEL_ID", "D2345678");
        payloadData.put("PHONE", "");
        payloadData.put("POSTCODE", "");
        payloadData.put("NAME", "Johnson");
        ArrayList<HashMap<String, Object>> income = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            HashMap<String, Object> incomeItem = new HashMap<>();
            incomeItem.put("SALARY", 0);
            ArrayList<Integer> bonus = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                bonus.add(0);
            }
            incomeItem.put("BONUS", bonus);
            incomeItem.put("CURRCODE", "");
            income.add(incomeItem);
        }
        payloadData.put("INCOME", income);
        payloadData.put("FIRST_NAME", "Max");
        payloadData.put("DEPT", "SALE01");
        ArrayList<HashMap<String, Object>> leaveBooked = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            HashMap<String, Object> leaveBookedItem = new HashMap<>();
            leaveBookedItem.put("LEAVE_END", 0);
            leaveBookedItem.put("LEAVE_START", 0);
            leaveBooked.add(leaveBookedItem);
        }
        payloadData.put("LEAVE_BOOKED", leaveBooked);

        HashMap<String, Object> uabdItem = new HashMap<>();
        uabdItem.put("PAYLOAD_DATA", payloadData);
        uabdItem.put("UABDISNG", 0);
        uabdItem.put("UABDRETS", formatter.parse("2023-11-14T12:08:39.863Z"));
        uabdItem.put("UABDSUBC", "");
        uabdItem.put("UABDRSP", 0);
        uabdItem.put("UABDTY", "DATA");
        uabdItem.put("UABDISN", 109425);
        uabdItems.add(uabdItem);

        uabdItem = new HashMap<>();
        uabdItem.put("UABDISNG", 0);
        uabdItem.put("UABDRETS", formatter.parse("2023-11-13T11:58:04.861Z"));
        uabdItem.put("UABDSUBC", "");
        uabdItem.put("UABDRSP", 0);
        uabdItem.put("UABDTY", "ACBX");
        uabdItem.put("UABDISN", 0);
        HashMap<String, Object> payloadACBX = new HashMap<>();
        payloadACBX.put("CMDID", "14800101");
        payloadACBX.put("USERFIELD", "00000000000000000000000000000000");
        payloadACBX.put("RSPCODE", 0);
        payloadACBX.put("FNR", 1);
        payloadACBX.put("RSPSUBCODE", 0);
        payloadACBX.put("ISN", 109425);
        payloadACBX.put("CMDCODE", "S1");
        payloadACBX.put("ISQ", 1);
        payloadACBX.put("ADDITIONS1", "4040404040404040");
        payloadACBX.put("DBID", 0);
        payloadACBX.put("RSPSUBCODECHAR", "");
        uabdItem.put("PAYLOAD_ACBX", payloadACBX);
        uabdItems.add(uabdItem);

        uabdItem = new HashMap<>();
        uabdItem.put("UABDRETS", formatter.parse("2020-10-01T16:59:24.839Z"));
        uabdItem.put("UABDSUBC", "");
        uabdItem.put("UABDRSP", 0);
        uabdItem.put("UABDTY", "FBUF");
        uabdItem.put("UABDISN", 0);
        HashMap<String, Object> payloadFBUF = new HashMap<>();
        payloadFBUF.put("FORMATBUFFER", "AI1-5,20,A,AC,20,A,AD,20,A,AE,20,A,AJ,20,A,AO,6,A,AL,3,A.");
        uabdItem.put("PAYLOAD_FBUF", payloadFBUF);
        uabdItems.add(uabdItem);

        uabdItem = new HashMap<>();
        uabdItem.put("UABDISNG", 0);
        uabdItem.put("UABDRETS", formatter.parse("2021-07-27T14:54:05.440Z"));
        uabdItem.put("UABDSUBC", "");
        uabdItem.put("UABDRSP", 0);
        uabdItem.put("UABDTY", "CLNT");
        uabdItem.put("UABDISN", 0);
        HashMap<String, Object> payloadCLNT = new HashMap<>();
        payloadCLNT.put("SECUID", "GER");
        payloadCLNT.put("LPARNAME", "DA3F");
        payloadCLNT.put("NATSTMT", "1480");
        payloadCLNT.put("USERTYPE", "COMPLETE");
        payloadCLNT.put("ACCTINFO", "ACCOUNT 2005");
        payloadCLNT.put("COM-PLETE-TID", 7);
        payloadCLNT.put("TPUSERID", "GER");
        payloadCLNT.put("NATAPPL", "GER");
        payloadCLNT.put("NATPROG", "EMPLAPP");
        payloadCLNT.put("NATLIB", "GER");
        payloadCLNT.put("NATUID", "GER");
        payloadCLNT.put("CALLPGM", "NAT92");
        payloadCLNT.put("JOBID", "S0206859");
        uabdItem.put("PAYLOAD_CLNT", payloadCLNT);
        uabdItems.add(uabdItem);
        uabiItems.put("UABIDCNT", 3);
        uabiItems.put("UABIPTIM", formatter.parse("2025-02-04T14:23:21.420Z"));
        uabiItems.put("UABD_ITEMS", uabdItems);

        testData.put("UABI_ITEMS", uabiItems);
        testData.put("UABHNAME", "ANSERVER");
        testData.put("UABHTIME", formatter.parse("2025-02-04T14:23:21.420Z"));
        testData.put("UABHID", 22134);
        testData.put("UABHNUCI", 0);
        return testData;
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