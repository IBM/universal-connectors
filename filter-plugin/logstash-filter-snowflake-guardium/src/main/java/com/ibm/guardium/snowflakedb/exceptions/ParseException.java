//
// Copyright 2021-2023 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.snowflakedb.exceptions;

public class ParseException extends Exception{
    private String message;
    public ParseException(String msg){
        this.message = msg;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
