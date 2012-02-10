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

package org.apache.isis.core.progmodel.facets.disable;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.members.disable.DisabledFacet;
import org.apache.isis.core.progmodel.facets.members.disable.DisabledFacetAbstract;
import org.apache.isis.core.progmodel.facets.members.disable.annotation.DisabledAnnotationFacetFactory;

public class DisabledAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private DisabledAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new DisabledAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testDisabledAnnotationPickedUpOnProperty() {
        class Customer {
            @SuppressWarnings("unused")
            @Disabled
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisabledFacetAbstract);

        assertNoMethodsRemoved();
    }

    public void testDisabledAnnotationPickedUpOnCollection() {
        class Customer {
            @SuppressWarnings("unused")
            @Disabled
            public Collection<?> getOrders() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisabledFacetAbstract);

        assertNoMethodsRemoved();
    }

    public void testDisabledAnnotationPickedUpOnAction() {
        class Customer {
            @SuppressWarnings("unused")
            @Disabled
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisabledFacetAbstract);

        assertNoMethodsRemoved();
    }

    public void testDisabledWhenAlwaysAnnotationPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            @Disabled(When.ALWAYS)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;

        assertEquals(org.apache.isis.core.metamodel.facets.When.ALWAYS, disabledFacetAbstract.value());
    }

    public void testDisabledWhenNeverAnnotationPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            @Disabled(When.NEVER)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;

        assertEquals(org.apache.isis.core.metamodel.facets.When.NEVER, disabledFacetAbstract.value());
    }

    public void testDisabledWhenOncePersistedAnnotationPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            @Disabled(When.ONCE_PERSISTED)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;

        assertEquals(org.apache.isis.core.metamodel.facets.When.ONCE_PERSISTED, disabledFacetAbstract.value());
    }

    public void testDisabledWhenUntilPersistedAnnotationPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            @Disabled(When.UNTIL_PERSISTED)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;

        assertEquals(org.apache.isis.core.metamodel.facets.When.UNTIL_PERSISTED, disabledFacetAbstract.value());
    }

}
