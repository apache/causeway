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
package org.apache.isis.commons.internal.spring;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ResolvableType;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.commons.ioc.LifecycleContext;
import org.apache.isis.core.commons.collections.Bin;

import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Framework internal Spring support.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0.0
 */
public class _Spring {

    public static void init(ApplicationContext context) {
        _Context.putSingleton(ApplicationContext.class, context);
    }
    
    public static ApplicationContext context() {
        return _Context.getElseFail(ApplicationContext.class);
    }
    
    public static <T> Bin<T> select(final Class<T> requiredType) {
        val allMatchingBeans = context().getBeanProvider(requiredType).orderedStream();
        return Bin.ofStream(allMatchingBeans);
    }
    
    /**
     * 
     * @param filterDomainService - usually ServiceRegistry::isDomainServiceType
     * @param beanNameProvider - usually ServiceUtil::idOfBean
     */
    public static Stream<BeanAdapter> streamAllBeans(
            Predicate<Class<?>> filterDomainService) {
        
        val context = context();
        val beanFactory = ((ConfigurableApplicationContext)context).getBeanFactory();
        
        return Stream.of(context.getBeanDefinitionNames())
        .map(name->{
        
            
            val type = context.getType(name);
            val isDomainService = filterDomainService.test(type);
            val id = name; // just reuse the bean's name
            
            val scope = beanFactory.getBeanDefinition(name).getScope();
            val lifecycleContext = LifecycleContext.valueOf(scope);
            
            val resolvableType = ResolvableType.forClass(type);
            val bean = context.getBeanProvider(resolvableType);
            
            val beanAdapter = BeanAdapterSpring.of(id, lifecycleContext, type, bean, isDomainService);
            
            return beanAdapter;
        });

        
    }

    /**
     * @return Spring managed singleton wrapped in an Optional
     */
    public static <T> Optional<T> getSingleton(@Nullable Class<T> type) {
        if(type==null) {
            return Optional.empty();
        }
        return select(type).getSingleton();
    }
    
    /**
     * @return Spring managed singleton
     * @throws NoSuchElementException - if the singleton is not resolvable
     */
    public static <T> T getSingletonElseFail(@Nullable Class<T> type) {
        return getSingleton(type)
                .orElseThrow(()->_Exceptions.noSuchElement("Cannot resolve singleton '%s'", type));
                        
    }

    
}
