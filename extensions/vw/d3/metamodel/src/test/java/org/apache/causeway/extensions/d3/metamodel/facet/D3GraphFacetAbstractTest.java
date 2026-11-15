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
package org.apache.causeway.extensions.d3.metamodel.facet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.mmtestsupport.MetaModelContext_forTesting;
import org.apache.causeway.extensions.d3.applib.config.D3Config;

/**
 * Tests the default behaviour of {@link D3GraphFacetAbstract} — {@code configFor(...)},
 * the {@code config()} accessor and {@code getClickAction(...)} — which
 * {@link D3GraphFacetFromAnnotation} overrides and would otherwise leave uncovered.
 */
@DisplayName("D3GraphFacetAbstract — default behaviour")
class D3GraphFacetAbstractTest {

    private final MetaModelContext mmc = MetaModelContext_forTesting.buildDefault();

    /** Minimal concrete facet that inherits the abstract base's default behaviour. */
    private static final class MinimalFacet extends D3GraphFacetAbstract {
        MinimalFacet(final D3Config config, final FacetHolder holder) {
            super(config, holder);
        }
    }

    private D3GraphFacetAbstract facet(final D3Config config) {
        return new MinimalFacet(config, FacetHolder.forTesting(mmc));
    }

    @Test
    @DisplayName("configFor(...) returns the backing config, regardless of the domain class")
    void configFor_returnsConfig() {
        var config = D3Config.defaults().withClickAction("doIt");
        var facet = facet(config);

        assertThat(facet.configFor(Object.class)).isSameAs(config);
        assertThat(facet.configFor(String.class)).isSameAs(config);
    }

    @Test
    @DisplayName("config() exposes the backing config")
    void config_accessor_exposesConfig() {
        var config = D3Config.defaults();

        assertThat(facet(config).config()).isSameAs(config);
    }

    @Test
    @DisplayName("getClickAction(...) is empty when the click action is blank")
    void getClickAction_emptyWhenBlank() {
        assertThat(facet(D3Config.defaults()).getClickAction(Object.class)).isEmpty();
    }

    @Test
    @DisplayName("getClickAction(...) returns the click action when set")
    void getClickAction_presentWhenSet() {
        var facet = facet(D3Config.defaults().withClickAction("doIt"));

        assertThat(facet.getClickAction(Object.class)).contains("doIt");
    }
}
