package com.ibm.guardium.s3;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.google.gson.*;
import com.ibm.guardium.universalconnector.commons.structures.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.net.util.IPAddressUtil;

public class Parser {

    private static Logger log = LogManager.getLogger(Parser.class);

    private static final String DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss";// "yyyy-MM-dd'T'HH:mm:ssZ";
    public static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    public static final String UNKNOWN_STRING = "";
    public static final String UNKNOWN_IP = "0.0.0.0";
    public static final String BUCKETNAME_PROPERTY = "bucketName";
    public static final String BUCKET_PROPERTY = "Bucket";
    public static final String S3_TYPE = "S3";
    public static final String S3_PROTOCOL = "S3 native audit";
    private static final Gson   gson = new Gson();


    public static Record buildRecord(JsonElement auditEl) throws Exception {

        if (auditEl==null || !auditEl.isJsonObject()){
            throw new Exception("Invalid event data");
        }

        Record record = new Record();

        // ------------------ Build record upper layer
        // userIdentity is a mandatory property
        JsonObject auditObj = (JsonObject)auditEl;
        JsonObject userIdentity = auditObj.get("userIdentity").getAsJsonObject();
        String userName = searchForAppUserName(userIdentity);
        record.setAppUserName(userName);

        record.setSessionId(userIdentity.toString());

        JsonObject requestParameters = auditObj.get("requestParameters")!=null ? auditObj.get("requestParameters").getAsJsonObject() : null;
        record.setDbName(searchForBucketName(requestParameters));

        Time time = getTime(getStrValue(auditObj,"eventTime"));
        record.setTime(time);

        // ------------------ Build Session locator
        SessionLocator sessionLocator = new SessionLocator();
        String sourceIPAddress = validateIP(getStrValue(auditObj,"sourceIPAddress"));
        sessionLocator.setClientIp(sourceIPAddress);
        sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);

        String host = getHost(requestParameters);
        String serverIP = UNKNOWN_IP;//validateIP(host);
        sessionLocator.setServerIp(serverIP);
        sessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);

        record.setSessionLocator(sessionLocator);

        // ------------------ Build Accessor
        Accessor accessor = new Accessor();
        accessor.setDbUser(userName);
        accessor.setServerType(S3_TYPE);
        accessor.setDbProtocol(S3_PROTOCOL);
        accessor.setClientHostName(getStrValue(auditObj,"sourceIPAddress"));
        accessor.setServerHostName(getStrValue(auditObj,"eventSource"));
        accessor.setCommProtocol(getStrValue(auditObj,"eventType")); // talk to Itai
        accessor.setDbProtocolVersion(getStrValue(auditObj,"eventVersion"));
        accessor.setServiceName(getStrValue(auditObj,"awsRegion"));
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);

        accessor.setServerDescription(getStrValue(auditObj,"awsRegion"));
        accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);

        accessor.setServerOs(UNKNOWN_STRING);
        accessor.setOsUser(UNKNOWN_STRING);
        accessor.setClient_mac(UNKNOWN_STRING);
        accessor.setOsUser(UNKNOWN_STRING);

        setUserAgentRelatedFields(accessor, auditObj);

        record.setAccessor(accessor);

        // ------------------ Build data or Exception
        String auditAsSql = gson.toJson(auditObj);
        String errorCode = getStrValue(auditObj,"errorCode");
        if (!UNKNOWN_STRING.equalsIgnoreCase(errorCode)){
            ExceptionRecord exceptionRecord = new ExceptionRecord();
            record.setException(exceptionRecord);

            exceptionRecord.setExceptionTypeId(errorCode);
            exceptionRecord.setDescription(getStrValue(auditObj,"errorMessage"));
            exceptionRecord.setSqlString(auditAsSql);

        } else {
            Data data = new Data();
            record.setData(data);

            Construct construct = new Construct();
            data.setConstruct(construct);
            construct.setFullSql(auditAsSql);
            construct.setRedactedSensitiveDataSql(auditAsSql);

            ArrayList<Sentence> sentences = new ArrayList<>();
            construct.setSentences(sentences);

            String eventName = getStrValue(auditObj, "eventName");
            Sentence sentence = new Sentence(eventName); //verb
            sentences.add(sentence);

            ArrayList<SentenceObject> objects = new ArrayList<>();
            sentence.setObjects(objects);

            // if there are resources - treat each one of them as object and add to the data
            String resourcesStr = getStrValue(auditObj, "resources");
            try {
                if (!UNKNOWN_STRING.equals(resourcesStr) && resourcesStr != null && resourcesStr.length() > 0) {

                    resourcesStr = gson.toJson(auditObj.get("resources"));

                    JsonElement resourcesJSON = JsonParser.parseString(resourcesStr);
                    if (resourcesJSON.isJsonArray()) {
                        JsonArray resourcesArr = (JsonArray) resourcesJSON;
                        for (int i = 0; i < resourcesArr.size(); i++) {
                            try {
                                SentenceObject resourceToObject = parseResourceToObject((JsonObject) resourcesArr.get(i));
                                objects.add(resourceToObject);
                            } catch (Exception e) {
                                log.error("Failed to parse resource to object, resource str is " + resourcesArr.get(i).getAsString(), e);
                            }
                        }
                    } else if (resourcesJSON.isJsonObject()) {
                        try {
                            SentenceObject resourceToObject = parseResourceToObject((JsonObject) resourcesJSON);
                            objects.add(resourceToObject);
                        } catch (Exception e) {
                            log.error("Failed to parse resource to object, resource str is " + resourcesJSON.getAsString(), e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to process resources for event, resourcesStr " + resourcesStr, e);
            }

            // if not found object in resources property - use the value in "key" field of request parameters
            if (objects.size() == 0) {
                SentenceObject object = new SentenceObject(getStrValue(requestParameters, "key"));
                objects.add(object);
            }
        }

        return record;
    }

    static void setUserAgentRelatedFields(Accessor accessor, JsonObject auditObj){
        //"userAgent": "aws-cli/1.10.32 Python/2.7.9 Windows/7 botocore/1.4.22",
        //"userAgent": "[Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:68.0) Gecko/20100101 Firefox/68.0]"
        //"userAgent": "[S3Console/0.4, aws-internal/3 aws-sdk-java/1.11.783 Linux/4.9.217-0.1.ac.205.84.332.metal1.x86_64 OpenJDK_64-Bit_Server_VM/25.252-b09 java/1.8.0_252 vendor/Oracle_Corporation]"
        //"userAgent": "cloudtrail.amazonaws.com"
        String userAgent = getStrValue(auditObj,"userAgent");

        // source program
        String sourceProgram = userAgent;
        int start = userAgent.indexOf("[") >= 0 ? userAgent.indexOf("[")+1 : 0;
        int end = userAgent.indexOf("/") > 0 ? userAgent.indexOf("/") : -1;
        if (start >= 0 && end > 0) {
            sourceProgram = userAgent.substring(start, end).trim();
        }
        accessor.setSourceProgram(sourceProgram);

        // client os
        String clientOs = UNKNOWN_STRING;
        start = userAgent != null ? userAgent.indexOf(";")+1 : -1;
        end = userAgent != null ? userAgent.indexOf(";", start) : -1;
        if (start >= 0 && end > 0){
            clientOs = userAgent.substring(start, end).trim();
        }
        accessor.setClientOs(clientOs);
    }

    private static String searchForBucketName(JsonElement data) {

        if (data==null){
            return UNKNOWN_STRING;

        } else if (data.isJsonPrimitive()){
            return UNKNOWN_STRING;

        } else if (data.isJsonArray()){
            // scan each arr object for bucket, use first bucket found
            JsonArray array = data.getAsJsonArray();
            String bucketName = null;
            for (JsonElement jsonElement : array) {
                bucketName = searchForBucketName(jsonElement);
                if (bucketName!=null){
                    return bucketName;
                }
            }

        } else if (data.isJsonObject()){
            // scan object properties for bucket
            JsonObject jsonObject = data.getAsJsonObject();
            JsonElement bucketNameEl = jsonObject.get(BUCKETNAME_PROPERTY);
            if (bucketNameEl!=null){
                return bucketNameEl.getAsString();
            }
            bucketNameEl = jsonObject.get(BUCKET_PROPERTY);
            if (bucketNameEl!=null){
                return bucketNameEl.getAsString();
            }

            // no direct bucket property - need to inner objects
            Set<String> properties = jsonObject.keySet();
            for (String property : properties) {
                String bucketName = searchForBucketName(jsonObject.get(property));
                if (bucketName!=null){
                    return bucketName;
                }
            }
        }
        // At this point - nothing was found, so just return null
        return UNKNOWN_STRING;
    }

    public static String searchForAppUserName(JsonObject userIdentity) {
        //https://docs.aws.amazon.com/awscloudtrail/latest/userguide/cloudtrail-event-reference-user-identity.html#cloudtrail-event-reference-user-identity-examples
        //maybe better use the link below?
        //https://docs.aws.amazon.com/macie/latest/userguide/macie-users.html
        String type = getStrValue(userIdentity, "type");
        String userName = UNKNOWN_STRING;
        switch (type){
            case "Root":
            case "IAMUser":
                userName = getStrValue(userIdentity,"userName");
                break;
            case "AssumedRole":
            case "FederatedUser":
                JsonObject sessionContext = userIdentity.get("sessionContext").getAsJsonObject();
                JsonObject sessionIssuer = sessionContext.get("sessionIssuer").getAsJsonObject();
                String sessionIssuerType = getStrValue( sessionIssuer, "type ");
                if ("Root".equals(sessionIssuerType)){
                    userName = "Root sessionIssuer type";
                } else {
                    userName = getStrValue(sessionIssuer, "userName");
                }
                break;
            case "AWSAccount":
                userName = "AWSService";
                break;
            case "AWSService":
                userName = "AWSService";
        }
        return userName;
    }

    public static String getHost(JsonObject requestParameters){
        String[] properties = {"Host", "host"};
        for (String property : properties) {
            JsonElement val = requestParameters.get(property);
            if (val != null) {
                if (val.isJsonPrimitive()) {
                    return val.getAsString();
                } else if (val.isJsonArray()){
                    return val.getAsJsonArray().get(0).getAsString();
                }
            }
        }
        return null;
    }

    public static String validateIP(String sourceIPAddress){
        if (IPAddressUtil.isIPv4LiteralAddress(sourceIPAddress) || IPAddressUtil.isIPv6LiteralAddress(sourceIPAddress)){
            return sourceIPAddress;
        } else {
            return UNKNOWN_IP;
        }
    }

    /*
    * {\n" +
                    "            \"accountId\": \"987076625343\",\n" +
                    "            \"type\": \"AWS::S3::Bucket\",\n" +
                    "            \"ARN\": \"arn:aws:s3:::guardiumdatalogig\"\n" +
                    "        }\n"
    * */
    private static SentenceObject parseResourceToObject(JsonObject jsonObj) {
        String arn = getStrValue(jsonObj, "ARN");
        String name = extractLastPartOfString(arn, /*":::"*/":");
        SentenceObject obj = new SentenceObject(name);

        String typeStr = getStrValue(jsonObj, "type");
        String type = extractLastPartOfString(typeStr, /*"::"*/":");
        obj.type = type;

        return obj;
    }

    private static String extractLastPartOfString(String orig, String delimiter){
        String[] parts = orig.split(delimiter);
        String part = parts.length>0 ? parts[parts.length-1]:null;
        return part;
    }

    private static String getStrValue(JsonObject data, String[] properties){
        if (properties==null){
            return UNKNOWN_STRING;
        }
        for (String property : properties) {
            String val = getStrValue(data, property);
            if (!UNKNOWN_STRING.equalsIgnoreCase(val)){
                return val;
            }
        }
        return UNKNOWN_STRING;
    }

    private static String getStrValue(JsonObject data, String property){
        String value = UNKNOWN_STRING;
        if (data!=null){
            JsonElement val = data.get(property);
            if (val!=null){
                if (val.isJsonPrimitive()) {
                    value = val.getAsString();
                } else {
                    value = val.toString();
                }
            }
        }
        if (value==null){
            value = UNKNOWN_STRING;
        }
        return value;
    }

    public static Time getTime(String dateString){
        ZonedDateTime date = ZonedDateTime.parse(dateString, DATE_TIME_FORMATTER);
        long millis = date.toInstant().toEpochMilli();
        int  minOffset = date.getOffset().getTotalSeconds()/60;
        //int  minDst = date.getOffset().getRules().isDaylightSavings(date.toInstant()) ? 60 : 0;
        return new Time(millis, minOffset, 0);
    }

    public static void main(String[] args){
        //Parser parser = new Parser();
        try {
//            String anotherEvent = "{\"eventID\":\"e3de78ee-4fab-40ef-b421-bfeff7f3c61c\",\"awsRegion\":\"us-east-1\",\"eventCategory\":\"Data\",\"eventVersion\":\"1.07\",\"responseElements\":null,\"sourceIPAddress\":\"77.125.48.244\",\"requestParameters\":{\"bucketName\":\"bucketnewbucketkkkkk\",\"X-Amz-Date\":\"20200622T202153Z\",\"response-content-disposition\":\"inline\",\"X-Amz-Algorithm\":\"AWS4-HMAC-SHA256\",\"X-Amz-SignedHeaders\":\"host\",\"Host\":\"bucketnewbucketkkkkk.s3.us-east-1.amazonaws.com\",\"X-Amz-Expires\":\"300\",\"key\":\"mkkkk/sampleJson.json\"},\"eventSource\":\"s3.amazonaws.com\",\"resources\":[{\"type\":\"AWS::S3::Object\",\"ARN\":\"arn:aws:s3:::bucketnewbucketkkkkk/mkkkk/sampleJson.json\"},{\"type\":\"AWS::S3::Bucket\",\"ARN\":\"arn:aws:s3:::bucketnewbucketkkkkk\",\"accountId\":\"987076625343\"}],\"readOnly\":true,\"userAgent\":\"[Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36]\",\"userIdentity\":{\"sessionContext\":{\"attributes\":{\"mfaAuthenticated\":\"false\",\"creationDate\":\"2020-06-22T08:52:40Z\"}},\"accessKeyId\":\"ASIA6LUS2AO73E6D2NJP\",\"accountId\":\"987076625343\",\"principalId\":\"AIDAJWW2XAIOY2WN3KAAM\",\"arn\":\"arn:aws:iam::987076625343:user/ProxyTest\",\"type\":\"IAMUser\",\"userName\":\"ProxyTest\"},\"eventType\":\"AwsApiCall\",\"additionalEventData\":{\"SignatureVersion\":\"SigV4\",\"AuthenticationMethod\":\"QueryString\",\"x-amz-id-2\":\"CcbPzGZRlYbGdJPjFp8IcxuVi7ZhB01wkVVwhjkRDFbUDdFB+GKsV5oRannp3oOt2qCMUEXy37I\\u003d\",\"CipherSuite\":\"ECDHE-RSA-AES128-GCM-SHA256\",\"bytesTransferredOut\":19.0,\"bytesTransferredIn\":0.0},\"requestID\":\"4D5B1CD23AEDD7C7\",\"eventTime\":\"2020-06-22T20:21:53Z\",\"eventName\":\"GetObject\",\"recipientAccountId\":\"987076625343\",\"managementEvent\":false}";
//            JsonObject inputJSON = (JsonObject) JsonParser.parseString(anotherEvent);

            String parseErrorEvent = "{ \"message\" : {\"eventVersion\":\"1.05\",\"userIdentity\":{\"type\":\"Root\",\"principalId\":\"987076625343\",\"arn\":\"arn:aws:iam::987076625343:root\",\"accountId\":\"987076625343\",\"accessKeyId\":\"ASIA6LUS2AO72SC3PEOE\",\"userName\":\"guardiumproxy\",\"sessionContext\":{\"attributes\":{\"mfaAuthenticated\":\"false\",\"creationDate\":\"2020-07-24T18:35:41Z\"}}},\"eventTime\":\"2020-07-24T18:56:42Z\",\"eventSource\":\"s3.amazonaws.com\",\"eventName\":\"GetBucketPublicAccessBlock\",\"awsRegion\":\"us-east-1\",\"sourceIPAddress\":\"108.20.188.184\",\"userAgent\":\"[S3Console/0.4, aws-internal/3 aws-sdk-java/1.11.783 Linux/4.9.217-0.1.ac.205.84.332.metal1.x86_64 OpenJDK_64-Bit_Server_VM/25.252-b09 java/1.8.0_252 vendor/Oracle_Corporation]\",\"requestParameters\":{\"publicAccessBlock\":[\"\"],\"host\":[\"guardium01.s3.us-east-1.amazonaws.com\"],\"bucketName\":\"guardium01\"},\"responseElements\":null,\"additionalEventData\":{\"SignatureVersion\":\"SigV4\",\"CipherSuite\":\"ECDHE-RSA-AES128-SHA\",\"AuthenticationMethod\":\"AuthHeader\",\"vpcEndpointId\":\"vpce-f40dc59d\"},\"requestID\":\"CB080A8B962B420B\",\"eventID\":\"3a9e5492-e212-4dd9-9829-6aea6c861272\",\"eventType\":\"AwsApiCall\",\"recipientAccountId\":\"987076625343\",\"vpcEndpointId\":\"vpce-f40dc59d\"}}";
            JsonObject inputJSON = (JsonObject) JsonParser.parseString(parseErrorEvent);

//            JsonObject inputJSON = (JsonObject) JsonParser.parseString(EventSamples.getSamplesByEventName(EventSamples.EventName.DeleteBucket).get(0).getJsonStr());
            Record record = Parser.buildRecord(inputJSON);
            System.out.println(record);
            String recordStr = gson.toJson(record);
            System.out.println(recordStr);
        } catch (Exception e){
            log.error("Failed to parse", e);
            System.out.println(e);
            e.printStackTrace();
        }
    }
}