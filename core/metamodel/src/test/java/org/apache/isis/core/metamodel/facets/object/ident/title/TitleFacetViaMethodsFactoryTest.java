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

package org.apache.isis.core.metamodel.facets.object.ident.title;

import java.lang.reflect.Method;
import java.util.Optional;

import org.jmock.Expectations;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.title.methods.TitleFacetViaMethodsFactory;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.object.title.methods.TitleFacetViaTitleMethod;
import org.apache.isis.core.metamodel.facets.object.title.methods.TitleFacetViaToStringMethod;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

public class TitleFacetViaMethodsFactoryTest extends AbstractFacetFactoryTest {

    private JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    private ServicesInjector mockServicesInjector;
    private TranslationService mockTranslationService;

    private TitleFacetViaMethodsFactory facetFactory;

    public void setUp() throws Exception {
        super.setUp();
        mockServicesInjector = context.mock(ServicesInjector.class);
        mockTranslationService = context.mock(TranslationService.class);

        context.checking(new Expectations() {{
            allowing(mockServicesInjector).lookupService(TranslationService.class);
            will(returnValue(Optional.of(mockTranslationService)));
        }});

        facetFactory = new TitleFacetViaMethodsFactory();

        facetFactory.setServicesInjector(mockServicesInjector);
    }


    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testTitleMethodPickedUpOnClassAndMethodRemoved() {
        class Customer {
            @SuppressWarnings("unused")
            public String title() {
                return "Some title";
            }
        }
        final Method titleMethod = findMethod(Customer.class, "title");

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TitleFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TitleFacetViaTitleMethod);
        final TitleFacetViaTitleMethod titleFacetViaTitleMethod = (TitleFacetViaTitleMethod) facet;
        assertEquals(titleMethod, titleFacetViaTitleMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(titleMethod));
    }

    public void testToStringMethodPickedUpOnClassAndMethodRemoved() {
        class Customer {
            @Override
            public String toString() {
                return "Some title via toString";
            }
        }
        final Method toStringMethod = findMethod(Customer.class, "toString");

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TitleFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TitleFacetViaToStringMethod);
        final TitleFacetViaToStringMethod titleFacetViaTitleMethod = (TitleFacetViaToStringMethod) facet;
        assertEquals(toStringMethod, titleFacetViaTitleMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(toStringMethod));
    }

    /**
     * This change means that it will be ignored by
     * {@link ObjectSpecificationAbstract#getFacet(Class)} is the superclass has
     * a none no-op implementation.
     */
    public void testTitleFacetMethodUsingToStringIsClassifiedAsANoop() {
        assertTrue(new TitleFacetViaToStringMethod(null, facetedMethod).isNoop());
    }

    public void testNoExplicitTitleOrToStringMethod() {
        class Customer {
        }

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(TitleFacet.class));

        assertNoMethodsRemoved();
    }

}
