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

package org.apache.isis.core.metamodel.facets.actions.notinservicemenu.method;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.NotInServiceMenuFacet;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.NotInServiceMenuFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.method.NotInServiceMenuFacetViaMethodFactory;

public class NotInServiceMenuMethodFacetFactoryTest extends AbstractFacetFactoryTest {

    private NotInServiceMenuFacetViaMethodFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new NotInServiceMenuFacetViaMethodFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testSupportingMethodForActionPickedUp() {
        // given
        class CustomerRepository {
            @SuppressWarnings("unused")
            public void someAction() {
            }

            @SuppressWarnings("unused")
            public boolean notInServiceMenuSomeAction() {
                return true;
            }
        }
        final Method actionMethod = findMethod(CustomerRepository.class, "someAction");

        // when
        facetFactory.process(new ProcessMethodContext(CustomerRepository.class, null, null, actionMethod, methodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(NotInServiceMenuFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NotInServiceMenuFacetAbstract);

        // assertNoMethodsRemoved();
        assertEquals(1, methodRemover.getRemovedMethodMethodCalls().size());
    }

}
