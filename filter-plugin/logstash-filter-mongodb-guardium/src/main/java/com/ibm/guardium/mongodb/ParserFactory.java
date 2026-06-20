package com.ibm.guardium.mongodb;

import com.google.gson.JsonObject;
import com.ibm.guardium.mongodb.parsersbytype.*;
import com.ibm.guardium.mongodb.parsersbytype.CollectionParser;
import com.ibm.guardium.mongodb.parsersbytype.DatabaseParser;
import com.ibm.guardium.mongodb.parsersbytype.IndexParser;
import com.ibm.guardium.mongodb.parsersbytype.RoleParser;
import com.ibm.guardium.mongodb.parsersbytype.UserParser;
import com.ibm.guardium.mongodb.parsersbytype.ClientMetadataParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParserFactory {

    private static Logger log = LogManager.getLogger(ParserFactory.class);

    public static BaseParser getParser(JsonObject data) throws Exception {
        if (!data.has("atype")) {
            log.info("ParserFactory: Missing 'atype' field in data: {}", data);
            throw new IllegalArgumentException("Missing required 'atype' field in audit log");
        }

        final String atypeValue = data.get("atype").getAsString();
        if (atypeValue == null || atypeValue.trim().isEmpty()) {
            log.error("ParserFactory: Empty or null 'atype' value in data: {}", data);
            throw new IllegalArgumentException("'atype' field cannot be empty or null");
        }

        AType aType = AType.typeByPropertyValue(atypeValue);
        if (aType == null) {
            log.warn("ParserFactory: Unsupported audit type '{}'. This audit event will be skipped. Data: {}",
                    atypeValue, data);
            throw new IllegalArgumentException("Unsupported audit type: " + atypeValue);
        }
        BaseParser parser = null;
        switch (aType) {
            case GENERAL_SHUTDOWN:
            case GENERAL_APPLICATION_MESSAGE:
            case GENERAL_REPL_SET_RECONFIG:
                parser = new GeneralParser();
                break;
            case SHARD_ADD:
            case SHARD_REMOVE:
                parser = new ShardParser();
                break;
            case DATABASE_CREATE:
            case DATABASE_DROP:
            case DATABASE_DROP_ALL_ROLES:
            case DATABASE_DROP_ALL_USERS:
            case DATABASE_ENABLE_SHARDING:
                parser = new DatabaseParser();
                break;
            case COLLECTION_CREATE:
            case COLLECTION_DROP:
            case COLLECTION_RENAME:
            case COLLECTION_SHARD:
                parser = new CollectionParser();
                break;
            case INDEX_CREATE:
            case INDEX_DROP:
                parser = new IndexParser();
                break;
            case ROLE_CREATE:
            case ROLE_DROP:
            case ROLE_UPDATE:
            case ROLE_GRANT_PRIVILEGES:
            case ROLE_REVOKE_PRIVILEGES:
            case ROLE_GRANT_ROLES:
            case ROLE_REVOKE_ROLES:
                parser = new RoleParser();
                break;
            case USER_CREATE:
            case USER_DROP:
            case USER_UPDATE:
            case USER_GRANT_ROLES:
            case USER_REVOKE_ROLES:
            case AUTHENTICATE:
                parser = new UserParser();
                break;
            case AUTHCHECK:
                parser = new AuthCheckParser();
                break;
            case CLIENT_METADATA:
                parser = new ClientMetadataParser();
                break;
            case LOGOUT:
                parser = new LoginParser();
                break;
            default:
                // This should never happen if AType enum is properly maintained
                throw new IllegalStateException("Skipping unwanted audit type in switch statement: " + aType +
                        ".");
        }

        if (parser == null) {
            log.error("ParserFactory: Parser is null after switch statement for audit type '{}'", aType);
            throw new IllegalStateException("Failed to instantiate parser for audit type: " + aType);
        }

        return parser;
    }

}