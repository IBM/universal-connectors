/*
 *
 * Copyright 2020-2021 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 *
 */
package com.ibm.guardium.s3;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.s3.Parser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class AppUserNameTest {

    //@Parameter(value = 0)
    public String userIdentity;
    public String expectedUserName;

    public AppUserNameTest(String userIdentity, String expectedUserName){
        this.userIdentity = userIdentity;
        this.expectedUserName = expectedUserName;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"{\n" +
                        "  \"type\": \"IAMUser\",\n" +
                        "  \"principalId\": \"<dummy_principal_id>\",\n" +
                        "  \"arn\": \"arn:aws:iam::123456789012:user/Alice\",\n" +
                        "  \"accountId\": \"123456789012\",\n" +
                        "  \"accessKeyId\": \"<dummy_access_key_id>\",\n" +
                        "  \"userName\": \"Alice\"\n" +
                        "}", "Alice" },

                { "{\n" +
                        "    \"type\": \"AssumedRole\",\n" +
                        "    \"principalId\": \"<dummy_principal_id>:AssumedRoleSessionName\",\n" +
                        "    \"arn\": \"arn:aws:sts::123456789012:assumed-role/RoleToBeAssumed/MySessionName\",\n" +
                        "    \"accountId\": \"123456789012\",\n" +
                        "    \"accessKeyId\": \"<dummy_access_key_id>\",\n" +
                        "    \"sessionContext\": {\n" +
                        "      \"attributes\": {\n" +
                        "        \"mfaAuthenticated\": \"false\",\n" +
                        "        \"creationDate\": \"20131102T010628Z\"\n" +
                        "      },\n" +
                        "      \"sessionIssuer\": {\n" +
                        "        \"type\": \"Role\",\n" +
                        "        \"principalId\": \"<dummy_principal_id>\",\n" +
                        "        \"arn\": \"arn:aws:iam::123456789012:role/RoleToBeAssumed\",\n" +
                        "        \"accountId\": \"123456789012\",\n" +
                        "        \"userName\": \"RoleToBeAssumed\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "}", "RoleToBeAssumed" },

                { "{\n" +
                        "    \"type\": \"AWSService\",\n" +
                        "    \"invokedBy\": \"s3.amazonaws.com\"\n" +
                        "}",  "AWSService"},

                { "{\n" +
                        "    \"accessKeyId\": \"<dummy_access_key_id>\",\n" +
                        "    \"sessionContext\": {\n" +
                        "      \"attributes\": {\n" +
                        "        \"creationDate\": \"2020-07-09T14:31:10Z\",\n" +
                        "        \"mfaAuthenticated\": \"false\"\n" +
                        "      }\n" +
                        "    },\n" +
                        "    \"accountId\": \"987076625343\",\n" +
                        "    \"principalId\": \"<dummy_principal_id>\",\n" +
                        "    \"userName\": \"ProxyTest\",\n" +
                        "    \"type\": \"IAMUser\",\n" +
                        "    \"arn\": \"arn:aws:iam::987076625343:user/ProxyTest\"\n" +
                        "  }", "ProxyTest" }
        });
    }

    @Test
    public void testParseAsConstruct_appUserName() throws Exception{

        JsonObject inputJSON = (JsonObject) JsonParser.parseString(userIdentity);
        String userName = Parser.searchForAppUserName(inputJSON);
        System.out.println(userName);
        Assert.assertTrue("app user name is null, userIdentity "+userIdentity, userName!= null);
        Assert.assertTrue("app user name is empty, userIdentity "+userIdentity, userName.length() != 0);
        Assert.assertTrue("app user name is not as expected value "+expectedUserName +" userIdentity "+userIdentity, userName.equals(expectedUserName));
    }
}
