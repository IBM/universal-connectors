package com.ibm.guardium.mongodb.parsersbytype;

import com.google.gson.JsonObject;
import com.ibm.guardium.mongodb.parsersbytype.BaseParser;
import com.ibm.guardium.mongodb.parsersbytype.RoleParser;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Sentence;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.SentenceObject;

public class UserParser extends BaseParser {

    public Sentence parseSentence(JsonObject data){

        JsonObject param = data.get("param").getAsJsonObject();

        String user = param.get("user").getAsString();
        String db = param.get("db").getAsString();
        Sentence sentence = new Sentence(getAType(data)); // atype is command
        SentenceObject sentenceObject = new SentenceObject(user, db);
        sentenceObject.setType("user");
        sentence.getObjects().add(sentenceObject);

        Sentence rolesSentence = RoleParser.parseRoles(param.get("roles"));
        if (rolesSentence!=null){
            sentence.getDescendants().add(rolesSentence);
        }

        String passwordChanged = param.get("passwordChanged")!=null ? param.get("passwordChanged").getAsString() : null;
        if (passwordChanged!=null && "true".equalsIgnoreCase(passwordChanged)){
            sentence.getFields().add("passwordChanged");
        }

        return sentence;
    }

}

/*mongod: {
	"atype": "createUser",
	"ts": {
		"$date": "2022-07-05T12:17:20.539-05:00"
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
		"user": "usrgbdi1",
		"db": "dbgbdi1",
		"roles": [
			{
				"role": "readWrite",
				"db": "dbgbdi1"
			}
		]
	},
	"result": 0
}*/
/*mongod: {
	"atype": "dropUser",
	"ts": {
		"$date": "2022-07-05T12:17:20.649-05:00"
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
		"user": "usrgbdi2",
		"db": "dbgbdi1"
	},
	"result": 0
}*/
/*
mongod: {
	"atype": "updateUser",
	"ts": {
		"$date": "2022-07-05T12:17:20.637-05:00"
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
		"user": "usrgbdi1",
		"db": "dbgbdi1",
		"passwordChanged": false,
		"customData": {
			"employeeId": "0x3039"
		},
		"roles": [
			{
				"role": "readWrite",
				"db": "dbgbdi1"
			}
		]
	},
	"result": 0
}
*/
/*mongod: {
	"atype": "grantRolesToUser",
	"ts": {
		"$date": "2022-07-05T12:17:20.632-05:00"
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
		"user": "usrgbdi1",
		"db": "dbgbdi1",
		"roles": [
			{
				"role": "readWrite",
				"db": "dbgbdi1"
			},
			{
				"role": "dbgbdirol1",
				"db": "dbgbdi1"
			}
		]
	},
	"result": 0
}*/
/*mongod: {
	"atype": "revokeRolesFromUser",
	"ts": {
		"$date": "2022-07-05T12:17:20.646-05:00"
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
		"user": "usrgbdi1",
		"db": "dbgbdi1",
		"roles": [
			{
				"role": "dbgbdirol1",
				"db": "dbgbdi1"
			}
		]
	},
	"result": 0
}
*/