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
public abstract class PropertyDomainEvent<S,T> extends AbstractDomainEvent<S> {

    // -- Default class
    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.Property#domainEvent()} annotation attribute.  Whether this
     * raises an event or not depends upon the <tt>isis.core.meta-model.annotation.property.domain-event.post-for-default</tt>
     * configuration property.
     */
    public static class Default extends PropertyDomainEvent<Object, Object> {}
    

    // -- Noop class

    /**
     * Convenience class to use indicating that an event should <i>not</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event.
     */
    public static class Noop extends PropertyDomainEvent<Object, Object> {}
    

    // -- Doop class

    /**
     * Convenience class meaning that an event <i>should</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event..
     */
    public static class Doop extends PropertyDomainEvent<Object, Object> {}
    

    // -- constructors

    /**
     * If used then the framework will set state via (non-API) setters.
     *
     * <p>
     *     Recommended because it reduces the amount of boilerplate in the domain object classes.
     * </p>
     */
    public PropertyDomainEvent() {
    }


    // -- oldValue
    private T oldValue;

    /**
     * The current (pre-modification) value of the property; populated at {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#VALIDATE} and subsequent phases
     * (but null for {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#HIDE hidden} and {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#DISABLE disable} phases).
     */
    public T getOldValue() {
        return oldValue;
    }
    /**
     * Not API; for framework use only.
     */
    public void setOldValue(T oldValue) {
        this.oldValue = oldValue;
    }
    

    // -- newValue
    private T newValue;
    /**
     * The proposed (post-modification) value of the property; populated at {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#VALIDATE} and subsequent phases
     * (but null for {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#HIDE hidden} and {@link org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase#DISABLE disable} phases).
     */
    public T getNewValue() {
        return newValue;
    }
    /**
     * Not API; for framework use only.
     */
    public void setNewValue(T newValue) {
        this.newValue = newValue;
    }
    

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
    
}