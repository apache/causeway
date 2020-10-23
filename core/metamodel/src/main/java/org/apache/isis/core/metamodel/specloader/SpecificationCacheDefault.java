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

package org.apache.isis.core.metamodel.specloader;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections.snapshot._VersionedList;
import org.apache.isis.core.metamodel.commons.ClassUtil;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.val;


class SpecificationCacheDefault<T extends ObjectSpecification> implements SpecificationCache<T> {

    private final Map<ObjectSpecId, Class<?>> classBySpecId = _Maps.newHashMap();
    private final Map<Class<?>, T> specByClass = _Maps.newHashMap();
    
    
    // optimization: specialized list to keep track of any additions to the cache fast
    @Getter(onMethod_ = {@Override})//(value = AccessLevel.PACKAGE)
    private final _VersionedList<T> vList = new _VersionedList<>(); 
    
    @Override
    public Optional<T> lookup(Class<?> cls) {
        return Optional.ofNullable(specByClass.get(cls));
    }
    
    @Override
    public T computeIfAbsent(
            Class<?> cls, 
            Function<Class<?>, T> mappingFunction) {
        
        T spec = specByClass.get(cls);
        if(spec==null) {
            spec = mappingFunction.apply(cls);
            internalPut(spec);
        }
        return spec;
    }

    @Override
    public void clear() {
        synchronized(this) {
            specByClass.clear();
            classBySpecId.clear();
            vList.clear();
        }
    }

    /** @returns thread-safe defensive copy */
    @Override
    public Can<T> snapshotSpecs() {
        synchronized(this) {
            return Can.ofCollection(specByClass.values());
        }
    }
    
    @Override
    public Class<?> resolveType(final ObjectSpecId objectSpecID) {
        val classFromCache = classBySpecId.get(objectSpecID);
        return classFromCache != null
                ? classFromCache 
                : ClassUtil.forNameElseNull(objectSpecID.asString());
    }
    
    @Override
    public T getByObjectType(final ObjectSpecId objectSpecID) {
        val className = classBySpecId.get(objectSpecID);
        return className != null ? specByClass.get(className) : null;
    }

    private void internalPut(T spec) {
        if(spec==null) {
            return;
        }
        val cls = spec.getCorrespondingClass();
        val specId = spec.getSpecId();
        val existing = specByClass.put(cls, spec);
        if(existing==null) {
            vList.add(spec); // add to vList only if we don't have it already
        }
        if (specId == null) {
            return;
        }
        classBySpecId.put(specId, cls);
    }

    @Override
    public T remove(Class<?> cls) {
        final T removed = specByClass.remove(cls);
        if(removed!=null) {
            vList.clear(); // invalidate
            vList.addAll(specByClass.values());
        }
        if(hasUsableSpecId(removed)) {
            val specId = removed.getSpecId();
            classBySpecId.remove(specId);
        }
        return removed;
    }

    @Override
    public void recache(T spec) {
        if(hasUsableSpecId(spec)) {
            classBySpecId.put(spec.getSpecId(), spec.getCorrespondingClass());
        }
    }
    
    private boolean hasUsableSpecId(T spec) {
        // umm.  It turns out that anonymous inner classes (eg org.estatio.dom.WithTitleGetter$ToString$1)
        // don't have an ObjectSpecId; hence the guard.
        return spec!=null && spec.containsNonFallbackFacet(ObjectSpecIdFacet.class);
    }

}
