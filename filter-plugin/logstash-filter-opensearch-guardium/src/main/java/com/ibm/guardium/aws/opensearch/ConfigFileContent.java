package com.ibm.guardium.aws.opensearch;

public class ConfigFileContent {

    public String getConfigFileContent() {
        StringBuilder config = new StringBuilder("{\n");

        // Common fields across all categories
        config.append(" \"app_user_name\": \"audit_request_effective_user\",\n");
        config.append(" \"client_hostname\": \"audit_node_host_name\",\n");
        config.append(" \"client_ip\": \"audit_request_remote_address\",\n");
        config.append(" \"client_ipv6\": \"\",\n");
        config.append(" \"client_mac\": \"\",\n");
        config.append(" \"client_os\": \"\",\n");
        config.append(" \"client_port\": \"{-1}\",\n");
        config.append(" \"comm_protocol\": \"audit_request_layer\",\n");
        config.append(" \"construct\": \"audit_category\",\n");
        config.append(" \"db_name\": \"audit_cluster_name\",\n");
        config.append(" \"db_protocol\": \"{OPSEARCH}\",\n");
        config.append(" \"db_user\": \"audit_request_effective_user\",\n");
        config.append(" \"db_user_initiating_user\": \"audit_request_effective_user\",\n");
        config.append(" \"server_hostname\": \"{opensearch.aws.com}\",\n");
        config.append(" \"server_ip\": \"\",\n");
        config.append(" \"server_port\": \"{-1}\",\n");
        config.append(" \"server_type\": \"{Opensearch}\",\n");
        config.append(" \"service_name\": \"audit_cluster_name\",\n");
        config.append(" \"session_id\": \"\",\n");
        config.append(" \"source_program\": \"audit_request_origin\",\n");
        config.append(" \"sql_parsing_active\": \"true\",\n");
        config.append(" \"timestamp\": \"@timestamp\",\n");

        // REST
        config.append(" \"REST_PATH\": \"audit_rest_request_path\",\n");
        config.append(" \"REST_METHOD\": \"audit_rest_request_method\",\n");

        // Transport
        config.append(" \"TRANSPORT_AUTHENTICATED\": \"audit_transport_request_type\",\n");
        config.append(" \"TRANSPORT_FAILED_LOGIN\": \"audit_request_exception_stacktrace\",\n");
        config.append(" \"TRANSPORT_PRIVILEGE\": \"audit_request_privilege\",\n");

        config.append(" \"COMPLIANCE_OPERATION\": \"audit_compliance_operation\",\n");
        config.append(" \"COMPLIANCE_DOC_INDEX\": \"audit_trace_resolved_indices[0]\",\n");

        config.append(" \"parsing_format\": \"JSON\",\n");
        config.append(" \"parsing_type\": \"SNIFFER\",\n");
        config.append(" \"sniffer_parser\": \"OPEN_SEARCH\",\n");
        config.append(" \"TEXT\": \"TEXT\"\n");

        config.append("}\n");
        return config.toString();
    }
}
