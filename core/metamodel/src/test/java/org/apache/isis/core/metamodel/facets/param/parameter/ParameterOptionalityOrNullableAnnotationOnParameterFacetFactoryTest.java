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

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetInvertedByNullableAnnotationOnParameter;

public class ParameterOptionalityOrNullableAnnotationOnParameterFacetFactoryTest extends AbstractFacetFactoryTest {

    private ParameterAnnotationFacetFactory facetFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        facetFactory = new ParameterAnnotationFacetFactory(metaModelContext);
    }

    public void testParameterAnnotationWithOptionalityPickedUpOnActionParameter() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@Parameter(optionality = Optionality.OPTIONAL) final String foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { String.class });

        facetFactory.processParamsOptional(
                new ProcessParameterContext(
                        Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, method, 0, null, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MandatoryFacetForParameterAnnotation.Optional);
    }

    public void testParameterAnnotationWithOptionalityIgnoredForPrimitiveOnActionParameter() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@Parameter(optionality = Optionality.OPTIONAL) final int foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParamsOptional(
                new ProcessParameterContext(
                        Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, method, 0, null, facetedMethodParameter));

        assertNull(facetedMethod.getFacet(MandatoryFacet.class));
    }

    public void testNullableAnnotationPickedUpOnActionParameter() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final @Nullable String foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { String.class });

        facetFactory.processParamsOptional(
                new ProcessParameterContext(
                        Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, method, 0, null, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MandatoryFacetInvertedByNullableAnnotationOnParameter);
    }

    public void testNullableAnnotationIgnoredForPrimitiveOnActionParameter() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final @Nullable int foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParamsOptional(
                new ProcessParameterContext(
                        Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, method, 0, null, facetedMethodParameter));

        assertNull(facetedMethod.getFacet(MandatoryFacet.class));
    }

}
