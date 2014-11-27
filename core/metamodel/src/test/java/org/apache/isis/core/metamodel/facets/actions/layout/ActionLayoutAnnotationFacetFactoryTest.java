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

package org.apache.isis.core.metamodel.facets.actions.layout;

import java.lang.reflect.Method;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;

public class ActionLayoutAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    public void testActionLayoutAnnotationPickedUp() {
        final ActionLayoutFacetFactory facetFactory = new ActionLayoutFacetFactory();

        class Customer {
            @SuppressWarnings("unused")
            @ActionLayout(position = ActionLayout.Position.PANEL)
            public String foo() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "foo");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionLayoutFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionLayoutFacetAnnotation);
        final ActionLayoutFacetAnnotation actionLayoutFacetAnnotation = (ActionLayoutFacetAnnotation) facet;
        assertEquals(ActionLayout.Position.PANEL, actionLayoutFacetAnnotation.position());
    }

    public void testActionLayoutFallbackPickedUp() {
        final ActionLayoutFacetFactory facetFactory = new ActionLayoutFacetFactory();

        class Customer {
            @SuppressWarnings("unused")
            // no @ActionLayout
            public String foo() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "foo");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionLayoutFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionLayoutFacetFallback);
    }

}
