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

package org.apache.isis.applib.services.registry;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Priority;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.base._Reduction;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.ioc._ManagedBeanAdapter;

import lombok.val;

/**
 * Collects together methods for injecting or looking up domain services
 * (either provided by the framework or application-specific) currently known
 * to the runtime.
 *
 * @since 1.x {@index}
 */
public interface ServiceRegistry {

    /**
     * Obtains a {@link Can} container containing any matching instances for the given required type
     * and additional required qualifiers.
     *
     * @param type
     * @param qualifiers
     * @return non-null
     */
    <T> Can<T> select(Class<T> type, Annotation[] qualifiers);

    /**
     * Obtains a {@link Can} container containing any matching instances for the given required type.
     *
     * @param type
     * @return non-null
     */
    default <T> Can<T> select(final Class<T> type){

        return select(type, _Constants.emptyAnnotations);
    }

    /**
     * Streams all registered bean adapters implementing the requested type.
     */
    default Stream<_ManagedBeanAdapter> streamRegisteredBeansOfType(Class<?> requiredType) {

        return streamRegisteredBeans()
                .filter(beanAdapter->beanAdapter.isCandidateFor(requiredType));
    }

    /**
     * Returns all bean adapters that have been registered.
     */
    Stream<_ManagedBeanAdapter> streamRegisteredBeans();

    /**
     * Returns a registered bean of given {@code name}.
     *
     * @param id - corresponds to the ObjectSpecificationId of the bean's type
     */
    Optional<_ManagedBeanAdapter> lookupRegisteredBeanById(String id);

    /**
     * Returns a registered bean of given {@code name}, or throws when no such bean.
     *
     * @param id - corresponds to the ObjectSpecificationId of the bean's type
     */
    default _ManagedBeanAdapter lookupRegisteredBeanByIdElseFail(String id) {
        return lookupRegisteredBeanById(id).orElseThrow(
                ()->_Exceptions.unrecoverable(
                        "Failed to lookup BeanAdapter by id '" + id + "'"));
    }

    Optional<?> lookupBeanById(final String id);

    /**
     * Returns a domain service implementing the requested type.
     *
     * <p>
     * If this lookup is ambiguous, the service annotated with highest priority is returned.
     * see {@link Priority}
     * </p>
     */
    default <T> Optional<T> lookupService(final Class<T> serviceClass) {
        final Comparator<Object> comparator = InstanceByPriorityComparator.instance();
        return lookupService(serviceClass, comparator);
    }

    /**
     * Returns a domain service implementing the requested type.
     *
     * <p>
     * If this lookup is ambiguous, then the provided comparator is used.
     * </p>
     */
    default <T> Optional<T> lookupService(Class<T> serviceClass, Comparator<Object> comparator) {
        val bin = select(serviceClass);
        if(bin.isEmpty()) {
            return Optional.empty();
        }
        if(bin.isCardinalityOne()) {
            return bin.getSingleton();
        }
        // dealing with ambiguity, get the one, with highest priority annotated

        val toComparatorReduction =
                //TODO [2033] not tested yet, whether the 'direction' is correct < vs >
                _Reduction.<T>of((max, next)-> {
                    final boolean b = comparator.compare(next, max) > 0;
                    return b ? next : max;
                });

        bin.forEach(toComparatorReduction);

        return toComparatorReduction.getResult();
    }

    /**
     * Looks up a domain service of the requested type (same as
     * {@link #lookupService(Class)}) but throws a
     * {@link NoSuchElementException} if there are no such instances.
     *
     * @param serviceClass
     * @param <T>
     */
    default <T> T lookupServiceElseFail(final Class<T> serviceClass) {

        return lookupService(serviceClass)
                .orElseThrow(()->
                new NoSuchElementException("Could not locate service of type '" + serviceClass + "'"));

        // ...
    }

    /**
     * Invalidates any cached service adapters that might hold a reference to
     * the current {@link SpecificationLoader}. Particularly useful when discarding
     * a meta-model instance, that is, purging the {@link ObjectSpecification} cache.
     */
    void clearRegisteredBeans();


}
