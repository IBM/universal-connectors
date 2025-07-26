package com.ibm.guardium.aws.opensearch;

public class Constants {
    public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_opensearch_guardium_json_parse_error";
    public static final String AUDIT_CATEGORY = "audit_category";
    public static final String AUDIT_REQUEST_LAYER = "audit_request_layer";
    //request type
    public static final String REQUEST_TYPE_REST = "REST";
    public static final String REQUEST_TYPE_TRANSPORT = "TRANSPORT";
    public static final String CATEGORY_FAILED_LOGIN = "FAILED_LOGIN";
    public static final String CATEGORY_MISSING_PRIVILEGES = "MISSING_PRIVILEGES";


    //OpenSearch event categories
    public static final String CATEGORY_BAD_HEADERS = "BAD_HEADERS";
    public static final String CATEGORY_SSL_EXCEPTION = "SSL_EXCEPTION";
    public static final String CATEGORY_GRANTED_PRIVILEGES = "GRANTED_PRIVILEGES";
    public static final String CATEGORY_OPENSEARCH_SECURITY_INDEX_ATTEMPT = "OPENSEARCH_SECURITY_INDEX_ATTEMPT";
    public static final String CATEGORY_AUTHENTICATED = "AUTHENTICATED";
    //rest
    public static final String CATEGORY_REST_FAILED_LOGIN = "REST_FAILED_LOGIN";
    public static final String CATEGORY_REST_AUTHENTICATED = "REST_AUTHENTICATED";
    public static final String CATEGORY_REST_SSL_EXCEPTION = "REST_SSL_EXCEPTION";
    public static final String CATEGORY_REST_BAD_HEADERS = "REST_BAD_HEADERS";
    public static final String CATEGORY_REST_MISSING_PRIVILEGES = "REST_MISSING_PRIVILEGES";
    //transport
    public static final String CATEGORY_TRANSPORT_FAILED_LOGIN = "TRANSPORT_FAILED_LOGIN";
    public static final String CATEGORY_TRANSPORT_AUTHENTICATED = "TRANSPORT_AUTHENTICATED";
    public static final String CATEGORY_TRANSPORT_MISSING_PRIVILEGES = "TRANSPORT_MISSING_PRIVILEGES";
    public static final String CATEGORY_TRANSPORT_GRANTED_PRIVILEGES = "TRANSPORT_GRANTED_PRIVILEGES";
    public static final String CATEGORY_TRANSPORT_SSL_EXCEPTION = "TRANSPORT_SSL_EXCEPTION";
    public static final String CATEGORY_TRANSPORT_BAD_HEADERS = "TRANSPORT_BAD_HEADERS";
    public static final String CATEGORY_TRANSPORT_SECURITY_INDEX_ATTEMPT = "TRANSPORT_OPENSEARCH_SECURITY_INDEX";
    //standard categories
    public static final String CATEGORY_INDEX_EVENT = "INDEX_EVENT";
    public static final String CATEGORY_COMPLIANCE_DOC_READ = "COMPLIANCE_DOC_READ";
    public static final String CATEGORY_COMPLIANCE_DOC_WRITE = "COMPLIANCE_DOC_WRITE";
    public static final String CATEGORY_COMPLIANCE_INTERNAL_CONFIG_READ = "COMPLIANCE_INTERNAL_CONFIG_READ";
    public static final String CATEGORY_COMPLIANCE_INTERNAL_CONFIG_WRITE = "COMPLIANCE_INTERNAL_CONFIG_WRITE";
    public static final String LANGUAGE_STRING = "OPEN_SEARCH";
    public static final String DB_PROTOCOL = "OPEN_SEARCH";
    static final String MESSAGE = "message";
    static final String INVALID_MSG_OPENSEARCH = "OPENSEARCH_EVENT_IS_INVALID";


}
