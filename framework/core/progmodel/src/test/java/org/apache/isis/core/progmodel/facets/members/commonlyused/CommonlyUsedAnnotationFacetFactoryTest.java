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

package org.apache.isis.core.progmodel.facets.members.commonlyused;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.isis.applib.annotation.CommonlyUsed;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.commonlyused.CommonlyUsedFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;

public class CommonlyUsedAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private CommonlyUsedAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new CommonlyUsedAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testCommonlyUsedAnnotationPickedUpOnProperty() {

        class Customer {
            @CommonlyUsed
            public int getNumberOfOrders() {
                return 0;
            }
        }

        facetedMethod = FacetedMethod.createForProperty(Customer.class, "numberOfOrders");
        facetFactory.process(new ProcessMethodContext(Customer.class, facetedMethod.getMethod(), methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CommonlyUsedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CommonlyUsedFacetAnnotation);

        assertNoMethodsRemoved();
    }

    public void testCommonlyUsedAnnotationPickedUpOnCollection() {
        class Customer {
            @CommonlyUsed
            public Collection<?> getOrders() {
                return null;
            }
        }
        facetedMethod = FacetedMethod.createForCollection(Customer.class, "orders");
        facetFactory.process(new ProcessMethodContext(Customer.class, facetedMethod.getMethod(), methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CommonlyUsedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CommonlyUsedFacetAnnotation);

        assertNoMethodsRemoved();
    }

    public void testCommonlyUsedAnnotationPickedUpOnAction() {
        class Customer {
            @CommonlyUsed
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");
        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CommonlyUsedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CommonlyUsedFacetAnnotation);

        assertNoMethodsRemoved();
    }
}
