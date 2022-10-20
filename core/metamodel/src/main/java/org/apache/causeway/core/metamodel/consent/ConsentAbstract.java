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
package org.apache.causeway.core.metamodel.consent;

import java.io.Serializable;

public abstract class ConsentAbstract implements Serializable, Consent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Factory method.
     *
     * <p>
     * Used extensively by the DnD viewer.
     */
    public static Consent allowIf(final boolean allowed) {
        return allowed ? Allow.DEFAULT : Veto.DEFAULT;
    }

    private final InteractionResult interactionResult;
    private final String reason;

    /**
     * Can be subsequently {@link #setDescription(String) modified}, but is only
     * a description of the event to which this consent applies and does not
     * change whether the Consent represents an allow or a veto.
     */
    private String description;

    private static String determineReason(final InteractionResult interactionResult) {
        if (interactionResult == null) {
            return null;
        }
        return interactionResult.getReason();
    }

    /**
     *
     * @param interactionResult
     *            - if <tt>null</tt> then defaults to an {@link #isAllowed()
     *            allowing} {@link Consent}.
     */
    protected ConsentAbstract(final InteractionResult interactionResult) {
        this(interactionResult, null, determineReason(interactionResult));
    }

    /**
     * Enable legacy {@link Consent}s (not created using an
     * {@link InteractionResult}) to create an {@link Consent}, specifying a
     * {@link #getDescription() description} of the event and the
     * {@link #getReason() reason} (if any) that the consent is vetoed.
     *
     * @param description
     *            - a description of the event to which this consent relates
     * @param reason
     *            - if not <tt>null</tt> and not empty, is the reason this
     *            consent is vetoed.
     */
    protected ConsentAbstract(final String description, final String reason) {
        this(null, description, reason);
    }

    private ConsentAbstract(
            final InteractionResult interactionResult,
            final String description,
            final String reason) {
        this.interactionResult = interactionResult;
        this.description = description;
        this.reason = reason;
    }

    /**
     * The reason why this has been vetoed.
     */
    @Override
    public String getReason() {
        return isVetoed() ? this.reason : null;
    }

    @Override
    public Consent setDescription(final String description) {
        this.description = description;
        return this;
    }

    /**
     * Returns <tt>true</tt> if this object is giving permission (if the
     * {@link #getReason() reason} is <tt>null</tt> or empty.
     *
     * @see #getReason()
     */
    @Override
    public boolean isAllowed() {
        return this.reason == null || this.reason.equals("");
    }

    /**
     * Returns true if this object is NOT giving permission.
     *
     * @see #isAllowed()
     */
    @Override
    public boolean isVetoed() {
        return !isAllowed();
    }

    /**
     * Underlying {@link InteractionResult} that created this {@link Consent}
     * (may be <tt>null</tt>).
     *
     */
    @Override
    public InteractionResult getInteractionResult() {
        return interactionResult;
    }

    /**
     * Description of the action allowed by this event.
     *
     * <p>
     * (Previously, {@link Allow} consents overloaded the {@link #getReason()
     * reason} property with a description of the event. This has now been
     * changed so that a non-<tt>null</tt> reason always implies a {@link Veto}.
     * This property captures the description.
     */
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return (isVetoed() ? "VETOED" : "ALLOWED") + ", reason=" + reason;
    }

}
