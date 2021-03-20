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

package org.apache.isis.core.runtimeservices.session;

import static org.apache.isis.commons.internal.base._With.requires;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.util.schema.ChangesDtoUtils;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.commons.functional.ThrowingRunnable;
import org.apache.isis.commons.internal.concurrent._ConcurrentContext;
import org.apache.isis.commons.internal.concurrent._ConcurrentTaskList;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.interaction.integration.InteractionAwareTransactionalBoundaryHandler;
import org.apache.isis.core.interaction.scope.InteractionScopeAware;
import org.apache.isis.core.interaction.scope.InteractionScopeBeanFactoryPostProcessor;
import org.apache.isis.core.interaction.scope.InteractionScopeLifecycleHandler;
import org.apache.isis.core.interaction.session.AuthenticationLayer;
import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.core.interaction.session.InteractionSession;
import org.apache.isis.core.interaction.session.InteractionTracker;
import org.apache.isis.core.interaction.session.IsisInteraction;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.services.publishing.CommandPublisher;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.events.MetamodelEventService;
import org.apache.isis.core.security.authentication.Authentication;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Is the factory of {@link InteractionSession}s.
 * 
 * @implNote holds a reference to the current session using a thread-local
 */
@Service
@Named("isis.runtimeservices.InteractionFactoryDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class InteractionFactoryDefault 
implements 
    InteractionFactory, 
    InteractionTracker {

    @Inject AuthenticationManager authenticationManager;
    @Inject MetamodelEventService runtimeEventService;
    @Inject SpecificationLoader specificationLoader;
    @Inject MetaModelContext metaModelContext;
    @Inject IsisConfiguration configuration;
    @Inject ServiceInjector serviceInjector;
    
    @Inject InteractionAwareTransactionalBoundaryHandler txBoundaryHandler;
    @Inject ClockService clockService;
    @Inject CommandPublisher commandPublisher;
    @Inject List<InteractionScopeAware> interactionScopeAwareBeans;

    private InteractionScopeLifecycleHandler interactionScopeLifecycleHandler;

    @PostConstruct
    public void initIsisInteractionScopeSupport() {
        this.interactionScopeLifecycleHandler = InteractionScopeBeanFactoryPostProcessor
                .initIsisInteractionScopeSupport(serviceInjector);        
    }
    
    //@PostConstruct .. too early, needs services to be provisioned first
    @EventListener
    public void init(ContextRefreshedEvent event) {

        requires(authenticationManager, "authenticationManager");

        log.info("Initialising Isis System");
        log.info("working directory: {}", new File(".").getAbsolutePath());

        runtimeEventService.fireBeforeMetamodelLoading();

        val taskList = _ConcurrentTaskList.named("IsisInteractionFactoryDefault Init")
                .addRunnable("SpecificationLoader::createMetaModel", specificationLoader::createMetaModel)
                .addRunnable("ChangesDtoUtils::init", ChangesDtoUtils::init)
                .addRunnable("InteractionDtoUtils::init", InteractionDtoUtils::init)
                .addRunnable("CommandDtoUtils::init", CommandDtoUtils::init)
                ;

        taskList.submit(_ConcurrentContext.forkJoin());
        taskList.await();

        { // log any validation failures, experimental code however, not sure how to best propagate failures
            val validationResult = specificationLoader.getValidationResult();
            if(validationResult.getNumberOfFailures()==0) {
                log.info("Validation PASSED");
            } else {
                log.error("### Validation FAILED, failure count: {}", validationResult.getNumberOfFailures());
                validationResult.forEach(failure->{
                    log.error("# " + failure.getMessage());
                });
                //throw _Exceptions.unrecoverable("Validation FAILED");
            }
        }

        runtimeEventService.fireAfterMetamodelLoaded();

    }

    private final ThreadLocal<Stack<AuthenticationLayer>> authenticationStack = 
            ThreadLocal.withInitial(Stack::new);
    
    @Override
    public int getAuthenticationLayerCount() {
        return authenticationStack.get().size();
    }
    
    @Override
    public AuthenticationLayer openInteraction() {
        return currentAuthenticationLayer()
                // or else create an anonymous authentication layer
                .orElseGet(()->openInteraction(new AnonymousSession())); 
    }
    
    @Override
    public AuthenticationLayer openInteraction(final @NonNull Authentication authToUse) {

        val interactionSession = getOrCreateInteractionSession();
        
        // check whether we should reuse any current authenticationLayer, 
        // that is, if current authentication and authToUse are equal
        
        val reuseCurrentLayer = currentAuthentication()
                .map(currentAuthentication->Objects.equals(currentAuthentication, authToUse))
                .orElse(false);
        
        if(reuseCurrentLayer) {
            // we are done, just return the stack's top
            return authenticationStack.get().peek();
        }
        
        val authenticationLayer = new AuthenticationLayer(interactionSession, authToUse);
        
        authenticationStack.get().push(authenticationLayer);

        if(isInBaseLayer()) {
        	postSessionOpened(interactionSession);
        }
        
        if(log.isDebugEnabled()) {
            log.debug("new authentication layer created (conversation-id={}, total-layers-on-stack={}, {})", 
                    conversationId.get(), 
                    authenticationStack.get().size(),
                    _Probe.currentThreadId());
        }
        
        if(XrayUi.isXrayEnabled()) {
            _Xray.newAuthenticationLayer(authenticationStack.get());    
        }
        
        return authenticationLayer;
    }
    
    private InteractionSession getOrCreateInteractionSession() {
    	
    	return authenticationStack.get().isEmpty()
    			? new InteractionSession(metaModelContext, UUID.randomUUID())
				: authenticationStack.get().firstElement().getInteractionSession();
    }

    @Override
    public void closeSessionStack() {
        log.debug("about to close the authentication stack (conversation-id={}, total-layers-on-stack={}, {})", 
                conversationId.get(), 
                authenticationStack.get().size(),
                _Probe.currentThreadId());

        closeSessionStackDownToStackSize(0);
    }

	@Override
    public Optional<AuthenticationLayer> currentAuthenticationLayer() {
    	val stack = authenticationStack.get();
    	return stack.isEmpty() 
    	        ? Optional.empty() 
                : Optional.of(stack.lastElement());
    }

    @Override
    public boolean isInInteractionSession() {
        return !authenticationStack.get().isEmpty();
    }

    // -- AUTHENTICATED EXECUTION
    
    @Override
    @SneakyThrows
    public <R> R callAuthenticated(
            @NonNull final Authentication authentication, 
            @NonNull final Callable<R> callable) {
        
        final int stackSizeWhenEntering = authenticationStack.get().size();
        openInteraction(authentication);
        
        try {
            serviceInjector.injectServicesInto(callable);
            return callable.call();
        } finally {
            closeSessionStackDownToStackSize(stackSizeWhenEntering);
        }

    }
    
    @Override
    @SneakyThrows
    public void runAuthenticated(
            @NonNull final Authentication authentication, 
            @NonNull final ThrowingRunnable runnable) {
        
        final int stackSizeWhenEntering = authenticationStack.get().size();
        openInteraction(authentication);
        
        try {
            serviceInjector.injectServicesInto(runnable);
            runnable.run();
        } finally {
            closeSessionStackDownToStackSize(stackSizeWhenEntering);
        }

    }

    // -- ANONYMOUS EXECUTION
    
    @SneakyThrows
    public <R> R callAnonymous(@NonNull final Callable<R> callable) {
        if(isInInteractionSession()) {
            serviceInjector.injectServicesInto(callable);
            return callable.call(); // reuse existing session
        }
        return callAuthenticated(new AnonymousSession(), callable);
    }

    /**
     * Variant of {@link #callAnonymous(Callable)} that takes a runnable.
     * @param runnable
     */
    @SneakyThrows
    public void runAnonymous(@NonNull final ThrowingRunnable runnable) {
        if(isInInteractionSession()) {
            serviceInjector.injectServicesInto(runnable);
            runnable.run(); // reuse existing session
            return;
        }
        runAuthenticated(new AnonymousSession(), runnable);
    }

    // -- CONVERSATION ID
    
    private final ThreadLocal<UUID> conversationId = ThreadLocal.withInitial(()->null);
    
    @Override
    public Optional<UUID> getConversationId() {
        return Optional.ofNullable(conversationId.get());
    }
    
    // -- HELPER
    
    private boolean isInBaseLayer() {
    	return authenticationStack.get().size()==1; 
    }
    
    private void postSessionOpened(InteractionSession session) {
        conversationId.set(session.getInteractionId());
        interactionScopeAwareBeans.forEach(bean->bean.beforeEnteringTransactionalBoundary(session));
        txBoundaryHandler.onOpen(session);
        val isSynchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
        interactionScopeAwareBeans.forEach(bean->bean.afterEnteringTransactionalBoundary(session, isSynchronizationActive));
        interactionScopeLifecycleHandler.onTopLevelInteractionOpened();
    }
    
    private void preSessionClosed(InteractionSession session) {
        completeAndPublishCurrentCommand();
        interactionScopeLifecycleHandler.onTopLevelInteractionClosing(); // cleanup the isis-session scope
        val isSynchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
        interactionScopeAwareBeans.forEach(bean->bean.beforeLeavingTransactionalBoundary(session, isSynchronizationActive));
        txBoundaryHandler.onClose(session);
        interactionScopeAwareBeans.forEach(bean->bean.afterLeavingTransactionalBoundary(session));
        session.close(); // do this last
    }
    
    private void closeSessionStackDownToStackSize(int downToStackSize) {
        
        log.debug("about to close authenication stack down to size {} (conversation-id={}, total-sessions-on-stack={}, {})",
                downToStackSize,
                conversationId.get(), 
                authenticationStack.get().size(),
                _Probe.currentThreadId());
        
        val stack = authenticationStack.get();
        while(stack.size()>downToStackSize) {
        	if(isInBaseLayer()) {
        		// keep the stack unmodified yet, to allow for callbacks to properly operate
        		preSessionClosed(stack.peek().getInteractionSession());
        	}
        	_Xray.closeAuthenticationLayer(stack);
            stack.pop();
        }
        if(downToStackSize == 0) {
            // cleanup thread-local
            authenticationStack.remove();
            conversationId.remove();
        }
    }
    
    private IsisInteraction getInternalInteractionElseFail() {
        val interaction = currentInteractionElseFail();
        if(interaction instanceof IsisInteraction) {
            return (IsisInteraction) interaction;
        }
        throw _Exceptions.unrecoverableFormatted("the framework does not recognice "
                + "this implementation of an Interaction: %s", interaction.getClass().getName());
    }
    
    // -- HELPER - COMMAND COMPLETION
    
    private void completeAndPublishCurrentCommand() {

        val interaction = getInternalInteractionElseFail();
        val command = interaction.getCommand();

        if(command.getStartedAt() != null && command.getCompletedAt() == null) {
            // the guard is in case we're here as the result of a redirect following a previous exception;just ignore.

            val priorInteractionExecution = interaction.getPriorExecution();
            final Timestamp completedAt =
                    priorInteractionExecution != null
                    ?
                        // copy over from the most recent (which will be the top-level) interaction
                        priorInteractionExecution.getCompletedAt()
                    :
                        // this could arise as the result of calling SessionManagementService#nextSession within an action
                        // the best we can do is to use the current time

                        // REVIEW: as for the interaction object, it is left somewhat high-n-dry.
                         clockService.getClock().javaSqlTimestamp();

            command.updater().setCompletedAt(completedAt);
        }

        commandPublisher.complete(command);

        interaction.clear();
    }


}
