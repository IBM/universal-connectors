package com.ibm.guardium.mongodb.parsersbytype;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.guardium.mongodb.parsersbytype.BaseParser;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Sentence;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.SentenceObject;

import java.util.ArrayList;
import java.util.Collections;

public class RoleParser extends BaseParser {

    public static final String ROLES_COMMAND = "assignRoles";
    public static final String PRIVILEGES_COMMAND = "grantPrivileges";

    public Sentence parseSentence(JsonObject data){

        JsonObject param = data.get("param").getAsJsonObject();

        String roleName = param.get("role").getAsString();
        String db = param.get("db").getAsString();

        Sentence sentence = new Sentence(getAType(data)); // atype is command

        SentenceObject roleMainObject = new SentenceObject(roleName, db);
        roleMainObject.setType("role");
        sentence.getObjects().add(roleMainObject);

        JsonElement roles = param.get("roles");
        Sentence rolesSentence = parseRoles(roles);
        if (rolesSentence!=null){
            sentence.getDescendants().add(rolesSentence);
        }

        JsonElement privileges = param.get("privileges");
        Sentence privilegesSentence = parsePrivileges(privileges);
        if (privilegesSentence!=null){
            sentence.getDescendants().add(privilegesSentence);
        }

        return sentence;
    }

    public static Sentence parseRoles(JsonElement roles){
        if (roles!=null && roles.isJsonArray()) {
            ArrayList<SentenceObject> objects = new ArrayList<SentenceObject>();
            for (final JsonElement roleDesc : roles.getAsJsonArray()) {
                JsonObject roleJson = roleDesc.getAsJsonObject();
                String roleType = roleJson.get("role").getAsString();
                String roleDb = roleJson.get("db").getAsString();

                SentenceObject roleObject = new SentenceObject(roleType, roleDb);
                roleObject.setType("role");
                objects.add(roleObject);
            }
            if (objects.size()>0){
                Sentence rolesSentence = new Sentence(ROLES_COMMAND); // atype is command
                rolesSentence.getObjects().addAll(objects);
                return rolesSentence;
            }
        }
        return null;
    }

    public static Sentence parsePrivileges(JsonElement privileges){
        if (privileges!=null && privileges.isJsonArray()){
            ArrayList<SentenceObject> objects = new ArrayList<SentenceObject>();
            for (final JsonElement privilegeDesc : privileges.getAsJsonArray()) {
                JsonObject privilegeJson = privilegeDesc.getAsJsonObject();
                JsonObject resourceJson = privilegeJson.get("resource").getAsJsonObject();
                SentenceObject resourceObject = null;

                String resourceDb = resourceJson.get("db")!=null ? resourceJson.get("db").getAsString() : null;
                if (resourceDb != null) {
                    String resourceName = resourceJson.get("collection") != null ? resourceJson.get("collection").getAsString() : resourceDb;
                    String resourceType = resourceJson.get("collection") != null ? "collection" : "database";
                    resourceObject = new SentenceObject(resourceName, resourceDb);
                    resourceObject.setType(resourceType);
                } else if (resourceJson.get("cluster")!=null){
                    resourceObject = new SentenceObject("cluster", "cluster");
                    resourceObject.setType("cluster");
                } else if (resourceJson.get("anyResource")!=null) {
                    resourceObject = new SentenceObject("anyResource", "anyResource");
                    resourceObject.setType("anyResource");
                }

                JsonArray actions = privilegeJson.get("actions").getAsJsonArray();
                if (actions!=null && actions.size()>0){
                    ArrayList<String> actionsArr = new ArrayList<>();
                    for (JsonElement action : actions) {
                        actionsArr.add(action.getAsString());
                    }
                    resourceObject.setFields(actionsArr.stream().toArray(String[]::new));
                }
                objects.add(resourceObject);
            }
            if (objects.size()>0) {
                Sentence privilegesSentence = new Sentence(PRIVILEGES_COMMAND);
                privilegesSentence.getObjects().addAll(objects);
                return privilegesSentence;
            }
        }
        return null;
    }
}

/*
*
*{ 	"atype": "createRole",
	"ts": {
		"$date": "2022-07-05T12:17:20.495-05:00"
	},
	"local": {
		"ip": "db-server-ip-removed",
		"port": 27017
	},
	"remote": {
		"ip": "db-server-ip-removed",
		"port": 34753
	},
	"users": [
		{
			"user": "admin",
			"db": "admin"
		}
	],
	"roles": [
		{
			"role": "root",
			"db": "admin"
		}
	],
	"param": {
		"role": "dbgbdirol1",
		"db": "dbgbdi1",
		"roles": [
			{
				"role": "read",
				"db": "dbgbdi1"
			}
		],
		"privileges": [
			{
				"resource": {
					"db": "dbgbdi1",
					"collection": "collgbdi1"
				},
				"actions": [
					"insert",
					"remove",
					"update"
				]
			}
		]
	},
	"result": 0
}
*
* */
/*mongod: {
	"atype": "updateRole",
	"ts": {
		"$date": "2022-07-05T12:17:20.635-05:00"
	},
	"local": {
		"ip": "db-server-ip-removed",
		"port": 27017
	},
	"remote": {
		"ip": "db-server-ip-removed",
		"port": 34753
	},
	"users": [
		{
			"user": "admin",
			"db": "admin"
		}
	],
	"roles": [
		{
			"role": "root",
			"db": "admin"
		}
	],
	"param": {
		"role": "dbgbdirol2",
		"db": "dbgbdi1",
		"privileges": [
			{
				"resource": {
					"db": "dbgbdi1",
					"collection": " "
				},
				"actions": [
					"createCollection",
					"createIndex"
				]
			}
		]
	},
	"result": 0
}*/
/*mongod:{
	"atype": "dropRole",
	"ts": {
		"$date": "2022-07-05T12:17:20.643-05:00"
	},
	"local": {
		"ip": "db-server-ip-removed",
		"port": 27017
	},
	"remote": {
		"ip": "db-server-ip-removed",
		"port": 34753
	},
	"users": [
		{
			"user": "admin",
			"db": "admin"
		}
	],
	"roles": [
		{
			"role": "root",
			"db": "admin"
		}
	],
	"param": {
		"role": "dbgbdirol3",
		"db": "dbgbdi1"
	},
	"result": 0
}*/
/*mongod: {
	"atype": "revokePrivilegesFromRole",
	"ts": {
		"$date": "2022-07-05T12:17:20.641-05:00"
	},
	"local": {
		"ip": "db-server-ip-removed",
		"port": 27017
	},
	"remote": {
		"ip": "db-server-ip-removed",
		"port": 34753
	},
	"users": [
		{
			"user": "admin",
			"db": "admin"
		}
	],
	"roles": [
		{
			"role": "root",
			"db": "admin"
		}
	],
	"param": {
		"role": "dbgbdirol3",
		"db": "dbgbdi1",
		"privileges": [
			{
				"resource": {
					"db": "dbgbdi1",
					"collection": "collgbdiT1"
				},
				"actions": [
					"insert"
				]
			}
		]
	},
	"result": 0
}*/
