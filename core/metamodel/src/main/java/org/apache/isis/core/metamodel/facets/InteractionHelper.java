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

package org.apache.isis.core.metamodel.facets;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.eventbus.*;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;


public class InteractionHelper {

    private final ServicesInjector servicesInjector;


    public InteractionHelper(
            final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

    //region > postEventForAction, newActionInteractionEvent
    @SuppressWarnings({ "rawtypes" })
    public ActionInteractionEvent<?> postEventForAction(
            final Class eventType,
            final ActionInteractionEvent<?> existingEvent,
            final AbstractInteractionEvent.Phase phase,
            final IdentifiedHolder identified,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] argumentAdapters) {

        if(!hasEventBusService()) {
            return null;
        }
        try {
            final ActionInteractionEvent<?> event;
            if (existingEvent != null) {
                event = existingEvent;
            } else {
                final Object source = ObjectAdapter.Util.unwrap(targetAdapter);
                final Object[] arguments = ObjectAdapter.Util.unwrap(argumentAdapters);
                final Identifier identifier = identified.getIdentifier();
                event = newActionInteractionEvent(eventType, source, identifier, arguments);
            }
            event.setPhase(phase);
            getEventBusService().post(event);
            return event;
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static <S> ActionInteractionEvent<S> newActionInteractionEvent(
            final Class<? extends ActionInteractionEvent<S>> type,
            final S source,
            final Identifier identifier,
            final Object... arguments) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        final Constructor<?>[] constructors = type.getConstructors();
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
            if(!parameterTypes[2].isAssignableFrom(Object[].class)) {
                continue;
            }
            final Object event = constructor.newInstance(source, identifier, arguments);
            return (ActionInteractionEvent<S>) event;
        }
        throw new NoSuchMethodException(type.getName()+".<init>(? super " + source.getClass().getName() + ", " + Identifier.class.getName() + ", [Ljava.lang.Object;)");
    }
    //endregion

    //region > postEventForProperty, newPropertyInteraction
    public PropertyInteractionEvent<?, ?> postEventForProperty(
            final Class eventType,
            final PropertyInteractionEvent<?, ?> existingEvent,
            final AbstractInteractionEvent.Phase phase,
            final ObjectAdapter targetAdapter,
            final IdentifiedHolder identified,
            final Object oldValue,
            final Object newValue) {
        if(!hasEventBusService()) {
            return null;
        }
        try {
            final PropertyInteractionEvent<?, ?> event;
            if(existingEvent != null) {
                event = existingEvent;
            } else {
                final Object source = ObjectAdapter.Util.unwrap(targetAdapter);
                final Identifier identifier = identified.getIdentifier();
                event = newPropertyInteractionEvent(eventType, source, identifier, oldValue, newValue);
            }
            event.setPhase(phase);
            getEventBusService().post(event);
            return event;
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static <S,T> PropertyInteractionEvent<S,T> newPropertyInteractionEvent(
            final Class<? extends PropertyInteractionEvent<S, T>> type,
            final S source,
            final Identifier identifier,
            final T oldValue,
            final T newValue) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException  {

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
            if(oldValue != null && !parameterTypes[2].isAssignableFrom(oldValue.getClass())) {
                continue;
            }
            if(newValue != null && !parameterTypes[3].isAssignableFrom(newValue.getClass())) {
                continue;
            }
            final Object event = constructor.newInstance(source, identifier, oldValue, newValue);
            return (PropertyInteractionEvent<S, T>) event;
        }

        throw new NoSuchMethodException(type.getName()+".<init>(? super " + source.getClass().getName() + ", " + Identifier.class.getName() + ", java.lang.Object, java.lang.Object)");
    }
    //endregion

    //region > postEventForCollection, newCollectionInteractionEvent

    public CollectionInteractionEvent<?, ?> postEventForCollection(
            final Class eventType,
            final CollectionInteractionEvent<?, ?> existingEvent,
            AbstractInteractionEvent.Phase phase, final ObjectAdapter targetAdapter,
            final IdentifiedHolder identified,
            final CollectionInteractionEvent.Of of,
            final Object reference) {
        if(!hasEventBusService()) {
            return null;
        }
        try {
            final CollectionInteractionEvent<?, ?> event;
            if (existingEvent != null) {
                event = existingEvent;
                event.setOf(of);
            } else {
                final Object source = ObjectAdapter.Util.unwrap(targetAdapter);
                final Identifier identifier = identified.getIdentifier();
                event = newCollectionInteractionEvent(eventType, null, source, identifier, of, reference);
            }
            event.setPhase(phase);
            getEventBusService().post(event);
            return event;
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }

    @SuppressWarnings("unchecked")
    <S, T> CollectionInteractionEvent<S, T> newCollectionInteractionEvent(
            final Class<? extends CollectionInteractionEvent<S, T>> type,
            final AbstractInteractionEvent.Phase phase,
            final S source,
            final Identifier identifier,
            final CollectionInteractionEvent.Of of,
            final T value)
            throws NoSuchMethodException, SecurityException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        final Constructor<?>[] constructors = type.getConstructors();

        // search for constructor accepting source, identifier, type, value
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
            final Object event = constructor.newInstance(source, identifier, of, value);
            return (CollectionInteractionEvent<S, T>) event;
        }

        if(phase == AbstractInteractionEvent.Phase.EXECUTE) {
            if(of == CollectionInteractionEvent.Of.ADD_TO) {
                // support for @PostsCollectionAddedTo annotation:
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
                    final Object event = constructor.newInstance(source, identifier, value);
                    return (CollectionInteractionEvent<S, T>) event;
                }
            } else if(of == CollectionInteractionEvent.Of.REMOVE_FROM) {
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
            }
        }
        throw new NoSuchMethodException(type.getName()+".<init>(? super " + source.getClass().getName() + ", " + Identifier.class.getName() + ", java.lang.Object)");
    }

    //endregion


    //region > eventBusService

    private EventBusService eventBusService;
    private boolean searchedForEventBusService = false;

    public boolean hasEventBusService() {
        return getEventBusService() != null;
    }

    private EventBusService getEventBusService() {
        if (!searchedForEventBusService) {
            eventBusService = this.servicesInjector.lookupService(EventBusService.class);
        }
        searchedForEventBusService = true;
        return eventBusService;
    }

    //endregion

}
