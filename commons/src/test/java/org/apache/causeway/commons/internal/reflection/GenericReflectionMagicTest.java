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

import org.junit.jupiter.api.Test;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.collections.Can;

import lombok.val;

class GenericReflectionMagicTest {

    @Test
    void resolveGenericReturnTypeAndParameterType() {
        val declaredMethodsMatching = Can.ofStream(_Reflect.streamAllMethods(_GenericAbstractImpl.class, true))
                .filter(m->m.getName().equals("sampleAction4"));

        //debug
        //declaredMethodsMatching.forEach(m->System.err.printf("%s (%s)%n", _Util.methodSummary(m), m));

        assertEquals(1, declaredMethodsMatching.size());

        val sampleAction = declaredMethodsMatching.getFirstElseFail();

        val returnType = GenericTypeResolver.resolveReturnType(sampleAction, _GenericAbstractImpl.class);
        assertEquals(String.class, returnType);

        @SuppressWarnings("deprecation") // proposed alternative is not publicly visible
        val param0Type = GenericTypeResolver.resolveParameterType(new MethodParameter(sampleAction, 0), _GenericAbstractImpl.class);
        assertEquals(String.class, param0Type);
    }

    @Test
    void detectMethodOverride() {
        val declaredMethodsMatching = Can.ofStream(_Reflect.streamAllMethods(_GenericAbstractImpl.class, true))
                .filter(m->m.getName().equals("sampleAction2"))
                .filter(m->!m.isBridge());

        //debug
        declaredMethodsMatching.forEach(m->System.err.printf("%s->%b%n", m, m.isBridge()));

        assertEquals(2, declaredMethodsMatching.size());


    }

}