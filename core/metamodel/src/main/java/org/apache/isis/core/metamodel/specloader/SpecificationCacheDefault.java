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

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.id.HasLogicalType;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections.snapshot._VersionedList;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.val;


class SpecificationCacheDefault<T extends ObjectSpecification> implements SpecificationCache<T> {

    private final Map<Class<?>, T> specByClass = _Maps.newHashMap();

    // optimization: specialized list to keep track of any additions to the cache fast
    private final _VersionedList<T> vList = new _VersionedList<>();

    @Override
    public Optional<T> lookup(Class<?> cls) {
        synchronized(this) {
            return Optional.ofNullable(specByClass.get(cls));
        }
    }

    @Override
    public T computeIfAbsent(
            Class<?> cls,
            Function<Class<?>, T> mappingFunction) {
        synchronized(this) {
            T spec = specByClass.get(cls);
            if(spec==null) {
                spec = mappingFunction.apply(cls);
                internalPut(spec);

//debug
//                if(cls.getSimpleName().equals("MarkupStream")) {
//                    System.out.println("!!! MarkupStream");
//                }

            }
            return spec;
        }
    }

    @Override
    public void clear() {
        synchronized(this) {
            specByClass.clear();
            vList.clear();
        }
    }

    @Override
    public Can<T> snapshotSpecs() {
        synchronized(this) {
            return Can.ofCollection(specByClass.values());
        }
    }

    @Override
    public T remove(@NonNull Class<?> cls) {
        synchronized(this) {
            final T removed = specByClass.remove(cls);
            if(removed!=null) {
                vList.clear(); // invalidate
                vList.addAll(specByClass.values());
            }
            return removed;
        }
    }

    @Override
    public void forEach(Consumer<T> onSpec, boolean shouldRunConcurrent) {
        if(shouldRunConcurrent) {
            vList.forEachParallel(onSpec);
        } else {
            vList
                .stream().sorted(Comparator.comparing(HasLogicalType::getLogicalTypeName))
                .forEach(onSpec);
        }
    }

    // -- HELPER

    private void internalPut(@Nullable T spec) {
        if(spec==null) {
            return;
        }
        val cls = spec.getCorrespondingClass();
        val existing = specByClass.put(cls, spec);
        if(existing==null) {
            vList.add(spec); // add to vList only if we don't have it already
        }
    }


}
