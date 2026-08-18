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
package org.apache.causeway.viewer.graphql.model.domain.common.query;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.config.CausewayConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourcePathTest {

    private static final Bookmark BOOKMARK =
            Bookmark.forLogicalTypeNameAndIdentifier("example.Customer", "cust-1");

    @Test
    void rootDeploymentUsesOneOriginRelativePrefix() {
        var path = ResourcePath.of(null, null, "/graphql");

        assertEquals(
                "/graphql/object/example.Customer:cust-1/_meta/grid",
                path.metadata(BOOKMARK, "_meta", "grid"));
        assertFalse(path.controllerPath().startsWith("//"));
    }

    @Test
    void servletContextAndNonDefaultEndpointAreRetained() {
        var path = ResourcePath.of(null, "/causeway/", "/api/query/");

        assertEquals(
                "/causeway/api/query/object/example.Customer:cust-1/photo/blobBytes",
                path.property(BOOKMARK, "photo", "blobBytes"));
    }

    @Test
    void configuredReverseProxyPrefixIsRetainedExactlyOnce() {
        var path = ResourcePath.of("/public", "/causeway", "/graphql");

        assertEquals(
                "/public/causeway/graphql/object",
                path.controllerPath());
    }

    @Test
    void opaqueUrlSafeIdentityIsNotDoubleEncoded() {
        var bookmark = Bookmark.forLogicalTypeNameAndIdentifier(
                "example.Customer",
                "cust%2D1");

        var resource = ResourcePath.of(null, null, "/graphql")
                .property(bookmark, "photo", "blobBytes");

        assertTrue(resource.contains("cust%2D1"));
        assertFalse(resource.contains("%252D"));
    }

    @Test
    void unsafeAuthorityAndIdentityAreRejectedWithoutEchoingInput() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ResourcePath.of(null, null, "https://other.example/graphql"));
        var failure = assertThrows(
                IllegalArgumentException.class,
                () -> ResourcePath.of(null, null, "/graphql").property(
                        Bookmark.forLogicalTypeNameAndIdentifier("example.Customer", "secret/id"),
                        "photo",
                        "blobBytes"));
        assertFalse(failure.getMessage().contains("secret"));
    }

    @Test
    @SuppressWarnings("deprecation")
    void categoryPoliciesFallBackToLegacySettingUnlessOverridden() {
        var legacy = new CausewayConfiguration.Viewer.Graphql.Resources(
                CausewayConfiguration.Viewer.Graphql.ResponseType.ATTACHMENT,
                null,
                null,
                null);
        assertEquals(
                CausewayConfiguration.Viewer.Graphql.ResponseType.ATTACHMENT,
                legacy.effectiveStructuralMetadataResponseType());
        assertEquals(
                CausewayConfiguration.Viewer.Graphql.ResponseType.ATTACHMENT,
                legacy.effectiveValueContentResponseType());

        var split = new CausewayConfiguration.Viewer.Graphql.Resources(
                CausewayConfiguration.Viewer.Graphql.ResponseType.ATTACHMENT,
                CausewayConfiguration.Viewer.Graphql.ResponseType.DIRECT,
                CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN,
                "/public");
        assertEquals(
                CausewayConfiguration.Viewer.Graphql.ResponseType.DIRECT,
                split.effectiveStructuralMetadataResponseType());
        assertEquals(
                CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN,
                split.effectiveValueContentResponseType());
    }
}
