/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.causeway.core.metamodel.facets.actions.layout;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.AbstractFacetFactoryJupiterTestCase;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.causeway.core.metamodel.facets.actions.position.ActionPositionFacetFallback;
import org.apache.causeway.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;

class ActionLayoutXmlLayoutAnnotationFacetFactoryTest
extends AbstractFacetFactoryJupiterTestCase {

    ActionLayoutFacetFactory facetFactory;

    @BeforeEach
    void setUp() throws Exception {
        facetFactory = new ActionLayoutFacetFactory(metaModelContext);
    }

    @Test
    void testActionLayoutAnnotationPickedUp() {

        class Customer {
            @ActionLayout(position = ActionLayout.Position.PANEL)
            public String foz() {
                return null;
            }
        }

        final Method method = findMethod(Customer.class, "foz");

        facetFactory.process(ProcessMethodContext.forTesting(Customer.class, null, method, mockMethodRemover,
                facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionPositionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionPositionFacetForActionLayoutAnnotation);
        final ActionPositionFacetForActionLayoutAnnotation actionLayoutFacetAnnotation = (ActionPositionFacetForActionLayoutAnnotation) facet;
        assertEquals(ActionLayout.Position.PANEL, actionLayoutFacetAnnotation.position());
    }

    @Test
    void testActionLayoutFallbackPickedUp() {

        class Customer {
            @SuppressWarnings("unused")
            // no @ActionLayout
            public String foo() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "foo");

        facetFactory.process(ProcessMethodContext.forTesting(Customer.class, null, method, mockMethodRemover,
                facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionPositionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionPositionFacetFallback);
    }

    static class CssClassFa extends ActionLayoutXmlLayoutAnnotationFacetFactoryTest {

        @Test
        void testDefaultPosition() {

            class Customer {
                @ActionLayout(cssClassFa = "font-awesome")
                public String foz() {
                    return null;
                }
            }
            final Method method = findMethod(Customer.class, "foz");

            facetFactory.process(ProcessMethodContext
                    .forTesting(Customer.class, null, method, mockMethodRemover, facetedMethod));

            Facet facet = facetedMethod.getFacet(CssClassFaFacet.class);
            assertThat(facet, is(notNullValue()));
            assertThat(facet, is(instanceOf(CssClassFaFacetForActionLayoutAnnotation.class)));
            CssClassFaFacetForActionLayoutAnnotation classFaFacetForActionLayoutAnnotation = (CssClassFaFacetForActionLayoutAnnotation) facet;
            assertThat(classFaFacetForActionLayoutAnnotation.asSpaceSeparated(), is(equalTo("fa fa-fw fa-font-awesome")));
            assertThat(classFaFacetForActionLayoutAnnotation.getPosition(), is(CssClassFaPosition.LEFT));
        }

        @Test
        void testRightPosition() {

            class Customer {
                @ActionLayout(cssClassFa = "font-awesome", cssClassFaPosition = CssClassFaPosition.RIGHT)
                public String foz() {
                    return null;
                }
            }
            final Method method = findMethod(Customer.class, "foz");

            facetFactory.process(ProcessMethodContext
                    .forTesting(Customer.class, null, method, mockMethodRemover, facetedMethod));

            Facet facet = facetedMethod.getFacet(CssClassFaFacet.class);
            assertThat(facet, is(notNullValue()));
            assertThat(facet, is(instanceOf(CssClassFaFacetForActionLayoutAnnotation.class)));
            CssClassFaFacetForActionLayoutAnnotation classFaFacetForActionLayoutAnnotation = (CssClassFaFacetForActionLayoutAnnotation) facet;
            assertThat(classFaFacetForActionLayoutAnnotation.asSpaceSeparated(), is(equalTo("fa fa-fw fa-font-awesome")));
            assertThat(classFaFacetForActionLayoutAnnotation.getPosition(), is(CssClassFaPosition.RIGHT));
        }

    }

}
