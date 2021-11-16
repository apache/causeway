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
package org.apache.isis.commons.internal.reflection;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReflectTest {

    static class Parent {
        // method a
        Object getSomething(final Collection<String> input){
            return 10;
        }
    }

    static class Child extends Parent {

        // method b
        @Override
        Integer getSomething(final Collection<String> input) {
            return 10;
        }

        // method c
        Integer getSomething(final List<String> input) {
            return 10;
        }

    }

    static class Outer{
        class NonStaticInner {
            public void nonStaticInnerMethod() {}
        }
        static class StaticInner {
            public void staticInnerMethod() {}
        }
    }

    Method a, b, c;
    Method nonStaticInnerMethod, staticInnerMethod;

    @BeforeEach
    void setUp() throws Exception {

        // given

        a = ReflectionUtils.findMethod(Parent.class, "getSomething", new Class[] {Collection.class});
        b = ReflectionUtils.findMethod(Child.class, "getSomething", new Class[] {Collection.class});
        c = ReflectionUtils.findMethod(Child.class, "getSomething", new Class[] {List.class});
        nonStaticInnerMethod = ReflectionUtils
                .findMethod(Outer.NonStaticInner.class, "nonStaticInnerMethod", new Class[] {});
        staticInnerMethod = ReflectionUtils
                .findMethod(Outer.StaticInner.class, "staticInnerMethod", new Class[] {});

        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(c);
        assertNotNull(nonStaticInnerMethod);
        assertNotNull(staticInnerMethod);

        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, c);
    }

    @Test
    void methodSameness() {
        assertTrue(_Reflect.methodsSame(a, b));
        assertFalse(_Reflect.methodsSame(a, c));
    }

    @Test
    void methodWeakOrdering() {
        assertTrue(_Reflect.methodWeakCompare(a, b) == 0);
        assertTrue(_Reflect.methodWeakCompare(a, c) != 0);
    }

    @Test
    void methodIsInner() {
        assertTrue(_Reflect.isNonStaticInnerMethod(nonStaticInnerMethod));
        assertFalse(_Reflect.isNonStaticInnerMethod(staticInnerMethod));
    }

}
