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

package org.apache.isis.core.metamodel.facets.propparam.renderedasdaybefore;

import java.lang.reflect.Method;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.RenderedAsDayBefore;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.param.renderedasdaybefore.annotation.RenderedAsDayBeforeFacetOnParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.renderedasdaybefore.annotation.RenderedAsDayBeforeFacetOnParameterAnnotationFactory;
import org.apache.isis.core.metamodel.facets.objectvalue.renderedadjusted.RenderedAdjustedFacet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.properties.renderedasdaybefore.annotation.RenderedAsDayBeforeAnnotationOnPropertyFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.renderedasdaybefore.annotation.RenderedAsDayBeforeFacetAnnotationOnProperty;

public class RenderedAsDayBeforeAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    public void testRenderedAdjustedAnnotationPickedUpOnProperty() {
        final RenderedAsDayBeforeAnnotationOnPropertyFacetFactory facetFactory = new RenderedAsDayBeforeAnnotationOnPropertyFacetFactory();

        class Customer {
            @RenderedAsDayBefore
            public LocalDate getEndDate() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getEndDate");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(RenderedAdjustedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RenderedAsDayBeforeFacetAnnotationOnProperty);
        final RenderedAsDayBeforeFacetAnnotationOnProperty facetAnnotation = (RenderedAsDayBeforeFacetAnnotationOnProperty) facet;
        assertEquals(-1, facetAnnotation.value());
    }

    public void testRenderedAdjustedAnnotationPickedUpOnActionParameter() {
        final RenderedAsDayBeforeFacetOnParameterAnnotationFactory facetFactory = new RenderedAsDayBeforeFacetOnParameterAnnotationFactory();

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@RenderedAsDayBefore @Named("End Date") final LocalDate endDate) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { LocalDate.class });

        facetFactory.processParams(new ProcessParameterContext(Customer.class, method, 0, null, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(RenderedAdjustedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RenderedAsDayBeforeFacetOnParameterAnnotation);
        final RenderedAsDayBeforeFacetOnParameterAnnotation facetAnnotation = (RenderedAsDayBeforeFacetOnParameterAnnotation) facet;
        assertEquals(-1, facetAnnotation.value());
    }

}
