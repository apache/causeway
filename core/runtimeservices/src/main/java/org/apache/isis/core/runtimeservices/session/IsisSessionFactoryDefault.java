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
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.util.schema.ChangesDtoUtils;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.concurrent._ConcurrentContext;
import org.apache.isis.core.commons.internal.concurrent._ConcurrentTaskList;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.session.RuntimeEventService;
import org.apache.isis.core.runtime.session.IsisSession;
import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.runtime.session.IsisSessionTracker;
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
 * Is the factory of {@link IsisSession}s, also holding a reference to the current session using
 * a thread-local.
 *
 * <p>
 *     The class is considered as analogous to (and is in many ways a wrapper for) a JDO
 *     <code>PersistenceManagerFactory</code>.
 * </p>
 *
 */
@Service
@Named("isisRuntime.IsisSessionFactoryDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class IsisSessionFactoryDefault implements IsisSessionFactory, IsisSessionTracker {

    @Inject private AuthenticationManager authenticationManager;
    @Inject private RuntimeEventService runtimeEventService;
    @Inject private SpecificationLoader specificationLoader;
    @Inject private MetaModelContext metaModelContext;
    @Inject private IsisConfiguration configuration;
    //@Inject private FactoryService factoryService;

    private IsisLocaleInitializer localeInitializer;
    private IsisTimeZoneInitializer timeZoneInitializer;
    private final Object $lock = new Object[0];

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

        val taskList = _ConcurrentTaskList.named("IsisSessionFactoryDefault Init")
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

    private final ThreadLocal<Stack<IsisSession>> isisSessionStack = ThreadLocal.withInitial(Stack::new);
    
    @Override
    public IsisSession openSession(@NonNull final AuthenticationSession authenticationSession) {
        synchronized($lock) {
            
            val isisSession = getAuthenticationSessionOverride()
            .map(authenticationSessionOverride->new IsisSession(metaModelContext, authenticationSessionOverride))
            .orElseGet(()->new IsisSession(metaModelContext, authenticationSession));
            
            isisSessionStack.get().push(isisSession);
            if(isisSessionStack.get().size()==1) {
                runtimeEventService.fireSessionOpened(isisSession); // only fire on top-level session    
            }
            return isisSession;
        }
    }

    @Override
    public void closeSessionStack() {
        closeSessionStackDownToStackSize(0);
    }
    
    @Override
    public Optional<IsisSession> currentSession() {
        val stack = isisSessionStack.get();
        return stack.isEmpty() ? Optional.empty() : Optional.of(stack.lastElement());
    }

    @Override
    public boolean isInSession() {
        return !isisSessionStack.get().isEmpty();
    }

    @Override
    public boolean isInTransaction() {

        return currentSession().map(isisSession->{
            if (isisSession.getCurrentTransactionId() != null) {
                if (!isisSession.getCurrentTransactionState().isComplete()) {
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
        
        final int stackSizeWhenEntering = isisSessionStack.get().size();
        openSession(authenticationSession);
        
        try {
            return callable.call();
        } finally {
            closeSessionStackDownToStackSize(stackSizeWhenEntering);
        }

    }

    // -- HELPER - SESSION STACK CLOSING
    
    private void closeSessionStackDownToStackSize(int downToStackSize) {
        synchronized($lock) {
            val stack = isisSessionStack.get();
            while(stack.size()>downToStackSize) {
                val isisSession = stack.pop();
                if(stack.isEmpty()) {                
                    runtimeEventService.fireSessionClosing(isisSession); // only fire on top-level session 
                }
            }
            if(downToStackSize == 0) {
                isisSessionStack.remove();
            }
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
