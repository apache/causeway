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

package org.apache.isis.core.security.authorization.manager;

import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authorization.standard.Authorizor;

import lombok.NonNull;

/**
 * Authorizes the user in the current session view and use members of an object.
 */
@Service
@Named("isisSecurityApi.AuthorizationManager")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class AuthorizationManager {

    private final Authorizor authorizor;

    @Inject
    public AuthorizationManager(Authorizor authorizor) {
        this.authorizor = authorizor;
    }

    /**
     * Whether the user represented by the specified session is authorized to view the member of the class/object
     * represented by the member identifier.
     *
     * <p>
     * Normally the view of the specified field, or the display of the action will be suppress if this returns false.
     * </p>
     */
    public boolean isUsable(final AuthenticationSession session, final Identifier identifier) {
        if (isPerspectiveMember(identifier)) {
            return true;
        }
        if(containsSudoSuperuserRole(session)) {
            return true;
        }
        if (authorizor.isUsableInAnyRole(identifier)) {
            return true;
        }
        return anyMatchOnRoles(session, roleName->authorizor.isUsableInRole(roleName, identifier));
    }

    /**
     * Whether the user represented by the specified session is authorized to change the field represented by the
     * member identifier.
     *
     * <p>
     * Normally the specified field will be not appear editable if this returns false.
     * </p>
     */
    public boolean isVisible(final AuthenticationSession session, final Identifier identifier) {
        if (isPerspectiveMember(identifier)) {
            return true;
        }
        // no-op if is visibility context check at object-level
        if (identifier.getMemberName().equals("")) {
            return true;
        }
        if(containsSudoSuperuserRole(session)) {
            return true;
        }
        if (authorizor.isVisibleInAnyRole(identifier)) {
            return true;
        }
        return anyMatchOnRoles(session, roleName->authorizor.isVisibleInRole(roleName, identifier));
    }

    // -- HELPER
    
    private static boolean containsSudoSuperuserRole(
            final @Nullable AuthenticationSession session) {
        if(session==null || session.getUser()==null) {
            return false;
        }
        return session.getUser().hasRoleName(SudoService.ACCESS_ALL_ROLE.getName());
    }
    
    private boolean anyMatchOnRoles(
            final @Nullable AuthenticationSession session, 
            final @NonNull Predicate<String> predicate) {
        if(session==null || session.getUser()==null) {
            return false;
        }
        return session.getUser().streamRoleNames()
                .anyMatch(predicate);
    }
    

    private boolean isPerspectiveMember(final Identifier identifier) {
        return (identifier.getClassName().equals(""));
    }

}
