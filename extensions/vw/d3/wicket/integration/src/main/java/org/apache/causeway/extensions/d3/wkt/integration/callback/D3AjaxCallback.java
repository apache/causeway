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

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;

import org.apache.causeway.extensions.d3.wkt.integration.components.D3Panel;

import java.io.Serial;

/**
 * Base class for AJAX callbacks issued by the D3 viewer.
 * <p>
 * The {@code <PLACEHOLDER>} token is appended to the callback URL, so that the
 * client-side JavaScript can substitute it with dynamic parameters (for example
 * the clicked node's id), mirroring the fullcalendar extension's pattern.
 * </p>
 */
abstract class D3AjaxCallback extends AbstractDefaultAjaxBehavior {

    @Serial
    private static final long serialVersionUID = 1L;

    static final String PLACEHOLDER = "<PLACEHOLDER>";

    @Override
    public final CharSequence getCallbackUrl() {
        return super.getCallbackUrl() + PLACEHOLDER;
    }

    protected D3Panel getD3Panel() {
        return (D3Panel) getComponent();
    }

}
