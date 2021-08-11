//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.universalconnector.commons.structures;

import java.util.ArrayList;

/**
 * A parsed part of a data source command; added to a {@link Construct} [of
 * sentences]. Each sentence contains at least the command verb and the object.
 * <p>
 * For example, <code>db.collectionx.find();</code> MongoDB command can be
 * broken into a Sentence <code>{verb: "find", objects: ["collectionA"] }</code>
 */
public class Sentence {
    private String verb;
    private ArrayList<SentenceObject> objects = new ArrayList<>();
    private ArrayList<Sentence> descendants = new ArrayList<>();
    private ArrayList<String> fields = new ArrayList<>();

    public Sentence(String verb) {
        this.verb = verb;
    }

    public String getVerb() {
        return verb;
    }

    /**
     * Sets the action of the atomic command.
     *   
     * @param verb
     */
    public void setVerb(String verb) {
        this.verb = verb;
    }

    public ArrayList<SentenceObject> getObjects() {
        return objects;
    }

    /**
     * Sets the objects that were involved in the atomic command. For example: A
     * compound query (like JOIN) can select fields from two objects.
     * 
     * @param objects
     */
    public void setObjects(ArrayList<SentenceObject> objects) {
        this.objects = objects;
    }

    public ArrayList<Sentence> getDescendants() {
        return descendants;
    }

    /**
     * Sets inner parsed sentences. (Optional) 
     * 
     * @param descendants
     */
    public void setDescendants(ArrayList<Sentence> descendants) {
        this.descendants = descendants;
    }

    public ArrayList<String> getFields() {
        return fields;
    }

    /**
     * Sets specific fields that were involved in the atomic command. (Optional)
     * 
     * @param fields    Fields involved in the command. (Optional)
     */
    public void setFields(ArrayList<String> fields) {
        this.fields = fields;
    }
}