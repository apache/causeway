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
package org.apache.isis.core.metamodel.facets.properties.property;

import java.lang.reflect.Method;

import javax.validation.constraints.Pattern;

import org.junit.Before;

import org.apache.isis.applib.annotations.Property;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.properties.property.regex.RegExFacetForPatternAnnotationOnProperty;

import lombok.val;

public class RegExAnnotationOnPropertyFacetFactoryTest
extends AbstractFacetFactoryTest {

    private PropertyAnnotationFacetFactory facetFactory;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        facetFactory = new PropertyAnnotationFacetFactory(metaModelContext);
    }

    private void processRegEx(
            final PropertyAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processRegEx(processMethodContext, propertyIfAny);
    }

    public void testRegExAnnotationPickedUpOnProperty() {

        class Customer {
            @Pattern(regexp = "^A.*", flags = { Pattern.Flag.CASE_INSENSITIVE })
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        processRegEx(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(RegExFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RegExFacetForPatternAnnotationOnProperty);
        final RegExFacetForPatternAnnotationOnProperty regExFacet = (RegExFacetForPatternAnnotationOnProperty) facet;
        assertEquals("^A.*", regExFacet.regexp());
        assertEquals(2, regExFacet.patternFlags());
    }

    public void testRegExAnnotationIgnoredForNonStringsProperty() {

        class Customer {
            @SuppressWarnings("unused")
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method method = findMethod(Customer.class, "getNumberOfOrders");

        processRegEx(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, method, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(RegExFacet.class));
    }


}
