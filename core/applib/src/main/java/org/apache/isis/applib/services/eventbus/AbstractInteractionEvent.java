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
package org.apache.isis.applib.services.eventbus;

import org.apache.isis.applib.Identifier;

/**
 * @deprecated - replaced by {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent}.
 * @param <S>
 */
@Deprecated
public abstract class AbstractInteractionEvent<S> extends AbstractDomainEvent<S> {

    public AbstractInteractionEvent(
            final S source,
            final Identifier identifier) {
        super(source, identifier);
    }


    //region > Phase

    /**
     * @deprecated - replaced by {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase}.
     */
    @Deprecated
    public enum Phase {
        @Deprecated
        HIDE,
        @Deprecated
        DISABLE,
        @Deprecated
        VALIDATE,
        @Deprecated
        EXECUTING,
        @Deprecated
        EXECUTED;

        @Deprecated
        public static Phase from(final AbstractDomainEvent.Phase phase) {
            switch (phase) {
                case HIDE:
                    return AbstractInteractionEvent.Phase.HIDE;
                case DISABLE:
                    return AbstractInteractionEvent.Phase.DISABLE;
                case VALIDATE:
                    return AbstractInteractionEvent.Phase.VALIDATE;
                case EXECUTING:
                    return AbstractInteractionEvent.Phase.EXECUTING;
                case EXECUTED:
                    return AbstractInteractionEvent.Phase.EXECUTED;
            }
            throw new IllegalArgumentException(String.format("Phase '%s' not recognized", phase));
        }

        @Deprecated
        public static AbstractDomainEvent.Phase from(final Phase phase) {
            switch (phase) {
                case HIDE:
                    return AbstractDomainEvent.Phase.HIDE;
                case DISABLE:
                    return AbstractDomainEvent.Phase.DISABLE;
                case VALIDATE:
                    return AbstractDomainEvent.Phase.VALIDATE;
                case EXECUTING:
                    return AbstractDomainEvent.Phase.EXECUTING;
                case EXECUTED:
                    return AbstractDomainEvent.Phase.EXECUTED;
            }
            throw new IllegalArgumentException(String.format("Phase '%s' not recognized", phase));
        }
    }

    private Phase phase;

    /**
     * @deprecated - use {@link #getEventPhase()} instead.
     */
    @Deprecated
    public Phase getPhase() {
        return phase;
    }

    /**
     * Not API, set by the framework.
     */
    @Deprecated
    public void setPhase(Phase phase) {
        this.phase = phase;
    }
    //endregion

}