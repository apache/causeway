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

import java.lang.reflect.Method;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.collections.Can;

import lombok.Builder;

@lombok.Value @Builder
public class _Expectations {

    int methodCount;
    int syntheticCount;
    int bridgeCount;
    /**
     * short notation see {@link _Util}
     */
    String methodNameOrdinals;

    _Expectations actual(final Can<Method> methods) {
        return _Expectations.builder()
                .methodCount(methods.size())
                .syntheticCount((int) methods.stream()
                        .filter(method->method.isSynthetic())
                        //.peek(m->System.err.printf("syn: %s%n", m)) // debug
                        .count())
                .bridgeCount((int) methods.stream()
                        .filter(method->method.isBridge())
                        //.peek(m->System.err.printf("bdg: %s%n", m)) // debug
                        .count())
                .methodNameOrdinals(methods.stream()
                        .sorted((a, b)->a.getName().compareTo(b.getName()))
                        .map(_Util::methodSummary)
                        .sorted((a, b)->a.compareTo(b))
                        .collect(Collectors.joining(",")))
                .build();
    }

    void assertAll(final Can<Method> methods) {
        assertEquals(this, actual(methods));
    }

}
