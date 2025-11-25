
package org.logstashplugins;
import co.elastic.logstash.api.Configuration;
import java.util.HashMap;
import org.logstash.plugins.ConfigurationImpl;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Consumer;

public class JavaInputExampleTest{

//    @Test
    public void testActualPullingFromMongoAtlas() throws InterruptedException {

        Map<String, Object> configValues = new HashMap<>();
        configValues.put(MongoAtlasInput.INTERVAL_CONFIG.name(), 300L);
        configValues.put(MongoAtlasInput.PRIVATE_KEY_CONFIG.name(),"abc");
        configValues.put(MongoAtlasInput.PUBLIC_KEY_CONFIG.name(),"aaa-81d0-bbbb-ccc-a12345676789");
        configValues.put(MongoAtlasInput.GROUP_ID_CONFIG.name(), "123456789abcefg");
        configValues.put(MongoAtlasInput.HOSTNAME_CONFIG.name(), "cluster1-shard-12-34.i1234.mongodb.net");
        configValues.put(MongoAtlasInput.TYPE_CONFIG.name(), "mongodbatlas");
        configValues.put(MongoAtlasInput.MONGO_API_URL_CONFIG.name(), "https://cloud.mongodb.com/api/atlas/v1.0/groups/");

        Configuration config = new ConfigurationImpl(configValues);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MongoAtlasInput inputSample = new MongoAtlasInput("test-id", config, null);

        Consumer<Map<String, Object>> consumer = new Consumer<Map<String, Object>>() {
            @Override
            public void accept(Map<String, Object> stringObjectMap) {
                System.out.println("Got results");
                for (String s : stringObjectMap.keySet()) {
                    System.out.println(" key "+s+", value "+stringObjectMap.get(s));
                }
            }
        };

        inputSample.start(consumer);

        System.out.println("Started");

    //

    //            e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, s3Record);
    //            events.add(e);
    //            output.output(events);

        Thread.sleep(10000);
    }

}