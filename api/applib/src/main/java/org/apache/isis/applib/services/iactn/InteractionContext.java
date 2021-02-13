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
package org.apache.isis.applib.services.iactn;

import java.util.Optional;

import org.apache.isis.commons.internal.exceptions._Exceptions;

/**
 * Provides the current thread's {@link Interaction}.
 *
 * <p>
 * An {@link Interaction}  contains a top-level {@link Execution}
 * representing the invocation of an action or the editing of a property.
 * If that top-level action or property uses the
 * {@link org.apache.isis.applib.services.wrapper.WrapperFactory} domain
 * service to invoke child actions/properties, then those sub-executions are
 * captured as a call-graph. The {@link Execution} is thus a
 * graph structure.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface InteractionContext {


    /**
     * Optionally, the currently active {@link Interaction} for the calling thread.
     */
    Optional<Interaction> currentInteraction();

    // -- SHORTCUTS

    default Interaction currentInteractionElseFail() {
    	return currentInteraction().orElseThrow(()->_Exceptions
    			.illegalState("No InteractionSession on current thread"));
    }


}
