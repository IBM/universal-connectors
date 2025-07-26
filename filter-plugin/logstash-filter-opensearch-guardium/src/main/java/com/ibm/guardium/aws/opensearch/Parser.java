/*
Copyright IBM Corp. 2021, 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.aws.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibm.guardium.universalconnector.commons.custom_parsing.CustomParser;
import com.ibm.guardium.universalconnector.commons.custom_parsing.ParserFactory;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.*;

/**
 * Parser Class will perform operation on parsing events and messages from the
 * opensearch audit logs into a Guardium record instance Guardium records include
 * the accessor, the sessionLocator, data, and exceptions. If there are no
 * errors, the data contains details about the query "construct"
 *
 * @className @Parser
 */
public class Parser extends CustomParser {
    private static Logger logger = LogManager.getLogger(Parser.class);
    private final ObjectMapper mapper = new ObjectMapper();

    public Parser(ParserFactory.ParserType parserType) {
        super(parserType);
    }

    @Override
    public Record parseRecord(String payload) {
        try {
            String normalizedPayload = normalizeAuditCategory(payload);
            return super.parseRecord(normalizedPayload);
        } catch (Exception e) {
            logger.error("Error extracting record: " + e.getMessage(), e);
            return null;
        }
    }

    String normalizeAuditCategory(String payload) {
        String category = getValueFromPayload(payload, Constants.AUDIT_CATEGORY);
        if (category == null || category.isEmpty()) {
            logger.error("Error normalizing audit category: " + category);
        }

        try {
            String layer = getValueFromPayload(payload, Constants.AUDIT_REQUEST_LAYER);


            String normalizedCategory = category;

            if (layer.equals(Constants.REQUEST_TYPE_REST)) {

                if (category.equals(Constants.CATEGORY_FAILED_LOGIN)) {
                    normalizedCategory = Constants.CATEGORY_REST_FAILED_LOGIN;
                } else if (category.equals(Constants.CATEGORY_AUTHENTICATED)) {
                    normalizedCategory = Constants.CATEGORY_REST_AUTHENTICATED;
                } else if (category.equals(Constants.CATEGORY_SSL_EXCEPTION)) {
                    normalizedCategory = Constants.CATEGORY_REST_SSL_EXCEPTION;
                } else if (category.equals(Constants.CATEGORY_BAD_HEADERS)) {
                    normalizedCategory = Constants.CATEGORY_REST_BAD_HEADERS;
                }
            } else if (layer.equals(Constants.REQUEST_TYPE_TRANSPORT)) {

                if (category.equals(Constants.CATEGORY_FAILED_LOGIN)) {
                    normalizedCategory = Constants.CATEGORY_TRANSPORT_FAILED_LOGIN;
                } else if (category.equals(Constants.CATEGORY_AUTHENTICATED)) {
                    normalizedCategory = Constants.CATEGORY_TRANSPORT_AUTHENTICATED;
                } else if (category.equals(Constants.CATEGORY_MISSING_PRIVILEGES)) {
                    normalizedCategory = Constants.CATEGORY_TRANSPORT_MISSING_PRIVILEGES;
                } else if (category.equals(Constants.CATEGORY_GRANTED_PRIVILEGES)) {
                    normalizedCategory = Constants.CATEGORY_TRANSPORT_GRANTED_PRIVILEGES;
                } else if (category.equals(Constants.CATEGORY_SSL_EXCEPTION)) {
                    normalizedCategory = Constants.CATEGORY_TRANSPORT_SSL_EXCEPTION;
                } else if (category.equals(Constants.CATEGORY_REST_BAD_HEADERS)) {
                    normalizedCategory = Constants.CATEGORY_TRANSPORT_BAD_HEADERS;
                }
            }

            //standard category
            if (normalizedCategory.equals(category)) {
                if (category.equals(Constants.CATEGORY_INDEX_EVENT)) {
                    normalizedCategory = Constants.CATEGORY_INDEX_EVENT;
                } else if (category.equals(Constants.CATEGORY_COMPLIANCE_DOC_READ)) {
                    normalizedCategory = Constants.CATEGORY_COMPLIANCE_DOC_READ;
                } else if (category.equals(Constants.CATEGORY_COMPLIANCE_DOC_WRITE)) {
                    normalizedCategory = Constants.CATEGORY_COMPLIANCE_DOC_WRITE;
                } else if (category.equals(Constants.CATEGORY_COMPLIANCE_INTERNAL_CONFIG_READ)) {
                    normalizedCategory = Constants.CATEGORY_COMPLIANCE_INTERNAL_CONFIG_READ;
                } else if (category.equals(Constants.CATEGORY_COMPLIANCE_INTERNAL_CONFIG_WRITE)) {
                    normalizedCategory = Constants.CATEGORY_COMPLIANCE_INTERNAL_CONFIG_WRITE;
                }
            }

            if (!normalizedCategory.equals(category)) {
                JsonNode rootNode = mapper.readTree(payload);
                ((ObjectNode) rootNode).put(Constants.AUDIT_CATEGORY, normalizedCategory);
                return rootNode.toString();
            }

            return payload;
        } catch (Exception e) {
            logger.error("Error normalizing audit category: " + e.getMessage(), e);
            return payload;
        }
    }

    @Override
    protected Record extractRecord(String payload) {
        Record record = new Record();
        record.setSessionId(this.getSessionId(payload));
        record.setDbName(this.getDbName(payload));
        record.setAppUserName(this.getAppUserName(payload));
        String sqlString = this.getSqlString(payload);
        record.setException(this.getException(payload, sqlString));
        record.setAccessor(this.getAccessor(payload));
        record.setSessionLocator(this.getSessionLocator(payload));
        record.setTime(this.getTimestamp(payload));
        record.setData(this.getData(payload, sqlString));
        return record;
    }

    @Override
    protected String parse(String payload, String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        try {
            return getValueFromPayload(payload, key);
        } catch (Exception e) {
            logger.error("Error parsing key '{}' from payload: {}", key, e.getMessage(), e);
            return null;
        }
    }

    String getValueFromPayload(String payload, String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return null;
        }
        try {
            JsonNode rootNode = mapper.readTree(payload);

            if (fieldName.contains("[") && fieldName.contains("]")) {
                int arrayStart = fieldName.indexOf("[");
                int arrayEnd = fieldName.indexOf("]");
                String arrayField = fieldName.substring(0, arrayStart);
                int index = Integer.parseInt(fieldName.substring(arrayStart + 1, arrayEnd));

                JsonNode arrayNode = rootNode.path(arrayField);
                if (arrayNode.isArray() && arrayNode.size() > index) {
                    return arrayNode.get(index).asText();
                }
                return "";
            }

            if (rootNode.has(fieldName)) {
                JsonNode fieldNode = rootNode.get(fieldName);
                if (fieldNode.isArray()) {
                    return fieldNode.toString();
                } else {
                    return fieldNode.asText();
                }
            }
        } catch (Exception e) {
            logger.error("Error getting value from payload: " + e.getMessage(), e);
        }
        return "";
    }
    @Override
    protected String getSqlString(String payload) {
        StringBuilder sb = new StringBuilder();

        String category = getValueFromPayload(payload, Constants.AUDIT_CATEGORY);
        String layer = getValueFromPayload(payload, Constants.AUDIT_REQUEST_LAYER);

        boolean compliance_write = Constants.CATEGORY_COMPLIANCE_DOC_WRITE.equals(category) || Constants.CATEGORY_COMPLIANCE_INTERNAL_CONFIG_WRITE.equals(category);
        boolean compliance_read = Constants.CATEGORY_COMPLIANCE_DOC_READ.equals(category) || Constants.CATEGORY_COMPLIANCE_INTERNAL_CONFIG_READ.equals(category);
        String requestType = "";

        sb.append("__OPSEARCH ");
        if (layer != null && !layer.isEmpty()) {

            if (Constants.REQUEST_TYPE_REST.equals(layer)) {
                String method = getValueFromPayload(payload, "audit_rest_request_method");
                String path = checkURIPath(getValueFromPayload(payload, "audit_rest_request_path"));

                sb.append(method).append(" ").append(path).append(" ");

            } else if (Constants.REQUEST_TYPE_TRANSPORT.equals(layer)) {
                requestType = getValueFromPayload(payload, "audit_transport_request_type");
                String requestPrivilege = checkURIPath(getValueFromPayload(payload, "audit_request_privilege"));

                sb.append(requestType).append(" ").append(requestPrivilege).append(" ");
            }
        } else {
            if (compliance_read) {
                sb.append("GET").append(" ").append("/");
            } else if (compliance_write) {
                sb.append("POST").append(" ").append("/");
            }
        }

        sb.append("#");

        sb.append("{");

        sb.append("\"category\":\"").append(category).append("\"");
        if (compliance_write) {
            String complianceOperation = getValueFromPayload(payload, "audit_compliance_operation");
            sb.append(", \"action\":\"").append(complianceOperation).append("\"");
        }

        String body = getValueFromPayload(payload, "audit_request_body");
        if (body != null && !body.isEmpty()) {
            sb.append(", \"_query\":").append(body);
        }

        String resolvedIndex = getValueFromPayload(payload, "audit_trace_resolved_indices");
        if (resolvedIndex.isEmpty()){
            resolvedIndex =  getValueFromPayload(payload, "audit_trace_indices");
        }
        if (!resolvedIndex.isEmpty()) {
            sb.append(", \"_indices\":\"").append(sanitizeResolvedIndices(resolvedIndex)).append("\"");
        }

        sb.append("}");
        return sb.toString();
    }

    public List<String> sanitizeResolvedIndices(String jsonArrayString) {
        List<String> sanitized = new ArrayList<>();

        if (jsonArrayString == null || jsonArrayString.isEmpty()) {
            return sanitized;
        }

        try {
            JsonNode arrayNode = mapper.readTree(jsonArrayString);
            if (arrayNode.isArray()) {
                for (JsonNode node : arrayNode) {
                    String index = node.asText();
                    if (index != null && !index.startsWith(".")) {
                        sanitized.add(normalizeReservedKeyword(index));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse resolved indices: " + e.getMessage(), e);
        }

        return sanitized;
    }


    public static String checkURIPath(String uri) {
        if (uri == null || uri.isEmpty()) {
            return uri;
        }

        uri = uri.replaceAll("\\[.*?\\]", "");

        try {
            uri = URLDecoder.decode(uri, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            uri = uri.replaceAll("%", "_");
            }

        uri = uri.replace(":", "/");

        if (uri.contains("<?xml") || uri.trim().startsWith("<")) {
            return "/invalid/xml_input";
        }

        uri = uri.replaceAll("[^a-zA-Z0-9/_-]", "_");

        if (uri.startsWith("indices/")) {
            uri = uri.substring("indices/".length());
            }

        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }

        String[] parts = uri.substring(1).split("/");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = normalizeReservedKeyword(parts[i]);
        }
        return "/" + String.join("/", parts);
    }



    public static String normalizeReservedKeyword(String word) {
        Set<String> snifRestrictedKeywords = Set.of("template", "mappings", "get", "aliases", "user");
        if (word != null && snifRestrictedKeywords.contains(word)) {
            return "_" + word;
        }
        return word;
    }


    @Override
    protected ExceptionRecord getException(String payload, String sqlString) {
        ExceptionRecord exceptionRecord = new ExceptionRecord();
        String exceptionTypeId = this.getExceptionTypeId(payload);
        String category = "";

        if (exceptionTypeId.isEmpty()) {
            category = getValueFromPayload(payload, Constants.AUDIT_CATEGORY);
            exceptionTypeId = getExceptionTypeFromCategory(category);
            if (exceptionTypeId == null) {
                return null;
            }
        }
        exceptionRecord.setExceptionTypeId(exceptionTypeId);
        exceptionRecord.setDescription(category);
        exceptionRecord.setSqlString(sqlString);
        return exceptionRecord;
    }

    private String getExceptionTypeFromCategory(String category) {
        if (category.contains(Constants.CATEGORY_FAILED_LOGIN) || category.equals(Constants.CATEGORY_REST_FAILED_LOGIN) || category.equals(Constants.CATEGORY_TRANSPORT_FAILED_LOGIN)) {
            return "LOGIN_FAILED";
        }
        if (category.equals(Constants.CATEGORY_MISSING_PRIVILEGES) || category.equals(Constants.CATEGORY_BAD_HEADERS) || category.equals(Constants.CATEGORY_SSL_EXCEPTION) || category.equals(Constants.CATEGORY_REST_MISSING_PRIVILEGES) || category.equals(Constants.CATEGORY_REST_BAD_HEADERS) || category.equals(Constants.CATEGORY_REST_SSL_EXCEPTION) || category.equals(Constants.CATEGORY_TRANSPORT_MISSING_PRIVILEGES) || category.equals(Constants.CATEGORY_TRANSPORT_BAD_HEADERS) || category.equals(Constants.CATEGORY_TRANSPORT_SSL_EXCEPTION)) {
            return "SQL_ERROR";
        }
        return null;
    }

    @Override
    protected String getDbUser(String payload) {
        String value = this.getValue(payload, "db_user");
        if (value == null || value.isEmpty()) {
            value = this.getValue(payload, "db_user_initiating_user");
        }
        return (value == null || value.isEmpty()) ? "N.A." : value;
    }

    public static Time parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            throw new IllegalArgumentException("Timestamp cannot be null or empty");
        }

        ZonedDateTime date;
        try {
            date = ZonedDateTime.parse(timestamp);
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                LocalDateTime localDateTime = LocalDateTime.parse(timestamp, formatter);
                date = localDateTime.atZone(ZoneId.systemDefault());
            } catch (Exception e2) {
                throw new IllegalArgumentException("Could not parse timestamp: " + timestamp, e2);
            }
        }
        long millis = date.toInstant().toEpochMilli();
        int minOffset = date.getOffset().getTotalSeconds() / 60;
        int minDst = date.getZone().getRules().isDaylightSavings(date.toInstant()) ? 60 : 0;
        return new Time(millis, minOffset, minDst);
    }

    @Override
    public String getConfigFileContent() {
        return new ConfigFileContent().getConfigFileContent();
    }
}