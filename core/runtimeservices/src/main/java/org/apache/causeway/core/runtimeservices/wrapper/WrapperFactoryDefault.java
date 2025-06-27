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
package org.apache.causeway.core.runtimeservices.wrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import org.jspecify.annotations.NonNull;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.applib.services.wrapper.events.ActionArgumentEvent;
import org.apache.causeway.applib.services.wrapper.events.ActionInvocationEvent;
import org.apache.causeway.applib.services.wrapper.events.ActionUsabilityEvent;
import org.apache.causeway.applib.services.wrapper.events.ActionVisibilityEvent;
import org.apache.causeway.applib.services.wrapper.events.CollectionAccessEvent;
import org.apache.causeway.applib.services.wrapper.events.CollectionMethodEvent;
import org.apache.causeway.applib.services.wrapper.events.CollectionUsabilityEvent;
import org.apache.causeway.applib.services.wrapper.events.CollectionVisibilityEvent;
import org.apache.causeway.applib.services.wrapper.events.InteractionEvent;
import org.apache.causeway.applib.services.wrapper.events.ObjectTitleEvent;
import org.apache.causeway.applib.services.wrapper.events.ObjectValidityEvent;
import org.apache.causeway.applib.services.wrapper.events.PropertyAccessEvent;
import org.apache.causeway.applib.services.wrapper.events.PropertyModifyEvent;
import org.apache.causeway.applib.services.wrapper.events.PropertyUsabilityEvent;
import org.apache.causeway.applib.services.wrapper.events.PropertyVisibilityEvent;
import org.apache.causeway.applib.services.wrapper.listeners.InteractionListener;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.proxy.ProxyFactoryService;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.services.command.CommandDtoFactory;
import org.apache.causeway.core.runtime.wrap.WrappingObject;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.core.runtimeservices.session.InteractionIdGenerator;
import org.apache.causeway.core.runtimeservices.wrapper.dispatchers.InteractionEventDispatcher;
import org.apache.causeway.core.runtimeservices.wrapper.dispatchers.InteractionEventDispatcherTypeSafe;
import org.apache.causeway.core.runtimeservices.wrapper.handlers.ProxyGenerator;
import org.apache.causeway.core.runtimeservices.wrapper.internal.CommandRecord;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Default implementation of {@link WrapperFactory}.
 */
@Service
@Named(WrapperFactoryDefault.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class WrapperFactoryDefault
implements WrapperFactory, HasMetaModelContext {

    static final String LOGICAL_TYPE_NAME = CausewayModuleCoreRuntimeServices.NAMESPACE + ".WrapperFactoryDefault";

    @Inject private FactoryService factoryService;
    @Inject @Getter(onMethod_= {@Override}) MetaModelContext metaModelContext; // HasMetaModelContext
    @Inject protected ProxyFactoryService proxyFactoryService; // protected: in support of JUnit tests
    @Inject @Lazy private CommandDtoFactory commandDtoFactory;

    @Inject private Provider<InteractionService> interactionServiceProvider;
    @Inject private Provider<TransactionService> transactionServiceProvider;
    @Inject private InteractionIdGenerator interactionIdGenerator;

    private final List<InteractionListener> listeners = new ArrayList<>();
    private final Map<Class<? extends InteractionEvent>, InteractionEventDispatcher>
        dispatchersByEventClass = new HashMap<>();

    private ExecutorService commonExecutorService;
    private ProxyGenerator proxyGenerator;

    @Getter(lazy = true) @Accessors(fluent=true)
    private final AsyncExecutionFinisher executionFinisher = new AsyncExecutionFinisher(this, getRepositoryService());

    @PostConstruct
    public void init() {

        this.commonExecutorService = newCommonExecutorService();

        this.proxyGenerator = new ProxyGenerator(proxyFactoryService, new CommandRecord.Factory(interactionIdGenerator));

        putDispatcher(ObjectTitleEvent.class, InteractionListener::objectTitleRead);
        putDispatcher(PropertyVisibilityEvent.class, InteractionListener::propertyVisible);
        putDispatcher(PropertyUsabilityEvent.class, InteractionListener::propertyUsable);
        putDispatcher(PropertyAccessEvent.class, InteractionListener::propertyAccessed);
        putDispatcher(PropertyModifyEvent.class, InteractionListener::propertyModified);
        putDispatcher(CollectionVisibilityEvent.class, InteractionListener::collectionVisible);
        putDispatcher(CollectionUsabilityEvent.class, InteractionListener::collectionUsable);
        putDispatcher(CollectionAccessEvent.class, InteractionListener::collectionAccessed);
        putDispatcher(ActionVisibilityEvent.class, InteractionListener::actionVisible);
        putDispatcher(ActionUsabilityEvent.class, InteractionListener::actionUsable);
        putDispatcher(ActionArgumentEvent.class, InteractionListener::actionArgument);
        putDispatcher(ActionInvocationEvent.class, InteractionListener::actionInvoked);
        putDispatcher(ObjectValidityEvent.class, InteractionListener::objectPersisted);
        putDispatcher(CollectionMethodEvent.class, InteractionListener::collectionMethodInvoked);
    }

    @PreDestroy
    public void close() {
        commonExecutorService.shutdown();
    }

    // -- WRAPPING

    @Override
    public <T> T wrap(
            final @NonNull T domainObject) {
        return wrap(domainObject, SyncControl.defaults());
    }

    @Override
    public <T> T wrap(
            final @NonNull T domainObject,
            final @NonNull SyncControl syncControl) {

        var spec = getSpecificationLoader().specForTypeElseFail(domainObject.getClass());
        if(spec.isMixin()) {
            throw _Exceptions.illegalArgument("cannot wrap a mixin instance directly, "
                    + "use WrapperFactory.wrapMixin(...) instead");
        }

        if (isWrapper(domainObject)) {
            var wrapperObject = (WrappingObject) domainObject;
            var origin = wrapperObject.__causeway_origin();
            if(origin.syncControl().isEquivalent(syncControl)) {
                return domainObject;
            }
            var underlyingDomainObject = wrapperObject.__causeway_origin().pojo();
            return _Casts.uncheckedCast(createProxy(underlyingDomainObject, syncControl));
        }
        return createProxy(domainObject, syncControl);
    }

    @Override
    public <T> T wrapMixin(
            final @NonNull Class<T> mixinClass,
            final @NonNull Object mixee) {
        return wrapMixin(mixinClass, mixee, SyncControl.defaults());
    }

    @Override
    public <T> T wrapMixin(
            final @NonNull Class<T> mixinClass,
            final @NonNull Object mixee,
            final @NonNull SyncControl syncControl) {

        T mixin = factoryService.mixin(mixinClass, mixee);
        // no need to inject services into the mixin, factoryService does it for us.

        if (isWrapper(mixee)) {
            var wrappingObject = (WrappingObject) mixee;
            var origin = wrappingObject.__causeway_origin();
            var underlyingMixee = origin.pojo();

            getServiceInjector().injectServicesInto(underlyingMixee);

            if(origin.syncControl().isEquivalent(syncControl)) {
                return mixin;
            }
            return _Casts.uncheckedCast(createMixinProxy(underlyingMixee, mixin, syncControl));
        }

        getServiceInjector().injectServicesInto(mixee);

        return createMixinProxy(mixee, mixin, syncControl);
    }

    protected <T> T createProxy(final T domainObject, final SyncControl syncControl) {
        var objAdapter = adaptAndGuardAgainstWrappingNotSupported(domainObject);
        return proxyGenerator.objectProxy(domainObject, objAdapter.objSpec(), syncControl);
    }

    protected <T> T createMixinProxy(final Object mixee, final T mixin, final SyncControl syncControl) {
        var mixeeAdapter = adaptAndGuardAgainstWrappingNotSupported(mixee);
        var mixinAdapter = adaptAndGuardAgainstWrappingNotSupported(mixin);
        return proxyGenerator.mixinProxy(mixin, mixeeAdapter, mixinAdapter.objSpec(), syncControl);
    }

    @Override
    public boolean isWrapper(final Object obj) {
        return obj instanceof WrappingObject;
    }

    @Override
    public <T> T unwrap(final T t) {
        return t instanceof WrappingObject wrappingObject
                ? _Casts.uncheckedCast(wrappingObject.__causeway_origin().pojo())
                : t;
    }

    // -- ASYNC WRAPPING

    AsyncExecutor asyncExecutor(AsyncControl asyncControl) {
        return new AsyncExecutor(
                interactionServiceProvider.get(),
                transactionServiceProvider.get(),
                asyncControl.override(InteractionContext.builder().build()),
                Optional.of(Propagation.REQUIRES_NEW),
                Optional.ofNullable(asyncControl.executorService())
                    .orElse(commonExecutorService));
    }

    AsyncExecutionFinisher finisher() {
        return null;
    }

    @Override
    public <T> AsyncProxy<T> asyncWrap(T domainObject, AsyncControl asyncControl) {
        var proxy = wrap(domainObject, asyncControl.syncControl());
        return new AsyncProxyInternal<>(
                CompletableFuture.completedFuture(proxy),
                asyncExecutor(asyncControl),
                executionFinisher());
    }

    @Override
    public <T> AsyncProxy<T> asyncWrapMixin(
            final @NonNull Class<T> mixinClass,
            final @NonNull Object mixee,
            final @NonNull AsyncControl asyncControl) {
        var proxy = wrapMixin(mixinClass, mixee, asyncControl.syncControl());
        return new AsyncProxyInternal<>(
                CompletableFuture.completedFuture(proxy),
                asyncExecutor(asyncControl),
                executionFinisher());
    }

    // -- LISTENERS

    @Override
    public List<InteractionListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    @Override
    public boolean addInteractionListener(final InteractionListener listener) {
        return listeners.add(listener);
    }

    @Override
    public boolean removeInteractionListener(final InteractionListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public void notifyListeners(final InteractionEvent interactionEvent) {
        var dispatcher = dispatchersByEventClass.get(interactionEvent.getClass());
        if (dispatcher == null) {
            var msg = String.format("Unknown InteractionEvent %s - "
                    + "needs registering into dispatchers map", interactionEvent.getClass());
            throw _Exceptions.unrecoverable(msg);
        }
        dispatcher.dispatch(interactionEvent);
    }

    // -- HELPER - CHECK WRAPPING SUPPORTED

    private ManagedObject adaptAndGuardAgainstWrappingNotSupported(
            final @NonNull Object domainObject) {

        var adapter = getObjectManager().adapt(domainObject);
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)
                || !adapter.objSpec().getBeanSort().policy().isWrappingSupported()) {
            throw _Exceptions.illegalArgument("Cannot wrap an object of type %s",
                    domainObject.getClass().getName());
        }

        return adapter;
    }

    // -- HELPER - SETUP

    private <T extends InteractionEvent> void putDispatcher(
            final Class<T> type, final BiConsumer<InteractionListener, T> onDispatch) {

        var dispatcher = new InteractionEventDispatcherTypeSafe<T>() {
            @Override
            public void dispatchTypeSafe(final T interactionEvent) {
                for (InteractionListener l : listeners) {
                    onDispatch.accept(l, interactionEvent);
                }
            }
        };

        dispatchersByEventClass.put(type, dispatcher);
    }

    private final static int MIN_POOL_SIZE = 2; // at least 2
    private final static int MAX_POOL_SIZE = 4; // max 4
    private ExecutorService newCommonExecutorService() {
        final int poolSize = Math.min(
                MAX_POOL_SIZE,
                Math.max(
                        MIN_POOL_SIZE,
                        Runtime.getRuntime().availableProcessors()));
        return Executors.newFixedThreadPool(poolSize);
    }

}
