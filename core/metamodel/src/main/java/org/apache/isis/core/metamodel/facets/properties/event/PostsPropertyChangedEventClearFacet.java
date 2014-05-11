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

package org.apache.isis.core.metamodel.facets.properties.event;

import java.lang.reflect.InvocationTargetException;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.eventbus.PropertyChangedEvent;
import org.apache.isis.core.metamodel.facets.SingleValueFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyClearFacet;

/**
 * Indicates that (the specified subclass of) {@link PropertyChangedEvent} should be posted to the
 * {@link EventBusService}.
 */
public interface PostsPropertyChangedEventClearFacet 
        extends SingleValueFacet<Class<? extends PropertyChangedEvent<?,?>>>, PropertyClearFacet {

    public static class Util {
        private Util(){}
        public static <S,T> PropertyChangedEvent<S,T> newEvent(
                final Class<? extends PropertyChangedEvent<S, T>> type, 
                final S source, 
                final Identifier identifier,
                final T oldValue, 
                final T newValue) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
            return org.apache.isis.core.metamodel.facets.properties.event.Utils.newEvent(type, source, identifier, oldValue, newValue);
        }
    }
}
