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
import org.apache.isis.commons.internal.reflection._Reflect;

import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
// tag::refguide[]
public interface ServiceRegistry {

    /**
     * Obtains a {@link Can} container containing any matching instances for the given required type
     * and additional required qualifiers.
     *
     * @param type
     * @param qualifiers
     * @return non-null
     */
    // tag::refguide[]
    <T> Can<T> select(Class<T> type, Annotation[] qualifiers);

    /**
     * Obtains a {@link Can} container containing any matching instances for the given required type.
     *
     * @param type
     * @return non-null
     */
    // tag::refguide[]
    default <T> Can<T> select(final Class<T> type){
        // end::refguide[]

        return select(type, _Constants.emptyAnnotations);

        // tag::refguide[]
        // ...
    }

    // end::refguide[]
    /**
     * Streams all registered bean adapters implementing the requested type.
     */
    // tag::refguide[]
    default Stream<_ManagedBeanAdapter> streamRegisteredBeansOfType(Class<?> requiredType) {
        // end::refguide[]

        return streamRegisteredBeans()
                .filter(beanAdapter->beanAdapter.isCandidateFor(requiredType));

        // tag::refguide[]
        // ...
    }

    // end::refguide[]
    /**
     * Returns all bean adapters that have been registered.
     */
    // tag::refguide[]
    Stream<_ManagedBeanAdapter> streamRegisteredBeans();

    // end::refguide[]
    /**
     * Returns a registered bean of given {@code name}.
     *
     * @param id - corresponds to the ObjectSpecificationId of the bean's type
     */
    // tag::refguide[]
    Optional<_ManagedBeanAdapter> lookupRegisteredBeanById(String id);

    // end::refguide[]
    /**
     * Returns a registered bean of given {@code name}, or throws when no such bean.
     *
     * @param id - corresponds to the ObjectSpecificationId of the bean's type
     */
    // tag::refguide[]
    default _ManagedBeanAdapter lookupRegisteredBeanByIdElseFail(String id) {
        return lookupRegisteredBeanById(id).orElseThrow(
                ()->_Exceptions.unrecoverable(
                        "Failed to lookup BeanAdapter by id '" + id + "'"));
    }

    Optional<?> lookupBeanById(final String id);

    // end::refguide[]
    /**
     * Returns a domain service implementing the requested type.
     * <p>
     * If this lookup is ambiguous, the service annotated with highest priority is returned.
     * see {@link Priority}
     */
    // tag::refguide[]
    default <T> Optional<T> lookupService(final Class<T> serviceClass) {
        // end::refguide[]

        val bin = select(serviceClass);
        if(bin.isEmpty()) {
            return Optional.empty();
        }
        if(bin.isCardinalityOne()) {
            return bin.getSingleton();
        }
        // dealing with ambiguity, get the one, with highest priority annotated

        val prioComparator = InstanceByPriorityComparator.instance();
        val toMaxPrioReduction =
                //TODO [2033] not tested yet, whether the 'direction' is correct < vs >
                _Reduction.<T>of((max, next)-> prioComparator.leftIsHigherThanRight(next, max) ? next : max);

        bin.forEach(toMaxPrioReduction);

        return toMaxPrioReduction.getResult();

        // tag::refguide[]
        // ...
    }

    default <T> T lookupServiceElseFail(final Class<T> serviceClass) {
        // end::refguide[]

        return lookupService(serviceClass)
                .orElseThrow(()->
                new NoSuchElementException("Could not locate service of type '" + serviceClass + "'"));

        // tag::refguide[]
        // ...
    }
    // end::refguide[]

    // -- PRIORITY ANNOTATION HANDLING

    class InstanceByPriorityComparator implements Comparator<Object> {

        private static final InstanceByPriorityComparator INSTANCE =
                new InstanceByPriorityComparator();

        public static InstanceByPriorityComparator instance() {
            return INSTANCE;
        }

        @Override
        public int compare(Object o1, Object o2) {

            if(o1==null) {
                if(o2==null) {
                    return 0;
                } else {
                    return -1; // o1 < o2
                }
            }
            if(o2==null) {
                return 1; // o1 > o2
            }

            val prioAnnot1 = _Reflect.getAnnotation(o1.getClass(), Priority.class);
            val prioAnnot2 = _Reflect.getAnnotation(o2.getClass(), Priority.class);
            val prio1 = prioAnnot1!=null ? prioAnnot1.value() : 0;
            val prio2 = prioAnnot2!=null ? prioAnnot2.value() : 0;
            return Integer.compare(prio1, prio2);
        }

        public boolean leftIsHigherThanRight(Object left, Object right) {
            return compare(left, right) > 0;
        }

    }

    // tag::refguide[]
}
// end::refguide[]
