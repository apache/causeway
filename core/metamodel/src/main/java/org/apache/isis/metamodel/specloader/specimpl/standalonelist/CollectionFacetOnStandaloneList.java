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

package org.apache.isis.metamodel.specloader.specimpl.standalonelist;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.collections.CollectionFacetAbstract;
import org.apache.isis.metamodel.spec.FreeStandingList;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

public class CollectionFacetOnStandaloneList extends CollectionFacetAbstract {

    public CollectionFacetOnStandaloneList(final FacetHolder holder) {
        super(holder);
    }

    @Override
    public <T extends ManagedObject> Stream<T> stream(T wrappedObjectList) {
        final List<ManagedObject> list = collection(wrappedObjectList);
        return list.stream()
                .map(x->uncheckedCast(x));
    }

    /**
     * Expected to be called with a {@link ObjectAdapter} wrapping a
     * {@link FreeStandingList}.
     */
    @Override
    public int size(final ManagedObject wrappedInstanceCollectionVector) {
        return collection(wrappedInstanceCollectionVector).size();
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

    private List<ManagedObject> collection(final ManagedObject wrappedObjectList) {
        return (FreeStandingList) wrappedObjectList.getPojo();
    }

}
