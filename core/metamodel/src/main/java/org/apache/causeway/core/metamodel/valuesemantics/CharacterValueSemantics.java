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

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.exceptions.recoverable.InvalidEntryException;
import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.schema.common.v2.ValueDto;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.NonNull;
import lombok.val;

/**
 * due to auto-boxing also handles the primitive variant
 */
@Component
@Named("causeway.val.CharacterValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class CharacterValueSemantics
extends ValueSemanticsAbstract<Character>
implements
    DefaultsProvider<Character>,
    Parser<Character>,
    Renderer<Character>,
    IdStringifier.EntityAgnostic<Character> {

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

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final Character value) {
        return decomposeAsNullable(value, UnaryOperator.identity(), ()->null);
    }

    @Override
    public Character compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, ValueDto::getChar, this::fromString, ()->null);
    }

    private Character fromString(final String data) {
        return data!=null
                && data.length()>0
                ? Character.valueOf(data.charAt(0))
                : null;
    }

    // -- ID STRINGIFIER

    static final List<Character> NON_SAFE_URL_CHARS = StringValueSemantics.NON_SAFE_URL_CHARS
            .stream()
            .map(x -> x.charAt(0))
            .collect(Collectors.toList());
    static final String REGULAR_PREFIX = "c" + IdStringifier.SEPARATOR;
    static final String BASE64_PREFIX = "cbse64" + IdStringifier.SEPARATOR;

    @Override
    public String enstring(final @NonNull Character id) {
        if(NON_SAFE_URL_CHARS.stream().anyMatch(x -> Objects.equals(x, id))) {
            return BASE64_PREFIX + _Strings.base64UrlEncode(""+id);
        }
        return REGULAR_PREFIX + id;
    }

    @Override
    public Character destring(final @NonNull String stringified) {
        if(stringified.startsWith(REGULAR_PREFIX)) {
            return stringified.substring(REGULAR_PREFIX.length()).charAt(0);
        }
        if(stringified.startsWith(BASE64_PREFIX)) {
            return _Strings.base64UrlDecode(stringified.substring(BASE64_PREFIX.length())).charAt(0);
        }
        throw new IllegalArgumentException("Could not parse '" + stringified + "'");
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final Character value) {
        return renderTitle(value, c->""+c);
    }

    @Override
    public String htmlPresentation(final Context context, final Character value) {
        return renderHtml(value, c->""+c);
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
