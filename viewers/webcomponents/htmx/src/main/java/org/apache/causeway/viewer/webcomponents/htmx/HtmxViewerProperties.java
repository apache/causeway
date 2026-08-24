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

    private String basePath = "/htmx";
    private String graphQlEndpoint = "/graphql";
    private String brand = "Apache Causeway";
    private String language = "en";
    private String wicketComparisonPath;
    private String applicationStylesheet;
    private boolean vaadinReferenceWidgets;
    private String vaadinFieldFamilies = "";
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

    public boolean isVaadinReferenceWidgets() {
        return vaadinReferenceWidgets;
    }

    public void setVaadinReferenceWidgets(final boolean vaadinReferenceWidgets) {
        this.vaadinReferenceWidgets = vaadinReferenceWidgets;
    }

    public String getVaadinFieldFamilies() {
        return vaadinFieldFamilies;
    }

    public void setVaadinFieldFamilies(final String vaadinFieldFamilies) {
        this.vaadinFieldFamilies = normalizeFieldFamilies(vaadinFieldFamilies);
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
