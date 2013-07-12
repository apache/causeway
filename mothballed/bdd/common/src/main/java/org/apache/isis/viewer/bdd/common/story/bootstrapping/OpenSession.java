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
package org.apache.isis.viewer.bdd.common.story.bootstrapping;

import java.util.List;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.fixtures.authentication.AuthenticationRequestLogonFixture;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.bdd.common.Scenario;

public class OpenSession {

    @SuppressWarnings("unused")
    private final Scenario story;

    public OpenSession(final Scenario story) {
        this.story = story;
    }

    public void openSession(final String userName, final List<String> roles) {
        IsisContext.closeSession();
        final LogonFixture logonFixture = new LogonFixture(userName, roles);
        final AuthenticationRequestLogonFixture authRequest = new AuthenticationRequestLogonFixture(logonFixture);
        final AuthenticationSession authSession = getAuthenticationManager().authenticate(authRequest);

        IsisContext.openSession(authSession);
    }

    protected AuthenticationManager getAuthenticationManager() {
        return IsisContext.getAuthenticationManager();
    }

}
