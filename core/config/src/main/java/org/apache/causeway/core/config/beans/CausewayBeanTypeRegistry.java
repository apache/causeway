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

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.HomePage;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData.PersistenceStack;

import lombok.NonNull;

/**
 * Holds discovered domain types grouped by bean-sort.
 * <p>
 * Except for view models, types need to be eagerly discovered by Spring,
 * hence collections of those types cannot be modified on the fly.
 */
@Programmatic
public class CausewayBeanTypeRegistry {

    public static CausewayBeanTypeRegistry empty() {
        return new CausewayBeanTypeRegistry(Can.empty());
    }
    
    /**
     * (immutable) scan result, as used by the SpecificationLoader for introspection
     */
    private final Can<CausewayBeanMetaData> scannedTypes;
    private final Can<CausewayBeanMetaData> entities;
    private final PersistenceStack persistenceStack;
    
    private final Map<Class<?>, CausewayBeanMetaData> scannedTypesByClass = new HashMap<>();

    // -- DISTINCT CATEGORIES OF BEAN SORTS

    private final Map<Class<?>, CausewayBeanMetaData> domainServices = new HashMap<>();
    private final Map<Class<?>, CausewayBeanMetaData> viewmodelTypes = new HashMap<>();
    private final Map<Class<?>, CausewayBeanMetaData> mixinTypes = new HashMap<>();
    private final Map<Class<?>, CausewayBeanMetaData> valueTypes = new HashMap<>();

    // -- CONSTRUCTOR

    CausewayBeanTypeRegistry(final @NonNull Can<CausewayBeanMetaData> scannedTypes) {
        this.scannedTypes = scannedTypes;

        var entityTypes = new HashMap<Class<?>, CausewayBeanMetaData>();
        
        scannedTypes.forEach(typeMeta->{

            var cls = typeMeta.getCorrespondingClass();

            scannedTypesByClass.put(cls, typeMeta);

            switch (typeMeta.beanSort()) {
            case MANAGED_BEAN_CONTRIBUTING:
                domainServices.put(cls, typeMeta);
                return;
            case MIXIN:
                mixinTypes.put(cls, typeMeta);
                return;
            case ENTITY:
                if(!typeMeta.persistenceStack().isPresent()) return;
                entityTypes.put(cls, typeMeta);
                return;
            case VIEW_MODEL:
                viewmodelTypes.put(cls, typeMeta);
                return;
            case VALUE:
                valueTypes.put(cls, typeMeta);
                return;

            // skip introspection for these
            case PROGRAMMATIC:
            case MANAGED_BEAN_NOT_CONTRIBUTING:
            case COLLECTION:
            case ABSTRACT: // <-- unexpected code reach
            case VETOED:
            case UNKNOWN:
                return;
            }
        });
        
        this.entities = Can.ofCollection(entityTypes.values());
        this.persistenceStack = entities.stream()
                .map(CausewayBeanMetaData::persistenceStack)
                .filter(PersistenceStack::isPresent)
                .findFirst()
                .orElse(PersistenceStack.UNSPECIFIED);
    }

    // -- FIELDS

    /**
     * Returns 'JDO' or 'JPA' based on metadata found during {@link CausewayBeanTypeClassifier type-classification}.
     * If no (concrete) entity type is found, returns 'UNSPECIFIED'.
     * @implNote assumes that there can be only one persistence stack
     */
    public PersistenceStack persistenceStack() {
        return persistenceStack; 
    }
    
    // -- STREAMS

    public Stream<CausewayBeanMetaData> streamScannedTypes() { return scannedTypes.stream(); }
    public Stream<Class<?>> streamDomainServices() { return domainServices.keySet().stream(); }
    public Stream<Class<?>> streamMixinTypes() { return mixinTypes.keySet().stream(); }
    public Stream<Class<?>> streamValueTypes() { return valueTypes.keySet().stream(); }
    public Stream<Class<?>> streamEntityTypes() { return streamEntityTypes(persistenceStack); }
    public Stream<Class<?>> streamEntityTypes(@Nullable final PersistenceStack selectedStack) {
        if(selectedStack==null || !selectedStack.isPresent()) return Stream.empty();
        return entities.stream()
                .filter(typeMeta->typeMeta.persistenceStack()==selectedStack)
                .<Class<?>>map(CausewayBeanMetaData::getCorrespondingClass);
    }
    
    // -- LOOKUP

    public Optional<String> lookupDomainServiceNameForType(final Class<?> type) {
        return Optional.ofNullable(domainServices.get(type))
                .map(CausewayBeanMetaData::getBeanName);
    }

    public boolean containsManagedBeansContributing(@NonNull final Class<?> type) {
        return domainServices.containsKey(type);
    }
    
    public Optional<CausewayBeanMetaData> lookupScannedType(final Class<?> type) {
        return Optional.ofNullable(scannedTypesByClass.get(type));
    }

    /**
     * Will only find if initially discovered by Spring.
     */
    public Optional<Class<?>> findHomepageViewmodel() {
        var classCache = _ClassCache.getInstance();
        return viewmodelTypes.keySet().stream()
                .filter(viewModelType -> classCache.head(viewModelType).hasAnnotation(HomePage.class))
                .findFirst();
    }

}
