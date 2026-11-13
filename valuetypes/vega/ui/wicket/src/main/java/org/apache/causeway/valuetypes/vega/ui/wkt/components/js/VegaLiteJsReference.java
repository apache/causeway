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
package org.apache.causeway.valuetypes.vega.ui.wkt.components.js;

import org.apache.causeway.viewer.commons.model.webjar.WebJar;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Provides a local copy of {@linkplain "https://cdn.jsdelivr.net/npm/vega-lite@5.9.1"}
 * <p>
 * LICENSE <a href="https://vega.github.io/vega/vega/blob/main/LICENSE">BSD-3-Clause license</a>
 *
 * @see "https://vega.github.io/vega-lite/usage/embed.html"
 * @since 2.0
 */
public class VegaLiteJsReference extends WebjarsJavaScriptResourceReference {

    private static final long serialVersionUID = 1L;

    @Getter(lazy = true) @Accessors(fluent = true)
    private static final VegaLiteJsReference instance =
        new VegaLiteJsReference();

    private VegaLiteJsReference() {
        super(WebJar.VEGA_LITE_JS.resource());
    }

    /**
     * @return this resource reference singleton instance as header item
     */
    public static HeaderItem asHeaderItem() {
        return JavaScriptHeaderItem.forReference(instance());
    }

}
