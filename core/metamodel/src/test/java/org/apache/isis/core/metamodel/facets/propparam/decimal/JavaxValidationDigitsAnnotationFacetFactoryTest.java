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
package org.apache.isis.core.metamodel.facets.propparam.decimal;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxFractionalDigitsFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxTotalDigitsFacet;
import org.apache.isis.core.metamodel.facets.param.bigdecimal.javaxvaldigits.BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.bigdecimal.javaxvaldigits.MaxFractionalDigitsFacetOnParameterFromJavaxValidationDigitsAnnotation;
import org.apache.isis.core.metamodel.facets.param.bigdecimal.javaxvaldigits.MaxTotalDigitsFacetOnParameterFromJavaxValidationDigitsAnnotation;
import org.apache.isis.core.metamodel.facets.properties.bigdecimal.javaxvaldigits.BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory;
import org.apache.isis.core.metamodel.facets.properties.bigdecimal.javaxvaldigits.MaxFractionalDigitsFacetOnPropertyFromJavaxValidationDigitsAnnotation;
import org.apache.isis.core.metamodel.facets.properties.bigdecimal.javaxvaldigits.MaxTotalDigitsFacetOnPropertyFromJavaxValidationDigitsAnnotation;

public class JavaxValidationDigitsAnnotationFacetFactoryTest
extends AbstractFacetFactoryTest {

    public void testAnnotationPickedUpOnProperty() {
        final BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory facetFactory =
                new BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory(metaModelContext);

        class Order {
            @javax.validation.constraints.Digits(integer=14, fraction=4)
            public BigDecimal getCost() {
                return null;
            }
        }
        final Method method = findMethod(Order.class, "getCost");

        facetFactory.process(ProcessMethodContext
                .forTesting(Order.class, null, method, methodRemover, facetedMethod));

        assertBigDecimalSemantics(facetedMethod, 18, 4);
    }

    public void testAnnotationPickedUpOnActionParameter() {
        final BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory facetFactory =
                new BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory(metaModelContext);

        class Order {
            @SuppressWarnings("unused")
            public void updateCost(
                    @javax.validation.constraints.Digits(integer=14, fraction=4)
                    final BigDecimal cost) {
            }
        }
        final Method method = findMethod(Order.class, "updateCost", new Class[] { BigDecimal.class });

        facetFactory.processParams(new FacetFactory
                .ProcessParameterContext(Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, method, null, facetedMethodParameter));

        assertBigDecimalSemantics(facetedMethodParameter, 18, 4);

    }

    // -- HELPER

    private void assertBigDecimalSemantics(
            final FacetHolder facetedMethod, final int maxTotalDigits, final int maxFractionalDigits) {
        if(maxTotalDigits>=0) {
            final MaxTotalDigitsFacet facet = facetedMethod.getFacet(MaxTotalDigitsFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof MaxTotalDigitsFacetOnPropertyFromJavaxValidationDigitsAnnotation
                    ||facet instanceof MaxTotalDigitsFacetOnParameterFromJavaxValidationDigitsAnnotation);
            assertThat(facet.maxTotalDigits(), is(maxTotalDigits));
        }

        if(maxFractionalDigits>=0) {
            final MaxFractionalDigitsFacet facet = facetedMethod.getFacet(MaxFractionalDigitsFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof MaxFractionalDigitsFacetOnPropertyFromJavaxValidationDigitsAnnotation
                    ||facet instanceof MaxFractionalDigitsFacetOnParameterFromJavaxValidationDigitsAnnotation);
            assertThat(facet.maxFractionalDigits(), is(maxFractionalDigits));
        }
    }

}
