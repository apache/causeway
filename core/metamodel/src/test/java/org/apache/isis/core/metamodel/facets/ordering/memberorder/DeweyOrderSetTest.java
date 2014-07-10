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

import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.google.common.collect.ImmutableList;

import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.layout.DeweyOrderSet;
import org.apache.isis.core.metamodel.facets.members.order.annotprop.MemberOrderFacetAnnotation;

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
    private final List<IdentifiedHolder> lastNameAndFirstName = ImmutableList.of(lastNameMember, firstNameMember);
    private final List<IdentifiedHolder> nameAndAddressMembers = ImmutableList.of(lastNameMember, firstNameMember, houseNumberMember, streetNameMember, postalTownMember);
    private final List<IdentifiedHolder> lastNameFirstNameAndPostalTown = ImmutableList.of(lastNameMember, firstNameMember, postalTownMember);

    @Override
    protected void setUp() {

    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testDefaultGroup() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", firstNameMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName);
        assertEquals("", orderSet.getGroupName());
        assertEquals("", orderSet.getGroupFullName());
        assertEquals("", orderSet.getGroupPath());
    }

    public void testDefaultGroupSize() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", firstNameMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName);
        assertEquals(2, orderSet.size());
        assertEquals(2, orderSet.elementList().size());
        assertEquals(0, orderSet.children().size());
    }

    public void testDefaultGroupTwoMembersSorted() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", firstNameMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName);
        assertEquals(lastNameMember, orderSet.elementList().get(0));
        assertEquals(firstNameMember, orderSet.elementList().get(1));
    }

    public void testTwoMembersAtDefaultGroupOtherWay() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", firstNameMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName);
        assertEquals(firstNameMember, orderSet.elementList().get(0));
        assertEquals(lastNameMember, orderSet.elementList().get(1));
    }

    public void testWithChildGroupDefaultGroupName() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", firstNameMember));
        houseNumberMember.addFacet(new MemberOrderFacetAnnotation("address", "1", houseNumberMember));
        streetNameMember.addFacet(new MemberOrderFacetAnnotation("address", "2", streetNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("address", "3", postalTownMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers);
        assertEquals("", orderSet.getGroupName());
        assertEquals("", orderSet.getGroupFullName());
        assertEquals("", orderSet.getGroupPath());
    }

    public void testWithChildGroupSize() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", firstNameMember));
        houseNumberMember.addFacet(new MemberOrderFacetAnnotation("address", "1", houseNumberMember));
        streetNameMember.addFacet(new MemberOrderFacetAnnotation("address", "2", streetNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("address", "3", postalTownMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers);
        assertEquals(1, orderSet.children().size());
        assertEquals(3, orderSet.size());
    }

    public void testWithChildGroupChildsGroupName() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", firstNameMember));
        houseNumberMember.addFacet(new MemberOrderFacetAnnotation("address", "1", houseNumberMember));
        streetNameMember.addFacet(new MemberOrderFacetAnnotation("address", "2", streetNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("address", "3", postalTownMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers);
        final List<?> children = orderSet.children();
        final DeweyOrderSet childOrderSet = (DeweyOrderSet) children.get(0);
        assertEquals("Address", childOrderSet.getGroupName());
        assertEquals("address", childOrderSet.getGroupFullName());
        assertEquals("", childOrderSet.getGroupPath());
    }

    public void testWithChildGroupChildsGroupSize() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", firstNameMember));
        houseNumberMember.addFacet(new MemberOrderFacetAnnotation("address", "1", houseNumberMember));
        streetNameMember.addFacet(new MemberOrderFacetAnnotation("address", "2", streetNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("address", "3", postalTownMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers);
        final DeweyOrderSet childOrderSet = orderSet.children().get(0);
        assertEquals(3, childOrderSet.size());
        assertEquals(0, childOrderSet.children().size());
    }

    public void testWithChildGroupChildsGroupElementOrdering() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", firstNameMember));
        houseNumberMember.addFacet(new MemberOrderFacetAnnotation("address", "6", houseNumberMember));
        streetNameMember.addFacet(new MemberOrderFacetAnnotation("address", "5", streetNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("address", "4", postalTownMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers);
        final DeweyOrderSet childOrderSet = orderSet.children().get(0);
        assertEquals(postalTownMember, childOrderSet.elementList().get(0));
        assertEquals(streetNameMember, childOrderSet.elementList().get(1));
        assertEquals(houseNumberMember, childOrderSet.elementList().get(2));
    }

    public void testWithChildGroupOrderedAtEnd() {
        houseNumberMember.addFacet(new MemberOrderFacetAnnotation("address", "6", houseNumberMember));
        streetNameMember.addFacet(new MemberOrderFacetAnnotation("address", "5", streetNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("address", "4", postalTownMember));
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "3", lastNameMember));
        firstNameMember.addFacet(new MemberOrderFacetAnnotation("", "2", firstNameMember));

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
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", lastNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("address", "2", postalTownMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameFirstNameAndPostalTown);
        assertEquals(3, orderSet.elementList().size());
    }

    public void testDefaultGroupMixOfAnnotatedAndNotOrderedWithAnnotatedFirst() {
        lastNameMember.addFacet(new MemberOrderFacetAnnotation("", "1", lastNameMember));
        postalTownMember.addFacet(new MemberOrderFacetAnnotation("", "2", postalTownMember));

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameFirstNameAndPostalTown);

        assertEquals(lastNameMember, orderSet.elementList().get(0));
        assertEquals(postalTownMember, orderSet.elementList().get(1));
        assertEquals(firstNameMember, orderSet.elementList().get(2));
    }

}
