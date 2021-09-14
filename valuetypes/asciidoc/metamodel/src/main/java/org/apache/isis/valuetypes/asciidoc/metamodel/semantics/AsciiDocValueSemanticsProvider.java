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

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

public class AsciiDocValueSemanticsProvider
implements
    ValueSemanticsProvider<AsciiDoc>,
    //EncoderDecoder<AsciiDoc>,
    Parser<AsciiDoc> {

    @Override
    public String parseableTextRepresentation(final Parser.Context context, final AsciiDoc adoc) {
        return adoc!=null ? adoc.getAdoc() : null;
    }

    @Override
    public AsciiDoc parseTextRepresentation(final Parser.Context context, final String adoc) {
        return adoc!=null ? AsciiDoc.valueOf(adoc) : null;
    }

    @Override
    public String presentationValue(final Parser.Context context, final AsciiDoc adoc) {
        return adoc != null? adoc.asHtml(): "[null]";
    }

    @Override
    public int typicalLength() {
        return 0;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Parser<AsciiDoc> getParser() {
        return this;
    }

    @Override
    public EncoderDecoder<AsciiDoc> getEncoderDecoder() {
        return null;
    }

    @Override
    public DefaultsProvider<AsciiDoc> getDefaultsProvider() {
        return null;
    }

    public static void loadJRuby() {
        Converter.getAsciidoctor();
    }

}
