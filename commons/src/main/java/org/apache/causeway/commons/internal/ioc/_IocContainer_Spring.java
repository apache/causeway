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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Spring specific implementation of _IocContainer (package private)
 * @since 2.0
 *
 */
@RequiredArgsConstructor(staticName = "of")
final class _IocContainer_Spring implements _IocContainer {

    @NonNull private final ApplicationContext springContext;

    @Override
    public <T> Optional<T> get(final @NonNull Class<T> requiredType) {
        var provider = springContext.getBeanProvider(requiredType);
        try {
            return Optional.ofNullable(provider.getIfUnique());
        } catch (Exception cause) {
            throw _Exceptions.unrecoverable(cause, "Failed to create an instance of type %s", requiredType);
        }
    }

    @Override
    public Stream<_SingletonBeanProvider> streamAllBeans() {
        return Stream.of(springContext.getBeanDefinitionNames())
                .map(name->{
                    var type = ClassUtils.getUserClass(springContext.getType(name));
                    var beanAdapter = _SingletonBeanProvider.of(name, type, ()->springContext.getBean(name));
                    return beanAdapter;
                });
    }

    @Override
    public boolean containsBean(final String id) {
        return springContext.containsBean(id);
    }

    @Override
    public Optional<?> lookupBean(final String id) {
        return springContext.containsBean(id)
                ? Optional.of(springContext.getBean(id))
                : Optional.empty();
    }

    @Override
    public <T> Can<T> select(final @NonNull Class<T> requiredType) {
        var allMatchingBeans = springContext.getBeanProvider(requiredType)
                .orderedStream()
                .collect(Can.toCan());
        return allMatchingBeans;
    }

    @Override
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

    // -- QUALIFIER PROCESSING

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
                .filter(_IocContainer_Spring::isGenericQualifier)
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
