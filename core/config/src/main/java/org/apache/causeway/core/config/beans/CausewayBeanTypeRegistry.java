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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.annotations.BeanInternal;

import lombok.NonNull;

/**
 * Holds discovered domain types grouped by bean-sort.
 */
@BeanInternal
public class CausewayBeanTypeRegistry {

    /**
     * (immutable) scan result, as used by the SpecificationLoader for introspection
     */
    private final Can<CausewayBeanMetaData> introspectableTypes;
    private final Map<Class<?>, CausewayBeanMetaData> introspectableTypesByClass = new HashMap<>();

    // -- DISTINCT CATEGORIES OF BEAN SORTS

    private final Map<Class<?>, CausewayBeanMetaData> managedBeansContributing = new HashMap<>();
    private final Map<Class<?>, CausewayBeanMetaData> entityTypes = new HashMap<>();
    private final Map<Class<?>, CausewayBeanMetaData> viewmodelTypes = new HashMap<>();
    private final Map<Class<?>, CausewayBeanMetaData> mixinTypes = new HashMap<>();
    private final Map<Class<?>, CausewayBeanMetaData> valueTypes = new HashMap<>();

    // -- CONSTRUCTOR

    public CausewayBeanTypeRegistry(final @NonNull Can<CausewayBeanMetaData> introspectableTypes) {
        this.introspectableTypes = introspectableTypes;

        introspectableTypes.forEach(typeMeta->{

            var cls = typeMeta.getCorrespondingClass();

            introspectableTypesByClass.put(typeMeta.getCorrespondingClass(), typeMeta);

            switch (typeMeta.beanSort()) {
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
                viewmodelTypes.put(cls, typeMeta);
                return;
            case VALUE:
                valueTypes.put(cls, typeMeta);
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
    
    // -- SIZE
    
    public int managedBeansContributingCount() { return managedBeansContributing.size(); }
    public int entityTypeCount() { return entityTypes.size(); }
    public int viewmodelTypeCount() { return viewmodelTypes.size(); }
    public int mixinTypeCount() { return mixinTypes.size(); }
    public int valueTypeCount() { return valueTypes.size(); }

    // -- STREAM

    public Stream<CausewayBeanMetaData> streamIntrospectableTypes() { return introspectableTypes.stream(); }
    public Stream<Class<?>> streamManagedBeansContributing() { return managedBeansContributing.keySet().stream(); }
    public Stream<Class<?>> streamEntityTypes() { return entityTypes.keySet().stream(); }
    public Stream<Class<?>> streamViewmodelTypes() { return entityTypes.keySet().stream(); }
    public Stream<Class<?>> streamMixinTypes() { return mixinTypes.keySet().stream(); }
    public Stream<Class<?>> streamValueTypes() { return valueTypes.keySet().stream(); }
    
    // -- AS SET

    public Set<Class<?>> entityTypeSet() { return Collections.unmodifiableSet(entityTypes.keySet()); }

    // -- LOOKUP

    /**
     * If given type is part of the meta-model and is available for injection,
     * returns the <em>Managed Bean's</em> name (id) as
     * recognized by the IoC container.
     */
    public Optional<String> lookupManagedBeanNameForType(final Class<?> type) {
        return Optional.ofNullable(managedBeansContributing.get(type))
                .map(CausewayBeanMetaData::getBeanName);
    }
    
    public boolean containsManagedBeansContributing(@NonNull Class<?> type) {
        return managedBeansContributing.containsKey(type);
    }

    /**
     * Returns 'JDO' or 'JPA' based on metadata found during {@link CausewayBeanTypeClassifier type-classification}.
     * If no (concrete) entity type is found, returns 'UNSPECIFIED'.
     * @implNote assumes that there can be only one persistence stack
     */
    public PersistenceStack determineCurrentPersistenceStack() {
        return entityTypes.values().stream()
            .map(meta->meta.persistenceStack())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(persistenceStack->!persistenceStack.isUnspecified())
            .findFirst()
            .orElse(PersistenceStack.UNSPECIFIED);
    }

    public Optional<CausewayBeanMetaData> lookupIntrospectableType(final Class<?> type) {
        return Optional.ofNullable(introspectableTypesByClass.get(type));
    }

}
