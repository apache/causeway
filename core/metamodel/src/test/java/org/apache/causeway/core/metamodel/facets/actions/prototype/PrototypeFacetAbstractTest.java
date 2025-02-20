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
package org.apache.causeway.core.metamodel.facets.actions.prototype;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.core.config.environment.DeploymentType;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.interactions.vis.VisibilityContext;

class PrototypeFacetAbstractTest {

    @Mock VisibilityContext mockVisibilityContext;
    @Mock FacetHolder mockFacetHolder;

    @Test
    public void allCombinations() throws Exception {
        givenWhenThen(DeploymentType.PROTOTYPING, null);
        givenWhenThen(DeploymentType.PRODUCTION, "Prototyping action not visible in production mode");
    }

    protected void givenWhenThen(final DeploymentType deploymentType, final String expected) {
        // given
        final PrototypeFacetAbstract facet = new PrototypeFacetAbstract(mockFacetHolder, deploymentType){};

        // when
        final String reason = facet.hides(mockVisibilityContext);

        // then
        assertEquals(expected, reason);
    }
}