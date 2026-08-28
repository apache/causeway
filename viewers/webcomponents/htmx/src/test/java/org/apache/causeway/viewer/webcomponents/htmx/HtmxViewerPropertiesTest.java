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

        assertThat(properties.getComponentToolkit()).isEqualTo(HtmxViewerProperties.ComponentToolkit.VAADIN);
        assertThat(properties.getResolvedComponentToolkit()).isEqualTo("vaadin");
        assertThat(properties.getToolkitConfigurationSource()).isEqualTo("default");
        assertThat(properties.isEffectiveVaadinPresentation()).isTrue();
        assertThat(properties.isEffectiveVaadinActionButtons()).isTrue();
        assertThat(properties.isEffectiveVaadinReferenceWidgets()).isTrue();
        assertThat(properties.getEffectiveVaadinFieldFamilies())
                .isEqualTo("basic,numeric,local-temporal");
        assertThat(properties.usesDeprecatedToolkitConfiguration()).isFalse();
    }

    @Test
    void explicitNativePolicyDisablesEveryVaadinAdapter() {
        final var properties = new HtmxViewerProperties();

        properties.setComponentToolkit(HtmxViewerProperties.ComponentToolkit.NATIVE);

        assertThat(properties.getResolvedComponentToolkit()).isEqualTo("native");
        assertThat(properties.getToolkitConfigurationSource()).isEqualTo("component");
        assertThat(properties.isEffectiveVaadinPresentation()).isFalse();
        assertThat(properties.isEffectiveVaadinActionButtons()).isFalse();
        assertThat(properties.isEffectiveVaadinReferenceWidgets()).isFalse();
        assertThat(properties.getEffectiveVaadinFieldFamilies()).isEmpty();
    }

    @Test
    @SuppressWarnings("deprecation")
    void explicitCommonPolicyOverridesDeprecatedValues() {
        final var properties = new HtmxViewerProperties();
        properties.setVaadinReferenceWidgets(false);
        properties.setVaadinFieldFamilies("");

        properties.setComponentToolkit(HtmxViewerProperties.ComponentToolkit.VAADIN);

        assertThat(properties.usesDeprecatedToolkitConfiguration()).isFalse();
        assertThat(properties.getResolvedComponentToolkit()).isEqualTo("vaadin");
        assertThat(properties.getToolkitConfigurationSource()).isEqualTo("component");
        assertThat(properties.isEffectiveVaadinReferenceWidgets()).isTrue();
        assertThat(properties.getEffectiveVaadinFieldFamilies())
                .isEqualTo("basic,numeric,local-temporal");
    }

    @Test
    @SuppressWarnings("deprecation")
    void explicitComponentPolicyWinsOverConflictingEditorAndPilotValues() {
        final var nativeToolkit = new HtmxViewerProperties();
        nativeToolkit.setEditorToolkit(HtmxViewerProperties.EditorToolkit.VAADIN);
        nativeToolkit.setVaadinReferenceWidgets(true);
        nativeToolkit.setVaadinFieldFamilies("basic,numeric");
        nativeToolkit.setComponentToolkit(HtmxViewerProperties.ComponentToolkit.NATIVE);

        assertThat(nativeToolkit.getResolvedComponentToolkit()).isEqualTo("native");
        assertThat(nativeToolkit.getToolkitConfigurationSource()).isEqualTo("component");
        assertThat(nativeToolkit.isEffectiveVaadinReferenceWidgets()).isFalse();
        assertThat(nativeToolkit.getEffectiveVaadinFieldFamilies()).isEmpty();
        assertThat(nativeToolkit.isEffectiveVaadinPresentation()).isFalse();
        assertThat(nativeToolkit.isEffectiveVaadinActionButtons()).isFalse();
    }

    @Test
    @SuppressWarnings("deprecation")
    void explicitEditorCompatibilityWinsOverConflictingPilotValues() {
        final var nativeToolkit = new HtmxViewerProperties();
        nativeToolkit.setVaadinReferenceWidgets(true);
        nativeToolkit.setVaadinFieldFamilies("basic,numeric,local-temporal");
        nativeToolkit.setEditorToolkit(HtmxViewerProperties.EditorToolkit.NATIVE);

        assertThat(nativeToolkit.getResolvedComponentToolkit()).isEqualTo("native");
        assertThat(nativeToolkit.getToolkitConfigurationSource()).isEqualTo("editor-compatibility");
        assertThat(nativeToolkit.isEffectiveVaadinReferenceWidgets()).isFalse();
        assertThat(nativeToolkit.getEffectiveVaadinFieldFamilies()).isEmpty();
        assertThat(nativeToolkit.isEffectiveVaadinPresentation()).isFalse();
        assertThat(nativeToolkit.isEffectiveVaadinActionButtons()).isFalse();
    }

    @Test
    @SuppressWarnings("deprecation")
    void anyDeprecatedPropertyActivatesTheCompleteOldDefaultPolicy() {
        final var referenceOnly = new HtmxViewerProperties();
        referenceOnly.setVaadinReferenceWidgets(true);
        assertThat(referenceOnly.getResolvedEditorToolkit()).isEqualTo("compatibility");
        assertThat(referenceOnly.isEffectiveVaadinReferenceWidgets()).isTrue();
        assertThat(referenceOnly.getEffectiveVaadinFieldFamilies()).isEmpty();
        assertThat(referenceOnly.isEffectiveVaadinPresentation()).isFalse();
        assertThat(referenceOnly.isEffectiveVaadinActionButtons()).isFalse();
        assertThat(referenceOnly.getToolkitConfigurationSource()).isEqualTo("pilot-compatibility");

        final var fieldsOnly = new HtmxViewerProperties();
        fieldsOnly.setVaadinFieldFamilies("local-temporal,basic");
        assertThat(fieldsOnly.getResolvedEditorToolkit()).isEqualTo("compatibility");
        assertThat(fieldsOnly.isEffectiveVaadinReferenceWidgets()).isFalse();
        assertThat(fieldsOnly.getEffectiveVaadinFieldFamilies()).isEqualTo("basic,local-temporal");
        assertThat(fieldsOnly.isEffectiveVaadinPresentation()).isFalse();
        assertThat(fieldsOnly.isEffectiveVaadinActionButtons()).isFalse();
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
    void deprecatedEditorToolkitMapsToCompleteComponentPolicy() {
        final var vaadin = new HtmxViewerProperties();
        vaadin.setEditorToolkit(HtmxViewerProperties.EditorToolkit.VAADIN);
        assertThat(vaadin.usesEditorToolkitCompatibility()).isTrue();
        assertThat(vaadin.getResolvedComponentToolkit()).isEqualTo("vaadin");
        assertThat(vaadin.getToolkitConfigurationSource()).isEqualTo("editor-compatibility");
        assertThat(vaadin.isEffectiveVaadinPresentation()).isTrue();
        assertThat(vaadin.isEffectiveVaadinActionButtons()).isTrue();

        final var nativeToolkit = new HtmxViewerProperties();
        nativeToolkit.setEditorToolkit(HtmxViewerProperties.EditorToolkit.NATIVE);
        assertThat(nativeToolkit.getResolvedComponentToolkit()).isEqualTo("native");
        assertThat(nativeToolkit.isEffectiveVaadinReferenceWidgets()).isFalse();
        assertThat(nativeToolkit.getEffectiveVaadinFieldFamilies()).isEmpty();
        assertThat(nativeToolkit.isEffectiveVaadinActionButtons()).isFalse();
    }

    @Test
    @SuppressWarnings("deprecation")
    void invalidValuesAreRejectedWithoutBroadeningPolicy() {
        final var properties = new HtmxViewerProperties();

        assertThatThrownBy(() -> properties.setComponentToolkit(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vaadin or native");
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
