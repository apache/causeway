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

package org.apache.isis.core.metamodel.services.registry;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.spring._Spring;
import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.commons.ioc.BeanSortClassifier;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.config.registry.IsisBeanTypeRegistry;
import org.apache.isis.core.runtime.threadpool.ThreadPoolExecutionMode;
import org.apache.isis.core.runtime.threadpool.ThreadPoolSupport;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * @since 2.0.0
 */
@Singleton @Slf4j
public final class ServiceRegistryDefault implements ServiceRegistry, ApplicationContextAware {

    @Override
    public Stream<BeanAdapter> streamRegisteredBeans() {
        return registeredBeans.get().stream();
    }
    
    @Override
    public void setApplicationContext(ApplicationContext springContext) throws BeansException {
        
        // disables concurrent Spec-Loading
        ThreadPoolSupport.HIGHEST_CONCURRENCY_EXECUTION_MODE_ALLOWED = 
                ThreadPoolExecutionMode.SEQUENTIAL_WITHIN_CALLING_THREAD;
        
        _Context.clear(); // ensures a well defined precondition

        _Context.putSingleton(ApplicationContext.class, springContext);
        _Config.putAll(_Spring.copySpringEnvironmentToMap(configurableEnvironment));

        log.info("Spring's context was passed over to Isis");
        
        isisConfiguration = _Config.getConfiguration(); // finalize config
        
        // dump config to log
        if(log.isInfoEnabled() && !isisConfiguration.getEnvironment().isUnitTesting()) {
            log.info("\n" + _Config.getConfiguration().toStringFormatted());
        }    
        
    }
    
    @Bean @Singleton
    public IsisConfiguration getConfiguration() {
        return isisConfiguration;
    }

    // -- DEPS
    
    @Autowired private ConfigurableEnvironment configurableEnvironment;
    private IsisConfiguration isisConfiguration;
    
    // -- HELPER
    
    private final _Lazy<Set<BeanAdapter>> registeredBeans = _Lazy.threadSafe(this::enumerateBeans);
    
    Set<BeanAdapter> enumerateBeans() {
        
        val beanSortClassifier = IsisBeanTypeRegistry.current();
        
        return _Spring.streamAllBeans(beanSortClassifier)
        .filter(_NullSafe::isPresent)
        .collect(Collectors.toCollection(HashSet::new));
    }


}
