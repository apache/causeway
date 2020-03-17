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

package org.apache.isis.applib.services.wrapper;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;

public abstract class InteractionException extends RecoverableException {

    private static final long serialVersionUID = 1L;

    private final InteractionEvent interactionEvent;

    public InteractionException(final InteractionEvent interactionEvent) {
        super(interactionEvent.getReason());
        this.interactionEvent = interactionEvent;
    }

    /**
     * The {@link InteractionEvent event} passed into the
     * {@link #InteractionException(InteractionEvent) constructor}.
     *
     * <p>
     * Not part of the API, but made available so that subclasses can expose as
     * the appropriate subtype of {@link InteractionEvent}. This would have been
     * more obvious to see if {@link InteractionException} was generic, but
     * generic subclasses of {@link Throwable} are (apparently) not allowed.
     *
     * @return
     */
    protected InteractionEvent getInteractionEvent() {
        return interactionEvent;
    }

    /**
     * Convenience method that returns the
     * {@link InteractionEvent#getAdvisorClass() advisor class} of the wrapped
     * {@link #getInteractionEvent() interaction event}.
     *
     * @return
     */
    public Class<?> getAdvisorClass() {
        return interactionEvent.getAdvisorClass();
    }

    /**
     * Convenience method that returns the
     * {@link InteractionEvent#getIdentifier() identifier} of the wrapped
     * {@link #getInteractionEvent() interaction event}.
     *
     * @return
     */
    public Identifier getIdentifier() {
        return interactionEvent.getIdentifier();
    }

}
