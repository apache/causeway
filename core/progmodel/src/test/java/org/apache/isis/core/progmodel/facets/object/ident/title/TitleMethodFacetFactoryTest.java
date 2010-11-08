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


package org.apache.isis.core.progmodel.facets.object.ident.title;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.runtimecontext.spec.IntrospectableSpecificationAbstract;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.progmodel.facets.object.ident.title.TitleFacet;
import org.apache.isis.core.progmodel.facets.object.ident.title.TitleFacetViaTitleMethod;
import org.apache.isis.core.progmodel.facets.object.ident.title.TitleFacetViaToStringMethod;
import org.apache.isis.core.progmodel.facets.object.ident.title.TitleMethodFacetFactory;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;


public class TitleMethodFacetFactoryTest extends AbstractFacetFactoryTest {

    private TitleMethodFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new TitleMethodFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @Override
    public void testFeatureTypes() {
        final ObjectFeatureType[] featureTypes = facetFactory.getFeatureTypes();
        assertTrue(contains(featureTypes, ObjectFeatureType.OBJECT));
        assertFalse(contains(featureTypes, ObjectFeatureType.PROPERTY));
        assertFalse(contains(featureTypes, ObjectFeatureType.COLLECTION));
        assertFalse(contains(featureTypes, ObjectFeatureType.ACTION));
        assertFalse(contains(featureTypes, ObjectFeatureType.ACTION_PARAMETER));
    }

    public void testTitleMethodPickedUpOnClassAndMethodRemoved() {
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
        class Customer {
            @SuppressWarnings("unused")
            public String title() {
                return "Some title";
            }
        }
        final Method titleMethod = findMethod(Customer.class, "title");

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(TitleFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TitleFacetViaTitleMethod);
        final TitleFacetViaTitleMethod titleFacetViaTitleMethod = (TitleFacetViaTitleMethod) facet;
        assertEquals(titleMethod, titleFacetViaTitleMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(titleMethod));
    }

    public void testToStringMethodPickedUpOnClassAndMethodRemoved() {
        class Customer {
            @Override
            public String toString() {
                return "Some title via toString";
            }
        }
        final Method toStringMethod = findMethod(Customer.class, "toString");

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(TitleFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TitleFacetViaToStringMethod);
        final TitleFacetViaToStringMethod titleFacetViaTitleMethod = (TitleFacetViaToStringMethod) facet;
        assertEquals(toStringMethod, titleFacetViaTitleMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemoveMethodMethodCalls().contains(toStringMethod));
    }

    /**
     * This change means that it will be ignored by {@link IntrospectableSpecificationAbstract#getFacet(Class)}
     * is the superclass has a none no-op implementation.
     */
    public void testTitleFacetMethodUsingToStringIsClassifiedAsANoop() {
        assertTrue(new TitleFacetViaToStringMethod(null, facetHolder).isNoop());
    }

    public void testNoExplicitTitleOrToStringMethod() {
        class Customer {}

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        assertNull(facetHolder.getFacet(TitleFacet.class));

        assertNoMethodsRemoved();
    }

}

