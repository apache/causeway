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

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

/**
 * @since 2.0
 */
@Value(staticConstructor = "of")
public final class _SingletonBeanProvider {

    // -- TEST FACTORIES

    public static <T> _SingletonBeanProvider forTestingLazy(
            final String logicalTypeName, final Class<T> beanClass, final Supplier<T> beanProvider) {
        return _SingletonBeanProvider.of(logicalTypeName, beanClass, beanProvider);
    }

    public static <T> _SingletonBeanProvider forTestingLazy(
            final Class<T> beanClass, final Supplier<T> beanProvider) {
        return _SingletonBeanProvider.of(beanClass.getName(), beanClass, beanProvider);
    }

    public static <T> _SingletonBeanProvider forTesting(final T bean) {
        return _SingletonBeanProvider.of(bean.getClass().getName(), bean.getClass(), ()->bean);
    }

    // -- CONSTRUCTION

    /**
     * Unique bean name (not an alias).
     * Corresponds to the logical-type-name (Causeway semantics).
     */
    private final @NonNull String id;
    private final @NonNull Class<?> beanClass;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    private final @NonNull Supplier<?> beanProvider;

    public Optional<?> lookupInstance() {
        return Optional.ofNullable(beanProvider.get());
    }

    public Object getInstanceElseFail() {
        return lookupInstance().orElseThrow(
                ()->_Exceptions.noSuchElement("Cannot create bean instance for name '%s' (with required type %s)",
                        id,
                        beanClass.getName()));
    }

    public boolean isCandidateFor(final @Nullable Class<?> requiredType) {
        if(requiredType==null) return false;
        return requiredType.isAssignableFrom(beanClass);
    }

    // -- UTILITY

    public static Predicate<_SingletonBeanProvider> satisfying(final Class<?> requiredType) {
        return singletonProvider->singletonProvider.isCandidateFor(requiredType);
    }

}