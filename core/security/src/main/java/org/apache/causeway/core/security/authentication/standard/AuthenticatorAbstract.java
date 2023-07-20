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
package org.apache.causeway.core.security.authentication.standard;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.core.security.authentication.AuthenticationRequest;
import org.apache.causeway.core.security.authentication.Authenticator;

import lombok.val;

public abstract class AuthenticatorAbstract implements Authenticator {

    /**
     * Default implementation returns a validated {@link InteractionContext}; can be overridden
     * if required.
     */
    @Override
    public final InteractionContext authenticate(
            final AuthenticationRequest request,
            final String validationCode) {

        if (!isValid(request)) {
            return null;
        }

        val user = UserMemento.ofNameAndRoleNames(request.getName(), request.streamRoles())
                .withAuthenticationCode(validationCode);
        return InteractionContext.ofUserWithSystemDefaults(user);
    }


    /**
     * Whether this {@link Authenticator} is valid/applicable in the running context (and
     * optionally with respect to the provided {@link AuthenticationRequest}).
     */
    protected abstract boolean isValid(AuthenticationRequest request);

    @Override
    public final void logout() {
        // no-op
    }

}
