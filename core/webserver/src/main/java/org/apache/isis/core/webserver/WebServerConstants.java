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

package org.apache.isis.core.webserver;

import org.apache.isis.config.ConfigurationConstants;
import org.apache.isis.core.webserver.WebServer.StartupMode;

public final class WebServerConstants {

    private static final String ROOT = ConfigurationConstants.ROOT + "embedded-web-server" + ".";

    public static final String EMBEDDED_WEB_SERVER_PORT_KEY = ROOT + "port";
    public static final int EMBEDDED_WEB_SERVER_PORT_DEFAULT = 8080;

    public static final String EMBEDDED_WEB_SERVER_ADDRESS_KEY = ROOT + "address";
    public static final String EMBEDDED_WEB_SERVER_ADDRESS_DEFAULT = "localhost";

    public static final String EMBEDDED_WEB_SERVER_RESOURCE_BASE_KEY = ROOT + "webapp";
    public static final String EMBEDDED_WEB_SERVER_RESOURCE_BASE_DEFAULT = ""; // or
    // "webapp"
    // ??

    public static final String EMBEDDED_WEB_SERVER_STARTUP_MODE_KEY = ROOT + "startupMode";
    public static final String EMBEDDED_WEB_SERVER_STARTUP_MODE_DEFAULT = StartupMode.FOREGROUND.name();

    private WebServerConstants() {
    }

}
