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
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actions.action.hidden.HiddenFacetForHiddenAnnotationOnAction;

public class HiddenAnnotationOnActionFacetFactoryTest extends AbstractFacetFactoryTest {

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

    public void testHiddenAnnotationPickedUpOnAction() {
        class Customer {
            @Hidden
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processHidden(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final HiddenFacetForHiddenAnnotationOnAction facet = facetedMethod.getFacet(HiddenFacetForHiddenAnnotationOnAction.class);
        assertNotNull(facet);

        assertNoMethodsRemoved();
    }

    public void testHiddenWhenAlwaysAnnotationPickedUpOn() {
        class Customer {
            @Hidden(when=When.ALWAYS)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processHidden(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final HiddenFacetForHiddenAnnotationOnAction facet = facetedMethod.getFacet(HiddenFacetForHiddenAnnotationOnAction.class);

        assertEquals(When.ALWAYS, facet.when());
    }

    public void testHiddenWhenNeverAnnotationPickedUpOn() {
        class Customer {
            @Hidden(when=When.NEVER)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processHidden(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final HiddenFacetForHiddenAnnotationOnAction facet = facetedMethod.getFacet(HiddenFacetForHiddenAnnotationOnAction.class);

        assertEquals(When.NEVER, facet.when());
    }

    public void testHiddenWhenOncePersistedAnnotationPickedUpOn() {
        class Customer {
            @Hidden(when=When.ONCE_PERSISTED)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processHidden(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final HiddenFacetForHiddenAnnotationOnAction facet = facetedMethod.getFacet(HiddenFacetForHiddenAnnotationOnAction.class);

        assertEquals(When.ONCE_PERSISTED, facet.when());
    }

    public void testHiddenWhenUntilPersistedAnnotationPickedUpOn() {
        class Customer {
            @Hidden(when=When.UNTIL_PERSISTED)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processHidden(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final HiddenFacetForHiddenAnnotationOnAction facet = facetedMethod.getFacet(HiddenFacetForHiddenAnnotationOnAction.class);

        assertEquals(When.UNTIL_PERSISTED, facet.when());
        assertEquals(Where.ANYWHERE, facet.where());
    }

    public void testHiddenWhereCollectionTableAnnotationPickedUpOn() {
        class Customer {
            @Hidden(where=Where.PARENTED_TABLES)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processHidden(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final HiddenFacetForHiddenAnnotationOnAction facet = facetedMethod.getFacet(HiddenFacetForHiddenAnnotationOnAction.class);

        assertEquals(Where.PARENTED_TABLES, facet.where());
        assertEquals(When.ALWAYS, facet.when());
    }


    public void testHiddenWhenAndWhereTableAnnotationPickedUpOn() {
        class Customer {
            @Hidden(where=Where.PARENTED_TABLES, when=When.UNTIL_PERSISTED)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processHidden(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final HiddenFacetForHiddenAnnotationOnAction facet = facetedMethod.getFacet(HiddenFacetForHiddenAnnotationOnAction.class);

        assertEquals(Where.PARENTED_TABLES, facet.where());
        assertEquals(When.UNTIL_PERSISTED, facet.when());
    }

}
