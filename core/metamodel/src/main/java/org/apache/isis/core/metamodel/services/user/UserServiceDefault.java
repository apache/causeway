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

package org.apache.isis.core.metamodel.services.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.security.RoleMemento;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class UserServiceDefault implements UserService {

    @Programmatic
    @Override
    public UserMemento getUser() {
        final AuthenticationSession session =
                authenticationSessionProvider.getAuthenticationSession();

        final UserAndRoleOverrides userAndRoleOverrides = currentOverridesIfAny();

        final String username = userAndRoleOverrides != null
                ? userAndRoleOverrides.user
                : session.getUserName();
        final List<String> roles = userAndRoleOverrides != null
                ? userAndRoleOverrides.roles != null
                ? userAndRoleOverrides.roles
                : session.getRoles()
                : session.getRoles();
        final List<RoleMemento> roleMementos = asRoleMementos(roles);

        final UserMemento user = new UserMemento(username, roleMementos);
        return user;
    }


    static class UserAndRoleOverrides {
        final String user;
        final List<String> roles;

        UserAndRoleOverrides(final String user) {
            this(user, null);
        }

        UserAndRoleOverrides(final String user, final List<String> roles) {
            this.user = user;
            this.roles = roles;
        }
    }

    private final ThreadLocal<Stack<UserAndRoleOverrides>> overrides =
            new ThreadLocal<Stack<UserAndRoleOverrides>>() {
                @Override protected Stack<UserAndRoleOverrides> initialValue() {
                    return new Stack<>();
                }
            };

    /**
     * Not API; for use by the implementation of {@link SudoService}.
     */
    @Programmatic
    public void overrideUser(final String user) {
        overrideUserAndRoles(user, null);
    }
    /**
     * Not API; for use by the implementation of {@link SudoService}.
     */
    @Programmatic
    public void overrideUserAndRoles(final String user, final List<String> roles) {
        this.overrides.get().push(new UserAndRoleOverrides(user, roles));
    }
    /**
     * Not API; for use by the implementation of {@link SudoService}.
     */
    @Programmatic
    public void resetOverrides() {
        this.overrides.get().pop();
    }


    private UserAndRoleOverrides currentOverridesIfAny() {
        final Stack<UserAndRoleOverrides> userAndRoleOverrides = overrides.get();
        return !userAndRoleOverrides.empty()
                ? userAndRoleOverrides.peek()
                : null;
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


    @javax.inject.Inject
    AuthenticationSessionProvider authenticationSessionProvider;

}
