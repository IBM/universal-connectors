package com.ibm.guardium.alloydb;

public class ConfigurationGenerator {
  static String getConfig() {
    return "{\n"
        + "  \"db_protocol\": \"{ALLOYDB}\",\n"
        + "  \"text_payload\": \"/\\\"textPayload\\\"\",\n"
        + "  \"exception_type_id\": \"/\\\"severity\\\"\",\n"
        + "  \"db_name\": \"/\\\"protoPayload\\\"/\\\"request\\\"/\\\"database\\\"\",\n"
        + "  \"db_user\": \"/\\\"protoPayload\\\"/\\\"authenticationInfo\\\"/\\\"principalEmail\\\"\",\n"
        + "  \"parsing_type\": \"CUSTOM_PARSER\",\n"
        + "  \"client_ip\": \"/\\\"protoPayload\\\"/\\\"requestMetadata\\\"/\\\"callerIp\\\"\",\n"
        + "  \"client_ipv6\": \"/\\\"protoPayload\\\"/\\\"requestMetadata\\\"/\\\"callerIp\\\"\",\n"
        + "  \"server_type\": \"{ALLOYDB}\",\n"
        + "  \"session_id\": \"{N.A.}\",\n"
        + "  \"language\": \"{PGRS}\",\n"
        + "  \"data_type\": \"{TEXT}\",\n"
        + "  \"server_port\": \"{-1}\",\n"
        + "  \"timestamp\": \"/\\\"timestamp\\\"\"\n"
        + "}";
  }
}
