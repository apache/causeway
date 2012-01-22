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

package org.apache.isis.core.progmodel.facets.naming.describedas;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.describedas.DescribedAsFacetAbstract;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.members.describedas.annotation.DescribedAsAnnotationOnMemberFacetFactory;
import org.apache.isis.core.progmodel.facets.object.describedas.annotation.DescribedAsAnnotationOnTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.param.describedas.annotation.DescribedAsAnnotationOnParameterFacetFactory;

public class DescribedAsAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    public void testDescribedAsAnnotationPickedUpOnClass() {
        final DescribedAsAnnotationOnTypeFacetFactory facetFactory = new DescribedAsAnnotationOnTypeFacetFactory();

        @DescribedAs("some description")
        class Customer {
        }

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DescribedAsFacetAbstract);
        final DescribedAsFacetAbstract describedAsFacetAbstract = (DescribedAsFacetAbstract) facet;
        assertEquals("some description", describedAsFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testDescribedAsAnnotationPickedUpOnProperty() {
        final DescribedAsAnnotationOnMemberFacetFactory facetFactory = new DescribedAsAnnotationOnMemberFacetFactory();

        class Customer {
            @SuppressWarnings("unused")
            @DescribedAs("some description")
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DescribedAsFacetAbstract);
        final DescribedAsFacetAbstract describedAsFacetAbstract = (DescribedAsFacetAbstract) facet;
        assertEquals("some description", describedAsFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testDescribedAsAnnotationPickedUpOnCollection() {
        final DescribedAsAnnotationOnMemberFacetFactory facetFactory = new DescribedAsAnnotationOnMemberFacetFactory();

        class Customer {
            @SuppressWarnings("unused")
            @DescribedAs("some description")
            public Collection<?> getOrders() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DescribedAsFacetAbstract);
        final DescribedAsFacetAbstract describedAsFacetAbstract = (DescribedAsFacetAbstract) facet;
        assertEquals("some description", describedAsFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testDescribedAsAnnotationPickedUpOnAction() {
        final DescribedAsAnnotationOnMemberFacetFactory facetFactory = new DescribedAsAnnotationOnMemberFacetFactory();

        class Customer {
            @SuppressWarnings("unused")
            @DescribedAs("some description")
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DescribedAsFacetAbstract);
        final DescribedAsFacetAbstract describedAsFacetAbstract = (DescribedAsFacetAbstract) facet;
        assertEquals("some description", describedAsFacetAbstract.value());

        assertNoMethodsRemoved();
    }

    public void testDescribedAsAnnotationPickedUpOnActionParameter() {
        final DescribedAsAnnotationOnParameterFacetFactory facetFactory = new DescribedAsAnnotationOnParameterFacetFactory();

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@DescribedAs("some description") final int x) {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(new ProcessParameterContext(actionMethod, 0, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DescribedAsFacetAbstract);
        final DescribedAsFacetAbstract describedAsFacetAbstract = (DescribedAsFacetAbstract) facet;
        assertEquals("some description", describedAsFacetAbstract.value());
    }

}
