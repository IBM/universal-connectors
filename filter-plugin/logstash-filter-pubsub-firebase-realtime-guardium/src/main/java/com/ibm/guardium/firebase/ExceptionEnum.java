/*

© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0

*/
package com.ibm.guardium.firebase;

public enum ExceptionEnum {

	EXCEPTION_CODE("EXCEPTION_CODE", "EXCEPTION_CODE"), EXCEPTION_MESSAGE("EXCEPTION_MESSAGE", "EXCEPTION_MESSAGE");

	private String exceptionCode;
	private String exceptionMes***REMOVED***ge;

	private ExceptionEnum(String exceptionCode, String exceptionMes***REMOVED***ge) {
		this.exceptionCode = exceptionCode;
		this.exceptionMes***REMOVED***ge = exceptionMes***REMOVED***ge;
	}

	/**
	 * @return the exceptionMes***REMOVED***ge
	 */
	public String getExceptionMes***REMOVED***ge() {
		return exceptionMes***REMOVED***ge;
	}
}
