package com.ibm.guardium.mongodb.parsersbytype;

import com.google.gson.JsonObject;
import com.ibm.guardium.mongodb.AType;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;

import static com.ibm.guardium.mongodb.AType.*;

public class CollectionParser extends BaseParser{

    public Sentence parseSentence(JsonObject data){
        String atype = getAType(data);
        Sentence sentence = new Sentence(atype); // atype is command
        AType atypeEn = typeByPropertyValue(atype);
        if (COLLECTION_RENAME.equals(atypeEn)){
            return buildRenameSentence(data, sentence);
        }

        String[] nsArray = ParserUtils.getDbAndCollection(data, "ns");
        SentenceObject sentenceObject = buildObject(nsArray);
        sentence.getObjects().add(sentenceObject);

        if (COLLECTION_SHARD.equals(atypeEn)){
            return addShardDetailsToSentence(data, sentence);
        }

        return sentence;
    }

    @Override
    protected String parseDatabaseName(JsonObject param, JsonObject args, JsonObject wholeData) {
        if (getAType(wholeData).equals(COLLECTION_RENAME.getPropertyValue())){
            String[] oldParts = ParserUtils.getDbAndCollection(wholeData, "old");
            return oldParts[0];
        } else {
            return super.parseDatabaseName(param, args, wholeData);
        }
    }

    private Sentence addShardDetailsToSentence(JsonObject data, Sentence sentence) {
        JsonObject param = data.get("param").getAsJsonObject();
        String keyPattern = param.get("key").getAsString();
        sentence.getFields().add(keyPattern);
        String unique = param.get("options") == null ? "false" : param.get("options").getAsJsonObject().get("unique").getAsString();
        if ("true".equals(unique)){
            sentence.getFields().add("unique");
        }
        return sentence;
    }

    private Sentence buildRenameSentence(JsonObject data, Sentence sentence) {
        String[] oldParts = ParserUtils.getDbAndCollection(data, "old");
        SentenceObject sentenceObjectOld = buildObject(oldParts);
        sentence.getObjects().add(sentenceObjectOld);
        String[] newParts = ParserUtils.getDbAndCollection(data, "new");
        SentenceObject sentenceObjectNew = buildObject(newParts);
        sentence.getObjects().add(sentenceObjectNew);
        return sentence;
    }

    private SentenceObject buildObject(String[] parts){
        SentenceObject sentenceObject = new SentenceObject(parts[1], parts[0]);
        sentenceObject.setType("collection");
        return sentenceObject;
    }

}

/*
*
* {
	"atype": "createCollection",
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
		"ns": "dbgbdi1.collgbdi1"
	},
	"result": 0
}
*/
/*mongod: {
	"atype": "dropCollection",
	"ts": {
		"$date": "2022-07-05T12:17:20.653-05:00"
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
		"ns": "dbgbdi1.collgbdiT1"
	},
	"result": 0
}*/
