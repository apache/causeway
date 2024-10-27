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
package org.apache.causeway.core.metamodel.facets.properties.propertylayout;

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

import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.all.i8n.staatic.HasStaticText;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;

class PropertyLayoutAnnotationFactoryTest
extends FacetFactoryTestAbstract {

    private PropertyLayoutFacetFactory facetFactory;

    @BeforeEach
    protected void setUp() {
        facetFactory = new PropertyLayoutFacetFactory(getMetaModelContext());
    }

    @Test
    void propertyLayoutAnnotation_named() {

        class Customer {
            @PropertyLayout(named = "1st name")
            public String getFirstName() { return null; }
        }
        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            //when
            facetFactory.process(processMethodContext);
            //then
            var facet = facetedMethod.getFacet(MemberNamedFacet.class);
            assertThat(facet, is(notNullValue()));
            assertThat(facet, is(instanceOf(NamedFacetForPropertyLayoutAnnotation.class)));
            assertThat(((HasStaticText)facet).text(), is(equalTo("1st name")));
        });
    }

    @Test
    void propertyLayoutAnnotation_hidden() {

        class Customer {
            @PropertyLayout(hidden = Where.OBJECT_FORMS)
            public String getFirstName() { return null; }
        }
        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            //when
            facetFactory.process(processMethodContext);
            //then
            final Facet facet = facetedMethod.getFacet(HiddenFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof HiddenFacetForPropertyLayoutAnnotation);
            var propLayoutFacetAnnotation = (HiddenFacetForPropertyLayoutAnnotation) facet;
            assertEquals(Where.OBJECT_FORMS, propLayoutFacetAnnotation.where());
        });
    }

    @Test
    void propertyLayoutAnnotation_labelPosition() {

        class Customer {
            @PropertyLayout(labelPosition = LabelPosition.LEFT)
            public String getFirstName() { return null; }
        }
        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            //when
            facetFactory.process(processMethodContext);
            //then
            final Facet facet = facetedMethod.getFacet(LabelAtFacet.class);
            assertThat(facet, is(notNullValue()));
            assertThat(facet, is(instanceOf(LabelAtFacetForPropertyLayoutAnnotation.class)));
            var layoutAnnotation = (LabelAtFacetForPropertyLayoutAnnotation) facet;
            assertThat(layoutAnnotation.label(), is(equalTo(LabelPosition.LEFT)));
        });
    }

}
