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

package org.apache.isis.runtime.system.session;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.concurrent._ConcurrentContext;
import org.apache.isis.commons.internal.concurrent._ConcurrentTaskList;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.config.internal._Config;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.context.session.RuntimeEventService;
import org.apache.isis.runtime.system.internal.IsisLocaleInitializer;
import org.apache.isis.runtime.system.internal.IsisTimeZoneInitializer;
import org.apache.isis.schema.utils.ChangesDtoUtils;
import org.apache.isis.schema.utils.CommandDtoUtils;
import org.apache.isis.schema.utils.InteractionDtoUtils;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authentication.manager.AuthenticationManager;

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
@Service @Log4j2 
public class IsisSessionFactoryDefault implements IsisSessionFactory {

    @Inject private AuthenticationManager authenticationManager;
    @Inject private RuntimeEventService runtimeEventService;
    @Inject private SpecificationLoader specificationLoader;

    private IsisLocaleInitializer localeInitializer;
    private IsisTimeZoneInitializer timeZoneInitializer;

    //@PostConstruct .. too early, needs services to be provisioned first
    @EventListener
    public void init(ContextRefreshedEvent event) {
        this.localeInitializer = new IsisLocaleInitializer();
        this.timeZoneInitializer = new IsisTimeZoneInitializer();

        log.info("Initialising Isis System");
        log.info("working directory: {}", new File(".").getAbsolutePath());

        final IsisConfigurationLegacy configuration = _Config.getConfiguration();
        //log.info("resource stream source: {}", configuration.getResourceStreamSource());

        localeInitializer.initLocale(configuration);
        timeZoneInitializer.initTimeZone(configuration);

        runtimeEventService.fireAppPreMetamodel();

        val taskList = _ConcurrentTaskList.named("IsisSessionFactoryDefault Init")
        
        .addRunnable("SpecificationLoader::createMetaModel", specificationLoader::createMetaModel)
        .addRunnable("ChangesDtoUtils::init", ChangesDtoUtils::init)
        .addRunnable("InteractionDtoUtils::init", InteractionDtoUtils::init)
        .addRunnable("CommandDtoUtils::init", CommandDtoUtils::init)
        .addRunnable("AuthenticationManager::init", authenticationManager::init)
        .addRunnable("AuthorizationManager::init", IsisContext.getAuthorizationManager()::init);

        taskList.submit(_ConcurrentContext.forkJoin());
        taskList.await();

        runtimeEventService.fireAppPostMetamodel();

    }

    @PreDestroy
    public void shutdown() {
        // call might originate from a different thread than main

        // just in case we still have an open session, must also work if called from a different thread than 'main'
        openSessions.forEach(IsisSession::close);
        openSessions.clear();

        runtimeEventService.fireAppPreDestroy();
        authenticationManager.shutdown();
        IsisContext.getAuthorizationManager().shutdown();
        //specificationLoader.shutdown(); // lifecycle is managed by IoC
    }

    // -- 

    private final Set<IsisSession> openSessions = _Sets.newHashSet();

    @Override
    public IsisSession openSession(final AuthenticationSession authenticationSession) {

        closeSession();

        val isisSession = new IsisSession(runtimeEventService, authenticationSession);
        isisSession.open();
        openSessions.add(isisSession);
        return isisSession;
    }

    @Override
    public void closeSession() {
        final IsisSession existingSessionIfAny = getCurrentSession();
        if (existingSessionIfAny == null) {
            _Context.threadLocalCleanup(); // just in case, to have a well defined post condition here
            return;
        }
        existingSessionIfAny.close();
        openSessions.remove(existingSessionIfAny);
    }

    @Override
    public boolean isInSession() {
        return getCurrentSession() != null;
    }

    @Override
    public boolean isInTransaction() {
        if (isInSession()) {
            if (getCurrentSession().getCurrentTransactionId() != null) {
                if (!getCurrentSession().getCurrentTransactionState().isComplete()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public <R> R doInSession(final Callable<R> callable, final AuthenticationSession authenticationSession) {
        final IsisSessionFactoryDefault sessionFactory = this;
        boolean noSession = !sessionFactory.isInSession();
        try {
            if (noSession) {
                sessionFactory.openSession(authenticationSession);
            }

            return callable.call();
        } catch (Exception cause) {
            val msg = String.format("An error occurred while executing code in %s session", noSession ? "a temporary" : "a"); 
            throw new RuntimeException(msg, cause);
        } finally {
            if (noSession) {
                sessionFactory.closeSession();
            }
        }
    }




}
