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
import org.apache.isis.applib.annotation.When;
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

        facetFactory.processDisabled(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisabledFacetAbstract);

        assertNoMethodsRemoved();
    }

    public void testDisabledWhenAlwaysAnnotationPickedUpOn() {
        class Customer {
            @Disabled(when = When.ALWAYS)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processDisabled(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;

        final DisabledFacet disabledFacet = (DisabledFacet) facet;
        assertThat(disabledFacet.disabledReason(null), is("Always disabled"));

        assertEquals(When.ALWAYS, disabledFacetAbstract.when());
    }

    public void testDisabledWhenNeverAnnotationPickedUpOn() {
        class Customer {
            @Disabled(when = When.NEVER)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processDisabled(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;

        assertEquals(When.NEVER, disabledFacetAbstract.when());
    }

    public void testDisabledWhenOncePersistedAnnotationPickedUpOn() {
        class Customer {
            @Disabled(when = When.ONCE_PERSISTED)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processDisabled(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;

        assertEquals(When.ONCE_PERSISTED, disabledFacetAbstract.when());
    }

    public void testDisabledWhenUntilPersistedAnnotationPickedUpOn() {
        class Customer {
            @Disabled(when = When.UNTIL_PERSISTED)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processDisabled(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;

        assertEquals(When.UNTIL_PERSISTED, disabledFacetAbstract.when());
        assertEquals(Where.ANYWHERE, disabledFacetAbstract.where());
    }


    public void testDisabledWhereCollectionTableAnnotationPickedUpOn() {
        class Customer {
            @Disabled(where = Where.PARENTED_TABLES)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processDisabled(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;

        assertEquals(When.ALWAYS, disabledFacetAbstract.when());
        assertEquals(Where.PARENTED_TABLES, disabledFacetAbstract.where());
    }


    public void testDisabledWhenAndWhereAnnotationPickedUpOn() {
        class Customer {
            @Disabled(where = Where.PARENTED_TABLES, when=When.UNTIL_PERSISTED)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processDisabled(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;

        assertEquals(When.UNTIL_PERSISTED, disabledFacetAbstract.when());
        assertEquals(Where.PARENTED_TABLES, disabledFacetAbstract.where());
    }


}
