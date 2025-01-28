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
package org.apache.causeway.applib.events.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.events.EventObjectBase;
import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.applib.util.ObjectContracts;
import org.apache.causeway.applib.util.ToString;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

/**
 * Superclass for all domain events that are raised by the framework when
 * interacting with actions, properties or collections.
 *
 * <p>
 *     The main purpose of the class is to define the protocol by which
 *     subscribers can influence an interaction (eg hide a collection,
 *     disable a property, validate action arguments).
 * </p>
 *
 * <p>
 *     The class also provides a simple mechanism to allow adhoc sharing of
 *     user data between different phases.
 * </p>
 *
 * @see ActionDomainEvent
 * @see PropertyDomainEvent
 * @see CollectionDomainEvent
 *
 * @since 1.x {@index}
 */
public abstract class AbstractDomainEvent<S> extends EventObjectBase<S> {

    /**
     * If used then the framework will set state via (non-API) setters.
     *
     * <p>
     *     Because the {@link EventObjectBase} superclass prohibits a null source,
     *     a dummy value is temporarily used.
     * </p>
     */
    public AbstractDomainEvent() {
        this(null, null);
    }

    /**
     * If used then the framework will set the remaining state via (non-API) setters.
     *
     * <p>
     *     Provided to allow nested non-static implementations, for use in nested non-static mixins.
     * </p>
     */
    public AbstractDomainEvent(final S source) {
        this(source, null);
    }

    public AbstractDomainEvent(
            final S source,
            final Identifier identifier) {
        super(source);
        this.identifier = identifier;
    }

    /**
     * The domain object raising this event.
     *
     * <p>
     * For a "regular" action, property or collection then this will be the
     * target domain object.
     * </p>
     *
     * <p>
     *     But for a "mixin" action, this will be an instance of the mixin
     *     itself.
     * </p>
     */
    @Nullable
    @Override
    public S getSource() {
        return super.getSource();
    }

    /**
     * Populated only for mixins; holds the underlying domain object that the
     * mixin contributes to.
     *
     * <p>
     * For a "regular" action, this will return <code>null</code>.
     * </p>
     */
    @Getter
    private Object mixee;

    /**
     * Not API - set by the framework.
     */
    public final void setMixee(final Object mixee) {
        this.mixee = mixee;
    }

    /**
     * The subject of the event, which will be either the
     * {@link #getSource() source} for a regular action, or the
     * {@link #getMixee() mixed-in} domain object for a mixin.
     */
    public <T> T getSubject() {
        var mixee = getMixee();
        var mixedInElseSource = mixee != null
                ? mixee
                : getSource();
        return _Casts.uncheckedCast(mixedInElseSource);
    }

    /**
     *
     * {@index}
     */
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
         * When the {@link org.apache.causeway.applib.services.command.Command} is made available on the
         * {@link org.apache.causeway.applib.events.domain.ActionDomainEvent} via {@link org.apache.causeway.applib.services.iactn.Interaction#getCommand()}.
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

    /**
     * Whether the framework is checking visibility, enablement, validity or actually executing (invoking action,
     * updating property or collection).
     */
    @Getter
    private Phase eventPhase;

    /**
     * Not API, set by the framework.
     */
    public final void setEventPhase(final Phase phase) {
        this.eventPhase = phase;
    }

    /**
     * Identifier of the action, property or collection being interacted with.
     *
     */
    @Getter
    private Identifier identifier;

    /**
     * Not API, set by the framework if the no-arg constructor is used.
     */
    public final void setIdentifier(final Identifier identifier) {
        this.identifier = identifier;
    }

    // -- HIDING

    /**
     * Used by the framework to determine if the member should be hidden (not
     * rendered).
     */
    @Getter
    private boolean hidden;

    /**
     * API for subscribers to hide the member.
     *
     * @see #veto(String, Object...)
     */
    public final void hide() {
        this.hidden = true;
    }

    /**
     * Same as ... {@code if(condition) hide(); }
     * @see #hide()
     */
    public void hideIf(final boolean condition) {
        if(condition) {
            hide();
        }
    }

    /**
     * Same as ... {@code if(shouldHide.getAsBoolean()) hide(); }
     * @see #hide()
     */
    public void hideIf(final @NonNull BooleanSupplier shouldHide) {
        if(shouldHide.getAsBoolean()) {
            hide();
        }
    }

    // -- DISABLING

    /**
     * If {@link #isDisabled() disabled}, then either this method returns
     * non-null or {@link #getDisabledReasonTranslatable()} will.
     */
    @Getter
    private String disabledReason;

    /**
     * If {@link #isDisabled() disabled}, then either this method returns non-null or {@link #getDisabledReason()} will.
     */
    @Getter
    private TranslatableString disabledReasonTranslatable;

    public boolean isDisabled() {
        return disabledReason != null || disabledReasonTranslatable != null;
    }

    /**
     * API for subscribers to disable the member, specifying the reason why.
     *
     * @see #disable(org.apache.causeway.applib.services.i18n.TranslatableString)
     * @see #veto(String, Object...)
     */
    public final void disable(final String reason) {
        this.disabledReason = reason;
    }

    /**
     * API for subscribers to disable the member, specifying the reason why as
     * a {@link TranslatableString}.
     *
     * @see #disable(java.lang.String)
     * @see #veto(org.apache.causeway.applib.services.i18n.TranslatableString)
     */
    public final void disable(final TranslatableString reason) {
        this.disabledReasonTranslatable = reason;
    }

    /**
     * Same as ... {@code if(reasonSupplier.get()!=null) disable(reasonSupplier.get()); }
     * @see #disable(String)
     */
    public void disableIfReason(final @NonNull Supplier<String> reasonSupplier) {
        Optional
            .ofNullable(reasonSupplier.get())
            .ifPresent(this::disable);
    }

    /**
     * Same as ... {@code if(reasonSupplier.get()!=null) disable(reasonSupplier.get()); }
     * @see #disable(TranslatableString)
     */
    public void disableIfTranslatableReason(final @NonNull Supplier<TranslatableString> reasonSupplier) {
        Optional
            .ofNullable(reasonSupplier.get())
            .ifPresent(this::disable);
    }

    // -- INVALIDATING

    /**
     * Used by the framework to determine whether the interaction is invalid
     * and should be blocked (eg pressing OK shows message).
     *
     * <p>
     * If {@link #isInvalid() invalid}, then either this method returns
     * non-null or {@link #getInvalidityReasonTranslatable()} will.
     * </p>
     */
    @Getter
    private String invalidityReason;

    /**
     * Used by the framework to determine whether the interaction is invalid
     * and should be blocked (eg pressing OK shows message).
     *
     * <p>
     * If {@link #isInvalid() invalid}, then either this method returns non-null
     * or {@link #getInvalidityReason()} will.
     * </p>
     */
    @Getter
    private TranslatableString invalidityReasonTranslatable;

    public boolean isInvalid() {
        return invalidityReason != null || invalidityReasonTranslatable != null;
    }

    /**
     * API for subscribers to invalidate an interaction, eg invalid arguments
     * to an action.
     *
     * @see #invalidate(org.apache.causeway.applib.services.i18n.TranslatableString)
     * @see #veto(String, Object...)
     */
    public void invalidate(final String reason) {
        this.invalidityReason = reason;
    }

    /**
     * API for subscribers to invalidate an interaction, specifying the reason
     * as a {@link TranslatableString}.
     *
     * @see #invalidate(String)
     * @see #veto(org.apache.causeway.applib.services.i18n.TranslatableString)
     */
    public void invalidate(final TranslatableString reason) {
        this.invalidityReasonTranslatable = reason;
    }

    /**
     * Same as ... {@code if(reasonSupplier.get()!=null) invalidate(reasonSupplier.get()); }
     * @see #invalidate(String)
     */
    public void invalidateIfReason(final @NonNull Supplier<String> reasonSupplier) {
        Optional
            .ofNullable(reasonSupplier.get())
            .ifPresent(this::invalidate);
    }

    /**
     * Same as ... {@code if(reasonSupplier.get()!=null) invalidate(reasonSupplier.get()); }
     * @see #invalidate(TranslatableString)
     */
    public void invalidateIfTranslatableReason(final @NonNull Supplier<TranslatableString> reasonSupplier) {
        Optional
            .ofNullable(reasonSupplier.get())
            .ifPresent(this::invalidate);
    }

    // -- VETOING

    /**
     * Use instead of {@link #hide()}, {@link #disable(String)} and
     * {@link #invalidate(String)}; just delegates to
     * appropriate vetoing method based upon the {@link #getEventPhase() phase}.
     *
     * <p>
     *     If hiding, just pass <tt>null</tt> for the parameter.
     * </p>
     *
     * @param reason - reason why the interaction is being invalidated (ignored
     *              if in {@link org.apache.causeway.applib.events.domain.AbstractDomainEvent.Phase#HIDE hide} phase).
     * @param args
     *
     * @see #veto(org.apache.causeway.applib.services.i18n.TranslatableString)
     */
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
        case EXECUTED:
        case EXECUTING:
            break;
        default:
            throw _Exceptions.unmatchedCase(getEventPhase());
        }
    }

    /**
     * Use instead of {@link #hide()}, {@link #disable(org.apache.causeway.applib.services.i18n.TranslatableString)} and {@link #invalidate(org.apache.causeway.applib.services.i18n.TranslatableString)}; just delegates to
     * appropriate vetoing method based upon the {@link #getEventPhase() phase}.
     *
     * <p>
     *     If hiding, just pass <tt>null</tt> for the parameter.
     * </p>
     *
     * @param translatableReason - reason why the interaction is being invalidated (ignored if in {@link org.apache.causeway.applib.events.domain.AbstractDomainEvent.Phase#HIDE hide} phase).
     *
     * @see #veto(String, Object...)
     */
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
        case EXECUTED:
        case EXECUTING:
            break;
        default:
            throw _Exceptions.unmatchedCase(getEventPhase());
        }
    }

    // -- USER DATA

    /**
     * Provides a mechanism to pass data to the next {@link #getEventPhase() phase}.
     */
    private final Map<Object, Object> userData = new HashMap<>();

    /**
     * Obtain user-data, as set by a previous {@link #getEventPhase() phase}.
     */
    public Object get(final Object key) {
        return userData.get(key);
    }

    /**
     * Mechanism to allow subscribers to share arbitrary information between
     * phases. One event instance is used for both the hide and disable phases,
     * and a different event instance is shared between validate/pre-execute/post-execute.
     *
     * Set user-data, for the use of a subsequent {@link #getEventPhase() phase}.
     */
    public final void put(final Object key, final Object value) {
        userData.put(key, value);
    }

    private static final ToString<AbstractDomainEvent<?>> toString =
            ObjectContracts.<AbstractDomainEvent<?>>
    toString("source", AbstractDomainEvent::getSource)
    .thenToString("identifier", AbstractDomainEvent::getIdentifier)
    .thenToString("eventPhase", AbstractDomainEvent::getEventPhase)
    ;

    @Override
    public String toString() {
        return toString.toString(this);
    }

}
