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

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.junit.Rule;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.order.annotprop.MemberOrderFacetAnnotation;
import org.apache.isis.core.metamodel.layout.memberorderfacet.MemberOrderComparator;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MemberOrderComparatorTest extends TestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(new TestSuite(MemberOrderComparatorTest.class));
    }

    private MemberOrderComparator comparator, laxComparator;

    public static class Customer {
        private String abc;

        public String getAbc() {
            return abc;
        }
    }

    private final FacetedMethod m1 = FacetedMethod.createForProperty(Customer.class, "abc");
    private final FacetedMethod m2 = FacetedMethod.createForProperty(Customer.class, "abc");

    TranslationService mockTranslationService;

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Override
    protected void setUp() {

        _Context.clear();

        comparator = new MemberOrderComparator(true);
        laxComparator = new MemberOrderComparator(false);

        mockTranslationService = context.mock(TranslationService.class);
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

    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testDefaultGroupOneComponent() {
        m1.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, m1));
        m2.addFacet(new MemberOrderFacetAnnotation("", "2", mockTranslationService, m2));
        assertEquals(-1, comparator.compare(m1, m2));
    }

    public void testDefaultGroupOneComponentOtherWay() {
        m1.addFacet(new MemberOrderFacetAnnotation("", "2", mockTranslationService, m1));
        m2.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, m2));
        assertEquals(+1, comparator.compare(m1, m2));
    }

    public void testDefaultGroupOneComponentSame() {
        m1.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, m1));
        m2.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, m2));
        assertEquals(0, comparator.compare(m1, m2));
    }

    public void testDefaultGroupOneSideRunsOutOfComponentsFirst() {
        m1.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, m1));
        m2.addFacet(new MemberOrderFacetAnnotation("", "1.1", mockTranslationService, m2));
        assertEquals(-1, comparator.compare(m1, m2));
    }

    public void testDefaultGroupOneSideRunsOutOfComponentsFirstOtherWay() {
        m1.addFacet(new MemberOrderFacetAnnotation("", "1.1", mockTranslationService, m1));
        m2.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, m2));
        assertEquals(+1, comparator.compare(m1, m2));
    }

    public void testDefaultGroupOneSideRunsTwoComponents() {
        m1.addFacet(new MemberOrderFacetAnnotation("", "1.1", mockTranslationService, m1));
        m2.addFacet(new MemberOrderFacetAnnotation("", "1.2", mockTranslationService, m2));
        assertEquals(-1, comparator.compare(m1, m2));
    }

    public void testDefaultGroupOneSideRunsTwoComponentsOtherWay() {
        m1.addFacet(new MemberOrderFacetAnnotation("", "1.2", mockTranslationService, m1));
        m2.addFacet(new MemberOrderFacetAnnotation("", "1.1", mockTranslationService, m2));
        assertEquals(+1, comparator.compare(m1, m2));
    }

    public void testDefaultGroupOneSideRunsLotsOfComponents() {
        m1.addFacet(new MemberOrderFacetAnnotation("", "1.2.5.8.3.3", mockTranslationService, m1));
        m2.addFacet(new MemberOrderFacetAnnotation("", "1.2.5.8.3.4", mockTranslationService, m2));
        assertEquals(-1, comparator.compare(m1, m2));
    }

    public void testDefaultGroupOneSideRunsLotsOfComponentsOtherWay() {
        m1.addFacet(new MemberOrderFacetAnnotation("", "1.2.5.8.3.4", mockTranslationService, m1));
        m2.addFacet(new MemberOrderFacetAnnotation("", "1.2.5.8.3.3", mockTranslationService, m2));
        assertEquals(+1, comparator.compare(m1, m2));
    }

    public void testDefaultGroupOneSideRunsLotsOfComponentsSame() {
        m1.addFacet(new MemberOrderFacetAnnotation("", "1.2.5.8.3.3", mockTranslationService, m1));
        m2.addFacet(new MemberOrderFacetAnnotation("", "1.2.5.8.3.3", mockTranslationService, m2));
        assertEquals(0, comparator.compare(m1, m2));
    }

    public void testNamedGroupOneSideRunsLotsOfComponents() {
        m1.addFacet(new MemberOrderFacetAnnotation("abc", "1.2.5.8.3.3", mockTranslationService, m1));
        m2.addFacet(new MemberOrderFacetAnnotation("abc", "1.2.5.8.3.4", mockTranslationService, m2));
        assertEquals(-1, comparator.compare(m1, m2));
    }

    public void testEnsuresInSameGroup() {
        m1.addFacet(new MemberOrderFacetAnnotation("abc", "1", mockTranslationService, m1));
        m2.addFacet(new MemberOrderFacetAnnotation("def", "2", mockTranslationService, m2));
        try {
            assertEquals(-1, comparator.compare(m1, m2));
            fail("Exception should have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

    public void testEnsuresInSameGroupCanBeDisabled() {
        m1.addFacet(new MemberOrderFacetAnnotation("abc", "1", mockTranslationService, m1));
        m2.addFacet(new MemberOrderFacetAnnotation("def", "2", mockTranslationService, m2));
        assertEquals(-1, laxComparator.compare(m1, m2));
    }

    public void testNonAnnotatedAfterAnnotated() {
        // don't annotate m1
        m2.addFacet(new MemberOrderFacetAnnotation("def", "2", mockTranslationService, m2));
        assertEquals(+1, comparator.compare(m1, m2));
    }

}
