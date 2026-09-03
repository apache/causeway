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

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtmxDeclarativeTemplateTest {

    @Test
    void bindsOnlyTokensPresentInTheOriginalTemplate() {
        assertThat(HtmxDeclarativeTemplate.bind(
                "{{causeway.content}} {{causeway.brand}}",
                Map.of(
                        "content", "<article>{{causeway.brand}}</article>",
                        "brand", "Pet Clinic"),
                "TEST_BINDING_INVALID"))
                .isEqualTo("<article>{{causeway.brand}}</article> Pet Clinic");
    }

    @Test
    void rejectsUnknownAndUnusedBindingsWithoutDisclosingValues() {
        assertThatThrownBy(() -> HtmxDeclarativeTemplate.bind(
                "{{causeway.unknown}}",
                Map.of("secret", "do-not-disclose"),
                "TEST_BINDING_INVALID"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TEST_BINDING_INVALID")
                .hasMessageNotContaining("do-not-disclose");

        assertThatThrownBy(() -> HtmxDeclarativeTemplate.bind(
                "plain",
                Map.of("secret", "do-not-disclose"),
                "TEST_BINDING_INVALID"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TEST_BINDING_INVALID")
                .hasMessageNotContaining("do-not-disclose");
    }
}
