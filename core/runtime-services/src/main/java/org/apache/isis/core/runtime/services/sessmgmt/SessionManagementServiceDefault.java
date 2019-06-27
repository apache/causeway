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

package org.apache.isis.core.runtime.services.sessmgmt;

import javax.inject.Singleton;

import org.apache.isis.applib.services.sessmgmt.SessionManagementService;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.security.authentication.AuthenticationSession;

@Singleton
public class SessionManagementServiceDefault implements SessionManagementService {

    @Override
    public void nextSession() {

        final AuthenticationSession authenticationSession =
                isisSessionFactory.getCurrentSession().getAuthenticationSession();

        persistenceSessionServiceInternal.commit();
        isisSessionFactory.closeSession();

        isisSessionFactory.openSession(authenticationSession);
        persistenceSessionServiceInternal.beginTran();
    }


    @javax.inject.Inject
    IsisSessionFactory isisSessionFactory;
    @javax.inject.Inject
    PersistenceSessionServiceInternal persistenceSessionServiceInternal;

}
