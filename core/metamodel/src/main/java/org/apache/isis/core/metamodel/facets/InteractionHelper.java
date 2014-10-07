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
import java.util.Arrays;
import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.Command2;
import org.apache.isis.applib.services.eventbus.*;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;


public class InteractionHelper {

    private final ServicesInjector servicesInjector;


    public InteractionHelper(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

    //region > postEventForAction, newActionInteractionEvent
    @SuppressWarnings({ "rawtypes" })
    public ActionInteractionEvent<?> postEventForAction(
            final Class eventType,
            final ActionInteractionEvent<?> existingEvent,
            final Command command,
            final AbstractInteractionEvent.Phase phase,
            final IdentifiedHolder identified,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] argumentAdapters) {

        if(!hasEventBusService()) {
            return null;
        }
        try {
            final ActionInteractionEvent<?> event;
            if (existingEvent != null && phase.isValidatingOrLater()) {
                event = existingEvent;
                final Object[] arguments = ObjectAdapter.Util.unwrap(argumentAdapters);
                event.setArguments(Arrays.asList(arguments));
                if(phase.isExecutingOrLater()) {

                    // current event always references the command (originally created by the xactn)
                    event.setCommand(command);
                    if(command != null && command instanceof Command2) {
                        final Command2 command2 = (Command2) command;

                        // don't overwrite any existing event.
                        //
                        // this can happen when one action invokes another (wrapped), and can also occur if a
                        // bulk action is performed (when the query that produced the list is automatically re-executed).
                        // (This logic is in the Wicket viewer's BulkActionsLinkFactory).
                        //
                        // This also means that the command could refer to a different event (the one of the original
                        // outer-most action that started the xactn) compared to the event that references it.

                        final ActionInteractionEvent<?> actionInteractionEvent = command2.getActionInteractionEvent();
                        if(actionInteractionEvent == null) {
                            command2.setActionInteractionEvent(event);
                        }
                    }
                }
            } else {
                final Object source = ObjectAdapter.Util.unwrap(targetAdapter);
                final Object[] arguments = ObjectAdapter.Util.unwrap(argumentAdapters);
                final Identifier identifier = identified.getIdentifier();
                event = newActionInteractionEvent(eventType, identifier, source, arguments);
            }

            if(identified instanceof ObjectAction) {
                // should always be the case...
                final ObjectAction objectAction = (ObjectAction) identified;
                event.setActionSemantics(objectAction.getSemantics());
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
            final Identifier identifier,
            final S source,
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
            final IdentifiedHolder identified,
            final ObjectAdapter targetAdapter,
            final Object oldValue,
            final Object newValue) {
        if(!hasEventBusService()) {
            return null;
        }
        try {
            final PropertyInteractionEvent<?, ?> event;
            if(existingEvent != null && phase.isValidatingOrLater()) {
                event = existingEvent;
                setEventOldValue(event, oldValue);
                setEventNewValue(event, newValue);
            } else {
                final Object source = ObjectAdapter.Util.unwrap(targetAdapter);
                final Identifier identifier = identified.getIdentifier();
                event = newPropertyInteractionEvent(eventType, identifier, source, oldValue, newValue);
            }
            event.setPhase(phase);
            getEventBusService().post(event);
            return event;
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }

    private static <S,T> void setEventOldValue(PropertyInteractionEvent<S, T> event, Object oldValue) {
        event.setOldValue((T) oldValue);
    }

    private static <S,T> void setEventNewValue(PropertyInteractionEvent<S, T> event, Object newValue) {
        event.setNewValue((T) newValue);
    }

    @SuppressWarnings("unchecked")
    static <S,T> PropertyInteractionEvent<S,T> newPropertyInteractionEvent(
            final Class<? extends PropertyInteractionEvent<S, T>> type,
            final Identifier identifier,
            final S source,
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
            AbstractInteractionEvent.Phase phase,
            final IdentifiedHolder identified,
            final ObjectAdapter targetAdapter,
            final CollectionInteractionEvent.Of of,
            final Object reference) {
        if(!hasEventBusService()) {
            return null;
        }
        try {
            final CollectionInteractionEvent<?, ?> event;
            if (existingEvent != null && phase.isValidatingOrLater()) {
                event = existingEvent;
                event.setOf(of);
                setEventValue(event, reference);
            } else {
                final Object source = ObjectAdapter.Util.unwrap(targetAdapter);
                final Identifier identifier = identified.getIdentifier();
                event = newCollectionInteractionEvent(eventType, null, identifier, source, of, reference);
            }
            event.setPhase(phase);
            getEventBusService().post(event);
            return event;
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }

    private static <T,S> void setEventValue(CollectionInteractionEvent<T, S> event, Object reference) {
        event.setValue((S) reference);
    }

    @SuppressWarnings("unchecked")
    <S, T> CollectionInteractionEvent<S, T> newCollectionInteractionEvent(
            final Class<? extends CollectionInteractionEvent<S, T>> type,
            final AbstractInteractionEvent.Phase phase,
            final Identifier identifier,
            final S source,
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

        if(phase == AbstractInteractionEvent.Phase.EXECUTED) {
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
        // this caching has been disabled, because it prevents integration tests from switching out the
        // EventBusService with a mock.
        // perhaps a better appraoch
        //searchedForEventBusService = true;
        return eventBusService;
    }

    //endregion

}
