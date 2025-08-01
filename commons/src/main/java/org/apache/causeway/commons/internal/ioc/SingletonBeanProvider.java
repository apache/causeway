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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.exceptions._Exceptions;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Framework internal Bean provider support.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0
 */
public record SingletonBeanProvider(
        /**
         * Unique bean name (not an alias).
         * Corresponds to the logical-type-name (Causeway semantics).
         */
        @NonNull String id,
        @NonNull Class<?> beanClass,

        //@ToString.Exclude @EqualsAndHashCode.Exclude
        @NonNull Supplier<?> beanProvider) {

    // -- TEST FACTORIES

    public static <T> SingletonBeanProvider forTestingLazy(
            final String logicalTypeName, final Class<T> beanClass, final Supplier<T> beanProvider) {
        return new SingletonBeanProvider(logicalTypeName, beanClass, beanProvider);
    }

    public static <T> SingletonBeanProvider forTestingLazy(
            final Class<T> beanClass, final Supplier<T> beanProvider) {
        return new SingletonBeanProvider(beanClass.getName(), beanClass, beanProvider);
    }

    public static <T> SingletonBeanProvider forTesting(final T bean) {
        return new SingletonBeanProvider(bean.getClass().getName(), bean.getClass(), ()->bean);
    }

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

    @Override
    public final boolean equals(final Object obj) {
        return obj instanceof SingletonBeanProvider other
                ? Objects.equals(this.id(), other.id())
                        && Objects.equals(this.beanClass(), other.beanClass())
                : false;
    }
    @Override
    public final int hashCode() {
        return Objects.hash(id, beanClass);
    }
    @Override
    public final String toString() {
        return "SingletonBeanProvider[id=%s, beanClass=%s]".formatted(id, beanClass);
    }

    // -- UTILITY

    public static Predicate<SingletonBeanProvider> satisfying(final Class<?> requiredType) {
        return singletonProvider->singletonProvider.isCandidateFor(requiredType);
    }

}