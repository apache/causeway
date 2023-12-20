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
package org.apache.causeway.core.config.beans;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;

import lombok.NonNull;

/**
 * Holds the set of domain services, persistent entities and fixture scripts etc., but not values.
 * @since 2.0
 */
public interface CausewayBeanTypeRegistry {

    Optional<CausewayBeanMetaData> lookupIntrospectableType(Class<?> type);
    Stream<CausewayBeanMetaData> streamIntrospectableTypes();

    @NonNull Map<Class<?>, CausewayBeanMetaData> getManagedBeansContributing();
    @NonNull Map<Class<?>, CausewayBeanMetaData> getEntityTypes();
    @NonNull Map<Class<?>, CausewayBeanMetaData> getMixinTypes();
    @NonNull Map<Class<?>, CausewayBeanMetaData> getViewModelTypes();
    /** discovered per {@code @Value} annotation (vs. registered using a {@link ValueSemanticsProvider})*/
    @NonNull Map<Class<?>, CausewayBeanMetaData> getDiscoveredValueTypes();

    // -- SHORTCUTS

    default Stream<Class<?>> streamMixinTypes() {
        return getMixinTypes().keySet().stream();
    }

    // -- LOOKUPS

    /**
     * If given type is part of the meta-model and is available for injection,
     * returns the <em>Managed Bean's</em> name (id) as
     * recognized by the IoC container.
     *
     * @param type
     */
    default Optional<String> lookupManagedBeanNameForType(final Class<?> type) {
        return Optional.ofNullable(getManagedBeansContributing().get(type))
                .map(CausewayBeanMetaData::getBeanName);
    }

    /**
     * Returns 'JDO' or 'JPA' based on metadata found during {@link CausewayBeanTypeClassifier type-classification}.
     * If no (concrete) entity type is found, returns 'UNSPECIFIED'.
     * @implNote assumes that there can be only one persistence stack
     */
    default PersistenceStack determineCurrentPersistenceStack() {
        return getEntityTypes().values().stream()
            .map(meta->meta.getPersistenceStack())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(persistenceStack->!persistenceStack.isUnspecified())
            .findFirst()
            .orElse(PersistenceStack.UNSPECIFIED);
    }

}