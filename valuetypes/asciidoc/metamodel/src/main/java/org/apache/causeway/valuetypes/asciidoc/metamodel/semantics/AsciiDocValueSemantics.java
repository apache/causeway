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
package org.apache.causeway.valuetypes.asciidoc.metamodel.semantics;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.valuetypes.asciidoc.applib.CausewayModuleValAsciidocApplib;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;

@Component
@Named(CausewayModuleValAsciidocApplib.NAMESPACE + ".AsciiDocValueSemantics")
public class AsciiDocValueSemantics
extends ValueSemanticsAbstract<AsciiDoc>
implements
    Renderer<AsciiDoc>,
    Parser<AsciiDoc> {

    @Override
    public Class<AsciiDoc> getCorrespondingClass() {
        return AsciiDoc.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final AsciiDoc value) {
        return decomposeAsString(value, AsciiDoc::getAdoc, ()->null);
    }

    @Override
    public AsciiDoc compose(final ValueDecomposition decomposition) {
        return composeFromString(decomposition, AsciiDoc::valueOf, ()->null);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final ValueSemanticsProvider.Context context, final AsciiDoc adoc) {
        return renderTitle(adoc, AsciiDoc::toString);
    }

    @Override
    public String htmlPresentation(final ValueSemanticsProvider.Context context, final AsciiDoc adoc) {
        return renderHtml(adoc, AsciiDoc::asHtml);
    }

    @Override
    public SyntaxHighlighter syntaxHighlighter() {
        return SyntaxHighlighter.PRISM_COY;
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final AsciiDoc adoc) {
        return adoc!=null ? adoc.getAdoc() : null;
    }

    @Override
    public AsciiDoc parseTextRepresentation(final ValueSemanticsProvider.Context context, final String adoc) {
        return adoc!=null ? AsciiDoc.valueOf(adoc) : null;
    }

    @Override
    public int typicalLength() {
        return 0;
    }

    // -- EXAMPLES

    @Override
    public Can<AsciiDoc> getExamples() {
        return Can.of(
                AsciiDoc.valueOf("a AsciiDoc"),
                AsciiDoc.valueOf("another AsciiDoc"));
    }

}
