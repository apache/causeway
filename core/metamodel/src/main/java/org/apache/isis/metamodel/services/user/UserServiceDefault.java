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

package org.apache.isis.metamodel.services.user;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.security.RoleMemento;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.security.api.authentication.AuthenticationSession;
import org.apache.isis.security.api.authentication.AuthenticationSessionProvider;

@Service
@Named("isisMetaModel.userServiceDefault")
@Log4j2
public class UserServiceDefault implements UserService {

    @Override
    public UserMemento getUser() {

        final UserAndRoleOverrides userAndRoleOverrides = currentOverridesIfAny();

        if (userAndRoleOverrides != null) {

            final String username = userAndRoleOverrides.user;

            final List<String> roles;
            if (userAndRoleOverrides.roles != null) {
                roles = userAndRoleOverrides.roles;
            } else {
                // preserve the roles if were not overridden
                roles = previousRoles();
            }

            final List<RoleMemento> roleMementos = asRoleMementos(roles);
            return new UserMemento(username, roleMementos);

        } else {
            final AuthenticationSession session =
                    authenticationSessionProvider.getAuthenticationSession();
            return session.createUserMemento();
        }
    }

    private List<String> previousRoles() {
        final List<String> roles;

        final AuthenticationSession session =
                authenticationSessionProvider.getAuthenticationSession();
        roles = session.getRoles();
        return roles;
    }

    public static class UserAndRoleOverrides {
        final String user;
        final List<String> roles;


        UserAndRoleOverrides(final String user) {
            this(user, null);
        }

        UserAndRoleOverrides(final String user, final List<String> roles) {
            this.user = user;
            this.roles = roles;
        }

        public String getUser() {
            return user;
        }

        public List<String> getRoles() {
            return roles;
        }
    }

    private final ThreadLocal<Stack<UserAndRoleOverrides>> overrides =
            new ThreadLocal<Stack<UserAndRoleOverrides>>() {
        @Override protected Stack<UserAndRoleOverrides> initialValue() {
            return new Stack<>();
        }
    };


    private void overrideUserAndRoles(final String user, final List<String> rolesIfAny) {
        final List<String> roles = rolesIfAny != null ? rolesIfAny : inheritRoles();
        this.overrides.get().push(new UserAndRoleOverrides(user, roles));
    }

    private void resetOverrides() {
        this.overrides.get().pop();
    }

    /**
     * Not API; for use by the implementation of sudo/runAs (see {@link SudoService} etc.
     */
    public UserAndRoleOverrides currentOverridesIfAny() {
        final Stack<UserAndRoleOverrides> userAndRoleOverrides = overrides.get();
        return !userAndRoleOverrides.empty()
                ? userAndRoleOverrides.peek()
                        : null;
    }

    private List<String> inheritRoles() {
        final UserAndRoleOverrides currentOverridesIfAny = currentOverridesIfAny();
        return currentOverridesIfAny != null
                ? currentOverridesIfAny.getRoles()
                        : authenticationSessionProvider.getAuthenticationSession().getRoles();
    }

    private static List<RoleMemento> asRoleMementos(final List<String> roles) {
        final List<RoleMemento> mementos = new ArrayList<RoleMemento>();
        if (roles != null) {
            for (final String role : roles) {
                mementos.add(new RoleMemento(role));
            }
        }
        return mementos;
    }


    @Service
    @Named("isisMetaModel.userServiceDefault.SudoServiceSpi")
    @Log4j2
    public static class SudoServiceSpi implements SudoService.Spi {

        @Override
        public void runAs(final String username, final List<String> roles) {
            userServiceDefault.overrideUserAndRoles(username, roles);
        }

        @Override
        public void releaseRunAs() {
            userServiceDefault.resetOverrides();
        }

        @Inject
        UserServiceDefault userServiceDefault;
    }

    @Inject
    AuthenticationSessionProvider authenticationSessionProvider;

}
