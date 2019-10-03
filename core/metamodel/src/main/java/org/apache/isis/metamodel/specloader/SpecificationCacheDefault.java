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

package org.apache.isis.metamodel.specloader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import lombok.val;


class SpecificationCacheDefault<T extends ObjectSpecification> {

    private final Map<String, T> specByClassName = _Maps.newHashMap();
    private final Map<ObjectSpecId, String> classNameBySpecId = _Maps.newHashMap();
    
    public T get(String className) {
        return specByClassName.get(className);
    }
    
    public T computeIfAbsent(
            String className, 
            Function<? super String, T> mappingFunction) {
        
        T spec = specByClassName.get(className);
        if(spec==null) {
            spec = mappingFunction.apply(className);
            internalPut(spec);
        }
        return spec;
    }

    public void clear() {
        specByClassName.clear();
        classNameBySpecId.clear();
    }

    /** @returns thread-safe defensive copy */
    public Collection<T> snapshotSpecs() {
        synchronized(this) {
            return new ArrayList<T>(specByClassName.values());
        }
    }

    public T getByObjectType(final ObjectSpecId objectSpecID) {
        val className = classNameBySpecId.get(objectSpecID);
        return className != null ? specByClassName.get(className) : null;
    }

    private void internalPut(T spec) {
        if(spec==null) {
            return;
        }
        val className = spec.getCorrespondingClass().getName();
        val specId = spec.getSpecId();
        specByClassName.put(className, spec);
        if (specId == null) {
            return;
        }
        classNameBySpecId.put(specId, className);
    }

    public T remove(String typeName) {
        final T removed = specByClassName.remove(typeName);
        if(hasUsableSpecId(removed)) {
            val specId = removed.getSpecId();
            classNameBySpecId.remove(specId);
        }
        return removed;
    }

    public void recache(T spec) {
        if(hasUsableSpecId(spec)) {
            classNameBySpecId.put(spec.getSpecId(), spec.getCorrespondingClass().getName());
        }
    }
    
    private boolean hasUsableSpecId(T spec) {
        // umm.  It turns out that anonymous inner classes (eg org.estatio.dom.WithTitleGetter$ToString$1)
        // don't have an ObjectSpecId; hence the guard.
        return spec!=null && spec.containsDoOpFacet(ObjectSpecIdFacet.class);
    }

}
