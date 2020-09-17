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
package org.apache.isis.commons.internal.ioc.spring;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._With;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.ioc.IocContainer;
import org.apache.isis.commons.internal.ioc.ManagedBeanAdapter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
@RequiredArgsConstructor(staticName = "of")
public class IocContainerSpring implements IocContainer {
    
    @NonNull private final ApplicationContext springContext;

    @Override
    public <T> Optional<T> get(Class<T> requiredType) {
        val provider = springContext.getBeanProvider(requiredType);
        try {
            return Optional.ofNullable(provider.getIfUnique());
        } catch (Exception cause) {
            throw _Exceptions.unrecoverable("Failed to create an instance of type " + requiredType, cause);
        }
    }
    
    @Override
    public Stream<ManagedBeanAdapter> streamAllBeans() {

        val context = springContext;

        return Stream.of(context.getBeanDefinitionNames())
                .map(name->{

                    val type = context.getType(name);
                    val id = name; // just reuse the bean's name

                    //val scope = beanFactory.getBeanDefinition(name).getScope();

                    val resolvableType = ResolvableType.forClass(type);
                    val bean = context.getBeanProvider(resolvableType);

                    val beanAdapter = BeanAdapterSpring.of(id, type, bean);

                    return beanAdapter;
                });


    }

    @Override
    public Optional<?> lookupById(String id) {
        return springContext.containsBean(id)
                ? Optional.of(springContext.getBean(id))
                : Optional.empty();
    }

    @Override
    public <T> Can<T> select(final Class<T> requiredType) {
        _With.requires(requiredType, "requiredType");

        val allMatchingBeans = springContext.getBeanProvider(requiredType)
                .orderedStream()
                .collect(Can.toCan());
        
        return allMatchingBeans;
    }

    @Override
    public <T> Can<T> select(
            final Class<T> requiredType, 
            @Nullable Set<Annotation> qualifiersRequired) {

        _With.requires(requiredType, "requiredType");

        if(_NullSafe.isEmpty(qualifiersRequired)) {
            
            val allMatchingBeans = springContext.getBeanProvider(requiredType)
                    .orderedStream()
                    .collect(Can.toCan());
            
            return allMatchingBeans;
        }

        val allMatchingBeans = springContext.getBeanProvider(requiredType)
                .orderedStream()
                .filter(t->{
                    val qualifiersPresent = _Sets.of(t.getClass().getAnnotations());
                    return qualifiersPresent.containsAll(qualifiersRequired);
                })
                .collect(Can.toCan());
        
        return allMatchingBeans;
        
    }

}
