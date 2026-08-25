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
package org.apache.causeway.commons.internal.ioc;

import java.lang.annotation.Annotation;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.Cardinality;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Framework internal holder of Spring's {@link ApplicationContext}.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0
 */
public record SpringContextHolder(
    @NonNull ApplicationContext springContext) {

    public Stream<SingletonBeanProvider> streamAllBeans() {
        return Stream.of(springContext.getBeanDefinitionNames())
                .map(name->{
                    var type = ClassUtils.getUserClass(springContext.getType(name));
                    var beanAdapter = new SingletonBeanProvider(name, type, ()->springContext.getBean(name));
                    return beanAdapter;
                });
    }

    public boolean containsBean(final String id) {
        return springContext.containsBean(id);
    }

    public Optional<?> lookupBean(final String id) {
        return springContext.containsBean(id)
                ? Optional.of(springContext.getBean(id))
                : Optional.empty();
    }

    /**
     * Return an instance (possibly shared or independent) of the object managed by the IoC container.
     * @param <T>
     * @param requiredType
     * @return an instance of the bean, or null if not available or not unique
     * (i.e. multiple candidates found with none marked as primary)
     * @throws RuntimeException if instance creation failed
     */
    public <T> Optional<T> get(final @NonNull Class<T> requiredType) {
        var provider = springContext.getBeanProvider(requiredType);
        try {
            return Optional.ofNullable(provider.getIfUnique());
        } catch (Exception cause) {
            throw _Exceptions.unrecoverable(cause, "Failed to create an instance of type %s", requiredType);
        }
    }

    /**
     * Returns all available implementations of the service, ordered by priority.
     *
     * <p>If there is more than one implementation, then the one with the &quot;highest&quot;
     * priority (either annotated with {@link org.springframework.context.annotation.Primary},
     * else with encountered with earliest {@link org.apache.causeway.applib.annotation.PriorityPrecedence precedence})
     * is used instead.
     *
     * @param <T> - the generic type parameter (to save the caller from having to downcast)
     * @param requiredType - the required type
     * @throws NoSuchElementException - if the singleton is not resolvable
     *
     * @see #select(Class, Annotation[])
     * @see #getSingletonElseFail(Class)
     */
    public <T> Can<T> select(final @NonNull Class<T> requiredType) {
        var allMatchingBeans = springContext.getBeanProvider(requiredType)
                .orderedStream()
                .collect(Can.toCan());
        return allMatchingBeans;
    }

    /**
     * Returns all available implementations of the service that match the additional qualifiers, ordered by priority.
     *
     * <p>If there is more than one implementation, then the one with the &quot;highest&quot;
     * priority (either annotated with {@link org.springframework.context.annotation.Primary},
     * else with encountered with earliest {@link org.apache.causeway.applib.annotation.PriorityPrecedence precedence})
     * is used instead.
     *
     * @param <T> - the generic type parameter (to save the caller from having to downcast)
     * @param requiredType - the required type
     * @param beanQualifier - optionally the qualifier, that is required to be present on the Bean to look up
     * @throws NoSuchElementException - if the singleton is not resolvable
     *
     * @see #select(Class)
     */
    public <T> Can<T> select(
            final @NonNull Class<T> requiredType,
            final @Nullable Qualifier beanQualifier) {

    	if (beanQualifier == null
    			|| !StringUtils.hasText(beanQualifier.value()))
			return springContext.getBeanProvider(requiredType)
    				.orderedStream()
    				.collect(Can.toCan());

        return springContext.getBeansOfType(requiredType).entrySet()
        	.stream()
        	.filter(entry->
        		lookupQualifier(entry.getKey())
        			.map(availableQualifier->availableQualifier.equals(beanQualifier.value()))
        			.orElse(false))
        	.map(Entry::getValue)
        	.collect(Can.toCan()); // I believe Spring allows at most one qualified match
    }

    /**
     * Requires that there is AT LEAST one implementation of the service, and returns it.
     *
     * <p>If there is more than one implementation, then the one with the &quot;highest&quot;
     * priority (either annotated with {@link org.springframework.context.annotation.Primary},
     * else with encountered with earliest {@link org.apache.causeway.applib.annotation.PriorityPrecedence precedence})
     * is used instead.
     *
     * @param type - the required type
     * @param <T>  - the generic type parameter (to save the caller from having to downcast)
     * @return IoC managed singleton
     * @throws NoSuchElementException - if the singleton is not resolvable
     */
    public <T> T getSingletonElseFail(final @NonNull Class<T> type) {
        var candidates = select(type);
        if (candidates.getCardinality() == Cardinality.ZERO)
            throw _Exceptions.noSuchElement("Cannot resolve singleton '%s'", type);
        return candidates.getFirstElseFail();
    }

    // -- HELPER - QUALIFIER PROCESSING

    private Optional<String> lookupQualifier(final String beanName) {
    	var beanFactory = ((ConfigurableApplicationContext) springContext).getBeanFactory();
        var beanDefinition = beanFactory.getBeanDefinition(beanName);
        if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
            // the bean factory method or else the bean-type itself, we are inspecting for a Qualifier annotation
            var annotatedTypeMetadata = Optional.<AnnotatedTypeMetadata>ofNullable(annotatedBeanDefinition.getFactoryMethodMetadata())
                .orElseGet(annotatedBeanDefinition::getMetadata);
            return Optional.ofNullable(
                    annotatedTypeMetadata.getAnnotationAttributes(Qualifier.class.getName()))
                .map(map->map.get("value"))
                .map(String.class::cast)
                .map(_Strings::emptyToNull);
        }
        return Optional.empty();
    }

}
