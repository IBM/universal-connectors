package com.ibm.guardium.mongodb.parsersbytype;

import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;

/**
 * Parser for clientMetadata atype events.
 *
 * The clientMetadata event contains information about the client connection,
 * including driver details, OS information, and application metadata.
 *
 * Example structure:
 * {
 * "atype": "clientMetadata",
 * "ts": { "$date": "..." },
 * "local": { "ip": "...", "port": ... },
 * "remote": { "ip": "...", "port": ... },
 * "users": [...],
 * "roles": [...],
 * "param": {
 * "clientMetadata": {
 * "driver": { "name": "...", "version": "..." },
 * "os": { "type": "...", "name": "...", "architecture": "...", "version": "..."
 * },
 * "platform": "...",
 * "application": { "name": "..." }
 * }
 * },
 * "result": 0
 * }
 */
public class ClientMetadataParser extends BaseParser {

    @Override
    protected Sentence parseSentence(final JsonObject data) {
        Sentence sentence = new Sentence("clientMetadata");

        JsonObject param = data.has("param") ? data.get("param").getAsJsonObject() : null;

        if (param != null && param.has("clientMetadata")) {
            JsonObject clientMetadata = param.getAsJsonObject("clientMetadata");

            // Extract driver information
            if (clientMetadata.has("driver")) {
                JsonObject driver = clientMetadata.getAsJsonObject("driver");
                String driverName = driver.has("name") ? driver.get("name").getAsString() : UNKOWN_STRING;
                String driverVersion = driver.has("version") ? driver.get("version").getAsString() : UNKOWN_STRING;

                if (!driverName.equals(UNKOWN_STRING)) {
                    sentence.getFields().add("driver:" + driverName + ":" + driverVersion);
                }
            }

            // Extract OS information
            if (clientMetadata.has("os")) {
                JsonObject os = clientMetadata.getAsJsonObject("os");
                String osType = os.has("type") ? os.get("type").getAsString() : UNKOWN_STRING;
                String osName = os.has("name") ? os.get("name").getAsString() : UNKOWN_STRING;
                String osArch = os.has("architecture") ? os.get("architecture").getAsString() : UNKOWN_STRING;
                String osVersion = os.has("version") ? os.get("version").getAsString() : UNKOWN_STRING;

                if (!osType.equals(UNKOWN_STRING) || !osName.equals(UNKOWN_STRING)) {
                    StringBuilder osInfo = new StringBuilder("os:" + osType + ":" + osName);
                    if (!osArch.equals(UNKOWN_STRING)) {
                        osInfo.append(":").append(osArch);
                    }
                    if (!osVersion.equals(UNKOWN_STRING)) {
                        osInfo.append(":").append(osVersion);
                    }
                    sentence.getFields().add(osInfo.toString());
                }
            }

            // Extract platform information
            if (clientMetadata.has("platform")) {
                String platform = clientMetadata.get("platform").getAsString();
                if (!platform.isEmpty()) {
                    sentence.getFields().add("platform:" + platform);
                }
            }

            // Extract application information
            if (clientMetadata.has("application")) {
                JsonObject application = clientMetadata.getAsJsonObject("application");
                if (application.has("name")) {
                    String appName = application.get("name").getAsString();
                    SentenceObject sentenceObject = new SentenceObject(appName);
                    sentenceObject.setType("application");
                    sentence.getObjects().add(sentenceObject);
                }
            }
        }

        // If no specific metadata was found, add a generic object
        if (sentence.getObjects().isEmpty()) {
            SentenceObject sentenceObject = new SentenceObject("client");
            sentenceObject.setType("metadata");
            sentence.getObjects().add(sentenceObject);
        }

        return sentence;
    }

    @Override
    public boolean validate(JsonObject data) {
        // clientMetadata events are always valid if they have the correct atype
        return true;
    }
}