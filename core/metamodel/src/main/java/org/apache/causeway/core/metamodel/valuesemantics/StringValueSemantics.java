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
package org.apache.causeway.core.metamodel.valuesemantics;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.NonNull;

@Component
@Named("causeway.val.StringValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class StringValueSemantics
extends ValueSemanticsAbstract<String>
implements
    Parser<String>,
    Renderer<String>,
    IdStringifier.EntityAgnostic<String> {

    @Override
    public Class<String> getCorrespondingClass() {
        return String.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final String text) {
        return decomposeAsString(text, UnaryOperator.identity(), ()->null);
    }

    @Override
    public String compose(final ValueDecomposition decomposition) {
        return composeFromString(decomposition, UnaryOperator.identity(), ()->null);
    }

    // -- ID STRINGIFIER

    static final List<String> NON_SAFE_URL_CHARS =
            Arrays.asList("/", "\\", "?", ":", "&", "%", "+");
    static final String REGULAR_PREFIX = "s" + IdStringifier.SEPARATOR;
    static final String BASE64_PREFIX = "base64" + IdStringifier.SEPARATOR;

    @Override
    public String enstring(final @NonNull String id) {
        if(NON_SAFE_URL_CHARS.stream().anyMatch(id::contains)) {
            return BASE64_PREFIX + _Strings.base64UrlEncode(id);
        }
        return REGULAR_PREFIX + id;
    }

    @Override
    public String destring(final @NonNull String stringified) {
        if(stringified.startsWith(REGULAR_PREFIX)) {
            return stringified.substring(REGULAR_PREFIX.length());
        }
        if(stringified.startsWith(BASE64_PREFIX)) {
            return _Strings.base64UrlDecode(stringified.substring(BASE64_PREFIX.length()));
        }
        throw new IllegalArgumentException(String.format("Could not parse stringified id '%s'", stringified));
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final String value) {
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

    @Override
    public Can<String> getExamples() {
        return Can.of("a String", "another String");
    }

}
