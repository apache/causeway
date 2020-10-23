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

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;

class SpecIdToClassResolverDefault implements SpecIdToClassResolver {

    private final Map<ObjectSpecId, Class<?>> classBySpecId = _Maps.newConcurrentHashMap();

    @Override
    public void clear() {
        classBySpecId.clear();
    }

    @Override
    public Optional<Class<?>> lookup(final @NonNull ObjectSpecId specId) {
        return Optional.ofNullable(classBySpecId.get(specId));
    }
    
    @Override
    public void register(final @NonNull ObjectSpecification spec) {
        if(hasUsableSpecId(spec)) {
            classBySpecId.put(spec.getSpecId(), spec.getCorrespondingClass());
        }
    }
    
    // -- HELPER
    
    private boolean hasUsableSpecId(ObjectSpecification spec) {
        // umm.  It turns out that anonymous inner classes (eg org.estatio.dom.WithTitleGetter$ToString$1)
        // don't have an ObjectSpecId; hence the guard.
        return spec.containsNonFallbackFacet(ObjectSpecIdFacet.class);
    }

}
