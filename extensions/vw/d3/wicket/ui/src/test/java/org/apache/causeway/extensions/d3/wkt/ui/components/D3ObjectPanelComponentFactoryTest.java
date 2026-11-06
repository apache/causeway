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
package org.apache.causeway.extensions.d3.wkt.ui.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.wicket.model.IModel;
import org.junit.jupiter.api.Test;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.extensions.d3.metamodel.facet.D3GraphFacet;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory.ApplicationAdvice;

/**
 * Verifies the {@code appliesTo} logic of {@link D3ObjectPanelComponentFactory}.
 */
class D3ObjectPanelComponentFactoryTest {

    private final D3ObjectPanelComponentFactory factory = new D3ObjectPanelComponentFactory();

    private static UiObjectWkt objectModel(final boolean hasD3GraphFacet) {
        var objectModel = mock(UiObjectWkt.class);
        var objectSpec = mock(ObjectSpecification.class);
        when(objectSpec.isSingular()).thenReturn(true);
        when(objectSpec.isValue()).thenReturn(false);
        doReturn(hasD3GraphFacet).when(objectSpec).containsFacet(D3GraphFacet.class);
        when(objectModel.getTypeOfSpecification()).thenReturn(objectSpec);
        return objectModel;
    }

    @Test
    void appliesExclusively_whenTypeHasD3GraphFacet() {
        var advice = factory.appliesTo(UiComponentType.DOMAIN_OBJECT, objectModel(true));
        assertThat(advice).isEqualTo(ApplicationAdvice.APPLIES_EXCLUSIVELY);
    }

    @Test
    void doesNotApply_whenTypeMissingD3GraphFacet() {
        var advice = factory.appliesTo(UiComponentType.DOMAIN_OBJECT, objectModel(false));
        assertThat(advice.applies()).isFalse();
    }

    @Test
    void doesNotApply_whenModelIsNotUiObjectWkt() {
        var advice = factory.appliesTo(UiComponentType.DOMAIN_OBJECT, mock(IModel.class));
        assertThat(advice.applies()).isFalse();
    }

    @Test
    void doesNotApply_whenSpecIsValue() {
        var objectModel = mock(UiObjectWkt.class);
        var objectSpec = mock(ObjectSpecification.class);
        when(objectSpec.isSingular()).thenReturn(true);
        when(objectSpec.isValue()).thenReturn(true);
        doReturn(true).when(objectSpec).containsFacet(D3GraphFacet.class);
        when(objectModel.getTypeOfSpecification()).thenReturn(objectSpec);

        var advice = factory.appliesTo(UiComponentType.DOMAIN_OBJECT, objectModel);
        assertThat(advice.applies()).isFalse();
    }

}
