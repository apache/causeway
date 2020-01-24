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

package org.apache.isis.applib.services.factory;

import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import org.apache.isis.core.commons.exceptions.IsisException;

public interface FactoryService {

    /**
     * Carefree general purpose factory method, to automatically get or create an instance of
     * {@code requiredType}. 
     * <p>
     * Maps onto one of the specialized factory methods {@link #get(Class)} or {@link #create(Class)} 
     * based on the type's meta-data.
     * @param <T>
     * @param requiredType
     * @return
     * @throws NoSuchElementException if result is empty
     * @throws IsisException if instance creation failed
     */
    <T> T getOrCreate(Class<T> requiredType);
    
    /**
     * Gets an instance (possibly shared or independent) of the specified {@code requiredType}, 
     * with injection points resolved 
     * and any life-cycle callback processed.
     * 
     * @param <T>
     * @param requiredType - only applicable to IoC container managed types
     * @return (non-null), an instance of {@code requiredType}, if available and unique
     * (i.e. not multiple candidates found with none marked as primary)
     * 
     * @throws NoSuchElementException if result is empty
     * @throws IsisException if instance creation failed
     * 
     * @apiNote does not force the requiredType to be added to the meta-model
     * 
     * @since 2.0
     */
    <T> T get(Class<T> requiredType);
    
    /**
     * Creates a new detached entity instance, with injection points resolved
     * and defaults applied.
     * @param <T>
     * @param domainClass - only applicable to entity types
     * @return
     * @throws IllegalArgumentException if domainClass is not an entity type  
     * @apiNote forces the domainClass to be added to the meta-model if not already
     */
    <T> T detachedEntity(Class<T> domainClass);

    /**
     * Creates a new Mixin instance, with injection points resolved.
     * @param <T>
     * @param mixinClass
     * @param mixedIn
     * @return
     * @throws IllegalArgumentException if mixinClass is not a mixin type
     * @apiNote forces the mixinClass to be added to the meta-model if not already
     */
    <T> T mixin(Class<T> mixinClass, Object mixedIn);

    /**
     * Creates a new ViewModel instance, with injection points resolved, 
     * and initialized according to the given {@code mementoStr} 
     * @param viewModelClass
     * @param mementoStr - ignored if {@code null}
     * @throws IllegalArgumentException if viewModelClass is not a viewmodel type
     * @apiNote forces the viewModelClass to be added to the meta-model if not already
     * @since 2.0
     */
    <T> T viewModel(Class<T> viewModelClass, @Nullable String mementoStr);

    /**
     * Creates a new ViewModel instance, 
     * with injection points resolved
     * and defaults applied. 
     * @param viewModelClass
     * @throws IllegalArgumentException if viewModelClass is not a viewmodel type
     * @apiNote forces the viewModelClass to be added to the meta-model if not already
     * @since 2.0
     */
    default <T> T viewModel(Class<T> viewModelClass) {
        return viewModel(viewModelClass, /*mementoStr*/null);
    }

    /**
     * Creates a new instance of the specified class, 
     * with injection points resolved
     * and defaults applied.
     * @param domainClass - not applicable to IoC container managed types
     * @throws IllegalArgumentException if domainClass is not an IoC container managed type
     * @apiNote forces the domainClass to be added to the meta-model if not already
     * @since 2.0
     */
    <T> T create(Class<T> domainClass);


}
