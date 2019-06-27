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

package org.apache.isis.metamodel.facets.object.callback;

import java.lang.reflect.Method;

import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.metamodel.facets.object.callbacks.PersistCallbackViaSaveMethodFacetFactory;
import org.apache.isis.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.PersistedCallbackFacetViaMethod;
import org.apache.isis.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.PersistingCallbackFacetViaMethod;

public class SaveCallbackFacetFactoryTest extends AbstractFacetFactoryTest {

    private PersistCallbackViaSaveMethodFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new PersistCallbackViaSaveMethodFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testSavingLifecycleMethodPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            public void saving() {
            };
        }
        final Method method = findMethod(Customer.class, "saving");

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PersistingCallbackFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PersistingCallbackFacetViaMethod);
        final PersistingCallbackFacetViaMethod savingCallbackFacetViaMethod = (PersistingCallbackFacetViaMethod) facet;
        assertEquals(method, savingCallbackFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(method));
    }

    public void testSavedLifecycleMethodPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            public void saved() {
            };
        }
        final Method method = findMethod(Customer.class, "saved");

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PersistedCallbackFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PersistedCallbackFacetViaMethod);
        final PersistedCallbackFacetViaMethod savedCallbackFacetViaMethod = (PersistedCallbackFacetViaMethod) facet;
        assertEquals(method, savedCallbackFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(method));
    }

}
