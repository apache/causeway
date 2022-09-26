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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.isis.applib.services.wrapper.callable.AsyncCallable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.locale.UserLocale;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
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
import org.apache.isis.applib.services.wrapper.events.CollectionMethodEvent;
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
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.MixinConstructor;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.object.ManagedObjects;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.services.command.CommandDtoFactory;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.MixedInMember;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.core.runtimeservices.wrapper.dispatchers.InteractionEventDispatcher;
import org.apache.isis.core.runtimeservices.wrapper.dispatchers.InteractionEventDispatcherTypeSafe;
import org.apache.isis.core.runtimeservices.wrapper.handlers.DomainObjectInvocationHandler;
import org.apache.isis.core.runtimeservices.wrapper.handlers.ProxyContextHandler;
import org.apache.isis.core.runtimeservices.wrapper.proxy.ProxyCreator;
import org.apache.isis.schema.cmd.v2.CommandDto;

import static org.apache.isis.applib.services.metamodel.MetaModelService.Mode.RELAXED;
import static org.apache.isis.applib.services.wrapper.control.SyncControl.control;

import lombok.*;

@Service
@Named(WrapperFactoryDefault.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class WrapperFactoryDefault implements WrapperFactory {

    static final String LOGICAL_TYPE_NAME = IsisModuleCoreRuntimeServices.NAMESPACE + ".WrapperFactoryDefault";

    @Inject InteractionLayerTracker interactionLayerTracker;
    @Inject FactoryService factoryService;
    @Inject MetaModelContext metaModelContext;
    @Inject SpecificationLoader specificationLoader;
    @Inject ServiceInjector serviceInjector;
    @Inject _ProxyFactoryService proxyFactoryService;
    @Inject @Lazy CommandDtoFactory commandDtoFactory;

    @Inject Provider<InteractionService> interactionServiceProvider;
    @Inject Provider<TransactionService> transactionServiceProvider;
    @Inject Provider<CommandExecutorService> commandExecutorServiceProvider;
    @Inject Provider<InteractionProvider> interactionProviderProvider;
    @Inject Provider<BookmarkService> bookmarkServiceProvider;
    @Inject Provider<RepositoryService> repositoryServiceProvider;
    @Inject Provider<MetaModelService> metaModelServiceProvider;

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
//XXX[ISIS-3084] - removal of collection modification events
//        putDispatcher(CollectionAddToEvent.class, InteractionListener::collectionAddedTo);
//        putDispatcher(CollectionRemoveFromEvent.class, InteractionListener::collectionRemovedFrom);
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
        // no need to inject services into the mixin, factoryService does it for us.

        if (isWrapper(mixee)) {
            val wrapperObject = (WrappingObject) mixee;
            val executionMode = wrapperObject.__isis_executionModes();
            val underlyingMixee = wrapperObject.__isis_wrapped();

            serviceInjector.injectServicesInto(underlyingMixee);

            if(equivalent(executionMode, syncControl.getExecutionModes())) {
                return mixin;
            }
            return _Casts.uncheckedCast(createMixinProxy(underlyingMixee, mixin, syncControl));
        }

        serviceInjector.injectServicesInto(mixee);

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

        val mixinConstructor = MixinConstructor.PUBLIC_SINGLE_ARG_RECEIVING_MIXEE
                .getConstructorElseFail(mixinClass, mixee.getClass());

        val proxyFactory = proxyFactoryService
                .factory(mixinClass, new Class[]{WrappingObject.class}, mixinConstructor.getParameterTypes());

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
        val head = InteractionHead.regular(targetAdapter);

        CommandDto commandDto;
        switch (memberAndTarget.getType()) {
            case ACTION:
                val action = memberAndTarget.getAction();
                commandDto = commandDtoFactory
                        .asCommandDto(commandInteractionId, head, action, argAdapters);
                break;
            case PROPERTY:
                val property = memberAndTarget.getProperty();
                commandDto = commandDtoFactory
                        .asCommandDto(commandInteractionId, head, property, argAdapters.getElseFail(0));
                break;
            default:
                // shouldn't happen, already catered for this case previously
                return null;
        }
        val oidDto = commandDto.getTargets().getOid().get(0);

        asyncControl.setMethod(method);
        asyncControl.setBookmark(Bookmark.forOidDto(oidDto));

        val executorService = asyncControl.getExecutorService();
        AsyncTask<R> task = serviceInjector.injectServicesInto(
                new AsyncTask<R>(
                        asyncInteractionContext,
                        Propagation.REQUIRES_NEW,
                        commandDto,
                        asyncControl.getReturnType(),
                        command.getInteractionId()) // this command becomes the parent of child command
        );
        val future = executorService.submit(task);
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
            final T mixee,
            final ManagedObject mixinAdapter) {

        val mixinMember = mixinAdapter.getSpecification().getMember(method).orElse(null);
        if (mixinMember == null) {
            return MemberAndTarget.notFound();
        }

        // find corresponding action of the mixee (this is the 'real' target, the target usable for invocation).
        val mixeeClass = mixee.getClass();

        // don't care about anything other than actions
        // (contributed properties and collections are read-only).
        final ObjectAction targetAction = specificationLoader.specForType(mixeeClass)
        .flatMap(mixeeSpec->mixeeSpec.streamAnyActions(MixedIn.ONLY)
                .filter(act -> ((MixedInMember)act).hasMixinAction((ObjectAction) mixinMember))
                .findFirst()
        )
        .orElseThrow(()->new UnsupportedOperationException(String.format(
                "Could not locate objectAction delegating to mixinAction id='%s' on mixee class '%s'",
                mixinMember.getId(), mixeeClass.getName())));

        return MemberAndTarget.foundAction(targetAction, currentObjectManager().adapt(mixee), method);
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
    private static class AsyncTask<R> implements Callable<R>, AsyncCallable<R> {

        @Getter private final InteractionContext interactionContext;
        @Getter private final Propagation propagation;
        @Getter private final CommandDto commandDto;
        @Getter private final Class<R> returnType;
        @Getter private final UUID parentInteractionId;

        /**
         * Note that is a <code>transient</code> field in order that {@link org.apache.isis.applib.services.wrapper.callable.AsyncCallable} can be declared as
         * {@link java.io.Serializable}.
         *
         * <p>
         *  Because this field needs to be populated, the {@link java.util.concurrent.ExecutorService} that ultimately
         *  executes the task will need to be a custom implementation because it must reinitialize this field first,
         *  using the {@link ServiceInjector} service.  Alternatively, it could call
         *  {@link WrapperFactory#execute(AsyncCallable)} directly, which achieves the same thing.
         * </p>
         */
        @Inject transient WrapperFactory wrapperFactory;

        /**
         * If the {@link java.util.concurrent.ExecutorService} used to execute this task (as defined by
         * {@link AsyncControl#with(ExecutorService)} is not custom, then it can simply invoke this method, but it is
         * important that it has not serialized/deserialized the object since important transient state would be lost.
         *
         * <p>
         *  On the other hand, a custom implementation of {@link ExecutorService} is free to serialize this object, and
         *  deserialize it later.  When deserializing it can either reinitialize the necessary state using the
         *  {@link ServiceInjector} service, then call this method, or it can instead call
         *  {@link WrapperFactory#execute(AsyncCallable)} directly, which achieves the same thing.
         * </p>
         */
        @Override
        public R call() {
            if (wrapperFactory == null) {
                throw new IllegalStateException(
                        "The transient wrapperFactory is null; suggests that this async task been serialized and " +
                        "then deserialized, but is now being executed by an ExecutorService that has not re-injected necessary services.");
            }
            return wrapperFactory.execute(this);
        }
    }


    public <R> R execute(AsyncCallable<R> asyncCallable) {
        serviceInjector.injectServicesInto(this);
        return interactionServiceProvider.get().call(asyncCallable.getInteractionContext(), () -> updateDomainObjectHonoringTransactionalPropagation(asyncCallable));
    }

    private <R> R updateDomainObjectHonoringTransactionalPropagation(AsyncCallable<R> asyncCallable) {
        return transactionServiceProvider.get().callTransactional(asyncCallable.getPropagation(), () -> updateDomainObject(asyncCallable))
                .ifFailureFail()
                .getValue().orElse(null);
    }

    private <R> R updateDomainObject(AsyncCallable<R> asyncCallable) {

        val childCommand = interactionProviderProvider.get().currentInteractionElseFail().getCommand();
        childCommand.updater().setParentInteractionId(asyncCallable.getParentInteractionId());

        val bookmark = commandExecutorServiceProvider.get().executeCommand(asyncCallable.getCommandDto(), childCommand.updater());
        if (bookmark == null) {
            return null;
        }
        R domainObject = bookmarkServiceProvider.get().lookup(bookmark, asyncCallable.getReturnType()).orElse(null);
        if (metaModelServiceProvider.get().sortOf(bookmark, RELAXED).isEntity()) {
            domainObject = repositoryServiceProvider.get().detach(domainObject);
        }
        return domainObject;
    }

}
