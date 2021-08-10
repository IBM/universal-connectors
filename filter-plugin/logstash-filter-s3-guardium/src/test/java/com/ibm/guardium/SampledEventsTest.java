package com.ibm.guardium;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.samples.EventSamples;
import com.ibm.guardium.s3.Parser;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;
import java.util.stream.Collectors;

@RunWith(value = Parameterized.class)
public class SampledEventsTest {

    //@Parameter(value = 0)
    public EventSamples.Sample event;

    public SampledEventsTest(EventSamples.Sample event){
        this.event = event;
    }

    @Parameters
    public static Collection<Object[]> data() {
        Set<EventSamples.Sample> allSamples = EventSamples.getSamplesContainer().getAllSamples();
        List<Object[]> data = allSamples.stream().map(s->new Object[]{s}).collect(Collectors.toList());
        return data;
    }



    @Test
    public void testParseAsConstruct_dbname() throws Exception{

        JsonObject inputJSON = (JsonObject) JsonParser.parseString(event.getJsonStr());
        Record result = Parser.buildRecord(inputJSON);
        Assert.assertTrue("db name is null, event "+event, result.getDbName() != null);
        // for PutAccessPolicy event we do not have dbname available
        if (!EventSamples.EventName.PutAccessPolicy.equals(event.getEventName()) &&
            !EventSamples.EventName.CreateAccessPoint.equals(event.getEventName())) {
            Assert.assertTrue("db name is empty, event " + event, result.getDbName().length() != 0);
        }
    }

    @Test
    public void testParseAsConstruct_appUserName() throws Exception{
        JsonObject inputJSON = (JsonObject) JsonParser.parseString(event.getJsonStr());
        Record result = Parser.buildRecord(inputJSON);
        Assert.assertTrue("app user name is null, event "+event, result.getAppUserName()!= null);
        Assert.assertTrue("app user name is empty, event "+event, result.getAppUserName().length() != 0);
    }

    @Test
    public void testParseAsConstruct_SessionLocator_clientIp() throws Exception{

        JsonObject inputJSON = (JsonObject) JsonParser.parseString(event.getJsonStr());
        Record result = Parser.buildRecord(inputJSON);
        String clientIp = result.getSessionLocator().getClientIp();
        System.out.println(clientIp);
        Assert.assertTrue("app user name is null, event "+event, clientIp!= null);
        Assert.assertTrue("app user name is empty, event "+event, clientIp.length() != 0);
        Assert.assertTrue("app user name is N/A, event "+event, !clientIp.equalsIgnoreCase(Parser.UNKNOWN_STRING));
    }

    @Test
    public void testParseAsConstruct_SessionLocator_serverIp() throws Exception{

        JsonObject inputJSON = (JsonObject) JsonParser.parseString(event.getJsonStr());
        Record result = Parser.buildRecord(inputJSON);
        String serverIp = result.getSessionLocator().getServerIp();
        System.out.println(serverIp);
        Assert.assertTrue("app user name is null, event "+event, serverIp!= null);
        Assert.assertTrue("app user name is empty, event "+event, serverIp.length() != 0);
        Assert.assertTrue("app user name is N/A, event "+event, !serverIp.equalsIgnoreCase(Parser.UNKNOWN_STRING));
    }

}
