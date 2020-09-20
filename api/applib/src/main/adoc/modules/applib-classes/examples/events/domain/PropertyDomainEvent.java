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
package org.apache.isis.applib.events.domain;

import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;

import lombok.Getter;
import lombok.Setter;

// tag::refguide[]
public abstract class PropertyDomainEvent<S,T> extends AbstractDomainEvent<S> {

    // end::refguide[]
    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.Property#domainEvent()} annotation attribute.  Whether this
     * raises an event or not depends upon the <tt>isis.core.meta-model.annotation.property.domain-event.post-for-default</tt>
     * configuration property.
     */
    // tag::refguide[]
    public static class Default extends PropertyDomainEvent<Object, Object> {}

    // end::refguide[]
    /**
     * Convenience class to use indicating that an event should <i>not</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event.
     */
    // tag::refguide[]
    public static class Noop extends PropertyDomainEvent<Object, Object> {}

    // end::refguide[]
    /**
     * Convenience class meaning that an event <i>should</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event..
     */
    // tag::refguide[]
    public static class Doop extends PropertyDomainEvent<Object, Object> {}

    // end::refguide[]
    /**
     * If used then the framework will set state via (non-API) setters.
     *
     * <p>
     *     Recommended because it reduces the amount of boilerplate in the domain object classes.
     * </p>
     */
    public PropertyDomainEvent() {
    }

    // end::refguide[]
    /**
     * The current (pre-modification) value of the property; populated at {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#VALIDATE} and subsequent phases
     * (but null for {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#HIDE hidden} and {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#DISABLE disable} phases).
     */
    // tag::refguide[]
    @Getter @Setter
    private T oldValue;

    // end::refguide[]
    /**
     * The proposed (post-modification) value of the property; populated at {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#VALIDATE} and subsequent phases
     * (but null for {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#HIDE hidden} and {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#DISABLE disable} phases).
     *
     * <p>
     *     The proposed new value can also be modified by event handlers
     *     during the {@link Phase#EXECUTING} phase.  The new value must be
     *     the same type as the expected value; the framework performs
     *     no sanity checks.
     * </p>
     */
    // tag::refguide[]
    @Getter @Setter
    private T newValue;

    // end::refguide[]



    private static final ToString<PropertyDomainEvent<?,?>> toString =
            ObjectContracts.<PropertyDomainEvent<?,?>>
    toString("source", PropertyDomainEvent::getSource)
    .thenToString("identifier", PropertyDomainEvent::getIdentifier)
    .thenToString("eventPhase", PropertyDomainEvent::getEventPhase)
    .thenToString("oldValue", PropertyDomainEvent::getOldValue)
    .thenToString("newValue", PropertyDomainEvent::getNewValue)
    ;

    @Override
    public String toString() {
        return toString.toString(this);
    }

    // tag::refguide[]

}
// end::refguide[]
