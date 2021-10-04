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
package org.apache.isis.valuetypes.asciidoc.metamodel.semantics;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.adapters.AbstractValueSemanticsProvider;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

@Component
@Named("isis.val.AsciiDocValueSemantics")
public class AsciiDocValueSemantics
extends AbstractValueSemanticsProvider<AsciiDoc>
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

    // -- RENDERER

    @Override
    public String simpleTextRepresentation(final ValueSemanticsProvider.Context context, final AsciiDoc adoc) {
        return render(adoc, AsciiDoc::asHtml);
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

}
