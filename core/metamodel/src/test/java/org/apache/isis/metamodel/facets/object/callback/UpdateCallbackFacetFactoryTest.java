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
import org.apache.isis.metamodel.facets.object.callbacks.UpdateCallbackFacetFactory;
import org.apache.isis.metamodel.facets.object.callbacks.UpdatedCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.UpdatedCallbackFacetViaMethod;
import org.apache.isis.metamodel.facets.object.callbacks.UpdatingCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.UpdatingCallbackFacetViaMethod;

public class UpdateCallbackFacetFactoryTest extends AbstractFacetFactoryTest {

    private UpdateCallbackFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new UpdateCallbackFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testUpdatingLifecycleMethodPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            public void updating() {
            };
        }
        final Method method = findMethod(Customer.class, "updating");

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(UpdatingCallbackFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof UpdatingCallbackFacetViaMethod);
        final UpdatingCallbackFacetViaMethod updatingCallbackFacetViaMethod = (UpdatingCallbackFacetViaMethod) facet;
        assertEquals(method, updatingCallbackFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(method));
    }

    public void testUpdatedLifecycleMethodPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            public void updated() {
            };
        }
        final Method method = findMethod(Customer.class, "updated");

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(UpdatedCallbackFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof UpdatedCallbackFacetViaMethod);
        final UpdatedCallbackFacetViaMethod updatedCallbackFacetViaMethod = (UpdatedCallbackFacetViaMethod) facet;
        assertEquals(method, updatedCallbackFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(method));
    }

}
