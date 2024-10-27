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
package org.apache.causeway.core.metamodel.facets;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.events.EventObjectBase;
import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.applib.events.domain.CollectionDomainEvent;
import org.apache.causeway.applib.events.domain.PropertyDomainEvent;
import org.apache.causeway.applib.exceptions.UnrecoverableException;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.services.events.MetamodelEventService;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;

import static org.apache.causeway.commons.internal.base._Casts.uncheckedCast;
import static org.apache.causeway.commons.internal.reflection._Reflect.predicates.paramAssignableFrom;
import static org.apache.causeway.commons.internal.reflection._Reflect.predicates.paramAssignableFromValue;
import static org.apache.causeway.commons.internal.reflection._Reflect.predicates.paramCount;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor(staticName = "ofEventService")
@Log4j2
public class DomainEventHelper {

    public static DomainEventHelper ofServiceRegistry(final ServiceRegistry serviceRegistry) {
        var eventService = serviceRegistry.lookupServiceElseFail(MetamodelEventService.class);
        return ofEventService(eventService);
    }

    private final MetamodelEventService metamodelEventService;

    // -- postEventForAction

    // variant using eventType and no existing event
    public @Nullable ActionDomainEvent<?> postEventForAction(
            final AbstractDomainEvent.Phase phase,
            final @NonNull Class<? extends ActionDomainEvent<?>> eventType,
            final ObjectAction objectAction,
            final FacetHolder facetHolder,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final @Nullable Object resultPojo) {

        return postEventForAction(phase, uncheckedCast(eventType), /*existingEvent*/null,
                objectAction, facetHolder,
                head, argumentAdapters, resultPojo);
    }

    // variant using existing event and not eventType (is derived from event)
    public void postEventForAction(
            final AbstractDomainEvent.Phase phase,
            final @NonNull ActionDomainEvent<?> existingEvent,
            final ObjectAction objectAction,
            final FacetHolder facetHolder,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final @Nullable Object resultPojo) {

        postEventForAction(phase,
                uncheckedCast(existingEvent.getClass()), existingEvent, objectAction, facetHolder,
                head, argumentAdapters, resultPojo);
    }

    private @Nullable <S> ActionDomainEvent<S> postEventForAction(
            final AbstractDomainEvent.Phase phase,
            final Class<? extends ActionDomainEvent<S>> eventType,
            final ActionDomainEvent<S> existingEvent,
            final ObjectAction objectAction,
            final FacetHolder facetHolder,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final @Nullable Object resultPojo) {

        _Assert.assertTypeIsInstanceOf(eventType, ActionDomainEvent.class);

        try {
            final ActionDomainEvent<S> event;

            if (existingEvent != null && phase.isExecuted()) {
                // reuse existing event from the executing phase
                event = existingEvent;
            } else {
                // all other phases, create a new event
                final S source = uncheckedCast(MmUnwrapUtils.single(head.getTarget()));
                final Object[] arguments = MmUnwrapUtils.multipleAsArray(argumentAdapters);
                final Identifier identifier = facetHolder.getFeatureIdentifier();
                event = newActionDomainEvent(eventType, identifier, source, arguments);

                // copy over if mixee is present
                if (event != null) {
                    head.getMixee()
                    .ifPresent(mixedInAdapter->
                        event.setMixee(mixedInAdapter.getPojo()));
                }

                if(objectAction != null) {  // should always be the case...

                    if (event != null) {
                        event.setSemantics(objectAction.getSemantics());
                    }

                    var parameters = objectAction.getParameters();

                    var parameterNames = parameters.stream()
                            .map(ObjectActionParameter::getCanonicalFriendlyName)
                            .collect(_Lists.toUnmodifiable());

                    final List<Class<?>> parameterTypes = parameters.stream()
                            .map(ObjectActionParameter::getElementType)
                            .map(ObjectSpecification::getCorrespondingClass)
                            .collect(_Lists.toUnmodifiable());

                    if (event != null) {
                        event.setParameterNames(parameterNames);
                        event.setParameterTypes(parameterTypes);
                    }
                }
            }

            if (event != null) {
                event.setEventPhase(phase);

                if(phase.isExecuted()) {
                    event.setReturnValue(resultPojo);
                }

                metamodelEventService.fireActionDomainEvent(event);
            }

            return event;
        } catch (Exception e) {
            throw new UnrecoverableException(e);
        }
    }

    static @Nullable <S> ActionDomainEvent<S> newActionDomainEvent(
            final Class<? extends ActionDomainEvent<S>> type,
            final Identifier identifier,
            final S source,
            final Object... arguments)
        throws IllegalArgumentException,
            SecurityException {

        var constructors = _Reflect.getPublicConstructors(type);

        var noArgConstructor = constructors
                .filter(paramCount(0))
                .getFirst().orElse(null);
        if(noArgConstructor!=null) {
            final ActionDomainEvent<S> ade = EventObjectBase.getInstanceWithSource(type, source).orElseThrow();
            ade.setIdentifier(identifier);
            ade.setArguments(asList(arguments));
            return ade;
        }

        var oneArgConstructor = constructors
                .filter(paramCount(1)
                        .and(paramAssignableFrom(0, source.getClass())))
                .getFirst().orElse(null);
        if(oneArgConstructor!=null) {

            final Object event = invokeConstructor(oneArgConstructor, source);
            final ActionDomainEvent<S> ade = uncheckedCast(event);

            ade.setIdentifier(identifier);
            ade.setArguments(asList(arguments));
            return ade;
        }

        var threeArgConstructor = constructors
                .filter(paramCount(3)
                        .and(paramAssignableFrom(0, source.getClass()))
                        .and(paramAssignableFrom(1, Identifier.class))
                        .and(paramAssignableFrom(2, Object[].class))
                        )
                .getFirst()
                .orElse(null);

        if(threeArgConstructor!=null) {
            var event = invokeConstructor(threeArgConstructor, source, identifier, arguments);
            return uncheckedCast(event);
        }

        log.error("Unable to locate constructor of ActionDomainEvent subclass.\n* event's class name : {}\n* source's class name: {}\n* identifier         : {}\n", type.getName(), source.getClass().getName(), identifier.getMemberLogicalName());

        return null;
    }

    // same as in ActionDomainEvent's constructor.
    private static List<Object> asList(final Object[] arguments) {
        return arguments != null
                ? Arrays.asList(arguments)
                        : Collections.emptyList();
    }

    // -- postEventForProperty, newPropertyInteraction
    public <S, T> PropertyDomainEvent<S, T> postEventForProperty(
            final AbstractDomainEvent.Phase phase,
            final Class<? extends PropertyDomainEvent<S, T>> eventType,
            final PropertyDomainEvent<S, T> existingEvent,
            final FacetHolder facetHolder,
            final InteractionHead head,
            final T oldValue,
            final T newValue) {

        _Assert.assertTypeIsInstanceOf(eventType, PropertyDomainEvent.class);

        try {
            final PropertyDomainEvent<S, T> event;

            if(existingEvent != null && phase.isExecuted()) {
                // reuse existing event from the executing phase
                event = existingEvent;
            } else {
                // all other phases, create a new event

                final S source = uncheckedCast(MmUnwrapUtils.single(head.getTarget()));
                final Identifier identifier = facetHolder.getFeatureIdentifier();

                event = newPropertyDomainEvent(eventType, identifier, source, oldValue, newValue);

                // copy over if have
                head.getMixee()
                .ifPresent(mixeeAdapter->
                    event.setMixee(mixeeAdapter.getPojo()));

            }

            event.setEventPhase(phase);

            // just in case the actual new value held by the object is different from that applied
            setEventNewValue(event, newValue);

            metamodelEventService.firePropertyDomainEvent(event);
            return event;
        } catch (Exception e) {
            throw new UnrecoverableException(e);
        }
    }

    private static <S,T> void setEventNewValue(final PropertyDomainEvent<S, T> event, final T newValue) {
        event.setNewValue(newValue);
    }

    static <S,T> PropertyDomainEvent<S,T> newPropertyDomainEvent(
            final @NonNull Class<? extends PropertyDomainEvent<S, T>> type,
            final @NonNull Identifier identifier,
            final S source,
            final T oldValue,
            final T newValue) throws NoSuchMethodException, SecurityException, IllegalArgumentException {

        var constructors = _Reflect.getPublicConstructors(type);

        var noArgonstructor = constructors
                .filter(paramCount(0))
                .getFirst().orElse(null);
        if(noArgonstructor != null) {
            final PropertyDomainEvent<S, T> pde = EventObjectBase.getInstanceWithSource(type, source).orElseThrow();
            pde.setIdentifier(identifier);
            pde.setOldValue(oldValue);
            pde.setNewValue(newValue);
            return pde;
        }

        var oneArgConstructor = constructors
                .filter(paramCount(1)
                        .and(paramAssignableFrom(0, source.getClass())))
                .getFirst().orElse(null);
        if(oneArgConstructor != null) {
            final Object event = invokeConstructor(oneArgConstructor, source);
            final PropertyDomainEvent<S, T> pde = uncheckedCast(event);
            pde.setIdentifier(identifier);
            pde.setOldValue(oldValue);
            pde.setNewValue(newValue);
            return pde;
        }

        // else
        var fourArgConstructor = constructors
                .filter(paramCount(4)
                        .and(paramAssignableFrom(0, source.getClass()))
                        .and(paramAssignableFrom(1, Identifier.class))
                        .and(paramAssignableFromValue(2, oldValue))
                        .and(paramAssignableFromValue(3, newValue))
                ).getFirst().orElse(null);
        if(fourArgConstructor != null) {
            var event = invokeConstructor(fourArgConstructor, source, identifier, oldValue, newValue);
            return uncheckedCast(event);
        }

        // else
        throw new NoSuchMethodException(type.getName()+".<init>(...)");
    }

    // -- postEventForCollection, newCollectionDomainEvent

    public <S, T> CollectionDomainEvent<S, T> postEventForCollection(
            final AbstractDomainEvent.Phase phase,
            final Class<? extends CollectionDomainEvent<S, T>> eventType,
            final FacetHolder facetHolder,
            final InteractionHead head) {

        _Assert.assertTypeIsInstanceOf(eventType, CollectionDomainEvent.class);

        try {
            final CollectionDomainEvent<S, T> event;

            final S source = uncheckedCast(MmUnwrapUtils.single(head.getTarget()));
            final Identifier identifier = facetHolder.getFeatureIdentifier();
            event = newCollectionDomainEvent(eventType, phase, identifier, source);

            // copy over if have
            head.getMixee()
            .ifPresent(mixeeAdapter->
                event.setMixee(mixeeAdapter.getPojo()));

            event.setEventPhase(phase);

            metamodelEventService.fireCollectionDomainEvent(event);
            return event;
        } catch (Exception e) {
            throw new UnrecoverableException(e);
        }
    }

    <S, T> CollectionDomainEvent<S, T> newCollectionDomainEvent(
            final Class<? extends CollectionDomainEvent<S, T>> type,
            final AbstractDomainEvent.Phase phase,
            final Identifier identifier,
            final S source)
            throws NoSuchMethodException, SecurityException,
            IllegalArgumentException {

        var constructors = _Reflect.getPublicConstructors(type);

        var noArgConstructor = constructors
                .filter(paramCount(0))
                .getFirst().orElse(null);
        if(noArgConstructor != null) {
            final CollectionDomainEvent<S, T> cde = EventObjectBase.getInstanceWithSource(type, source).orElseThrow();;
            cde.setIdentifier(identifier);
            return cde;
        }

        var oneArgConstructor = constructors
                .filter(paramCount(1)
                        .and(paramAssignableFrom(0, source.getClass())))
                .getFirst().orElse(null);
        if(oneArgConstructor != null) {
            final Object event = invokeConstructor(oneArgConstructor, source);
            final CollectionDomainEvent<S, T> cde = uncheckedCast(event);

            cde.setIdentifier(identifier);
            return cde;
        }

        // else
        // search for constructor accepting source, identifier
        var twoArgConstructor = constructors
                .filter(paramCount(2)
                        .and(paramAssignableFrom(0, source.getClass()))
                        .and(paramAssignableFrom(1, Identifier.class))
                        )
                .getFirst().orElse(null);
        if(twoArgConstructor != null) {
            var event = invokeConstructor(twoArgConstructor, source, identifier);
            return uncheckedCast(event);
        }

        // else
        throw new NoSuchMethodException(type.getName()+".<init>(...)");
    }

    private static <T> T invokeConstructor(
            final @NonNull Constructor<T> constructor,
            final Object... args){

        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw _Exceptions.unrecoverable(e,
                    "failed to invoke constructor %s", constructor);
        }
    }

}
