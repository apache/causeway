/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.htmx;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtmxRouteCodecTest {

    private final HtmxRouteCodec codec = new HtmxRouteCodec("/apps/petclinic");

    @Test
    void roundTripsUnicodeLogicalTypeAndOpaqueIdentifier() {
        final var route = new HtmxObjectRoute("petclinic.Pet Owner/β".replace("/", "∕"), "owner ?#42 café");

        final var path = codec.objectPath(route);

        assertThat(path).isEqualTo("/apps/petclinic/object/petclinic.Pet%20Owner%E2%88%95%CE%B2/owner%20%3F%2342%20caf%C3%A9");
        assertThat(codec.parseObjectPath(path)).isEqualTo(route);
    }

    @Test
    void roundTripsLongOpaqueIdentifierWithinCanonicalEncodedBound() {
        final var identifier = "memento-" + "a".repeat(3000);
        final var route = new HtmxObjectRoute("demo.CompositeValuesPage", identifier);

        final var path = codec.objectPath(route);

        assertThat(path).endsWith(identifier);
        assertThat(codec.parseObjectPath(path)).isEqualTo(route);
    }

    @Test
    void acceptsExactEncodedBoundaryAndRejectsOverflowOrExpansion() {
        assertThat(HtmxRouteCodec.encodeSegment("a".repeat(HtmxRouteCodec.MAX_ENCODED_LENGTH)))
                .hasSize(HtmxRouteCodec.MAX_ENCODED_LENGTH);
        assertThatThrownBy(() -> HtmxRouteCodec.encodeSegment("a".repeat(HtmxRouteCodec.MAX_ENCODED_LENGTH + 1)))
                .isInstanceOf(InvalidHtmxRouteException.class);
        assertThatThrownBy(() -> HtmxRouteCodec.encodeSegment("é".repeat(2049)))
                .isInstanceOf(InvalidHtmxRouteException.class);
        assertThatThrownBy(() -> HtmxRouteCodec.encodeSegment(" ".repeat(1366)))
                .isInstanceOf(InvalidHtmxRouteException.class);
        assertThat(HtmxRouteCodec.encodeSegment("!'()*~"))
                .isEqualTo("%21%27%28%29%2A~");
    }

    @Test
    void supportsConfiguredDeploymentBasePath() {
        assertThat(codec.basePath()).isEqualTo("/apps/petclinic");
        assertThat(codec.isRootPath("/apps/petclinic/")).isTrue();
        assertThat(codec.objectPath(new HtmxObjectRoute("petclinic.PetOwner", "1")))
                .startsWith("/apps/petclinic/object/");
    }

    @ParameterizedTest
    @MethodSource("invalidPaths")
    void rejectsMalformedOrNonCanonicalPaths(final String path) {
        assertThatThrownBy(() -> codec.parseObjectPath(path))
                .isInstanceOf(InvalidHtmxRouteException.class)
                .hasMessage("The requested application route is invalid.");
    }

    static Stream<String> invalidPaths() {
        return Stream.of(
                "/apps/petclinic/object/type",
                "/apps/petclinic/object/type/",
                "/apps/petclinic/object//id",
                "/apps/petclinic/object/type/id/extra",
                "/apps/petclinic/object/type/%",
                "/apps/petclinic/object/type/%2f",
                "/apps/petclinic/object/type/%2F",
                "/apps/petclinic/object/type/%5C",
                "/apps/petclinic/object/type/%00",
                "/apps/petclinic/object/type/%C2%80",
                "/apps/petclinic/object/type/%ED%A0%80",
                "/apps/petclinic/object/type/%2E%2E",
                "/apps/petclinic/object/type/%41",
                "/other/object/type/id");
    }

    @Test
    void rejectsOverlongValuesMalformedUnicodeAndInvalidBasePaths() {
        assertThatThrownBy(() -> codec.objectPath(new HtmxObjectRoute("type", "x".repeat(4097))))
                .isInstanceOf(InvalidHtmxRouteException.class);
        assertThatThrownBy(() -> codec.objectPath(new HtmxObjectRoute("type", "\ud800")))
                .isInstanceOf(InvalidHtmxRouteException.class);
        assertThatThrownBy(() -> new HtmxRouteCodec("/"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HtmxRouteCodec("relative"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HtmxRouteCodec("/app/../viewer"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
