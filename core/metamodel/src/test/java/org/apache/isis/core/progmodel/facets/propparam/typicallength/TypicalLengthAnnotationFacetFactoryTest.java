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

package org.apache.isis.core.progmodel.facets.propparam.typicallength;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.typicallen.TypicalLengthFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.param.typicallen.annotation.TypicalLengthAnnotationOnParameterFacetFactory;
import org.apache.isis.core.progmodel.facets.param.typicallen.annotation.TypicalLengthFacetAnnotationOnParameter;
import org.apache.isis.core.progmodel.facets.properties.typicallen.annotation.TypicalLengthAnnotationOnPropertyFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.typicallen.annotation.TypicalLengthFacetAnnotationOnProperty;

public class TypicalLengthAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    public void testTypicalLengthAnnotationPickedUpOnProperty() {
        final TypicalLengthAnnotationOnPropertyFacetFactory facetFactory = new TypicalLengthAnnotationOnPropertyFacetFactory();

        class Customer {
            @SuppressWarnings("unused")
            @TypicalLength(30)
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        facetFactory.process(new ProcessMethodContext(Customer.class, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TypicalLengthFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypicalLengthFacetAnnotationOnProperty);
        final TypicalLengthFacetAnnotationOnProperty typicalLengthFacetAnnotation = (TypicalLengthFacetAnnotationOnProperty) facet;
        assertEquals(30, typicalLengthFacetAnnotation.value());
    }

    public void testTypicalLengthAnnotationPickedUpOnActionParameter() {
        final TypicalLengthAnnotationOnParameterFacetFactory facetFactory = new TypicalLengthAnnotationOnParameterFacetFactory();

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@TypicalLength(20) final int foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(new ProcessParameterContext(method, 0, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(TypicalLengthFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypicalLengthFacetAnnotationOnParameter);
        final TypicalLengthFacetAnnotationOnParameter typicalLengthFacetAnnotation = (TypicalLengthFacetAnnotationOnParameter) facet;
        assertEquals(20, typicalLengthFacetAnnotation.value());
    }

}
