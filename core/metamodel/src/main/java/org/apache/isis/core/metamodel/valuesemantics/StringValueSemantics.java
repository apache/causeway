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

import org.apache.isis.applib.adapters.AbstractValueSemanticsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.val.StringValueSemantics")
public class StringValueSemantics
extends AbstractValueSemanticsProvider<String>
implements
    EncoderDecoder<String>,
    Parser<String>,
    Renderer<String> {

    @Override
    public Class<String> getCorrespondingClass() {
        return String.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final String text) {
        return text;
    }

    @Override
    public String fromEncodedString(final String data) {
        return data;
    }

    // -- RENDERER

    @Override
    public String simpleTextRepresentation(final Context context, final String value) {
        return value == null ? "" : value;
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final String value) {
        return value;
    }

    @Override
    public String parseTextRepresentation(final Context context, final String text) {
        return text;
    }

    @Override
    public int typicalLength() {
        return 25;
    }

}
