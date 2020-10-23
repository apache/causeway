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
package org.apache.isis.core.config.beans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Holds the set of domain services, persistent entities and fixture scripts etc.
 * @since 2.0
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE) 
//@Log4j2
final class IsisBeanTypeRegistryImpl 
implements IsisBeanTypeRegistry {

    /**
     * (quasi-immutable) scan result, as used by the SpecificationLoader for introspection
     * 
     * @implSpec once set, expected to not being modified 
     */
    private Can<IsisBeanMetaData> introspectableTypes;
    
    private final Map<Class<?>, IsisBeanMetaData> introspectableTypesByClass = _Maps.newHashMap();

    // -- DISTINCT CATEGORIES OF BEAN SORTS
    
    private final Map<Class<?>, String> managedBeanNamesByType = new HashMap<>();
    @Getter(onMethod_ = {@Override}) private final Set<Class<?>> managedBeansContributing = new HashSet<>();
    @Getter(onMethod_ = {@Override}) private final Set<Class<?>> managedBeansNotContributing = new HashSet<>();
    @Getter(onMethod_ = {@Override}) private final Set<Class<?>> entityTypes = new HashSet<>();
    @Getter(onMethod_ = {@Override}) private final Set<Class<?>> mixinTypes = new HashSet<>();
    @Getter(onMethod_ = {@Override}) private final Set<Class<?>> viewModelTypes = new HashSet<>();
    private final Set<Class<?>> vetoedTypes = _Sets.newConcurrentHashSet();
    
    private final Can<Set<? extends Class<? extends Object>>> allCategorySets = Can.of(
            entityTypes,
            mixinTypes,
            viewModelTypes,
            vetoedTypes
            );

    @Override
    public void clear() {
        managedBeanNamesByType.clear();
        introspectableTypesByClass.clear();
        allCategorySets.forEach(Set::clear);
    }
    
    @Override
    public void veto(Class<?> type) {
        vetoedTypes.add(type);
    }

    // -- LOOKUPS
    
    @Override
    public Optional<String> lookupManagedBeanNameForType(Class<?> type) {
        if(vetoedTypes.contains(type)) { // vetos are coming from the spec-loader during init
            return Optional.empty();
        }
        return Optional.ofNullable(managedBeanNamesByType.get(type));
    }

    @Override
    public Optional<BeanSort> lookupBeanSortByIntrospectableType(Class<?> type) {
        return Optional.ofNullable(introspectableTypesByClass.get(type))
                .map(IsisBeanMetaData::getBeanSort);
    }
    
    // -- ITERATORS
    
    @Override
    public Stream<IsisBeanMetaData> streamIntrospectableTypes() {
        return _NullSafe.stream(introspectableTypes);
    }
    
    // -- INIT
    
    void setIntrospectableTypes(Can<IsisBeanMetaData> introspectableTypes) {
        this.introspectableTypes = introspectableTypes;
        clear();
        
        introspectableTypes.forEach(type->{
            
            val cls = type.getCorrespondingClass();
            
            introspectableTypesByClass.put(type.getCorrespondingClass(), type);
            
            switch (type.getBeanSort()) {
            case MANAGED_BEAN_CONTRIBUTING:
                managedBeansContributing.add(cls);
                managedBeanNamesByType.put(cls, type.getBeanName());
                return;
            case MANAGED_BEAN_NOT_CONTRIBUTING:
                managedBeansNotContributing.add(cls);
                managedBeanNamesByType.put(cls, type.getBeanName());
                return;
            case MIXIN:
                mixinTypes.add(cls);
                return;
            case ENTITY:
                entityTypes.add(cls);
                return;
            case VIEW_MODEL:
                viewModelTypes.add(cls);
                return;
            
            // skip introspection for these
            case COLLECTION:
            case VALUE:
            case UNKNOWN:
                return;
            }    
        });
        
    }
    
}