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
package org.apache.isis.core.metamodel;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Produces;

import org.mockito.Mockito;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.builder.IsisConfigurationBuilder;
import org.apache.isis.core.metamodel.services.ServiceInjectorDefault;
import org.apache.isis.core.metamodel.services.registry.ServiceRegistryDefault;
import org.apache.isis.core.metamodel.specloader.InjectorMethodEvaluatorDefault;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

/**
 * 
 * @since 2.0.0-M2
 *
 */
public final class BeansForTesting {

    // -- BUILDER
    
    public static BeanListBuilder builder() {
        return new BeanListBuilder();
    }
    
    public static class BeanListBuilder {
        
        private final Set<Class<?>> beans = new HashSet<>();
        
        public Class<?>[] build() {
            return beans.toArray(_Constants.emptyClasses);
        }
        
        public BeanListBuilder add(Class<?> bean) {
            beans.add(bean);
            return this;
        }
        
        public BeanListBuilder addAll(Class<?> ... beans) {
            stream(beans)
            .forEach(this.beans::add);
            return this;
        }
        
        /**
         * Adds IsisConfiguration support.
         */
        public BeanListBuilder config() {
            beans.add(config);
            return this;
        } 

        /**
         * Adds ServiceRegistry and ServiceInjector support.
         */
        public BeanListBuilder injector() {
           
            addAll(
                config,
                InjectorMethodEvaluatorDefault.class,
                ServiceRegistryDefault.class,
                ServiceInjectorDefault.class);
            
            return this;
        }
        
        
    }
    
    
    // -- PREDEFINED
    
    public static Class<?> config = _Config.class;
    public static Class<?> mock_repository = _Mock_RepositoryService.class;
    
    static class _Config {
        
        @Produces
        IsisConfiguration getConfiguration() {
            return IsisConfigurationBuilder.getDefaultForUnitTesting()
            .build();
        }
        
    }

    static class _Mock_RepositoryService {
        
        @Produces
        RepositoryService mockRepositoryService() {
            return Mockito.mock(RepositoryService.class);
        }
        
    }
    
    
}
