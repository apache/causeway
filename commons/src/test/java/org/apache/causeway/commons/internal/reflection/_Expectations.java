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

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;

import lombok.Builder;

@lombok.Value @Builder
public class _Expectations {

    int methodCount;
    int syntheticCount;
    int bridgeCount;
    /**
     * short notation see {@link _MethodSummaryUtil}
     */
    String methodNameOrdinals;

    _Expectations actual(final Can<ResolvedMethod> methods) {
        return _Expectations.builder()
                .methodCount(methods.size())
                .syntheticCount((int) methods.stream()
                        .filter(method->method.method().isSynthetic())
                        //.peek(m->System.err.printf("syn: %s%n", m)) // debug
                        .count())
                .bridgeCount((int) methods.stream()
                        .filter(method->method.method().isBridge())
                        //.peek(m->System.err.printf("bdg: %s%n", m)) // debug
                        .count())
                .methodNameOrdinals(methods.stream()
                        .sorted((a, b)->a.name().compareTo(b.name()))
                        .map(_MethodSummaryUtil::methodSummary)
                        .sorted((a, b)->a.compareTo(b))
                        .collect(Collectors.joining(",")))
                .build();
    }

    void assertAll(final Can<ResolvedMethod> methods) {
        assertEquals(this, actual(methods));
    }

}
