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

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

@Service
@Named("isisMetaModel.IsisBeanTypeRegistryImpl")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("Default")
//@Log4j2
public class IsisBeanTypeRegistryDefault implements IsisBeanTypeRegistry {

    /**
     * (immutable) scan result, as used by the SpecificationLoader for introspection
     */
    private final Can<IsisBeanMetaData> introspectableTypes;
    
    private final Map<Class<?>, IsisBeanMetaData> introspectableTypesByClass = _Maps.newHashMap();

    // -- DISTINCT CATEGORIES OF BEAN SORTS
    
    @Getter(onMethod_ = {@Override}) private final Map<Class<?>, IsisBeanMetaData> managedBeansContributing = new HashMap<>();
    @Getter(onMethod_ = {@Override}) private final Set<Class<?>> entityTypesJpa = new HashSet<>();
    @Getter(onMethod_ = {@Override}) private final Set<Class<?>> entityTypesJdo = new HashSet<>();
    @Getter(onMethod_ = {@Override}) private final Set<Class<?>> mixinTypes = new HashSet<>();
    @Getter(onMethod_ = {@Override}) private final Set<Class<?>> viewModelTypes = new HashSet<>();
    
    // -- LOOKUPS
    
    @Override
    public Optional<IsisBeanMetaData> lookupIntrospectableType(Class<?> type) {
        return Optional.ofNullable(introspectableTypesByClass.get(type));
    }
    
    // -- ITERATORS
    
    @Override
    public Stream<IsisBeanMetaData> streamIntrospectableTypes() {
        return _NullSafe.stream(introspectableTypes);
    }
    
    // -- CONSTRUCTOR
    
    @Inject @Named("isis.bean-meta-data")
    public IsisBeanTypeRegistryDefault(final @NonNull Can<IsisBeanMetaData> introspectableTypes) {
        this.introspectableTypes = introspectableTypes;
        
        introspectableTypes.forEach(type->{
            
            val cls = type.getCorrespondingClass();
            
            introspectableTypesByClass.put(type.getCorrespondingClass(), type);
            
            switch (type.getBeanSort()) {
            case MANAGED_BEAN_CONTRIBUTING:
                managedBeansContributing.put(cls, type);
                return;
            case MIXIN:
                mixinTypes.add(cls);
                return;
            case ENTITY_JDO:
                entityTypesJdo.add(cls);
                return;
            case ENTITY_JPA:
                entityTypesJpa.add(cls);
                return;
            case VIEW_MODEL:
                viewModelTypes.add(cls);
                return;
            
            // skip introspection for these
            case MANAGED_BEAN_NOT_CONTRIBUTING:
            case COLLECTION:
            case VALUE:
            case UNKNOWN:
                return;
            }    
        });
        
    }
    
}