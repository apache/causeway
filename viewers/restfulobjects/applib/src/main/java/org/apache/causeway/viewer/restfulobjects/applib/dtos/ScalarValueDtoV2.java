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
package org.apache.causeway.viewer.restfulobjects.applib.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Represents a nullable scalar value,
 * as used by ContentNegotiationServiceOrgApacheCausewayV2 and its clients.
 *
 * @since 2.0 {@index}
 */
@JsonIgnoreProperties({"links", "extensions"})
@Data @NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScalarValueDtoV2 {

    public static ScalarValueDtoV2 forNull(final @NonNull Class<?> type) {
        return new ScalarValueDtoV2(typeName(type), null);
    }

    public static ScalarValueDtoV2 forValue(final @NonNull Object value) {
        return new ScalarValueDtoV2(typeName(value.getClass()), value);
    }

    public static <T> ScalarValueDtoV2 forValue(final @NonNull T value, final @NonNull ValueSemanticsProvider<T> valueSemantics) {
        var valDecomp = valueSemantics.decompose(value);
        return new ScalarValueDtoV2(VALUE_DECOMPOSITION_TYPE_NAME, valDecomp.stringify());
    }

    private String type;
    private Object value;

    @JsonIgnore
    public boolean isNull() {
        return value == null;
    }

    @JsonIgnore
    public boolean isValueDecomposition() {
        return VALUE_DECOMPOSITION_TYPE_NAME.equals(getType());
    }

    @JsonIgnore
    public <T> T getValueAs(final Class<T> entityType) {
        if(isValueDecomposition()
                && (value instanceof String)) {
            try {
                var stringifiedComposite = (String)getValue();
                this.value = ValueDecomposition.destringify(ValueType.COMPOSITE, stringifiedComposite);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return _Casts.uncheckedCast(getValue());
    }

    // -- HELPER

    private static String VALUE_DECOMPOSITION_TYPE_NAME = "ValueDecomposition[base64/zlib]";

    private static String typeName(final @NonNull Class<?> cls) {
        return cls.isPrimitive()
                || cls.getPackageName().startsWith("java.")
                ? cls.getSimpleName()
                        : cls.getName();
    }

}
