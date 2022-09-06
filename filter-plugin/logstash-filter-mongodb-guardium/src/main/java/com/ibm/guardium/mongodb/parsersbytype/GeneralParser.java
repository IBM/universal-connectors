package com.ibm.guardium.mongodb.parsersbytype;

import com.google.gson.JsonObject;
import com.ibm.guardium.mongodb.AType;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;

public class GeneralParser extends BaseParser {

    @Override
    protected Sentence parseSentence(JsonObject data) {
        String atype = getAType(data);
        Sentence sentence = new Sentence(atype); // atype is command
        AType aTypeEn = AType.typeByPropertyValue(atype);
        if (AType.GENERAL_SHUTDOWN.equals(aTypeEn)){
            return buildShutdownSentence(data, sentence); // check with Tim if we can send sentence with only verb
        } else if (AType.GENERAL_APPLICATION_MESSAGE.equals(aTypeEn)){
            return buildLogApplicationMessageSentece(data, sentence);
        } else if (AType.GENERAL_REPL_SET_RECONFIG.equals(aTypeEn)){
            return buildReplSetReconfig(data, sentence);
        }
        return sentence;
    }

    private Sentence buildShutdownSentence(JsonObject data, Sentence sentence) {
        SentenceObject sentenceObject = new SentenceObject("all", "all");
        sentenceObject.setType("database");
        sentence.getObjects().add(sentenceObject);
        return sentence;
    }

    private Sentence buildLogApplicationMessageSentece(JsonObject data, Sentence sentence) {
        SentenceObject sentenceObject = new SentenceObject("msg", "applicationMessage");
        sentenceObject.setType("applicationMessage");
        sentence.getObjects().add(sentenceObject);
        sentence.getFields().add(data.get("param").getAsJsonObject().get("msg").getAsString());
        return sentence;
    }


    private Sentence buildReplSetReconfig(JsonObject data, Sentence sentence) {
        SentenceObject sentenceObjectOld = buildReplSetObject(data, "old");
        sentence.getObjects().add(sentenceObjectOld);

        SentenceObject sentenceObjectNew = buildReplSetObject(data, "new");
        sentence.getObjects().add(sentenceObjectNew);
        return sentence;
    }

    private SentenceObject buildReplSetObject(JsonObject data, String property){
        String oldName = data.get("param").getAsJsonObject().get(property).getAsJsonObject().get("_id").getAsString();
        SentenceObject sentenceObject = new SentenceObject(oldName, property);
        sentenceObject.setType("replSetConfig");
        return sentenceObject;
    }

}
