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

package org.apache.isis.core.runtime.authentication.standard;

import java.util.List;

import org.apache.isis.core.commons.components.InstallerAbstract;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;

public abstract class AuthenticationManagerStandardInstallerAbstract extends InstallerAbstract implements AuthenticationManagerInstaller {

    public AuthenticationManagerStandardInstallerAbstract(final String name) {
        super(name);
    }

    @Override
    public final AuthenticationManager createAuthenticationManager() {
        final AuthenticationManagerStandard authenticationManager = createAuthenticationManagerStandard();
        for (final Authenticator authenticator : createAuthenticators()) {
            authenticationManager.addAuthenticator(authenticator);
        }
        return authenticationManager;
    }

    protected AuthenticationManagerStandard createAuthenticationManagerStandard() {
        return new AuthenticationManagerStandard();
    }

    /**
     * Hook method
     *
     * @return
     */
    protected abstract List<Authenticator> createAuthenticators();

    @Override
    public List<Class<?>> getTypes() {
        return listOf(AuthenticationManager.class);
    }

}
