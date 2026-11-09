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
package org.apache.causeway.viewer.graphql.viewer.test.e2e.special;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Companion to {@code SimpleVisibility_IntegTest}, checking whether the same
 * member-level visibility bypass also exists in the GraphQL viewer's <i>rich</i>
 * schema.
 *
 * <p>
 * This is <b>beyond</b> the original report (which is about the simple schema),
 * and is included as an additional observation: the fix commit
 * {@code c01d1a459ee} (CAUSEWAY-4050) only hardened the simple schema.
 * </p>
 *
 * <p>
 * The methods assert the <b>desired</b> (secure) behaviour and are expected to
 * fail while the rich-schema read resolvers
 * ({@code RichAssociationGet}/{@code RichPropertyGetBlobAbstract}/
 * {@code RichPropertyGetClobAbstract}) do not evaluate {@code isVisible()}.
 * </p>
 */
//NOT USING @Transactional since we are running server within same transaction otherwise
@Order(61)
@ActiveProfiles("test")
public class RichVisibility_IntegTest extends Abstract_IntegTest {

    /**
     * Rich schema: a regular (non-LOB) property hidden via {@code hideXxx()}.
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
     * Rich schema: metadata (name, mimeType) of a hidden Blob property.
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
