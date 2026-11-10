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

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.agilecoders.wicket.webjars.WicketWebjars;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.mmtestsupport.MetaModelContext_forTesting;
import org.apache.causeway.extensions.d3.applib.config.D3Config;
import org.apache.causeway.extensions.d3.metamodel.facet.D3GraphFacet;
import org.apache.causeway.extensions.d3.wkt.integration.callback.NodeClickedCallback;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.FormExecutor;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory.ApplicationAdvice;
import org.apache.causeway.viewer.wicket.ui.exec.FormExecutorDefault;

@DisplayName("D3PanelComponentFactory — applicability & rendering")
class D3PanelComponentFactoryTest {

    private MetaModelContext mmc;
    private D3PanelComponentFactory factory;

    @BeforeEach
    void setUp() {
        mmc = MetaModelContext_forTesting.builder()
                .memberExecutor(mock(MemberExecutorService.class))
                .build();
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

    private ManagedObject managedInt(final int value) {
        var spec = mmc.getSpecificationLoader().loadSpecification(Integer.class);
        return ManagedObject.adaptSingular(spec, value);
    }

    private static UiAttributeWkt attributeModel(final boolean hasFacet) {
        var attributeModel = mock(UiAttributeWkt.class);
        var feature = mock(ObjectFeature.class);
        doReturn(hasFacet).when(feature).containsFacet(D3GraphFacet.class);
        when(attributeModel.getMetaModel()).thenReturn(feature);
        return attributeModel;
    }

    // -- appliesTo

    @Test
    @DisplayName("applies when the attribute has a D3GraphFacet")
    void applies_whenAttributeHasD3GraphFacet() {
        var attributeModel = attributeModel(true);
        when(attributeModel.getObject()).thenReturn(managedString("{\"nodes\":[],\"links\":[]}"));

        var advice = factory.appliesTo(attributeModel);
        assertThat(advice).isEqualTo(ApplicationAdvice.APPLIES);
    }

    @Test
    @DisplayName("does not apply when the model is not a UiAttributeWkt")
    void doesNotApply_whenModelIsNotUiAttributeWkt() {
        var advice = factory.appliesTo(mock(IModel.class));
        assertThat(advice.applies()).isFalse();
    }

    @Test
    @DisplayName("does not apply when the D3GraphFacet is missing")
    void doesNotApply_whenMissingD3GraphFacet() {
        var attributeModel = attributeModel(false);

        var advice = factory.appliesTo(attributeModel);
        assertThat(advice.applies()).isFalse();
    }

    @Test
    @DisplayName("applies when the property value is blank")
    void applies_whenPropertyValueIsBlank() {
        var attributeModel = attributeModel(true);
        when(attributeModel.getObject()).thenReturn(managedString("  "));

        var advice = factory.appliesTo(attributeModel);
        assertThat(advice.applies()).isTrue();
    }

    @Test
    @DisplayName("applies when the property value is null")
    void applies_whenPropertyValueIsNull() {
        var attributeModel = attributeModel(true);
        when(attributeModel.getObject()).thenReturn(null);

        var advice = factory.appliesTo(attributeModel);
        assertThat(advice.applies()).isTrue();
    }

    @Test
    @DisplayName("applies when the property value is not a CharSequence")
    void applies_whenPropertyValueIsNotCharSequence() {
        var attributeModel = attributeModel(true);
        var spec = mmc.getSpecificationLoader().loadSpecification(Integer.class);
        when(attributeModel.getObject()).thenReturn(ManagedObject.adaptSingular(spec, 123));

        var advice = factory.appliesTo(attributeModel);
        assertThat(advice.applies()).isTrue();
    }

    // -- createComponent

    @Test
    @DisplayName("createComponent(...) returns a D3Panel")
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
        when(d3GraphFacet.configFor(D3PanelComponentFactoryTest.class)).thenReturn(D3Config.defaults());
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

    /** Builds an attribute model whose property value and facet-provided config are given. */
    private UiAttributeWkt attributeModelWith(final ManagedObject value, final D3Config config) {
        var attributeModel = attributeModel(true);
        when(attributeModel.getObject()).thenReturn(value);

        var parentUiModel = mock(UiObjectWkt.class);
        when(attributeModel.getParentUiModel()).thenReturn(parentUiModel);
        var objSpec = mmc.getSpecificationLoader().loadSpecification(D3PanelComponentFactoryTest.class);
        when(parentUiModel.getManagedObject())
                .thenReturn(ManagedObject.adaptSingular(objSpec, new D3PanelComponentFactoryTest()));

        var feature = attributeModel.getMetaModel();
        var d3GraphFacet = mock(D3GraphFacet.class);
        doReturn(Optional.of(d3GraphFacet)).when(feature).lookupFacet(D3GraphFacet.class);
        when(d3GraphFacet.configFor(D3PanelComponentFactoryTest.class)).thenReturn(config);
        when(d3GraphFacet.getClickAction(D3PanelComponentFactoryTest.class)).thenReturn(Optional.empty());
        return attributeModel;
    }

    /** An attribute model whose {@code getMetaModel()} exposes no {@link D3GraphFacet}. */
    private UiAttributeWkt attributeModelWithoutFacet(final ManagedObject value) {
        var attributeModel = attributeModel(false);
        when(attributeModel.getObject()).thenReturn(value);

        var parentUiModel = mock(UiObjectWkt.class);
        when(attributeModel.getParentUiModel()).thenReturn(parentUiModel);
        var objSpec = mmc.getSpecificationLoader().loadSpecification(D3PanelComponentFactoryTest.class);
        when(parentUiModel.getManagedObject())
                .thenReturn(ManagedObject.adaptSingular(objSpec, new D3PanelComponentFactoryTest()));

        // getMetaModel().lookupFacet(...) is not stubbed => Optional.empty() => facet absent
        return attributeModel;
    }

    /** Renders the panel as a full page (so the header contribution is included) and returns the markup. */
    private String renderPanel(final UiAttributeWkt attributeModel) {
        var tester = new WicketTester(new WicketAppStup());
        try {
            tester.startPage(new D3TestPage(factory.createComponent("d3", attributeModel)));
            return tester.getLastResponseAsString();
        } finally {
            tester.destroy();
        }
    }

    @Test
    @DisplayName("renders the EMPTY_GRAPH when no graph data is available")
    void rendersEmptyGraph_whenNoGraphDataAvailable() {
        // non-CharSequence property value and a config without graph data => EMPTY_GRAPH fallback
        var attributeModel = attributeModelWith(managedInt(123), D3Config.defaults());

        var response = renderPanel(attributeModel);

        assertThat(response).contains("WicketStuff.D3.init(");
        assertThat(response.replace("\\\"", "\"")).contains("{\"nodes\":[],\"links\":[]}");
    }

    @Test
    @DisplayName("renders the property value as graph data")
    void rendersPropertyValue_asGraphData() {
        var attributeModel = attributeModelWith(
                managedString("{\"nodes\":[{\"id\":\"MARKER\"}],\"links\":[]}"), D3Config.defaults());

        var response = renderPanel(attributeModel);

        assertThat(response).contains("MARKER");
    }

    @Test
    @DisplayName("falls back to the defaults (and EMPTY_GRAPH) when the facet is absent")
    void rendersDefaults_whenFacetAbsent() {
        var attributeModel = attributeModelWithoutFacet(managedInt(123));

        var response = renderPanel(attributeModel);

        assertThat(response).contains("WicketStuff.D3.init(");
        assertThat(response.replace("\\\"", "\"")).contains("{\"nodes\":[],\"links\":[]}");
        assertThat(response).doesNotContain("PLACEHOLDER");
    }

    @Test
    @DisplayName("binds a click callback (emits the callback URL) when a click action is configured")
    void rendersCallbackUrl_whenClickActionBound() {
        var attributeModel = attributeModelForClickAction(hostObject(), "selectNode");

        var response = renderPanel(attributeModel);

        assertThat(response).contains("clickCallbackUrl");
        assertThat(response).contains("PLACEHOLDER");
    }

    @Test
    @DisplayName("invokes the configured domain action, binding the clicked node id, on node click")
    void invokesDomainAction_onNodeClicked() {
        var action = hostSpec().getAction("selectNode").orElseThrow();
        var attributeModel = attributeModelForClickAction(hostObject(), "selectNode");
        var parentUiModel = attributeModel.getParentUiModel();

        var pendingArgs = mock(ParameterNegotiationModel.class);
        when(pendingArgs.getParamCount()).thenReturn(1);
        var actionInteraction = mock(ActionInteraction.class);
        when(actionInteraction.startParameterNegotiation()).thenReturn(Optional.of(pendingArgs));
        var actionModel = mock(ActionModel.class);
        when(actionModel.getActionInteraction()).thenReturn(actionInteraction);
        var formExecutor = mock(FormExecutor.class);

        var tester = new WicketTester(new WicketAppStup());
        try {
            var panel = (D3Panel) factory.createComponent("d3", attributeModel);
            tester.startPage(new D3TestPage(panel));
            var callback = panel.getBehaviors(NodeClickedCallback.class).get(0);

            try (var actionModelStatic = mockStatic(ActionModel.class);
                 var formExecutorStatic = mockStatic(FormExecutorDefault.class)) {

                actionModelStatic.when(() -> ActionModel.forEntity(action, parentUiModel)).thenReturn(actionModel);
                formExecutorStatic.when(() -> FormExecutorDefault.forAction(actionModel)).thenReturn(formExecutor);

                tester.executeAjaxUrl(callbackUrl(callback, "node-42"));

                verify(pendingArgs).updateParamValuePojo(eq(0), any());
                verify(formExecutor).executeAndProcessResults(any(), isNull(), eq(actionModel));
            }
        } finally {
            tester.destroy();
        }
    }

    // -- click-action fixtures & helpers

    @DomainObject
    public static class GraphHost {
        public String selectNode(final String nodeId) {
            return nodeId;
        }
    }

    private ObjectSpecification hostSpec() {
        return mmc.getSpecificationLoader().loadSpecification(GraphHost.class);
    }

    private ManagedObject hostObject() {
        return ManagedObject.adaptSingular(hostSpec(), new GraphHost());
    }

    private UiAttributeWkt attributeModelForClickAction(final ManagedObject hostObject, final String clickAction) {
        var attributeModel = attributeModel(true);
        when(attributeModel.getObject()).thenReturn(managedString("{\"nodes\":[],\"links\":[]}"));

        var parentUiModel = mock(UiObjectWkt.class);
        when(attributeModel.getParentUiModel()).thenReturn(parentUiModel);
        when(parentUiModel.getManagedObject()).thenReturn(hostObject);

        var feature = attributeModel.getMetaModel();
        var d3GraphFacet = mock(D3GraphFacet.class);
        doReturn(Optional.of(d3GraphFacet)).when(feature).lookupFacet(D3GraphFacet.class);
        when(d3GraphFacet.configFor(any())).thenReturn(D3Config.defaults());
        when(d3GraphFacet.getClickAction(any())).thenReturn(Optional.of(clickAction));
        return attributeModel;
    }

    private static Url callbackUrl(final NodeClickedCallback callback, final String nodeId) {
        var raw = callback.getCallbackUrl().toString().replace("<PLACEHOLDER>", "");
        var url = Url.parse(raw, StandardCharsets.UTF_8);
        url.getQueryParameters().add(new Url.QueryParameter("nodeId", nodeId));
        return url;
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
