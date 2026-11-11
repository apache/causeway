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
package org.apache.causeway.extensions.d3.applib.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.causeway.commons.io.JsonUtils;

/**
 * Unit tests for the immutable {@link D3Config} value type: its defaults, the
 * (seeded) builder, the validating {@code withXxx(...)} copy methods and JSON
 * serialization/deserialization.
 */
@DisplayName("D3Config — immutable value type")
class D3ConfigTest {

    // -- DEFAULTS & BUILDER

    @Test
    @DisplayName("defaults() exposes the documented default values")
    void defaults_hasExpectedValues() {
        var config = D3Config.defaults();

        assertThat(config.initialWidth()).isEqualTo(800);
        assertThat(config.initialHeight()).isEqualTo(600);
        assertThat(config.enableDragging()).isTrue();
        assertThat(config.enableZoom()).isTrue();
        assertThat(config.linkDistance()).isEqualTo(120);
        assertThat(config.repulsionStrength()).isEqualTo(-400);
        assertThat(config.collisionRadius()).isEqualTo(60);
        assertThat(config.distributionStrength()).isEqualTo(0.08);
        assertThat(config.clickAction()).isEmpty();
        assertThat(config.canvasId()).isNull();
        assertThat(config.graphData()).isNull();
        assertThat(config.clickCallbackUrl()).isNull();
    }

    @Test
    @DisplayName("builder() is pre-seeded with the defaults")
    void builder_isSeededWithDefaults() {
        assertThat(D3Config.builder().build()).isEqualTo(D3Config.defaults());
    }

    @Test
    @DisplayName("builder() allows overriding individual components")
    void builder_allowsOverridingDefaults() {
        var config = D3Config.builder()
                .initialWidth(1024)
                .enableDragging(false)
                .canvasId("canvas-1")
                .build();

        assertThat(config.initialWidth()).isEqualTo(1024);
        assertThat(config.enableDragging()).isFalse();
        assertThat(config.canvasId()).isEqualTo("canvas-1");
        // untouched components keep their defaults
        assertThat(config.initialHeight()).isEqualTo(600);
        assertThat(config.enableZoom()).isTrue();
    }

    // -- withXxx(...) COPY METHODS

    @Test
    @DisplayName("withXxx(...) returns a new instance, leaving the original untouched")
    void with_returnsNewInstance_andLeavesOriginalUntouched() {
        var original = D3Config.defaults();

        var updated = original.withInitialWidth(1000);

        assertThat(updated).isNotSameAs(original);
        assertThat(updated.initialWidth()).isEqualTo(1000);
        assertThat(original.initialWidth()).isEqualTo(800); // original unchanged
    }

    @ParameterizedTest(name = "rejects out-of-range {0}")
    @MethodSource("outOfRangeMutations")
    @DisplayName("withXxx(...) rejects out-of-range values")
    void with_rejectsOutOfRange(final String field, final Function<D3Config, D3Config> mutation) {
        assertThatThrownBy(() -> mutation.apply(D3Config.defaults()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(field);
    }

    static Stream<Arguments> outOfRangeMutations() {
        return Stream.of(
                Arguments.of("initialWidth", mutate(c -> c.withInitialWidth(199))),
                Arguments.of("initialWidth", mutate(c -> c.withInitialWidth(4001))),
                Arguments.of("initialHeight", mutate(c -> c.withInitialHeight(199))),
                Arguments.of("initialHeight", mutate(c -> c.withInitialHeight(4001))),
                Arguments.of("linkDistance", mutate(c -> c.withLinkDistance(19))),
                Arguments.of("linkDistance", mutate(c -> c.withLinkDistance(1001))),
                Arguments.of("repulsionStrength", mutate(c -> c.withRepulsionStrength(1))),
                Arguments.of("repulsionStrength", mutate(c -> c.withRepulsionStrength(-5001))),
                Arguments.of("collisionRadius", mutate(c -> c.withCollisionRadius(-1))),
                Arguments.of("collisionRadius", mutate(c -> c.withCollisionRadius(501))),
                Arguments.of("distributionStrength", mutate(c -> c.withDistributionStrength(-0.1))),
                Arguments.of("distributionStrength", mutate(c -> c.withDistributionStrength(1.5))));
    }

    /** Gives the argument a target type, so the lambda need not be cast in {@link Arguments#of}. */
    private static Function<D3Config, D3Config> mutate(final Function<D3Config, D3Config> mutation) {
        return mutation;
    }

    @Test
    @DisplayName("withXxx(...) accepts boundary values")
    void with_boundaryValues_areAccepted() {
        var config = D3Config.defaults()
                .withInitialWidth(200)
                .withInitialHeight(4000)
                .withLinkDistance(20)
                .withRepulsionStrength(-5000)
                .withCollisionRadius(0)
                .withDistributionStrength(1.0)
                .withEnableDragging(false)
                .withEnableZoom(false)
                .withClickAction("doSomething");

        assertThat(config.initialWidth()).isEqualTo(200);
        assertThat(config.initialHeight()).isEqualTo(4000);
        assertThat(config.linkDistance()).isEqualTo(20);
        assertThat(config.repulsionStrength()).isEqualTo(-5000);
        assertThat(config.collisionRadius()).isZero();
        assertThat(config.distributionStrength()).isEqualTo(1.0);
        assertThat(config.enableDragging()).isFalse();
        assertThat(config.enableZoom()).isFalse();
        assertThat(config.clickAction()).isEqualTo("doSomething");
    }

    @Test
    @DisplayName("withXxx(...) accepts the complementary boundary values")
    void with_complementaryBoundaryValues_areAccepted() {
        // the opposite end of each validated range to with_boundaryValues_areAccepted
        var config = D3Config.defaults()
                .withInitialWidth(4000)
                .withInitialHeight(200)
                .withLinkDistance(1000)
                .withRepulsionStrength(0)
                .withCollisionRadius(500)
                .withDistributionStrength(0.0);

        assertThat(config.initialWidth()).isEqualTo(4000);
        assertThat(config.initialHeight()).isEqualTo(200);
        assertThat(config.linkDistance()).isEqualTo(1000);
        assertThat(config.repulsionStrength()).isZero();
        assertThat(config.collisionRadius()).isEqualTo(500);
        assertThat(config.distributionStrength()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("asBuilder() round-trips to an equal instance")
    void asBuilder_roundTripsToEqualInstance() {
        var config = D3Config.defaults()
                .withLinkDistance(300)
                .withGraphData("{\"nodes\":[]}");

        assertThat(config.asBuilder().build()).isEqualTo(config);
    }

    // -- JSON

    @Test
    @DisplayName("toJsonString() contains all properties")
    void toJsonString_containsAllProperties() {
        var json = D3Config.defaults().withCanvasId("c").toJsonString();

        assertThat(json)
                .contains("\"initialWidth\":800")
                .contains("\"initialHeight\":600")
                .contains("\"enableDragging\":true")
                .contains("\"enableZoom\":true")
                .contains("\"linkDistance\":120")
                .contains("\"repulsionStrength\":-400")
                .contains("\"collisionRadius\":60")
                .contains("\"distributionStrength\":0.08")
                .contains("\"clickAction\":\"\"")
                .contains("\"canvasId\":\"c\"")
                .contains("\"graphData\":null")
                .contains("\"clickCallbackUrl\":null");
    }

    @Test
    @DisplayName("JSON round-trip preserves the value")
    void jsonRoundTrip_preservesValue() {
        var config = D3Config.defaults()
                .withCanvasId("canvas-1")
                .withGraphData("{\"nodes\":[{\"id\":\"A\"}]}")
                .withClickAction("selectNode");

        var restored = JsonUtils.tryRead(D3Config.class, config.toJsonString())
                .getValue()
                .orElseThrow();

        assertThat(restored).isEqualTo(config);
    }

    // -- VALUE SEMANTICS

    @Test
    @DisplayName("equals/hashCode are value-based")
    void equalsAndHashCode_areValueBased() {
        var a = D3Config.defaults().withGraphData("g");
        var b = D3Config.defaults().withGraphData("g");
        var c = D3Config.defaults().withGraphData("other");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }
}
