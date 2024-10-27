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
package org.apache.causeway.core.metamodel.facets.properties.property;

import javax.validation.constraints.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactory;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.regex.RegExFacetForPatternAnnotationOnProperty;

class RegExAnnotationOnPropertyFacetFactoryTest
extends FacetFactoryTestAbstract {

    private PropertyAnnotationFacetFactory facetFactory;

    @BeforeEach
    protected void setUp() {
        facetFactory = new PropertyAnnotationFacetFactory(getMetaModelContext());
    }

    private void processRegEx(
            final PropertyAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        var propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processRegEx(processMethodContext, propertyIfAny);
    }

    @Test
    void regExAnnotationPickedUpOnProperty() {
        class Customer {
            @Pattern(regexp = "^A.*", flags = { Pattern.Flag.CASE_INSENSITIVE })
            public String getFirstName() { return null; }
        }
        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            processRegEx(facetFactory, processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(RegExFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof RegExFacetForPatternAnnotationOnProperty);
            final RegExFacetForPatternAnnotationOnProperty regExFacet = (RegExFacetForPatternAnnotationOnProperty) facet;
            assertEquals("^A.*", regExFacet.regexp());
            assertEquals(2, regExFacet.patternFlags());
        });
    }

    @Test
    void regExAnnotationIgnoredForNonStringsProperty() {
        @SuppressWarnings("unused")
        class Customer {
            public int getNumberOfOrders() { return 0; }
        }
        propertyScenario(Customer.class, "numberOfOrders", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            processRegEx(facetFactory, processMethodContext);
            // then
            assertNull(facetedMethod.getFacet(RegExFacet.class));
        });
    }

}
