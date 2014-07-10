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

package org.apache.isis.core.metamodel.facets.members.resolve;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.render.RenderFacet;
import org.apache.isis.core.metamodel.facets.members.render.annotprop.RenderFacetAnnotation;
import org.apache.isis.core.metamodel.facets.members.render.annotprop.RenderFacetOrResolveFactory;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;

public class RenderOrResolveAnnotationFacetFactoryTest_withRenderAnnotation extends AbstractFacetFactoryTest {

    private RenderFacetOrResolveFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new RenderFacetOrResolveFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testAnnotationWithNoHintPickedUpOnProperty() {

        class Customer {
            @SuppressWarnings("unused")
            @Render
            public int getNumberOfOrders() {
                return 0;
            }
        }

        facetedMethod = FacetedMethod.createForProperty(Customer.class, "numberOfOrders");
        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, facetedMethod.getMethod(), methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(RenderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RenderFacetAnnotation);
        RenderFacet resolveFacet = (RenderFacet) facet;
        assertThat(resolveFacet.value(), is(Render.Type.EAGERLY));

        assertNoMethodsRemoved();
    }

    public void testAnnotationWithEagerlyHintPickedUpOnProperty() {

        class Customer {
            @SuppressWarnings("unused")
            @Render(Type.EAGERLY)
            public int getNumberOfOrders() {
                return 0;
            }
        }

        facetedMethod = FacetedMethod.createForProperty(Customer.class, "numberOfOrders");
        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, facetedMethod.getMethod(), methodRemover, facetedMethod));

        final RenderFacet facet = facetedMethod.getFacet(RenderFacet.class);
        assertThat(facet.value(), is(Render.Type.EAGERLY));
    }

    public void testAnnotationForLazilyPickedUpOnProperty() {

        class Customer {
            @SuppressWarnings("unused")
            @Render(Type.LAZILY)
            public int getNumberOfOrders() {
                return 0;
            }
        }

        facetedMethod = FacetedMethod.createForProperty(Customer.class, "numberOfOrders");
        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, facetedMethod.getMethod(), methodRemover, facetedMethod));

        final RenderFacet facet = facetedMethod.getFacet(RenderFacet.class);
        assertThat(facet.value(), is(Render.Type.LAZILY));
    }

    public void testAnnotationNoHintPickedUpOnCollection() {
        class Customer {
            @SuppressWarnings("unused")
            @Render
            public Collection<?> getOrders() {
                return null;
            }
        }
        facetedMethod = FacetedMethod.createForCollection(Customer.class, "orders");
        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, facetedMethod.getMethod(), methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(RenderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RenderFacetAnnotation);
        RenderFacet resolveFacet = (RenderFacet) facet;
        assertThat(resolveFacet.value(), is(Render.Type.EAGERLY));

        assertNoMethodsRemoved();
    }

    public void testAnnotationEagerlyHintPickedUpOnCollection() {
        class Customer {
            @SuppressWarnings("unused")
            @Render(Type.EAGERLY)
            public Collection<?> getOrders() {
                return null;
            }
        }
        facetedMethod = FacetedMethod.createForCollection(Customer.class, "orders");
        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, facetedMethod.getMethod(), methodRemover, facetedMethod));

        final RenderFacet facet = facetedMethod.getFacet(RenderFacet.class);
        assertThat(facet.value(), is(Render.Type.EAGERLY));
    }

    public void testAnnotationWithLazilyHintPickedUpOnCollection() {
        class Customer {
            @SuppressWarnings("unused")
            @Render(Type.LAZILY)
            public Collection<?> getOrders() {
                return null;
            }
        }
        facetedMethod = FacetedMethod.createForCollection(Customer.class, "orders");
        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, facetedMethod.getMethod(), methodRemover, facetedMethod));

        final RenderFacet facet = facetedMethod.getFacet(RenderFacet.class);
        assertThat(facet.value(), is(Render.Type.LAZILY));
    }


}
