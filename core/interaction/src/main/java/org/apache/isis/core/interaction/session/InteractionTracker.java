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
package org.apache.isis.core.interaction.session;

import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import org.apache.isis.applib.services.iactn.ExecutionContext;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.security.authentication.Authentication;
import org.apache.isis.core.security.authentication.AuthenticationContext;

import lombok.NonNull;
import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
public interface InteractionTracker 
extends InteractionContext, AuthenticationContext {

    boolean isInInteractionSession();
    
    /** @return the AuthenticationLayer that sits on top of the current 
     * request- or test-scoped InteractionSession's stack*/
    Optional<AuthenticationLayer> currentAuthenticationLayer();
    
    default AuthenticationLayer currentAuthenticationLayerElseFail() {
        return currentAuthenticationLayer()
        .orElseThrow(()->_Exceptions.illegalState("No InteractionSession available on current thread"));
    }
    
    /** @return the current request- or test-scoped InteractionSession*/
    default Optional<InteractionSession> currentInteractionSession() {
    	return currentAuthenticationLayer().map(AuthenticationLayer::getInteractionSession);
    }
    
    default Optional<ExecutionContext> currentExecutionContext() {
        return currentAuthenticationLayer().map(AuthenticationLayer::getExecutionContext);
    }
    
    /** @return the unique id of the current top-level request- or test-scoped Interaction*/
    Optional<UUID> getConversationId();
    
    /** @return authentication-layer-stack size */
    int getAuthenticationLayerCount();

    // -- MESSAGE BROKER

    default Optional<MessageBroker> currentMessageBroker() {
        return currentInteractionSession().map(InteractionSession::getMessageBroker);
    }
    
    default Optional<MessageBroker> currentMessageBroker(@NonNull Logger logWarnIfMissing) {
        val currentMessageBroker = currentMessageBroker();
        if(!currentMessageBroker.isPresent()) {
            logWarnIfMissing.warn(
                    "No InteractionSession available on current thread, "
                            + "such that cannot locate a MessageBroker");
        }
        return currentMessageBroker;
    }
    
    default MessageBroker currentMessageBrokerElseFail() {
        return currentMessageBroker()
        .orElseThrow(()->_Exceptions.illegalState(
                "No InteractionSession available on current thread, "
                        + "such that cannot locate a MessageBroker"));
    }
    
    // -- AUTHENTICATION CONTEXT
    
    @Override
    default Optional<Authentication> currentAuthentication() {
        return currentAuthenticationLayer().map(AuthenticationLayer::getAuthentication);
    }
    
    // -- INTERACTION CONTEXT
    
    @Override
    default Optional<Interaction> currentInteraction(){
    	return currentInteractionSession().map(InteractionSession::getInteraction);
    }

    
    
}
