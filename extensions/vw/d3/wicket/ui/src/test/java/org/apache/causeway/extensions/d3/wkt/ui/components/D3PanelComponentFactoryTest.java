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

import java.util.Optional;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.extensions.d3.applib.config.D3Config;
import org.apache.causeway.extensions.d3.metamodel.facet.D3GraphFacet;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory.ApplicationAdvice;

class D3PanelComponentFactoryTest {

    private MetaModelContext mmc;
    private D3PanelComponentFactory factory;

    @BeforeEach
    void setUp() {
        mmc = org.apache.causeway.core.mmtestsupport.MetaModelContext_forTesting.buildDefault();
        factory = new D3PanelComponentFactory();
        ThreadContext.setApplication(new WicketAppStup());
    }

    @AfterEach
    void tearDown() {
        ThreadContext.detach();
    }

    private ManagedObject managedString(final String value) {
        var spec = mmc.getSpecificationLoader().loadSpecification(String.class);
        return ManagedObject.adaptSingular(spec, value);
    }

    private static UiAttributeWkt attributeModel(final boolean hasFacet) {
        var attributeModel = mock(UiAttributeWkt.class);
        var feature = mock(ObjectFeature.class);
        doReturn(hasFacet).when(feature).containsFacet(D3GraphFacet.class);
        when(attributeModel.getMetaModel()).thenReturn(feature);
        return attributeModel;
    }

    @Test
    void applies_whenAttributeHasD3GraphFacetAndNonBlankJsonString() {
        var attributeModel = attributeModel(true);
        when(attributeModel.getObject()).thenReturn(managedString("{\"nodes\":[],\"links\":[]}"));

        var advice = factory.appliesTo(attributeModel);
        assertThat(advice).isEqualTo(ApplicationAdvice.APPLIES);
    }

    @Test
    void doesNotApply_whenModelIsNotUiAttributeWkt() {
        var advice = factory.appliesTo(mock(IModel.class));
        assertThat(advice.applies()).isFalse();
    }

    @Test
    void doesNotApply_whenMissingD3GraphFacet() {
        var attributeModel = attributeModel(false);

        var advice = factory.appliesTo(attributeModel);
        assertThat(advice.applies()).isFalse();
    }

    @Test
    void doesNotApply_whenPropertyValueIsBlank() {
        var attributeModel = attributeModel(true);
        when(attributeModel.getObject()).thenReturn(managedString("  "));

        var advice = factory.appliesTo(attributeModel);
        assertThat(advice.applies()).isFalse();
    }

    @Test
    void doesNotApply_whenPropertyValueIsNull() {
        var attributeModel = attributeModel(true);
        when(attributeModel.getObject()).thenReturn(null);

        var advice = factory.appliesTo(attributeModel);
        assertThat(advice.applies()).isFalse();
    }

    @Test
    void doesNotApply_whenPropertyValueIsNotCharSequence() {
        var attributeModel = attributeModel(true);
        var spec = mmc.getSpecificationLoader().loadSpecification(Integer.class);
        when(attributeModel.getObject()).thenReturn(ManagedObject.adaptSingular(spec, 123));

        var advice = factory.appliesTo(attributeModel);
        assertThat(advice.applies()).isFalse();
    }

    // panel construction requires wicket-tester (see status doc)
    //TODO add wicket-tester test dependency and re-enable
    //@Test
    void createComponent_returnsD3Panel() {
        var attributeModel = attributeModel(true);
        when(attributeModel.getObject()).thenReturn(managedString("{\"nodes\":[],\"links\":[]}"));

        var parentUiModel = mock(UiObjectWkt.class);
        when(attributeModel.getParentUiModel()).thenReturn(parentUiModel);
        var objSpec = mmc.getSpecificationLoader().loadSpecification(D3PanelComponentFactoryTest.class);
        var managedObject = ManagedObject.adaptSingular(objSpec, new D3PanelComponentFactoryTest());
        when(parentUiModel.getManagedObject()).thenReturn(managedObject);

        var feature = attributeModel.getMetaModel();
        var d3GraphFacet = mock(D3GraphFacet.class);
        doReturn(Optional.of(d3GraphFacet)).when(feature).lookupFacet(D3GraphFacet.class);
        when(d3GraphFacet.configFor(D3PanelComponentFactoryTest.class)).thenReturn(new D3Config());
        when(d3GraphFacet.getClickAction(D3PanelComponentFactoryTest.class)).thenReturn(Optional.empty());

        var component = factory.createComponent("d3", attributeModel);
        assertThat(component).isInstanceOf(D3Panel.class);
    }

    /** minimal Wicket Application so that {@code new D3Panel(...)} works without a servlet container */
    static class WicketAppStup extends Application {
        @Override
        public String getApplicationKey() {
            return "test";
        }

        @Override
        public RuntimeConfigurationType getConfigurationType() {
            return RuntimeConfigurationType.DEVELOPMENT;
        }

        @Override
        public Class<? extends Page> getHomePage() {
            return null;
        }

        @Override
        public Session newSession(final Request request, final Response response) {
            return null;
        }
    }
}
