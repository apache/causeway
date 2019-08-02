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

package org.apache.isis.metamodel.facets.ordering.memberorder;

import java.util.List;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.junit.Rule;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.members.order.annotprop.MemberOrderFacetAnnotation;
import org.apache.isis.metamodel.layout.DeweyOrderSet;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DeweyOrderSetTest extends TestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(new TestSuite(DeweyOrderSetTest.class));
    }

    public static class Customer {
        private String lastName;
        private String firstName;
        private String houseNumber;
        private String streetName;
        private String postalTown;

        public String getLastName() {
            return lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getHouseNumber() {
            return houseNumber;
        }

        public String getStreetName() {
            return streetName;
        }

        public String getPostalTown() {
            return postalTown;
        }
    }

    private final IdentifiedHolder lastNameMember = FacetedMethod.createForProperty(Customer.class, "Last Name");
    private final IdentifiedHolder firstNameMember = FacetedMethod.createForProperty(Customer.class, "First Name");
    private final IdentifiedHolder houseNumberMember = FacetedMethod.createForProperty(Customer.class, "House Number");
    private final IdentifiedHolder streetNameMember = FacetedMethod.createForProperty(Customer.class, "Street Name");
    private final IdentifiedHolder postalTownMember = FacetedMethod.createForProperty(Customer.class, "Postal Town");
    private final List<IdentifiedHolder> lastNameAndFirstName = _Lists.of(lastNameMember, firstNameMember);
    private final List<IdentifiedHolder> nameAndAddressMembers = _Lists.of(lastNameMember, firstNameMember, houseNumberMember, streetNameMember, postalTownMember);
    private final List<IdentifiedHolder> lastNameFirstNameAndPostalTown = _Lists.of(lastNameMember, firstNameMember, postalTownMember);

    TranslationService mockTranslationService;

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Override
    protected void setUp() {
        
        _Context.clear();
        
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

    public void testDefaultGroup() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", mockTranslationService, firstNameMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName);
        assertEquals("", orderSet.getGroupName());
        assertEquals("", orderSet.getGroupFullName());
        assertEquals("", orderSet.getGroupPath());
    }

    public void testDefaultGroupSize() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", mockTranslationService, firstNameMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName);
        assertEquals(2, orderSet.size());
        assertEquals(2, orderSet.elementList().size());
        assertEquals(0, orderSet.children().size());
    }

    public void testDefaultGroupTwoMembersSorted() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", mockTranslationService, firstNameMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName);
        assertEquals(lastNameMember, orderSet.elementList().get(0));
        assertEquals(firstNameMember, orderSet.elementList().get(1));
    }

    public void testTwoMembersAtDefaultGroupOtherWay() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", mockTranslationService, lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, firstNameMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName);
        assertEquals(firstNameMember, orderSet.elementList().get(0));
        assertEquals(lastNameMember, orderSet.elementList().get(1));
    }

    public void testWithChildGroupDefaultGroupName() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", mockTranslationService, firstNameMember));
        houseNumberMember.addFacet(new MemberOrderFacetAnnotation("address", "1", mockTranslationService, houseNumberMember));
        streetNameMember.addFacet(new MemberOrderFacetAnnotation("address", "2", mockTranslationService, streetNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("address", "3", mockTranslationService, postalTownMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers);
        assertEquals("", orderSet.getGroupName());
        assertEquals("", orderSet.getGroupFullName());
        assertEquals("", orderSet.getGroupPath());
    }

    public void testWithChildGroupSize() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", mockTranslationService, firstNameMember));
        houseNumberMember.addFacet(new MemberOrderFacetAnnotation("address", "1", mockTranslationService, houseNumberMember));
        streetNameMember.addFacet(new MemberOrderFacetAnnotation("address", "2", mockTranslationService, streetNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("address", "3", mockTranslationService, postalTownMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers);
        assertEquals(1, orderSet.children().size());
        assertEquals(3, orderSet.size());
    }

    public void testWithChildGroupChildsGroupName() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", mockTranslationService, firstNameMember));
        houseNumberMember.addFacet(new MemberOrderFacetAnnotation("address", "1", mockTranslationService, houseNumberMember));
        streetNameMember.addFacet(new MemberOrderFacetAnnotation("address", "2", mockTranslationService, streetNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("address", "3", mockTranslationService, postalTownMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers);
        final List<?> children = orderSet.children();
        final DeweyOrderSet childOrderSet = (DeweyOrderSet) children.get(0);
        assertEquals("Address", childOrderSet.getGroupName());
        assertEquals("address", childOrderSet.getGroupFullName());
        assertEquals("", childOrderSet.getGroupPath());
    }

    public void testWithChildGroupChildsGroupSize() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", mockTranslationService, firstNameMember));
        houseNumberMember.addFacet(new MemberOrderFacetAnnotation("address", "1", mockTranslationService, houseNumberMember));
        streetNameMember.addFacet(new MemberOrderFacetAnnotation("address", "2", mockTranslationService, streetNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("address", "3", mockTranslationService, postalTownMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers);
        final DeweyOrderSet childOrderSet = orderSet.children().get(0);
        assertEquals(3, childOrderSet.size());
        assertEquals(0, childOrderSet.children().size());
    }

    public void testWithChildGroupChildsGroupElementOrdering() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", mockTranslationService, firstNameMember));
        houseNumberMember.addFacet(new MemberOrderFacetAnnotation("address", "6", mockTranslationService, houseNumberMember));
        streetNameMember.addFacet(new MemberOrderFacetAnnotation("address", "5", mockTranslationService, streetNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("address", "4", mockTranslationService, postalTownMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers);
        final DeweyOrderSet childOrderSet = orderSet.children().get(0);
        assertEquals(postalTownMember, childOrderSet.elementList().get(0));
        assertEquals(streetNameMember, childOrderSet.elementList().get(1));
        assertEquals(houseNumberMember, childOrderSet.elementList().get(2));
    }

    public void testWithChildGroupOrderedAtEnd() {
        houseNumberMember.addFacet(new MemberOrderFacetAnnotation("address", "6", mockTranslationService, houseNumberMember));
        streetNameMember.addFacet(new MemberOrderFacetAnnotation("address", "5", mockTranslationService, streetNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("address", "4", mockTranslationService, postalTownMember));
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "3", mockTranslationService, lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", mockTranslationService, firstNameMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers);
        assertEquals(firstNameMember, orderSet.elementList().get(0));
        assertEquals(lastNameMember, orderSet.elementList().get(1));
        assertTrue(orderSet.elementList().get(2) instanceof DeweyOrderSet);
    }

    public void testDefaultGroupNeitherAnnotatedSize() {
        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName);
        assertEquals(2, orderSet.elementList().size());
    }

    public void testDefaultGroupNeitherAnnotatedOrderedByName() {
        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName);
        assertEquals(firstNameMember, orderSet.elementList().get(0));
        assertEquals(lastNameMember, orderSet.elementList().get(1));
    }

    public void testDefaultGroupMixOfAnnotatedAndNotSize() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, lastNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("address", "2", mockTranslationService, postalTownMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameFirstNameAndPostalTown);
        assertEquals(3, orderSet.elementList().size());
    }

    public void testDefaultGroupMixOfAnnotatedAndNotOrderedWithAnnotatedFirst() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", mockTranslationService, lastNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("", "2", mockTranslationService, postalTownMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameFirstNameAndPostalTown);

        assertEquals(lastNameMember, orderSet.elementList().get(0));
        assertEquals(postalTownMember, orderSet.elementList().get(1));
        assertEquals(firstNameMember, orderSet.elementList().get(2));
    }

}
