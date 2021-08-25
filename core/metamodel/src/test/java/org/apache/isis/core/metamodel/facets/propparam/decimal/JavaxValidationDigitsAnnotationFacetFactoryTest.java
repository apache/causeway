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

import org.apache.isis.applib.annotation.Encapsulation.EncapsulationPolicy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.param.bigdecimal.javaxvaldigits.BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.bigdecimal.javaxvaldigits.BigDecimalFacetOnParameterFromJavaxValidationDigitsAnnotation;
import org.apache.isis.core.metamodel.facets.properties.bigdecimal.javaxvaldigits.BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotation;
import org.apache.isis.core.metamodel.facets.properties.bigdecimal.javaxvaldigits.BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;

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

        facetFactory.process(new FacetFactory.ProcessMethodContext(Order.class, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(BigDecimalValueFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotation);
        final BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotation annotation = (BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotation) facet;
        assertEquals(18, annotation.getPrecision());
        assertEquals(4, annotation.getScale());
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



        facetFactory.processParams(new FacetFactory.ProcessParameterContext(Customer.class, EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED, method, 0, null, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(BigDecimalValueFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof BigDecimalFacetOnParameterFromJavaxValidationDigitsAnnotation);
        final BigDecimalFacetOnParameterFromJavaxValidationDigitsAnnotation annotation = (BigDecimalFacetOnParameterFromJavaxValidationDigitsAnnotation) facet;
        assertEquals(18, annotation.getPrecision());
        assertEquals(4, annotation.getScale());
    }

}
