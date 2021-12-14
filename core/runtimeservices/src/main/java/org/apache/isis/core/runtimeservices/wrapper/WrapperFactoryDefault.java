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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.locale.UserLocale;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandExecutorService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionLayer;
import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrappingObject;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;
import org.apache.isis.applib.services.wrapper.control.ExecutionMode;
import org.apache.isis.applib.services.wrapper.control.SyncControl;
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
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.proxy._ProxyFactoryService;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.services.command.CommandDtoFactory;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.core.runtimeservices.wrapper.dispatchers.InteractionEventDispatcher;
import org.apache.isis.core.runtimeservices.wrapper.dispatchers.InteractionEventDispatcherTypeSafe;
import org.apache.isis.core.runtimeservices.wrapper.handlers.DomainObjectInvocationHandler;
import org.apache.isis.core.runtimeservices.wrapper.handlers.ProxyContextHandler;
import org.apache.isis.core.runtimeservices.wrapper.proxy.ProxyCreator;
import org.apache.isis.schema.cmd.v2.CommandDto;

import static org.apache.isis.applib.services.metamodel.MetaModelService.Mode.RELAXED;
import static org.apache.isis.applib.services.wrapper.control.SyncControl.control;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isis.runtimeservices.WrapperFactoryDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class WrapperFactoryDefault implements WrapperFactory {

    @Inject InteractionLayerTracker interactionLayerTracker;
    @Inject FactoryService factoryService;
    @Inject MetaModelContext metaModelContext;
    @Inject SpecificationLoader specificationLoader;
    @Inject ServiceInjector serviceInjector;
    @Inject _ProxyFactoryService proxyFactoryService; // protected to allow JUnit test
    @Inject @Lazy CommandDtoFactory commandDtoFactory;

    private final List<InteractionListener> listeners = new ArrayList<>();
    private final Map<Class<? extends InteractionEvent>, InteractionEventDispatcher>
        dispatchersByEventClass = new HashMap<>();
    private ProxyContextHandler proxyContextHandler;

    @PostConstruct
    public void init() {

        val proxyCreator = new ProxyCreator(proxyFactoryService);
        proxyContextHandler = new ProxyContextHandler(proxyCreator);

        putDispatcher(ObjectTitleEvent.class, InteractionListener::objectTitleRead);
        putDispatcher(PropertyVisibilityEvent.class, InteractionListener::propertyVisible);
        putDispatcher(PropertyUsabilityEvent.class, InteractionListener::propertyUsable);
        putDispatcher(PropertyAccessEvent.class, InteractionListener::propertyAccessed);
        putDispatcher(PropertyModifyEvent.class, InteractionListener::propertyModified);
        putDispatcher(CollectionVisibilityEvent.class, InteractionListener::collectionVisible);
        putDispatcher(CollectionUsabilityEvent.class, InteractionListener::collectionUsable);
        putDispatcher(CollectionAccessEvent.class, InteractionListener::collectionAccessed);
        putDispatcher(CollectionAddToEvent.class, InteractionListener::collectionAddedTo);
        putDispatcher(CollectionRemoveFromEvent.class, InteractionListener::collectionRemovedFrom);
        putDispatcher(ActionVisibilityEvent.class, InteractionListener::actionVisible);
        putDispatcher(ActionUsabilityEvent.class, InteractionListener::actionUsable);
        putDispatcher(ActionArgumentEvent.class, InteractionListener::actionArgument);
        putDispatcher(ActionInvocationEvent.class, InteractionListener::actionInvoked);
        putDispatcher(ObjectValidityEvent.class, InteractionListener::objectPersisted);
        putDispatcher(CollectionMethodEvent.class, InteractionListener::collectionMethodInvoked);
    }

    // -- WRAPPING

    @Override
    public <T> T wrap(
            final @NonNull T domainObject) {
        return wrap(domainObject, control());
    }

    @Override
    public <T> T wrap(
            final @NonNull T domainObject,
            final @NonNull SyncControl syncControl) {

        // skip in support of JUnit tests, that don't inject a SpecificationLoader
        if(specificationLoader!=null) {
            val spec = specificationLoader.specForTypeElseFail(domainObject.getClass());
            if(spec.isMixin()) {
                throw _Exceptions.illegalArgument("cannot wrap a mixin instance directly, "
                        + "use WrapperFactory.wrapMixin(...) instead");
            }
        }

        if (isWrapper(domainObject)) {
            val wrapperObject = (WrappingObject) domainObject;
            val executionMode = wrapperObject.__isis_executionModes();
            if(equivalent(executionMode, syncControl.getExecutionModes())) {
                return domainObject;
            }
            val underlyingDomainObject = wrapperObject.__isis_wrapped();
            return _Casts.uncheckedCast(createProxy(underlyingDomainObject, syncControl));
        }
        return createProxy(domainObject, syncControl);
    }

    private static boolean equivalent(final ImmutableEnumSet<ExecutionMode> first, final ImmutableEnumSet<ExecutionMode> second) {
        return equivalent(first.toEnumSet(), second.toEnumSet());
    }

    private static boolean equivalent(final EnumSet<ExecutionMode> first, final EnumSet<ExecutionMode> second) {
        return first.containsAll(second) && second.containsAll(first);
    }

    @Override
    public <T> T wrapMixin(
            final @NonNull Class<T> mixinClass,
            final @NonNull Object mixee) {
        return wrapMixin(mixinClass, mixee, control());
    }

    @Override
    public <T> T wrapMixin(
            final @NonNull Class<T> mixinClass,
            final @NonNull Object mixee,
            final @NonNull SyncControl syncControl) {

        T mixin = factoryService.mixin(mixinClass, mixee);

        if (isWrapper(mixee)) {
            val wrapperObject = (WrappingObject) mixee;
            val executionMode = wrapperObject.__isis_executionModes();
            if(equivalent(executionMode, syncControl.getExecutionModes())) {
                return mixin;
            }
            val underlyingMixee = wrapperObject.__isis_wrapped();
            return _Casts.uncheckedCast(createMixinProxy(underlyingMixee, mixin, syncControl));
        }

        return createMixinProxy(mixee, mixin, syncControl);
    }

    protected <T> T createProxy(final T domainObject, final SyncControl syncControl) {
        val objAdapter = adaptAndGuardAgainstWrappingNotSupported(domainObject);
        return proxyContextHandler.proxy(domainObject, objAdapter, syncControl);
    }

    protected <T> T createMixinProxy(final Object mixee, final T mixin, final SyncControl syncControl) {
        val mixeeAdapter = adaptAndGuardAgainstWrappingNotSupported(mixee);
        val mixinAdapter = adaptAndGuardAgainstWrappingNotSupported(mixin);
        return proxyContextHandler.mixinProxy(mixin, mixeeAdapter, mixinAdapter, syncControl);
    }

    @Override
    public boolean isWrapper(final Object obj) {
        return obj instanceof WrappingObject;
    }

    @Override
    public <T> T unwrap(final T possibleWrappedDomainObject) {
        if(isWrapper(possibleWrappedDomainObject)) {
            val wrappingObject = (WrappingObject) possibleWrappedDomainObject;
            return _Casts.uncheckedCast(wrappingObject.__isis_wrapped());
        }
        return possibleWrappedDomainObject;
    }


    // -- ASYNC WRAPPING


    @Override
    public <T,R> T asyncWrap(
            final @NonNull T domainObject,
            final AsyncControl<R> asyncControl) {

        val targetAdapter = adaptAndGuardAgainstWrappingNotSupported(domainObject);
        if(targetAdapter.getSpecification().isMixin()) {
            throw _Exceptions.illegalArgument("cannot wrap a mixin instance directly, "
                    + "use WrapperFactory.asyncWrapMixin(...) instead");
        }

        val proxyFactory = proxyFactoryService
                .<T>factory(_Casts.uncheckedCast(domainObject.getClass()), WrappingObject.class);

        return proxyFactory.createInstance((proxy, method, args) -> {

            if (isInheritedFromJavaLangObject(method)) {
                return method.invoke(domainObject, args);
            }

            if (shouldCheckRules(asyncControl)) {
                val doih = new DomainObjectInvocationHandler<>(
                        domainObject,
                        null, // mixeeAdapter ignored
                        targetAdapter,
                        control().withNoExecute(),
                        null);
                doih.invoke(null, method, args);
            }

            val memberAndTarget = memberAndTargetForRegular(method, targetAdapter);
            if( ! memberAndTarget.isMemberFound()) {
                return method.invoke(domainObject, args);
            }

            return submitAsync(memberAndTarget, args, asyncControl);
        }, false);
    }

    private boolean shouldCheckRules(final AsyncControl<?> asyncControl) {
        val executionModes = asyncControl.getExecutionModes();
        val skipRules = executionModes.contains(ExecutionMode.SKIP_RULE_VALIDATION);
        return !skipRules;
    }

    @Override
    public <T, R> T asyncWrapMixin(
            final @NonNull Class<T> mixinClass,
            final @NonNull Object mixee,
            final @NonNull AsyncControl<R> asyncControl) {

        T mixin = factoryService.mixin(mixinClass, mixee);

        val mixeeAdapter = adaptAndGuardAgainstWrappingNotSupported(mixee);
        val mixinAdapter = adaptAndGuardAgainstWrappingNotSupported(mixin);

        val proxyFactory = proxyFactoryService
                .factory(mixinClass, new Class[]{WrappingObject.class}, new Class[]{mixee.getClass()});

        return proxyFactory.createInstance((proxy, method, args) -> {

            final boolean inheritedFromObject = isInheritedFromJavaLangObject(method);
            if (inheritedFromObject) {
                return method.invoke(mixin, args);
            }

            if (shouldCheckRules(asyncControl)) {
                val doih = new DomainObjectInvocationHandler<>(
                        mixin,
                        mixeeAdapter,
                        mixinAdapter,
                        control().withNoExecute(),
                        null);
                doih.invoke(null, method, args);
            }

            val actionAndTarget = memberAndTargetForMixin(method, mixee, mixinAdapter);
            if (! actionAndTarget.isMemberFound()) {
                return method.invoke(mixin, args);
            }

            return submitAsync(actionAndTarget, args, asyncControl);
        }, new Object[]{ mixee });
    }

    private boolean isInheritedFromJavaLangObject(final Method method) {
        return method.getDeclaringClass().equals(Object.class);
    }

    private <R> Object submitAsync(
            final MemberAndTarget memberAndTarget,
            final Object[] args,
            final AsyncControl<R> asyncControl) {

        val interactionLayer = currentInteractionLayer();
        val interactionContext = interactionLayer.getInteractionContext();
        val asyncInteractionContext = interactionContextFrom(asyncControl, interactionContext);

        val command = interactionLayerTracker.currentInteractionElseFail().getCommand();
        val commandInteractionId = command.getInteractionId();

        val targetAdapter = memberAndTarget.getTarget();
        val method = memberAndTarget.getMethod();

        val argAdapters = Can.ofArray(WrapperFactoryDefault.this.adaptersFor(args));
        val targetList = Can.ofSingleton(InteractionHead.regular(targetAdapter));

        CommandDto commandDto;
        switch (memberAndTarget.getType()) {
            case ACTION:
                val action = memberAndTarget.getAction();
                commandDto = commandDtoFactory
                        .asCommandDto(commandInteractionId, targetList, action, argAdapters);
                break;
            case PROPERTY:
                val property = memberAndTarget.getProperty();
                commandDto = commandDtoFactory
                        .asCommandDto(commandInteractionId, targetList, property, argAdapters.getElseFail(0));
                break;
            default:
                // shouldn't happen, already catered for this case previously
                return null;
        }
        val oidDto = commandDto.getTargets().getOid().get(0);

        asyncControl.setMethod(method);
        asyncControl.setBookmark(Bookmark.forOidDto(oidDto));

        val executorService = asyncControl.getExecutorService();
        val future = executorService.submit(
                new ExecCommand<R>(
                        asyncInteractionContext,
                        Propagation.REQUIRES_NEW,
                        commandDto,
                        asyncControl.getReturnType(),
                        command,
                        serviceInjector)
        );

        asyncControl.setFuture(future);

        return null;
    }

    private MemberAndTarget memberAndTargetForRegular(
            final Method method,
            final ManagedObject targetAdapter) {

        val objectMember = targetAdapter.getSpecification().getMember(method).orElse(null);
        if(objectMember == null) {
            return MemberAndTarget.notFound();
        }

        if (objectMember instanceof OneToOneAssociation) {
            return MemberAndTarget.foundProperty((OneToOneAssociation) objectMember, targetAdapter, method);
        }
        if (objectMember instanceof ObjectAction) {
            return MemberAndTarget.foundAction((ObjectAction) objectMember, targetAdapter, method);
        }

        throw new UnsupportedOperationException(
                "Only properties and actions can be executed in the background "
                        + "(method " + method.getName() + " represents a " + objectMember.getFeatureType().name() + "')");
    }

    private <T> MemberAndTarget memberAndTargetForMixin(
            final Method method,
            final T mixedIn,
            final ManagedObject mixinAdapter) {

        val mixinMember = mixinAdapter.getSpecification().getMember(method).orElse(null);
        if (mixinMember == null) {
            return MemberAndTarget.notFound();
        }

        // find corresponding action of the mixedIn (this is the 'real' target).
        val mixedInClass = mixedIn.getClass();

        // don't care about anything other than actions
        // (contributed properties and collections are read-only).
        final ObjectActionMixedIn targetAction = specificationLoader
        .specForType(mixedInClass)
        .flatMap(mixedInSpec->mixedInSpec.streamAnyActions(MixedIn.INCLUDED)
                .filter(ObjectActionMixedIn.class::isInstance)
                .map(ObjectActionMixedIn.class::cast)
                .filter(x -> x.hasMixinAction((ObjectAction) mixinMember))
                .findFirst()
        )
        .orElseThrow(()->new UnsupportedOperationException(String.format(
                "Could not locate objectAction delegating to mixinAction id='%s' on mixedIn class '%s'",
                mixinMember.getId(), mixedInClass.getName())));

        return MemberAndTarget.foundAction(targetAction, currentObjectManager().adapt(mixedIn), method);
    }

    private static <R> InteractionContext interactionContextFrom(
            final AsyncControl<R> asyncControl,
            final InteractionContext interactionContext) {

        return InteractionContext.builder()
            .clock(Optional.ofNullable(asyncControl.getClock()).orElseGet(interactionContext::getClock))
            .locale(Optional.ofNullable(asyncControl.getLocale()).map(UserLocale::valueOf).orElse(null)) // if not set in asyncControl use defaults (set override to null)
            .timeZone(Optional.ofNullable(asyncControl.getTimeZone()).orElseGet(interactionContext::getTimeZone))
            .user(Optional.ofNullable(asyncControl.getUser()).orElseGet(interactionContext::getUser))
            .build();
    }

    @Data
    static class MemberAndTarget {
        static MemberAndTarget notFound() {
            return new MemberAndTarget(Type.NONE, null, null, null, null);
        }
        static MemberAndTarget foundAction(final ObjectAction action, final ManagedObject target, final Method method) {
            return new MemberAndTarget(Type.ACTION, action, null, target, method);
        }
        static MemberAndTarget foundProperty(final OneToOneAssociation property, final ManagedObject target, final Method method) {
            return new MemberAndTarget(Type.PROPERTY, null, property, target, method);
        }

        public boolean isMemberFound() {
            return type != Type.NONE;
        }

        enum Type {
            ACTION,
            PROPERTY,
            NONE
        }
        private final Type type;
        /**
         * Populated if and only if {@link #type} is {@link Type#ACTION}.
         */
        private final ObjectAction action;
        /**
         * Populated if and only if {@link #type} is {@link Type#PROPERTY}.
         */
        private final OneToOneAssociation property;
        private final ManagedObject target;
        private final Method method;
    }

    private ManagedObject[] adaptersFor(final Object[] args) {
        final ObjectManager objectManager = currentObjectManager();
        return _NullSafe.stream(args)
                .map(objectManager::adapt)
                .collect(_Arrays.toArray(ManagedObject.class, _NullSafe.size(args)));
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
        val dispatcher = dispatchersByEventClass.get(interactionEvent.getClass());
        if (dispatcher == null) {
            val msg = String.format("Unknown InteractionEvent %s - "
                    + "needs registering into dispatchers map", interactionEvent.getClass());
            throw _Exceptions.unrecoverable(msg);
        }
        dispatcher.dispatch(interactionEvent);
    }

    // -- HELPER - CHECK WRAPPING SUPPORTED

    private ManagedObject adaptAndGuardAgainstWrappingNotSupported(
            final @NonNull Object domainObject) {

        val adapter = currentObjectManager().adapt(domainObject);
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)
                || !adapter.getSpecification().getBeanSort().isWrappingSupported()) {
            throw _Exceptions.illegalArgument("Cannot wrap an object of type %s",
                    domainObject.getClass().getName());
        }

        return adapter;
    }

    // -- HELPER - SETUP

    private <T extends InteractionEvent> void putDispatcher(
            final Class<T> type, final BiConsumer<InteractionListener, T> onDispatch) {

        val dispatcher = new InteractionEventDispatcherTypeSafe<T>() {
            @Override
            public void dispatchTypeSafe(final T interactionEvent) {
                for (InteractionListener l : listeners) {
                    onDispatch.accept(l, interactionEvent);
                }
            }
        };

        dispatchersByEventClass.put(type, dispatcher);
    }

    private InteractionLayer currentInteractionLayer() {
        return interactionLayerTracker.currentInteractionLayerElseFail();
    }

    private ObjectManager currentObjectManager() {
        return metaModelContext.getObjectManager();
    }

    @RequiredArgsConstructor
    private static class ExecCommand<R> implements Callable<R> {

        private final InteractionContext interactionContext;
        private final Propagation propagation;
        private final CommandDto commandDto;
        private final Class<R> returnType;
        private final Command parentCommand;
        private final ServiceInjector serviceInjector;

        @Inject InteractionService interactionService;
        @Inject TransactionService transactionService;
        @Inject CommandExecutorService commandExecutorService;
        @Inject Provider<InteractionProvider> interactionProviderProvider;
        @Inject BookmarkService bookmarkService;
        @Inject RepositoryService repositoryService;
        @Inject MetaModelService metaModelService;

        @Override
        public R call() {
            serviceInjector.injectServicesInto(this);
            return interactionService.call(interactionContext, this::updateDomainObjectHonoringTransactionalPropagation);
        }

        private R updateDomainObjectHonoringTransactionalPropagation() {
            return transactionService.callTransactional(propagation, this::updateDomainObject)
                    .optionalElseFail()
                    .orElse(null);
        }

        private R updateDomainObject() {

            val childCommand = interactionProviderProvider.get().currentInteractionElseFail().getCommand();
            childCommand.updater().setParent(parentCommand);

            val bookmark = commandExecutorService.executeCommand(commandDto, childCommand.updater());
            if (bookmark == null) {
                return null;
            }
            R domainObject = bookmarkService.lookup(bookmark, returnType).orElse(null);
            if (metaModelService.sortOf(bookmark, RELAXED).isEntity()) {
                domainObject = repositoryService.detach(domainObject);
            }
            return domainObject;

        }


    }
}
