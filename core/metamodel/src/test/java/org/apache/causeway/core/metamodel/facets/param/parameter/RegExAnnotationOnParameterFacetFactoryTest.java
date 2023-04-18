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
package org.apache.causeway.core.metamodel.facets.param.parameter;

import java.lang.reflect.Method;

import javax.validation.constraints.Pattern;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.causeway.core.metamodel.facets.param.parameter.regex.RegExFacetForPatternAnnotationOnParameter;

class RegExAnnotationOnParameterFacetFactoryTest
extends FacetFactoryTestAbstract {

    private ParameterAnnotationFacetFactory facetFactory;

    @BeforeEach
    protected void setUp() {
        facetFactory = new ParameterAnnotationFacetFactory(getMetaModelContext());
    }

    public void testRegExAnnotationPickedUpOnActionParameter() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@Pattern(regexp = "^A.*", flags = { Pattern.Flag.CASE_INSENSITIVE }) final String foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { String.class });

        facetFactory.processParams(
                ProcessParameterContext.forTesting(
                        Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, method, null, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(RegExFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RegExFacetForPatternAnnotationOnParameter);
        final RegExFacetForPatternAnnotationOnParameter regExFacet = (RegExFacetForPatternAnnotationOnParameter) facet;
        assertEquals("^A.*", regExFacet.regexp());
        assertEquals(2, regExFacet.patternFlags());
    }

    public void testRegExAnnotationIgnoredForPrimitiveOnActionParameter() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(
                ProcessParameterContext.forTesting(
                        Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, method, null, facetedMethodParameter));

        assertNull(facetedMethod.getFacet(RegExFacet.class));
    }

}
