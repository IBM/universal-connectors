package com.ibm.guardium.capella;

public class ConfigurationGenerator {
  static String getConfig() {
    return "{\n"
        + "  \"db_protocol\": \"{CAPELLA}\",\n"
        + "  \"exception_type_id\": \"\",\n"
        + "  \"db_name\": \"/\\\"bucket_name\\\"\",\n"
        + "  \"db_user_domain\": \"/\\\"real_userid\\\"/\\\"domain\\\"\",\n"
        + "  \"db_user\": \"/\\\"real_userid\\\"/\\\"user\\\"\",\n"
        + "  \"bucket\": \"/\\\"bucket\\\"\",\n"
        + "  \"index\": \"/\\\"index_name\\\"\",\n"
        + "  \"service_name\": \"/\\\"name\\\"\",\n"
        + "  \"parsing_type\": \"CUSTOM_PARSER\",\n"
        + "  \"server_port\": \"{-1}\",\n"
        + "  \"server_ip\": \"/\\\"local\\\"/\\\"ip\\\"\",\n"
        + "  \"server_ipv6\": \"/\\\"local\\\"/\\\"ip\\\"\",\n"
        + "  \"client_ip\": \"/\\\"remote\\\"/\\\"ip\\\"\",\n"
        + "  \"client_ipv6\": \"/\\\"remote\\\"/\\\"ip\\\"\",\n"
        + "  \"client_port\": \"/\\\"remote\\\"/\\\"port\\\"\",\n"
        + "  \"server_type\": \"{CAPELLA}\",\n"
        + "  \"session_id\": \"/\\\"sessionid\\\"\",\n"
        + "  \"language\": \"{FREE_TEXT}\",\n"
        + "  \"data_type\": \"{CONSTRUCT}\",\n"
        + "  \"sql_parsing_active\": \"true\",\n"
        + "  \"verb\": \"/\\\"name\\\"\",\n"
        + "  \"object\": \"/\\\"bucket_name\\\"\",\n"
        + "  \"timestamp\": \"/\\\"timestamp\\\"\",\n"
        + "  \"sql_string\": \"/\\\"description\\\"\",\n"
        + "  \"statement\": \"/\\\"statement\\\"\",\n"
        + "  \"status\": \"/\\\"status\\\"\",\n"
        + "  \"server_hostname\": \"{cloud.couchbase.com}\"\n"
        + "}";
  }
}
