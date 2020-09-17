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
package org.apache.isis.commons.internal.ioc;

import java.lang.annotation.Annotation;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.internal.base._With;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
public interface IocContainer {

    Stream<ManagedBeanAdapter> streamAllBeans();

    Optional<?> lookupById(final String id);

    /**
     * Return an instance (possibly shared or independent) of the object managed by the IoC container.
     * @param <T>
     * @param requiredType
     * @return an instance of the bean, or null if not available or not unique
     * (i.e. multiple candidates found with none marked as primary)
     * @throws IsisException if instance creation failed
     */
    <T> Optional<T> get(Class<T> requiredType);    
    
    <T> Can<T> select(Class<T> requiredType);
    
    <T> Can<T> select(Class<T> requiredType, Set<Annotation> qualifiersRequired);

    /**
     * @return IoC managed singleton wrapped in an Optional
     */
    public default <T> Optional<T> getSingleton(@Nullable Class<T> type) {
        if(type==null) {
            return Optional.empty();
        }
        return select(type).getSingleton();
    }

    /**
     * @return IoC managed singleton
     * @throws NoSuchElementException - if the singleton is not resolvable
     */
    public default <T> T getSingletonElseFail(@Nullable Class<T> type) {
        _With.requires(type, "type");
        
        val candidates = select(type);
        
        switch (candidates.getCardinality()) {
        case ZERO:
            throw _Exceptions.noSuchElement("Cannot resolve singleton '%s'", type);
        case ONE:
            return candidates.getFirstOrFail();
        default:
            throw _Exceptions.unrecoverableFormatted("Cannot resolve singleton '%s' got more than one: {%s}",
                    type,
                    candidates.stream()
                    .map(Object::getClass)
                    .map(Class::getName)
                    .collect(Collectors.joining(", "))
                    );
        }

    }



}
