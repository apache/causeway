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

import java.util.Locale;

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

import lombok.val;

@Component
@Named("isis.val.LocaleValueSemantics")
public class LocaleValueSemantics
extends ValueSemanticsAbstract<Locale>
implements
    EncoderDecoder<Locale>,
    Parser<Locale>,
    Renderer<Locale> {

    @Override
    public Class<Locale> getCorrespondingClass() {
        return Locale.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING; // this type can be easily converted to string and back
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final Locale object) {
        return object!=null
                ? object.toLanguageTag()
                : null;
    }

    @Override
    public Locale fromEncodedString(final String data) {
        return data!=null
                ? Locale.forLanguageTag(data)
                : null;
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final ValueSemanticsProvider.Context context, final Locale value) {

        return render(value, v->{

            val userLanguageLocale = context.getInteractionContext().getLocale().getLanguageLocale();

            val language = value.getDisplayLanguage(userLanguageLocale);
            if(_Strings.isEmpty(language)) {
                return toEncodedString(v);
            }

            val country = value.getDisplayCountry(userLanguageLocale);
            if(_Strings.isEmpty(country)) {
                return language;
            }

            return String.format("%s (%s)",
                    value.getDisplayLanguage(userLanguageLocale),
                    value.getDisplayCountry(userLanguageLocale));

        });
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final Locale value) {
        return value == null ? null : toEncodedString(value);
    }

    @Override
    public Locale parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        return input!=null
                ? fromEncodedString(input)
                : null;
    }

    @Override
    public int typicalLength() {
        return maxLength();
    }

    @Override
    public int maxLength() {
        return 80;
    }

    // -- EXAMPLES

    @Override
    public Can<Locale> getExamples() {
        return Can.of(Locale.US, Locale.GERMAN);
    }

}
