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
package org.apache.isis.core.metamodel.facets.actions.prototype;

import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.commons.internal.plugins.environment.DeploymentType;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.junit.Assert.assertEquals;

public class PrototypeFacetAbstractTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    private VisibilityContext mockVisibilityContext;
    @Mock
    private FacetHolder mockFacetHolder;

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