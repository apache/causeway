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
package org.apache.causeway.viewer.wicket.ui.components.scalars.markup;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.value.semantics.Renderer.SyntaxHighlighter;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.viewer.commons.prism.Prism;

interface _HighlightBehavior {

    void renderHead(final IHeaderResponse response);
    CharSequence htmlContentPostProcess(final CharSequence htmlContent);

    public static _HighlightBehavior NONE = new _HighlightBehavior() {
        @Override public void renderHead(final IHeaderResponse response) { }
        @Override public CharSequence htmlContentPostProcess(final CharSequence htmlContent) {
            return htmlContent;
        }
    };

    public static _HighlightBehavior valueOf(final @Nullable SyntaxHighlighter syntaxHighlighter) {
        if(syntaxHighlighter==null) {
            return NONE;
        }
        switch(syntaxHighlighter) {
        case PRISM_DEFAULT:
            return new _HighlightBehaviorPrism(Prism.DEFAULT);
        case PRISM_COY:
            return new _HighlightBehaviorPrism(Prism.COY);
        case NONE:
            return NONE;
        default:
            throw _Exceptions.unmatchedCase(syntaxHighlighter);
        }
    }

}
