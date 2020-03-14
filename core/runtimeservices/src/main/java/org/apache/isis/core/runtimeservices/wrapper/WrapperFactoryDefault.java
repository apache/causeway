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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.command.CommandExecutorService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;
import org.apache.isis.applib.services.wrapper.control.AsyncControlService;
import org.apache.isis.applib.services.wrapper.control.RuleCheckingPolicy;
import org.apache.isis.applib.services.wrapper.control.ExecutionMode;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrappingObject;
import org.apache.isis.applib.services.wrapper.control.ExecutionModes;
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
import org.apache.isis.core.commons.internal.plugins.codegen.ProxyFactory;
import org.apache.isis.core.commons.internal.plugins.codegen.ProxyFactoryService;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.runtime.session.IsisSessionTracker;
import org.apache.isis.core.runtimeservices.wrapper.dispatchers.InteractionEventDispatcher;
import org.apache.isis.core.runtimeservices.wrapper.dispatchers.InteractionEventDispatcherTypeSafe;
import org.apache.isis.core.runtimeservices.wrapper.handlers.ProxyContextHandler;
import org.apache.isis.core.runtimeservices.wrapper.proxy.ProxyCreator;
import org.apache.isis.schema.cmd.v2.CommandDto;

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
    @Inject private BookmarkService bookmarkService;
    @Inject private MetaModelContext metaModelContext;
    @Inject private SpecificationLoader specificationLoader;
    @Inject private IsisSessionTracker isisSessionTracker;
    @Inject private IsisSessionFactory isisSessionFactory;
    @Inject private CommandExecutorService commandExecutorService;
    @Inject private Provider<CommandContext> commandContextProvider;
    @Inject private TransactionService transactionService;
    @Inject protected ProxyFactoryService proxyFactoryService; // protected to allow JUnit test
    @Inject private CommandDtoServiceInternal commandDtoServiceInternal;

    private final List<InteractionListener> listeners = new ArrayList<InteractionListener>();
    private final Map<Class<? extends InteractionEvent>, InteractionEventDispatcher>
        dispatchersByEventClass = 
            new HashMap<Class<? extends InteractionEvent>, InteractionEventDispatcher>();
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
    public <T> T wrapMixin(Class<T> mixinClass, Object mixedIn) {
        return wrap(factoryService.mixin(mixinClass, mixedIn));
    }

    @Override
    public <T> T wrap(T domainObject) {
        return wrap(domainObject, ExecutionModes.EXECUTE);
    }

    @Override
    public <T> T wrapTry(T domainObject) {
        return wrap(domainObject, ExecutionModes.TRY);
    }

    @Override
    public <T> T wrapNoExecute(T domainObject) {
        return wrap(domainObject, ExecutionModes.NO_EXECUTE);
    }

    @Override
    public <T> T wrapSkipRules(T domainObject) {
        return wrap(domainObject, ExecutionModes.SKIP_RULES);
    }

    @Override
    public <T> T wrap(
            final T domainObject,
            final ImmutableEnumSet<ExecutionMode> modes) {
        if (domainObject instanceof WrappingObject) {
            val wrapperObject = (WrappingObject) domainObject;
            val executionMode = wrapperObject.__isis_executionMode();
            if(executionMode != modes) {
                val underlyingDomainObject = wrapperObject.__isis_wrapped();
                return _Casts.uncheckedCast(createProxy(underlyingDomainObject, modes));
            }
            return domainObject;
        }
        return createProxy(domainObject, modes);
    }

    protected <T> T createProxy(T domainObject, ImmutableEnumSet<ExecutionMode> modes) {
        return proxyContextHandler.proxy(metaModelContext, domainObject, modes);
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

    @Inject
    AsyncControlService asyncControlService;

    @Override
    public <T,R> T async(
            final T domainObject,
            final AsyncControl<R> asyncControl) {

        final ImmutableEnumSet<ExecutionMode> executionModes = asyncControl.getExecutionModes();
        final RuleCheckingPolicy ruleCheckingPolicy = asyncControl.getRuleCheckingPolicy();
        final ExecutorService executorService = asyncControl.getExecutorService();

        Class<T> domainClass = (Class<T>) domainObject.getClass();
        ProxyFactory<T> factory = proxyFactoryService.factory(domainClass, WrappingObject.class);

        return factory.createInstance((Object proxy, Method method, Object[] args) -> {

            val executionModesAsync = determineExecutionModesAsync(executionModes, ruleCheckingPolicy);
            if (!shouldValidateAsync(executionModesAsync) && !shouldExecuteAsync(executionModesAsync)) {
                // nothing to be done.
                return null;
            }

            //
            // executed in the same thread as caller..
            //
            final boolean inheritedFromObject = method.getDeclaringClass().equals(Object.class);
            if(inheritedFromObject) {
                return method.invoke(domainObject, args);
            }

            val executionModesSync = determineExecutionModesSync(executionModes, ruleCheckingPolicy);
            if(shouldValidateSync(executionModesSync)) {
                // normal wrapped object...
                T syncProxy = createProxy(domainObject, executionModesSync);
                method.invoke(syncProxy, args);
            }

            //
            // submit async stuff
            //
            final ObjectSpecificationDefault targetObjSpec = (ObjectSpecificationDefault) specificationLoader.loadSpecification(method.getDeclaringClass());
            final ObjectMember member = targetObjSpec.getMember(method);

            if(member == null) {
                return method.invoke(domainObject, args);
            }

            if(!(member instanceof ObjectAction)) {
                throw new UnsupportedOperationException(
                        "Only actions can be executed in the background "
                                + "(method " + method.getName() + " represents a " + member.getFeatureType().name() + "')");
            }

            ObjectAction action = (ObjectAction) member;

            val isisSession = isisSessionTracker.currentSession().orElseThrow(() -> new RuntimeException("No IsisSession is open"));
            val authSession = isisSession.getAuthenticationSession();

            final ManagedObject domainObjectAdapter = isisSession.getObjectManager().adapt(domainObject);
            final List<ManagedObject> argAdapters = Arrays.asList(adaptersFor(args));

            final List<ManagedObject> targetList = Collections.singletonList(domainObjectAdapter);
            final CommandDto dto =
                    commandDtoServiceInternal.asCommandDto(targetList, action, argAdapters);
            asyncControlService.init(asyncControl, method, dto.getTargets().getOid().get(0));

            Future<?> submit = executorService.submit(() -> {

                isisSessionFactory.runAuthenticated(authSession, () -> {

                    if (shouldValidateAsync(executionModesAsync)) {
                        // TODO...
                    }
                    if (shouldExecuteAsync(executionModesAsync)) {
                        commandExecutorService.executeCommand(dto);
                    }

                });
            });
            asyncControlService.update(asyncControl, submit);
            return null;


        }, false);
    }

    @Override
    public <T> T asyncMixin(
            Class<T> mixinClass, Object mixedIn,
            ImmutableEnumSet<ExecutionMode> modes,
            RuleCheckingPolicy ruleCheckingPolicy,
            ExecutorService executorService) {

        throw new RuntimeException("TODO");
    }

    private ManagedObject[] adaptersFor(final Object[] args) {
        final ObjectManager objectManager =
                isisSessionTracker.currentSession().get().getObjectManager();
        return CommandUtil.adaptersFor(args, objectManager);
    }

    private static ImmutableEnumSet<ExecutionMode> determineExecutionModesSync(
            ImmutableEnumSet<ExecutionMode> executionModes,
            RuleCheckingPolicy ruleCheckingPolicy) {
        return executionModes.contains(ExecutionMode.SKIP_RULE_VALIDATION) ||
                ruleCheckingPolicy != RuleCheckingPolicy.IMMEDIATE
                ? ExecutionModes.NOOP
                : ExecutionModes.NO_EXECUTE;
    }

    private static ImmutableEnumSet<ExecutionMode> determineExecutionModesAsync(
            ImmutableEnumSet<ExecutionMode> executionModes,
            RuleCheckingPolicy ruleCheckingPolicy) {
        return executionModes.contains(ExecutionMode.SKIP_RULE_VALIDATION) ||
                RuleCheckingPolicy.IMMEDIATE == ruleCheckingPolicy
                ? join(executionModes, ExecutionMode.SKIP_RULE_VALIDATION)
                : executionModes;
    }

    private static ImmutableEnumSet<ExecutionMode> join(
            ImmutableEnumSet<ExecutionMode> executionModes,
            ExecutionMode skipRuleValidation) {
        val tmp = executionModes.toEnumSet();
        tmp.add(skipRuleValidation);
        return ImmutableEnumSet.from(tmp);
    }

    private boolean shouldValidateSync(ImmutableEnumSet<ExecutionMode> executionModesSync) {
        return !executionModesSync.contains(ExecutionMode.SKIP_RULE_VALIDATION);
    }

    private boolean shouldValidateAsync(ImmutableEnumSet<ExecutionMode> executionModesAsync) {
        return !executionModesAsync.contains(ExecutionMode.SKIP_RULE_VALIDATION);
    }

    private boolean shouldExecuteAsync(ImmutableEnumSet<ExecutionMode> executionModesAsync) {
        return !executionModesAsync.contains(ExecutionMode.SKIP_EXECUTION);
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
