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
package org.apache.isis.core.metamodel.facets.ordering.memberorder;

import java.lang.reflect.Method;
import java.util.Collection;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.junit.Rule;

import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.CollectionLayout;
import org.apache.isis.applib.annotations.PropertyLayout;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.members.layout.order.LayoutOrderFacet;
import org.apache.isis.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromActionLayoutAnnotation;
import org.apache.isis.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromCollectionLayoutAnnotation;
import org.apache.isis.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromPropertyLayoutAnnotation;

import lombok.val;

public class LayoutOrderTest
extends AbstractFacetFactoryTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        context.checking(new Expectations() {{
            allowing(mockTranslationService).translate(with(any(TranslationContext.class)), with(any(String.class)));
            will(new Action() {
                @Override
                public Object invoke(final Invocation invocation) throws Throwable {
                    return invocation.getParameter(1);
                }

                @Override
                public void describeTo(final Description description) {
                    description.appendText("Returns parameter #1");
                }
            });
        }});
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMemberOrderAnnotationPickedUpOnProperty() {
        class Customer {
            @PropertyLayout(sequence = "1")
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        val facetFactory = super.createPropertyLayoutFacetFactory(metaModelContext);
        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, null, method, methodRemover, facetedMethod));

        val facet = facetedMethod.getFacet(LayoutOrderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof LayoutOrderFacetFromPropertyLayoutAnnotation);
        assertEquals("1", facet.getSequence());

        assertNoMethodsRemoved();
    }

    public void testMemberOrderAnnotationPickedUpOnCollection() {
        class Order {
        }
        class Customer {
            @CollectionLayout(sequence = "2")
            public Collection<Order> getOrders() {
                return null;
            }

            @SuppressWarnings("unused")
            public void addToOrders(final Order o) {
            }
        }
        final Method method = findMethod(Customer.class, "getOrders");

        val facetFactory = super.createCollectionLayoutFacetFactory(metaModelContext);
        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, null, method, methodRemover, facetedMethod));

        val facet = facetedMethod.getFacet(LayoutOrderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof LayoutOrderFacetFromCollectionLayoutAnnotation);
        assertEquals("2", facet.getSequence());

        assertNoMethodsRemoved();
    }

    public void testMemberOrderAnnotationPickedUpOnAction() {
        class Customer {
            @ActionLayout(sequence = "3")
            public void someAction() {
            }
        }
        final Method method = findMethod(Customer.class, "someAction");

        val facetFactory = super.createActionLayoutFacetFactory(metaModelContext);
        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, null, method, methodRemover, facetedMethod));

        val facet = facetedMethod.getFacet(LayoutOrderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof LayoutOrderFacetFromActionLayoutAnnotation);
        assertEquals("3", facet.getSequence());

        assertNoMethodsRemoved();
    }

}
