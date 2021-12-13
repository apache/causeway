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

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.SneakyThrows;
import lombok.val;

@Component
@Named("isis.val.URLValueSemantics")
public class URLValueSemantics
extends ValueSemanticsAbstract<java.net.URL>
implements
    EncoderDecoder<java.net.URL>,
    Parser<java.net.URL>,
    Renderer<java.net.URL> {

    @Override
    public Class<java.net.URL> getCorrespondingClass() {
        return java.net.URL.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING; // this type can be easily converted to string and back
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final java.net.URL url) {
        return url != null? url.toString(): "NULL";
    }

    @Override
    public java.net.URL fromEncodedString(final String data) {
        if("NULL".equals(data)) {
            return null;
        }
        try {
            return new java.net.URL(data);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final ValueSemanticsProvider.Context context, final java.net.URL value) {
        return value != null ? value.toString(): "";
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final java.net.URL value) {
        return value != null ? value.toString(): null;
    }

    @Override
    public java.net.URL parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        try {
            return new java.net.URL(input);
        } catch (final MalformedURLException ex) {
            throw new IllegalArgumentException("Not parseable as an URL ('" + input + "')", ex);
        }
    }

    @Override
    public int typicalLength() {
        return 100;
    }

    @Override
    public int maxLength() {
        return 2083;
    }

    @SneakyThrows
    @Override
    public Can<URL> getExamples() {
        return Can.of(
                new URL("https://a.b.c"),
                new URL("https://b.c.d"));
    }

}
