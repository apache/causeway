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
 */
// tag::refguide[]
public interface InteractionContext {

    // end::refguide[]

    /**
     * Optionally, the currently active {@link Interaction} for the calling thread.
     */
    // tag::refguide[]
    Optional<Interaction> getInteraction();    // <.>
    // end::refguide[]

    // -- SHORTCUTS
    
    default Interaction getInteractionIfAny() {
    	return getInteraction().orElse(null);
    }
    
    default Interaction getInteractionElseFail() {
    	return getInteraction().orElseThrow(()->_Exceptions
    			.unrecoverable("needs an InteractionSession on current thread"));
    }
    
    // tag::refguide[]

}
// end::refguide[]
