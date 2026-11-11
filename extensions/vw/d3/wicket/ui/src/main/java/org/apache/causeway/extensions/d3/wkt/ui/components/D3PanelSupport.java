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
import org.apache.causeway.extensions.d3.wkt.integration.callback.NodeClickedEvent;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.exec.FormExecutorDefault;

/**
 * Shared behaviour for the D3 graph panels ({@link D3Panel} for a
 * {@code @D3Graph}-annotated property and {@link D3ObjectPanel} for a
 * {@code @D3Graph}-annotated object type), which differ only in how the owning
 * object model is obtained.
 */
final class D3PanelSupport {

    private D3PanelSupport() {
    }

    /**
     * Resolves the domain action identified by {@code clickActionId} on the owning
     * object, binds the clicked node's id to its first parameter (if any) and
     * executes it through the standard Wicket viewer action pipeline.
     */
    static void invokeClickAction(
            final NodeClickedEvent event,
            final AjaxRequestTarget target,
            final String clickActionId,
            final UiObjectWkt ownerModel) {

        var objectSpec = ownerModel.getManagedObject().objSpec();
        var action = objectSpec.getAction(clickActionId)
                .orElseThrow(() -> new RuntimeException(
                        "D3 node-click action '" + clickActionId + "' not found on "
                        + objectSpec.logicalTypeName()));

        var actionModel = ActionModel.forEntity(action, ownerModel);
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

    static boolean isBlank(final CharSequence cs) {
        return cs == null || cs.toString().isBlank();
    }

    static Class<?> domainClassOf(final ManagedObject managedObject) {
        return managedObject.getCorrespondingClass();
    }
}
