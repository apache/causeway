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
package org.apache.causeway.viewer.commons.model.components;

import java.io.Serializable;

@lombok.Value(staticConstructor = "of")
public class UiString implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Typically to be rendered with a label component, that supports escaping.
     * <p>
     * In other words, given {@code text} must not to be interpreted by the browser, that renders it.
     */
    public static UiString text(final String text) {
        return UiString.of(text, false);
    }

    /**
     * Typically to be rendered with a markup component, to be rendered as is.
     * <p>
     * In other words, given {@code html} must be interpreted by the browser, that renders it.
     */
    public static UiString markup(final String html) {
        return UiString.of(html, true);
    }

    private String string;
    private boolean markup;

}
