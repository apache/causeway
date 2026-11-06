/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.core.config.observation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationSpanNamingTest {
    @Test void fullIdentityAndFallbackArePreservedWhenTheyFit() {
        assertEquals("demo.Invoice#approve load", CausewayObservationNaming.forApplicationSpan("demo.Invoice#approve", "load"));
        assertEquals("app load", CausewayObservationNaming.forApplicationSpan(null, "load"));
        var exact = "x".repeat(45);
        assertEquals(exact + " load", CausewayObservationNaming.forApplicationSpan(exact, "load"));
    }

    @Test void namespaceIsRemovedBeforeIdentityIsTruncated() {
        assertEquals("Invoice#approve process", CausewayObservationNaming.forApplicationSpan(
                "a.very.long.logical.namespace.for.domain.Invoice#approve", "process"));
        assertEquals("A".repeat(42) + " process", CausewayObservationNaming.forApplicationSpan("A".repeat(80), "process"));
        assertEquals(50, CausewayObservationNaming.forApplicationSpan("A".repeat(80), "process").length());
    }

    @Test void everyAcceptedSuffixIsPreservedWithAndWithoutIdentity() {
        for (int length = 1; length <= 46; length++) {
            var suffix = "s".repeat(length);
            for (String id : new String[] { null, "namespace." + "Owner".repeat(20) + "#doWork" }) {
                var name = CausewayObservationNaming.forApplicationSpan(id, suffix);
                assertTrue(name.length() <= 50);
                assertTrue(name.endsWith(" " + suffix));
                if (length == 46) assertEquals(50, name.length());
            }
        }
    }

    @Test void invalidSuffixesAreRejected() {
        assertThrows(NullPointerException.class, () -> CausewayObservationNaming.forApplicationSpan(null, null));
        for (String invalid : new String[] { "", " \t\n", "x".repeat(47) }) {
            assertThrows(IllegalArgumentException.class, () -> CausewayObservationNaming.forApplicationSpan(null, invalid));
        }
    }
}
