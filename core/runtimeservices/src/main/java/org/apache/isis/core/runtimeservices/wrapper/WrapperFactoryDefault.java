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
package org.apache.isis.core.runtimeservices.wrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.wrapper.AsyncWrap;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrappingObject;
import org.apache.isis.applib.services.wrapper.events.ActionArgumentEvent;
import org.apache.isis.applib.services.wrapper.events.ActionInvocationEvent;
import org.apache.isis.applib.services.wrapper.events.ActionUsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.ActionVisibilityEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionAccessEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionAddToEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionMethodEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionRemoveFromEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionUsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.CollectionVisibilityEvent;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.applib.services.wrapper.events.ObjectTitleEvent;
import org.apache.isis.applib.services.wrapper.events.ObjectValidityEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyAccessEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyModifyEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyUsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyVisibilityEvent;
import org.apache.isis.applib.services.wrapper.listeners.InteractionListener;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.commons.internal.plugins.codegen.ProxyFactoryService;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.runtimeservices.wrapper.dispatchers.InteractionEventDispatcher;
import org.apache.isis.core.runtimeservices.wrapper.dispatchers.InteractionEventDispatcherTypeSafe;
import org.apache.isis.core.runtimeservices.wrapper.handlers.ProxyContextHandler;
import org.apache.isis.core.runtimeservices.wrapper.proxy.ProxyCreator;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * This service provides the ability to 'wrap' a domain object such that it can
 * be interacted with, while enforcing the hide/disable/validate rules as implied by
 * the Isis programming model.
 */
@Service
@Named("isisRuntimeServices.WrapperFactoryDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class WrapperFactoryDefault implements WrapperFactory {
    
    @Inject private FactoryService factoryService;
    @Inject private MetaModelContext metaModelContext;
    @Inject private IsisSessionFactory isisSessionFactory;
    @Inject private TransactionService transactionService;
    @Inject protected ProxyFactoryService proxyFactoryService; // protected to allow JUnit test

    private final List<InteractionListener> listeners = new ArrayList<InteractionListener>();
    private final Map<Class<? extends InteractionEvent>, InteractionEventDispatcher>
        dispatchersByEventClass = 
            new HashMap<Class<? extends InteractionEvent>, InteractionEventDispatcher>();
    private ProxyContextHandler proxyContextHandler;
    
    @PostConstruct
    public void init() {

        val proxyCreator = new ProxyCreator(proxyFactoryService);
        proxyContextHandler = new ProxyContextHandler(proxyCreator);
        
        putDispatcher(ObjectTitleEvent.class, (listener, event)->listener.objectTitleRead(event));
        putDispatcher(PropertyVisibilityEvent.class, (listener, event)->listener.propertyVisible(event));
        putDispatcher(PropertyUsabilityEvent.class, (listener, event)->listener.propertyUsable(event));
        putDispatcher(PropertyAccessEvent.class, (listener, event)->listener.propertyAccessed(event));
        putDispatcher(PropertyModifyEvent.class, (listener, event)->listener.propertyModified(event));
        putDispatcher(CollectionVisibilityEvent.class, (listener, event)->listener.collectionVisible(event));
        putDispatcher(CollectionUsabilityEvent.class, (listener, event)->listener.collectionUsable(event));
        putDispatcher(CollectionAccessEvent.class, (listener, event)->listener.collectionAccessed(event));
        putDispatcher(CollectionAddToEvent.class, (listener, event)->listener.collectionAddedTo(event));
        putDispatcher(CollectionRemoveFromEvent.class, (listener, event)->listener.collectionRemovedFrom(event));
        putDispatcher(ActionVisibilityEvent.class, (listener, event)->listener.actionVisible(event));
        putDispatcher(ActionUsabilityEvent.class, (listener, event)->listener.actionUsable(event));
        putDispatcher(ActionArgumentEvent.class, (listener, event)->listener.actionArgument(event));
        putDispatcher(ActionInvocationEvent.class, (listener, event)->listener.actionInvoked(event));
        putDispatcher(ObjectValidityEvent.class, (listener, event)->listener.objectPersisted(event));
        putDispatcher(CollectionMethodEvent.class, (listener, event)->listener.collectionMethodInvoked(event));
    }

    // -- WRAPPING
    
    @Override
    public <T> T wrapMixin(Class<T> mixinClass, Object mixedIn) {
        return wrap(factoryService.mixin(mixinClass, mixedIn));
    }

    @Override
    public <T> T wrap(T domainObject) {
        return wrap(domainObject, ExecutionMode.EXECUTE);
    }

    @Override
    public <T> T wrapTry(T domainObject) {
        return wrap(domainObject, ExecutionMode.TRY);
    }

    @Override
    public <T> T wrapNoExecute(T domainObject) {
        return wrap(domainObject, ExecutionMode.NO_EXECUTE);
    }

    @Override
    public <T> T wrapSkipRules(T domainObject) {
        return wrap(domainObject, ExecutionMode.SKIP_RULES);
    }

    @Override
    public <T> T wrap(T domainObject, ImmutableEnumSet<ExecutionMode> mode) {
        if (domainObject instanceof WrappingObject) {
            val wrapperObject = (WrappingObject) domainObject;
            val executionMode = wrapperObject.__isis_executionMode();
            if(executionMode != mode) {
                val underlyingDomainObject = wrapperObject.__isis_wrapped();
                return _Casts.uncheckedCast(createProxy(underlyingDomainObject, mode));
            }
            return domainObject;
        }
        return createProxy(domainObject, mode);
    }

    protected <T> T createProxy(T domainObject, ImmutableEnumSet<ExecutionMode> mode) {
        
        return proxyContextHandler.proxy(metaModelContext, domainObject, mode);
    }

    @Override
    public boolean isWrapper(Object possibleWrappedDomainObject) {
        return possibleWrappedDomainObject instanceof WrappingObject;
    }

    @Override
    public <T> T unwrap(T possibleWrappedDomainObject) {
        if(isWrapper(possibleWrappedDomainObject)) {
            val wrappingObject = (WrappingObject) possibleWrappedDomainObject;
            return _Casts.uncheckedCast(wrappingObject.__isis_wrapped());
        }
        return possibleWrappedDomainObject;
    }

    // -- ASYNC WRAPPING

    
    @Override
    public <T> AsyncWrap<T> async(T domainObject, ImmutableEnumSet<ExecutionMode> mode) {
        val executor = ForkJoinPool.commonPool(); // default, but can be overwritten through withers on the returned AsyncWrap
        return new AsyncWrapDefault<T>(
                this, isisSessionFactory, transactionService, domainObject, mode, executor, log::error);
    }
    
    @Override
    public <T> AsyncWrap<T> asyncMixin(Class<T> mixinClass, Object mixedIn, ImmutableEnumSet<ExecutionMode> mode) {
        val mixin = factoryService.<T>mixin(mixinClass, mixedIn);
        return async(mixin, mode);
    }

    // -- LISTENERS

    @Override
    public List<InteractionListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    @Override
    public boolean addInteractionListener(InteractionListener listener) {
        return listeners.add(listener);
    }

    @Override
    public boolean removeInteractionListener(InteractionListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public void notifyListeners(InteractionEvent interactionEvent) {
        val dispatcher = dispatchersByEventClass.get(interactionEvent.getClass());
        if (dispatcher == null) {
            val msg = String.format("Unknown InteractionEvent %s - "
                    + "needs registering into dispatchers map", interactionEvent.getClass());
            throw _Exceptions.unrecoverable(msg);
        }
        dispatcher.dispatch(interactionEvent);
    }
    
    // -- HELPER - SETUP
    
    private <T extends InteractionEvent> void putDispatcher(
            Class<T> type, BiConsumer<InteractionListener, T> onDispatch) {
    
        val dispatcher = new InteractionEventDispatcherTypeSafe<T>() {
            @Override
            public void dispatchTypeSafe(T interactionEvent) {
                for (InteractionListener l : listeners) {
                    onDispatch.accept(l, interactionEvent);
                }
            }
        };
        
        dispatchersByEventClass.put(type, dispatcher);
    }


}
