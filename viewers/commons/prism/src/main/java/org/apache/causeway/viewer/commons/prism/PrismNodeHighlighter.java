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

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 * Processes a single {@literal <code>} node.
 */
record PrismNodeHighlighter(
    PrismLanguage prismLanguage,
    /**
     * Expects the supplier to load the prism main JS and also the grammar JS associated with the given language.
     */
    Supplier<Context> contextSupplier
    ) implements UnaryOperator<Element> {

    @Override
    public Element apply(final Element codeNode) {
        try (Context context = contextSupplier.get()) {
            // Get the highlight function and execute it
            Value prism = context.getBindings("js").getMember("Prism");
            Value highlight = prism.getMember("highlight");

            Value language = prism.getMember("languages").getMember(prismLanguage.languageId());
            String highlightedCode = highlight.execute(codeNode.html(), language, prismLanguage.languageId()).asString();

            var code = """
                    <code class="language-%s" data-lang="%s">%s</code>"""
                    .formatted(prismLanguage.languageId(), prismLanguage.languageId(), highlightedCode);

            var doc = Jsoup.parseBodyFragment(code);
            return doc.body().child(0);
        }
    }

}
