//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.universalconnector.commons.structures;

/**
 * An object that describes a successful data source command. It contains
 * details on the command structure (Construct) or the original command, if
 * parsing the command grammar is within Guardium responsibility.
 */
public class Data {
    private Construct construct;
    private String originalSqlCommand;

    /**
     * Sets the original data source statement. 
     * Required if Guardium is set to parse the command grammar.
     * Otherwise, use {@link #setConstruct(Construct) setConstruct} method, instead.
     * 
     * @param originalSqlCommand    
     * @see Accessor#setDataType(String) Accessor.setDataType 
     */
    public void setOriginalSqlCommand(String originalSqlCommand) {
        this.originalSqlCommand = originalSqlCommand;
    }

    public Construct getConstruct() {
        return construct;
    }

    /**
     * Sets a parsed data source command as a {@link Construct}. Required if the
     * plug-in takes responsibility for parsing the command grammar. 
     * 
     * @param construct
     * @see Accessor#setDataType(String) Accessor.setDataType
     */
    public void setConstruct(Construct construct) {
        this.construct = construct;
    }

    public String getOriginalSqlCommand() {
        return originalSqlCommand;
    }

    @Override
    public String toString() {
        return "Data{" +
                "construct=" + construct +
                ", originalSqlCommand='" + originalSqlCommand + '\'' +
                '}';
    }
}
