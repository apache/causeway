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
package org.apache.causeway.valuetypes.asciidoc.builder;

import java.util.function.Consumer;

import org.asciidoctor.ast.Document;

import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;

import static org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocFactory.doc;

/**
 * Provides fluent factory methods to build a (AsciiDoc) Document Model.
 * @since Sep 8, 2023
 * @apiNote The <i>AsciiDoc<i> name is trademarked by the <i>Eclipse Foundation</i>.
 * <p>
    This project is <b>not</b> part of the specification effort for <i>AsciiDoc<i> under the
    <i>AsciiDoc Working Group</i>. See https://projects.eclipse.org/proposals/asciidoc-language
    and https://accounts.eclipse.org/mailing-list/asciidoc-wg. However, we are happy to
    help with transfer of source code, if any project (under the umbrella of the
    <i>AsciiDoc Working Group</i>) is willing to take over.
    </p>
 */
public class AsciiDocBuilder {

    private final Document doc = doc();

    // -- APPEND

    public AsciiDocBuilder append(final Consumer<Document> appender) {
        appender.accept(doc);
        return this;
    }

//XXX looks great, but would not support block nesting ...
//    // -- TITLE
//
//    public AsciiDocBuilder title(final String title) {
//        doc.setTitle(title);
//        return this;
//    }
//
//    // -- BLOCKS
//
//    public AsciiDocBuilder javaBlockWithTitle(final String source, @Nullable final String title) {
//        AsciiDocFactory.SourceFactory.java(doc, source, title);
//        return this;
//    }
//
//    public AsciiDocBuilder xmlBlockWithTitle(final String source, @Nullable final String title) {
//        AsciiDocFactory.SourceFactory.xml(doc, source, title);
//        return this;
//    }
//
//    public AsciiDocBuilder yamlBlockWithTitle(final String source, @Nullable final String title) {
//        AsciiDocFactory.SourceFactory.yaml(doc, source, title);
//        return this;
//    }
//
//    public AsciiDocBuilder htmlPassthrough(final String html) {
//        AsciiDocFactory.htmlPassthroughBlock(doc, html);
//        return this;
//    }

    // -- BUILD

    /**
     * Builds the underlying <i>AsciiDoc<i> document as {@link String}.
     */
    public String buildAsString() {
        return AsciiDocWriter.toString(doc);
    }

    /**
     * Builds the underlying <i>AsciiDoc<i> document as {@link AsciiDoc} value.
     */
    public AsciiDoc buildAsValue() {
        return AsciiDoc.valueOf(buildAsString());
    }

    /**
     * Builds a simple {@link AsciiDoc} value,
     * that has a source block section containing the underlying <i>AsciiDoc<i> document
     * for introspection and debugging of the emitted source.
     */
    public AsciiDoc buildAsDebugValue() {
        return new AsciiDocBuilder().append(doc->{
            AsciiDocFactory.sourceBlock(doc, "txt", this.buildAsString());
        })
        .buildAsValue();
    }

}
