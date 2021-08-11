//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.universalconnector.commons.structures;

/**
 * Contains details about the user who accessed the data source, 
 * and sets up the grammar parsing responsibility and language.
 */
public class Accessor {
    // TYPE
    public static final String  DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL = "CONSTRUCT"; // Signals Guardium not to parse
    public static final String  DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL = "TEXT";  // Signals Guardium to parse (supported DB languages only)
    
    public static final String LANGUAGE_FREE_TEXT_STRING = "FREE_TEXT"; // Used when no need to parse by Guardium Sniffer 

	private String dbUser;
    private String serverType;
    private String serverOs;
    private String clientOs;
    private String clientHostName;
    private String serverHostName;
    private String commProtocol;
    private String dbProtocol;
    private String dbProtocolVersion;
    private String osUser;
    private String sourceProgram;
    private String client_mac;
    private String serverDescription;
    private String serviceName;
    private String language;
    private String dataType;

    public String getDbUser() {
        return dbUser;
    }

    /**
     * Sets the data source user.
     * 
     * @param dbUser
     */
    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getServerType() {
        return serverType;
    }
    
    /**
     * Sets and identifier for data source type (or product name)
     * 
     * @param serverType For example, MongoDB, AmazonS3. Refrain from using spaces.
     */
    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getServerOs() {
        return serverOs;
    }

    public void setServerOs(String serverOs) {
        this.serverOs = serverOs;
    }

    public String getClientOs() {
        return clientOs;
    }

    public void setClientOs(String clientOs) {
        this.clientOs = clientOs;
    }

    public String getClientHostName() {
        return clientHostName;
    }

    public void setClientHostName(String clientHostName) {
        this.clientHostName = clientHostName;
    }

    public String getServerHostName() {
        return serverHostName;
    }

    public void setServerHostName(String serverHostName) {
        this.serverHostName = serverHostName;
    }

    public String getCommProtocol() {
        return commProtocol;
    }

    public void setCommProtocol(String commProtocol) {
        this.commProtocol = commProtocol;
    }

    public String getDbProtocol() {
        return dbProtocol;
    }

    /**
     * Sets an identifier for data source/protocol used to get the record.
     * 
     * For example: “MongoDB native log"
     * 
     * @param dbProtocol Limited to 18-20 characters; a "UC_" prefix added when sent
     *                   to Guardium.
     */
    public void setDbProtocol(String dbProtocol) {
        this.dbProtocol = dbProtocol;
    }

    public String getDbProtocolVersion() {
        return dbProtocolVersion;
    }

    /**
     * Sets the data source protocol version. (Optional)
     * 
     * @param dbProtocolVersion
     */
    public void setDbProtocolVersion(String dbProtocolVersion) {
        this.dbProtocolVersion = dbProtocolVersion;
    }

    public String getOsUser() {
        return osUser;
    }

    public void setOsUser(String osUser) {
        this.osUser = osUser;
    }

    public String getSourceProgram() {
        return sourceProgram;
    }

    /**
     * Sets the source application used to run the data source command. (Optional)
     * 
     * @param sourceProgram
     */
    public void setSourceProgram(String sourceProgram) {
        this.sourceProgram = sourceProgram;
    }

    public String getClient_mac() {
        return client_mac;
    }

    public void setClient_mac(String client_mac) {
        this.client_mac = client_mac;
    }

    public String getServerDescription() {
        return serverDescription;
    }

    public void setServerDescription(String serverDescription) {
        this.serverDescription = serverDescription;
    }

    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the data source service name. Populate with the data source name, if
     * service name is not applicable to your data source. Origianally used to state
     * the OS level service that runs the DB instace.
     * 
     * @param serviceName Usually identical to DB name, or container identifier in data source.
     * @see Record#setDbName(String)
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLanguage() {
        return language;
    }

    /**
     * Signals to Guardium which language parser to use, if dataType was set to
     * DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL. In that case, language acronym should
     * comply with languages supported by Guardium.
     * 
     * Optional if dataType is set to DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL.
     * 
     * @param language Acronym of language, as known to Guardium.
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDataType() {
        return dataType;
    }

    /**
     * Signals to Guardium who is responsible for parsing the command grammar. If
     * Guardium should parse the data source command, it is required to state the
     * command language in {@link #setLanguage(String) setLanguage} method, and use
     * {@link Data#setOriginalSqlCommand(String) Data.setOriginalSqlCommand}
     * 
     * @param dataType Either {@link #DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL} or
     *                 {@link #DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL}.
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
