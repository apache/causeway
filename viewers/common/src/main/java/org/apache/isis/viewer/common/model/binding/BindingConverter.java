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
package org.apache.isis.viewer.common.model.binding;

import java.util.Optional;

import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;

public interface BindingConverter<T> {
    
    ObjectSpecification getValueSpecification();
    
    default T unwrap(ManagedObject object) {
        return _Casts.uncheckedCast(ManagedObjects.UnwrapUtil.single(object));
    }
    
    default ManagedObject wrap(T pojo) {
        return ManagedObject.of(getValueSpecification(), pojo);
    }
    
    default Optional<? extends Facet> lookupFacet(@NonNull final Class<? extends Facet> facetType) {
        return Optional.ofNullable(getValueSpecification().getFacet(facetType));
    }
    
    default Optional<? extends Facet> lookupFacetOneOf(
            @SuppressWarnings("unchecked") final Class<? extends Facet> ... facetTypes) {
        
        return _NullSafe.stream(facetTypes)
        .map(getValueSpecification()::getFacet)
        .filter(_NullSafe::isPresent)
        .findFirst();
    }
    
}


    

