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


package org.apache.isis.core.progmodel.facets.object.callback;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.object.callbacks.PersistCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.PersistedCallbackFacetViaMethod;
import org.apache.isis.core.progmodel.facets.object.callbacks.PersistingCallbackFacetViaMethod;


public class PersistCallbackFacetFactoryTest extends AbstractFacetFactoryTest {

    private PersistCallbackFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new PersistCallbackFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @Override
    public void testFeatureTypes() {
        final List<FeatureType> featureTypes = facetFactory.getFeatureTypes();
        assertTrue(contains(featureTypes, FeatureType.OBJECT));
        assertFalse(contains(featureTypes, FeatureType.PROPERTY));
        assertFalse(contains(featureTypes, FeatureType.COLLECTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION_PARAMETER));
    }


    public void testPersistingLifecycleMethodPickedUpOn() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
			public void persisting() {};
        }
        final Method method = findMethod(Customer.class, "persisting");

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(PersistingCallbackFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PersistingCallbackFacetViaMethod);
        final PersistingCallbackFacetViaMethod persistingCallbackFacetViaMethod = (PersistingCallbackFacetViaMethod) facet;
        assertEquals(method, persistingCallbackFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(method));
    }

    public void testPersistedLifecycleMethodPickedUpOn() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
			public void persisted() {};
        }
        final Method method = findMethod(Customer.class, "persisted");

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(PersistedCallbackFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PersistedCallbackFacetViaMethod);
        final PersistedCallbackFacetViaMethod persistedCallbackFacetViaMethod = (PersistedCallbackFacetViaMethod) facet;
        assertEquals(method, persistedCallbackFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(method));
    }


}

