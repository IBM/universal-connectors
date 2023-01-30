//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.universalconnector.commons.structures;

import java.util.ArrayList;

public class Construct {
    public ArrayList<Sentence> sentences = new ArrayList<>();
    public String fullSql;
    public String redactedSensitiveDataSql;

    public ArrayList<Sentence> getSentences() {
        return sentences;
    }

    /**
     * Specifies the structure of the command. Usually, a single {@link Sentence}.
     * 
     * @param sentences
     */
    public void setSentences(ArrayList<Sentence> sentences) {
        this.sentences = sentences;
    }

    public String getFullSql() {
        return fullSql;
    }

    /**
     * Sets the original (full) data source command, as registered in the data
     * source log. 
     * 
     * @param fullSql For example, the original JSON, stringified.
     */
    public void setFullSql(String fullSql) {
        this.fullSql = fullSql;
    }

    public String getRedactedSensitiveDataSql() {
        return redactedSensitiveDataSql;
    }

    /**
     * Sets a redacted version of the original (full) data source command, where
     * each sensitive field value is replaced by a question mark (?). Guardium
     * appliance will show this in Reports, as sql, as not all users have priviliges
     * to see the fullSql you set in {@link #setFullSql(String) setFullSql} method.
     * 
     * @param redactedSensitiveDataSql
     */
    public void setRedactedSensitiveDataSql(String redactedSensitiveDataSql) {
        this.redactedSensitiveDataSql = redactedSensitiveDataSql;
    }

    @Override
    public String toString() {
        return "Construct{" +
                "sentences=" + sentences +
                ", fullSql='" + fullSql + '\'' +
                ", redactedSensitiveDataSql='" + redactedSensitiveDataSql + '\'' +
                '}';
    }
}