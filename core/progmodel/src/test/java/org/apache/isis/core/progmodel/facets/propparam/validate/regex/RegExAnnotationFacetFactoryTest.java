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

package org.apache.isis.core.progmodel.facets.propparam.validate.regex;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.object.regex.RegExFacet;
import org.apache.isis.core.progmodel.facets.object.regex.annotation.RegExFacetAnnotationForType;
import org.apache.isis.core.progmodel.facets.object.regex.annotation.RegExFacetAnnotationForTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.param.validate.regexannot.RegExFacetAnnotationForParameter;
import org.apache.isis.core.progmodel.facets.param.validate.regexannot.RegExFacetAnnotationForParameterFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.validate.regexannot.RegExFacetAnnotationForProperty;
import org.apache.isis.core.progmodel.facets.properties.validate.regexannot.RegExFacetAnnotationForPropertyFacetFactory;

public class RegExAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    public void testRegExAnnotationPickedUpOnClass() {
        final RegExFacetAnnotationForTypeFacetFactory facetFactory = new RegExFacetAnnotationForTypeFacetFactory();

        @RegEx(validation = "^A.*", caseSensitive = false)
        class Customer {
        }
        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(RegExFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RegExFacetAnnotationForType);
        final RegExFacetAnnotationForType regExFacet = (RegExFacetAnnotationForType) facet;
        assertEquals("^A.*", regExFacet.validation());
        assertEquals(false, regExFacet.caseSensitive());
    }

    public void testRegExAnnotationPickedUpOnProperty() {
        final RegExFacetAnnotationForPropertyFacetFactory facetFactory = new RegExFacetAnnotationForPropertyFacetFactory();

        class Customer {
            @SuppressWarnings("unused")
            @RegEx(validation = "^A.*", caseSensitive = false)
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        facetFactory.process(new ProcessMethodContext(Customer.class, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(RegExFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RegExFacetAnnotationForProperty);
        final RegExFacetAnnotationForProperty regExFacet = (RegExFacetAnnotationForProperty) facet;
        assertEquals("^A.*", regExFacet.validation());
        assertEquals(false, regExFacet.caseSensitive());
    }

    public void testRegExAnnotationPickedUpOnActionParameter() {
        final RegExFacetAnnotationForParameterFacetFactory facetFactory = new RegExFacetAnnotationForParameterFacetFactory();

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@RegEx(validation = "^A.*", caseSensitive = false) final String foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { String.class });

        facetFactory.processParams(new ProcessParameterContext(method, 0, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(RegExFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RegExFacetAnnotationForParameter);
        final RegExFacetAnnotationForParameter regExFacet = (RegExFacetAnnotationForParameter) facet;
        assertEquals("^A.*", regExFacet.validation());
        assertEquals(false, regExFacet.caseSensitive());
    }

    public void testRegExAnnotationIgnoredForNonStringsProperty() {
        final RegExFacetAnnotationForParameterFacetFactory facetFactory = new RegExFacetAnnotationForParameterFacetFactory();

        class Customer {
            @SuppressWarnings("unused")
            @RegEx(validation = "^A.*", caseSensitive = false)
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method method = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, method, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(RegExFacet.class));
    }

    public void testRegExAnnotationIgnoredForPrimitiveOnActionParameter() {
        final RegExFacetAnnotationForParameterFacetFactory facetFactory = new RegExFacetAnnotationForParameterFacetFactory();

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
