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

package org.apache.isis.applib.services.wrapper.events;

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.events.EventObjectBase;

/**
 * <i>Supported only by {@link org.apache.isis.applib.services.wrapper.WrapperFactory} service, </i> 
 * represents an interaction with a domain object or a particular feature
 * (property, collection, action) of a domain object.
 *
 * <p>
 * Many of the interactions are checks for {@link VisibilityEvent visibility},
 * {@link UsabilityEvent usability} and {@link ValidityEvent validity}.
 *
 * @since 2.0 {@index}
 */
public abstract class InteractionEvent extends EventObjectBase<Object> {

    private final Identifier identifier;
    private String reason;
    private Class<?> advisorClass;

    public InteractionEvent(final Object source, final Identifier identifier) {
        super(source);
        this.identifier = identifier;
    }

    /**
     * The domain object (pojo) against which the interaction occurred.
     */
    @Override
    public Object getSource() {
        return super.getSource();
    }

    /**
     * The {@link Identifier} of the feature of the object being interacted
     * with.
     *
     * <p>
     * Will be consistent with the subclass of {@link InteractionEvent}. So for
     * example a {@link PropertyModifyEvent} will have an {@link Identifier}
     * that identifies the property being modified.
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * As per {@link #getClassName()}, but naturalized.
     *
     * @see #getIdentifier
     */
    public String getClassName() {
        return identifier.getClassName();
    }

    /**
     * Convenience method that returns the
     * {@link Identifier#getClassNaturalName() natural class name} of the
     * {@link #getIdentifier() identifier}.
     */
    public String getClassNaturalName() {
        return identifier.getClassNaturalName();
    }

    /**
     * Convenience method that returns the {@link Identifier#getMemberName()
     * member name} of the {@link #getIdentifier() identifier}.
     *
     * @see #getIdentifier
     */
    public String getMemberName() {
        return identifier.getMemberName();
    }

    /**
     * As per {@link #getMemberName()}, but naturalized.
     */
    public String getMemberNaturalName() {
        return identifier.getMemberNaturalName();
    }

    /**
     * Convenience method that returns the {@link Identifier#getClassName()
     * class name} of the {@link #getIdentifier() identifier}.
     */
    public List<String> getMemberParameterNames() {
        return identifier.getMemberParameterNames();
    }

    /**
     * As per {@link #getMemberParameterName()}, but naturalized.
     */
    public List<String> getMemberParameterNaturalNames() {
        return identifier.getMemberParameterNaturalNames();
    }

    /**
     * The reason, if any, that this interaction may have been vetoed or
     * otherwise disallowed.
     *
     * <p>
     * Intended to be {@link #setExecuteIn(String) set} as a result of consulting
     * one of the facets.
     */
    public String getReason() {
        return reason;
    }

    /**
     * The reason message, if any, that this interaction may have been vetoed or
     * otherwise disallowed.
     *
     * <p>
     * This message should be overridden by subclasses for containing the Reason, the Identifier and any other relevant context information.
     */
    public String getReasonMessage() {
        if (this.getIdentifier() != null) {
            return String.format("Reason: %s. Identifier: %s", this.getReason(), this.getIdentifier());
        } else {
            return String.format("Reason: %s", this.getReason());
        }
    }

    /**
     * The class of the (first) advisor, if any, that provided the
     * {@link #getReason() reason} that this interaction is {@link #isVeto()
     * vetoed}.
     */
    public Class<?> getAdvisorClass() {
        return advisorClass;
    }

    /**
     * Specify the {@link #getReason() reason} that this interaction has been
     * vetoed and the {@link #getAdvisorClass() class of the advisor} that did
     * the veto.
     */
    public void advised(final String reason, final Class<?> advisorClass) {
        this.reason = reason;
        this.advisorClass = advisorClass;
    }

    /**
     * Whether this interaction has been vetoed (meaning that
     * {@link #getReason()} and {@link #getAdvisorClass()} will both be non-
     * <tt>null</tt> and the {@link #getReason() reason} non-empty.)
     *
     * <p>
     * The interpretation of this depends on the subclass:
     * <ul>
     * <li>for {@link VisibilityEvent}, a veto means that the feature (property,
     * collection, action) is hidden</li>
     * <li>for {@link UsabilityEvent}, a veto means that the feature is disabled
     * </li>
     * <li>for {@link ValidityEvent}, a veto means that the proposed
     * modification (property value, object added/removed, action argument) is
     * invalid</li>
     * </ul>
     */
    public boolean isVeto() {
        return getReason() != null && getReason().length() > 0;
    }

}
