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

package org.apache.isis.core.metamodel.facets.object.callback;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistCallbackViaSaveMethodFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacetViaMethod;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingCallbackFacetViaMethod;

import lombok.val;

public class PersistAndSaveCallbackFacetFactoryTest extends AbstractFacetFactoryTest {

    private PersistCallbackViaSaveMethodFacetFactory saveFacetFactory;
    private PersistCallbackFacetFactory persistFacetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        saveFacetFactory = new PersistCallbackViaSaveMethodFacetFactory(metaModelContext);
        persistFacetFactory = new PersistCallbackFacetFactory(metaModelContext);
    }

    @Override
    protected void tearDown() throws Exception {
        saveFacetFactory = null;
        persistFacetFactory = null;
        super.tearDown();
    }

    public void testSavingAndPersistingLifecycleMethodPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            public void saving() {
            };

            @SuppressWarnings("unused")
            public void persisting() {
            };
        }
        final Method saveMethod = findMethod(Customer.class, "saving");
        final Method persistMethod = findMethod(Customer.class, "persisting");

        saveFacetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));
        persistFacetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PersistingCallbackFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PersistingCallbackFacetViaMethod);
        final PersistingCallbackFacetViaMethod persistingCallbackFacetViaMethod = (PersistingCallbackFacetViaMethod) facet;
        val methods = persistingCallbackFacetViaMethod.getMethods();
        assertTrue(methods.contains(saveMethod));
        assertTrue(methods.contains(persistMethod));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(saveMethod));
    }

    public void testSavedAndPersistedLifecycleMethodPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            public void saved() {
            };

            @SuppressWarnings("unused")
            public void persisted() {
            };
        }
        final Method saveMethod = findMethod(Customer.class, "saved");
        final Method persistMethod = findMethod(Customer.class, "persisted");

        saveFacetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));
        persistFacetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PersistedCallbackFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PersistedCallbackFacetViaMethod);
        final PersistedCallbackFacetViaMethod persistedCallbackFacetViaMethod = (PersistedCallbackFacetViaMethod) facet;
        val methods = persistedCallbackFacetViaMethod.getMethods();
        assertTrue(methods.contains(saveMethod));
        assertTrue(methods.contains(persistMethod));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(saveMethod));
    }

}
