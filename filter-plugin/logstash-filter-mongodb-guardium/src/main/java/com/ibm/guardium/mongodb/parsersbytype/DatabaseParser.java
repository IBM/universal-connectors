package com.ibm.guardium.mongodb.parsersbytype;

import com.google.gson.JsonObject;
import com.ibm.guardium.mongodb.parsersbytype.BaseParser;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Sentence;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.SentenceObject;

public class DatabaseParser extends BaseParser {

    public Sentence parseSentence(JsonObject data){

        JsonObject param = data.get("param").getAsJsonObject();

        String db = param.get("ns")!= null ? param.get("ns").getAsString() : param.get("db").getAsString();// dropAllRolesFromDatabase has "db"

        Sentence sentence = new Sentence(getAType(data)); // atype is command
        SentenceObject sentenceObject = new SentenceObject(db, db);
        sentenceObject.setType("database");
        sentence.getObjects().add(sentenceObject);

        return sentence;
    }

}

/*
* {
	"atype": "createDatabase",
	"ts": {
		"$date": "2022-07-05T12:17:20.472-05:00"
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
		"ns": "dbgbdi1"
	},
	"result": 0
}
*/
/*mongod: {
	"atype": "dropDatabase",
	"ts": {
		"$date": "2022-07-05T12:17:20.654-05:00"
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
		"ns": "dbgbdi1"
	},
	"result": 0
}*/
/*mongod: {
	"atype": "dropAllRolesFromDatabase",
	"ts": {
		"$date": "2022-07-05T12:17:20.650-05:00"
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
		"db": "dbgbdi1"
	},
	"result": 0
}*/
