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

import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadingCallbackFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.progmodel.facets.object.callbacks.LoadCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.LoadedCallbackFacetViaMethod;
import org.apache.isis.core.progmodel.facets.object.callbacks.LoadingCallbackFacetViaMethod;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;


public class LoadCallbackFacetFactoryTest extends AbstractFacetFactoryTest {

    private LoadCallbackFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new LoadCallbackFacetFactory();
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


    public void testLoadingLifecycleMethodPickedUpOn() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
			public void loading() {};
        }
        final Method method = findMethod(Customer.class, "loading");

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(LoadingCallbackFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof LoadingCallbackFacetViaMethod);
        final LoadingCallbackFacetViaMethod loadingCallbackFacetViaMethod = (LoadingCallbackFacetViaMethod) facet;
        assertEquals(method, loadingCallbackFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(method));
    }

    public void testLoadedLifecycleMethodPickedUpOn() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
			public void loaded() {};
        }
        final Method method = findMethod(Customer.class, "loaded");

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(LoadedCallbackFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof LoadedCallbackFacetViaMethod);
        final LoadedCallbackFacetViaMethod loadedCallbackFacetViaMethod = (LoadedCallbackFacetViaMethod) facet;
        assertEquals(method, loadedCallbackFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(method));
    }


}

