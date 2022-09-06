package com.ibm.guardium.mongodb.parsersbytype;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;

public class AuthCheckParser extends BaseParser {

    @Override
    public boolean validate(JsonObject data){
        final JsonArray users = data.getAsJsonArray("users");

        if ( users.size() == 0 )  { // nor messages with empty users array, as it's an internal command (except authenticate, which states in param.user)
            return false;
        }

        return true;
    }

    /**
     * Redact except values of objects and verbs
     */
    @Override
    public JsonElement redactWithExceptions(JsonObject data) {

        final JsonObject param = data.get("param").getAsJsonObject();
        final String command = param.get("command").getAsString();
        final JsonObject args = param.getAsJsonObject("args");

        final JsonElement originalCollection = args.get(command);
        final JsonElement originalDB = args.get("$db");

        final JsonElement redactedArgs = redact(args);

        // restore common field values not to redact
        args.remove(command);
        args.add(command, originalCollection);
        args.remove("$db");
        args.add("$db", originalDB);

        return redactedArgs;
    }

    @Override
    protected Sentence parseSentence(final JsonObject data) {

        Sentence sentence = null;

        final String atype = data.get("atype").getAsString();
        final JsonObject param = data.get("param").getAsJsonObject();


        final String command = param.get("command").getAsString();
        final JsonObject args = param.getAsJsonObject("args");

        // + main object
        sentence = new Sentence(command);
        if (args.has(command)) {
            sentence.getObjects().add(parseSentenceObject(args, command));
        } else if (args.has(command.toLowerCase())) { // 2 word commands are changed to lowercase in args, sometimes(?), like mapReduce, resetErrors
            sentence.getObjects().add(parseSentenceObject(args, command.toLowerCase()));
        }

        switch (command) {
            case "aggregate":
                /*
                 * Assumes no inner-lookups; only sequential stages in pipeline.
                 */
                final JsonArray pipeline = args.getAsJsonArray("pipeline");
                if (pipeline != null && pipeline.size() > 0) {
                    for (final JsonElement stage : pipeline) {
                        // handle * lookups
                        // + object if stage has $lookup or $graphLookup: { from: obj2 }
                        JsonObject lookupStage = null;

                        if (stage.getAsJsonObject().has("$lookup")) {
                            lookupStage = stage.getAsJsonObject().getAsJsonObject("$lookup");
                        } else if (stage.getAsJsonObject().has("$graphLookup")) {
                            lookupStage = stage.getAsJsonObject().getAsJsonObject("$graphLookup");
                        }

                        if (lookupStage != null && lookupStage.has("from")) {
                            final SentenceObject lookupStageObject = new SentenceObject(
                                    lookupStage.get("from").getAsString());
                            // + object
                            sentence.getObjects().add(lookupStageObject);
                        }
                    }
                }
            default: // find, insert, delete, update, ...
                break; // already done before switch
        }

        return sentence;
    }


    protected static SentenceObject parseSentenceObject(JsonObject args, String command) {
        SentenceObject sentenceObject = null;
        if (args.get(command).isJsonPrimitive()) {
            sentenceObject = new SentenceObject(args.get(command).getAsString());
            sentenceObject.setType("collection"); // this used to be default value, but since sentence is defined in common package, "collection" as default value was removed
        } else {
            sentenceObject = new SentenceObject(COMPOUND_OBJECT_STRING);
        }
        return sentenceObject;
    }
}
