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

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.schema.common.v2.TypedTupleDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

@Programmatic
public record ValueDecomposition(
    @Nullable ValueWithTypeDto fundamental,
    @Nullable TypedTupleDto composite)
implements Serializable {

    public static ValueDecomposition ofFundamental(final @NonNull ValueWithTypeDto valueWithTypeDto) {
        return new ValueDecomposition(valueWithTypeDto, null);
    }

    public static ValueDecomposition ofComposite(final @NonNull TypedTupleDto typedTupleDto) {
        return new ValueDecomposition(null, typedTupleDto);
    }

    // used by EncodableFacet
    public static ValueDecomposition fromJson(final ValueType vType, final String json) {
        return vType==ValueType.COMPOSITE
            ? ofComposite(CommonDtoUtils.getCompositeValueFromJson(json))
            : ofFundamental(CommonDtoUtils.getFundamentalValueFromJson(vType, json));
    }

    // for transport over REST
    public static ValueDecomposition destringify(final ValueType vType, final String string) {
        return fromJson(vType, _Strings.base64UrlDecodeZlibCompressed(string));
    }

    // as of time of writing there is no JAXB support for record types
    ValueDecomposition() {
        this(null, null);
    }

    public Optional<ValueWithTypeDto> fundamentalAsOptional() {
        return Optional.ofNullable(fundamental);
    }
    public Optional<TypedTupleDto> compositeAsOptional() {
        return Optional.ofNullable(composite);
    }

    // used by RO-Viewer to render values
    public String toJson() {
        return fundamental!=null
                ? CommonDtoUtils.getFundamentalValueAsJson(fundamental)
                : CommonDtoUtils.getCompositeValueAsJson(composite);
    }

    // for transport over REST
    public String stringify() {
        return _Strings.base64UrlEncodeZlibCompressed(toJson());
    }

    public void accept(
            final @NonNull Consumer<ValueWithTypeDto> fundamentalConsumer,
            final @NonNull Consumer<TypedTupleDto> compositeConsumer) {
        if(fundamental!=null) {
            fundamentalConsumer.accept(fundamental);
        } else {
            compositeConsumer.accept(composite);
        }
    }

}