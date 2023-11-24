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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.config.IsisModuleCoreConfig;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

@Service
@Named(IsisModuleCoreConfig.NAMESPACE + "..IsisBeanTypeRegistryImpl")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
public class IsisBeanTypeRegistryDefault
implements IsisBeanTypeRegistry {

    /**
     * (immutable) scan result, as used by the SpecificationLoader for introspection
     */
    private final Can<IsisBeanMetaData> introspectableTypes;

    private final Map<Class<?>, IsisBeanMetaData> introspectableTypesByClass = _Maps.newHashMap();

    // -- DISTINCT CATEGORIES OF BEAN SORTS

    @Getter(onMethod_ = {@Override})
    private final Map<Class<?>, IsisBeanMetaData> managedBeansContributing = new HashMap<>();

    @Getter(onMethod_ = {@Override})
    private final Map<Class<?>, IsisBeanMetaData> entityTypes = new HashMap<>();

    @Getter(onMethod_ = {@Override})
    private final Map<Class<?>, IsisBeanMetaData> mixinTypes = new HashMap<>();

    @Getter(onMethod_ = {@Override})
    private final Map<Class<?>, IsisBeanMetaData> viewModelTypes = new HashMap<>();

    @Getter(onMethod_ = {@Override})
    private final Map<Class<?>, IsisBeanMetaData> discoveredValueTypes = new HashMap<>();

    // -- LOOKUPS

    @Override
    public Optional<IsisBeanMetaData> lookupIntrospectableType(final Class<?> type) {
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

        introspectableTypes.forEach(typeMeta->{

            val cls = typeMeta.getCorrespondingClass();

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



}
