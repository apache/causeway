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

package org.apache.isis.core.metamodel.facets.collections.event;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.eventbus.CollectionInteractionEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.metamodel.facetapi.MultiTypedFacet;
import org.apache.isis.core.metamodel.facets.SingleValueFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;

/**
 * Indicates that (the specified subclass of) {@link CollectionInteractionEvent} should be posted to the
 * {@link EventBusService}.
 */
public interface InteractionWithCollectionRemoveFacet extends SingleValueFacet<Class<? extends CollectionInteractionEvent<?,?>>>, CollectionRemoveFromFacet, MultiTypedFacet {


    public static class Util {
        private Util(){}

        @SuppressWarnings("unchecked")
        public static <S, T> CollectionInteractionEvent<S, T> newEvent(
                final Class<? extends CollectionInteractionEvent<S, T>> type,
                        final S source, 
                        final Identifier identifier, 
                        final T value) 
                                throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

            // search for constructor accepting source, identifier, type, value
            final Constructor<?>[] constructors = type.getConstructors();
            for (final Constructor<?> constructor : constructors) {
                final Class<?>[] parameterTypes = constructor.getParameterTypes();
                if(parameterTypes.length != 4) {
                    continue;
                }
                if(!parameterTypes[0].isAssignableFrom(source.getClass())) {
                    continue;
                }
                if(!parameterTypes[1].isAssignableFrom(Identifier.class)) {
                    continue;
                }
                if(!parameterTypes[2].isAssignableFrom(CollectionInteractionEvent.Of.class)) {
                    continue;
                }
                if(value != null && !parameterTypes[3].isAssignableFrom(value.getClass())) {
                    continue;
                }
                final Object event = constructor.newInstance(
                        source, identifier, CollectionInteractionEvent.Of.REMOVE_FROM, value);
                return (CollectionInteractionEvent<S, T>) event;
            }

            // support for @PostsCollectionRemovedFrom annotation:
            // search for constructor accepting source, identifier, value
            for (final Constructor<?> constructor : constructors) {
                final Class<?>[] parameterTypes = constructor.getParameterTypes();
                if(parameterTypes.length != 3) {
                    continue;
                }
                if(!parameterTypes[0].isAssignableFrom(source.getClass())) {
                    continue;
                }
                if(!parameterTypes[1].isAssignableFrom(Identifier.class)) {
                    continue;
                }
                if(value != null && !parameterTypes[2].isAssignableFrom(value.getClass())) {
                    continue;
                }
                final Object event = constructor.newInstance(
                        source, identifier, value);
                return (CollectionInteractionEvent<S, T>) event;
            }

            throw new NoSuchMethodException(type.getName()+".<init>(? super " + source.getClass().getName() + ", " + Identifier.class.getName() + ", java.lang.Object)");
        }
    }

}

