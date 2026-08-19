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

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtmxPageFragmentRegistryTest {

    @Test
    void selectsOnlyAnExactLogicalType() {
        final var factory = factory("petclinic.PetOwner", "<p>owner</p>");
        final var registry = new HtmxPageFragmentRegistry(List.of(factory));

        assertThat(registry.find("petclinic.PetOwner")).contains(factory);
        assertThat(registry.find("petclinic.SpecialPetOwner")).isEmpty();
    }

    @Test
    void rejectsDuplicateOrBlankRegistrations() {
        assertThatThrownBy(() -> new HtmxPageFragmentRegistry(List.of(
                factory("petclinic.PetOwner", "first"),
                factory("petclinic.PetOwner", "second"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("More than one");
        assertThatThrownBy(() -> new HtmxPageFragmentRegistry(List.of(factory(" ", "blank"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must declare");
    }

    private static HtmxPageFragmentFactory factory(final String logicalTypeName, final String html) {
        return new HtmxPageFragmentFactory() {
            @Override
            public String logicalTypeName() {
                return logicalTypeName;
            }

            @Override
            public String render(final HtmxObjectRoute route) {
                return html;
            }
        };
    }
}
