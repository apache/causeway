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

package org.apache.isis.config;

import org.apache.isis.applib.fixtures.LogonFixture;

public final class WebAppConstants {

    /**
     * Key under which the {@link AuthenticationSession} is bound as a session
     * attribute ( <tt>HttpSession#getAttribute(String)</tt>).
     */
    public final static String HTTP_SESSION_AUTHENTICATION_SESSION_KEY = WebAppConstants.class.getPackage().getName() + ".authenticationSession";

    /**
     * Key used to determine if a logon has already been performed implicitly
     * using the {@link LogonFixture}, meaning that a Logout should be followed
     * by the Logon page.
     */
    public final static String HTTP_SESSION_LOGGED_ON_PREVIOUSLY_USING_LOGON_FIXTURE_KEY = WebAppConstants.class.getPackage().getName() + ".loggedOnPreviouslyUsingLogonFixture";

    /**
     * Property name given to the web app directory.
     */
    public static final String WEB_APP_DIR = "application.webapp.dir";

    /**
     * Servlet context parameter name used to specify the location for property file
     * overloads. Property files in this location take precedence over property files
     * in other locations. Since the ResourceStreamSourceComposite is being used
     * property files from various locations are merged together. So only overloaded
     * properties (entries in the file) need to present.
     */
    public static final String CONFIG_DIR_PARAM = "isis.config.dir";

    private WebAppConstants() {
    }

}
