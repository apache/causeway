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
package org.apache.isis.core.runtime.system.context;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.spring._Spring;
import org.apache.isis.config.ConfigurationConstants;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.services.persistsession.ObjectAdapterService;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelDeficiencies;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.plugins.environment.IsisSystemEnvironment;
import org.apache.isis.core.runtime.system.context.session.RuntimeContext;
import org.apache.isis.core.runtime.system.context.session.RuntimeContextBase;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

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

//    /**
//     *
//     * @return Isis's session factory
//     * @throws IllegalStateException if IsisSessionFactory not initialized
//     */
//    // Implementation Note: Populated only by {@link IsisSessionFactoryBuilder}.
//    public static IsisSessionFactory getSessionFactory() {
//        return _Context.getOrThrow(
//                IsisSessionFactory.class,
//                ()->new IllegalStateException(
//                        "internal error: should have been populated by IsisSessionFactoryBuilder") );
//    }

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

    /**
     * @return pre-bootstrapping configuration
     */
    public static IsisSystemEnvironment getEnvironment() {
        return _Context.getEnvironment();
    }
    
    // -- LIFE-CYCLING

    /**
     * Destroys this context and clears any state associated with it.
     * It marks the end of IsisContext's life-cycle. Subsequent calls have no effect.
     */
    public static void clear() {
        _Context.clear();
    }

    // -- CONVENIENT SHORTCUTS
    
    /**
     * @return new instance of ManagedObjectContext
     */
    public static RuntimeContext newManagedObjectContext() {
        return new RuntimeContextBase() {}; 
    }
    
    /**
     * @return framework's IsisConfiguration
     * @throws NoSuchElementException - if IsisConfiguration not managed
     */
    public static IsisConfiguration getConfiguration() {
        return _Config.getConfiguration(); // currently not utilizing CDI, to support unit testing
    }
    
    /**
     * @return framework's ServicesInjector
     * @throws NullPointerException - if AppManifest not resolvable
     */
    public static AppManifest getAppManifest() {
        return Objects.requireNonNull(getConfiguration().getAppManifest()); 
    }

    /**
     * @return framework's SpecificationLoader
     * @throws NoSuchElementException - if SpecificationLoader not managed
     */
    public static SpecificationLoader getSpecificationLoader() {
        return _Spring.getSingletonElseFail(SpecificationLoader.class);
    }

    /**
     * @return framework's ServicesInjector
     * @throws NoSuchElementException - if ServicesInjector not managed
     */
    public static ServiceInjector getServiceInjector() {
        return _Spring.getSingletonElseFail(ServiceInjector.class);
    }
    
    /**
     * @return framework's ServiceRegistry
     * @throws NoSuchElementException - if ServiceRegistry not managed
     */
    public static ServiceRegistry getServiceRegistry() {
        return _Spring.getSingletonElseFail(ServiceRegistry.class);
    }
    
    /**
     * @return framework's IsisSessionFactory 
     * @throws NoSuchElementException - if IsisSessionFactory not resolvable
     */
    public static IsisSessionFactory getSessionFactory() {
        return _Spring.getSingletonElseFail(IsisSessionFactory.class);
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
     * @return framework's one of framework's current PersistenceSessions
     */
    public static Optional<PersistenceSession> getPersistenceSession() {
    	return PersistenceSession.current(PersistenceSession.class)
    	.getFirst();
    }
    
    /**
     * TODO [2033] its unclear whether there is only one or multiple
     * @return framework's current IsisTransactionManager (if any)
     * @throws IllegalStateException - if IsisSessionFactory not resolvable
     */
    public static Optional<IsisTransactionManager> getTransactionManager() {
    	return getPersistenceSession()
    			.map(PersistenceSession::getTransactionManager);
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
        return getObjectAdapterProvider()::adapterFor;
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
    
    // -- 

    public static StringBuilder dumpConfig() {
        
        final StringBuilder sb = new StringBuilder();

        final IsisConfiguration configuration;
        try {
            configuration = getConfiguration();
        } catch (Exception e) {
            // ignore
            return sb;
        }

        final Map<String, String> map = 
                ConfigurationConstants.maskIfProtected(configuration.asMap(), TreeMap::new);

        String head = String.format("ISIS %s (%s) ", 
                IsisConfiguration.getVersion(), getEnvironment().getDeploymentType().name());
        final int fillCount = 46-head.length();
        final int fillLeft = fillCount/2;
        final int fillRight = fillCount-fillLeft;
        head = _Strings.padStart("", fillLeft, ' ') + head + _Strings.padEnd("", fillRight, ' ');
        
        sb.append("================================================\n");
        sb.append("="+head+"=\n");
        sb.append("================================================\n");
        map.forEach((k,v)->{
            sb.append(k+" -> "+v).append("\n");
        });
        sb.append("================================================\n");
        
        return sb;
    }




}
