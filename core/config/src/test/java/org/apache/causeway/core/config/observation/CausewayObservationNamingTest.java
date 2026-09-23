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
    void typeNameIsCompactAndNormalized() {
        assertEquals("render customer",
                CausewayObservationNaming.forType("render", "demo.Customer"));
        assertEquals("render customer",
                CausewayObservationNaming.forType("render", "other/context:Customer"));
    }

    @Test
    void memberNameIsOperationFirstAndNormalized() {
        assertEquals("invoke update-name on customer",
                CausewayObservationNaming.forMember(
                        "invoke", "demo.Customer", "updateName"));
        assertEquals("prompt create-url on customer",
                CausewayObservationNaming.forMember(
                        "prompt", "demo.Customer", "createURL"));
    }

    @Test
    void compactNamesCanCollideWhileCanonicalIdentifiersRemainExternal() {
        assertEquals(
                CausewayObservationNaming.forMember(
                        "invoke", "sales.Customer", "updateName"),
                CausewayObservationNaming.forMember(
                        "invoke", "support.Customer", "updateName"));
    }

    @Test
    void longNameKeepsDiagnosticOperationAndMemberBeforeType() {
        final String contextualName = CausewayObservationNaming.forMember(
                "invoke",
                "demo.CustomerWithAnUnusuallyLongLogicalTypeName",
                "performAnUnusuallyLongAdministrativeOperation");

        assertTrue(contextualName.startsWith(
                "invoke perform-an-unusually-long-administrative-operation on "));
        assertTrue(contextualName.length() > 50);
    }
}
