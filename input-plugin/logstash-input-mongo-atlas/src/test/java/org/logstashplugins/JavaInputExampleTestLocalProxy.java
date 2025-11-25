
package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.logstash.plugins.ConfigurationImpl;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class JavaInputExampleTestLocalProxy {
    @BeforeClass
    public static void setUp() {
        System.setProperty("HTTPS_PROXY","http://localhost:1234");
    }

    @Test
    public void testAtlasInputUsingProxy(){

        Map<String, Object> configValues = new HashMap<>();
        configValues.put(MongoAtlasInput.INTERVAL_CONFIG.name(), 5L);
        configValues.put(MongoAtlasInput.PRIVATE_KEY_CONFIG.name(),"abc");
        configValues.put(MongoAtlasInput.PUBLIC_KEY_CONFIG.name(),"aaa-81d0-bbbb-ccc-a12345676789");
        configValues.put(MongoAtlasInput.GROUP_ID_CONFIG.name(), "123456789abcefg");
        configValues.put(MongoAtlasInput.HOSTNAME_CONFIG.name(), "cluster1-shard-12-34.i1234.mongodb.net");
        configValues.put(MongoAtlasInput.TYPE_CONFIG.name(), "mongodbatlas");
        configValues.put(MongoAtlasInput.MONGO_API_URL_CONFIG.name(), "https://cloud.mongodb.com/api/atlas/v1.0/groups/");

        Configuration config = new ConfigurationImpl(configValues);
        MongoAtlasInput inputSample = new MongoAtlasInput("test-id", config, null);

        new Thread(new Runnable() {
            HttpProxyServer httpProxyServer = null;
            @Override
            public void run() {
                Consumer<String> consumer = new Consumer<String>() {
                    @Override
                    public void accept(String msg) {
                        inputSample.stop();
                        System.out.println("Message is "+msg);
                        Assert.assertTrue(HttpProxyServer.PROXY_GOT_REQUEST.equals(msg));
                        Thread.currentThread().interrupt();
                    }
                };
                httpProxyServer = new HttpProxyServer(1234, consumer);
            }
        }).start();

        Consumer<Map<String, Object>> consumer = new Consumer<Map<String, Object>>() {
            @Override
            public void accept(Map<String, Object> stringObjectMap) {
                System.out.println("testAtlasInputUsingProxy - got results");
                for (String s : stringObjectMap.keySet()) {
                    System.out.println("testAtlasInputUsingProxy key "+s+", value "+stringObjectMap.get(s));
                }
            }
        };

        inputSample.start(consumer);
    }

}