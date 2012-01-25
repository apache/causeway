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

package org.apache.isis.core.progmodel.facets.hide;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.members.hide.HiddenFacetAbstract;
import org.apache.isis.core.progmodel.facets.members.hide.annotation.HiddenAnnotationForMemberFacetFactory;

public class HiddenAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private HiddenAnnotationForMemberFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new HiddenAnnotationForMemberFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testHiddenAnnotationPickedUpOnProperty() {
        class Customer {
            @SuppressWarnings("unused")
            @Hidden
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HiddenFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HiddenFacetAbstract);

        assertNoMethodsRemoved();
    }

    public void testHiddenAnnotationPickedUpOnCollection() {
        class Customer {
            @SuppressWarnings("unused")
            @Hidden
            public Collection<?> getOrders() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HiddenFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HiddenFacetAbstract);

        assertNoMethodsRemoved();
    }

    public void testHiddenAnnotationPickedUpOnAction() {
        class Customer {
            @SuppressWarnings("unused")
            @Hidden
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HiddenFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HiddenFacetAbstract);

        assertNoMethodsRemoved();
    }

    public void testHiddenWhenAlwaysAnnotationPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            @Hidden(When.ALWAYS)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HiddenFacet.class);
        final HiddenFacetAbstract hiddenFacetAbstract = (HiddenFacetAbstract) facet;

        assertEquals(org.apache.isis.core.metamodel.facets.When.ALWAYS, hiddenFacetAbstract.value());
    }

    public void testHiddenWhenNeverAnnotationPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            @Hidden(When.NEVER)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HiddenFacet.class);
        final HiddenFacetAbstract hiddenFacetAbstract = (HiddenFacetAbstract) facet;

        assertEquals(org.apache.isis.core.metamodel.facets.When.NEVER, hiddenFacetAbstract.value());
    }

    public void testHiddenWhenOncePersistedAnnotationPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            @Hidden(When.ONCE_PERSISTED)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HiddenFacet.class);
        final HiddenFacetAbstract hiddenFacetAbstract = (HiddenFacetAbstract) facet;

        assertEquals(org.apache.isis.core.metamodel.facets.When.ONCE_PERSISTED, hiddenFacetAbstract.value());
    }

    public void testDisabledWhenUntilPersistedAnnotationPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            @Hidden(When.UNTIL_PERSISTED)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HiddenFacet.class);
        final HiddenFacetAbstract hiddenFacetAbstract = (HiddenFacetAbstract) facet;

        assertEquals(org.apache.isis.core.metamodel.facets.When.UNTIL_PERSISTED, hiddenFacetAbstract.value());
    }

}
