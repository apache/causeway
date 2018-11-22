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

import java.util.EventObject;
import java.util.Map;
import com.google.common.collect.Maps;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.util.ObjectContracts;

public abstract class AbstractDomainEvent<S> extends java.util.EventObject {

    private static final long serialVersionUID = 1L;

    /**
     * If used then the framework will set state via (non-API) setters.
     *
     * <p>
     *     Because the {@link EventObject} superclass prohibits a null source, a dummy value is temporarily used.
     * </p>
     */
    public AbstractDomainEvent() {
        this(null, null);
    }

    public AbstractDomainEvent(
            final S source,
            final Identifier identifier) {
        super(sourceElseDummy(source));
        this.identifier = identifier;
    }

    private static Object sourceElseDummy(final Object source) {
        return source != null ? source : new Object();
    }


    // region > mixedIn
    private Object mixedIn;

    /**
     * Populated only for mixins; holds the underlying domain object that the mixin contributes to.
     */
    public Object getMixedIn() {
        return mixedIn;
    }
    /**
     * Not API - set by the framework.
     */
    public void setMixedIn(final Object mixedIn) {
        this.mixedIn = mixedIn;
    }
    // endregion



    // region > subject

    /**
     * The subject of the event, which will be either the {@link #getSource() source} for a regular action, or the
     * {@link #getMixedIn() mixed-in} domain object for a mixin.
     */
    public Object getSubject() {
        final Object mixedIn = getMixedIn();
        return mixedIn != null ? mixedIn : getSource();
    }


    //endregion


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
            return isExecuting() || isExecuted();
        }

        public boolean isExecuting() {
            return this == EXECUTING;
        }

        public boolean isExecuted() {
            return this == EXECUTED;
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

    /**
     * Not API, set by the framework if the no-arg constructor is used.
     */
    public void setSource(S source) {
        this.source = source;
    }
    //endregion

    //region > identifier
    /**
     * If the no-arg constructor is used, then the framework will populate this field reflectively.
     */
    private Identifier identifier;
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Not API, set by the framework if the no-arg constructor is used.
     */
    public void setIdentifier(final Identifier identifier) {
        this.identifier = identifier;
    }
    //endregion

    //region > hide, isHidden
    private boolean hidden;
    public boolean isHidden() {
        return hidden;
    }

    /**
     * @see #veto(String, Object...)
     */
    public void hide() {
        this.hidden = true;
    }
    //endregion

    //region > disable, isDisabled, getDisabledReason, getDisabledReasonTranslatable
    private String disabledReason;

    public boolean isDisabled() {
        return disabledReason != null || disabledReasonTranslatable != null;
    }

    /**
     * If {@link #isDisabled() disabled}, then either this method returns non-null or {@link #getDisabledReasonTranslatable()} will.
     */
    public String getDisabledReason() {
        return disabledReason;
    }

    /**
     * @see #disable(org.apache.isis.applib.services.i18n.TranslatableString)
     * @see #veto(String, Object...)
     */
    public void disable(final String reason) {
        this.disabledReason = reason;
    }

    private TranslatableString disabledReasonTranslatable;
    /**
     * If {@link #isDisabled() disabled}, then either this method returns non-null or {@link #getDisabledReason()} will.
     */
    public TranslatableString getDisabledReasonTranslatable() {
        return disabledReasonTranslatable;
    }
    /**
     * @see #disable(java.lang.String)
     * @see #veto(org.apache.isis.applib.services.i18n.TranslatableString)
     */
    public void disable(final TranslatableString reason) {
        this.disabledReasonTranslatable = reason;
    }
    //endregion

    //region > invalidate, isInvalid, getInvalidityReason, getInvalidityReasonTranslatable
    private String invalidatedReason;
    public boolean isInvalid() {
        return invalidatedReason != null || invalidatedReasonTranslatable != null;
    }

    /**
     * If {@link #isInvalid() invalid}, then either this method returns non-null or {@link #getInvalidityReasonTranslatable()} will.
     */
    public String getInvalidityReason() {
        return invalidatedReason;
    }
    /**
     * @see #invalidate(org.apache.isis.applib.services.i18n.TranslatableString)
     * @see #veto(String, Object...)
     */
    public void invalidate(final String reason) {
        this.invalidatedReason = reason;
    }

    private TranslatableString invalidatedReasonTranslatable;
    /**
     * If {@link #isInvalid() invalid}, then either this method returns non-null or {@link #getInvalidityReason()} will.
     */
    public TranslatableString getInvalidityReasonTranslatable() {
        return invalidatedReasonTranslatable;
    }

    /**
     * @see #invalidate(String)
     * @see #veto(org.apache.isis.applib.services.i18n.TranslatableString)
     */
    public void invalidate(final TranslatableString reason) {
        this.invalidatedReasonTranslatable = reason;
    }

    //endregion

    //region > veto
    /**
     * Use instead of {@link #hide()}, {@link #disable(String)} and {@link #invalidate(String)}; just delegates to
     * appropriate vetoing method based upon the {@link #getEventPhase() phase}.
     *
     * <p>
     *     If hiding, just pass <tt>null</tt> for the parameter.
     * </p>
     *
     * @param reason - reason why the interaction is being invalidated (ignored if in {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#HIDE hide} phase).
     * @param args
     *
     * @see #veto(org.apache.isis.applib.services.i18n.TranslatableString)
     */
    @Programmatic
    public void veto(final String reason, final Object... args) {
        switch (getEventPhase()) {
            case HIDE:
                hide();
                break;
            case DISABLE:
                if(reason == null) {
                    throw new IllegalArgumentException("Reason must be non-null");
                }
                disable(String.format(reason, args));
                break;
            case VALIDATE:
                if(reason == null) {
                    throw new IllegalArgumentException("Reason must be non-null");
                }
                invalidate(String.format(reason, args));
                break;
        }
    }
    /**
     * Use instead of {@link #hide()}, {@link #disable(org.apache.isis.applib.services.i18n.TranslatableString)} and {@link #invalidate(org.apache.isis.applib.services.i18n.TranslatableString)}; just delegates to
     * appropriate vetoing method based upon the {@link #getEventPhase() phase}.
     *
     * <p>
     *     If hiding, just pass <tt>null</tt> for the parameter.
     * </p>
     *
     * @param translatableReason - reason why the interaction is being invalidated (ignored if in {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#HIDE hide} phase).
     *
     * @see #veto(String, Object...)
     */
    @Programmatic
    public void veto(final TranslatableString translatableReason) {
        switch (getEventPhase()) {
            case HIDE:
                hide();
                break;
            case DISABLE:
                disable(translatableReason);
                break;
            case VALIDATE:
                invalidate(translatableReason);
                break;
        }
    }
    //endregion

    //region > userData
    /**
     * Provides a mechanism to pass data to the next {@link #getEventPhase() phase}.
     */
    private final Map<Object, Object> userData = Maps.newHashMap();

    /**
     * Obtain user-data, as set by a previous {@link #getEventPhase() phase}.
     */
    public Object get(Object key) {
        return userData.get(key);
    }
    /**
     * Set user-data, for the use of a subsequent {@link #getEventPhase() phase}.
     */
    public void put(Object key, Object value) {
        userData.put(key, value);
    }
    //endregion

    //region > toString
    @Override
    public String toString() {
        return ObjectContracts.toString(this, "source","identifier","eventPhase");
    }
    //endregion
}