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
package org.apache.causeway.applib.value.semantics;

import org.apache.causeway.commons.internal.base._Strings;

/**
 * @since 2.x {@index}
 */
public interface Renderer<T> {

    /**
     * The value in its read-only summarizing text presentation form. (title form)
     */
    String titlePresentation(ValueSemanticsProvider.Context context, T value);

    /**
     * The value rendered as HTML.
     * <p>
     * Default implementation uses the 'escaped' titlePresentation.
     * Override for custom HTML, but be aware of potential XSS attack vectors.
     */
    default String htmlPresentation(final ValueSemanticsProvider.Context context, final T value) {
        return _Strings.htmlEscape(titlePresentation(context, value));
    }

    public static enum SyntaxHighlighter {
        NONE,
        /** <i>Prism<> with 'default' theme */
        PRISM_DEFAULT,
        /** <i>Prism<> with 'coy' theme */
        PRISM_COY;
    }

    /**
     * Governs whether, to switch on client-side syntax highlighting.
     * @apiNote a rendered page can currently only support a single high-lighter theme;
     * if there is a mix of themes the behavior is unpredictable
     */
    default SyntaxHighlighter syntaxHighlighter() {
        return SyntaxHighlighter.NONE;
    }

}
