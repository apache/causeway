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
package org.apache.causeway.core.metamodel.facets.object.value;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "forSemantics")
public class ValueSerializerDefault<T>
implements ValueSerializer<T> {

    public static final String ENCODED_NULL = "NULL";

    private final @NonNull ValueSemanticsProvider<T> semantics;

    @Override
    public T destring(final @NonNull Format format, final @NonNull String encodedData) {
        if (ENCODED_NULL.equals(encodedData)) {
            return null;
        }
        switch(format) {
        case JSON:
            return semantics.compose(
                    ValueDecomposition.fromJson(semantics.getSchemaValueType(), encodedData));
        case URL_SAFE:
            //TODO could use IdStringifiers instead
            return destring(Format.JSON, _Strings.base64UrlDecode(encodedData));
        }
        throw _Exceptions.unmatchedCase(format);
    }

    @Override
    public String enstring(final @NonNull Format format, final @Nullable T value) {
        if(value == null) {
            return ENCODED_NULL;
        }
        switch(format) {
        case JSON:
            return semantics.decompose(_Casts.uncheckedCast(value)).toJson();
        case URL_SAFE:
            //TODO could use IdStringifiers instead
            return _Strings.base64UrlEncode(enstring(Format.JSON, value));
        }
        throw _Exceptions.unmatchedCase(format);
    }

}
