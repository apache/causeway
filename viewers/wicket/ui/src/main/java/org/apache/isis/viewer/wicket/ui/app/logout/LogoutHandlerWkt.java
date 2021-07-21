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
package org.apache.isis.viewer.wicket.ui.app.logout;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.isis.core.interaction.session.IsisInteraction;
import org.apache.isis.core.security.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor
public class LogoutHandlerWkt implements LogoutHandler {

    final InteractionLayerTracker interactionLayerTracker;

    @Override
    public void logout() {

        val currentWktSession = AuthenticatedWebSession.get();
        if(currentWktSession==null) {
            return;
        }

        if(interactionLayerTracker.isInInteraction()) {
            interactionLayerTracker.currentInteraction()
            .map(IsisInteraction.class::cast)
            .ifPresent(interaction->
                interaction.setOnClose(currentWktSession::invalidateNow));

        } else {
            currentWktSession.invalidateNow();
        }
    }

    @Override
    public boolean isHandlingCurrentThread() {
        return RequestCycle.get()!=null;
    }

}
