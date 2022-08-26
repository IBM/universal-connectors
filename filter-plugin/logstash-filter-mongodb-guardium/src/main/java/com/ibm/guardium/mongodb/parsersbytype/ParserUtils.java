package com.ibm.guardium.mongodb.parsersbytype;

import com.google.gson.JsonObject;

public class ParserUtils {

    public static String[] getDbAndCollection(JsonObject source, String paramPropertyName){
        String[] parts = null;
        String str = source.get("param").getAsJsonObject().get(paramPropertyName).getAsString();
        if (str!=null){
            parts = str.split("\\."); //"ns": "dbgbdi1.collgbdi1"
        }
        return parts;
    }
}
