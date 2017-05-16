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

package org.apache.isis.core.metamodel.facets.propparam.validate.mask;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Mask;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.object.mask.annotation.MaskFacetOnTypeAnnotation;
import org.apache.isis.core.metamodel.facets.object.mask.annotation.MaskFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.validating.maskannot.MaskFacetOnParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.validating.maskannot.MaskFacetOnParameterAnnotationFactory;
import org.apache.isis.core.metamodel.facets.properties.validating.maskannot.MaskFacetOnPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.validating.maskannot.MaskFacetOnPropertyAnnotationFactory;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.testspec.ObjectSpecificationStub;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.object.mask.MaskFacet;

public class MaskAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private final ObjectSpecification customerNoSpec = new ObjectSpecificationStub(String.class);

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // mockSpecificationLoader.setLoadSpecificationStringReturn(customerNoSpec);
        allowing_specificationLoader_loadSpecification_any_willReturn(customerNoSpec);

    }

    public void testMaskAnnotationPickedUpOnClass() {
        final MaskFacetOnTypeAnnotationFactory facetFactory = new MaskFacetOnTypeAnnotationFactory();

        @Mask("###")
        class Customer {
        }
        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MaskFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MaskFacetOnTypeAnnotation);
        final MaskFacetOnTypeAnnotation maskFacet = (MaskFacetOnTypeAnnotation) facet;
        assertEquals("###", maskFacet.value());
    }

    public void testMaskAnnotationPickedUpOnProperty() {
        final MaskFacetOnPropertyAnnotationFactory facetFactory = new MaskFacetOnPropertyAnnotationFactory();
        facetFactory.setServicesInjector(stubServicesInjector);

        class Customer {
            @SuppressWarnings("unused")
            @Mask("###")
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MaskFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MaskFacetOnPropertyAnnotation);
        final MaskFacetOnPropertyAnnotation maskFacet = (MaskFacetOnPropertyAnnotation) facet;
        assertEquals("###", maskFacet.value());
    }

    public void testMaskAnnotationPickedUpOnActionParameter() {
        final MaskFacetOnParameterAnnotationFactory facetFactory = new MaskFacetOnParameterAnnotationFactory();
        facetFactory.setServicesInjector(stubServicesInjector);


        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@Mask("###") final String foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { String.class });

        facetFactory.processParams(new ProcessParameterContext(Customer.class, method, 0, null, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(MaskFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MaskFacetOnParameterAnnotation);
        final MaskFacetOnParameterAnnotation maskFacet = (MaskFacetOnParameterAnnotation) facet;
        assertEquals("###", maskFacet.value());
    }

    public void testMaskAnnotationNotIgnoredForNonStringsProperty() {
        final MaskFacetOnPropertyAnnotationFactory facetFactory = new MaskFacetOnPropertyAnnotationFactory();
        facetFactory.setServicesInjector(stubServicesInjector);


        class Customer {
            @SuppressWarnings("unused")
            @Mask("###")
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method method = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod));

        assertNotNull(facetedMethod.getFacet(MaskFacet.class));
    }

    public void testMaskAnnotationNotIgnoredForPrimitiveOnActionParameter() {
        final MaskFacetOnParameterAnnotationFactory facetFactory = new MaskFacetOnParameterAnnotationFactory();
        facetFactory.setServicesInjector(stubServicesInjector);


        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@Mask("###") final int foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { int.class });

        facetFactory.processParams(new ProcessParameterContext(Customer.class, method, 0, null, facetedMethodParameter));

        assertNotNull(facetedMethodParameter.getFacet(MaskFacet.class));
    }

}
