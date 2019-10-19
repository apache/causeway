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
package org.apache.isis.runtime.services.authsess;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.metamodel.services.user.UserServiceDefault;
import org.apache.isis.runtime.system.session.IsisSession;
import org.apache.isis.runtime.system.session.IsisSessionFactory;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authentication.AuthenticationSessionProvider;
import org.apache.isis.security.authentication.standard.SimpleSession;

@Service
public class AuthenticationSessionProviderDefault implements AuthenticationSessionProvider {

    @Inject protected UserServiceDefault userServiceDefault;
    @Inject protected IsisSessionFactory isisSessionFactory;
    
    /**
     * This class and {@link UserServiceDefault} both call each other, so the code below is carefully
     * ordered to ensure no infinite loop.
     *
     * In particular, we check if there are overrides, and if so return a {@link SimpleSession} to represent those
     * overrides.
     */
    @Override
    public AuthenticationSession getAuthenticationSession() {

        // if user/role has been overridden by SudoService, then honour that value.
        final UserServiceDefault.UserAndRoleOverrides userAndRoleOverrides =
                userServiceDefault.currentOverridesIfAny();

        if(userAndRoleOverrides != null) {
            final String user = userAndRoleOverrides.getUser();
            final List<String> roles = userAndRoleOverrides.getRoles();
            return new SimpleSession(user, roles);
        }

        // otherwise...
        return IsisSession.current()
                .map(IsisSession::getAuthenticationSession)
                .orElse(null);
    }
    

}
