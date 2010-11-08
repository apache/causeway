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


package org.apache.isis.metamodel.facets.actions;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;


public class IteratorFilteringFacetFactoryTest extends AbstractFacetFactoryTest {

    private IteratorFilteringFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new IteratorFilteringFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @Override
    public void testFeatureTypes() {
        final ObjectFeatureType[] featureTypes = facetFactory.getFeatureTypes();
        assertTrue(contains(featureTypes, ObjectFeatureType.OBJECT));
        assertFalse(contains(featureTypes, ObjectFeatureType.PROPERTY));
        assertFalse(contains(featureTypes, ObjectFeatureType.COLLECTION));
        assertFalse(contains(featureTypes, ObjectFeatureType.ACTION));
        assertFalse(contains(featureTypes, ObjectFeatureType.ACTION_PARAMETER));
    }

    public void testRequestsRemoverToRemoveIteratorMethods() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            public void someAction() {}
        }
        facetFactory.process(Customer.class, methodRemover, facetHolder);

        assertEquals(1, methodRemover.getRemoveMethodArgsCalls().size());
    }

    public void testNoIteratorMethodFiltered() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            public void someAction() {}
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        assertFalse(facetFactory.recognizes(actionMethod));
    }

    /**
     * Not tested; this facet factory is needed, I think, but only filters out stuff when generics are in use.
     */
    public void xxxtestIterableIteratorMethodFiltered() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer implements Iterable {
            public void someAction() {}

            public Iterator iterator() {
                return null;
            }
        }
        final Method iteratorMethod = findMethod(Customer.class, "iterator");

        assertTrue(facetFactory.recognizes(iteratorMethod));
    }

}

