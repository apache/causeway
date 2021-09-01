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

package org.apache.isis.core.metamodel.facets.object.hidden;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.hidden.method.HiddenObjectFacetViaMethod;
import org.apache.isis.core.metamodel.facets.object.hidden.method.HiddenObjectFacetViaMethodFactory;

public class ObjectHiddenMethodFacetFactoryTest extends AbstractFacetFactoryTest {

    private HiddenObjectFacetViaMethodFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new HiddenObjectFacetViaMethodFactory(metaModelContext);
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testDisabledMethodPickedUpAndMethodRemovedBooleanType() {
        class Customer {
            @SuppressWarnings("unused")
            public boolean hidden() {
                return true;
            }
        }
        final Method hiddenMethod = findMethod(Customer.class, "hidden");

        final ProcessClassContext processClassContext = ProcessClassContext
                .forTesting(Customer.class, methodRemover, facetHolder);
        facetFactory.process(processClassContext);

        final Facet facet = facetHolder.getFacet(HiddenObjectFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HiddenObjectFacetViaMethod);

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(hiddenMethod));
    }

}
