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

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.runtime.system.DeploymentType;
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
     * @return pre-bootstrapping configuration
     */
    public static IsisSystemEnvironment getEnvironment() {
        return _Context.getOrElse(IsisSystemEnvironment.class, IsisSystemEnvironment::getDefault);
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
    
    public static void dumpConfig() {

        final IsisConfiguration configuration;
        try {
            configuration = getConfiguration();
        } catch (Exception e) {
            // ignore
            return;
        }

        final Map<String, String> map = new TreeMap<>(configuration.asMap());

        String head = String.format("ISIS %s (%s) ", getVersion(), DeploymentType.get().name());
        final int fillCount = 46-head.length();
        final int fillLeft = fillCount/2;
        final int fillRight = fillCount-fillLeft;
        head = _Strings.padStart("", fillLeft, ' ') + head + _Strings.padEnd("", fillRight, ' ');
        
        System.out.println("================================================");
        System.out.println("="+head+"=");
        System.out.println("================================================");
        map.forEach((k,v)->{
            System.out.println(k+" -> "+v);
        });
        System.out.println("================================================");
    }

}
