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

package org.apache.isis.core.metamodel.facets.param.parameter;

import java.lang.reflect.Method;

import javax.validation.constraints.Pattern;

import org.junit.Before;

import org.apache.isis.core.config.IsisConfiguration.Core.MetaModel.EncapsulationPolicy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.param.parameter.regex.RegExFacetForPatternAnnotationOnParameter;

public class RegExAnnotationOnParameterFacetFactoryTest extends AbstractFacetFactoryTest {

    private ParameterAnnotationFacetFactory facetFactory;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        facetFactory = new ParameterAnnotationFacetFactory(metaModelContext);
    }

    public void testRegExAnnotationPickedUpOnActionParameter() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@Pattern(regexp = "^A.*", flags = { Pattern.Flag.CASE_INSENSITIVE }) final String foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { String.class });

        facetFactory.processParams(new ProcessParameterContext(Customer.class, EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED, method, 0, null, facetedMethodParameter));

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

        facetFactory.processParams(new ProcessParameterContext(Customer.class, EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED, method, 0, null, facetedMethodParameter));

        assertNull(facetedMethod.getFacet(RegExFacet.class));
    }

}
