package com.ibm.guardium.mongodb.parsersbytype;

import com.google.gson.JsonObject;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Sentence;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.SentenceObject;

public class ShardParser extends BaseParser {
    @Override
    protected Sentence parseSentence(JsonObject data) {
        JsonObject param = data.get("param").getAsJsonObject();

        String shard = param.get("shard").getAsString();
        String connectionString = param.get("connectionString").getAsString();
        String maxSize = param.get("maxSize").getAsString();

        Sentence sentence = new Sentence(getAType(data)); // atype is command
        SentenceObject sentenceObject = new SentenceObject(shard, shard);
        sentenceObject.setType("shard");
        sentence.getObjects().add(sentenceObject);
        sentence.getFields().add(connectionString);
        sentence.getFields().add(maxSize);

        return sentence;

    }
}
