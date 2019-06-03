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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Singleton;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.commons.internal.spring._Spring;
import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.config.registry.IsisBeanTypeRegistry;
import org.apache.isis.core.runtime.threadpool.ThreadPoolExecutionMode;
import org.apache.isis.core.runtime.threadpool.ThreadPoolSupport;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0.0
 */
@Singleton @Log4j2
public final class ServiceRegistryDefault implements ServiceRegistry, ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext springContext) throws BeansException {
        
        // disables concurrent Spec-Loading
        ThreadPoolSupport.HIGHEST_CONCURRENCY_EXECUTION_MODE_ALLOWED = 
                ThreadPoolExecutionMode.SEQUENTIAL_WITHIN_CALLING_THREAD;
        
        
        // ensures a well defined precondition
        {
            val beanTypeRegistry = _Context.getIfAny(IsisBeanTypeRegistry.class);
            _Context.clear(); // special code in IsisBeanTypeRegistry.close() prevents auto-closing
            if(beanTypeRegistry!=null) {
                _Context.putSingleton(IsisBeanTypeRegistry.class, beanTypeRegistry);
            } 
        }

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

    @Override
    public Optional<BeanAdapter> lookupRegisteredBeanById(String id) {
        return Optional.ofNullable(registeredBeansById.get().get(id));
    }
    
    @Override
    public Stream<BeanAdapter> streamRegisteredBeans() {
        return registeredBeansById.get().values().stream();
    }
    
    // -- DEPS
    
    @Autowired private ConfigurableEnvironment configurableEnvironment;
    private IsisConfiguration isisConfiguration;
    
    // -- HELPER
    
    private final _Lazy<Map<String, BeanAdapter>> registeredBeansById = 
            _Lazy.threadSafe(this::enumerateBeans);
    
    private Map<String, BeanAdapter> enumerateBeans() {
        
        val beanSortClassifier = IsisBeanTypeRegistry.current();
        val map = _Maps.<String, BeanAdapter>newHashMap();
        
        _Spring.streamAllBeans(beanSortClassifier)
        .filter(_NullSafe::isPresent)
        .forEach(bean->{
            val id = extractObjectType(bean.getBeanClass()).orElse(bean.getId());
            map.put(id, bean);
        });
        
        return map;
    }

    //TODO[2112] this would be the responsibility of the specloader, but 
    // for now we use as very simple approach
    private Optional<String> extractObjectType(Class<?> type) {
        
        val aDomainService = _Reflect.getAnnotation(type, DomainService.class);
        if(aDomainService!=null) {
            val objectType = aDomainService.objectType();
            if(_Strings.isNotEmpty(objectType)) {
                return Optional.of(objectType); 
            }
            return Optional.empty(); // stop processing
        }
        
        val aDomainObject = _Reflect.getAnnotation(type, DomainObject.class);
        if(aDomainObject!=null) {
            val objectType = aDomainObject.objectType();
            if(_Strings.isNotEmpty(objectType)) {
                return Optional.of(objectType); 
            }
            return Optional.empty(); // stop processing
        }
        
        return Optional.empty();
        
    }


}
