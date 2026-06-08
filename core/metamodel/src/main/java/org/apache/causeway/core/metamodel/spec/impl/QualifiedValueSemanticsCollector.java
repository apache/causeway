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
package org.apache.causeway.core.metamodel.spec.impl;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.ioc.SpringContextHolder;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;

import lombok.extern.slf4j.Slf4j;

/**
 * Collects qualifier information into the _ClassCache.
 *
 * @since 4.0
 */
@Slf4j
record QualifiedValueSemanticsCollector(CausewaySystemEnvironment systemEnvironment) {

    /**
     * Collects qualifier information into the _ClassCache, such that
     * when given a {@link ValueSemanticsProvider} class,
     * we can quickly lookup its qualifier string.
     */
    void collect() {
        var springContext = Optional.ofNullable(systemEnvironment.springContextHolder())
                .map(SpringContextHolder::springContext)
                .orElse(null);
        if(springContext==null) {
            // seen this when JUnit testing
            log.warn("missing Spring Context; perhaps initialzed too early; "
                    + "as a consequence will not detect qualified bean factory methods");
            return;
        }
        var classCache = _ClassCache.getInstance();
        var beanFactory = ((ConfigurableApplicationContext) springContext).getBeanFactory();

        Stream.of(springContext.getBeanDefinitionNames())
            .forEach(name->{
                var type = ClassUtils.getUserClass(springContext.getType(name));
                lookupQualifier(name, beanFactory)
                    .ifPresent(qualifier->
                        classCache.head(type).putAttribute(_ClassCache.Attribute.SPRING_QUALIFIED, qualifier));
            });
    }

    private static Optional<String> lookupQualifier(final String id, final ConfigurableListableBeanFactory beanFactory) {
        var beanDefinition = beanFactory.getBeanDefinition(id);
        if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
            // the bean factory method or else the bean-type itself, we are inspecting for a Qualifier annotation
            var annotatedTypeMetadata = Optional.<AnnotatedTypeMetadata>ofNullable(annotatedBeanDefinition.getFactoryMethodMetadata())
                .orElseGet(annotatedBeanDefinition::getMetadata);
            return Optional.ofNullable(
                    annotatedTypeMetadata.getAnnotationAttributes(Qualifier.class.getName()))
                .map(it->it.get("value"))
                .map(String.class::cast)
                .map(_Strings::emptyToNull);
        }
        return Optional.empty();
    }

}
