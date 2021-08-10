package com.ibm.guardium.s3;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.s3.Parser;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class UserAgentFieldsTest {

    //@Parameter(value = 0)
    public String userAgent;
    public String expectedProgramName;
    public String expectedClientOs;

    public UserAgentFieldsTest(String userAgent, String expectedProgramName, String expectedClientOs){
        this.userAgent = userAgent;
        this.expectedProgramName = expectedProgramName;
        this.expectedClientOs = expectedClientOs;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "{\"userAgent\":\"aws-cli/1.10.32 Python/2.7.9 Windows/7 botocore/1.4.22\"}", "aws-cli", "" },

                { "{\"userAgent\":\"[Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:68.0) Gecko/20100101 Firefox/68.0]\"}", "Mozilla", "Intel Mac OS X 10.14" },

                { "{\"userAgent\":\"[S3Console/0.4, aws-internal/3 aws-sdk-java/1.11.783 Linux/4.9.217-0.1.ac.205.84.332.metal1.x86_64 OpenJDK_64-Bit_Server_VM/25.252-b09 java/1.8.0_252 vendor/Oracle_Corporation]\"}",  "S3Console", ""},

                { "{\"userAgent\":\"cloudtrail.amazonaws.com\"}", "cloudtrail.amazonaws.com", "" }
        });
    }

    @Test
    public void testParseAsConstruct_appSourceProgram(){

        JsonObject inputJSON = (JsonObject) JsonParser.parseString(userAgent);
        Accessor accessor = new Accessor();
        Parser.setUserAgentRelatedFields(accessor, inputJSON);

        System.out.println(accessor.getSourceProgram());
        Assert.assertTrue("sourceProgram is null, programName "+userAgent, accessor.getSourceProgram()!= null);
        Assert.assertTrue("sourceProgram is empty, programName "+userAgent, accessor.getSourceProgram().length() != 0);
        Assert.assertTrue("sourceProgram \""+accessor.getSourceProgram()+"\" is not as expected value "+expectedProgramName +", userAgent "+userAgent, accessor.getSourceProgram().equals(expectedProgramName));
    }

    @Test
    public void testParseAsConstruct_appClientOs(){

        JsonObject inputJSON = (JsonObject) JsonParser.parseString(userAgent);
        Accessor accessor = new Accessor();
        Parser.setUserAgentRelatedFields(accessor, inputJSON);

        System.out.println(accessor.getClientOs());
        Assert.assertTrue("clientOs is null, programName "+userAgent, accessor.getSourceProgram()!= null);
        Assert.assertTrue("clientOs is empty, programName "+userAgent, accessor.getSourceProgram().length() != 0);
        Assert.assertTrue("clientOs \""+accessor.getClientOs()+"\" is not as expected value \""+expectedClientOs +"\", userAgent "+userAgent, accessor.getClientOs().equals(expectedClientOs));
    }
}
