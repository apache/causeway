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

/**
 * Subclass of {@link AbstractDomainEvent} for collections.
 *
 * <p>
 * The class has a couple of responsibilities (in addition to those it
 * inherits):
 * </p>
 *
 * <ul>
 *     <li>
 *      capture the target object being interacted with
 *     </li>
 * </ul>
 *
 * <p>
 * The class itself is instantiated automatically by the framework whenever
 * interacting with a rendered object's collection.
 * </p>
 *
 * @since 1.x {@index}
 */
public abstract class CollectionDomainEvent<S,T> extends AbstractDomainEvent<S> {

    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.Collection#domainEvent()}
     * annotation attribute.
     *
     * <p>
     * Whether this raises an event or not depends upon the
     * <tt>isis.core.meta-model.annotation.collection.domain-event.post-for-default</tt>
     * configuration property.
     * </p>
     */
    public static class Default extends CollectionDomainEvent<Object, Object> { }

    /**
     * Convenience class to use indicating that an event should <i>not</i> be
     * posted (irrespective of the configuration property setting for the
     * {@link Default} event.
     */
    public static class Noop extends CollectionDomainEvent<Object, Object> {}

    /**
     * Convenience class meaning that an event <i>should</i> be posted
     * (irrespective of the configuration property setting for the
     * {@link Default} event.
     */
    public static class Doop extends CollectionDomainEvent<Object, Object> {}


    /**
     * Subtypes can define a no-arg constructor; the framework sets state
     * via (non-API) setters.
     */
    public CollectionDomainEvent() {
    }



    /**
     * The proposed reference to either add or remove (per {@link #getOf()}), populated at
     * {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#VALIDATE}
     * and subsequent phases (is null for
     * {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#HIDE hidden}
     * and {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#DISABLE disable} phases).
     */
    @Getter
    private T value;


    /**
     * Not API, set by the framework.
     */
    public void setValue(T value) {
        this.value = value;
    }


    private static final ToString<CollectionDomainEvent<?,?>> toString =
            ObjectContracts.<CollectionDomainEvent<?,?>>
    toString("source", CollectionDomainEvent::getSource)
    .thenToString("identifier", CollectionDomainEvent::getIdentifier)
    .thenToString("eventPhase", CollectionDomainEvent::getEventPhase)
    .thenToString("value", CollectionDomainEvent::getValue)
    ;

    @Override
    public String toString() {
        return toString.toString(this);
    }


}
