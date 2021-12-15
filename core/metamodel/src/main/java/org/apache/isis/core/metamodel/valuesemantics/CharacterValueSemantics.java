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

import org.apache.isis.applib.exceptions.recoverable.InvalidEntryException;
import org.apache.isis.applib.value.semantics.DefaultsProvider;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.val;

/**
 * due to auto-boxing also handles the primitive variant
 */
@Component
@Named("isis.val.CharacterValueSemantics")
public class CharacterValueSemantics
extends ValueSemanticsAbstract<Character>
implements
    DefaultsProvider<Character>,
    EncoderDecoder<Character>,
    Parser<Character>,
    Renderer<Character> {

    @Override
    public Class<Character> getCorrespondingClass() {
        return Character.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.CHAR;
    }

    @Override
    public Character getDefaultValue() {
        return (char) 0;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final Character object) {
        return object.toString();
    }

    @Override
    public Character fromEncodedString(final String data) {
        return Character.valueOf(data.charAt(0));
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final Context context, final Character value) {
        return render(value, c->""+c);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final Character value) {
        return value == null ? "" : value.toString();
    }

    @Override
    public Character parseTextRepresentation(final Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        if (input.length() > 1) {
            throw new InvalidEntryException("Only a single character is required");
        } else {
            return Character.valueOf(input.charAt(0));
        }
    }

    @Override
    public int typicalLength() {
        return 1;
    }

    @Override
    public int maxLength() {
        return 1;
    }

    @Override
    public Can<Character> getExamples() {
        return Can.of('a', 'b');
    }

}
