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
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ClassUtils;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.Cardinality;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
    @NonNull ApplicationContext springContext
    ) {

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
    @SuppressWarnings("javadoc")
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
     * @param qualifiersRequired - if contains annotations, that are not qualifiers, these are just ignored
     * @throws NoSuchElementException - if the singleton is not resolvable
     *
     * @see #select(Class)
     */
    @SuppressWarnings("javadoc")
    public <T> Can<T> select(
        final @NonNull Class<T> requiredType,
        final @Nullable Annotation[] qualifiers) {

        var qualifiersRequired = filterQualifiers(qualifiers);

        if(_NullSafe.isEmpty(qualifiersRequired)) {
            var allMatchingBeans = springContext.getBeanProvider(requiredType)
                .orderedStream()
                .collect(Can.toCan());
            return allMatchingBeans;
        }

        var allMatchingBeans = springContext.getBeanProvider(requiredType)
            .orderedStream()
            .filter(t->{
                var qualifiersPresent = _Sets.of(t.getClass().getAnnotations());
                return qualifiersPresent.containsAll(qualifiersRequired);
            })
            .collect(Can.toCan());
        return allMatchingBeans;
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
    @SuppressWarnings("javadoc")
    public <T> T getSingletonElseFail(final @NonNull Class<T> type) {
        var candidates = select(type);
        if (candidates.getCardinality() == Cardinality.ZERO) {
            throw _Exceptions.noSuchElement("Cannot resolve singleton '%s'", type);
        }
        return candidates.getFirstElseFail();
    }

    // -- HELPER - QUALIFIER PROCESSING

    /**
     * Filters the input array into a collection, such that only annotations are retained,
     * that are valid qualifiers for CDI.
     * @param annotations
     * @return non-null
     */
    private static Set<Annotation> filterQualifiers(final @Nullable Annotation[] annotations) {
        if(_NullSafe.isEmpty(annotations)) {
            return Collections.emptySet();
        }
        return _NullSafe.stream(annotations)
                .filter(SpringContextHolder::isGenericQualifier)
                .collect(Collectors.toSet());
    }

    /**
     * @param annotation
     * @return whether or not the annotation is a valid qualifier for Spring
     */
    private static boolean isGenericQualifier(final Annotation annotation) {
        if(annotation==null) {
            return false;
        }
        if(annotation.annotationType().getAnnotationsByType(Qualifier.class).length>0) {
            return true;
        }
        if(annotation.annotationType().equals(Primary.class)) {
            return true;
        }
        return false;
    }

}
