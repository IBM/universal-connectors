//
// Copyright 2021-2023 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.snowflakedb.utils;

import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;

public class DefaultGuardRecordBuilder {
    public Record buildGuardRecordWithDefaultValues(){
        Record record = new Record();
        record.setAppUserName(Constants.NOT_AVAILABLE);
        record.setDbName(Constants.UNKNOWN_STRING);
        record.setSessionId(Constants.UNKNOWN_STRING);
        record.setTime(buildTime());

        record.setSessionLocator(buildDefaultSessionLocator());
        record.setAccessor(buildDefaultAccessor());
        record.setData(buildDefaultData());
        record.setException(buildDefaultExceptionRecord());

        return record;
    }

    private SessionLocator buildDefaultSessionLocator(){
        SessionLocator sessionLocator = new SessionLocator();
        sessionLocator.setIpv6(false);

        sessionLocator.setClientIp("0.0.0.0");
        sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
        sessionLocator.setClientIpv6(Constants.NOT_AVAILABLE);

        sessionLocator.setServerIpv6(Constants.NOT_AVAILABLE);
        sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);
        sessionLocator.setServerIp("0.0.0.0");

        return sessionLocator;
    }

    private Accessor buildDefaultAccessor(){
        Accessor accessor = new Accessor();

        accessor.setDbProtocol(Constants.DB_PROTOCOL);
        accessor.setServerType(Constants.SERVER_TYPE);
        accessor.setServerHostName(Constants.UNKNOWN_STRING);
        accessor.setSourceProgram(Constants.UNKNOWN_STRING);
        accessor.setLanguage(Constants.LANGUAGE_SNOWFLAKE);

        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);

        accessor.setClient_mac(Constants.UNKNOWN_STRING);
        accessor.setClientHostName(Constants.UNKNOWN_STRING);
        accessor.setClientOs(Constants.UNKNOWN_STRING);
        accessor.setCommProtocol(Constants.UNKNOWN_STRING);
        accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
        accessor.setOsUser(Constants.UNKNOWN_STRING);
        accessor.setServerDescription(Constants.UNKNOWN_STRING);
        accessor.setServerOs(Constants.NOT_AVAILABLE);
        accessor.setServiceName(Constants.UNKNOWN_STRING);
        accessor.setDbUser(Constants.NOT_AVAILABLE);
        accessor.setDbUser(Constants.NOT_AVAILABLE);
        return accessor;
    }

    private Data buildDefaultData() {
        Data data = new Data();
        data.setOriginalSqlCommand(Constants.NOT_AVAILABLE);
        return data;
    }

    private ExceptionRecord buildDefaultExceptionRecord(){
        ExceptionRecord exceptionRecord = new ExceptionRecord();
        exceptionRecord.setDescription(Constants.NOT_AVAILABLE);
        exceptionRecord.setSqlString(Constants.NOT_AVAILABLE);
        return exceptionRecord;
    }

    private Time buildTime(){
        Time t = new Time();
        t.setTimstamp(0);
        t.setMinOffsetFromGMT(0);
        t.setMinDst(0);

        return t;
    }
}
