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

package org.apache.isis.core.metamodel.facets.actions.notcontributed.annotation;

import java.lang.reflect.Method;
import org.apache.isis.applib.annotation.NotContributed;
import org.apache.isis.applib.annotation.NotContributed.As;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.NotContributedFacet;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.NotContributedFacetAbstract;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NotContributedFacetAnnotationFactoryTest extends AbstractFacetFactoryTest {

    private NotContributedFacetAnnotationFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new NotContributedFacetAnnotationFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testAnnotationPickedUp() {
        class CustomerRepository {
            @NotContributed
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(CustomerRepository.class, "someAction");

        facetFactory.process(new ProcessMethodContext(CustomerRepository.class, null, null, actionMethod, methodRemover, facetedMethod));

        final NotContributedFacet facet = facetedMethod.getFacet(NotContributedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NotContributedFacetAbstract);
        assertThat(facet.value(), is(As.EITHER));

        assertNoMethodsRemoved();
    }

    public void testAnnotationPickedUpWithAsAssociation() {
        class CustomerRepository {
            @NotContributed(As.ASSOCIATION)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(CustomerRepository.class, "someAction");
        
        facetFactory.process(new ProcessMethodContext(CustomerRepository.class, null, null, actionMethod, methodRemover, facetedMethod));
        
        final NotContributedFacet facet = facetedMethod.getFacet(NotContributedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NotContributedFacetAbstract);
        assertThat(facet.value(), is(As.ASSOCIATION));
        
        assertNoMethodsRemoved();
    }
    
    public void testAnnotationPickedUpWithAsAction() {
        class CustomerRepository {
            @NotContributed(As.ACTION)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(CustomerRepository.class, "someAction");
        
        facetFactory.process(new ProcessMethodContext(CustomerRepository.class, null, null, actionMethod, methodRemover, facetedMethod));
        
        final NotContributedFacet facet = facetedMethod.getFacet(NotContributedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NotContributedFacetAbstract);
        assertThat(facet.value(), is(As.ACTION));
        
        assertNoMethodsRemoved();
    }
    
}
