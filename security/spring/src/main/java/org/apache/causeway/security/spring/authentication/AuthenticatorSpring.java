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
package org.apache.causeway.security.spring.authentication;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.core.security.authentication.AuthenticationRequest;
import org.apache.causeway.core.security.authentication.Authenticator;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("causeway.security.AuthenticatorSpring")
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@Qualifier("Spring")
public class AuthenticatorSpring implements Authenticator {

    @Inject private InteractionProvider interactionProvider;

    @Override
    public final boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
        return true;
    }

    @Override
    public InteractionContext authenticate(final AuthenticationRequest request, final String code) {
        // SpringSecurityFilter should already have taken care of Authentication creation
        return interactionProvider.currentInteractionContext().orElse(null);
    }

    @Override
    public void logout(final InteractionContext session) {
    }

}
