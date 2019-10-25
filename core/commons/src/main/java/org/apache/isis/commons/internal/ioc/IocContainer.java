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
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.commons.collections.Bin;
import org.apache.isis.commons.internal.exceptions._Exceptions;

/**
 * 
 * @since 2.0
 *
 */
public interface IocContainer {

    Stream<ManagedBeanAdapter> streamAllBeans();

    <T> Bin<T> select(Class<T> requiredType);
    
    <T> Bin<T> select(Class<T> requiredType, Set<Annotation> qualifiersRequired);

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
        return getSingleton(type)
                .orElseThrow(()->_Exceptions.noSuchElement("Cannot resolve singleton '%s'", type));

    }



}
