package com.ibm.guardium.mongodb;

import java.util.HashMap;

public enum AType {
    GENERAL_SHUTDOWN("shutdown"), GENERAL_APPLICATION_MESSAGE("applicationMessage"), GENERAL_REPL_SET_RECONFIG("replSetReconfig"),
    SHARD_ADD("addShard"), SHARD_REMOVE("removeShard"),
    DATABASE_CREATE("createDatabase"), DATABASE_DROP("dropDatabase"), DATABASE_DROP_ALL_ROLES("dropAllRolesFromDatabase"), DATABASE_DROP_ALL_USERS("dropAllUsersFromDatabase"), DATABASE_ENABLE_SHARDING("enableSharding"),
    COLLECTION_CREATE("createCollection"), COLLECTION_DROP("dropCollection"), COLLECTION_RENAME("renameCollection"), COLLECTION_SHARD("shardCollection"),
    INDEX_CREATE("createIndex"), INDEX_DROP("dropIndex"),
    ROLE_CREATE("createRole"), ROLE_DROP("dropRole"), ROLE_UPDATE("updateRole"), ROLE_GRANT_PRIVILEGES("grantPrivilegesToRole"), ROLE_REVOKE_PRIVILEGES("revokePrivilegesFromRole"), ROLE_GRANT_ROLES("grantRolesToRole"), ROLE_REVOKE_ROLES("revokeRolesFromRole"),
    USER_CREATE("createUser"), USER_DROP("dropUser"), USER_UPDATE("updateUser"), USER_GRANT_ROLES("grantRolesToUser"), USER_REVOKE_ROLES("revokeRolesFromUser"),
    AUTHENTICATE("authenticate"),
    AUTHCHECK("authCheck"),;


    private static final HashMap<String, AType> allValuesByName = new HashMap<>();

    static {
        for (AType e: values()) {
            allValuesByName.put(e.propertyValue, e);
        }
    }

    private final String propertyValue;

    AType(String name) {
        this.propertyValue = name;
    }

    public static AType typeByPropertyValue(String name) {
        return allValuesByName.get(name);
    }

    public String getPropertyValue() {
        return propertyValue;
    }
}
