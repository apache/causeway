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
package org.apache.causeway.applib.value;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.util.StringUtils;

import org.apache.causeway.applib.value.Listing.ListingHandler;
import org.apache.causeway.applib.value.Listing.MergePolicy;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.io.TextUtils;

class ListingTest {

    record Customer(
            String id,
            String name) {
        Customer(final String id, final String name) {
            assertTrue(StringUtils.hasLength(id));
            assertTrue(StringUtils.hasLength(name));
            this.id = id;
            this.name = name;
        }
        String stringify() {
            return String.format("[%s] %s", id, name);
        }
        static Customer destringify(final String input) {
            int i0 = input.indexOf('[');
            int i1 = input.indexOf(']');
            assertTrue(i0>-1);
            assertTrue(i1>i0);
            var cutter = TextUtils.cutter(input);
            var id = cutter.keepAfter("[")
                    .keepBefore("]")
                    .getValue();
            var name = cutter.keepAfter("]")
                    .getValue()
                    .trim();
            return new Customer(id, name);
        }
    }

    private final ListingHandler<Customer> listingHandler =
            new ListingHandler<>(Customer.class, Customer::stringify, Customer::destringify, Customer::id);
    private final Listing<Customer> listing =
            sampleListing(listingHandler);

    @Test
    void parsing() {
        var enabledCustomers = listing.streamEnabled().collect(Can.toCan());
        assertElementsMatch(Can.of(
                new Customer("a", "Jeff"),
                new Customer("b", "Jane")),
                enabledCustomers);

        var disabledCustomers = listing.streamDisabled().collect(Can.toCan());
        assertElementsMatch(Can.of(
                new Customer("c", "Henry")),
                disabledCustomers);
    }

    @Test
    void writing() {
        String expectedOutputAfterRoundtrip = """
                # this is a regular comment
                [a] Jeff
                #ERROR cannot parse ‹this is an invalid line› as Customer (org.opentest4j.AssertionFailedError: expected: <true> but was: <false>)
                #ERROR cannot parse ‹also an # invalid line› as Customer (org.opentest4j.AssertionFailedError: expected: <true> but was: <false>)
                #[c] Henry
                # the follwing is a blank line

                [b] Jane
                """;
        //debug
        //System.err.printf("%s%n", listing);
        assertTextLinesMatch(
                expectedOutputAfterRoundtrip,
                listing.toString());
    }

    @Test
    void merging() {
        String upd = """
                # this is a listing of updates we want to apply
                [a] Jeff
                [c] Henry
                [d] Martha
                """;
        var updateListing = listingHandler.parseListing(upd);
        var merged = listing.merge(MergePolicy.ADD_NEW_AS_DISABLED, updateListing);
        String expectedOutputAfterMerge = """
                # this is a regular comment
                [a] Jeff
                #ERROR cannot parse ‹this is an invalid line› as Customer (org.opentest4j.AssertionFailedError: expected: <true> but was: <false>)
                #ERROR cannot parse ‹also an # invalid line› as Customer (org.opentest4j.AssertionFailedError: expected: <true> but was: <false>)
                #[c] Henry
                # the follwing is a blank line

                #REMOVED [b] Jane

                #MERGED
                #[d] Martha""";
        //debug
        //System.err.printf("%s%n", merged);
        assertTextLinesMatch(
                expectedOutputAfterMerge,
                merged.toString());
    }

    // -- HELPER

    private Listing<Customer> sampleListing(final ListingHandler<Customer> listingHandler) {
        String input = """
                # this is a regular comment
                [a] Jeff
                this is an invalid line
                   also an # invalid line
                # ## [c] Henry
                # the follwing is a blank line

                [b] Jane
                """;
        return listingHandler.parseListing(input);
    }

    private static void assertTextLinesMatch(final String a, final String b) {
        assertLinesMatch(
                TextUtils.readLines(a).toList(),
                TextUtils.readLines(b).toList());
    }

    private static <T> void assertElementsMatch(final Can<T> a, final Can<T> b) {
        assertTextLinesMatch(
                a.join("\n"),
                b.join("\n"));
    }
}
