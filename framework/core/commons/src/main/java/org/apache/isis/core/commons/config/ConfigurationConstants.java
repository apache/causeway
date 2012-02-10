/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.commons.config;

public final class ConfigurationConstants {

    public static final String ROOT = "isis.";
    public static final String SHOW_EXPLORATION_OPTIONS = ROOT + "exploration.show";

    public static final String LIST_SEPARATOR = ",";
    public static final String DELIMITER = ".";
    public static final String DEFAULT_CONFIG_DIRECTORY = "config";
    public static final String WEBINF_DIRECTORY = "WEB-INF";
    public static final String WEBINF_FULL_DIRECTORY = "src/main/webapp/" + WEBINF_DIRECTORY;

    public static final String DEFAULT_CONFIG_FILE = "isis.properties";
    public static final String WEB_CONFIG_FILE = "web.properties";

    private ConfigurationConstants() {
    }
}
