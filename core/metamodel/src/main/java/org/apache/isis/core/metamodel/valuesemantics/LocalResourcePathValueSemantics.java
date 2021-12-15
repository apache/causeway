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

import java.nio.file.InvalidPathException;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.val;

@Component
@Named("isis.val.LocalResourcePathValueSemantics")
public class LocalResourcePathValueSemantics
extends ValueSemanticsAbstract<LocalResourcePath>
implements
    EncoderDecoder<LocalResourcePath>,
    Parser<LocalResourcePath>,
    Renderer<LocalResourcePath> {

    @Override
    public Class<LocalResourcePath> getCorrespondingClass() {
        return LocalResourcePath.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING; // this type can be easily converted to string and back
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final LocalResourcePath localResourcePath) {
        return localResourcePath != null
                ? localResourcePath.getValue()
                : "NULL";
    }

    @Override
    public LocalResourcePath fromEncodedString(final String data) {
        if("NULL".equals(data)) {
            return null;
        }
        try {
            return new LocalResourcePath(data);
        } catch (InvalidPathException e) {
            return null;
        }
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final Context context, final LocalResourcePath value) {
        return render(value, LocalResourcePath::getValue);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final LocalResourcePath value) {
        return value != null ? value.getValue() : null;
    }

    @Override
    public LocalResourcePath parseTextRepresentation(final Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        try {
            return new LocalResourcePath(input);
        } catch (final InvalidPathException ex) {
            throw new IllegalArgumentException("Not parseable as a LocalResourcePath ('" + input + "')", ex);
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

    @Override
    public Can<LocalResourcePath> getExamples() {
        return Can.of(
                new LocalResourcePath("img/a"),
                new LocalResourcePath("img/b"));
    }


}
