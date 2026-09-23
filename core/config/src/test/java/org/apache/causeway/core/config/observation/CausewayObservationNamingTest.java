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
package org.apache.causeway.core.config.observation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CausewayObservationNamingTest {

    @Test
    void logicalTypeNamePreservesNamespaceAndCaseWhenItFits() {
        assertEquals("render isisExtSecMan.ApplicationUser",
                CausewayObservationNaming.forType(
                        "render", "isisExtSecMan.ApplicationUser"));
    }

    @Test
    void logicalTypeNameFallsBackToSimpleTypeBeforeTruncating() {
        assertEquals("render ApplicationUser",
                CausewayObservationNaming.forType(
                        "render",
                        "aVeryLongApplicationNamespaceThatExceedsTheLimit.ApplicationUser"));
    }

    @Test
    void logicalMemberIdentifierPreservesNamespaceAndCaseWhenItFits() {
        assertEquals("act demo.Customer#updateName",
                CausewayObservationNaming.forLogicalMember(
                        "act", "demo.Customer#updateName"));
        assertEquals("prompt demo.Customer#createURL",
                CausewayObservationNaming.forMember(
                        "prompt", "demo.Customer", "createURL"));
    }

    @Test
    void logicalMemberIdentifierFallsBackToSimpleTypeBeforeTruncating() {
        assertEquals("act ApplicationUser#updateEmailAddress",
                CausewayObservationNaming.forLogicalMember(
                        "act",
                        "isisExtSecMan.ApplicationUser#updateEmailAddress"));
    }

    @Test
    void namespaceFreeNameIsTruncatedAtFiftyCharacters() {
        final String contextualName = CausewayObservationNaming.forMember(
                "act",
                "demo.CustomerWithAnUnusuallyLongLogicalTypeName",
                "performAnUnusuallyLongAdministrativeOperation");

        assertEquals(CausewayObservationNaming.MAX_CONTEXTUAL_NAME_LENGTH,
                contextualName.length());
        assertTrue(contextualName.startsWith(
                "act CustomerWithAnUnusuallyLongLogicalTypeName#"));
    }

    @Test
    void exactlyFiftyCharactersAreNotAltered() {
        final String contextualName = "render " + "X".repeat(43);

        assertEquals(contextualName,
                CausewayObservationNaming.bounded(contextualName));
    }

    @Test
    void renderRegionsUseMemberIdsAndDefaultFieldsetName() {
        assertEquals("render fieldset default",
                CausewayObservationNaming.forRenderRegion(
                        "fieldset", "<default>"));
        assertEquals("render property emailAddress",
                CausewayObservationNaming.forRenderRegion(
                        "property", "isisExtSecMan.ApplicationUser#emailAddress"));
        assertEquals("render collection roles",
                CausewayObservationNaming.forRenderRegion(
                        "collection", "isisExtSecMan.ApplicationUser#roles"));
        assertEquals("render action updateEmailAddress",
                CausewayObservationNaming.forRenderRegion(
                        "action", "isisExtSecMan.ApplicationUser#updateEmailAddress()"));
    }
}
