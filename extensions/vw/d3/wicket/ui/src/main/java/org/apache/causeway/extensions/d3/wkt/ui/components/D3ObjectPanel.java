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

import java.io.Serial;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.causeway.extensions.d3.applib.config.D3Config;
import org.apache.causeway.extensions.d3.metamodel.facet.D3GraphFacet;
import org.apache.causeway.extensions.d3.wkt.integration.callback.NodeClickedCallback;
import org.apache.causeway.extensions.d3.wkt.integration.callback.NodeClickedEvent;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.exec.FormExecutorDefault;

/**
 * Wicket panel that renders a domain object whose <b>type</b> is annotated with
 * {@code @D3Graph} (recognised through its {@link D3GraphFacet} on the object's
 * {@link org.apache.causeway.core.metamodel.spec.ObjectSpecification}) as an
 * interactive D3.js force-layout graph.
 * <p>
 * Extends the integration module's {@code D3Panel} (thus reusing its markup,
 * resource references and callback-URL wiring) and adds:
 * <ul>
 * <li>the graph data, resolved through a
 *     {@link org.apache.causeway.extensions.d3.applib.spi.D3GraphAdvisor} (there is
 *     no annotated property to read the data from),</li>
 * <li>an AJAX {@link NodeClickedCallback} that invokes the configured domain
 *     action (its first {@code String} parameter receiving the clicked node's id).</li>
 * </ul>
 *
 * @since 2.0 {@index}
 */
public class D3ObjectPanel extends org.apache.causeway.extensions.d3.wkt.integration.components.D3Panel {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String EMPTY_GRAPH = """
            {"nodes":[],"links":[]}""";

    private final UiObjectWkt objectModel;

    public D3ObjectPanel(final String id, final UiObjectWkt objectModel) {
        super(id, buildConfig(objectModel));
        this.objectModel = objectModel;

        var facet = objectModel.getTypeOfSpecification().lookupFacet(D3GraphFacet.class).orElse(null);
        var clickAction = facet != null
                ? facet.getClickAction(domainClassOf(objectModel)).orElse(null)
                : null;

        if (clickAction != null) {
            withClickCallback(new NodeClickedCallback() {
                @Serial
                private static final long serialVersionUID = 1L;
                @Override
                protected void onNodeClicked(final NodeClickedEvent event, final AjaxRequestTarget target) {
                    D3ObjectPanel.this.onNodeClicked(event, target, clickAction);
                }
            });
        }
    }

    // -- HELPER

    private static D3Config buildConfig(final UiObjectWkt objectModel) {
        var facet = objectModel.getTypeOfSpecification().lookupFacet(D3GraphFacet.class).orElse(null);
        var config = facet != null
                ? facet.configFor(domainClassOf(objectModel))
                : D3Config.defaults();

        // graph data is advisor-provided (merged in by configFor(...));
        // fall back to an empty graph when none is available
        if (isBlank(config.graphData())) {
            config = config.withGraphData(EMPTY_GRAPH);
        }
        return config;
    }

    private static Class<?> domainClassOf(final UiObjectWkt objectModel) {
        return objectModel.getManagedObject().getCorrespondingClass();
    }

    private static boolean isBlank(final CharSequence cs) {
        return cs == null || cs.toString().isBlank();
    }

    private void onNodeClicked(final NodeClickedEvent event, final AjaxRequestTarget target,
            final String clickActionId) {
        var objectSpec = objectModel.getManagedObject().objSpec();
        var action = objectSpec.getAction(clickActionId)
                .orElseThrow(() -> new RuntimeException(
                        "D3 node-click action '" + clickActionId + "' not found on "
                        + objectSpec.logicalTypeName()));

        var actionModel = ActionModel.forEntity(action, objectModel);
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
