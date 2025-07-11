package org.logstashplugins;

public interface Constants {

    String PLUGIN_CONFIG_KEY_INTERVAL =  "query_interval";
    String PLUGIN_CONFIG_KEY_TYPE =  "type";
    String PLUGIN_CONFIG_KEY_QUERY_LENGTH =  "query_length";
    String PLUGIN_CONFIG_KEY_ORG =  "organization_id";
    String PLUGIN_CONFIG_KEY_PROJ =  "project_id";
    String PLUGIN_CONFIG_KEY_API_URL =  "api_base_url";
    String PLUGIN_CONFIG_KEY_CLUSTER =  "cluster_id";
    String PLUGIN_CONFIG_KEY_AUTH =  "auth_token";

    String PLUGIN_TYPE =  "couchbasecapella";
    String V4_API =  "https://cloudapi.cloud.couchbase.com/v4";
    Long DEFAULT_QUERY_INTERVAL =  3600L;
    Long DEFAULT_QUERY_LENGTH =  3600L;

    String OUTPUT_KEY_TYPE =  "type";
    String OUTPUT_KEY_MESSAGE =  "message";
    String OUTPUT_KEY_ORG =  "organizationID";
    String OUTPUT_KEY_PROJ =  "projectID";
    String OUTPUT_KEY_CLUSTER =  "clusterID";



}