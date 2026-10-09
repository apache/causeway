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

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CausewayObservationNamingTest {
    @Test void fullNamesAndCasingAreRetainedWhenTheyFit() {
        assertEquals("act petclinic.PetOwner#updateName", CausewayObservationNaming.forMember("act", "petclinic.PetOwner", "updateName"));
        assertEquals("prop PetOwner#address", CausewayObservationNaming.forMember("prop", "PetOwner", "address"));
        assertEquals("coll PetOwner#pets", CausewayObservationNaming.forMember("coll", "PetOwner", "pets"));
    }
    @Test void namespaceIsShortenedBeforeMemberIsTruncated() {
        assertEquals("act PetOwner#updateName", CausewayObservationNaming.forMember("act", "very.long.namespace.with.many.components.PetOwner", "updateName"));
        var compact = CausewayObservationNaming.forMember("act", "a.b.PetOwner", "x".repeat(100));
        assertEquals(50, compact.length());
        assertTrue(compact.startsWith("act PetOwner#"));
    }
    @Test void namespaceCompactionMayCollide() {
        assertEquals(CausewayObservationNaming.forMember("act", "long.namespace.one.with.many.components.Owner", "name"),
                CausewayObservationNaming.forMember("act", "long.namespace.two.with.many.components.Owner", "name"));
    }
    @Test void exactBudgetAndInvalidInputs() {
        assertEquals(50, CausewayObservationNaming.forMember("act", "Type", "x".repeat(41)).length());
        assertThrows(NullPointerException.class, () -> CausewayObservationNaming.forMember("act", null, "name"));
        assertThrows(IllegalArgumentException.class, () -> CausewayObservationNaming.forMember("act", "Type", " "));
    }
}
