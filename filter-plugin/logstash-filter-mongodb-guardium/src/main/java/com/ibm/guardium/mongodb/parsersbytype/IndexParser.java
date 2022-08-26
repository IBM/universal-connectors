package com.ibm.guardium.mongodb.parsersbytype;

import com.google.gson.JsonObject;
import com.ibm.guardium.mongodb.parsersbytype.BaseParser;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Accessor;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Sentence;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.SentenceObject;

public class IndexParser extends BaseParser {

    public static final String USER_NOT_AVAILABLE = "N/A";

    public Sentence parseSentence(JsonObject data) {
        JsonObject param = data.get("param").getAsJsonObject();
//        String dbCollection = param.get("ns").getAsString();
        String ns = param.get("ns").getAsString();
        String[] nsArray = ns.split("\\.");
        String db = nsArray[0];
        String collection = nsArray[1];

        String indexName = param.get("indexName").getAsString();
        Sentence sentence = new Sentence(getAType(data)); // atype is command
        SentenceObject sentenceObject = new SentenceObject(indexName, db);
        sentenceObject.setType("index");
        sentence.getObjects().add(sentenceObject);
        sentence.getFields().add(collection);

        return sentence;
    }

    @Override
    public Accessor parseAccessor(JsonObject data) {
        Accessor accessor = super.parseAccessor(data);
        if (accessor.getDbUser() == "") {
            accessor.setDbUser(USER_NOT_AVAILABLE);
        }
        return accessor;
    }
}
/*mongod: {
	"atype": "dropIndex",
	"ts": {
		"$date": "2022-07-05T12:17:20.628-05:00"
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
		"ns": "dbgbdi1.collgbdi1",
		"indexName": "userid_1"
	},
	"result": 0
}
*/