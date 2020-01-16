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

package org.apache.isis.core.metamodel.specloader.specimpl.standalonelist;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacetAbstract;
import org.apache.isis.core.metamodel.spec.FreeStandingList;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

public class CollectionFacetOnContainer extends CollectionFacetAbstract {

    public CollectionFacetOnContainer(final FacetHolder holder) {
        super(holder);
    }

    @Override
    public Stream<ManagedObject> stream(ManagedObject wrappedObjectList) {
        val list = unwrap(wrappedObjectList);
        return list.stream();
    }

    /**
     * Expected to be called with a {@link ManagedObject} wrapping a
     * {@link FreeStandingList}.
     */
    @Override
    public int size(final ManagedObject wrappedInstanceCollectionVector) {
        return unwrap(wrappedInstanceCollectionVector).size();
    }

    @Override
    public <T extends ManagedObject> Object populatePojo(
            Supplier<Object> emptyCollectionPojoFactory,
            ObjectSpecification collectionSpec,
            Stream<T> elements, 
            int elementCount) {

        throw _Exceptions.unexpectedCodeReach();
    }

    // -- HELPER

    private FreeStandingList unwrap(final ManagedObject wrappedObjectList) {
        return (FreeStandingList) wrappedObjectList.getPojo();
    }

}
