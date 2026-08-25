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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtmxViewerPropertiesTest {

    @Test
    void defaultsToEveryQualifiedVaadinAdapter() {
        final var properties = new HtmxViewerProperties();

        assertThat(properties.getEditorToolkit()).isEqualTo(HtmxViewerProperties.EditorToolkit.VAADIN);
        assertThat(properties.getResolvedEditorToolkit()).isEqualTo("vaadin");
        assertThat(properties.isEffectiveVaadinReferenceWidgets()).isTrue();
        assertThat(properties.getEffectiveVaadinFieldFamilies())
                .isEqualTo("basic,numeric,local-temporal");
        assertThat(properties.usesDeprecatedToolkitConfiguration()).isFalse();
    }

    @Test
    void explicitNativePolicyDisablesEveryVaadinAdapter() {
        final var properties = new HtmxViewerProperties();

        properties.setEditorToolkit(HtmxViewerProperties.EditorToolkit.NATIVE);

        assertThat(properties.getResolvedEditorToolkit()).isEqualTo("native");
        assertThat(properties.isEffectiveVaadinReferenceWidgets()).isFalse();
        assertThat(properties.getEffectiveVaadinFieldFamilies()).isEmpty();
    }

    @Test
    @SuppressWarnings("deprecation")
    void explicitCommonPolicyOverridesDeprecatedValues() {
        final var properties = new HtmxViewerProperties();
        properties.setVaadinReferenceWidgets(false);
        properties.setVaadinFieldFamilies("");

        properties.setEditorToolkit(HtmxViewerProperties.EditorToolkit.VAADIN);

        assertThat(properties.usesDeprecatedToolkitConfiguration()).isFalse();
        assertThat(properties.getResolvedEditorToolkit()).isEqualTo("vaadin");
        assertThat(properties.isEffectiveVaadinReferenceWidgets()).isTrue();
        assertThat(properties.getEffectiveVaadinFieldFamilies())
                .isEqualTo("basic,numeric,local-temporal");
    }

    @Test
    @SuppressWarnings("deprecation")
    void anyDeprecatedPropertyActivatesTheCompleteOldDefaultPolicy() {
        final var referenceOnly = new HtmxViewerProperties();
        referenceOnly.setVaadinReferenceWidgets(true);
        assertThat(referenceOnly.getResolvedEditorToolkit()).isEqualTo("compatibility");
        assertThat(referenceOnly.isEffectiveVaadinReferenceWidgets()).isTrue();
        assertThat(referenceOnly.getEffectiveVaadinFieldFamilies()).isEmpty();

        final var fieldsOnly = new HtmxViewerProperties();
        fieldsOnly.setVaadinFieldFamilies("local-temporal,basic");
        assertThat(fieldsOnly.getResolvedEditorToolkit()).isEqualTo("compatibility");
        assertThat(fieldsOnly.isEffectiveVaadinReferenceWidgets()).isFalse();
        assertThat(fieldsOnly.getEffectiveVaadinFieldFamilies()).isEqualTo("basic,local-temporal");
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedFalseAndEmptyValuesRetainNativeBehavior() {
        final var properties = new HtmxViewerProperties();
        properties.setVaadinReferenceWidgets(false);
        properties.setVaadinFieldFamilies("");

        assertThat(properties.usesDeprecatedToolkitConfiguration()).isTrue();
        assertThat(properties.isEffectiveVaadinReferenceWidgets()).isFalse();
        assertThat(properties.getEffectiveVaadinFieldFamilies()).isEmpty();
    }

    @Test
    @SuppressWarnings("deprecation")
    void invalidValuesAreRejectedWithoutBroadeningPolicy() {
        final var properties = new HtmxViewerProperties();

        assertThatThrownBy(() -> properties.setEditorToolkit(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vaadin or native");
        assertThatThrownBy(() -> properties.setVaadinFieldFamilies("basic,unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("basic, numeric, and local-temporal");
        assertThatThrownBy(() -> properties.setVaadinFieldFamilies("basic,basic"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
