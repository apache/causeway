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

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal._Constants;

import lombok.val;

class GenericsTest {

    Class<?> typeUnderTest;
    Can<String> fieldUnderTest;
    Can<String> methodUnderTest(){ return null; };

    @BeforeEach
    void setUp() throws Exception {
        typeUnderTest = (new ArrayList<String>()).getClass();
    }

    @Test
    void streamGenericTypeArgumentsOfType() {

        val actualTypeArgs = _Generics.streamGenericTypeArgumentsOfType(typeUnderTest)
        .collect(Can.toCan());

        // impossible, so empty
        assertEquals(Can.empty(), actualTypeArgs);
    }

    @Test
    void streamGenericTypeArgumentsOfMethodReturnType() throws NoSuchMethodException, SecurityException {

        val methodUnderTest = this.getClass().getDeclaredMethod("methodUnderTest", _Constants.emptyClasses);

        val actualTypeArgs = _Generics.streamGenericTypeArgumentsOfMethodReturnType(methodUnderTest)
        .collect(Can.toCan());

        assertEquals(Can.of(String.class), actualTypeArgs);
    }

    @Test
    void streamGenericTypeArgumentsOfField() throws NoSuchFieldException, SecurityException {

        val fieldUnderTest = this.getClass().getDeclaredField("fieldUnderTest");

        val actualTypeArgs = _Generics.streamGenericTypeArgumentsOfField(fieldUnderTest)
        .collect(Can.toCan());

        assertEquals(Can.of(String.class), actualTypeArgs);
    }

}
