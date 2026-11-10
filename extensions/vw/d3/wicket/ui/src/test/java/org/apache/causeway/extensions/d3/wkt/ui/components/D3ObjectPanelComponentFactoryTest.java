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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.mmtestsupport.MetaModelContext_forTesting;
import org.apache.causeway.extensions.d3.applib.config.D3Config;
import org.apache.causeway.extensions.d3.metamodel.facet.D3GraphFacet;
import org.apache.causeway.extensions.d3.wkt.integration.callback.NodeClickedCallback;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.FormExecutor;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory.ApplicationAdvice;
import org.apache.causeway.viewer.wicket.ui.exec.FormExecutorDefault;

/**
 * Verifies the {@code appliesTo} logic of {@link D3ObjectPanelComponentFactory}.
 */
@DisplayName("D3ObjectPanelComponentFactory — applicability & rendering")
class D3ObjectPanelComponentFactoryTest {

    private final D3ObjectPanelComponentFactory factory = new D3ObjectPanelComponentFactory();

    private MetaModelContext mmc;

    @BeforeEach
    void setUp() {
        mmc = MetaModelContext_forTesting.builder()
                .memberExecutor(mock(MemberExecutorService.class))
                .build();
    }

    private static UiObjectWkt objectModel(final boolean hasD3GraphFacet) {
        var objectModel = mock(UiObjectWkt.class);
        var objectSpec = mock(ObjectSpecification.class);
        when(objectSpec.isSingular()).thenReturn(true);
        when(objectSpec.isValue()).thenReturn(false);
        doReturn(hasD3GraphFacet).when(objectSpec).containsFacet(D3GraphFacet.class);
        when(objectModel.getTypeOfSpecification()).thenReturn(objectSpec);
        return objectModel;
    }

    // -- appliesTo

    @Test
    @DisplayName("applies exclusively when the type has a D3GraphFacet")
    void appliesExclusively_whenTypeHasD3GraphFacet() {
        var advice = factory.appliesTo(UiComponentType.DOMAIN_OBJECT, objectModel(true));
        assertThat(advice).isEqualTo(ApplicationAdvice.APPLIES_EXCLUSIVELY);
    }

    @Test
    @DisplayName("does not apply when the type is missing the D3GraphFacet")
    void doesNotApply_whenTypeMissingD3GraphFacet() {
        var advice = factory.appliesTo(UiComponentType.DOMAIN_OBJECT, objectModel(false));
        assertThat(advice.applies()).isFalse();
    }

    @Test
    @DisplayName("does not apply when the model is not a UiObjectWkt")
    void doesNotApply_whenModelIsNotUiObjectWkt() {
        var advice = factory.appliesTo(UiComponentType.DOMAIN_OBJECT, mock(IModel.class));
        assertThat(advice.applies()).isFalse();
    }

    @Test
    @DisplayName("does not apply when the specification is a value")
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

    // -- createComponent

    /** An object model whose specification exposes a {@link D3GraphFacet} returning the given config. */
    private UiObjectWkt objectModelWithConfig(final D3Config config) {
        var objectModel = mock(UiObjectWkt.class);
        var objectSpec = mock(ObjectSpecification.class);
        when(objectSpec.isSingular()).thenReturn(true);
        when(objectSpec.isValue()).thenReturn(false);
        doReturn(true).when(objectSpec).containsFacet(D3GraphFacet.class);
        when(objectModel.getTypeOfSpecification()).thenReturn(objectSpec);

        var d3GraphFacet = mock(D3GraphFacet.class);
        doReturn(Optional.of(d3GraphFacet)).when(objectSpec).lookupFacet(D3GraphFacet.class);
        when(d3GraphFacet.configFor(any())).thenReturn(config);
        when(d3GraphFacet.getClickAction(any())).thenReturn(Optional.empty());

        var objSpec = mmc.getSpecificationLoader().loadSpecification(D3ObjectPanelComponentFactoryTest.class);
        when(objectModel.getManagedObject())
                .thenReturn(ManagedObject.adaptSingular(objSpec, new D3ObjectPanelComponentFactoryTest()));
        return objectModel;
    }

    /** An object model whose specification exposes no {@link D3GraphFacet}. */
    private UiObjectWkt objectModelWithoutFacet() {
        var objectModel = mock(UiObjectWkt.class);
        var objectSpec = mock(ObjectSpecification.class);
        when(objectSpec.isSingular()).thenReturn(true);
        when(objectSpec.isValue()).thenReturn(false);
        when(objectModel.getTypeOfSpecification()).thenReturn(objectSpec);
        // objectSpec.lookupFacet(...) is not stubbed => Optional.empty() => facet absent
        return objectModel;
    }

    /** Renders the created panel as a full page and returns the markup. */
    private String renderPanel(final UiObjectWkt objectModel) {
        var tester = new WicketTester(new D3PanelComponentFactoryTest.WicketAppStup());
        try {
            var component = factory.createComponent("d3", objectModel);
            assertThat(component).isInstanceOf(D3ObjectPanel.class);
            tester.startPage(new D3TestPage(component));
            return tester.getLastResponseAsString();
        } finally {
            tester.destroy();
        }
    }

    @Test
    @DisplayName("renders the EMPTY_GRAPH when no graph data is available")
    void createComponent_rendersEmptyGraph_whenNoGraphDataAvailable() {
        var response = renderPanel(objectModelWithConfig(D3Config.defaults()));

        assertThat(response).contains("WicketStuff.D3.init(");
        assertThat(response.replace("\\\"", "\"")).contains("{\"nodes\":[],\"links\":[]}");
    }

    @Test
    @DisplayName("renders the advisor-provided graph data")
    void createComponent_rendersAdvisorGraphData() {
        var config = D3Config.defaults().withGraphData("{\"nodes\":[{\"id\":\"ADVISOR\"}]}");

        var response = renderPanel(objectModelWithConfig(config));

        assertThat(response).contains("ADVISOR");
    }

    @Test
    @DisplayName("falls back to the defaults (and EMPTY_GRAPH) when the facet is absent")
    void createComponent_rendersEmptyGraph_whenFacetAbsent() {
        var response = renderPanel(objectModelWithoutFacet());

        assertThat(response).contains("WicketStuff.D3.init(");
        assertThat(response.replace("\\\"", "\"")).contains("{\"nodes\":[],\"links\":[]}");
        assertThat(response).doesNotContain("PLACEHOLDER");
    }

    @Test
    @DisplayName("binds a click callback (emits the callback URL) when a click action is configured")
    void rendersCallbackUrl_whenClickActionBound() {
        var response = renderPanel(objectModelWithClickAction(hostObject(), "selectNode"));

        assertThat(response).contains("clickCallbackUrl");
        assertThat(response).contains("PLACEHOLDER");
    }

    @Test
    @DisplayName("invokes the configured domain action, binding the clicked node id, on node click")
    void invokesDomainAction_onNodeClicked() {
        var action = hostSpec().getAction("selectNode").orElseThrow();
        var objectModel = objectModelWithClickAction(hostObject(), "selectNode");

        var pendingArgs = mock(ParameterNegotiationModel.class);
        when(pendingArgs.getParamCount()).thenReturn(1);
        var actionInteraction = mock(ActionInteraction.class);
        when(actionInteraction.startParameterNegotiation()).thenReturn(Optional.of(pendingArgs));
        var actionModel = mock(ActionModel.class);
        when(actionModel.getActionInteraction()).thenReturn(actionInteraction);
        var formExecutor = mock(FormExecutor.class);

        var tester = new WicketTester(new D3PanelComponentFactoryTest.WicketAppStup());
        try {
            var panel = (D3ObjectPanel) factory.createComponent("d3", objectModel);
            tester.startPage(new D3TestPage(panel));
            var callback = panel.getBehaviors(NodeClickedCallback.class).get(0);

            try (var actionModelStatic = mockStatic(ActionModel.class);
                 var formExecutorStatic = mockStatic(FormExecutorDefault.class)) {

                actionModelStatic.when(() -> ActionModel.forEntity(action, objectModel)).thenReturn(actionModel);
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

    private UiObjectWkt objectModelWithClickAction(final ManagedObject hostObject, final String clickAction) {
        var objectModel = mock(UiObjectWkt.class);
        var objectSpec = mock(ObjectSpecification.class);
        when(objectSpec.isSingular()).thenReturn(true);
        when(objectSpec.isValue()).thenReturn(false);
        doReturn(true).when(objectSpec).containsFacet(D3GraphFacet.class);
        when(objectModel.getTypeOfSpecification()).thenReturn(objectSpec);

        var d3GraphFacet = mock(D3GraphFacet.class);
        doReturn(Optional.of(d3GraphFacet)).when(objectSpec).lookupFacet(D3GraphFacet.class);
        when(d3GraphFacet.configFor(any())).thenReturn(D3Config.defaults());
        when(d3GraphFacet.getClickAction(any())).thenReturn(Optional.of(clickAction));

        when(objectModel.getManagedObject()).thenReturn(hostObject);
        return objectModel;
    }

    private static Url callbackUrl(final NodeClickedCallback callback, final String nodeId) {
        var raw = callback.getCallbackUrl().toString().replace("<PLACEHOLDER>", "");
        var url = Url.parse(raw, StandardCharsets.UTF_8);
        url.getQueryParameters().add(new Url.QueryParameter("nodeId", nodeId));
        return url;
    }

}
