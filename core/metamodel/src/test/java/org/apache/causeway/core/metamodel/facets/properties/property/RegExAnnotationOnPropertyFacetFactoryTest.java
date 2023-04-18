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

import java.lang.reflect.Method;

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
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract2;
import org.apache.causeway.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.regex.RegExFacetForPatternAnnotationOnProperty;

import lombok.val;

class RegExAnnotationOnPropertyFacetFactoryTest
extends FacetFactoryTestAbstract2 {

    private PropertyAnnotationFacetFactory facetFactory;

    @BeforeEach
    protected void setUp() {
        facetFactory = new PropertyAnnotationFacetFactory(getMetaModelContext());
    }

    private void processRegEx(
            final PropertyAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processRegEx(processMethodContext, propertyIfAny);
    }

    @Test
    void testRegExAnnotationPickedUpOnProperty() {

        class Customer {
            @Pattern(regexp = "^A.*", flags = { Pattern.Flag.CASE_INSENSITIVE })
            public String getFirstName() { return null; }
        }
        final Method method = findMethodExactOrFail(Customer.class, "getFirstName");

        processRegEx(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(RegExFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RegExFacetForPatternAnnotationOnProperty);
        final RegExFacetForPatternAnnotationOnProperty regExFacet = (RegExFacetForPatternAnnotationOnProperty) facet;
        assertEquals("^A.*", regExFacet.regexp());
        assertEquals(2, regExFacet.patternFlags());
    }

    @Test
    void testRegExAnnotationIgnoredForNonStringsProperty() {

        @SuppressWarnings("unused")
        class Customer {
            public int getNumberOfOrders() { return 0; }
        }
        final Method method = findMethodExactOrFail(Customer.class, "getNumberOfOrders");

        processRegEx(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, method, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(RegExFacet.class));
    }


}
