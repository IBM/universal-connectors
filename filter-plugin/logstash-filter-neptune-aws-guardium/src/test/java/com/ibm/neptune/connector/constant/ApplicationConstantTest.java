/*
* Copyright IBM Corp. 2021, 2022 All rights reserved.
* SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.neptune.connector.constant;

public interface ApplicationConstantTest {

	public static final String ID = "test-id";
	public static final String CONFIG_KEY = "source";
	public static final String CONFIG_VALUE = "message";

	public static final String LOG_MESSAGE_KEY = "message";
	public static final String LOG_MESSAGE = "1629182102678, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_GET, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /status HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity User-Agent: python-urllib3/1.26.5 content-length: 0\", /status";

	// for sparql log data.
	public static final String LOG_MESSAGE1 = "1629182102678, 172.31.20.251:47148, ip-172-31-29-87.ap-south-1.compute.internal/172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", query=%0ASELECT+%2A+WHERE+%7B%0A++++%3Fs+%3Fp+%3Fo%0A%7D+LIMIT+10%0A";

	public static final String TIME_STAMP_KEY = "timestamp";
	public static final String TIME_STAMP = "1629182102678";

	public static final int MINOFFSETFROMGMT = 19800000;

	public static final String SERVER_HOST_KEY = "serverhost";
	public static final String SERVER_HOST = "172.31.29.87:8182";
	public static final String SERVER_IP = "172.31.29.87";
	public static final String SERVER_PORT = "8182";

	public static final String CLIENT_HOST_KEY = "clienthost";
	public static final String CLIENT_HOST = "172.31.20.251:47148";
	public static final String CLIENT_IP = "172.31.20.251";
	public static final String CLIENT_PORT = "47148";

	// for sparql log data
	public static final String CLIENT_HOST1 = "172.31.20.251:47148";
	public static final String CLIENT_IP1 = "172.31.20.251";
	public static final String CLIENT_PORT1 = "47148";

	public static final String HTTP_HEADERS_KEY = "httpheaders";
	public static final String HTTP_HEADERS = "HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /status HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity User-Agent: python-urllib3/1.26.5 content-length: 0\\\", /status";

	public static final String PAYLOAD_KEY = "payload";
	public static final String PAYLOAD = "/status";
	
	public static final String CALLERIAM_KEY = "callerIAM";
	public static final String CALLERIAM = "arn:aws:iam::979326520502:user/sandeep-verma@hcl.com";

	public static final String SERVER_HOSTNAME_PREFIX_KEY = "serverHostnamePrefix";
	public static final String SERVER_HOSTNAME_PREFIX = "979326520502-database-1-instance-1.audit.log.0.22-03-24-11-03.0.0";
	
	public static final String DBNAME_PREFIX_KEY = "dbnamePrefix";
	public static final String DBNAME_PREFIX = "979326520502:database-1-instance-1.audit.log.0.22-03-24-11-03.0.0";
	// for sparql log data
	public static final String ACTUAL_PAYLOAD = "query=%0ASELECT+%2A+WHERE+%7B%0A++++%3Fs+%3Fp+%3Fo%0A%7D+LIMIT+10%0A";
	public static final String EXPECTED_PAYLOAD = "%0ASELECT+%2A+WHERE+%7B%0A++++%3Fs+%3Fp+%3Fo%0A%7D+LIMIT+10%0A";

	public static final String GUARDIUM_RECORD_FIELD_NAME = "GuardRecord";
	public static final String DOT = "\\.";

}
