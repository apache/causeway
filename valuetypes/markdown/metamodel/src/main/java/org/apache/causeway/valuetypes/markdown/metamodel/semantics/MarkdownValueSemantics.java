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
package org.apache.causeway.valuetypes.markdown.metamodel.semantics;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.valuetypes.markdown.applib.CausewayModuleValMarkdownApplib;
import org.apache.causeway.valuetypes.markdown.applib.value.Markdown;

@Component
@Named(CausewayModuleValMarkdownApplib.NAMESPACE + ".MarkdownValueSemantics")
public class MarkdownValueSemantics
extends ValueSemanticsAbstract<Markdown>
implements
    Parser<Markdown>,
    Renderer<Markdown> {

    @Override
    public Class<Markdown> getCorrespondingClass() {
        return Markdown.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final Markdown value) {
        return decomposeAsString(value, Markdown::getMarkdown, ()->null);
    }

    @Override
    public Markdown compose(final ValueDecomposition decomposition) {
        return composeFromString(decomposition, Markdown::valueOf, ()->null);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final Markdown value) {
        return renderTitle(value, Markdown::toString);
    }

    @Override
    public String htmlPresentation(final ValueSemanticsProvider.Context context, final Markdown adoc) {
        return renderHtml(adoc, Markdown::asHtml);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final Markdown value) {
        if(value==null) {
            return null;
        }
        return value.getMarkdown();
    }

    @Override
    public Markdown parseTextRepresentation(final Context context, final String text) {
        if(text==null) {
            return null;
        }
        return Markdown.valueOf(text);
    }

    @Override
    public int typicalLength() {
        return 0;
    }

    @Override
    public Can<Markdown> getExamples() {
        return Can.of(
                Markdown.valueOf("a Markdown"),
                Markdown.valueOf("another Markdown"));
    }

}
