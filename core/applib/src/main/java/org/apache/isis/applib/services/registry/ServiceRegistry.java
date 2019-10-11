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

import org.apache.isis.commons.collections.Bin;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.base._Reduction;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.ioc.ManagedBeanAdapter;
import org.apache.isis.commons.internal.ioc.spring._Spring;
import org.apache.isis.commons.internal.reflection._Reflect;

import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
public interface ServiceRegistry {


    /**
     * Obtains a Bin container containing any matching instances for the given required type 
     * and additional required qualifiers. 
     * @param type
     * @param qualifiers
     * @return non-null
     * 
     */
    default public <T> Bin<T> select(
            final Class<T> type, Annotation[] qualifiers){

        //CDI variant, just keep comment as a reference
        //return _CDI.select(type, _CDI.filterQualifiers(qualifiers)); 
        return _Spring.select(type, _Spring.filterQualifiers(qualifiers));
    }

    /**
     * Obtains a Bin container containing any matching instances for the given required type. 
     * @param type
     * @return non-null
     * 
     */
    default public <T> Bin<T> select(final Class<T> type){
        return select(type, _Constants.emptyAnnotations);
    }

    /**
     * Streams all registered bean adapters implementing the requested type.
     */
    default Stream<ManagedBeanAdapter> streamRegisteredBeansOfType(Class<?> requiredType) {
        return streamRegisteredBeans()
                .filter(beanAdapter->beanAdapter.isCandidateFor(requiredType));
    }

    /**
     * Returns all bean adapters that have been registered.
     */
    public Stream<ManagedBeanAdapter> streamRegisteredBeans();

    /**
     * Returns a registered bean of given {@code name}.
     *   
     * @param id - corresponds to the ObjectSpecificationId of the bean's type
     */
    public Optional<ManagedBeanAdapter> lookupRegisteredBeanById(String id);


    /**
     * Returns a registered bean of given {@code name}, or throws when no such bean.
     *   
     * @param id - corresponds to the ObjectSpecificationId of the bean's type
     */
    public default ManagedBeanAdapter lookupRegisteredBeanByNameElseFail(String id) {
        return lookupRegisteredBeanById(id).orElseThrow(
                ()->_Exceptions.unrecoverable(
                        "Failed to lookup BeanAdapter by id '" + id + "'")); 
    }

    /**
     * Returns a domain service implementing the requested type.
     * <p>
     * If this lookup is ambiguous, the service annotated with highest priority is returned.
     * see {@link Priority}   
     */
    default public <T> Optional<T> lookupService(final Class<T> serviceClass) {

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
    }

    public default <T> T lookupServiceElseFail(final Class<T> serviceClass) {
        return lookupService(serviceClass)
                .orElseThrow(()->
                new NoSuchElementException("Could not locate service of type '" + serviceClass + "'"));
    }
    
//XXX too slow
//    /**
//     * @param requiredType
//     * @return whether the requiredType can be resolved to a container managed bean
//     */
//    public default boolean isResolvableBean(Class<?> requiredType) {
//        return streamRegisteredBeans()
//                .anyMatch(bean->bean.getBeanClass().isAssignableFrom(requiredType));
//    }

    // -- PRIORITY ANNOTATION HANDLING

    static class InstanceByPriorityComparator implements Comparator<Object> {

        private final static InstanceByPriorityComparator INSTANCE =
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


}
