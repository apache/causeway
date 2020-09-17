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

package org.apache.isis.core.metamodel.facets.object.facets;

import org.apache.isis.applib.annotation.Facets;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.facets.annotation.FacetsFacetAnnotation;
import org.apache.isis.core.metamodel.facets.object.facets.annotation.FacetsFacetAnnotationFactory;

public class FacetsFacetAnnotationFactoryTest extends AbstractFacetFactoryTest {

    private FacetsFacetAnnotationFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new FacetsFacetAnnotationFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public static class CustomerFacetFactory implements FacetFactory {
        @Override
        public ImmutableEnumSet<FeatureType> getFeatureTypes() {
            return ImmutableEnumSet.noneOf(FeatureType.class);
        }

        @Override
        public void process(final ProcessClassContext processClassContaxt) {
        }

        @Override
        public void process(final ProcessMethodContext processMethodContext) {
        }

        @Override
        public void processParams(final ProcessParameterContext processParameterContext) {
        }
    }

    public static class CustomerFacetFactory2 implements FacetFactory {
        @Override
        public ImmutableEnumSet<FeatureType> getFeatureTypes() {
            return ImmutableEnumSet.noneOf(FeatureType.class);
        }

        @Override
        public void process(final ProcessClassContext processClassContaxt) {
        }

        @Override
        public void process(final ProcessMethodContext processMethodContext) {
        }

        @Override
        public void processParams(final ProcessParameterContext processParameterContext) {
        }
    }

    public void testFacetsFactoryNames() {
        @Facets(facetFactoryNames = { "org.apache.isis.core.metamodel.facets.object.facets.FacetsFacetAnnotationFactoryTest$CustomerFacetFactory", "FacetsFacetAnnotationFactoryTest$CustomerNotAFacetFactory" })
        class Customer {
        }

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(FacetsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof FacetsFacetAnnotation);
        final FacetsFacetAnnotation facetsFacet = (FacetsFacetAnnotation) facet;
        final Class<? extends FacetFactory>[] facetFactories = facetsFacet.facetFactories();
        assertEquals(1, facetFactories.length);
        assertEquals(CustomerFacetFactory.class, facetFactories[0]);

        assertNoMethodsRemoved();
    }

    public void testFacetsFactoryClass() {
        @Facets(facetFactoryClasses = { FacetsFacetAnnotationFactoryTest.CustomerFacetFactory.class, FacetsFacetAnnotationFactoryTest.CustomerNotAFacetFactory.class })
        class Customer {
        }

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(FacetsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof FacetsFacetAnnotation);
        final FacetsFacetAnnotation facetsFacet = (FacetsFacetAnnotation) facet;
        final Class<? extends FacetFactory>[] facetFactories = facetsFacet.facetFactories();
        assertEquals(1, facetFactories.length);
        assertEquals(CustomerFacetFactory.class, facetFactories[0]);
    }

    public static class CustomerNotAFacetFactory {
    }

    public void testFacetsFactoryNameAndClass() {
        @Facets(facetFactoryNames = { "org.apache.isis.core.metamodel.facets.object.facets.FacetsFacetAnnotationFactoryTest$CustomerFacetFactory" }, facetFactoryClasses = { FacetsFacetAnnotationFactoryTest.CustomerFacetFactory2.class })
        class Customer {
        }

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(FacetsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof FacetsFacetAnnotation);
        final FacetsFacetAnnotation facetsFacet = (FacetsFacetAnnotation) facet;
        final Class<? extends FacetFactory>[] facetFactories = facetsFacet.facetFactories();
        assertEquals(2, facetFactories.length);
        assertEquals(CustomerFacetFactory.class, facetFactories[0]);
        assertEquals(CustomerFacetFactory2.class, facetFactories[1]);
    }

    public void testFacetsFactoryNoop() {
        @Facets
        class Customer {
        }

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(FacetsFacet.class);
        assertNull(facet);
    }

}
