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
import org.apache.isis.applib.util.ObjectContracts;

public abstract class PropertyDomainEvent<S,T> extends AbstractInteractionEvent<S> {

    private static final long serialVersionUID = 1L;

    //region > Default class
    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.Property#domainEvent()} annotation attribute.  Whether this
     * raises an event or not depends upon the "isis.reflector.facet.propertyAnnotation.domainEvent.postForDefault"
     * configuration property.
     */
    public static class Default extends PropertyInteractionEvent<Object, Object> {
        private static final long serialVersionUID = 1L;
        public Default(){}
        @Deprecated
        public Default(Object source, Identifier identifier, Object oldValue, Object newValue) {
            super(source, identifier, oldValue, newValue);
        }
    }
    //endregion

    //region > Noop class

    /**
     * Convenience class to use indicating that an event should <i>not</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event.
     */
    public static class Noop extends PropertyInteractionEvent<Object, Object> {
        private static final long serialVersionUID = 1L;
    }
    //endregion

    //region > Doop class

    /**
     * Convenience class meaning that an event <i>should</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event..
     */
    public static class Doop extends PropertyInteractionEvent<Object, Object> {
        private static final long serialVersionUID = 1L;
    }
    //endregion

    //region > constructors

    /**
     * If used then the framework will set state via (non-API) setters.
     *
     * <p>
     *     Recommended because it reduces the amount of boilerplate in the domain object classes.
     * </p>
     */
    public PropertyDomainEvent() {
    }

    /**
     * @deprecated - the {@link #PropertyDomainEvent() no-arg constructor} is recommended instead, to reduce boilerplate.
     */
    @Deprecated
    public PropertyDomainEvent(
            final S source,
            final Identifier identifier) {
        super(source, identifier);
    }

    /**
     * @deprecated - the {@link #PropertyDomainEvent() no-arg constructor} is recommended instead, to reduce boilerplate.
     */
    @Deprecated
    public PropertyDomainEvent(
            final S source,
            final Identifier identifier,
            final T oldValue, final T newValue) {
        this(source, identifier);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    //endregion

    //region > oldValue
    private T oldValue;

    /**
     * The current (pre-modification) value of the property; populated at {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#VALIDATE} and subsequent phases
     * (but null for {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#HIDE hidden} and {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#DISABLE disable} phases).
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
    //endregion

    //region > newValue
    private T newValue;
    /**
     * The proposed (post-modification) value of the property; populated at {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#VALIDATE} and subsequent phases
     * (but null for {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#HIDE hidden} and {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#DISABLE disable} phases).
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
    //endregion

    //region > toString
    @Override
    public String toString() {
        return ObjectContracts.toString(this, "source,identifier,phase,oldValue,newValue");
    }
    //endregion
}