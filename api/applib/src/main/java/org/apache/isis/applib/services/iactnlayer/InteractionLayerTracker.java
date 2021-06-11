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
package org.apache.isis.applib.services.iactnlayer;

import java.util.Optional;

import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.commons.internal.exceptions._Exceptions;

/**
 *
 * @since 2.0
 */
public interface InteractionLayerTracker
extends InteractionProvider {

    /** @return the AuthenticationLayer that sits on top of the current
     * request- or test-scoped InteractionSession's stack*/
    Optional<InteractionLayer> currentInteractionLayer();

    default InteractionLayer currentInteractionLayerElseFail() {
        return currentInteractionLayer()
                .orElseThrow(()->_Exceptions
                        .illegalState("No InteractionLayer available on current thread"));
    }

    // -- INTERACTION CONTEXT

    /**
     * Returns the {@link InteractionContext} wrapped by the {@link #currentInteractionLayer()}
     * (if within an {@link InteractionLayer}).
     */
    @Override
    default Optional<InteractionContext> currentInteractionContext() {
        return currentInteractionLayer()
                .map(InteractionLayer::getInteractionContext);
    }


    // -- INTERACTION

    /**
     * Returns the {@link Interaction} wrapped by the {@link #currentInteractionLayer()}
     * (if within an {@link InteractionLayer}).
     */
    @Override
    default Optional<Interaction> currentInteraction(){
    	return currentInteractionLayer()
    	        .map(InteractionLayer::getInteraction);
    }

}
