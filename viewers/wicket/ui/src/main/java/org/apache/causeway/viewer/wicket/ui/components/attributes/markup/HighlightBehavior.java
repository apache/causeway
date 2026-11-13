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
package org.apache.causeway.viewer.wicket.ui.components.attributes.markup;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.value.semantics.Renderer.SyntaxHighlighter;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.viewer.commons.prism.PrismHighlighter;
import org.apache.causeway.viewer.commons.prism.PrismTheme;
import org.apache.causeway.viewer.wicket.ui.util.PrismResourcesWkt;

record HighlightBehavior(
    PrismTheme theme,
    List<CssResourceReference> cssResourceReferences) {

    public static Optional<HighlightBehavior> lookup(final @Nullable SyntaxHighlighter syntaxHighlighter) {
        if(syntaxHighlighter==null
                || syntaxHighlighter==SyntaxHighlighter.NONE) {
            return Optional.empty();
        }
        return Optional.of(cache.computeIfAbsent(syntaxHighlighter, key->
            switch(syntaxHighlighter) {
                case PRISM_DEFAULT -> new HighlightBehavior(PrismTheme.DEFAULT);
                case PRISM_COY -> new HighlightBehavior(PrismTheme.COY);
                default -> throw _Exceptions.unmatchedCase(syntaxHighlighter);
            }    
        ));
    }
    
    private static final Map<SyntaxHighlighter, HighlightBehavior> cache = new ConcurrentHashMap<>();
    
    private HighlightBehavior(PrismTheme theme) {
        this(theme, PrismResourcesWkt.cssResources(theme));
    }
        
    void renderHead(final IHeaderResponse response) {
        for(CssResourceReference cssRef : cssResourceReferences()) {
            response.render(CssHeaderItem.forReference(cssRef));
        }
    }
    
    String htmlContentPostProcess(final String htmlContent) {
        var highlighted = new PrismHighlighter().apply(htmlContent);
        return highlighted;
    }
    
}
