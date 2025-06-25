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
package org.apache.causeway.core.metamodel.spec.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import lombok.RequiredArgsConstructor;

class MixedInMemberNamingStrategyTest {

    static class Customer_placeOrder {}
    static class abc_ {}
    static class lock {}
    static class ApplicationUser_default_lock {}
    static class Customer {
        class placeOrder {}
    }
    
    
    @RequiredArgsConstructor
    enum Scenario {
        SINGLE_UNDERSCORE(Customer_placeOrder.class, "placeOrder", "Place Order"),
        SINGLE_DOLLAR(Customer.placeOrder.class, "placeOrder", "Place Order"),
        //EXACTLY_UNDERSCORE("_", "_", "_"), //TODO this should throw instead
        ENDS_WITH_UNDERSCORE(abc_.class, "abc_", "Abc"),
        HAS_NO_UNDERSCORE(lock.class, "lock", "Lock"),
        CONTAINS_MORE_THAN_ONE_UNDERSCORE(ApplicationUser_default_lock.class, "lock", "Lock")
        ;

        final Class<?> mixinClass;
        final String expectedMemberId;
        final String expectedFriendlyName;

        void verify() {
            
            System.err.printf("%s%n", mixinClass.getCanonicalName());
            
            assertThat(
                    _MixedInMemberNamingStrategy.mixinMemberId(mixinClass),
                    is(expectedMemberId));

            assertThat(
                    _MixedInMemberNamingStrategy.mixinFriendlyName(mixinClass),
                    is(expectedFriendlyName));
        }

        @Override
        public String toString() {
            return String.format("%s->%s (%s)", mixinClass, expectedMemberId, name());
        }

    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void scenario(final Scenario scenario) {
        scenario.verify();
    }

}