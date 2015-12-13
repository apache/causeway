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

/**
 * @deprecated - use {@link org.apache.isis.applib.services.eventbus.PropertyDomainEvent} instead.
 */
@Deprecated
public abstract class PropertyInteractionEvent<S,T> extends PropertyDomainEvent<S,T> {

    //region > Default class
    /**
     * @deprecated - use {@link org.apache.isis.applib.services.eventbus.PropertyDomainEvent.Default} instead.
     */
    @Deprecated
    public static class Default extends PropertyDomainEvent.Default {
        private static final long serialVersionUID = 1L;
        public Default(){}
        public Default(Object source, Identifier identifier, Object oldValue, Object newValue) {
            super(source, identifier, oldValue, newValue);
        }
    }
    //endregion

    //region > constructors
    public PropertyInteractionEvent() {}

    @Deprecated
    public PropertyInteractionEvent(
            final S source,
            final Identifier identifier) {
        super(source, identifier);
    }

    @Deprecated
    public PropertyInteractionEvent(
            final S source,
            final Identifier identifier,
            final T oldValue, final T newValue) {
        super(source, identifier, oldValue, newValue);
    }
    //endregion

}