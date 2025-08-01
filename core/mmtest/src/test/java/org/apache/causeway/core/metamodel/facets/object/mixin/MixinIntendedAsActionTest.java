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
package org.apache.causeway.core.metamodel.facets.object.mixin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Property;

import lombok.Data;
import lombok.RequiredArgsConstructor;

class MixinIntendedAsActionTest extends MixinIntendedAs {

    @BeforeEach
    void beforeEach() throws Exception {
        super.setUp();
    }

    @AfterEach
    void afterEach() throws Exception {
        super.tearDown();
    }

    // the non holder
    @DomainObject
    static class NoCustomer {

    }

    // the (shared) holder
    @DomainObject @Data
    static class Customer {
        String name;
    }

    // ------------------------------
    // -- classic mix-in declaration
    // ------------------------------

    @Action @RequiredArgsConstructor
    static class Customer_mixin {

        private final Customer holder;

        public void act(final String newName) { holder.setName(newName); }
    }

    @Test
    void classicMixin_shouldHaveProperFacet() {

        var mixinFacet = super.runTypeContextOn(Customer_mixin.class)
                .getFacet(MixinFacet.class);

        // proper predicates
        assertNotNull(mixinFacet);
        assertTrue(mixinFacet.isMixinFor(Customer.class));
        assertFalse(mixinFacet.isMixinFor(NoCustomer.class));

        // proper instantiation
        var holderPojo = new Customer();
        var mixinPojo = mixinFacet.instantiate(holderPojo);
        ((Customer_mixin)mixinPojo).act("hello");
        assertEquals("hello", holderPojo.getName());

    }

    // ------------------------------------------
    // -- advanced mix-in declaration ... @Action
    // ------------------------------------------

    @Action @RequiredArgsConstructor
    static class Customer_action {

        private final Customer holder;

        public void $$(final String newName) { holder.setName(newName); }
    }

    @Test
    void actionMixin_shouldHaveProperFacet() {

        var mixinFacet = super.runTypeContextOn(Customer_action.class)
                .getFacet(MixinFacet.class);

        // proper predicates
        assertNotNull(mixinFacet);
        assertTrue(mixinFacet.isMixinFor(Customer.class));
        assertFalse(mixinFacet.isMixinFor(NoCustomer.class));

        // proper instantiation
        var holderPojo = new Customer();
        var mixinPojo = mixinFacet.instantiate(holderPojo);
        ((Customer_action)mixinPojo).$$("hello");
        assertEquals("hello", holderPojo.getName());
    }

    // ------------------------------------------
    // -- advanced mix-in declaration ... @Property
    // ------------------------------------------

    @Property @RequiredArgsConstructor
    static class Customer_property {

        private final Customer holder;

        public void $$(final String newName) { holder.setName(newName); }
    }

    @Test
    void propertyMixin_shouldHaveProperFacet() {

        var mixinFacet = super.runTypeContextOn(Customer_property.class)
                .getFacet(MixinFacet.class);

        // proper predicates
        assertNotNull(mixinFacet);
        assertTrue(mixinFacet.isMixinFor(Customer.class));
        assertFalse(mixinFacet.isMixinFor(NoCustomer.class));

    }

    // ------------------------------------------
    // -- advanced mix-in declaration ... @Property
    // ------------------------------------------

    @Collection @RequiredArgsConstructor
    static class Customer_collection {

        private final Customer holder;

        public void $$(final String newName) { holder.setName(newName); }
    }

    @Test
    void collectionMixin_shouldHaveProperFacet() {

        var mixinFacet = super.runTypeContextOn(Customer_collection.class)
                .getFacet(MixinFacet.class);

        // proper predicates
        assertNotNull(mixinFacet);
        assertTrue(mixinFacet.isMixinFor(Customer.class));
        assertFalse(mixinFacet.isMixinFor(NoCustomer.class));

    }

}
