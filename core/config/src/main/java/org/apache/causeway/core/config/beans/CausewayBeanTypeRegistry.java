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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Maps;

import lombok.Getter;
import lombok.NonNull;

/**
 * Holds the set of domain services, persistent entities and fixture scripts etc., but not values.
 * @since 2.0
 */
public class CausewayBeanTypeRegistry {

    // -- CONSTRUCTOR

    public CausewayBeanTypeRegistry(final @NonNull Can<CausewayBeanMetaData> introspectableTypes) {
        this.introspectableTypes = introspectableTypes;

        introspectableTypes.forEach(typeMeta->{

            var cls = typeMeta.getCorrespondingClass();

            introspectableTypesByClass.put(typeMeta.getCorrespondingClass(), typeMeta);

            switch (typeMeta.getBeanSort()) {
            case MANAGED_BEAN_CONTRIBUTING:
                managedBeansContributing.put(cls, typeMeta);
                return;
            case MIXIN:
                mixinTypes.put(cls, typeMeta);
                return;
            case ENTITY:
                entityTypes.put(cls, typeMeta);
                return;
            case VIEW_MODEL:
                viewModelTypes.put(cls, typeMeta);
                return;
            case VALUE:
                discoveredValueTypes.put(cls, typeMeta);
                return;

            // skip introspection for these
            case MANAGED_BEAN_NOT_CONTRIBUTING:
            case COLLECTION:
            case ABSTRACT: // <-- unexpected code reach
            case VETOED:
            case UNKNOWN:
                return;
            }
        });

    }

    // -- SHORTCUTS

    public Stream<Class<?>> streamMixinTypes() {
        return getMixinTypes().keySet().stream();
    }

    // -- LOOKUPS

    /**
     * If given type is part of the meta-model and is available for injection,
     * returns the <em>Managed Bean's</em> name (id) as
     * recognized by the IoC container.
     */
    public Optional<String> lookupManagedBeanNameForType(final Class<?> type) {
        return Optional.ofNullable(getManagedBeansContributing().get(type))
                .map(CausewayBeanMetaData::getBeanName);
    }

    /**
     * Returns 'JDO' or 'JPA' based on metadata found during {@link CausewayBeanTypeClassifier type-classification}.
     * If no (concrete) entity type is found, returns 'UNSPECIFIED'.
     * @implNote assumes that there can be only one persistence stack
     */
    public PersistenceStack determineCurrentPersistenceStack() {
        return getEntityTypes().values().stream()
            .map(meta->meta.getPersistenceStack())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(persistenceStack->!persistenceStack.isUnspecified())
            .findFirst()
            .orElse(PersistenceStack.UNSPECIFIED);
    }


    /**
     * (immutable) scan result, as used by the SpecificationLoader for introspection
     */
    private final Can<CausewayBeanMetaData> introspectableTypes;

    private final Map<Class<?>, CausewayBeanMetaData> introspectableTypesByClass = _Maps.newHashMap();

    // -- DISTINCT CATEGORIES OF BEAN SORTS

    @Getter
    private final Map<Class<?>, CausewayBeanMetaData> managedBeansContributing = new HashMap<>();

    @Getter
    private final Map<Class<?>, CausewayBeanMetaData> entityTypes = new HashMap<>();

    @Getter
    private final Map<Class<?>, CausewayBeanMetaData> mixinTypes = new HashMap<>();

    @Getter
    private final Map<Class<?>, CausewayBeanMetaData> viewModelTypes = new HashMap<>();

    @Getter
    private final Map<Class<?>, CausewayBeanMetaData> discoveredValueTypes = new HashMap<>();

    // -- LOOKUPS

    public Optional<CausewayBeanMetaData> lookupIntrospectableType(final Class<?> type) {
        return Optional.ofNullable(introspectableTypesByClass.get(type));
    }

    // -- ITERATORS

    public Stream<CausewayBeanMetaData> streamIntrospectableTypes() {
        return _NullSafe.stream(introspectableTypes);
    }

}
