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
package org.apache.causeway.core.metamodel.facets.ordering.memberorder;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.members.layout.group.GroupIdAndName;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacetAbstract;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacetAbstract;
import org.apache.causeway.core.metamodel.layout.DeweyOrderSet;

class DeweyOrderSetTest {

    static class Customer {
        private String lastName;
        private String firstName;
        private String houseNumber;
        private String streetName;
        private String postalTown;
        public String getLastName() { return lastName; }
        public String getFirstName() { return firstName; }
        public String getHouseNumber() { return houseNumber; }
        public String getStreetName() { return streetName; }
        public String getPostalTown() { return postalTown; }
    }

    private final MetaModelContext mmc = MetaModelContext_forTesting.buildDefault();
    private final FacetedMethod lastNameMember = FacetedMethod.testing.createGetterForProperty(mmc, Customer.class, "Last Name");
    private final FacetedMethod firstNameMember = FacetedMethod.testing.createGetterForProperty(mmc, Customer.class, "First Name");
    private final FacetedMethod houseNumberMember = FacetedMethod.testing.createGetterForProperty(mmc, Customer.class, "House Number");
    private final FacetedMethod streetNameMember = FacetedMethod.testing.createGetterForProperty(mmc, Customer.class, "Street Name");
    private final FacetedMethod postalTownMember = FacetedMethod.testing.createGetterForProperty(mmc, Customer.class, "Postal Town");
    private final List<FacetedMethod> lastNameAndFirstName = List.of(lastNameMember, firstNameMember);
    private final List<FacetedMethod> nameAndAddressMembers = List.of(lastNameMember, firstNameMember, houseNumberMember, streetNameMember, postalTownMember);
    private final List<FacetedMethod> lastNameFirstNameAndPostalTown = List.of(lastNameMember, firstNameMember, postalTownMember);

    @Mock TranslationService mockTranslationService;

	static TranslationContext ctx = TranslationContext.named("test");

    @BeforeEach
    protected void setUp() {
        _Context.clear();
    }

    @Test
    void defaultGroup() {
        setupLayoutFacets("", "1", lastNameMember);
        setupLayoutFacets("", "2", firstNameMember);

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName.stream());
        assertEquals("", orderSet.getGroupName());
        assertEquals("", orderSet.getGroupFullName());
        assertEquals("", orderSet.getGroupPath());
    }

    @Test
    void defaultGroupSize() {
        setupLayoutFacets("", "1", lastNameMember);
        setupLayoutFacets("", "2", firstNameMember);

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName.stream());
        assertEquals(2, orderSet.size());
        assertEquals(2, orderSet.elementList().size());
        assertEquals(0, orderSet.children().size());
    }

    @Test
    void defaultGroupTwoMembersSorted() {
        setupLayoutFacets("", "1", lastNameMember);
        setupLayoutFacets("", "2", firstNameMember);

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName.stream());
        assertEquals(lastNameMember, orderSet.elementList().get(0));
        assertEquals(firstNameMember, orderSet.elementList().get(1));
    }

    @Test
    void twoMembersAtDefaultGroupOtherWay() {
        setupLayoutFacets("", "2", lastNameMember);
        setupLayoutFacets("", "1", firstNameMember);

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName.stream());
        assertEquals(firstNameMember, orderSet.elementList().get(0));
        assertEquals(lastNameMember, orderSet.elementList().get(1));
    }

    @Test
    void withChildGroupDefaultGroupName() {
        setupLayoutFacets("", "1", lastNameMember);
        setupLayoutFacets("", "2", firstNameMember);
        setupLayoutFacets("address", "1", houseNumberMember);
        setupLayoutFacets("address", "2", streetNameMember);
        setupLayoutFacets("address", "3", postalTownMember);

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers.stream());
        assertEquals("", orderSet.getGroupName());
        assertEquals("", orderSet.getGroupFullName());
        assertEquals("", orderSet.getGroupPath());
    }

    @Test
    void withChildGroupSize() {
        setupLayoutFacets("", "1", lastNameMember);
        setupLayoutFacets("", "2", firstNameMember);
        setupLayoutFacets("address", "1", houseNumberMember);
        setupLayoutFacets("address", "2", streetNameMember);
        setupLayoutFacets("address", "3", postalTownMember);

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers.stream());
        assertEquals(1, orderSet.children().size());
        assertEquals(3, orderSet.size());
    }

    @Test
    void withChildGroupChildsGroupName() {
        setupLayoutFacets("", "1", lastNameMember);
        setupLayoutFacets("", "2", firstNameMember);
        setupLayoutFacets("address", "1", houseNumberMember);
        setupLayoutFacets("address", "2", streetNameMember);
        setupLayoutFacets("address", "3", postalTownMember);

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers.stream());
        final List<?> children = orderSet.children();
        final DeweyOrderSet childOrderSet = (DeweyOrderSet) children.get(0);
        assertEquals("Address", childOrderSet.getGroupName());
        assertEquals("address", childOrderSet.getGroupFullName());
        assertEquals("", childOrderSet.getGroupPath());
    }

    @Test
    void withChildGroupChildsGroupSize() {
        setupLayoutFacets("", "1", lastNameMember);
        setupLayoutFacets("", "2", firstNameMember);
        setupLayoutFacets("address", "1", houseNumberMember);
        setupLayoutFacets("address", "2", streetNameMember);
        setupLayoutFacets("address", "3", postalTownMember);

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers.stream());
        final DeweyOrderSet childOrderSet = orderSet.children().get(0);
        assertEquals(3, childOrderSet.size());
        assertEquals(0, childOrderSet.children().size());
    }

    @Test
    void withChildGroupChildsGroupElementOrdering() {
        setupLayoutFacets("", "1", lastNameMember);
        setupLayoutFacets("", "2", firstNameMember);
        setupLayoutFacets("address", "6", houseNumberMember);
        setupLayoutFacets("address", "5", streetNameMember);
        setupLayoutFacets("address", "4", postalTownMember);

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers.stream());
        final DeweyOrderSet childOrderSet = orderSet.children().get(0);
        assertEquals(postalTownMember, childOrderSet.elementList().get(0));
        assertEquals(streetNameMember, childOrderSet.elementList().get(1));
        assertEquals(houseNumberMember, childOrderSet.elementList().get(2));
    }

    @Test
    void withChildGroupOrderedAtEnd() {
        setupLayoutFacets("address", "6", houseNumberMember);
        setupLayoutFacets("address", "5", streetNameMember);
        setupLayoutFacets("address", "4", postalTownMember);
        setupLayoutFacets("", "3", lastNameMember);
        setupLayoutFacets("", "2", firstNameMember);

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(nameAndAddressMembers.stream());
        assertEquals(firstNameMember, orderSet.elementList().get(0));
        assertEquals(lastNameMember, orderSet.elementList().get(1));
        assertTrue(orderSet.elementList().get(2) instanceof DeweyOrderSet);
    }

    @Test
    void defaultGroupNeitherAnnotatedSize() {
        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName.stream());
        assertEquals(2, orderSet.elementList().size());
    }

    @Test
    void defaultGroupNeitherAnnotatedOrderedByName() {
        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameAndFirstName.stream());
        assertEquals(firstNameMember, orderSet.elementList().get(0));
        assertEquals(lastNameMember, orderSet.elementList().get(1));
    }

    @Test
    void defaultGroupMixOfAnnotatedAndNotSize() {
        setupLayoutFacets("", "1", lastNameMember);
        setupLayoutFacets("address", "2", postalTownMember);

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameFirstNameAndPostalTown.stream());
        assertEquals(3, orderSet.elementList().size());
    }

    @Test
    void defaultGroupMixOfAnnotatedAndNotOrderedWithAnnotatedFirst() {
        setupLayoutFacets("", "1", lastNameMember);
        setupLayoutFacets("", "2", postalTownMember);

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(lastNameFirstNameAndPostalTown.stream());

        assertEquals(lastNameMember, orderSet.elementList().get(0));
        assertEquals(postalTownMember, orderSet.elementList().get(1));
        assertEquals(firstNameMember, orderSet.elementList().get(2));
    }

    // -- HELPER

    void setupLayoutFacets(final String groupId, final String sequence, final FacetHolder facetedHolder) {
        facetedHolder.addFacet(new LayoutGroupFacetAbstract(GroupIdAndName.of(groupId, ""), facetedHolder) {});
        facetedHolder.addFacet(new LayoutOrderFacetAbstract(sequence, facetedHolder) {});
    }

}
