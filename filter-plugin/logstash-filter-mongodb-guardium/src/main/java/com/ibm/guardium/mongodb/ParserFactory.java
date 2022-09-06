package com.ibm.guardium.mongodb;

import com.google.gson.JsonObject;
import com.ibm.guardium.mongodb.parsersbytype.*;
import com.ibm.guardium.mongodb.parsersbytype.CollectionParser;
import com.ibm.guardium.mongodb.parsersbytype.DatabaseParser;
import com.ibm.guardium.mongodb.parsersbytype.IndexParser;
import com.ibm.guardium.mongodb.parsersbytype.RoleParser;
import com.ibm.guardium.mongodb.parsersbytype.UserParser;

public class ParserFactory {


    public static BaseParser getParser(JsonObject data) throws Exception {
//        final JsonObject param = data.get("param").getAsJsonObject();
        final String atypeValue = data.get("atype").getAsString();
        AType aType = AType.typeByPropertyValue(atypeValue);
        if (aType==null){
            throw new Exception("Unsupported type "+atypeValue);
        }
        BaseParser parser = null;
        switch (aType){
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
            default:
                throw new Exception("Unsupported type "+aType);
        }
        return parser;
    }

}

