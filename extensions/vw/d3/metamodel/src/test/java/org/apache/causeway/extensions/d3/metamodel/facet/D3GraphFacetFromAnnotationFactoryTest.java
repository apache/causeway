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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.mmtestsupport.MetaModelContext_forTesting;
import org.apache.causeway.extensions.d3.applib.annotations.D3Graph;
import org.apache.causeway.extensions.d3.applib.spi.D3GraphAdvisor;

/**
 * Drives {@code @D3Graph} through the full {@link org.apache.causeway.core.metamodel.progmodel.ProgrammingModel}
 * (via the {@link D3GraphFacetFromAnnotationFactory.Register} refiner) and verifies that the
 * {@link D3GraphFacet} is attached to the annotated property or type.
 */
@DisplayName("D3GraphFacetFromAnnotationFactory — programming-model integration")
class D3GraphFacetFromAnnotationFactoryTest {

    @D3Graph
    static class AnnotatedType {
        @D3Graph
        public String getGraph() {
            return "{\"nodes\":[],\"links\":[]}";
        }
    }

    static class UnannotatedType {
        public String getGraph() {
            return "{}";
        }
    }

    /** Registered so the facet's {@code @Inject List<D3GraphAdvisor>} field can be satisfied. */
    static class TestAdvisor implements D3GraphAdvisor {
        @Override
        public Advice advise(final Class<?> domainClass) {
            return null;
        }
    }

    private MetaModelContext mmc;

    @BeforeEach
    void setUp() {
        mmc = MetaModelContext_forTesting.builder()
                .refiners(Can.of(ignored -> new D3GraphFacetFromAnnotationFactory.Register()))
                // resolves the facet's @Inject List<D3GraphAdvisor>
                .singleton(new TestAdvisor())
                .build();
    }

    // -- @D3Graph on a type

    @Test
    @DisplayName("@D3Graph on a type attaches the facet to its ObjectSpecification")
    void facetAttached_toAnnotatedType() {
        var spec = mmc.getSpecificationLoader().loadSpecification(AnnotatedType.class);

        assertThat(spec.containsFacet(D3GraphFacet.class)).isTrue();
    }

    @Test
    @DisplayName("an unannotated type gets no facet")
    void facetNotAttached_toUnannotatedType() {
        var spec = mmc.getSpecificationLoader().loadSpecification(UnannotatedType.class);

        assertThat(spec.containsFacet(D3GraphFacet.class)).isFalse();
    }

    // -- @D3Graph on a property

    @Test
    @DisplayName("@D3Graph on a property attaches the facet to the property")
    void facetAttached_toAnnotatedProperty() {
        var spec = mmc.getSpecificationLoader().loadSpecification(AnnotatedType.class);
        var property = spec.getProperty("graph").orElseThrow();

        assertThat(property.containsFacet(D3GraphFacet.class)).isTrue();
    }

    @Test
    @DisplayName("an unannotated property gets no facet")
    void facetNotAttached_toUnannotatedProperty() {
        var spec = mmc.getSpecificationLoader().loadSpecification(UnannotatedType.class);
        var property = spec.getProperty("graph").orElseThrow();

        assertThat(property.containsFacet(D3GraphFacet.class)).isFalse();
    }
}
