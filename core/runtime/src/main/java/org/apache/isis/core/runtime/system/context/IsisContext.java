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
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.plugins.environment.DeploymentType;
import org.apache.isis.core.plugins.environment.IsisSystemEnvironment;
import org.apache.isis.core.plugins.environment.IsisSystemEnvironmentPlugin;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

/**
 * Provides static access to current context's singletons
 * {@link MetaModelInvalidException} and {@link IsisSessionFactory}.
 */
public interface IsisContext {

    /**
     * Populated only if the meta-model was found to be invalid.
     * @return null, if there is none
     */
    public static MetaModelInvalidException getMetaModelInvalidExceptionIfAny() {
        return _Context.getIfAny(MetaModelInvalidException.class);
    }

    /**
     *
     * @return Isis's session factory
     * @throws IllegalStateException if IsisSessionFactory not initialized
     */
    // Implementation Note: Populated only by {@link IsisSessionFactoryBuilder}.
    public static IsisSessionFactory getSessionFactory() {
        return _Context.getOrThrow(
                IsisSessionFactory.class,
                ()->new IllegalStateException(
                        "internal error: should have been populated by IsisSessionFactoryBuilder") );
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

    /**
     * @return pre-bootstrapping configuration
     */
    public static IsisSystemEnvironment getEnvironment() {
        return _Context.getEnvironment();
    }

    /**
     * @deprecated use the {@link IsisSystemEnvironmentPlugin} SPI instead
     */
    @Deprecated
    public static class EnvironmentPrimer {

        /**
         * For integration testing allows to prime the environment via provided parameters. Will not override
         * any IsisSystemEnvironment instance, that is already registered with the current context, because the 
         * IsisSystemEnvironment is expected to be an immutable singleton within an application's life-cycle.
         * @deprecated use the {@link IsisSystemEnvironmentPlugin} SPI instead
         */
        @Deprecated
        public static void primeEnvironment(DeploymentType deploymentType) {
            _Context.computeIfAbsent(IsisSystemEnvironment.class, __->IsisSystemEnvironment.of(deploymentType));
        }
        
        @Deprecated
        public static void primeEnvironment(IsisConfiguration configurationOverride) {
            
            final String deploymentTypeLiteral = configurationOverride.getString("isis.deploymentType");
            if(_Strings.isNullOrEmpty(deploymentTypeLiteral)) {
                return; // do nothing
            }
            
            // at this point, the deploymentType seems explicitly set via config
            
            // throws if type can not be parsed
            final DeploymentType deploymentType = 
                    parseDeploymentType(deploymentTypeLiteral.toLowerCase());
            primeEnvironment(deploymentType);
        }

        private static DeploymentType parseDeploymentType(String deploymentTypeLiteral) {
            
            switch(deploymentTypeLiteral) {
            case "server_prototyping":
            case "prototyping":
                return DeploymentType.PROTOTYPING;
            case "server":
            case "production":
                return DeploymentType.PROTOTYPING;
            default:
                throw new IllegalArgumentException(
                        String.format("unknown deployment type '%s' in config property '%s'", 
                                deploymentTypeLiteral, "isis.deploymentType"));
            }

        }
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
     * @return framework's current PersistenceSession (if any)
     * @throws IllegalStateException if IsisSessionFactory not initialized
     */
    public static Optional<PersistenceSession> getPersistenceSession() {
        return Optional.ofNullable(getSessionFactory().getCurrentSession())
                .map(IsisSession::getPersistenceSession);
    }

    /**
     * @return framework's IsisConfiguration
     * @throws IllegalStateException if IsisSessionFactory not initialized
     */
    public static IsisConfiguration getConfiguration() {
        return getSessionFactory().getConfiguration();
    }

    /**
     * @return framework's SpecificationLoader
     * @throws IllegalStateException if IsisSessionFactory not initialized
     */
    public static SpecificationLoader getSpecificationLoader() {
        return getSessionFactory().getSpecificationLoader();
    }

    /**
     * @return framework's ServicesInjector
     * @throws IllegalStateException if IsisSessionFactory not initialized
     */
    public static ServicesInjector getServicesInjector() {
        return getSessionFactory().getServicesInjector();
    }

    public static String getVersion() {
        return "2.0.0-M2";
    }
    
    public static StringBuilder dumpConfig() {
        
        final StringBuilder sb = new StringBuilder();

        final IsisConfiguration configuration;
        try {
            configuration = getConfiguration();
        } catch (Exception e) {
            // ignore
            return sb;
        }

        final Map<String, String> map = new TreeMap<>(configuration.asMap());

        String head = String.format("ISIS %s (%s) ", getVersion(), IsisContext.getEnvironment().getDeploymentType().name());
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
