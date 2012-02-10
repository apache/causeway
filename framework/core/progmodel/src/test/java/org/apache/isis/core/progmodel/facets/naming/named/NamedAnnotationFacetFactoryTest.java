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

package org.apache.isis.core.progmodel.facets.naming.named;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.isis.applib.annotation.Named;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.named.NamedFacetAbstract;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.members.named.annotation.NamedAnnotationOnMemberFacetFactory;
import org.apache.isis.core.progmodel.facets.object.named.annotation.NamedAnnotationOnTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.param.named.annotation.NamedAnnotationOnParameterFacetFactory;

public class NamedAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    public void testNamedAnnotationPickedUpOnClass() {

        final NamedAnnotationOnTypeFacetFactory facetFactory = new NamedAnnotationOnTypeFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        @Named("some name")
        class Customer {
        }
        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacetAbstract);
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertEquals("some name", namedFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testNamedAnnotationPickedUpOnProperty() {

        final NamedAnnotationOnMemberFacetFactory facetFactory = new NamedAnnotationOnMemberFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            @Named("some name")
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacetAbstract);
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertEquals("some name", namedFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testNamedAnnotationPickedUpOnCollection() {
        final NamedAnnotationOnMemberFacetFactory facetFactory = new NamedAnnotationOnMemberFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            @Named("some name")
            public Collection<?> getOrders() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacetAbstract);
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertEquals("some name", namedFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testNamedAnnotationPickedUpOnAction() {
        final NamedAnnotationOnMemberFacetFactory facetFactory = new NamedAnnotationOnMemberFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            @Named("some name")
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacetAbstract);
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertEquals("some name", namedFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testNamedAnnotationPickedUpOnActionParameter() {

        final NamedAnnotationOnParameterFacetFactory facetFactory = new NamedAnnotationOnParameterFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@Named("some name") final int x) {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(new ProcessParameterContext(actionMethod, 0, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacetAbstract);
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertEquals("some name", namedFacetAbstract.value());
    }

}
