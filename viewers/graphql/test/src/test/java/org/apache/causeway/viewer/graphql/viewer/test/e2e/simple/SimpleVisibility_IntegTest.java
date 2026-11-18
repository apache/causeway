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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.causeway.viewer.graphql.viewer.test.e2e.simple;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Security regression tests for the GraphQL viewer's <i>simple</i> schema.
 *
 * <p>
 * The simple schema's read resolvers must evaluate the framework's
 * {@code isVisible()} visibility/authorization consent, so that an authenticated
 * but unauthorized user cannot read members the {@code Authorizor} (or an
 * imperative {@code hideXxx()}) would hide (CWE-863).
 * </p>
 *
 * <p>
 * Background: {@code SimpleProperty} / {@code SimpleCollection} originally called
 * {@code association.get(managedObject)} with no preceding visibility check.
 * Commit {@code c01d1a459ee} (CAUSEWAY-4050) added the check to the non-LOB
 * property branch and to collections, but <b>not</b> to the Blob/Clob (LOB)
 * branch of {@code SimpleProperty.fetchData} nor to
 * {@code SimplePropertyLobAbstract.fetchDataFromLob} (used by the {@code name}
 * and {@code mimeType} child fields).
 * </p>
 *
 * <p>
 * The methods named {@code *IsNotReadable} assert the <b>desired</b> (secure)
 * behaviour. The ones covering the still-unfixed LOB branch currently fail and
 * therefore document the remaining bug; they are expected to turn green once
 * the LOB resolvers are fixed.
 * </p>
 */
//NOT USING @Transactional since we are running server within same transaction otherwise
@Order(43)
@ActiveProfiles("test")
public class SimpleVisibility_IntegTest extends Abstract_IntegTest {

    /**
     * Verifies the fix for collections (CAUSEWAY-4050): a hidden collection must
     * not be readable through the simple schema.
     */
    @Test
    void hiddenCollectionIsNotReadable() {
        var response = submit("hiddenCollection");

        assertThat(response)
                .contains("Department#hiddenStaffMembers");
        assertThat(response)
                .doesNotContain("Letitia Leadbetter")
                .doesNotContain("Gerry Jones")
                .doesNotContain("Mervin Hughes");
    }

    /**
     * Verifies the fix for regular (non-LOB) properties (CAUSEWAY-4050): a
     * property hidden via {@code hideXxx()} must not be readable through the
     * simple schema.
     */
    @Test
    void hiddenPropertyIsNotReadable() {
        var response = submit("hiddenProperty");

        assertThat(response)
                .contains("StaffMember#hiddenName");
        assertThat(response)
                .doesNotContain("CONFIDENTIAL_MEMBER_NAME");
    }

    /**
     * Remaining bug: the LOB (Blob/Clob) branch of {@code SimpleProperty} and
     * {@code SimplePropertyLobAbstract} do not evaluate {@code isVisible()}, so
     * the metadata (name, mimeType) of a hidden Blob property leaks.
     *
     * <p>
     * Expected (secure): the framework raises a {@code HiddenException} and the
     * blob metadata is not disclosed. Currently this test <b>fails</b>.
     * </p>
     */
    @Test
    void hiddenBlobPropertyMetadataIsNotReadable() {
        var response = submit("hiddenBlobProperty");

        assertThat(response)
                .contains("StaffMember#hiddenPhoto");
        assertThat(response)
                .doesNotContain("hidden.pdf")
                .doesNotContain("application/pdf");
    }
}
