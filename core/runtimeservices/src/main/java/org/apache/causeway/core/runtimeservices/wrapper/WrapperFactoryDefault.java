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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayer;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.WrappingObject;
import org.apache.causeway.applib.services.wrapper.callable.AsyncCallable;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.applib.services.wrapper.control.ExecutionMode;
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
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.proxy._ProxyFactoryService;
import org.apache.causeway.commons.internal.reflection._GenericResolver;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.MixinConstructor;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.services.command.CommandDtoFactory;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.MixedInMember;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.core.runtimeservices.session.InteractionIdGenerator;
import org.apache.causeway.core.runtimeservices.wrapper.dispatchers.InteractionEventDispatcher;
import org.apache.causeway.core.runtimeservices.wrapper.dispatchers.InteractionEventDispatcherTypeSafe;
import org.apache.causeway.core.runtimeservices.wrapper.handlers.DomainObjectInvocationHandler;
import org.apache.causeway.core.runtimeservices.wrapper.handlers.ProxyContextHandler;
import org.apache.causeway.core.runtimeservices.wrapper.proxy.ProxyCreator;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import static org.apache.causeway.applib.services.wrapper.control.SyncControl.control;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Default implementation of {@link WrapperFactory}.
 *
 * @since 2.0 {@index}
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
    @Inject protected _ProxyFactoryService proxyFactoryService; // protected: in support of JUnit tests
    @Inject @Lazy private CommandDtoFactory commandDtoFactory;

    @Inject private Provider<InteractionService> interactionServiceProvider;
    @Inject private Provider<TransactionService> transactionServiceProvider;
    @Inject private Provider<CommandExecutorService> commandExecutorServiceProvider;
    @Inject private Provider<InteractionProvider> interactionProviderProvider;
    @Inject private Provider<BookmarkService> bookmarkServiceProvider;
    @Inject private Provider<RepositoryService> repositoryServiceProvider;
    @Inject private InteractionIdGenerator interactionIdGenerator;

    private final List<InteractionListener> listeners = new ArrayList<>();
    private final Map<Class<? extends InteractionEvent>, InteractionEventDispatcher>
        dispatchersByEventClass = new HashMap<>();
    private ProxyContextHandler proxyContextHandler;

    private ExecutorService commonExecutorService;

    @PostConstruct
    public void init() {

        this.commonExecutorService = newCommonExecutorService();

        var proxyCreator = new ProxyCreator(proxyFactoryService);
        proxyContextHandler = new ProxyContextHandler(proxyCreator);

        putDispatcher(ObjectTitleEvent.class, InteractionListener::objectTitleRead);
        putDispatcher(PropertyVisibilityEvent.class, InteractionListener::propertyVisible);
        putDispatcher(PropertyUsabilityEvent.class, InteractionListener::propertyUsable);
        putDispatcher(PropertyAccessEvent.class, InteractionListener::propertyAccessed);
        putDispatcher(PropertyModifyEvent.class, InteractionListener::propertyModified);
        putDispatcher(CollectionVisibilityEvent.class, InteractionListener::collectionVisible);
        putDispatcher(CollectionUsabilityEvent.class, InteractionListener::collectionUsable);
        putDispatcher(CollectionAccessEvent.class, InteractionListener::collectionAccessed);
//XXX[CAUSEWAY-3084] - removal of collection modification events
//        putDispatcher(CollectionAddToEvent.class, InteractionListener::collectionAddedTo);
//        putDispatcher(CollectionRemoveFromEvent.class, InteractionListener::collectionRemovedFrom);
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
        return wrap(domainObject, control());
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
            var executionMode = wrapperObject.__causeway_executionModes();
            if(equivalent(executionMode, syncControl.getExecutionModes())) {
                return domainObject;
            }
            var underlyingDomainObject = wrapperObject.__causeway_wrapped();
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
            var wrapperObject = (WrappingObject) mixee;
            var executionMode = wrapperObject.__causeway_executionModes();
            var underlyingMixee = wrapperObject.__causeway_wrapped();

            getServiceInjector().injectServicesInto(underlyingMixee);

            if(equivalent(executionMode, syncControl.getExecutionModes())) {
                return mixin;
            }
            return _Casts.uncheckedCast(createMixinProxy(underlyingMixee, mixin, syncControl));
        }

        getServiceInjector().injectServicesInto(mixee);

        return createMixinProxy(mixee, mixin, syncControl);
    }

    protected <T> T createProxy(final T domainObject, final SyncControl syncControl) {
        var objAdapter = adaptAndGuardAgainstWrappingNotSupported(domainObject);
        return proxyContextHandler.proxy(domainObject, objAdapter, syncControl);
    }

    protected <T> T createMixinProxy(final Object mixee, final T mixin, final SyncControl syncControl) {
        var mixeeAdapter = adaptAndGuardAgainstWrappingNotSupported(mixee);
        var mixinAdapter = adaptAndGuardAgainstWrappingNotSupported(mixin);
        return proxyContextHandler.mixinProxy(mixin, mixeeAdapter, mixinAdapter, syncControl);
    }

    @Override
    public boolean isWrapper(final Object obj) {
        return obj instanceof WrappingObject;
    }

    @Override
    public <T> T unwrap(final T possibleWrappedDomainObject) {
        if(isWrapper(possibleWrappedDomainObject)) {
            var wrappingObject = (WrappingObject) possibleWrappedDomainObject;
            return _Casts.uncheckedCast(wrappingObject.__causeway_wrapped());
        }
        return possibleWrappedDomainObject;
    }

    // -- ASYNC WRAPPING

    @Override
    public <T,R> T asyncWrap(
            final @NonNull T domainObject,
            final AsyncControl<R> asyncControl) {

        var targetAdapter = adaptAndGuardAgainstWrappingNotSupported(domainObject);
        if(targetAdapter.getSpecification().isMixin()) {
            throw _Exceptions.illegalArgument("cannot wrap a mixin instance directly, "
                    + "use WrapperFactory.asyncWrapMixin(...) instead");
        }

        var proxyFactory = proxyFactoryService
                .<T>factory(_Casts.uncheckedCast(domainObject.getClass()), WrappingObject.class);

        return proxyFactory.createInstance((proxy, method, args) -> {

            var resolvedMethod = _GenericResolver.resolveMethod(method, domainObject.getClass())
                    .orElseThrow(); // fail early on attempt to invoke method that is not part of the meta-model

            if (isInheritedFromJavaLangObject(method)) {
                return method.invoke(domainObject, args);
            }

            if (shouldCheckRules(asyncControl)) {
                var doih = new DomainObjectInvocationHandler<>(
                        domainObject,
                        null, // mixeeAdapter ignored
                        targetAdapter,
                        control().withNoExecute(),
                        null);
                doih.invoke(null, method, args);
            }

            var memberAndTarget = memberAndTargetForRegular(resolvedMethod, targetAdapter);
            if( ! memberAndTarget.isMemberFound()) {
                return method.invoke(domainObject, args);
            }

            return submitAsync(memberAndTarget, args, asyncControl);
        }, false);
    }

    private boolean shouldCheckRules(final AsyncControl<?> asyncControl) {
        var executionModes = asyncControl.getExecutionModes();
        var skipRules = executionModes.contains(ExecutionMode.SKIP_RULE_VALIDATION);
        return !skipRules;
    }

    @Override
    public <T, R> T asyncWrapMixin(
            final @NonNull Class<T> mixinClass,
            final @NonNull Object mixee,
            final @NonNull AsyncControl<R> asyncControl) {

        T mixin = factoryService.mixin(mixinClass, mixee);

        var mixeeAdapter = adaptAndGuardAgainstWrappingNotSupported(mixee);
        var mixinAdapter = adaptAndGuardAgainstWrappingNotSupported(mixin);

        var mixinConstructor = MixinConstructor.PUBLIC_SINGLE_ARG_RECEIVING_MIXEE
                .getConstructorElseFail(mixinClass, mixee.getClass());

        var proxyFactory = proxyFactoryService
                .factory(mixinClass, new Class[]{WrappingObject.class}, mixinConstructor.getParameterTypes());

        return proxyFactory.createInstance((proxy, method, args) -> {

            var resolvedMethod = _GenericResolver.resolveMethod(method, mixinClass)
                    .orElseThrow(); // fail early on attempt to invoke method that is not part of the meta-model

            final boolean inheritedFromObject = isInheritedFromJavaLangObject(method);
            if (inheritedFromObject) {
                return method.invoke(mixin, args);
            }

            if (shouldCheckRules(asyncControl)) {
                var doih = new DomainObjectInvocationHandler<>(
                        mixin,
                        mixeeAdapter,
                        mixinAdapter,
                        control().withNoExecute(),
                        null);
                doih.invoke(null, method, args);
            }

            var actionAndTarget = memberAndTargetForMixin(resolvedMethod, mixee, mixinAdapter);
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

        var interactionLayer = currentInteractionLayer();
        var interactionContext = interactionLayer.getInteractionContext();
        var asyncInteractionContext = interactionContextFrom(asyncControl, interactionContext);

        var parentCommand = getInteractionService().currentInteractionElseFail().getCommand();
        var parentInteractionId = parentCommand.getInteractionId();

        var targetAdapter = memberAndTarget.getTarget();
        var method = memberAndTarget.getMethod();

        var head = InteractionHead.regular(targetAdapter);

        var childInteractionId = interactionIdGenerator.interactionId();
        CommandDto childCommandDto;
        switch (memberAndTarget.getType()) {
            case ACTION:
                var action = memberAndTarget.getAction();
                var argAdapters = ManagedObject.adaptParameters(action.getParameters(), _Lists.ofArray(args));
                childCommandDto = commandDtoFactory
                        .asCommandDto(childInteractionId, head, action, argAdapters);
                break;
            case PROPERTY:
                var property = memberAndTarget.getProperty();
                var propertyValueAdapter = ManagedObject.adaptProperty(property, args[0]);
                childCommandDto = commandDtoFactory
                        .asCommandDto(childInteractionId, head, property, propertyValueAdapter);
                break;
            default:
                // shouldn't happen, already catered for this case previously
                return null;
        }
        var oidDto = childCommandDto.getTargets().getOid().get(0);

        asyncControl.setMethod(method);
        asyncControl.setBookmark(Bookmark.forOidDto(oidDto));

        var executorService = Optional.ofNullable(asyncControl.getExecutorService())
                .orElse(commonExecutorService);
        var asyncTask = getServiceInjector().injectServicesInto(new AsyncTask<R>(
            asyncInteractionContext,
            Propagation.REQUIRES_NEW,
            childCommandDto,
            asyncControl.getReturnType(),
            parentInteractionId)); // this command becomes the parent of child command

        var future = executorService.submit(asyncTask);
        asyncControl.setFuture(future);

        return null;
    }

    private MemberAndTarget memberAndTargetForRegular(
            final ResolvedMethod method,
            final ManagedObject targetAdapter) {

        var objectMember = targetAdapter.getSpecification().getMember(method).orElse(null);
        if(objectMember == null) {
            return MemberAndTarget.notFound();
        }

        if (objectMember instanceof OneToOneAssociation) {
            return MemberAndTarget.foundProperty((OneToOneAssociation) objectMember, targetAdapter, method.method());
        }
        if (objectMember instanceof ObjectAction) {
            return MemberAndTarget.foundAction((ObjectAction) objectMember, targetAdapter, method.method());
        }

        throw new UnsupportedOperationException(
                "Only properties and actions can be executed in the background "
                        + "(method " + method.name() + " represents a " + objectMember.getFeatureType().name() + "')");
    }

    private <T> MemberAndTarget memberAndTargetForMixin(
            final ResolvedMethod method,
            final T mixee,
            final ManagedObject mixinAdapter) {

        var mixinMember = mixinAdapter.getSpecification().getMember(method).orElse(null);
        if (mixinMember == null) {
            return MemberAndTarget.notFound();
        }

        // find corresponding action of the mixee (this is the 'real' target, the target usable for invocation).
        var mixeeClass = mixee.getClass();

        // don't care about anything other than actions
        // (contributed properties and collections are read-only).
        final ObjectAction targetAction = getSpecificationLoader().specForType(mixeeClass)
        .flatMap(mixeeSpec->mixeeSpec.streamAnyActions(MixedIn.ONLY)
                .filter(act -> ((MixedInMember)act).hasMixinAction((ObjectAction) mixinMember))
                .findFirst()
        )
        .orElseThrow(()->new UnsupportedOperationException(String.format(
                "Could not locate objectAction delegating to mixinAction id='%s' on mixee class '%s'",
                mixinMember.getId(), mixeeClass.getName())));

        return MemberAndTarget.foundAction(targetAction, getObjectManager().adapt(mixee), method.method());
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
                || !adapter.getSpecification().getBeanSort().isWrappingSupported()) {
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

    private InteractionLayer currentInteractionLayer() {
        return getInteractionService().currentInteractionLayerElseFail();
    }

    @RequiredArgsConstructor
    private static class AsyncTask<R> implements AsyncCallable<R> {

        private static final long serialVersionUID = 1L;

        @Getter private final InteractionContext interactionContext;
        @Getter private final Propagation propagation;
        @Getter private final CommandDto commandDto;
        @Getter private final Class<R> returnType;
        @Getter private final UUID parentInteractionId;

        /**
         * Note this is a <code>transient</code> field, in order that
         * {@link org.apache.causeway.applib.services.wrapper.callable.AsyncCallable} can be declared as
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

    @Override
    public <R> R execute(final AsyncCallable<R> asyncCallable) {
        getServiceInjector().injectServicesInto(this);
        final R result = interactionServiceProvider.get()
                .call(asyncCallable.getInteractionContext(),
                        () -> updateDomainObjectHonoringTransactionalPropagation(asyncCallable));
        return result;
    }

    private <R> R updateDomainObjectHonoringTransactionalPropagation(final AsyncCallable<R> asyncCallable) {
        return transactionServiceProvider.get()
                .callTransactional(asyncCallable.getPropagation(),
                        () -> updateDomainObject(asyncCallable))
                .ifFailureFail()
                .getValue().orElse(null);
    }

    private <R> R updateDomainObject(final AsyncCallable<R> asyncCallable) {

        // obtain the Command that is implicitly created (initially mainly empty) whenever an Interaction is started.
        var childCommand = interactionProviderProvider.get().currentInteractionElseFail().getCommand();

        // we will "take over" this Command, updating it with the parentInteractionId of the command for the action
        // that called WrapperFactory#asyncMixin in the first place.
        childCommand.updater().setParentInteractionId(asyncCallable.getParentInteractionId());

        var tryBookmark = commandExecutorServiceProvider.get().executeCommand(asyncCallable.getCommandDto());

        return tryBookmark.fold(
                throwable -> null,                  // failure
                bookmarkIfAny -> bookmarkIfAny.map( // success
                    bookmark -> {
                        var spec = getSpecificationLoader().specForBookmark(bookmark).orElse(null);
                        if(spec==null) {
                            return null;
                        }
                        R domainObject = bookmarkServiceProvider.get().lookup(bookmark, asyncCallable.getReturnType()).orElse(null);
                        if(spec.isEntity()) {
                            domainObject = repositoryServiceProvider.get().detach(domainObject);
                        }
                        return domainObject;
                    }).orElse(null));
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
