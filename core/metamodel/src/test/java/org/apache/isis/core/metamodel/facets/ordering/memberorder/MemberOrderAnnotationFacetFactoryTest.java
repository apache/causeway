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

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.members.order.annotprop.MemberOrderFacetAnnotation;
import org.apache.isis.core.metamodel.facets.members.order.annotprop.MemberOrderFacetFactory;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

public class MemberOrderAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private MemberOrderFacetFactory facetFactory;

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        context.checking(new Expectations() {{
            allowing(mockTranslationService).translate(with(any(String.class)), with(any(String.class)));
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


        facetFactory = new MemberOrderFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testMemberOrderAnnotationPickedUpOnProperty() {
        class Customer {
            @MemberOrder(sequence = "1")
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MemberOrderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MemberOrderFacetAnnotation);
        final MemberOrderFacetAnnotation memberOrderFacetAnnotation = (MemberOrderFacetAnnotation) facet;
        assertEquals("1", memberOrderFacetAnnotation.sequence());

        assertNoMethodsRemoved();
    }

    public void testMemberOrderAnnotationPickedUpOnCollection() {
        class Order {
        }
        class Customer {
            @MemberOrder(sequence = "2")
            public Collection<Order> getOrders() {
                return null;
            }

            @SuppressWarnings("unused")
            public void addToOrders(final Order o) {
            }
        }
        final Method method = findMethod(Customer.class, "getOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MemberOrderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MemberOrderFacetAnnotation);
        final MemberOrderFacetAnnotation memberOrderFacetAnnotation = (MemberOrderFacetAnnotation) facet;
        assertEquals("2", memberOrderFacetAnnotation.sequence());

        assertNoMethodsRemoved();
    }

    public void testMemberOrderAnnotationPickedUpOnAction() {
        class Customer {
            @MemberOrder(sequence = "3")
            public void someAction() {
            }
        }
        final Method method = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MemberOrderFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MemberOrderFacetAnnotation);
        final MemberOrderFacetAnnotation memberOrderFacetAnnotation = (MemberOrderFacetAnnotation) facet;
        assertEquals("3", memberOrderFacetAnnotation.sequence());

        assertNoMethodsRemoved();
    }

}
