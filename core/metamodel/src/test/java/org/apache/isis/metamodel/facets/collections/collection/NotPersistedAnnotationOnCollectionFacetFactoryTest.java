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

package org.apache.isis.metamodel.facets.collections.collection;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.MementoSerialization;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.metamodel.facets.collections.collection.notpersisted.NotPersistedFacetForCollectionAnnotation;
import org.apache.isis.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;

import lombok.val;

public class NotPersistedAnnotationOnCollectionFacetFactoryTest extends AbstractFacetFactoryTest {

    private CollectionAnnotationFacetFactory facetFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        facetFactory = new CollectionAnnotationFacetFactory();
    }
    
    private static void processNotPersisted(
            CollectionAnnotationFacetFactory facetFactory, ProcessMethodContext processMethodContext) {
        val collectionIfAny = processMethodContext.synthesizeOnMethod(Collection.class);
        facetFactory.processNotPersisted(processMethodContext, collectionIfAny);
    }

    public void testNotPersistedAnnotationPickedUpOnCollection() {

        class Order {
        }
        class Customer {
            @Collection(mementoSerialization = MementoSerialization.EXCLUDED)
            public java.util.Collection<Order> getOrders() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getOrders");

        processNotPersisted(facetFactory, new ProcessMethodContext(Customer.class, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NotPersistedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NotPersistedFacetForCollectionAnnotation);

        assertNoMethodsRemoved();
    }

}
