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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import lombok.RequiredArgsConstructor;

class MixedInMemberNamingStrategyTest {

    @RequiredArgsConstructor
    enum Scenario {
        SINGLE_UNDERSCORE("Customer_placeOrder", "placeOrder", "Place Order"),
        SINGLE_DOLLAR("Customer$placeOrder", "placeOrder", "Place Order"),
        //EXACTLY_UNDERSCORE("_", "_", "_"), //TODO this should throw instead
        ENDS_WITH_UNDERSCORE("abc_", "abc_", "Abc"),
        HAS_NO_UNDERSCORE("lock", "lock", "Lock"),
        CONTAINS_MORE_THAN_ONE_UNDERSCORE("ApplicationUser_default_lock", "lock", "Lock")
        ;

        final String mixinClassSimpleName;
        final String expectedMemberId;
        final String expectedFriendlyName;

        void verify() {
            assertThat(
                    _MixedInMemberNamingStrategy.mixinMemberId(mixinClassSimpleName),
                    is(expectedMemberId));

            assertThat(
                    _MixedInMemberNamingStrategy.mixinFriendlyName(mixinClassSimpleName),
                    is(expectedFriendlyName));
        }

        @Override
        public String toString() {
            return String.format("%s->%s (%s)", mixinClassSimpleName, expectedMemberId, name());
        }

    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void scenario(final Scenario scenario) {
        scenario.verify();
    }

}