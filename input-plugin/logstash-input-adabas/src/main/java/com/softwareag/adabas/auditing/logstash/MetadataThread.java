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

import com.softwareag.adabas.adamfmetadatarest.web.MetaDataController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class MetadataThread extends Thread {

    private int apiPort;
    private static final Logger logger = LogManager.getLogger();

    public MetadataThread(int apiPort) {
        this.apiPort = apiPort;
    }

    @Override
    public void run() {
        try {
            MetaDataController.startAPI(apiPort, false);
        } catch (IOException e) {
            logger.error("", e);
        }
    }
}
