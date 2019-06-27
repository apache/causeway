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

package org.apache.isis.runtime.system;

import org.apache.isis.config.ConfigurationConstants;
import org.apache.isis.config.IsisConfiguration;

public final class SystemConstants {
    
    public static final String MSG_ARE_YOU_SURE = "Are you sure?";
    public static final String MSG_CONFIRM = "Confirm";
    public static final String MSG_CANCEL = "Cancel";

    /**
     * Default for {@link #AUTHENTICATION_INSTALLER_KEY}.
     */
    public static final String AUTHENTICATION_DEFAULT = "shiro";

    /**
     * Default for {@link #AUTHORIZATION_DEFAULT}.
     */
    public static final String AUTHORIZATION_DEFAULT = "shiro";


    /**
     * Key by which requested fixture (eg via command line) is made available in
     * {@link IsisConfiguration}.
     */
    public final static String FIXTURE_KEY = ConfigurationConstants.ROOT + "fixtures";


    /**
     * Somewhat hacky, add this to the query
     */
    public static final String ISIS_SESSION_FILTER_QUERY_STRING_FORCE_LOGOUT = "__isis_force_logout";
    
    // -- SERVICE SUPPORT

    public static final String SERVICE_IDENTIFIER = "1";
    
    /**
     * Key by which requested user (eg via command line) is made available in
     * {@link IsisConfiguration} .
     *
     * @deprecated
     */
    @Deprecated
    public final static String USER_KEY = ConfigurationConstants.ROOT + "user";

    /**
     * Key by which requested password (eg via command line) is made available
     * in {@link IsisConfiguration}.
     *
     * @deprecated
     */
    @Deprecated
    public final static String PASSWORD_KEY = ConfigurationConstants.ROOT + "password";

    public static final String LOCALE_KEY = ConfigurationConstants.ROOT + "locale";
    
    private SystemConstants() {
    }

}
