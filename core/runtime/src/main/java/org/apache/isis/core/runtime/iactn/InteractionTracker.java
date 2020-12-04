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
package org.apache.isis.core.runtime.iactn;

import java.util.Optional;

import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionTracker;
import org.apache.isis.core.security.authentication.MessageBroker;

/**
 * 
 * @since 2.0
 *
 */
public interface InteractionTracker 
extends 
	AuthenticationSessionTracker, 
	InteractionContext {

    boolean isInInteractionSession();
    
    /** @return the InteractionClosure that sits on top of the current 
     * request- or test-scoped InteractionSession*/
    Optional<InteractionEnvironment> currentInteractionClosure();
    
    /** @return the current request- or test-scoped InteractionSession*/
    default Optional<InteractionSession> currentInteractionSession() {
    	return currentInteractionClosure().map(InteractionEnvironment::getInteractionSession);
    }
    
    @Override
    default Optional<AuthenticationSession> currentAuthenticationSession() {
        return currentInteractionClosure().map(InteractionEnvironment::getAuthenticationSession);
    }
    
    @Override
    default Optional<MessageBroker> currentMessageBroker() {
        return currentAuthenticationSession().map(AuthenticationSession::getMessageBroker);
    }
    
    /** @return the unique id of the current top-level request- or test-scoped IsisInteraction*/
    public Optional<String> getConversationId();

    // -- INTERACTION CONTEXT
    
    @Override
    default Optional<Interaction> getInteraction(){
    	return currentInteractionSession().map(InteractionSession::getInteraction);
    }
    
}
