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

package org.apache.isis.core.runtime.authentication;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.authentication.exploration.ExplorationAuthenticator;
import org.apache.isis.core.runtime.authentication.exploration.ExplorationSession;
import org.apache.isis.core.runtime.authentication.fixture.LogonFixtureAuthenticator;
import org.apache.isis.core.runtime.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;

/**
 * A refinement of the {@link AuthenticationManagerStandard}, which adds support
 * to make it easier without the need of logging in when running in either
 * {@link DeploymentType#SERVER_EXPLORATION exploration} mode or in
 * {@link DeploymentType#SERVER_PROTOTYPE prototype} mode.
 * 
 * <p>
 * Specifically:
 * <ul>
 * <li>the {@link ExplorationAuthenticator} will always provide a special
 * {@link ExplorationSession} if running in the {@link DeploymentType} of
 * {@link DeploymentType#SERVER_EXPLORATION exploration}.
 * <li>the {@link LogonFixtureAuthenticator} will set up a session using the
 * login provided by a {@link LogonFixture}, provided that the
 * {@link DeploymentType} is {@link DeploymentType#SERVER_EXPLORATION exploration} or
 * {@link DeploymentType#SERVER_PROTOTYPE prototyping}
 * </ul>
 */
public class AuthenticationManagerStandardForDfltRuntime extends AuthenticationManagerStandard {

    public AuthenticationManagerStandardForDfltRuntime(final IsisConfiguration configuration) {
        super(configuration);
    }

    // //////////////////////////////////////////////////////////
    // init
    // //////////////////////////////////////////////////////////

    @Override
    protected void addDefaultAuthenticators() {
        // we add to start to ensure that these special case authenticators
        // are always consulted first
        addAuthenticatorToStart(new ExplorationAuthenticator(getConfiguration()));
        addAuthenticatorToStart(new LogonFixtureAuthenticator(getConfiguration()));
    }

    // //////////////////////////////////////////////////////////
    // Session Management (including authenticate)
    // //////////////////////////////////////////////////////////

    @Override
    public void closeSession(final AuthenticationSession session) {
        super.closeSession(session);
        IsisContext.closeSession();
    }

}
