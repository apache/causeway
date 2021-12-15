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

import org.apache.isis.applib.value.Password;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.val.PasswordValueSemantics")
public class PasswordValueSemantics
extends ValueSemanticsAbstract<Password>
implements
    EncoderDecoder<Password>,
    Parser<Password>,
    Renderer<Password> {

    @Override
    public Class<Password> getCorrespondingClass() {
        return Password.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final Password value) {
        if(value==null) {
            return null;
        }
        return value.getPassword();
    }

    @Override
    public Password fromEncodedString(final String data) {
        if(data==null) {
            return null;
        }
        return new Password(data);
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final Context context, final Password value) {
        return render(value, v->"*");
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final Password value) {
        return render(value, v->"*");
    }

    @Override
    public Password parseTextRepresentation(final Context context, final String text) {
        if(_Strings.isEmpty(text)) {
            return null;
        }
        return new Password(text);
    }

    @Override
    public int typicalLength() {
        return 12;
    }

    @Override
    public Can<Password> getExamples() {
        return Can.of(Password.of("a Password"), Password.of("another Password"));
    }

}
