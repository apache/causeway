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

import java.io.File;
import java.sql.Timestamp;
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

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.util.schema.ChangesDtoUtils;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.commons.internal.concurrent._ConcurrentContext;
import org.apache.isis.commons.internal.concurrent._ConcurrentTaskList;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.services.publishing.CommandPublisher;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.events.RuntimeEventService;
import org.apache.isis.core.runtime.iactn.InteractionFactory;
import org.apache.isis.core.runtime.iactn.InteractionLayer;
import org.apache.isis.core.runtime.iactn.InteractionSession;
import org.apache.isis.core.runtime.iactn.InteractionTracker;
import org.apache.isis.core.runtime.iactn.IsisInteraction;
import org.apache.isis.core.runtime.iactn.scope.IsisInteractionScopeBeanFactoryPostProcessor;
import org.apache.isis.core.runtime.iactn.scope.IsisInteractionScopeCloseListener;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Is the factory of {@link InteractionSession}s, also holding a reference to the current session using
 * a thread-local.
 *
 * <p>
 *     The class is considered as analogous to (and is in many ways a wrapper for) a JDO
 *     <code>PersistenceManagerFactory</code>.
 * </p>
 *
 */
@Service
@Named("isisRuntime.InteractionFactoryDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class InteractionFactoryDefault 
implements InteractionFactory, InteractionTracker {

    @Inject AuthenticationManager authenticationManager;
    @Inject RuntimeEventService runtimeEventService;
    @Inject SpecificationLoader specificationLoader;
    @Inject MetaModelContext metaModelContext;
    @Inject IsisConfiguration configuration;
    @Inject ServiceInjector serviceInjector;
    
    @Inject ClockService clockService;
    @Inject CommandPublisher commandPublisher;

    private IsisInteractionScopeCloseListener isisInteractionScopeCloseListener;

    @PostConstruct
    public void initIsisInteractionScopeSupport() {
        this.isisInteractionScopeCloseListener = IsisInteractionScopeBeanFactoryPostProcessor.initIsisInteractionScopeSupport(serviceInjector);        
    }
    
    //@PostConstruct .. too early, needs services to be provisioned first
    @EventListener
    public void init(ContextRefreshedEvent event) {

        requires(authenticationManager, "authenticationManager");

        log.info("Initialising Isis System");
        log.info("working directory: {}", new File(".").getAbsolutePath());

        runtimeEventService.fireAppPreMetamodel();

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

        runtimeEventService.fireAppPostMetamodel();

    }

    private final ThreadLocal<Stack<InteractionLayer>> interactionLayerStack = 
            ThreadLocal.withInitial(Stack::new);
    
    @Override
    public InteractionLayer openInteraction() {
        return openInteraction(new InitialisationSession());
    }
    
    @Override
    public InteractionLayer openInteraction(final @NonNull AuthenticationSession authSessionToUse) {

        val interactionSession = getOrCreateInteractionSession();
        val newInteractionClosure = new InteractionLayer(interactionSession, authSessionToUse);
        
        interactionLayerStack.get().push(newInteractionClosure);

        if(isInTopLevelClosure()) {
        	postSessionOpened(interactionSession);
        }
        
        if(log.isDebugEnabled()) {
            log.debug("new InteractionClosure created (conversation-id={}, total-sessions-on-stack={}, {})", 
                    conversationId.get(), 
                    interactionLayerStack.get().size(),
                    _Probe.currentThreadId());
        }
        
        return newInteractionClosure;
    }
    
    private InteractionSession getOrCreateInteractionSession() {
    	
    	return interactionLayerStack.get().isEmpty()
    			? new InteractionSession(metaModelContext)
				: interactionLayerStack.get().firstElement().getInteractionSession();
    }

    @Override
    public void closeSessionStack() {
        log.debug("about to close IsisInteraction stack (conversation-id={}, total-sessions-on-stack={}, {})", 
                conversationId.get(), 
                interactionLayerStack.get().size(),
                _Probe.currentThreadId());

        closeInteractionStackDownToStackSize(0);
    }

	@Override
    public Optional<InteractionLayer> currentInteractionLayer() {
    	val stack = interactionLayerStack.get();
    	return stack.isEmpty() ? Optional.empty() : Optional.of(stack.lastElement());
    }

    @Override
    public boolean isInInteractionSession() {
        return !interactionLayerStack.get().isEmpty();
    }

    @Override
    public boolean isInTransaction() {

        return currentInteractionSession().map(isisInteraction->{
            if (isisInteraction.getCurrentTransactionId() != null) {
                if (!isisInteraction.getCurrentTransactionState().isComplete()) {
                    return true;
                }
            }
            return false;
        })
        .orElse(false);

    }

    @Override
    @SneakyThrows
    public <R> R callAuthenticated(
            @NonNull final AuthenticationSession authenticationSession, 
            @NonNull final Callable<R> callable) {
        
        final int stackSizeWhenEntering = interactionLayerStack.get().size();
        openInteraction(authenticationSession);
        
        try {
            serviceInjector.injectServicesInto(callable);
            return callable.call();
        } finally {
            closeInteractionStackDownToStackSize(stackSizeWhenEntering);
        }

    }

    @SneakyThrows
    public <R> R callAnonymous(Callable<R> callable) {
        if(isInInteractionSession()) {
            serviceInjector.injectServicesInto(callable);
            return callable.call(); // reuse existing session
        }
        return callAuthenticated(new InitialisationSession(), callable);
    }

    /**
     * Variant of {@link #callAnonymous(Callable)} that takes a runnable.
     * @param runnable
     */
    @SneakyThrows
    public void runAnonymous(ThrowingRunnable runnable) {
        if(isInInteractionSession()) {
            serviceInjector.injectServicesInto(runnable);
            runnable.run(); // reuse existing session
            return;
        }
        runAuthenticated(new InitialisationSession(), runnable);
    }

    private final ThreadLocal<UUID> conversationId = ThreadLocal.withInitial(()->null);
    
    @Override
    public Optional<String> getConversationId() {
        return Optional.ofNullable(conversationId.get())
                .map(UUID::toString);
    }
    
    // -- HELPER
    
    private boolean isInTopLevelClosure() {
    	return interactionLayerStack.get().size()==1; 
    }
    
    private void postSessionOpened(InteractionSession newIsisInteraction) {
        conversationId.set(UUID.randomUUID());
        runtimeEventService.fireInteractionHasStarted(newIsisInteraction); // only fire on top-level session
    }
    
    private void preSessionClosed(InteractionSession isisInteraction) {
        completeAndPublishCurrentCommand();
        runtimeEventService.fireInteractionIsEnding(isisInteraction); // only fire on top-level session 
        isisInteractionScopeCloseListener.preTopLevelIsisInteractionClose(); // cleanup the isis-session scope
        isisInteraction.close(); // do this last
    }
    
    private void closeInteractionStackDownToStackSize(int downToStackSize) {
        
        log.debug("about to close IsisInteraction stack down to size {} (conversation-id={}, total-sessions-on-stack={}, {})",
                downToStackSize,
                conversationId.get(), 
                interactionLayerStack.get().size(),
                _Probe.currentThreadId());
        
        val stack = interactionLayerStack.get();
        while(stack.size()>downToStackSize) {
        	if(isInTopLevelClosure()) {
        		// keep the stack unmodified yet, to allow for callbacks to properly operate
        		preSessionClosed(stack.peek().getInteractionSession());
        	}
            stack.pop();    
        }
        if(downToStackSize == 0) {
            // cleanup thread-local
            interactionLayerStack.remove();
            conversationId.remove();
        }
    }
    
    private IsisInteraction getInternalInteractionElseFail() {
        val interaction = getInteractionElseFail();
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
