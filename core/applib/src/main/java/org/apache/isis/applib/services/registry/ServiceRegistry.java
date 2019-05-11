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

import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.base._Reduction;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.commons.internal.spring._Spring;
import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.core.commons.collections.Bin;

import lombok.val;

/**
 * 
 * @since 2.0.0
 *
 */
public interface ServiceRegistry {


    /**
     * Whether or not the given type is annotated @DomainService.
     * @param cls
     * @return
     */
    boolean isDomainServiceType(Class<?> cls);

    /**
     * Obtains a child Instance for the given required type and additional required qualifiers. 
     * @param type
     * @param qualifiers
     * @return an optional, empty if passed two instances of the same qualifier type, or an 
     * instance of an annotation that is not a qualifier type
     */
    default public <T> Bin<T> select(
            final Class<T> type, Annotation[] qualifiers){

        //CDI variant, just keep comment as a reference
        //return _CDI.select(type, _CDI.filterQualifiers(qualifiers)); 
        return _Spring.select(type, _Spring.filterQualifiers(qualifiers));
    }

    default public <T> Bin<T> select(final Class<T> type){
        return select(type, _Constants.emptyAnnotations);
    }

    /**
     * Returns all bean adapters implementing the requested type.
     */
    default Stream<BeanAdapter> streamRegisteredBeansOfType(Class<?> requiredType) {
        return streamRegisteredBeans()
                .filter(beanAdapter->beanAdapter.isCandidateFor(requiredType));
    }

    /**
     * Returns all bean adapters that have been registered.
     */
    public Stream<BeanAdapter> streamRegisteredBeans();

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

    /**
     * 
     * @deprecated use streamRegisteredBeans() instead, then on call-site
     * don't keep service instances, instead keep BeanAdpaters 
     */
    @Deprecated //TODO [2033] as long as services are wrapped into ObjectAdapters that require a 
    // pojo, this is still required
    Stream<Object> streamServices();

    /**
     * @param cls
     * @return whether the exact type is registered as service
     */
    @Deprecated //TODO [2033] marked deprecated, because this should not be required by the 
    // framework at all, its also hard to implement correctly
    boolean isRegisteredBean(Class<?> cls);

    /**
     * synonym for isRegisteredBean
     * @param cls
     * @return
     */
    @Deprecated //TODO [2033] marked deprecated, because this should not be required by the
    default boolean isService(Class<?> cls) {
        return isRegisteredBean(cls);
    }

    /**
     * Verify domain service Ids are unique.
     * @throws IllegalStateException - if validation fails
     */
    void validateServices();


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
