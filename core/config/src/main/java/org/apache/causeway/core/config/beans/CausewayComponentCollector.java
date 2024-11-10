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
package org.apache.causeway.core.config.beans;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.context._Context;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * @implNote we must not rely on CausewayConfiguration or other provisioned
 * services to be available; type classification happens before the post-construct phase

 */
@Log4j2
record CausewayComponentCollector(
        BeanDefinitionRegistry registry,
        CausewayBeanTypeClassifier causewayBeanTypeClassifier,
        Map<Class<?>, CausewayBeanMetaData> introspectableTypes) {

    CausewayComponentCollector(
            @NonNull final BeanDefinitionRegistry registry,
            @NonNull final CausewayBeanTypeClassifier causewayBeanTypeClassifier) {
        this(registry, causewayBeanTypeClassifier, _Maps.newConcurrentHashMap());
    }

    void collect(final String beanDefinitionName) {
        var beanDefinition = lookupBeanDefinition(beanDefinitionName).orElse(null);
        if(beanDefinition == null) return;

        var beanClass = loadClass(beanDefinition).orElse(null);
        if(beanClass==null) return;

        var typeMeta = collectBeanClass(beanClass);

        if(typeMeta.getManagedBy().isVetoedForInjection()) {
            registry.removeBeanDefinition(beanDefinitionName);
            log.debug("vetoing bean {}", beanDefinitionName);
        } else {
            var beanNameOverride = typeMeta.getManagedBy().isBeanNameOverride()
                    ? typeMeta.getLogicalType().getLogicalTypeName()
                    : null;
            if(_Strings.isNotEmpty(beanNameOverride)) {
                registry.removeBeanDefinition(beanDefinitionName);
                registry.registerBeanDefinition(beanNameOverride, beanDefinition);
                log.debug("renaming bean {} -> {}", beanDefinitionName, beanNameOverride);
            }
        }
    }

    /**
     * Allows for the given type-meta to be modified before bean-definition registration
     * is finalized by Spring, immediately after the type-scan phase.
     * Aspects to be modified:
     * <br> - Whether given {@link Component} annotated or meta-annotated type should be made
     * available for injection.
     * <br> - Naming strategy to override that of Spring.
     *
     * @apiNote implementing classes might have side effects, eg. intercept
     * discovered types into a type registry
     */
    private CausewayBeanMetaData collectBeanClass(final Class<?> beanClass) {
        var typeMeta = causewayBeanTypeClassifier.classify(beanClass);
        var beanSort = typeMeta.getBeanSort();
        if(beanSort.isToBeIntrospected()) {
            introspectableTypes.put(beanClass, typeMeta);
            if(log.isDebugEnabled()) {
                log.debug("to-be-introspected: {} [{}]",
                        beanClass,
                        beanSort.name());
            }
        }
        return typeMeta;
    }

    // -- HELPER

    private static Optional<Class<?>> loadClass(@Nullable final BeanDefinition beanDefinition) {
        return Optional.ofNullable(beanDefinition.getBeanClassName())
                .flatMap(CausewayComponentCollector::loadClass);
    }

    private static Optional<Class<?>> loadClass(@Nullable final String className) {
        if(className==null) return Optional.empty();
        return Try.<Class<?>>call(()->_Context.loadClass(className))
            .mapFailureToSuccess(ex->{
                log.warn("Failed to load class for name '{}'", className);
                return (Class<?>)null;
            })
            .getValue();
    }

    private Optional<BeanDefinition> lookupBeanDefinition(final String beanDefinitionName) {
        try {
            var beanDefinition = registry.getBeanDefinition(beanDefinitionName);
            return Optional.of(beanDefinition);
        } catch (NoSuchBeanDefinitionException e) {
            return Optional.empty();
        }
    }

}
