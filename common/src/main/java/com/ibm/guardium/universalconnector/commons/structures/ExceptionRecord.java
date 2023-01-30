//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.universalconnector.commons.structures;

/**
 * Details an exception/error, whenever that occurs. Example errors:
 * Authentication error, Authorization error, syntax errors, etc.<br><br>
 * 
 * Make sure that Session ID, session, Server type, DB protocol, and DB user are
 * also set in their natural place (Record.accessor, Record.session, etc).
 */
public class ExceptionRecord {
    private String exceptionTypeId;
    private String description;
    private String sqlString;

    public String getExceptionTypeId() {
        return exceptionTypeId;
    }

    /**
     * Sets the exception category, which is then used as an error category in
     * Guardium reports.
     * 
     * @param exceptionTypeId A constant known to Guardium, as LOGIN_FAILED or
     *                        SQL_ERROR
     */
    public void setExceptionTypeId(String exceptionTypeId) {
        this.exceptionTypeId = exceptionTypeId;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Sets either (a) a description string or (b) an error code Guardium is
     * familiar with.
     * <p>
     * You have 2 options: (a) Enter a string description of your choice, like
     * "Unauthorized operation (13)" (b) Enter just the error code, if you want
     * Guardium to look it up in its known error code. That option is not suitable
     * for data sources that Guardium is not familiar (no supporting S-TAPs).
     * 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getSqlString() {
        return sqlString;
    }

    /**
     * Sets the original command that caused the error.
     * 
     * @param sqlString
     */
    public void setSqlString(String sqlString) {
        this.sqlString = sqlString;
    }

    @Override
    public String toString() {
        return "ExceptionRecord{" +
                "exceptionTypeId='" + exceptionTypeId + '\'' +
                ", description='" + description + '\'' +
                ", sqlString='" + sqlString + '\'' +
                '}';
    }
}
