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
package org.apache.causeway.commons.internal.reflection;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.io.TextUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
class _MethodSummaryUtil {

    String methodSummary(final @Nullable ResolvedMethod method) {
        return Optional.ofNullable(method)
        .map(ResolvedMethod::name)
        .map(name->TextUtils.cutter(name).keepAfter("sampleAction").getValue())
        .map(ordinal->String.format("%s%s%s%s%s%s",
                ordinal,
                _Reflect.hasGenericParam(method.method()) ? "p" : "",
                _Reflect.hasGenericReturn(method.method()) ? "r" : "",
                method.method().isSynthetic() ? "s" : "",
                method.method().isBridge() ? "b" : "",
                _NullSafe.stream(method.paramTypes())
                    .findFirst()
                    .map(paramType->paramType.equals(String.class) ? ":string" : ":?")
                    .orElse(""))
                )
        .orElse("-");
    }

}
