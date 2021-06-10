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
package org.apache.isis.core.interaction.integration;

import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.user.ImpersonatedUserHolder;
import org.apache.isis.applib.services.user.UserMemento;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 *
 * @since 2.0
 */
@RequiredArgsConstructor(staticName = "next")
public class IsisRequestCycle {

    private final InteractionService interactionService;
    private final ImpersonatedUserHolder impersonatedUserHolder;

    // -- SUPPORTING WEB REQUEST CYCLE FOR ISIS ...

    public void onBeginRequest(final InteractionContext authenticatedContext) {

        val contextToUse = impersonatedUserHolder.getUserMemento()
                .map(impersonatingUserMemento->
                    InteractionContext
                        .ofUserWithSystemDefaults(
                                merge(
                                        authenticatedContext.getUser(),
                                        impersonatingUserMemento)))
                .orElse(authenticatedContext);

        interactionService.openInteraction(contextToUse);
    }

    public void onRequestHandlerExecuted() {

    }

    public void onEndRequest() {
        interactionService.closeInteractionLayers();
    }

    // -- HELPER

    // not sure if this is strictly required; idea is to preserve some state from the origin user
    private static UserMemento merge(UserMemento origin, UserMemento fake) {
        return fake
                .withAuthenticationSource(origin.getAuthenticationSource())
                .withAuthenticationCode(origin.getAuthenticationCode());
    }


}
