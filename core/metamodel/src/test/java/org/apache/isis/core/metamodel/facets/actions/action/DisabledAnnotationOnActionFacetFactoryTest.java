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

package org.apache.isis.core.metamodel.facets.actions.action;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstract;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DisabledAnnotationOnActionFacetFactoryTest extends AbstractFacetFactoryTest {

    private ActionAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new ActionAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testDisabledAnnotationPickedUpOnAction() {
        class Customer {
            @Disabled
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processDisabled(new ProcessMethodContext(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisabledFacetAbstract);

        assertNoMethodsRemoved();
    }

    public void testDisabledWhenAlwaysAnnotationPickedUpOn() {
        class Customer {
            @Disabled
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processDisabled(new ProcessMethodContext(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;

        final DisabledFacet disabledFacet = (DisabledFacet) facet;
        assertThat(disabledFacet.disabledReason(null), is("Always disabled"));

    }

    public void testDisabledWhenNeverAnnotationPickedUpOn() {
        class Customer {
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processDisabled(new ProcessMethodContext(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;

        assertNull(disabledFacetAbstract);
    }


    public void testDisabledWhereCollectionTableAnnotationPickedUpOn() {
        class Customer {
            @Disabled(where = Where.PARENTED_TABLES)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processDisabled(new ProcessMethodContext(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;

        assertEquals(Where.PARENTED_TABLES, disabledFacetAbstract.where());
    }


    public void testDisabledWhereAnnotationPickedUpOn() {
        class Customer {
            @Disabled(where = Where.PARENTED_TABLES)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processDisabled(new ProcessMethodContext(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;

        assertEquals(Where.PARENTED_TABLES, disabledFacetAbstract.where());
    }


}
