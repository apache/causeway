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

package org.apache.isis.core.metamodel.facets.propparam.validate.mandatory;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.param.mandatory.annotation.MandatoryFacetOnParameterInvertedByOptionalAnnotation;
import org.apache.isis.core.metamodel.facets.param.mandatory.annotation.MandatoryFacetOnParameterInvertedByOptionalAnnotationFactory;
import org.apache.isis.core.metamodel.facets.properties.mandatory.annotation.optional.MandatoryFacetOnPropertyInvertedByOptionalAnnotation;
import org.apache.isis.core.metamodel.facets.properties.mandatory.annotation.optional.MandatoryFacetOnPropertyInvertedByOptionalAnnotationFactory;
import org.apache.isis.core.metamodel.facets.propparam.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;

public class OptionalAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    public void testOptionalAnnotationPickedUpOnProperty() {
        final MandatoryFacetOnPropertyInvertedByOptionalAnnotationFactory facetFactory = new MandatoryFacetOnPropertyInvertedByOptionalAnnotationFactory();

        class Customer {
            @Optional
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MandatoryFacetOnPropertyInvertedByOptionalAnnotation);
    }

    public void testOptionalAnnotationPickedUpOnActionParameter() {
        final MandatoryFacetOnParameterInvertedByOptionalAnnotationFactory facetFactory = new MandatoryFacetOnParameterInvertedByOptionalAnnotationFactory();

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@Optional final String foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { String.class });

        facetFactory.processParams(new ProcessParameterContext(method, 0, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MandatoryFacetOnParameterInvertedByOptionalAnnotation);
    }

    public void testOptionalAnnotationIgnoredForPrimitiveOnProperty() {
        final MandatoryFacetOnPropertyInvertedByOptionalAnnotationFactory facetFactory = new MandatoryFacetOnPropertyInvertedByOptionalAnnotationFactory();

        class Customer {
            @SuppressWarnings("unused")
            @Optional
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method method = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(MandatoryFacet.class));
    }

    public void testOptionalAnnotationIgnoredForPrimitiveOnActionParameter() {
        final MandatoryFacetOnParameterInvertedByOptionalAnnotationFactory facetFactory = new MandatoryFacetOnParameterInvertedByOptionalAnnotationFactory();

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@Optional final int foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(new ProcessParameterContext(method, 0, facetedMethodParameter));

        assertNull(facetedMethod.getFacet(MandatoryFacet.class));
    }

}
