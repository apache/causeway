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
import org.apache.isis.applib.services.command.Command3;
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.services.eventbus.AbstractInteractionEvent;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.applib.services.eventbus.ActionInteractionEvent;
import org.apache.isis.applib.services.eventbus.CollectionDomainEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.eventbus.PropertyDomainEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;


public class DomainEventHelper {

    private final ServicesInjector servicesInjector;


    public DomainEventHelper(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

    //region > postEventForAction, newActionDomainEvent
    @SuppressWarnings({ "rawtypes" })
    public ActionDomainEvent<?> postEventForAction(
            final Class eventType,
            final ActionDomainEvent<?> existingEvent,
            final Command command,
            final AbstractDomainEvent.Phase phase,
            final IdentifiedHolder identified,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] argumentAdapters,
            final ObjectAdapter resultAdapter) {

        if(!hasEventBusService()) {
            return null;
        }
        try {
            final ActionDomainEvent<?> event;
            if (existingEvent != null && phase.isValidatingOrLater()) {
                event = existingEvent;
                final Object[] arguments = ObjectAdapter.Util.unwrap(argumentAdapters);
                event.setArguments(Arrays.asList(arguments));
                if(phase.isExecutingOrLater()) {

                    // current event always references the command (originally created by the xactn)
                    event.setCommand(command);
                    if(command != null) {
                        if(command instanceof Command3) {
                            final Command3 command3 = (Command3) command;
                            command3.pushActionDomainEvent(event);
                        } else if(command instanceof Command2 && event instanceof ActionInteractionEvent) {
                            final Command2 command2 = (Command3) command;
                            final ActionInteractionEvent<?> aie = (ActionInteractionEvent<?>) event;
                            command2.pushActionInteractionEvent(aie);
                        }
                    }
                }

                if(phase.isExecuted()) {
                    event.setReturnValue(resultAdapter != null? resultAdapter.getObject(): null);
                }
            } else {
                final Object source = ObjectAdapter.Util.unwrap(targetAdapter);
                final Object[] arguments = ObjectAdapter.Util.unwrap(argumentAdapters);
                final Identifier identifier = identified.getIdentifier();
                event = newActionDomainEvent(eventType, identifier, source, arguments);
            }

            if(identified instanceof ObjectAction) {
                // should always be the case...
                final ObjectAction objectAction = (ObjectAction) identified;
                event.setActionSemantics(objectAction.getSemantics());
            }

            event.setEventPhase(phase);
            event.setPhase(AbstractInteractionEvent.Phase.from(phase));


            getEventBusService().post(event);
            return event;
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static <S> ActionDomainEvent<S> newActionDomainEvent(
            final Class<? extends ActionDomainEvent<S>> type,
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
            return (ActionDomainEvent<S>) event;
        }
        throw new NoSuchMethodException(type.getName()+".<init>(? super " + source.getClass().getName() + ", " + Identifier.class.getName() + ", [Ljava.lang.Object;)");
    }
    //endregion

    //region > postEventForProperty, newPropertyInteraction
    public PropertyDomainEvent<?, ?> postEventForProperty(
            final Class eventType,
            final PropertyDomainEvent<?, ?> existingEvent,
            final AbstractDomainEvent.Phase phase,
            final IdentifiedHolder identified,
            final ObjectAdapter targetAdapter,
            final Object oldValue,
            final Object newValue) {
        if(!hasEventBusService()) {
            return null;
        }
        try {
            final PropertyDomainEvent<?, ?> event;
            final Object source = ObjectAdapter.Util.unwrap(targetAdapter);
            final Identifier identifier = identified.getIdentifier();

            // because of guava event bus buffering, we always create a new property domain event
            //
            event = newPropertyDomainEvent(eventType, identifier, source, oldValue, newValue);
            event.setEventPhase(phase);
            event.setPhase(AbstractInteractionEvent.Phase.from(phase));

            // Old and New Values are populated only on the VALIDATION Phase and
            // afterwards.
            if (phase.isValidatingOrLater()) {
                setEventOldValue(event, oldValue);
                setEventNewValue(event, newValue);
            }
            this.getEventBusService().post(event);
            return event;
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }

    private static <S,T> void setEventOldValue(PropertyDomainEvent<S, T> event, Object oldValue) {
        event.setOldValue((T) oldValue);
    }

    private static <S,T> void setEventNewValue(PropertyDomainEvent<S, T> event, Object newValue) {
        event.setNewValue((T) newValue);
    }

    @SuppressWarnings("unchecked")
    static <S,T> PropertyDomainEvent<S,T> newPropertyDomainEvent(
            final Class<? extends PropertyDomainEvent<S, T>> type,
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
            return (PropertyDomainEvent<S, T>) event;
        }

        throw new NoSuchMethodException(type.getName()+".<init>(? super " + source.getClass().getName() + ", " + Identifier.class.getName() + ", java.lang.Object, java.lang.Object)");
    }
    //endregion

    //region > postEventForCollection, newCollectionDomainEvent

    public CollectionDomainEvent<?, ?> postEventForCollection(
            final Class eventType,
            final CollectionDomainEvent<?, ?> existingEvent,
            AbstractDomainEvent.Phase phase,
            final IdentifiedHolder identified,
            final ObjectAdapter targetAdapter,
            final CollectionDomainEvent.Of of,
            final Object reference) {
        if(!hasEventBusService()) {
            return null;
        }
        try {
            final CollectionDomainEvent<?, ?> event;
            if (existingEvent != null && phase.isValidatingOrLater()) {
                event = existingEvent;
                event.setOf(of);
                setEventValue(event, reference);
            } else {
                final Object source = ObjectAdapter.Util.unwrap(targetAdapter);
                final Identifier identifier = identified.getIdentifier();
                event = newCollectionDomainEvent(eventType, phase, identifier, source, of, reference);
            }
            event.setEventPhase(phase);
            event.setPhase(AbstractInteractionEvent.Phase.from(phase));
            getEventBusService().post(event);
            return event;
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }

    private static <T,S> void setEventValue(CollectionDomainEvent<T, S> event, Object reference) {
        event.setValue((S) reference);
    }

    @SuppressWarnings("unchecked")
    <S, T> CollectionDomainEvent<S, T> newCollectionDomainEvent(
            final Class<? extends CollectionDomainEvent<S, T>> type,
            final AbstractDomainEvent.Phase phase,
            final Identifier identifier,
            final S source,
            final CollectionDomainEvent.Of of,
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
            if(!parameterTypes[2].isAssignableFrom(CollectionDomainEvent.Of.class)) {
                continue;
            }
            if(value != null && !parameterTypes[3].isAssignableFrom(value.getClass())) {
                continue;
            }
            final Object event = constructor.newInstance(source, identifier, of, value);
            return (CollectionDomainEvent<S, T>) event;
        }

        if(phase == AbstractDomainEvent.Phase.EXECUTED) {
            if(of == CollectionDomainEvent.Of.ADD_TO) {
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
                    return (CollectionDomainEvent<S, T>) event;
                }
            } else if(of == CollectionDomainEvent.Of.REMOVE_FROM) {
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
                    return (CollectionDomainEvent<S, T>) event;
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
