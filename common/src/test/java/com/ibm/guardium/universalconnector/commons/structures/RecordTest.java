//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.universalconnector.commons.structures;

import org.junit.Assert;
import org.junit.Test;

public class RecordTest {

    Record record = new Record();

    @Test 
    public void testAccessorPostManipulation() {
        String actual = "dummy-server";
        record.setAccessor(new Accessor());
        record.getAccessor().setServerHostName(actual);
        Assert.assertEquals(record.getAccessor().getServerHostName(), actual);
    }
}