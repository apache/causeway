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
import org.junit.Before;
import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.param.parameter.regex.RegExFacetFromRegExAnnotationOnParameter;

public class RegExAnnotationOnParameterFacetFactoryTest extends AbstractFacetFactoryTest {

    private ParameterAnnotationFacetFactory facetFactory;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        facetFactory = new ParameterAnnotationFacetFactory();
    }

    public void testRegExAnnotationPickedUpOnActionParameter() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@RegEx(validation = "^A.*", caseSensitive = false) final String foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { String.class });

        facetFactory.processParams(new ProcessParameterContext(Customer.class, method, 0, null, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(RegExFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RegExFacetFromRegExAnnotationOnParameter);
        final RegExFacetFromRegExAnnotationOnParameter regExFacet = (RegExFacetFromRegExAnnotationOnParameter) facet;
        assertEquals("^A.*", regExFacet.validation());
        assertEquals(false, regExFacet.caseSensitive());
    }

    public void testRegExAnnotationIgnoredForPrimitiveOnActionParameter() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@RegEx(validation = "^A.*", caseSensitive = false) final int foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(new ProcessParameterContext(Customer.class, method, 0, null, facetedMethodParameter));

        assertNull(facetedMethod.getFacet(RegExFacet.class));
    }

}
