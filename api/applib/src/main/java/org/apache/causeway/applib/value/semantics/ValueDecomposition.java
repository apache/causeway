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

import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.functional.Either.HasEither;
import org.apache.causeway.schema.common.v2.TypedTupleDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(access=AccessLevel.PROTECTED)
@ToString @EqualsAndHashCode
public final class ValueDecomposition
implements
    HasEither<ValueWithTypeDto, TypedTupleDto>,
    Serializable {
    private static final long serialVersionUID = 1L;

    public static ValueDecomposition ofFundamental(final ValueWithTypeDto valueWithTypeDto) {
        return new ValueDecomposition(Either.left(valueWithTypeDto));
    }

    public static ValueDecomposition ofComposite(final TypedTupleDto typedTupleDto) {
        return new ValueDecomposition(Either.right(typedTupleDto));
    }

    /**
     * In support of JAXB de-serialization, 
     * returns an unspecified type.
     * (Introduced for the CalendarEvent demo to work.)
     * @deprecated not sure why we are hitting this; remove eventually
     */
    @Deprecated
    public ValueDecomposition() {
        this(Either.left(new ValueWithTypeDto()));
    }

    @Getter private final Either<ValueWithTypeDto, TypedTupleDto> either;

    // used by RO-Viewer to render values
    public String toJson() {
        return this.<String>fold(
                CommonDtoUtils::getFundamentalValueAsJson,
                CommonDtoUtils::getCompositeValueAsJson);
    }

    // used by EncodableFacet
    public static ValueDecomposition fromJson(final ValueType vType, final String json) {
        return vType==ValueType.COMPOSITE
            ? ofComposite(CommonDtoUtils.getCompositeValueFromJson(json))
            : ofFundamental(CommonDtoUtils.getFundamentalValueFromJson(vType, json));
    }

}