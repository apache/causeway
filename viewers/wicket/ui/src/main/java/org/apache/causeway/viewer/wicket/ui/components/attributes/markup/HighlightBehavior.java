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
import java.util.function.UnaryOperator;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.graalvm.polyglot.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.value.semantics.Renderer.SyntaxHighlighter;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.viewer.commons.prism.PrismLanguage;
import org.apache.causeway.viewer.commons.prism.PrismNodeHighlighter;
import org.apache.causeway.viewer.commons.prism.PrismTheme;
import org.apache.causeway.viewer.wicket.ui.util.PrismResourcesWkt;

import lombok.extern.log4j.Log4j2;

record HighlightBehavior(
    PrismTheme theme,
    List<CssResourceReference> cssResourceReferences,
    PrismHighlighter prismHighlighter) {

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
        this(theme, PrismResourcesWkt.cssResources(theme), new PrismHighlighter(PrismResourcesWkt.jsResourceMain()));
    }
        
    void renderHead(final IHeaderResponse response) {
        for(CssResourceReference cssRef : cssResourceReferences()) {
            response.render(CssHeaderItem.forReference(cssRef));
        }
    }
    
    String htmlContentPostProcess(final String htmlContent) {
        var highlighted = prismHighlighter.apply(htmlContent);
        return highlighted;
    }
    
    @Log4j2
    record PrismHighlighter(String prismJs) implements UnaryOperator<String> {
        
        PrismHighlighter(JavaScriptResourceReference jsResourceReference) {
            this(PrismResourcesWkt.read(jsResourceReference).orElseThrow());
            System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        }
        
        /**
         * Returns the highlighted HTML.
         * @param htmlContent code to be highlighted
         */
        @Override
        public String apply(String htmlContent) {
            
            var doc = Jsoup.parseBodyFragment(htmlContent);
            
            var visitor = new NodeVisitor() {
                
                @Override
                public void head(Node node, int depth) {
                    if(node instanceof Element element
                        && "code".equals(node.nodeName())) {
                        var prismLanguage = PrismLanguage.parseFromCssClass(node.attr("class")).orElse(null);
                        if(prismLanguage==null) return;
                        var grammarJs = PrismResourcesWkt.read(PrismResourcesWkt.jsResource(prismLanguage.languageId()))
                            .orElse(null);
                        if(grammarJs==null) {
                            log.warn("grammarJs not found for {}", prismLanguage);
                            return;
                        }
                        
                        var newNode = new PrismNodeHighlighter(prismLanguage, ()->{
                            var context = Context.create("js");
                            context.eval("js", prismJs);
                            context.eval("js", grammarJs);
                            return context;
                        }).apply(element);
                        
                        node.replaceWith(newNode);
                    }
                }
                @Override
                public void tail(Node node, int depth) {
                }
            };
            
            doc.traverse(visitor);
            
            return doc.body().html();
        }

    }
    
}
