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

package org.apache.isis.core.metamodel.facets.objcoll.paged;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.applib.annotation.Paged;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.collections.paged.PagedFacetForPagedAnnotationOnCollection;
import org.apache.isis.core.metamodel.facets.collections.paged.PagedFacetOnCollectionFactory;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;

public class PagedAnnotationOnCollectionFacetFactoryTest extends AbstractFacetFactoryTest {

    private PagedFacetOnCollectionFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new PagedFacetOnCollectionFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @Paged(20)
    static class Customer {
        @Paged(10)
        public List<?> getOrders() {
            return null;
        }
    }

    static class CustomerWithoutPagedAnnotation {
        public List<?> getOrders() {
            return null;
        }
    }


    public void testAnnotationPickedUpOnCollection() {
        facetedMethod = FacetedMethod.createForCollection(Customer.class, "orders");
        final Method method = facetedMethod.getMethod();
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod);
        facetFactory.process(processMethodContext);

        final Facet facet = facetedMethod.getFacet(PagedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PagedFacetForPagedAnnotationOnCollection);
        PagedFacet pagedFacet = (PagedFacet) facet;
        assertThat(pagedFacet.value(), is(10));
    }

    public void testNoAnnotationOnCollection() {
        facetedMethod = FacetedMethod.createForCollection(CustomerWithoutPagedAnnotation.class, "orders");
        final Method method = facetedMethod.getMethod();
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod);
        facetFactory.process(processMethodContext);

        final Facet facet = facetedMethod.getFacet(PagedFacet.class);
        assertNull(facet);
    }

}
