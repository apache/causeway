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
package org.apache.causeway.viewer.wicket.viewer.integration;

import java.util.Optional;

import org.apache.causeway.applib.services.iactn.InteractionContext;
import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
record SessionAuthenticator(
        InteractionService interactionService,
        UserService userService) {

    public Optional<InteractionContext> determineInteractionContext() {
        // participate if an InteractionContext was already provided through some other mechanism,
        // but fail early if the current user is impersonating
        // (seeing this if going back the browser history into a page, that was previously impersonated)
        var session = AuthenticatedWebSessionForCauseway.get();

        interactionService.currentInteractionContext()
            .ifPresent(ic->{
                if(ic.getUser().isImpersonating())
                    throw _Exceptions.illegalState("cannot enter a new request cycle with a left over impersonating user");
                session.setPrimedInteractionContext(ic);
            });

        var interactionContext = session.getInteractionContext();
        if (interactionContext == null) {
            log.warn("session was not opened (because not authenticated)");
            return Optional.empty();
        }

        // impersonation support
        return Optional.of(
                userService
                    .lookupImpersonatedUser()
                    .map(interactionContext::withUser)
                    .orElse(interactionContext));
    }

}
