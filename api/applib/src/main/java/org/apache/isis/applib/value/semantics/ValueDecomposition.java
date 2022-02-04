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
package org.apache.isis.applib.value.semantics;

import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.resources._Json;
import org.apache.isis.schema.common.v2.TypedTupleDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;

public final class ValueDecomposition extends _Either<ValueWithTypeDto, TypedTupleDto> {
    private static final long serialVersionUID = 1L;

    public static ValueDecomposition ofFundamental(final ValueWithTypeDto valueWithTypeDto) {
        return new ValueDecomposition(valueWithTypeDto, null);
    }

    public static ValueDecomposition ofComposite(final TypedTupleDto typedTupleDto) {
        return new ValueDecomposition(null, typedTupleDto);
    }

    private ValueDecomposition(final ValueWithTypeDto left, final TypedTupleDto right) {
        super(left, right);
    }

    // used by RO-Viewer to render values
    public String toJson() {
        return this.<String>fold(
                CommonDtoUtils::getFundamentalValueAsJson,
                composite->_Json.toString(
                        composite,
                        _Json::jaxbAnnotationSupport,
                        _Json::onlyIncludeNonNull));
    }

    // used by EncodableFacet
    public static ValueDecomposition fromJson(final ValueType vType, final String json) {
        if(vType==ValueType.COMPOSITE) {
            return _Json.readJson(ValueDecomposition.class, json).presentElseFail();
        }
        return ofFundamental(
                CommonDtoUtils.getFundamentalValueFromJson(vType, json));
    }

}