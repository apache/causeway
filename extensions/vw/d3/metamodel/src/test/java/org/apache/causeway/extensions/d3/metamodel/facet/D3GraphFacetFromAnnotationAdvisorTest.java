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

import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.causeway.core.mmtestsupport.MetaModelContext_forTesting;
import org.apache.causeway.extensions.d3.applib.annotations.D3Graph;
import org.apache.causeway.extensions.d3.applib.spi.D3GraphAdvisor;

/**
 * Verifies that {@link D3GraphFacetFromAnnotation#configFor(Class)} merges advisor-provided
 * advice (including {@code graphData}) into the {@link org.apache.causeway.extensions.d3.applib.config.D3Config}.
 */
class D3GraphFacetFromAnnotationAdvisorTest {

    @D3Graph(clickAction = "selectNode")
    static class Fixture {}

    private static final String ADVISOR_GRAPH_DATA = "{\"nodes\":[{\"id\":\"A\"}],\"links\":[]}";

    private D3GraphFacetFromAnnotation facet() {
        var mmc = MetaModelContext_forTesting.buildDefault();
        var objectSpec = mmc.getSpecificationLoader().loadSpecification(Fixture.class);
        var annotation = Fixture.class.getAnnotation(D3Graph.class);
        return D3GraphFacetFromAnnotation.create(annotation, objectSpec);
    }

    @Test
    void mergesAdvisorGraphData() {
        var facet = facet();
        facet.advisors = List.of(domainClass -> new D3GraphAdvisor.Advice(
                ADVISOR_GRAPH_DATA, null, null, null, null, null));

        var config = facet.configFor(Fixture.class);

        assertThat(config.getGraphData()).isEqualTo(ADVISOR_GRAPH_DATA);
    }

    @Test
    void advisorCanOverrideClickAction() {
        var facet = facet();
        facet.advisors = List.of(domainClass -> new D3GraphAdvisor.Advice(
                null, null, null, null, null, "advisorClickAction"));

        var config = facet.configFor(Fixture.class);

        assertThat(config.getClickAction()).isEqualTo("advisorClickAction");
    }

    @Test
    void graphDataNull_whenNoAdvisor() {
        var config = facet().configFor(Fixture.class);

        assertThat(config.getGraphData()).isNull();
        assertThat(config.getClickAction()).isEqualTo("selectNode");
    }

    @Test
    void advisorCanDeferDraggingAndZoom() {
        var facet = facet();
        // null dragging/zoom => keep annotation/config defaults (true)
        facet.advisors = List.of(domainClass -> new D3GraphAdvisor.Advice(
                null, null, null, null, null, null));

        var config = facet.configFor(Fixture.class);

        assertThat(config.isEnableDragging()).isTrue();
        assertThat(config.isEnableZoom()).isTrue();
    }

}
