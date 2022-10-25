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
package org.apache.causeway.applib.services.iactnlayer;

import java.util.Optional;

import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

/**
 * Provides access to the current {@link InteractionLayer}.
 *
 * <p>
 *     The bottom-most interaction &quot;layer&quot; can be thought of as a short-lived session with the database.
 *     A new layer can be started, for example using the {@link org.apache.causeway.applib.services.sudo.SudoService},
 *     or the {@link InteractionService}.  These allow the user (or the clock or other environmental aspects) to be
 *     temporarily altered - similar to a &quot;su&quot; command in Unix.
 * </p>
 *
 * @see Interaction
 * @see InteractionLayer
 * @see InteractionService
 * @see InteractionProvider
 * @see org.apache.causeway.applib.services.sudo.SudoService
 * @see org.apache.causeway.applib.services.clock.ClockService
 *
 * @since 2.0 {@index}
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
