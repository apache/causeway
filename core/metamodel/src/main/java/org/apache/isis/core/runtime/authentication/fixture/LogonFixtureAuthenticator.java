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

package org.apache.isis.core.runtime.authentication.fixture;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.standard.AuthenticatorAbstract;
import org.apache.isis.core.runtime.fixtures.authentication.AuthenticationRequestLogonFixture;

public class LogonFixtureAuthenticator extends AuthenticatorAbstract {

    public LogonFixtureAuthenticator(final IsisConfiguration configuration) {
        super(configuration);
    }

    /**
     * Can authenticate if a {@link AuthenticationRequestLogonFixture}.
     */
    @Override
    public final boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
        return AuthenticationRequestLogonFixture.class.isAssignableFrom(authenticationRequestClass);
    }

    @Override
    protected final boolean isValid(final AuthenticationRequest request) {
        return _Context.getEnvironment().getDeploymentType().isPrototyping();
    }


}
