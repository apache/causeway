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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "causeway.viewer.webcomponents.htmx")
public class HtmxViewerProperties {

    public enum ComponentToolkit {
        VAADIN("vaadin"),
        NATIVE("native");

        private final String externalName;

        ComponentToolkit(final String externalName) {
            this.externalName = externalName;
        }

        public String externalName() {
            return externalName;
        }
    }

    /**
     * @deprecated Use {@link ComponentToolkit} through {@code component-toolkit}.
     */
    @Deprecated(forRemoval = false)
    public enum EditorToolkit {
        VAADIN("vaadin"),
        NATIVE("native");

        private final String externalName;

        EditorToolkit(final String externalName) {
            this.externalName = externalName;
        }

        public String externalName() {
            return externalName;
        }
    }

    static final String ALL_VAADIN_FIELD_FAMILIES = "basic,numeric,local-temporal";

    private String basePath = "/htmx";
    private String graphQlEndpoint = "/graphql";
    private String brand = "Apache Causeway";
    private String language = "en";
    private String wicketComparisonPath;
    private String applicationStylesheet;
    private ComponentToolkit componentToolkit = ComponentToolkit.VAADIN;
    private boolean componentToolkitConfigured;
    private EditorToolkit editorToolkit = EditorToolkit.VAADIN;
    private boolean editorToolkitConfigured;
    private boolean vaadinReferenceWidgets;
    private boolean vaadinReferenceWidgetsConfigured;
    private String vaadinFieldFamilies = "";
    private boolean vaadinFieldFamiliesConfigured;
    private int referenceMinimumSearchLength = 2;
    private int referenceMaximumResults = 50;

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(final String basePath) {
        this.basePath = basePath;
    }

    public String getGraphQlEndpoint() {
        return graphQlEndpoint;
    }

    public void setGraphQlEndpoint(final String graphQlEndpoint) {
        this.graphQlEndpoint = graphQlEndpoint;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(final String brand) {
        this.brand = brand;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public String getWicketComparisonPath() {
        return wicketComparisonPath;
    }

    public void setWicketComparisonPath(final String wicketComparisonPath) {
        this.wicketComparisonPath = wicketComparisonPath;
    }

    public String getApplicationStylesheet() {
        return applicationStylesheet;
    }

    public void setApplicationStylesheet(final String applicationStylesheet) {
        this.applicationStylesheet = applicationStylesheet;
    }

    public ComponentToolkit getComponentToolkit() {
        return componentToolkit;
    }

    public void setComponentToolkit(final ComponentToolkit componentToolkit) {
        if (componentToolkit == null) {
            throw new IllegalArgumentException("Component toolkit must be vaadin or native.");
        }
        this.componentToolkit = componentToolkit;
        this.componentToolkitConfigured = true;
    }

    /**
     * @deprecated Use {@code component-toolkit}.
     */
    @Deprecated(forRemoval = false)
    public EditorToolkit getEditorToolkit() {
        return editorToolkit;
    }

    /**
     * @deprecated Use {@code component-toolkit}.
     */
    @Deprecated(forRemoval = false)
    public void setEditorToolkit(final EditorToolkit editorToolkit) {
        if (editorToolkit == null) {
            throw new IllegalArgumentException("Editor toolkit must be vaadin or native.");
        }
        this.editorToolkit = editorToolkit;
        this.editorToolkitConfigured = true;
    }

    /**
     * @deprecated Use {@code component-toolkit}.
     */
    @Deprecated(forRemoval = false)
    public boolean isVaadinReferenceWidgets() {
        return vaadinReferenceWidgets;
    }

    /**
     * @deprecated Use {@code editor-toolkit}.
     */
    @Deprecated(forRemoval = false)
    public void setVaadinReferenceWidgets(final boolean vaadinReferenceWidgets) {
        this.vaadinReferenceWidgets = vaadinReferenceWidgets;
        this.vaadinReferenceWidgetsConfigured = true;
    }

    /**
     * @deprecated Use {@code editor-toolkit}.
     */
    @Deprecated(forRemoval = false)
    public String getVaadinFieldFamilies() {
        return vaadinFieldFamilies;
    }

    /**
     * @deprecated Use {@code editor-toolkit}.
     */
    @Deprecated(forRemoval = false)
    public void setVaadinFieldFamilies(final String vaadinFieldFamilies) {
        this.vaadinFieldFamilies = normalizeFieldFamilies(vaadinFieldFamilies);
        this.vaadinFieldFamiliesConfigured = true;
    }

    public boolean isEffectiveVaadinReferenceWidgets() {
        if (componentToolkitConfigured) {
            return componentToolkit == ComponentToolkit.VAADIN;
        }
        if (editorToolkitConfigured) {
            return editorToolkit == EditorToolkit.VAADIN;
        }
        if (usesDeprecatedToolkitConfiguration()) {
            return vaadinReferenceWidgetsConfigured && vaadinReferenceWidgets;
        }
        return componentToolkit == ComponentToolkit.VAADIN;
    }

    public String getEffectiveVaadinFieldFamilies() {
        if (componentToolkitConfigured) {
            return componentToolkit == ComponentToolkit.VAADIN ? ALL_VAADIN_FIELD_FAMILIES : "";
        }
        if (editorToolkitConfigured) {
            return editorToolkit == EditorToolkit.VAADIN ? ALL_VAADIN_FIELD_FAMILIES : "";
        }
        if (usesDeprecatedToolkitConfiguration()) {
            return vaadinFieldFamiliesConfigured ? vaadinFieldFamilies : "";
        }
        return componentToolkit == ComponentToolkit.VAADIN ? ALL_VAADIN_FIELD_FAMILIES : "";
    }

    public boolean isEffectiveVaadinPresentation() {
        return !usesDeprecatedToolkitConfiguration() && effectiveComponentToolkit() == ComponentToolkit.VAADIN;
    }

    public boolean isEffectiveVaadinActionButtons() {
        return isEffectiveVaadinPresentation();
    }

    public boolean isEffectiveVaadinCollectionGrid() {
        return isEffectiveVaadinPresentation();
    }

    public boolean isEffectiveVaadinApplicationMenubar() {
        return isEffectiveVaadinPresentation();
    }

    public boolean usesEditorToolkitCompatibility() {
        return !componentToolkitConfigured && editorToolkitConfigured;
    }

    public boolean usesDeprecatedToolkitConfiguration() {
        return !componentToolkitConfigured
                && !editorToolkitConfigured
                && (vaadinReferenceWidgetsConfigured || vaadinFieldFamiliesConfigured);
    }

    public String getResolvedComponentToolkit() {
        return effectiveComponentToolkit().externalName();
    }

    public String getToolkitConfigurationSource() {
        if (componentToolkitConfigured) {
            return "component";
        }
        if (editorToolkitConfigured) {
            return "editor-compatibility";
        }
        if (usesDeprecatedToolkitConfiguration()) {
            return "pilot-compatibility";
        }
        return "default";
    }

    /**
     * @deprecated Use {@link #getResolvedComponentToolkit()} and {@link #getToolkitConfigurationSource()}.
     */
    @Deprecated(forRemoval = false)
    public String getResolvedEditorToolkit() {
        if (usesDeprecatedToolkitConfiguration()) {
            return "compatibility";
        }
        return effectiveComponentToolkit().externalName();
    }

    private ComponentToolkit effectiveComponentToolkit() {
        if (componentToolkitConfigured) {
            return componentToolkit;
        }
        if (editorToolkitConfigured) {
            return editorToolkit == EditorToolkit.VAADIN ? ComponentToolkit.VAADIN : ComponentToolkit.NATIVE;
        }
        if (usesDeprecatedToolkitConfiguration()) {
            return ComponentToolkit.NATIVE;
        }
        return componentToolkit;
    }

    public int getReferenceMinimumSearchLength() {
        return referenceMinimumSearchLength;
    }

    public void setReferenceMinimumSearchLength(final int referenceMinimumSearchLength) {
        this.referenceMinimumSearchLength = referenceMinimumSearchLength;
    }

    public int getReferenceMaximumResults() {
        return referenceMaximumResults;
    }

    public void setReferenceMaximumResults(final int referenceMaximumResults) {
        this.referenceMaximumResults = referenceMaximumResults;
    }

    private static String normalizeFieldFamilies(final String configured) {
        if (configured == null || configured.isBlank()) {
            return "";
        }
        final var requested = java.util.Arrays.stream(configured.split(",", -1))
                .map(String::trim)
                .toList();
        if (requested.stream().anyMatch(String::isBlank)
                || requested.stream().distinct().count() != requested.size()
                || requested.stream().anyMatch(value -> !java.util.Set.of("basic", "numeric", "local-temporal").contains(value))) {
            throw new IllegalArgumentException("Vaadin field families must be a unique comma-separated subset of basic, numeric, and local-temporal.");
        }
        return java.util.stream.Stream.of("basic", "numeric", "local-temporal")
                .filter(requested::contains)
                .collect(java.util.stream.Collectors.joining(","));
    }
}
