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

import org.junit.jupiter.api.Test;

import org.apache.causeway.commons.io.JsonUtils;

/**
 * Unit tests for the immutable {@link D3Config} value type: its defaults, the
 * (seeded) builder, the validating {@code withXxx(...)} copy methods and JSON
 * serialization/deserialization.
 */
class D3ConfigTest {

    @Test
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
    void builder_isSeededWithDefaults() {
        assertThat(D3Config.builder().build()).isEqualTo(D3Config.defaults());
    }

    @Test
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

    @Test
    void with_returnsNewInstance_andLeavesOriginalUntouched() {
        var original = D3Config.defaults();

        var updated = original.withInitialWidth(1000);

        assertThat(updated).isNotSameAs(original);
        assertThat(updated.initialWidth()).isEqualTo(1000);
        assertThat(original.initialWidth()).isEqualTo(800); // original unchanged
    }

    @Test
    void withInitialWidth_rejectsOutOfRange() {
        assertThatThrownBy(() -> D3Config.defaults().withInitialWidth(199))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("initialWidth");
        assertThatThrownBy(() -> D3Config.defaults().withInitialWidth(4001))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withInitialHeight_rejectsOutOfRange() {
        assertThatThrownBy(() -> D3Config.defaults().withInitialHeight(199))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("initialHeight");
    }

    @Test
    void withLinkDistance_rejectsOutOfRange() {
        assertThatThrownBy(() -> D3Config.defaults().withLinkDistance(19))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("linkDistance");
    }

    @Test
    void withRepulsionStrength_rejectsOutOfRange() {
        assertThatThrownBy(() -> D3Config.defaults().withRepulsionStrength(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("repulsionStrength");
    }

    @Test
    void withCollisionRadius_rejectsOutOfRange() {
        assertThatThrownBy(() -> D3Config.defaults().withCollisionRadius(501))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("collisionRadius");
    }

    @Test
    void withDistributionStrength_rejectsOutOfRange() {
        assertThatThrownBy(() -> D3Config.defaults().withDistributionStrength(1.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("distributionStrength");
    }

    @Test
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
    void asBuilder_roundTripsToEqualInstance() {
        var config = D3Config.defaults()
                .withLinkDistance(300)
                .withGraphData("{\"nodes\":[]}");

        assertThat(config.asBuilder().build()).isEqualTo(config);
    }

    @Test
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

    @Test
    void equalsAndHashCode_areValueBased() {
        var a = D3Config.defaults().withGraphData("g");
        var b = D3Config.defaults().withGraphData("g");
        var c = D3Config.defaults().withGraphData("other");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }
}
