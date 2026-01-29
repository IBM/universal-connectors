/*
 * Copyright © 2025 Software GmbH, Darmstadt, Germany and/or its licensors
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.softwareag.adabas.auditing.logstash;

public final class Constants {
    private Constants() {}

    public static final String LOGSTASH_TAG_SKIP_NOT_COMMAND = "_adabasguardium_skip_not_command";

    public static final String DATA_PROTOCOL_STRING = "Adabas native audit";
    public static final String SERVER_TYPE_STRING = "Adabas";
    public static final String UNKNOWN_STRING = "";

    // hashmap mappings
    public static final String RECORD_SESSION_ID = "CMDID";
    public static final String RECORD_DB_NAME = "UABIDBID";
    public static final String RECORD_TIME = "UABHTIME";
    public static final String RECORD_APP_USER_NAME = "TPUSERID";

    public static final String ACCESSOR_DB_USER = "NATUID";
    public static final String ACCESSOR_SERVER_HOST_NAME = "LPARNAME";
    public static final String ACCESSOR_SOURCE_PROGRAM = "NATPROG";

    // ACBX fields
    public static final String ACBX_RSP_CODE = "RSPCODE";
    public static final String ACBX_RSP_SUB_CODE = "RSPSUBCODE";
    public static final String ACBX_CMD_CODE = "CMDCODE";
    public static final String ACBX_ISN = "ISN";

    // Error type
    public static final String SQL_ERROR = "SQL_ERROR";

}
