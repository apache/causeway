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
package org.apache.isis.core.metamodel.facets.value.uuid;

import java.util.UUID;

import org.apache.isis.applib.adapters.AbstractValueSemanticsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.commons.internal.base._Strings;

public class UUIDValueSemantics
extends AbstractValueSemanticsProvider<UUID>
implements
    EncoderDecoder<UUID>,
    Parser<UUID> {

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final UUID object) {
        return object.toString();
    }

    @Override
    public UUID fromEncodedString(final String data) {
        return UUID.fromString(data);
    }

    // -- PARSER

    @Override
    public String presentationValue(final Context context, final UUID value) {
        return value == null ? "" : value.toString();
    }

    @Override
    public String parseableTextRepresentation(final Context context, final UUID value) {
        return value == null ? null : value.toString();
    }

    @Override
    public UUID parseTextRepresentation(final Context context, final String text) {
        final var input = _Strings.blankToNullOrTrim(text);
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
