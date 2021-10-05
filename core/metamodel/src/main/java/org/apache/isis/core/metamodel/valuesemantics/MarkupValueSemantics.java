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
package org.apache.isis.core.metamodel.valuesemantics;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.adapters.ValueSemanticsAbstact;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.applib.value.Markup;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.val.MarkupValueSemantics")
public class MarkupValueSemantics
extends ValueSemanticsAbstact<Markup>
implements
    EncoderDecoder<Markup>,
    Parser<Markup>,
    Renderer<Markup> {

    @Override
    public Class<Markup> getCorrespondingClass() {
        return Markup.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return UNREPRESENTED;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final Markup markup) {
        if(markup==null) {
            return null;
        }
        return markup.asHtml();
    }

    @Override
    public Markup fromEncodedString(final String html) {
        if(html==null) {
            return null;
        }
        return new Markup(html);
    }

    // -- RENDERER

    @Override
    public String simpleTextRepresentation(final ValueSemanticsProvider.Context context, final Markup value) {
        return value != null? value.asHtml(): "[null]";
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final Markup value) {
        return toEncodedString(value);
    }

    @Override
    public Markup parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        return fromEncodedString(text);
    }

    @Override
    public int typicalLength() {
        return 0;
    }


}
