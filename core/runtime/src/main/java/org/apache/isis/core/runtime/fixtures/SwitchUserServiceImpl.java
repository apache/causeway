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

package org.apache.isis.core.runtime.fixtures;

import java.util.List;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.applib.fixtures.switchuser.SwitchUserService;
import org.apache.isis.applib.fixtures.switchuser.SwitchUserServiceAware;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.fixtures.authentication.AuthenticationRequestLogonFixture;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
)
public class SwitchUserServiceImpl implements SwitchUserService {

    @Programmatic
    @Override
    public void switchUser(final String username, final List<String> roles) {
        switchUser(new LogonFixture(username, roles));
    }

    @Programmatic
    @Override
    public void switchUser(final String username, final String... roles) {
        switchUser(new LogonFixture(username, roles));
    }

    private void switchUser(final LogonFixture logonFixture) {
        reopenSession(new AuthenticationRequestLogonFixture(logonFixture));
    }

    private void reopenSession(final AuthenticationRequest authRequest) {
        persistenceSessionServiceInternal.commit();
        isisSessionFactory.closeSession();

        final AuthenticationSession authenticationSession = authenticationManager.authenticate(authRequest);

        isisSessionFactory.openSession(authenticationSession);
        persistenceSessionServiceInternal.beginTran();
    }


    @Deprecated
    @Programmatic
    public void injectInto(final Object fixture) {
        if (fixture instanceof SwitchUserServiceAware) {
            final SwitchUserServiceAware serviceAware = (SwitchUserServiceAware) fixture;
            serviceAware.setService(this);
        }
    }

    @javax.inject.Inject
    AuthenticationManager authenticationManager;

    @javax.inject.Inject
    IsisSessionFactory isisSessionFactory;

    @javax.inject.Inject
    PersistenceSessionServiceInternal persistenceSessionServiceInternal;

}
