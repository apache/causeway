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
package org.apache.causeway.applib.value.semantics;

import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Convenient base class for value-semantics,
 * that are inferred from an {@link org.apache.causeway.applib.services.bookmark.IdStringifier.EntityAgnostic}.
 * @since 2.0
 */
public abstract class ValueSemanticsBasedOnIdStringifierEntityAgnostic<T>
extends ValueSemanticsAbstract<T>
implements
    Renderer<T>,
    Parser<T>,
    IdStringifier.EntityAgnostic<T> {

    @Getter @Accessors(makeFinal = true)
    private final Class<T> correspondingClass;

    protected ValueSemanticsBasedOnIdStringifierEntityAgnostic(
            final @NonNull Class<T> correspondingClass) {
        _Assert.assertFalse(correspondingClass.isPrimitive(),
                ()->String.format("not allowed to be initialized with a primitive class (%s), "
                        + "use the boxed variant instead",
                        correspondingClass));
        this.correspondingClass = correspondingClass;
    }

    @Override
    public final ValueType getSchemaValueType() {
        return ValueType.STRING;
    }

    // -- ID STRINGIFIER

    @Override
    public String enstring(@NonNull final T value) {
        return value.toString();
    }

    // -- COMPOSER

    @Override
    public final ValueDecomposition decompose(final T value) {
        return decomposeAsString(value, this::enstring, ()->null);
    }

    @Override
    public final T compose(final ValueDecomposition decomposition) {
        return composeFromString(decomposition, this::destring, ()->null);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final T value) {
        return value == null ? "" : enstring(value);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final T value) {
        return enstring(value);
    }

    @Override
    public T parseTextRepresentation(final Context context, final String text) {
        return destring(text);
    }

    @Override
    public int typicalLength() {
        return 255;
    }

}
