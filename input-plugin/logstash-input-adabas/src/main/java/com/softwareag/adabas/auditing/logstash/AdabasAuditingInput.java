/*
 * Copyright © 2025 Software GmbH, Darmstadt, Germany and/or its licensors
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.softwareag.adabas.auditing.logstash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;

import com.softwareag.adabas.auditingparser.ALAParse;
import com.softwareag.adabas.collector.sdk.DataObject;
import com.softwareag.entirex.aci.Broker;
import com.softwareag.entirex.aci.BrokerException;
import com.softwareag.entirex.aci.BrokerMessage;
import com.softwareag.entirex.aci.BrokerService;
import com.softwareag.entirex.aci.UnitofWork;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Input;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

// class name must match plugin name
@LogstashPlugin(name = "adabas_auditing_input")
public class AdabasAuditingInput implements Input {

    // log4j2 logger
    private static final Logger logger = LogManager.getLogger(AdabasAuditingInput.class);

    public static final PluginConfigSpec<String> HOST_CONFIG = PluginConfigSpec.stringSetting("host", "localhost");
    public static final PluginConfigSpec<Long> PORT_CONFIG = PluginConfigSpec.numSetting("port", 3000);
    public static final PluginConfigSpec<String> BROKER_CLASS_CONFIG = PluginConfigSpec.stringSetting("brokerClass",
            "class");
    public static final PluginConfigSpec<String> BROKER_SERVER_CONFIG = PluginConfigSpec.stringSetting("brokerServer",
            "server");
    public static final PluginConfigSpec<String> BROKER_SERVICE_CONFIG = PluginConfigSpec.stringSetting("brokerService",
            "service");
    public static final PluginConfigSpec<String> USER_CONFIG = PluginConfigSpec.stringSetting("user", "user");
    public static final PluginConfigSpec<String> TOKEN_CONFIG = PluginConfigSpec.stringSetting("token", "token");
    public static final PluginConfigSpec<Long> RETRY_INTERVAL_CONFIG = PluginConfigSpec.numSetting("retryInterval",
            5);
    public static final PluginConfigSpec<Long> RETRY_COUNT_CONFIG = PluginConfigSpec.numSetting("retryCount", 10);
    public static final PluginConfigSpec<Long> WAIT_TIME_CONFIG = PluginConfigSpec.numSetting("waitTime", 30);
    public static final PluginConfigSpec<Long> RECEIVE_LENGTH_CONFIG = PluginConfigSpec.numSetting("receiveLength",
            32767);
    public static final PluginConfigSpec<Long> COMPESSION_CONFIG = PluginConfigSpec.numSetting("compression", 0);
    public static final PluginConfigSpec<String> REST_URL_CONFIG = PluginConfigSpec.stringSetting("restURL", "");

    private String id;

    // input plugin parameters
    private String host;
    private int port;
    private String brokerClass;
    private String brokerServer;
    private String brokerService;
    private String user;
    private String token;
    private String retryInterval;
    private int retryCount;
    private String waitTime;
    private int receiveLength;
    private int compression;
    private String restURL;

    // EntireX Broker
    private Broker broker;
    private BrokerService service;
    private UnitofWork uow;

    // Metadata API
    private MetadataThread metadataApi;

    private final CountDownLatch done = new CountDownLatch(1);
    private volatile boolean stopped;

    // all plugins must provide a constructor that accepts id, Configuration, and
    // Context
    public AdabasAuditingInput(String id, Configuration config, Context context) {
        // constructors should validate configuration options
        this.id = id;

        host = config.get(HOST_CONFIG);
        port = Long.valueOf(config.get(PORT_CONFIG)).intValue();
        brokerClass = config.get(BROKER_CLASS_CONFIG);
        brokerServer = config.get(BROKER_SERVER_CONFIG);
        brokerService = config.get(BROKER_SERVICE_CONFIG);
        user = config.get(USER_CONFIG);
        token = config.get(TOKEN_CONFIG);
        retryInterval = config.get(RETRY_INTERVAL_CONFIG) + "s";
        retryCount = Long.valueOf(config.get(RETRY_COUNT_CONFIG)).intValue();
        waitTime = config.get(WAIT_TIME_CONFIG) + "s";
        receiveLength = Long.valueOf(config.get(RECEIVE_LENGTH_CONFIG)).intValue();
        compression = Long.valueOf(config.get(COMPESSION_CONFIG)).intValue();
        restURL = config.get(REST_URL_CONFIG);
    }

    @Override
    public void start(Consumer<Map<String, Object>> consumer) {

        logger.info("Starting Adabas Auditing input plugin");
        logger.info("Host ............ {}", host);
        logger.info("Port ............ {}", port);
        logger.info("Broker Class .... {}", brokerClass);
        logger.info("Broker Server ... {}", brokerServer);
        logger.info("Broker Service .. {}", brokerService);
        logger.info("User ............ {}", user);
        logger.info("Token ........... {}", token);
        logger.info("Retry Interval .. {}", retryInterval);
        logger.info("Retry Count ..... {}", retryCount);
        logger.info("Wait Time ....... {}", waitTime);
        logger.info("Receive Length .. {}", receiveLength);
        logger.info("Compression ..... {}", compression);
        logger.info("REST URL ........ {}", restURL);

        // The start method should push Map<String, Object> instances to the supplied
        // QueueWriter
        // instance. Those will be converted to Event instances later in the Logstash
        // event
        // processing pipeline.
        //
        // Inputs that operate on unbounded streams of data or that poll indefinitely
        // for new
        // events should loop indefinitely until they receive a stop request. Inputs
        // that produce
        // a finite sequence of events should loop until that sequence is exhausted or
        // until they
        // receive a stop request, whichever comes first.

        if (restURL.equals("")) {
            restURL = "http://localhost:8080/metadata/JSON";
        }
        if (restURL.contains("localhost")) { // starts local REST API
            String regexPort = ":[0-9]+";
            Pattern pattern = Pattern.compile(regexPort);
            Matcher matcher = pattern.matcher(restURL);
            matcher.find();
            String url = matcher.group().split(":")[1];
            int port = Integer.valueOf(url);
            metadataApi = new MetadataThread(port);
            metadataApi.run();
        }

        try {
            service = new BrokerService(getBroker(),
                    brokerClass + "/" + brokerServer + "/" + brokerService);
            service.register();
            service.setDefaultWaittime(waitTime);
            service.setMaxReceiveLen(receiveLength);
            service.setAdjustReceiveLen(true);
            getBroker().logon();
        } catch (BrokerException e) {
            e.printStackTrace();
        }

        try {
            while (!stopped) {
                byte[] message = receive();
                if (message != null) {
                    ALAParse parser = ALAParse.getInstance();
                    parser.setRestURL(restURL);
                    ArrayList<DataObject> parsedMessage = parser.parseBytesAsIndividualUABIs(message);
                    for (DataObject obj : parsedMessage) {
                        consumer.accept(Collections.singletonMap("adabas-auditing", convertToHashMap(obj)));
                    }
                    commit();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopped = true;
            done.countDown();
        }
    }

    @Override
    public void stop() {
        stopped = true; // set flag to request cooperative stop of input
        close();
    }

    @Override
    public void awaitStop() throws InterruptedException {
        done.await(); // blocks until input has stopped
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        // should return a list of all configuration options for this plugin
        return Arrays.asList(HOST_CONFIG, PORT_CONFIG, BROKER_CLASS_CONFIG,
                BROKER_SERVER_CONFIG, BROKER_SERVICE_CONFIG, USER_CONFIG, TOKEN_CONFIG, RETRY_INTERVAL_CONFIG,
                RETRY_COUNT_CONFIG, WAIT_TIME_CONFIG, RECEIVE_LENGTH_CONFIG, COMPESSION_CONFIG,
                REST_URL_CONFIG);
    }

    @Override
    public String getId() {
        return this.id;
    }

    private void close() {
        try {
            if (service != null) {
                service.deregisterImmediate();
            }
            broker.logoff();
            broker.disconnect();
        } catch (Exception e) {
        }
    }

    private Broker getBroker() {
        if (broker == null) {
            broker = new Broker(host + ":" + port, user, token, Integer.MAX_VALUE);
        }
        return broker;
    }

    /**
     * Receive a message from Broker.
     * 
     * @return Raw message from Broker as a byte array.
     */
    private byte[] receive() {

        // logger.traceEntry();

        BrokerMessage brokerMessage = null;

        if (uow == null) {
            uow = new UnitofWork(service);
        }
        try {
            brokerMessage = uow.receive();
        } catch (BrokerException ex) {
            if (ex.getErrorClass() == 74 && ex.getErrorCode() == 74) { // simple timeout loop
                // logger.debug("BrokerException!: {}: Error class = {}: Error code = {}",
                // ex.getMessage(), ex.getErrorClass(), ex.getErrorCode());
            } else { // some real messaging error
                try {
                    logger.error("BrokerException: {}: Error class = {}: Error code = {}",
                            ex.getMessage(), ex.getErrorClass(), ex.getErrorCode());
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                } // wait before return
                  // connected = false; // all other errors assume connection lost
            }
        }
        // logger.traceExit();

        if (brokerMessage != null)
            return brokerMessage.getMessage();
        else
            return null;
    }

    private void commit() throws BrokerException {
        if (uow.getStatus().equals("RECV_ONLY") ||
                uow.getStatus().equals("RECV_LAST")) {
            uow.commitEndConversation();
            uow = null;
        }
    }

    private HashMap<String, Object> convertToHashMap(DataObject object) {
        HashMap<String, Object> map = new HashMap<>();
        // iterate over hashmap
        for (Map.Entry<String, Object> entry : object.getList().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof DataObject) {
                Object obj = convertToHashMap((DataObject) value);
                map.put(key, obj);
            } else {
                if (value instanceof ArrayList<?>) {
                    ArrayList<Object> list = new ArrayList<>();
                    for (Object obj : (ArrayList<?>) value) {
                        if (obj instanceof DataObject) {
                            list.add(convertToHashMap((DataObject) obj));
                        } else {
                            list.add(obj);
                        }
                    }
                    map.put(key, list);
                } else {
                    map.put(key, value);
                }
            }
        }
        return map;
    }
}
