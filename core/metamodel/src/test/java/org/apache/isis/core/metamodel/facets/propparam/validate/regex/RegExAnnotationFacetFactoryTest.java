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

package org.apache.isis.core.metamodel.facets.propparam.validate.regex;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.object.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.object.regex.annotation.RegExFacetOnTypeAnnotation;
import org.apache.isis.core.metamodel.facets.object.regex.annotation.RegExFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.validating.regexannot.RegExFacetFacetOnParameterAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.validating.regexannot.RegExFacetOnParameterAnnotation;
import org.apache.isis.core.metamodel.facets.properties.validating.regexannot.RegExFacetFacetOnPropertyAnnotationFactory;
import org.apache.isis.core.metamodel.facets.properties.validating.regexannot.RegExFacetOnPropertyAnnotation;

public class RegExAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    public void testRegExAnnotationPickedUpOnClass() {
        final RegExFacetOnTypeAnnotationFactory facetFactory = new RegExFacetOnTypeAnnotationFactory();

        @RegEx(validation = "^A.*", caseSensitive = false)
        class Customer {
        }
        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(RegExFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RegExFacetOnTypeAnnotation);
        final RegExFacetOnTypeAnnotation regExFacet = (RegExFacetOnTypeAnnotation) facet;
        assertEquals("^A.*", regExFacet.validation());
        assertEquals(false, regExFacet.caseSensitive());
    }

    public void testRegExAnnotationPickedUpOnProperty() {
        final RegExFacetFacetOnPropertyAnnotationFactory facetFactory = new RegExFacetFacetOnPropertyAnnotationFactory();

        class Customer {
            @SuppressWarnings("unused")
            @RegEx(validation = "^A.*", caseSensitive = false)
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(RegExFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RegExFacetOnPropertyAnnotation);
        final RegExFacetOnPropertyAnnotation regExFacet = (RegExFacetOnPropertyAnnotation) facet;
        assertEquals("^A.*", regExFacet.validation());
        assertEquals(false, regExFacet.caseSensitive());
    }

    public void testRegExAnnotationPickedUpOnActionParameter() {
        final RegExFacetFacetOnParameterAnnotationFactory facetFactory = new RegExFacetFacetOnParameterAnnotationFactory();

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@RegEx(validation = "^A.*", caseSensitive = false) final String foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { String.class });

        facetFactory.processParams(new ProcessParameterContext(method, 0, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(RegExFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RegExFacetOnParameterAnnotation);
        final RegExFacetOnParameterAnnotation regExFacet = (RegExFacetOnParameterAnnotation) facet;
        assertEquals("^A.*", regExFacet.validation());
        assertEquals(false, regExFacet.caseSensitive());
    }

    public void testRegExAnnotationIgnoredForNonStringsProperty() {
        final RegExFacetFacetOnParameterAnnotationFactory facetFactory = new RegExFacetFacetOnParameterAnnotationFactory();

        class Customer {
            @SuppressWarnings("unused")
            @RegEx(validation = "^A.*", caseSensitive = false)
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method method = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(RegExFacet.class));
    }

    public void testRegExAnnotationIgnoredForPrimitiveOnActionParameter() {
        final RegExFacetFacetOnParameterAnnotationFactory facetFactory = new RegExFacetFacetOnParameterAnnotationFactory();

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@RegEx(validation = "^A.*", caseSensitive = false) final int foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(new ProcessParameterContext(method, 0, facetedMethodParameter));

        assertNull(facetedMethod.getFacet(RegExFacet.class));
    }

}
