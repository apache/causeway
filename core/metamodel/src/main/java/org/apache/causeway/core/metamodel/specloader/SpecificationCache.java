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
package org.apache.causeway.core.metamodel.specloader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections.snapshot._VersionedList;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;

record SpecificationCache(
        Map<Class<?>, ObjectSpecification> specByClass,
        // optimization: specialized list to keep track of any additions to the cache fast
        _VersionedList<ObjectSpecification> vList) {

    SpecificationCache() {
        this(new HashMap<>(), new _VersionedList<>());
    }

    Optional<ObjectSpecification> lookup(final Class<?> cls) {
        synchronized(this) {
            return Optional.ofNullable(specByClass.get(cls));
        }
    }

    ObjectSpecification computeIfAbsent(
            final Class<?> cls,
            final Function<Class<?>, ObjectSpecification> mappingFunction) {
        synchronized(this) {
            var spec = specByClass.get(cls);
            if(spec==null) {
                spec = mappingFunction.apply(cls);
                internalPut(spec);
            }
            return spec;
        }
    }

    void clear() {
        synchronized(this) {
            specByClass.clear();
            vList.clear();
        }
    }

    /** @returns thread-safe defensive copy */
    Can<ObjectSpecification> snapshotSpecs() {
        synchronized(this) {
            return Can.ofCollection(specByClass.values());
        }
    }

    ObjectSpecification remove(@NonNull final Class<?> cls) {
        synchronized(this) {
            var removed = specByClass.remove(cls);
            if(removed!=null) {
                vList.clear(); // invalidate
                vList.addAll(specByClass.values());
            }
            return removed;
        }
    }

    void forEachConcurrent(final Consumer<ObjectSpecification> onSpec) {
        vList.forEachConcurrent(onSpec);
    }

    void forEach(final Consumer<ObjectSpecification> onSpec) {
        vList.forEach(onSpec);
    }

    // -- HELPER

    private void internalPut(@Nullable final ObjectSpecification spec) {
        if(spec==null) return;

        var cls = spec.getCorrespondingClass();
        var existing = specByClass.put(cls, spec);
        if(existing==null) {
            vList.add(spec); // add to vList only if we don't have it already
        }
    }

}
