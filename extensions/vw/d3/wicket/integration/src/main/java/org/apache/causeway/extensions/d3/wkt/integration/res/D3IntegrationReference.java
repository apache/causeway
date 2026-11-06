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
package org.apache.causeway.extensions.d3.wkt.integration.res;

import org.apache.causeway.extensions.d3.applib.config.D3Config;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceType;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serial;

public class D3IntegrationReference
extends JavaScriptResourceReference {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter(lazy = true) @Accessors(fluent = true)
    private static final D3IntegrationReference instance =
        new D3IntegrationReference();

    private D3IntegrationReference() {
        super(D3IntegrationReference.class, "d3-integration.mjs");
    }

    /**
     * @return this resource reference singleton instance as header item
     */
    public static HeaderItem asHeaderItem() {
        return JavaScriptHeaderItem.forReference(instance())
                .setType(JavaScriptReferenceType.MODULE);
    }

    public static OnDomReadyHeaderItem domReadyScript(
            final D3Config config) {
        return OnDomReadyHeaderItem.forScript(
                String.format("WicketStuff.D3.init(%s)", escapeJsonForInlineScript(config.toJsonString())));
    }

    /**
     * Makes a JSON document safe for embedding verbatim inside an inline
     * {@code <script>} element.
     * <p>
     * The graph data is serialized to JSON and injected directly into a
     * {@code WicketStuff.D3.init(...)} call. JSON does not escape {@code <},
     * {@code >}, {@code &} or the line separators U+2028/U+2029, so a node
     * label (or any other value) containing {@code </script>} could break out
     * of the script element. Replacing those characters with their equivalent
     * JSON Unicode escape sequences is semantically transparent (the parsed
     * value is unchanged) yet prevents the break-out.
     * </p>
     */
    private static String escapeJsonForInlineScript(final String json) {
        final String backslash = "\\";
        return json
                .replace("&", backslash + "u0026")
                .replace("<", backslash + "u003c")
                .replace(">", backslash + "u003e")
                .replace(String.valueOf((char) 0x2028), backslash + "u2028")
                .replace(String.valueOf((char) 0x2029), backslash + "u2029");
    }

}
