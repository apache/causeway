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

import java.util.Map;
import com.google.common.collect.Maps;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.util.ObjectContracts;

public abstract class AbstractDomainEvent<S> extends java.util.EventObject {

    private static final long serialVersionUID = 1L;

    public AbstractDomainEvent(
            final S source,
            final Identifier identifier) {
        super(source);
        this.identifier = identifier;
    }

    //region > Phase

    public enum Phase {
        HIDE,
        DISABLE,
        VALIDATE,
        EXECUTING,
        EXECUTED;

        /**
         * The significance being that at this point the proposed values/arguments are known, and so the event can be
         * fully populated.
         */
        public boolean isValidatingOrLater() {
            return this == VALIDATE || isExecutingOrLater();
        }

        /**
         * When the {@link org.apache.isis.applib.services.command.Command} is made available on the {@link org.apache.isis.applib.services.eventbus.ActionInteractionEvent}
         * via {@link org.apache.isis.applib.services.eventbus.ActionInteractionEvent#getCommand()}.
         */
        public boolean isExecutingOrLater() {
            return this == EXECUTING || this == EXECUTED;
        }
    }

    private Phase phase;

    /**
     * Whether the framework is checking visibility, enablement, validity or actually executing (invoking action,
     * updating property or collection).
     */
    public Phase getEventPhase() {
        return phase;
    }

    /**
     * Not API, set by the framework.
     */
    public void setEventPhase(Phase phase) {
        this.phase = phase;
    }
    //endregion

    //region > source (downcast to S)
    @Override
    @SuppressWarnings("unchecked")
    public S getSource() {
        return (S)source;
    }
    //endregion

    //region > identifier
    private final Identifier identifier;
    public Identifier getIdentifier() {
        return identifier;
    }
    //endregion

    //region > hide, isHidden
    private boolean hidden;
    public boolean isHidden() {
        return hidden;
    }

    public void hide() {
        this.hidden = true;
    }
    //endregion

    //region > disable, isDisabled, getDisabledReason
    private String disabledReason;
    public boolean isDisabled() {
        return disabledReason != null;
    }
    public String getDisabledReason() {
        return disabledReason;
    }
    public void disable(final String reason) {
        this.disabledReason = reason;
    }
    //endregion

    //region > invalidate, isInvalid, getInvalidityReason
    private String invalidatedReason;
    public boolean isInvalid() {
        return invalidatedReason != null;
    }
    public String getInvalidityReason() {
        return invalidatedReason;
    }
    public void invalidate(final String reason) {
        this.invalidatedReason = reason;
    }
    //endregion

    //region > veto
    /**
     * Use instead of {@link #hide()}, {@link #disable(String)} and {@link #invalidate(String)}; just delegates to
     * appropriate vetoing method based upon the {@link #getPhase()}.
     *
     * <p>
     *     If hiding, just pass <tt>null</tt> for the parameter.
     * </p>
     *
     * @param reason - reason why the interaction is being invalidated (ignored if in {@link org.apache.isis.applib.services.eventbus.AbstractInteractionEvent.Phase#HIDE hide} phase).
     * @param args
     */
    @Programmatic
    public void veto(final String reason, final Object... args) {
        switch (getEventPhase()) {
            case HIDE:
                hide();
            case DISABLE:
                disable(String.format(reason, args));
            case VALIDATE:
                invalidate(String.format(reason, args));
        }
    }
    //endregion

    //region > userData
    /**
     * Provides a mechanism to pass data to the next {@link #getPhase() phase}.
     */
    private final Map<Object, Object> userData = Maps.newHashMap();

    /**
     * Obtain user-data, as set by a previous {@link #getPhase() phase}.
     */
    public Object get(Object key) {
        return userData.get(key);
    }
    /**
     * Set user-data, for the use of a subsequent {@link #getPhase() phase}.
     */
    public void put(Object key, Object value) {
        userData.put(key, value);
    }
    //endregion

    //region > toString
    @Override
    public String toString() {
        return ObjectContracts.toString(this, "source,identifier,mode");
    }
    //endregion
}