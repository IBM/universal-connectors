/*
Copyright IBM Corp. 2021, 2025 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.guardium.bigquery;

import java.util.HashSet;

import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
/**
 * DTO to capture information from different parts of JSON msg.
 *
 */
public class BigQueryDTO {

	public Sentence sentence;
	public HashSet<String> DBName = new HashSet<String>();
	public Data data=new Data();
	public Construct construct;
	
	
	public Construct getConstruct() {
		return construct;
	}
	public void setConstruct(Construct construct) {
		this.construct = construct;
	}
	public Data getData() {
		return data;
	}
	public void setData(Data data) {
		this.data = data;
	}
	public BigQueryDTO() {
		super();
	}
	public BigQueryDTO(Sentence construct) {
		super();
		this.sentence = construct;
	}

	 

	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}

	public HashSet<String> getDBName() {
		return DBName;
	}

	public void setDBName(HashSet<String> dBName) {
		DBName = dBName;
	}

}
