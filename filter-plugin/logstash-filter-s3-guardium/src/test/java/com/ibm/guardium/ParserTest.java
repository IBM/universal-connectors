/*
 *
 * Copyright 2020-2021 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 *
 */
package com.ibm.guardium;

import static org.junit.Assert.assertEquals;

import com.google.gson.*;

import com.ibm.guardium.samples.EventSamples;
import com.ibm.guardium.s3.Parser;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

public class ParserTest {
    private static final Gson gson = new Gson();

    @Test
    public void testParseAsConstruct_Find() {

        // final String actualResult = Parser.Parse(mongoJson);
        JsonObject inputJSON = (JsonObject) JsonParser.parseString(EventSamples.getSamplesByEventName(EventSamples.EventName.GetObject).get(0).getJsonStr());
        try {
            // Record result = Parser.buildRecord(inputJSON);
            System.out.println("result");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        final Sentence sentence = result.sentences.get(0);
//        Assert.assertEquals("find", sentence.verb);
//        Assert.assertEquals("bios", sentence.objects.get(0).name);
//        Assert.assertEquals("collection", sentence.objects.get(0).type);
    }


}