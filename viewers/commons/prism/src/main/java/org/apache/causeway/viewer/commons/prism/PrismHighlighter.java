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
package org.apache.causeway.viewer.commons.prism;

import java.util.function.UnaryOperator;

import org.graalvm.polyglot.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import lombok.extern.log4j.Log4j2;

@Log4j2
public record PrismHighlighter() implements UnaryOperator<String> {
    
    /**
     * Returns the highlighted HTML.
     * @param htmlContent code to be highlighted
     */
    @Override
    public String apply(String htmlContent) {
        var prismJs = PrismUtils.jsResourceMain().orElseThrow();
        var doc = Jsoup.parseBodyFragment(htmlContent);
        
        doc.traverse((Node node, int depth)->{
            if(node instanceof Element element
                && "code".equals(node.nodeName())) {
                
                var prismLanguage = PrismLanguage.parseFromCssClass(node.attr("class")).orElse(null);
                if(prismLanguage==null) return;
                
                var grammarJs = PrismUtils.jsResource(prismLanguage).orElse(null);
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
        });
        
        return doc.body().html();
    }

}
