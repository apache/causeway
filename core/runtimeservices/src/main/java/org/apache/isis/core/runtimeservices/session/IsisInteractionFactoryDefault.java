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
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.util.schema.ChangesDtoUtils;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.concurrent._ConcurrentContext;
import org.apache.isis.commons.internal.concurrent._ConcurrentTaskList;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.events.RuntimeEventService;
import org.apache.isis.core.runtime.iactn.InteractionClosure;
import org.apache.isis.core.runtime.iactn.InteractionSession;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.core.runtime.iactn.IsisInteractionTracker;
import org.apache.isis.core.runtime.iactn.scope.IsisInteractionScopeBeanFactoryPostProcessor;
import org.apache.isis.core.runtime.iactn.scope.IsisInteractionScopeCloseListener;
import org.apache.isis.core.runtime.session.init.InitialisationSession;
import org.apache.isis.core.runtime.session.init.IsisLocaleInitializer;
import org.apache.isis.core.runtime.session.init.IsisTimeZoneInitializer;
import org.apache.isis.core.runtimeservices.user.UserServiceDefault;
import org.apache.isis.core.runtimeservices.user.UserServiceDefault.UserAndRoleOverrides;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authentication.standard.SimpleSession;

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
@Named("isisRuntime.IsisInteractionFactoryDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class IsisInteractionFactoryDefault implements IsisInteractionFactory, IsisInteractionTracker {

    @Inject AuthenticationManager authenticationManager;
    @Inject RuntimeEventService runtimeEventService;
    @Inject SpecificationLoader specificationLoader;
    @Inject MetaModelContext metaModelContext;
    @Inject IsisConfiguration configuration;
    @Inject ServiceInjector serviceInjector;
    @Inject Provider<InteractionContext> interactionContextProvider;

    private IsisLocaleInitializer localeInitializer;
    private IsisTimeZoneInitializer timeZoneInitializer;
    private IsisInteractionScopeCloseListener isisInteractionScopeCloseListener;

    @PostConstruct
    public void initIsisInteractionScopeSupport() {
        this.isisInteractionScopeCloseListener = IsisInteractionScopeBeanFactoryPostProcessor.initIsisInteractionScopeSupport(serviceInjector);        
    }
    
    //@PostConstruct .. too early, needs services to be provisioned first
    @EventListener
    public void init(ContextRefreshedEvent event) {

        requires(authenticationManager, "authenticationManager");

        this.localeInitializer = new IsisLocaleInitializer();
        this.timeZoneInitializer = new IsisTimeZoneInitializer();

        log.info("Initialising Isis System");
        log.info("working directory: {}", new File(".").getAbsolutePath());

        localeInitializer.initLocale(configuration);
        timeZoneInitializer.initTimeZone(configuration);

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

    private final ThreadLocal<Stack<InteractionClosure>> interactionClosureStack = 
            ThreadLocal.withInitial(Stack::new);
    
    @Override
    public InteractionClosure openInteraction(final @NonNull AuthenticationSession authenticationSession) {

        val authSessionToUse = getAuthenticationSessionOverride()
                .orElse(authenticationSession);
        
        val interactionSession = getOrCreateInteractionSession(authSessionToUse);
        val newInteractionClosure = new InteractionClosure(interactionSession, authSessionToUse);
        
        interactionClosureStack.get().push(newInteractionClosure);

        initializeApplibCommandAndInteraction();

        if(isInTopLevelClosure()) {
        	postSessionOpened(interactionSession);
        }
        
        if(log.isDebugEnabled()) {
            log.debug("new InteractionClosure created (conversation-id={}, total-sessions-on-stack={}, {})", 
                    conversationId.get(), 
                    interactionClosureStack.get().size(),
                    _Probe.currentThreadId());
        }
        
        return newInteractionClosure;
    }
    
    private InteractionSession getOrCreateInteractionSession(
    		final @NonNull AuthenticationSession authSessionToUse) {
    	
    	return interactionClosureStack.get().isEmpty()
    			? new InteractionSession(metaModelContext, authSessionToUse)
				: interactionClosureStack.get().firstElement().getInteractionSession();
    }
    

    private void initializeApplibCommandAndInteraction() {

        val command = new Command();
        val interaction = new Interaction(command);

        interactionContextProvider.get().setInteraction(interaction);
    }

    @Override
    public void closeSessionStack() {
        log.debug("about to close IsisInteraction stack (conversation-id={}, total-sessions-on-stack={}, {})", 
                conversationId.get(), 
                interactionClosureStack.get().size(),
                _Probe.currentThreadId());

        closeInteractionStackDownToStackSize(0);
    }

	@Override
    public Optional<InteractionClosure> currentInteractionClosure() {
    	val stack = interactionClosureStack.get();
    	return stack.isEmpty() ? Optional.empty() : Optional.of(stack.lastElement());
    }

    @Override
    public boolean isInInteractionSession() {
        return !interactionClosureStack.get().isEmpty();
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
        
        final int stackSizeWhenEntering = interactionClosureStack.get().size();
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
    	return interactionClosureStack.get().size()==1; 
    }
    
    private void postSessionOpened(InteractionSession newIsisInteraction) {
        conversationId.set(UUID.randomUUID());
        runtimeEventService.fireInteractionHasStarted(newIsisInteraction); // only fire on top-level session
    }
    
    private void preSessionClosed(InteractionSession isisInteraction) {
        runtimeEventService.fireInteractionIsEnding(isisInteraction); // only fire on top-level session 
        isisInteractionScopeCloseListener.preTopLevelIsisInteractionClose(); // cleanup the isis-session scope
        isisInteraction.close(); // do this last
    }
    
    private void closeInteractionStackDownToStackSize(int downToStackSize) {
        
        log.debug("about to close IsisInteraction stack down to size {} (conversation-id={}, total-sessions-on-stack={}, {})",
                downToStackSize,
                conversationId.get(), 
                interactionClosureStack.get().size(),
                _Probe.currentThreadId());
        
        val stack = interactionClosureStack.get();
        while(stack.size()>downToStackSize) {
        	if(isInTopLevelClosure()) {
        		// keep the stack unmodified yet, to allow for callbacks to properly operate
        		preSessionClosed(stack.peek().getInteractionSession());
        	}
            stack.pop();    
        }
        if(downToStackSize == 0) {
            // cleanup thread-local
            interactionClosureStack.remove();
            conversationId.remove();
        }
    }
    
    // -- HELPER - SUDO SUPPORT 

    @Inject private UserServiceDefault userServiceDefault;
    
    /**
     * Checks if there are overrides, and if so return a {@link SimpleSession} to represent those
     * overrides.
     */
    private Optional<AuthenticationSession> getAuthenticationSessionOverride() {

        // if user/role has been overridden by SudoService, then honor that value.
        final UserAndRoleOverrides userAndRoleOverrides = userServiceDefault.currentOverridesIfAny();

        if(userAndRoleOverrides != null) {
            String user = userAndRoleOverrides.getUser();
            Can<String> roles = userAndRoleOverrides.getRoles();
            return Optional.of(new SimpleSession(user, roles));
        }

        // otherwise...
        return Optional.empty();
    }


    


}
