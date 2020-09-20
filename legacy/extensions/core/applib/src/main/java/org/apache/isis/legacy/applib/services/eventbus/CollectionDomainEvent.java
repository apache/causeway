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
package org.apache.isis.legacy.applib.services.eventbus;

import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;

/**
 * @deprecated
 */
@Deprecated
public abstract class CollectionDomainEvent<S, T> extends AbstractDomainEvent<S> {

    // -- Default class
    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.Collection#domainEvent()} annotation attribute.  Whether this
     * raises an event or not depends upon the <tt>isis.core.meta-model.annotation.collection.domain-event.post-for-default</tt>
     * configuration property.
     */
    public static class Default extends CollectionDomainEvent<Object, Object> {}
    

    // -- Noop class

    /**
     * Convenience class to use indicating that an event should <i>not</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event.
     */
    public static class Noop extends CollectionDomainEvent<Object, Object> {}
    

    // -- Doop class

    /**
     * Convenience class meaning that an event <i>should</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event..
     */
    public static class Doop extends CollectionDomainEvent<Object, Object> {}
    


    // -- constructor

    /**
     * If used then the framework will set state via (non-API) setters.
     *
     * <p>
     *     Recommended because it reduces the amount of boilerplate in the domain object classes.
     * </p>
     */
    public CollectionDomainEvent() {
    }

    

    // -- value
    private T value;

    /**
     * The proposed reference to either add or remove (per {@link #getOf()}), populated at {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#VALIDATE}
     * and subsequent phases (is null for {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#HIDE hidden} and {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#DISABLE disable} phases).
     */
    public T getValue() {
        return value;
    }
    /**
     * Not API, set by the framework.
     */
    public void setValue(T value) {
        this.value = value;
    }
    

    // -- Of
    public static enum Of {
        /**
         * The collection is being accessed
         * ({@link Phase#HIDE hide} and
         * {@link Phase#DISABLE disable}) checks.
         */
        ACCESS,
        /**
         * The collection is being added to
         * ({@link Phase#VALIDATE validity} check and
         * {@link Phase#EXECUTED execution}).
         */
        ADD_TO,
        /**
         * The collection is being removed from
         * ({@link Phase#VALIDATE validity} check and
         * {@link Phase#EXECUTED execution}).
         */
        REMOVE_FROM
    }

    private Of of;

    public Of getOf() {
        return of;
    }

    /**
     * Not API; updates from {@link Of#ACCESS} to either {@link Of#ADD_TO} or {@link Of#REMOVE_FROM} when hits the
     * {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#VALIDATE validation phase}.
     */
    public void setOf(Of of) {
        this.of = of;
    }

    
    
    private static final ToString<CollectionDomainEvent<?,?>> toString = 
    		ObjectContracts.<CollectionDomainEvent<?,?>>
		    toString("source", CollectionDomainEvent::getSource)
		    .thenToString("identifier", CollectionDomainEvent::getIdentifier)
		    .thenToString("eventPhase", CollectionDomainEvent::getEventPhase)
		    .thenToString("of", CollectionDomainEvent::getOf) 
		    .thenToString("value", CollectionDomainEvent::getValue)
		    ;

    @Override
    public String toString() {
    	return toString.toString(this);
    }

}