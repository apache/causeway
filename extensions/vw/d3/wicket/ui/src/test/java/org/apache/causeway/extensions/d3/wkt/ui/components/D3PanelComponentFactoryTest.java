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

import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.agilecoders.wicket.webjars.WicketWebjars;

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
    void applies_whenAttributeHasD3GraphFacet() {
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
    void applies_whenPropertyValueIsBlank() {
        var attributeModel = attributeModel(true);
        when(attributeModel.getObject()).thenReturn(managedString("  "));

        var advice = factory.appliesTo(attributeModel);
        assertThat(advice.applies()).isTrue();
    }

    @Test
    void applies_whenPropertyValueIsNull() {
        var attributeModel = attributeModel(true);
        when(attributeModel.getObject()).thenReturn(null);

        var advice = factory.appliesTo(attributeModel);
        assertThat(advice.applies()).isTrue();
    }

    @Test
    void applies_whenPropertyValueIsNotCharSequence() {
        var attributeModel = attributeModel(true);
        var spec = mmc.getSpecificationLoader().loadSpecification(Integer.class);
        when(attributeModel.getObject()).thenReturn(ManagedObject.adaptSingular(spec, 123));

        var advice = factory.appliesTo(attributeModel);
        assertThat(advice.applies()).isTrue();
    }

    @Test
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

        // WicketTester provides the full Wicket infrastructure (Application, Session, RequestCycle)
        // needed to construct and render the D3Panel component
        var tester = new WicketTester(new WicketAppStup());
        try {
            var component = factory.createComponent("d3", attributeModel);
            tester.startComponentInPage(component);
            assertThat(component).isInstanceOf(D3Panel.class);
        } finally {
            tester.destroy();
        }
    }

    /**
     * Minimal Wicket {@link WebApplication} that enables Wicket infrastructure
     * (Session, RequestCycle) without a servlet container, as required by
     * {@link WicketTester} and the D3Panel component hierarchy.
     */
    static class WicketAppStup extends WebApplication {
        @Override
        protected void init() {
            super.init();
            // Required before any WebjarsJavaScriptResourceReference (e.g. D3Reference) can be used
            WicketWebjars.install(this);
            // Allows *.mjs, otherwise Wicket throws 'Access denied to (static) package resource'
            var resourceGuard =
                    (SecurePackageResourceGuard) getResourceSettings().getPackageResourceGuard();
            resourceGuard.addPattern("+*.mjs");
            // Allows *.css loaded via CssResourceReference
            resourceGuard.addPattern("+*.css");
        }

        @Override
        public RuntimeConfigurationType getConfigurationType() {
            return RuntimeConfigurationType.DEVELOPMENT;
        }

        @Override
        public Class<? extends Page> getHomePage() {
            return null;
        }
    }
}
