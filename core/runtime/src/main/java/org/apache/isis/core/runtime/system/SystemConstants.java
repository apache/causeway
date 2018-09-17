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

package org.apache.isis.core.runtime.system;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManagerInstaller;

public final class SystemConstants {

    /**
     * Key used to lookup {@link DeploymentType} (eg via command line) in
     * {@link IsisConfiguration}.
     *
     * <p>
     * Use {@link DeploymentType#lookup(String)} to decode.
     */
    public static final String DEPLOYMENT_TYPE_KEY = ConfigurationConstants.ROOT + "deploymentType";




    /**
     * Key used to lookup {@link AppManifest} (if any) from the {@link IsisConfiguration}.
     */
    public static final String APP_MANIFEST_KEY = ConfigurationConstants.ROOT + "appManifest";

    /**
     * Key used to lookup {@link AuthenticationManager authentication manager}
     * in {@link IsisConfiguration}, and root for any
     * {@link AuthenticationManagerInstaller authentication manager}-specific
     * configuration keys.
     */
    public static final String AUTHENTICATION_INSTALLER_KEY = ConfigurationConstants.ROOT + AuthenticationManagerInstaller.TYPE;
    /**
     * Default for {@link #AUTHENTICATION_INSTALLER_KEY}.
     */
    public static final String AUTHENTICATION_DEFAULT = "shiro";

    /**
     * Key used to lookup {@link AuthorizationManager authorization manager} in
     * {@link IsisConfiguration}, and root for any
     * {@link AuthorizationManagerInstaller authorization manager}-specific
     * configuration keys.
     */
    public static final String AUTHORIZATION_INSTALLER_KEY = ConfigurationConstants.ROOT + AuthorizationManagerInstaller.TYPE;
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
