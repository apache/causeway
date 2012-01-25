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

package org.apache.isis.core.progmodel.facets.actions.executed;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Executed;
import org.apache.isis.applib.annotation.Executed.Where;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actions.executed.ExecutedFacet;
import org.apache.isis.core.metamodel.facets.actions.executed.ExecutedFacetAbstract;
import org.apache.isis.core.metamodel.spec.Target;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.actions.executed.annotation.ExecutedAnnotationFacetFactory;

public class ExecutedAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private ExecutedAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new ExecutedAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testExecutedLocallyAnnotationPickedUp() {
        class Customer {
            @SuppressWarnings("unused")
            @Executed(Where.LOCALLY)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ExecutedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExecutedFacetAbstract);
        final ExecutedFacetAbstract executedFacetAbstract = (ExecutedFacetAbstract) facet;
        assertEquals(Target.LOCAL, executedFacetAbstract.getTarget());

        assertNoMethodsRemoved();
    }

    public void testExecutedRemotelyAnnotationPickedUp() {
        class Customer {
            @SuppressWarnings("unused")
            @Executed(Where.REMOTELY)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ExecutedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExecutedFacetAbstract);
        final ExecutedFacetAbstract executedFacetAbstract = (ExecutedFacetAbstract) facet;
        assertEquals(Target.REMOTE, executedFacetAbstract.getTarget());

        assertNoMethodsRemoved();
    }

}
