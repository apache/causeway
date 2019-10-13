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
package org.apache.isis.runtime.system.context;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.ioc.spring._Spring;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.services.persistsession.ObjectAdapterService;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.validator.MetaModelDeficiencies;
import org.apache.isis.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.runtime.system.context.session.RuntimeContext;
import org.apache.isis.runtime.system.context.session.RuntimeContextBase;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtime.system.session.IsisSession;
import org.apache.isis.runtime.system.session.IsisSessionFactory;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authentication.manager.AuthenticationManager;
import org.apache.isis.security.authorization.manager.AuthorizationManager;

import lombok.val;

/**
 * Provides static access to current context's singletons
 * {@link MetaModelInvalidException} and {@link IsisSessionFactory}.
 */
public interface IsisContext {

    /**
     * Populated only if the meta-model was found to be invalid.
     * @return null, if there is none
     */
    public static MetaModelDeficiencies getMetaModelDeficienciesIfAny() {
        return _Context.getIfAny(MetaModelDeficiencies.class);
    }

    /**
     *
     * @return Isis's default class loader
     */
    public static ClassLoader getClassLoader() {
        return _Context.getDefaultClassLoader();
    }

    /**
     * Non-blocking call.
     * <p>
     * Returns a new CompletableFuture that is asynchronously completed by a task running in the 
     * ForkJoinPool.commonPool() with the value obtained by calling the given Supplier {@code computation}.
     * <p>
     * If the calling thread is within an open {@link IsisSession} then the ForkJoinPool does make this
     * session also available for any forked threads, via means of {@link InheritableThreadLocal}.
     * 
     * @param computation
     */
    public static <T> CompletableFuture<T> compute(Supplier<T> computation){
        return CompletableFuture.supplyAsync(computation);
    }

    // -- CONVENIENT SHORTCUTS

    /**
     * @return new instance of ManagedObjectContext
     */
    public static RuntimeContext newManagedObjectContext() {
        return new RuntimeContextBase() {}; 
    }

    /**
     * @return framework's IsisConfigurationLegacy
     * @throws NoSuchElementException - if IsisConfigurationLegacy not managed
     */
    public static IsisConfigurationLegacy getConfigurationLegacy() {
        return MetaModelContext.current().getConfigurationLegacy();
    }
    
    /**
     * @return framework's IsisConfiguration
     * @throws NoSuchElementException - if IsisConfiguration not managed
     */
    public static IsisConfiguration getConfiguration() {
        return MetaModelContext.current().getConfiguration();
    }

    /**
     * @return framework's SpecificationLoader
     * @throws NoSuchElementException - if SpecificationLoader not managed
     */
    public static SpecificationLoader getSpecificationLoader() {
        return MetaModelContext.current().getSpecificationLoader();
        //return _Spring.getSingletonElseFail(SpecificationLoader.class);
    }

    /**
     * @return framework's ServicesInjector
     * @throws NoSuchElementException - if ServicesInjector not managed
     */
    public static ServiceInjector getServiceInjector() {
        return MetaModelContext.current().getServiceInjector();
        //return _Spring.getSingletonElseFail(ServiceInjector.class);
    }

    /**
     * @return framework's ServiceRegistry
     * @throws NoSuchElementException - if ServiceRegistry not managed
     */
    public static ServiceRegistry getServiceRegistry() {
        return MetaModelContext.current().getServiceRegistry();
        //return _Spring.getSingletonElseFail(ServiceRegistry.class);
    }

    /**
     * @return framework's IsisSessionFactory 
     * @throws NoSuchElementException - if IsisSessionFactory not resolvable
     */
    public static IsisSessionFactory getSessionFactory() {
        return _Spring.getSingletonElseFail(IsisSessionFactory.class);
    }

    /**
     * @return framework's TransactionService 
     * @throws NoSuchElementException - if TransactionService not resolvable
     */
    public static TransactionService getTransactionService() {
        return _Spring.getSingletonElseFail(TransactionService.class);
    }

    /**
     * @return framework's current IsisSession (if any)
     * @throws IllegalStateException - if IsisSessionFactory not resolvable
     */
    public static Optional<IsisSession> getCurrentIsisSession() {
        return IsisSession.current();
    }

    /**
     * TODO [2033] its unclear whether there is only one or multiple
     * @return framework's currently resolvable PersistenceSessions
     */
    public static Optional<PersistenceSession> getPersistenceSession() {
        return PersistenceSession.current(PersistenceSession.class)
                .getFirst();
    }

    // likely to be extended to support multiple PlatformTransactionManagers, by selecting one by its name
    public static TransactionTemplate createTransactionTemplate() {
        val txMan = _Spring.getSingletonElseFail(PlatformTransactionManager.class);
        return new TransactionTemplate(txMan);
    }

    /**
     * @return framework's ServiceRegistry
     * @throws NoSuchElementException - if ServiceRegistry not managed
     */
    public static ObjectAdapterProvider getObjectAdapterProvider() {
        return _Spring.getSingletonElseFail(ObjectAdapterService.class);
    }

    public static Function<Object, ObjectAdapter> pojoToAdapter() {
        return getObjectAdapterProvider()::adapterFor;
    }

    public static Function<RootOid, ObjectAdapter> rootOidToAdapter() {
        return rootOid -> {
            val ps = IsisContext.getPersistenceSession()
                    .orElseThrow(()->new RuntimeException(new IllegalStateException(
                            "There is no PersistenceSession on the current context.")));
            return ps.getObjectAdapterByIdProvider().adapterFor(rootOid);
        }; 
    }

    /**
     * @return framework's current AuthenticationSession (if any)
     * @throws IllegalStateException - if IsisSessionFactory not resolvable
     */
    public static Optional<AuthenticationSession> getAuthenticationSession() {
        return getCurrentIsisSession()
                .map(IsisSession::getAuthenticationSession);
    }

    public static AuthenticationManager getAuthenticationManager() {
        return _Spring.getSingletonElseFail(AuthenticationManager.class);
    }

    public static AuthorizationManager getAuthorizationManager() {
        return _Spring.getSingletonElseFail(AuthorizationManager.class);
    }


}
