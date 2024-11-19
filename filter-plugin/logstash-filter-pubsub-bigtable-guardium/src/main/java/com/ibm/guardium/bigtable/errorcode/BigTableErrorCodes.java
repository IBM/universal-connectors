package com.ibm.guardium.bigtable.errorcode;

import com.ibm.guardium.bigtable.ApplicationConstants;

public class BigTableErrorCodes {

    public static String getStatusException(int code) {
        switch (code) {
            case 0:
                return ApplicationConstants.STATUS_OK;
            case 1:
                return ApplicationConstants.STATUS_CANCELLED;
            case 2:
                return ApplicationConstants.STATUS_UNKNOWN;
            case 3:
            case 400:
                return ApplicationConstants.STATUS_INVALID_ARGUMENT;
            case 4:
                return ApplicationConstants.STATUS_DEADLINE_EXCEEDED;
            case 5:
                return ApplicationConstants.STATUS_NOT_FOUND;
            case 6:
                return ApplicationConstants.STATUS_ALREADY_EXISTS;
            case 7:
            case 403:
                return ApplicationConstants.STATUS_PERMISSION_DENIED;
            case 8:
                return ApplicationConstants.STATUS_RESOURCE_EXHAUSTED;
            case 9:
            case 412:
                return ApplicationConstants.STATUS_FAILED_PRECONDITION;
            case 10:
                return ApplicationConstants.STATUS_ABORTED;
            case 11:
                return ApplicationConstants.STATUS_OUT_OF_RANGE;
            case 12:
                return ApplicationConstants.STATUS_UNIMPLEMENTED;
            case 13:
                return ApplicationConstants.STATUS_INTERNAL;
            case 14:
                return ApplicationConstants.STATUS_UNAVAILABLE;
            case 401:
                return ApplicationConstants.STATUS_NOT_AUTHORIZED;
            case 404:
                return ApplicationConstants.STATUS_RESOURCE_NOT_FOUND;
            case 409:
                return ApplicationConstants.STATUS_RESOURCE_ALREADY_EXIST;
            case 429:
                return ApplicationConstants.STATUS_RATE_LIMIT_EXCEEDED;
            case 500:
                return ApplicationConstants.STATUS_INTERNAL_SERVER_ERROR;
            default:
                return ApplicationConstants.STATUS_GENERAL_EXCEPTION;
        }
    }
}
