//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.universalconnector.commons.structures;

/**
 * A standard Guardium object, required while developing a plug-in for Guardium Universal
 * Connector (a feature within IBM Security Guardium), as a way to insert data
 * to Guardium from potentially any data source.
 * <p>
 * Contents, in brief:
 * <ul>
 * <li>Time specifies the time the command was executed
 * <li>Session ID identifies a specific connection to the data source by a specific user
 * <li>Accessor describes the user who tried to access the data and sets up the
 * grammar parsing responsibility and language.
 * <li>SessionLocator describes location details about the data source
 * connection
 * <li>Data or Exception (one of the two). If the command was successful, Data
 * details its structure. If the command resulted in an error, an
 * ExceptionRecord describes the command and error.
 * <li>DB name identifies a container within the data source.
 * <li>App username specifies an application username
 * </ul>
 */
public class Record {

    /**
     * mandatory field - uniquely identifies sessionid
     */
    private String sessionId;

    /**
     * optional field - name of the database (db scheme)
     */
    private String dbName;

    /**
     * optional field - name of application user
     */
    private String appUserName;

    /**
     *  mandatory field - time of the event, in ms
     */
    private Time   time;

    /**
     * mandatory field - object that contains session related details
     */
    private SessionLocator  sessionLocator;

    /**
     * mandatory field - object that contains client connection details
     */
    private Accessor        accessor;

    /**
     * one of fields [data/ exception] must appear in the record
     * otherwise the record considered invalid
     */

    /**
     * mandatory field (one of fields [data/ exception] must appear in the record)
     * object that contains the details of an actual activity performed
     */
    private Data            data;

    /**
     * mandatory field (one of fields [data/ exception] must appear in the record)
     * object that contains session related details
     */
    private ExceptionRecord exception;

    public boolean isException(){
        return  (this.exception != null);
    }

    public Time getTime() {
        return time;
    }

    /**
     * Sets the {@link Time} the command was executed.
     * 
     * @param time
     */
    public void setTime(Time time) {
        this.time = time;
    }

    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets a [mandatory] session ID, which should uniquely identify a specific connection to
     * the data source by a specific user.
     * 
     * @param sessionId
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public SessionLocator getSessionLocator() {
        return sessionLocator;
    }

    /**
     * Sets {@link SessionLocator}, which describes location details about the data source
     * connection
     * 
     * @param sessionLocator
     */
    public void setSessionLocator(SessionLocator sessionLocator) {
        this.sessionLocator = sessionLocator;
    }

    public Accessor getAccessor() {
        return accessor;
    }

    /**
     * Sets an {@link Accessor} that describes the user who tried to access the data and
     * sets up the grammar parsing responsibility and language.
     * 
     * @param accessor
     */
    public void setAccessor(Accessor accessor) {
        this.accessor = accessor;
    }

    public String getDbName() {
        return dbName;
    }

    /**
     * Sets the name of the database, schema, or the container within the data
     * source that that relates to the command.
     * 
     * @param dbName
     * @see Accessor#setServiceName(String)
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getAppUserName() {
        return appUserName;
    }

    /**
     * Sets the name of the application user. (Optional)
     * 
     * @param appUserName
     */
    public void setAppUserName(String appUserName) {
        this.appUserName = appUserName;
    }

    public Data getData() {
        return data;
    }

    /**
     * Sets {@link Data}, if the data source command succeeded. 
     * 
     * @param data
     */
    public void setData(Data data) {
        this.data = data;
    }

    public ExceptionRecord getException() {
        return exception;
    }

    /**
     * Sets {@link ExceptionRecord}, if data source commands failed, with details about the error. 
     * 
     * @param exception
     */
    public void setException(ExceptionRecord exception) {
        this.exception = exception;
    }
}
