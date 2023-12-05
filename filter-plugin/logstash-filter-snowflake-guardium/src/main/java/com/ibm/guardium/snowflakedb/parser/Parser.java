//
// Copyright 2021-2023 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.snowflakedb.parser;

import com.ibm.guardium.snowflakedb.utils.Constants;
import com.ibm.guardium.snowflakedb.exceptions.ParseException;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Parser {

    Record parseRecord(Map<String, Object> event) throws ParseException;

    default String getClientOS(Map<String, String> clientEnvironment) {
        String osMake = Constants.UNKNOWN_STRING;
        String osVersion = Constants.UNKNOWN_STRING;

        Optional<String> optOsMake = Optional.ofNullable(
                clientEnvironment.get(Constants.CLIENT_OS)
        ).map(Object::toString);

        if(optOsMake.isPresent()){
            osMake = optOsMake.get();
        }

        Optional<String> optOsVersion = Optional.ofNullable(
                clientEnvironment.get(Constants.CLIENT_OS_VERSION)
        ).map(Object::toString);

        if(optOsMake.isPresent()){
            osVersion = optOsVersion.get();
        }

        String clientOs = StringUtils.join(new String[] {osMake, osVersion},", ").trim();
        if(!clientOs.isEmpty()){
            return clientOs;
        }
        return Constants.UNKNOWN_STRING;
    }

    static LocalDateTime parseTime(String ts){
        DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS[[XXX][X]]"));

        DateTimeFormatter formatter = dateTimeFormatterBuilder.toFormatter();

        try {
            return LocalDateTime.parse(ts,formatter);
        } catch (DateTimeParseException e) {
            DateTimeFormatterBuilder dateTimeFormatterBuilderUTC = new DateTimeFormatterBuilder()
                    .append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
            formatter = dateTimeFormatterBuilderUTC.toFormatter();

            return LocalDateTime.parse(ts, formatter);
        }
    }
}
