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

import java.util.UUID;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.adapters.ValueSemanticsAbstract;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.val;

@Component
@Named("isis.val.UUIDValueSemantics")
public class UUIDValueSemantics
extends ValueSemanticsAbstract<UUID>
implements
    EncoderDecoder<UUID>,
    Parser<UUID>,
    Renderer<UUID> {

    @Override
    public Class<UUID> getCorrespondingClass() {
        return UUID.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return UNREPRESENTED;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final UUID object) {
        return object.toString();
    }

    @Override
    public UUID fromEncodedString(final String data) {
        return UUID.fromString(data);
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final ValueSemanticsProvider.Context context, final UUID value) {
        return value == null ? "" : value.toString();
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final UUID value) {
        return value == null ? null : value.toString();
    }

    @Override
    public UUID parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        return input!=null
                ? UUID.fromString(input)
                : null;
    }

    @Override
    public int typicalLength() {
        return maxLength();
    }

    @Override
    public int maxLength() {
        return 36;
    }

}
