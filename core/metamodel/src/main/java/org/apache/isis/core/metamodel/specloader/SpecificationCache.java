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

import java.util.Optional;
import java.util.function.Function;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections.snapshot._VersionedList;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

interface SpecificationCache<T extends ObjectSpecification> {

    _VersionedList<T> getVList();
    
    Optional<T> lookup(Class<?> cls);

    T computeIfAbsent(Class<?> cls, Function<Class<?>, T> mappingFunction);
    
    T remove(Class<?> cls);

    void clear();

    /** @returns thread-safe defensive copy */
    Can<T> snapshotSpecs();

    Class<?> resolveType(ObjectSpecId objectSpecID);

    T getByObjectType(ObjectSpecId objectSpecID);

    void recache(T spec);

}