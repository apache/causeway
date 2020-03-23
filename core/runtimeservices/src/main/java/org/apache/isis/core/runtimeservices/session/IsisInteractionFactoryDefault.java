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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.util.schema.ChangesDtoUtils;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.concurrent._ConcurrentContext;
import org.apache.isis.core.commons.internal.concurrent._ConcurrentTaskList;
import org.apache.isis.core.commons.internal.debug._Probe;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.session.RuntimeEventService;
import org.apache.isis.core.runtime.iactn.IsisInteraction;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.core.runtime.iactn.IsisInteractionTracker;
import org.apache.isis.core.runtime.iactn.scope.IsisInteractionScopeBeanFactoryPostProcessor;
import org.apache.isis.core.runtime.iactn.scope.IsisInteractionScopeCloseListener;
import org.apache.isis.core.runtime.session.init.IsisLocaleInitializer;
import org.apache.isis.core.runtime.session.init.IsisTimeZoneInitializer;
import org.apache.isis.core.runtimeservices.user.UserServiceDefault;
import org.apache.isis.core.runtimeservices.user.UserServiceDefault.UserAndRoleOverrides;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authentication.standard.SimpleSession;

import static org.apache.isis.core.commons.internal.base._With.requires;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Is the factory of {@link IsisInteraction}s, also holding a reference to the current session using
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

    @Inject private AuthenticationManager authenticationManager;
    @Inject private RuntimeEventService runtimeEventService;
    @Inject private SpecificationLoader specificationLoader;
    @Inject private MetaModelContext metaModelContext;
    @Inject private IsisConfiguration configuration;
    @Inject private ServiceInjector serviceInjector;
    //@Inject private FactoryService factoryService;

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

    private final ThreadLocal<Stack<IsisInteraction>> isisInteractionStack = ThreadLocal.withInitial(Stack::new);
    
    @Override
    public IsisInteraction openSession(@NonNull final AuthenticationSession authenticationSession) {

        val authSessionToUse = getAuthenticationSessionOverride()
                .orElse(authenticationSession);
        val newIsisInteraction = new IsisInteraction(metaModelContext, authSessionToUse);
        
        isisInteractionStack.get().push(newIsisInteraction);
        postOpen(isisInteractionStack.get().size(), newIsisInteraction);
        
        log.debug("new IsisInteraction created (conversation-id={}, total-sessions-on-stack={}, {})", 
                conversationId.get(), 
                isisInteractionStack.get().size(),
                _Probe.currentThreadId());
        
        return newIsisInteraction;
        
    }

    @Override
    public void closeSessionStack() {
        log.debug("about to close IsisInteraction stack (conversation-id={}, total-sessions-on-stack={}, {})", 
                conversationId.get(), 
                isisInteractionStack.get().size(),
                _Probe.currentThreadId());
        closeSessionStackDownToStackSize(0);
    }
    
    @Override
    public Optional<IsisInteraction> currentInteraction() {
        val stack = isisInteractionStack.get();
        return stack.isEmpty() ? Optional.empty() : Optional.of(stack.lastElement());
    }

    @Override
    public boolean isInInteraction() {
        return !isisInteractionStack.get().isEmpty();
    }

    @Override
    public boolean isInTransaction() {

        return currentInteraction().map(isisInteraction->{
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
        
        final int stackSizeWhenEntering = isisInteractionStack.get().size();
        openSession(authenticationSession);
        
        try {
            return callable.call();
        } finally {
            closeSessionStackDownToStackSize(stackSizeWhenEntering);
        }

    }

    private final ThreadLocal<UUID> conversationId = ThreadLocal.withInitial(()->null);
    
    @Override
    public Optional<String> getConversationId() {
        return Optional.ofNullable(conversationId.get())
                .map(UUID::toString);
    }
    
    // -- HELPER
    
    private void postOpen(int stackSizeAfterOpen, IsisInteraction newIsisInteraction) {
        final boolean isTopLevel = stackSizeAfterOpen==1;
        if(isTopLevel) {
            conversationId.set(UUID.randomUUID());
            runtimeEventService.fireSessionOpened(newIsisInteraction); // only fire on top-level session
        }
    }
    
    private void preClose(int stackSizeBeforeClose, IsisInteraction isisInteraction) {
        final boolean isTopLevel = stackSizeBeforeClose==1;
        if(isTopLevel) {
            runtimeEventService.fireSessionClosing(isisInteraction); // only fire on top-level session 
            isisInteractionScopeCloseListener.preTopLevelIsisInteractionClose(); // cleanup the isis-session scope
        }
        isisInteraction.close(); // do this last
    }
    
    private void closeSessionStackDownToStackSize(int downToStackSize) {
        
        log.debug("about to close IsisInteraction stack down to size {} (conversation-id={}, total-sessions-on-stack={}, {})",
                downToStackSize,
                conversationId.get(), 
                isisInteractionStack.get().size(),
                _Probe.currentThreadId());
        
        val stack = isisInteractionStack.get();
        while(stack.size()>downToStackSize) {
            preClose(stack.size(), stack.peek()); // keep the stack unmodified yet, to allow for callbacks to properly operate
            stack.pop();    
        }
        if(downToStackSize == 0) {
            // cleanup thread-local
            isisInteractionStack.remove();
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
