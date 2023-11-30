/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.azure.cosmos;

public enum SuccessCode {
	CREATED(201), OK(200), NO_CONTENT(204), ACCEPTED(202);

	private final int code;

	private SuccessCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
    /**
     * Method to find the number
     * @param number
     * @return
     */
	public static SuccessCode findByNumber(int number) {
		for (SuccessCode code : values()) {
			if (code.getCode() == number) {
				return code;
			}
		}
		return null;
	}
}
