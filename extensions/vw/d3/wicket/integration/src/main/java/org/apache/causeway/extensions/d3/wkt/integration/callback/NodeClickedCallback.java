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
package org.apache.causeway.extensions.d3.wkt.integration.callback;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.Request;

import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

/**
 * Callback that handles a D3 node click event via AJAX.
 * <p>
 * The callback URL includes a placeholder that the client-side JavaScript
 * replaces with the clicked node's id at runtime.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Slf4j
public abstract class NodeClickedCallback
extends D3AjaxCallback {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void respond(final AjaxRequestTarget target) {
        final Request request = getD3Panel().getRequest();
        final var nodeIdParam = request.getRequestParameters().getParameterValue("nodeId");
        if (nodeIdParam == null || nodeIdParam.isEmpty()) {
            log.warn("D3 node-click callback invoked without a 'nodeId' request parameter; ignoring.");
            return;
        }
        onNodeClicked(new NodeClickedEvent(nodeIdParam.toString()), target);
    }

    /**
     * Invoked when a node click event arrives from the client.
     *
     * @param event the event carrying the clicked node's id
     * @param target the AJAX request target
     */
    protected abstract void onNodeClicked(NodeClickedEvent event, AjaxRequestTarget target);
}
