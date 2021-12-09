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

import org.springframework.lang.Nullable;

import org.apache.isis.applib.exceptions.UnrecoverableException;
import org.apache.isis.applib.services.bookmark.Bookmark;

import lombok.NonNull;

/**
 * Collects together methods for instantiating domain objects, also injecting
 * them with any domain services and calling lifecycle methods if defined.
 *
 * @since 1.x {@index}
 */
public interface FactoryService {

    /**
     * General purpose factory method, to automatically get or create an instance of
     * {@code requiredType}.
     *
     * <p>
     * Maps onto one of the specialized factory methods {@link #get(Class)} or {@link #create(Class)}
     * based on the type's meta-data.
     * </p>
     *
     * @param <T>
     * @param requiredType
     * @throws NoSuchElementException if result is empty
     * @throws UnrecoverableException if instance creation failed
     * @throws IllegalArgumentException if requiredType is not recognized by the meta-model
     *
     * @since 2.0
     *
     */

    /**
     * Gets an instance (possibly shared or independent) of the specified type, with injection points resolved and any
     * life-cycle callback processed.
     * @param requiredType
     * @param <T>
     */
    <T> T getOrCreate(@NonNull Class<T> requiredType);

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
     * @throws UnrecoverableException if instance creation failed
     *
     * @apiNote does not force the requiredType to be added to the meta-model
     * @since 2.0
     */
    <T> T get(@NonNull Class<T> requiredType);

    /**
     * Creates a new detached entity instance, with injection points resolved
     * and defaults applied.
     *
     * <p>
     *     The entity will be detacted, in other words not yet persisted.
     * </p>
     *
     * @param <T>
     * @param domainClass - only applicable to entity types
     * @throws IllegalArgumentException if domainClass is not an entity type
     * @apiNote forces the domainClass to be added to the meta-model if not already
     * @since 2.0
     */
    <T> T detachedEntity(@NonNull Class<T> domainClass);

    /**
     * Creates a new detached entity instance, with injection points resolved.
     *
     * @param <T>
     * @param entity - most likely just new-ed up, without injection points resolved
     * @throws IllegalArgumentException if domainClass is not an entity type
     * @apiNote forces the domainClass to be added to the meta-model if not already
     * @since 2.0
     */
    <T> T detachedEntity(@NonNull T entity);

    /**
     * Creates a new Mixin instance, with injection points resolved.
     *
     * @param <T>
     * @param mixinClass
     * @param mixedIn
     * @throws IllegalArgumentException if mixinClass is not a mixin type
     * @apiNote forces the mixinClass to be added to the meta-model if not already
     */
    <T> T mixin(@NonNull Class<T> mixinClass, @NonNull Object mixedIn);

    /**
     * Creates a new ViewModel instance, with injection points resolved,
     * and initialized according to the given {@code bookmark}
     *
     * @param viewModelClass
     * @param bookmark - ignored if {@code null}
     * @throws IllegalArgumentException if viewModelClass is not a viewmodel type
     * @apiNote forces the viewModelClass to be added to the meta-model if not already
     * @since 2.0
     */
    <T> T viewModel(@NonNull Class<T> viewModelClass, @Nullable Bookmark bookmark);

    /**
     * Creates a new ViewModel instance,
     * with injection points resolved
     * and defaults applied.
     * @param viewModelClass
     * @throws IllegalArgumentException if viewModelClass is not a viewmodel type
     * @apiNote forces the viewModelClass to be added to the meta-model if not already
     * @since 2.0
     */
    default <T> T viewModel(@NonNull final Class<T> viewModelClass) {
        return viewModel(viewModelClass, /*bookmark*/null);
    }

    /**
     * Resolves injection points for given ViewModel instance.
     * @param viewModel - most likely just new-ed up, without injection points resolved
     * @throws IllegalArgumentException if viewModelClass is not a viewmodel type
     * @apiNote forces the viewModel's class to be added to the meta-model if not already
     * @since 2.0
     */
    <T> T viewModel(@NonNull T viewModel);

    /**
     * Creates a new instance of the specified class,
     * with injection points resolved
     * and defaults applied.
     * @param domainClass - not applicable to IoC container managed types
     * @throws IllegalArgumentException if domainClass is an IoC container managed type,
     *      or not recognized by the meta-model
     * @apiNote forces the domainClass to be added to the meta-model if not already
     * @since 2.0
     */
    <T> T create(@NonNull Class<T> domainClass);

}
