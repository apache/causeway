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

public abstract class CollectionDomainEvent<S,T> extends AbstractInteractionEvent<S> {

    private static final long serialVersionUID = 1L;

    //region > Default class
    /**
     * Propagated if no custom subclass was specified using
     * {@link org.apache.isis.applib.annotation.Collection#domainEvent()} annotation attribute.
     */
    public static class Default extends CollectionInteractionEvent<Object, Object> {
        private static final long serialVersionUID = 1L;
        public Default(
                final Object source,
                final Identifier identifier,
                final Of of,
                final Object value) {
            super(source, identifier, of, value);
        }
    }
    //endregion

    //region > constructors
    public CollectionDomainEvent(
            final S source,
            final Identifier identifier,
            final Of of) {
        super(source, identifier);
        this.of = of;
    }

    public CollectionDomainEvent(
            final S source,
            final Identifier identifier,
            final Of of,
            final T value) {
        this(source, identifier, of);
        this.value = value;
    }
    //endregion

    //region > value
    private T value;

    /**
     * The proposed reference to either add or remove (per {@link #getOf()}), populated at {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#VALIDATE}
     * and subsequent phases (is null for {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#HIDE hidden} and {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#DISABLE disable} phases).
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
    //endregion

    //region > Of
    public static enum Of {
        /**
         * The collection is being accessed
         * ({@link org.apache.isis.applib.services.eventbus.AbstractInteractionEvent.Phase#HIDE hide} and
         * {@link org.apache.isis.applib.services.eventbus.AbstractInteractionEvent.Phase#DISABLE disable}) checks.
         */
        ACCESS,
        /**
         * The collection is being added to
         * ({@link org.apache.isis.applib.services.eventbus.AbstractInteractionEvent.Phase#VALIDATE validity} check and
         * {@link org.apache.isis.applib.services.eventbus.AbstractInteractionEvent.Phase#EXECUTED execution}).
         */
        ADD_TO,
        /**
         * The collection is being removed from
         * ({@link org.apache.isis.applib.services.eventbus.AbstractInteractionEvent.Phase#VALIDATE validity} check and
         * {@link org.apache.isis.applib.services.eventbus.AbstractInteractionEvent.Phase#EXECUTED execution}).
         */
        REMOVE_FROM
    }

    private Of of;

    public Of getOf() {
        return of;
    }

    /**
     * Not API; updates from {@link Of#ACCESS} to either {@link Of#ADD_TO} or {@link Of#REMOVE_FROM} when hits the
     * {@link org.apache.isis.applib.services.eventbus.AbstractDomainEvent.Phase#VALIDATE validation phase}.
     */
    public void setOf(Of of) {
        this.of = of;
    }

    //endregion

    //region > toString
    @Override
    public String toString() {
        return ObjectContracts.toString(this, "source,identifier,phase,of,value");
    }
    //endregion
}