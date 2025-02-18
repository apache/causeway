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
package org.apache.causeway.security.shiro.authorization;

import jakarta.inject.Named;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.core.security.authentication.Authenticator;
import org.apache.causeway.core.security.authorization.Authorizor;
import org.apache.causeway.security.shiro.context.ShiroSecurityContext;

/**
 * If Shiro is configured for both authentication and authorization (as recommended), then this class is
 * in the role of {@link Authorizor}.
 *
 * <p>
 * However, although there are two objects, they are set up to share the same
 * {@link SecurityManager Shiro SecurityManager}
 * (bound to a thread-local).
 * </p>
 *
 * @since 1.x {@index}
 */
@Service
@Named("causeway.security.AuthorizorShiro")
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
@Qualifier("Shiro")
public class AuthorizorShiro implements Authorizor {

    @Override
    public boolean isVisible(final InteractionContext authentication, final Identifier identifier) {
        return isPermitted(authentication.getUser().name(), identifier, "r");
    }

    @Override
    public boolean isUsable(final InteractionContext authentication, final Identifier identifier) {
        return isPermitted(authentication.getUser().name(), identifier, "w");
    }

    private boolean isPermitted(final String userName, final Identifier identifier, final String qualifier) {

        RealmSecurityManager securityManager = getSecurityManager();
        if(securityManager == null) {
            // since a security manager will always be present for regular web requests, presumably the user
            // is running in fixtures during bootstrapping.  We therefore permit the interaction.
            return true;
        }

        final Subject subject = SecurityUtils.getSubject();
        final String permission = asPermissionsString(identifier) + ":" + qualifier;

        try {
            //_Assert.assertEquals(userName, subject.getPrincipal().toString()); ... does not work
            return subject.isPermitted(permission);
        } finally {
            CausewayPermission.resetVetoedPermissions();
        }
    }

    private String asPermissionsString(final Identifier identifier) {
        var logicalTypeName = identifier.logicalType().getLogicalTypeNameFormatted(":", ":");
        return logicalTypeName + ":" + identifier.memberLogicalName();
    }

    // -- DEPS

    /**
     * The {@link SecurityManager} is shared between both the {@link Authenticator} and the {@link Authorizor}
     * (if shiro is configured for both components).
     */
    protected RealmSecurityManager getSecurityManager() {
        return ShiroSecurityContext.getSecurityManager();
    }

}
