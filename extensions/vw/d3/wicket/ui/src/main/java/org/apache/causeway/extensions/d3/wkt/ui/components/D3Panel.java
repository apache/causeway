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

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.extensions.d3.applib.config.D3Config;
import org.apache.causeway.extensions.d3.metamodel.facet.D3GraphFacet;
import org.apache.causeway.extensions.d3.wkt.integration.callback.NodeClickedCallback;
import org.apache.causeway.extensions.d3.wkt.integration.callback.NodeClickedEvent;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.exec.FormExecutorDefault;

import java.io.Serial;

/**
 * Wicket panel that renders a {@code @D3Graph}-annotated property
 * (recognised through its {@link D3GraphFacet})
 * as an interactive D3.js force-layout graph.
 * <p>
 * Extends the integration module's {@code D3Panel} (thus reusing its markup,
 * resource references and callback-URL wiring) and adds:
 * <ul>
 * <li>the graph data (JSON string) taken from the annotated property's value,</li>
 * <li>an AJAX {@link NodeClickedCallback} that invokes the configured domain
 *     action (its first {@code String} parameter receiving the clicked node's id).</li>
 * </ul>
 *
 * @since 2.0 {@index}
 */
public class D3Panel extends org.apache.causeway.extensions.d3.wkt.integration.components.D3Panel {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String EMPTY_GRAPH = "{\"nodes\":[],\"links\":[]}";

    private final UiAttributeWkt attributeModel;

    public D3Panel(final String id, final UiAttributeWkt attributeModel) {
        super(id, buildConfig(attributeModel));
        this.attributeModel = attributeModel;

        var domainClass = domainClassOf(attributeModel);
        var facet = attributeModel.getMetaModel().lookupFacet(D3GraphFacet.class).orElse(null);
        var clickAction = facet != null
                ? facet.getClickAction(domainClass).orElse(null)
                : null;

        if (clickAction != null) {
            withClickCallback(new NodeClickedCallback() {
                @Serial
                private static final long serialVersionUID = 1L;
                @Override
                protected void onNodeClicked(final NodeClickedEvent event, final AjaxRequestTarget target) {
                    D3Panel.this.onNodeClicked(event, target, clickAction);
                }
            });
        }
    }

    // -- HELPER

    private static D3Config buildConfig(final UiAttributeWkt attributeModel) {
        var domainClass = domainClassOf(attributeModel);
        var facet = attributeModel.getMetaModel().lookupFacet(D3GraphFacet.class).orElse(null);
        var config = facet != null
                ? facet.configFor(domainClass)
                : new D3Config();

        // graph data is the annotated property's value (a JSON string);
        // fall back to an empty graph when the value is null or blank
        var pojo = attributeModel.getObject().getPojo();
        if (pojo instanceof CharSequence cs && !cs.toString().isBlank()) {
            config = config.withGraphData(cs);
        } else {
            config = config.withGraphData(EMPTY_GRAPH);
        }
        return config;
    }

    private static Class<?> domainClassOf(final UiAttributeWkt attributeModel) {
        return parentObjectOf(attributeModel).getCorrespondingClass();
    }

    private static ManagedObject parentObjectOf(final UiAttributeWkt attributeModel) {
        return attributeModel.getParentUiModel().getManagedObject();
    }

    private void onNodeClicked(final NodeClickedEvent event, final AjaxRequestTarget target,
            final String clickActionId) {
        var parentUiModel = attributeModel.getParentUiModel();
        var objectSpec = parentUiModel.getManagedObject().objSpec();
        var action = objectSpec.getAction(clickActionId)
                .orElseThrow(() -> new RuntimeException(
                        "D3 node-click action '" + clickActionId + "' not found on "
                        + objectSpec.logicalTypeName()));

        var actionModel = ActionModel.forEntity(action, parentUiModel);
        var pendingArgs = actionModel.getActionInteraction().startParameterNegotiation()
                .orElseThrow(() -> new RuntimeException(
                        "could not start parameter negotiation for D3 node-click action '"
                        + clickActionId + "'"));
        if (pendingArgs.getParamCount() >= 1) {
            pendingArgs.updateParamValuePojo(0, __ -> event.nodeId());
        }

        FormExecutorDefault.forAction(actionModel)
                .executeAndProcessResults(target, null, actionModel);
    }

}
