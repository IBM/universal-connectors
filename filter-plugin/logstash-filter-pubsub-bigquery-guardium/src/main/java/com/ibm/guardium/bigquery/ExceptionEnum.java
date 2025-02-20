/*
Copyright IBM Corp. 2021, 2025 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.bigquery;

public enum ExceptionEnum {

	EXCEPTION_CODE("EXCEPTION_CODE", "EXCEPTION_CODE"), EXCEPTION_MESSAGE("EXCEPTION_MESSAGE", "EXCEPTION_MESSAGE");

	private String exceptionCode;
	private String exceptionMessage;

	private ExceptionEnum(String exceptionCode, String exceptionMessage) {
		this.exceptionCode = exceptionCode;
		this.exceptionMessage = exceptionMessage;
	}

	/**
	 * @return the exceptionMessage
	 */
	public String getExceptionMessage() {
		return exceptionMessage;
	}
}
