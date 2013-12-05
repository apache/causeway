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

package org.apache.isis.core.progmodel.facets.propparam.decimal;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.apache.isis.applib.annotation.Decimal;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.typicallen.TypicalLengthFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.param.decimal.BigDecimalFacetForParameterFromDecimalAnnotation;
import org.apache.isis.core.progmodel.facets.param.decimal.BigDecimalForParameterDerivedFromDecimalAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.param.typicallen.annotation.TypicalLengthAnnotationOnParameterFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.decimal.BigDecimalFacetForPropertyFromDecimalAnnotation;
import org.apache.isis.core.progmodel.facets.properties.decimal.BigDecimalForPropertyDerivedFromDecimalAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.value.bigdecimal.BigDecimalValueFacet;

public class BigDecimalAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    public void testDecimalAnnotationPickedUpOnProperty() {
        final BigDecimalForPropertyDerivedFromDecimalAnnotationFacetFactory facetFactory = new BigDecimalForPropertyDerivedFromDecimalAnnotationFacetFactory();

        class Order {
            @SuppressWarnings("unused")
            @Decimal(length=14, scale=4)
            public BigDecimal getCost() {
                return null;
            }
        }
        final Method method = findMethod(Order.class, "getCost");

        facetFactory.process(new ProcessMethodContext(Order.class, null, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(BigDecimalValueFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof BigDecimalFacetForPropertyFromDecimalAnnotation);
        final BigDecimalFacetForPropertyFromDecimalAnnotation annotation = (BigDecimalFacetForPropertyFromDecimalAnnotation) facet;
        assertEquals(new Integer(14), annotation.getLength());
        assertEquals(new Integer(4), annotation.getScale());
    }

    public void testDecimalAnnotationPickedUpOnActionParameter() {
        final BigDecimalForParameterDerivedFromDecimalAnnotationFacetFactory facetFactory = new BigDecimalForParameterDerivedFromDecimalAnnotationFacetFactory();

        class Order {
            @SuppressWarnings("unused")
            public void updateCost(@Decimal(length=14,scale=4) final BigDecimal cost) {
            }
        }
        final Method method = findMethod(Order.class, "updateCost", new Class[] { BigDecimal.class });

        facetFactory.processParams(new ProcessParameterContext(method, 0, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(BigDecimalValueFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof BigDecimalFacetForParameterFromDecimalAnnotation);
        final BigDecimalFacetForParameterFromDecimalAnnotation annotation = (BigDecimalFacetForParameterFromDecimalAnnotation) facet;
        assertEquals(new Integer(14), annotation.getLength());
        assertEquals(new Integer(4), annotation.getScale());
    }

}
