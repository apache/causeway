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

import org.apache.isis.applib.services.user.ImpersonatedUserHolder;
import org.apache.isis.core.interaction.session.InteractionService;
import org.apache.isis.core.security.authentication.Authentication;
import org.apache.isis.core.security.authentication.standard.SimpleAuthentication;

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

    public void onBeginRequest(final Authentication authentication) {

        val authenticationToUse = impersonatedUserHolder.getUserMemento()
                .<Authentication>map(impersonatingUserMemento->
                    SimpleAuthentication.of(
                        impersonatingUserMemento,
                        authentication.getValidationCode()))
                .orElse(authentication);

        interactionService.openInteraction(authenticationToUse.getInteractionContext());
    }

    public void onRequestHandlerExecuted() {

    }

    public void onEndRequest() {

        interactionService.closeInteractionLayers();

    }


}
