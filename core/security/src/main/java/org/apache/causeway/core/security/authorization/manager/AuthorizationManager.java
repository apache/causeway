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
package org.apache.causeway.core.security.authorization.manager;

import java.util.List;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.security.authorization.Authorizor;

/**
 * Authorizes the user in the current session view and use members of an object.
 *
 * @since 1.x {@index}
 */
@Service
@Named("causeway.security.AuthorizationManager")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class AuthorizationManager {

    private final Authorizor authorizor;

    @Inject
    public AuthorizationManager(
            final List<Authorizor> authorizors,
            final Optional<AuthorizorChooser> authorizorChooserIfAny) {

        _Assert.assertTrue(_NullSafe.size(authorizors)>0, ()->
            String.format(
                    "At least one %s is required to be registered for injection.",
                    Authorizor.class.getName()));

        this.authorizor =
            authorizorChooserIfAny
            .map(authorizorChooser->authorizorChooser.chooseFrom(authorizors))
            .orElseGet(()->authorizors.get(0));
    }

    /**
     * Whether the user represented by the specified session is authorized to view the member of the class/object
     * represented by the member identifier.
     *
     * <p>
     * Normally the view of the specified field, or the display of the action will be suppress if this returns false.
     * </p>
     */
    public boolean isUsable(
            final InteractionContext authentication,
            final Identifier identifier) {
        if (isPerspectiveMember(identifier)) {
            return true;
        }
        if(containsSudoSuperuserRole(authentication)) {
            return true;
        }
        if (authorizor.isUsable(authentication, identifier)) {
            return true;
        }
        return false;
    }

    /**
     * Whether the user represented by the specified session is authorized to change the field represented by the
     * member identifier.
     *
     * <p>
     * Normally the specified field will be not appear editable if this returns false.
     * </p>
     */
    public boolean isVisible(
            final InteractionContext authentication,
            final Identifier identifier) {
        if (isPerspectiveMember(identifier)) {
            return true;
        }
        // no-op if is visibility context check at object-level
        if (identifier.getMemberLogicalName().equals("")) {
            return true;
        }
        if(containsSudoSuperuserRole(authentication)) {
            return true;
        }
        if (authorizor.isVisible(authentication, identifier)) {
            return true;
        }
        return false;
    }

    // -- HELPER

    private static boolean containsSudoSuperuserRole(
            final @Nullable InteractionContext session) {
        if(session==null || session.getUser()==null) {
            return false;
        }
        return session.getUser().hasRoleName(SudoService.ACCESS_ALL_ROLE.getName());
    }

    private boolean isPerspectiveMember(final Identifier identifier) {
        return (identifier.getClassName().equals(""));
    }

}
