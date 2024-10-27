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
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.causeway.core.metamodel.facets.actions.position.ActionPositionFacetFallback;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;

class ActionLayoutAnnotationFacetFactoryTest
extends FacetFactoryTestAbstract {

    ActionLayoutFacetFactory facetFactory;

    @BeforeEach
    void setUp() throws Exception {
        facetFactory = new ActionLayoutFacetFactory(getMetaModelContext());
    }

    @Test
    void actionLayoutAnnotation_position() {

        class Customer {
            @ActionLayout(position = ActionLayout.Position.PANEL)
            public String foz() { return null; }
        }

        actionScenario(Customer.class, "foz", (processMethodContext, facetHolder, facetedMethod)->{
            facetFactory.process(processMethodContext);

            final Facet facet = facetedMethod.getFacet(ActionPositionFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof ActionPositionFacetForActionLayoutAnnotation);
            var actionLayoutFacetAnnotation = (ActionPositionFacetForActionLayoutAnnotation) facet;
            assertEquals(ActionLayout.Position.PANEL, actionLayoutFacetAnnotation.position());
        });
    }

    @Test
    void actionLayoutAnnotation_hidden() {

        class Customer {
            @ActionLayout(hidden = Where.ALL_TABLES)
            public String foz() { return null; }
        }

        actionScenario(Customer.class, "foz", (processMethodContext, facetHolder, facetedMethod)->{
            facetFactory.process(processMethodContext);

            final Facet facet = facetedMethod.getFacet(HiddenFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof HiddenFacetForActionLayoutAnnotation);
            var actionLayoutFacetAnnotation = (HiddenFacetForActionLayoutAnnotation) facet;
            assertEquals(Where.ALL_TABLES, actionLayoutFacetAnnotation.where());
        });
    }

    @Test
    void actionLayoutFallbackPickedUp() {

        @SuppressWarnings("unused")
        class Customer {
            // no @ActionLayout
            public String foo() { return null; }
        }

        actionScenario(Customer.class, "foo", (processMethodContext, facetHolder, facetedMethod)->{
            facetFactory.process(processMethodContext);

            final Facet facet = facetedMethod.getFacet(ActionPositionFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof ActionPositionFacetFallback);
        });
    }

    @Test
    void cssClassFa_defaultPosition() {

        class Customer {
            @ActionLayout(cssClassFa = "font-awesome")
            public String foz() { return null; }
        }

        actionScenario(Customer.class, "foz", (processMethodContext, facetHolder, facetedMethod)->{
            facetFactory.process(processMethodContext);

            Facet facet = facetedMethod.getFacet(FaFacet.class);
            assertThat(facet, is(notNullValue()));
            assertThat(facet, is(instanceOf(FaFacetForActionLayoutAnnotation.class)));
            var classFaFacetForActionLayoutAnnotation = (FaFacetForActionLayoutAnnotation) facet;
            assertThat(classFaFacetForActionLayoutAnnotation.getLayers().getIconEntries().get(0).getCssClasses(),
                    is(equalTo("fa fa-font-awesome fa-fw")));
            assertThat(classFaFacetForActionLayoutAnnotation.getLayers().getPosition(), is(CssClassFaPosition.LEFT));

        });
    }

    @Test
    void cssClassFa_rightPosition() {

        class Customer {
            @ActionLayout(cssClassFa = "font-awesome", cssClassFaPosition = CssClassFaPosition.RIGHT)
            public String foz() { return null; }
        }

        actionScenario(Customer.class, "foz", (processMethodContext, facetHolder, facetedMethod)->{
            facetFactory.process(processMethodContext);

            Facet facet = facetedMethod.getFacet(FaFacet.class);
            assertThat(facet, is(notNullValue()));
            assertThat(facet, is(instanceOf(FaFacetForActionLayoutAnnotation.class)));
            var classFaFacetForActionLayoutAnnotation = (FaFacetForActionLayoutAnnotation) facet;
            assertThat(classFaFacetForActionLayoutAnnotation.getLayers().getIconEntries().get(0).getCssClasses(),
                    is(equalTo("fa fa-font-awesome fa-fw")));
            assertThat(classFaFacetForActionLayoutAnnotation.getLayers().getPosition(), is(CssClassFaPosition.RIGHT));

        });

    }

}
