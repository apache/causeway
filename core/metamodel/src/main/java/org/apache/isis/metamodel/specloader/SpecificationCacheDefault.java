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

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import lombok.val;


/**
 * This is populated in two parts.
 *
 * Initially the <tt>specByClassName</tt> map is populated using {@link #put(String, ObjectSpecification)}.
 * This allows {@link #allSpecifications()} to return a list of specs.
 * Later on, {@link #init()} called which populates #classNameBySpecId.
 *
 * Attempting to call {@link #getByObjectType(ObjectSpecId)} before 
 * {@link #init() initialisation} will result in an
 * {@link IllegalStateException}.
 *
 */
class SpecificationCacheDefault<T extends ObjectSpecification> {

    private final Map<String, T> specByClassName = _Maps.newHashMap();
    private Map<ObjectSpecId, String> classNameBySpecId;

    public T get(final String className) {
        return specByClassName.get(className);
    }
    
    public T computeIfAbsent(
            final String className, 
            final Function<? super String, T> mappingFunction) {
        return specByClassName.computeIfAbsent(className, mappingFunction);
    }

    public void put(final String className, final T spec) {
        specByClassName.put(className, spec);
        recache(spec);
    }


    public void clear() {
        specByClassName.clear();
    }

    /** @returns thread-safe defensive copy */
    public Collection<T> snapshotSpecs() {
        synchronized(this) {
            return new ArrayList<T>(specByClassName.values());
        }
    }

    public T getByObjectType(final ObjectSpecId objectSpecID) {
        if (!isInitialized()) {
            throw new IllegalStateException("SpecificationCache by object type has not yet been initialized");
        }
        final String className = classNameBySpecId.get(objectSpecID);
        return className != null ? specByClassName.get(className) : null;
    }

    synchronized void init() {
        val specById = _Maps.<ObjectSpecId, T>newHashMap();
        val cachedSpecifications = _Lists.<T>newArrayList();
        for(;;) {
            val newSpecifications = snapshotSpecs();
            newSpecifications.removeAll(cachedSpecifications);
            if(newSpecifications.isEmpty()) {
                break;
            }
            for (val objSpec : newSpecifications) {
                val objectSpecId = objSpec.getSpecId();
                if (objectSpecId == null) {
                    continue;
                }
                specById.put(objectSpecId, objSpec);
            }
            cachedSpecifications.addAll(newSpecifications);
        }

        internalInit(specById);
    }

    void internalInit(final Map<ObjectSpecId, T> specById) {
        val classNameBySpecId = _Maps.<ObjectSpecId, String>newHashMap();
        val specByClassName = _Maps.<String, T>newHashMap();

        for (val objectSpecId : specById.keySet()) {
            val objectSpec = specById.get(objectSpecId);
            val className = objectSpec.getCorrespondingClass().getName();
            classNameBySpecId.put(objectSpecId, className);
            specByClassName.put(className, objectSpec);
        }
        this.classNameBySpecId = classNameBySpecId;
        this.specByClassName.clear();
        this.specByClassName.putAll(specByClassName);
    }

    public T remove(String typeName) {
        T removed = specByClassName.remove(typeName);
        if(removed != null) {
            if(removed.containsDoOpFacet(ObjectSpecIdFacet.class)) {
                // umm.  It turns out that anonymous inner classes (eg org.estatio.dom.WithTitleGetter$ToString$1)
                // don't have an ObjectSpecId; hence the guard.
                ObjectSpecId specId = removed.getSpecId();
                classNameBySpecId.remove(specId);
            }
        }
        return removed;
    }

    /**
     * @param spec
     */
    public void recache(T spec) {
        if(!isInitialized()) {
            // JRebel plugin might call this before we are actually up and running;
            // just ignore.
            return;
        }
        if(!spec.containsDoOpFacet(ObjectSpecIdFacet.class)) {
            return;
        }
        classNameBySpecId.put(spec.getSpecId(), spec.getCorrespondingClass().getName());
    }

    boolean isInitialized() {
        return classNameBySpecId != null;
    }

}
