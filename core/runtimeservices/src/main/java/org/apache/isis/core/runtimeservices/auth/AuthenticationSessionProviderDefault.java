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
package org.apache.isis.core.runtimeservices.auth;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.metamodel.services.user.UserServiceDefault;
import org.apache.isis.core.runtime.session.IsisSession;
import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.security.authentication.standard.SimpleSession;

import lombok.val;

@Service
@Named("isisRuntimeServices.AuthenticationSessionProviderDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
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

        // if user/role has been overridden by SudoService, then honor that value.
        val userAndRoleOverrides = userServiceDefault.currentOverridesIfAny();

        if(userAndRoleOverrides != null) {
            val user = userAndRoleOverrides.getUser();
            val roles = userAndRoleOverrides.getRoles();
            return new SimpleSession(user, roles);
        }

        // otherwise...
        return IsisSession.current()
                .map(IsisSession::getAuthenticationSession)
                .orElse(null);
    }
    

}
