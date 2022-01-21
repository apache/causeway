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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;

import lombok.val;

class ClassCacheTest {

    static abstract class AbstractBase {
        void commonAction(){}
    }

    static class Concrete extends AbstractBase {
        void specificAction(){}
    }

    static class ConcreteOverride extends AbstractBase {
        @Override void commonAction(){}
        void specificAction(){}
    }

    private _ClassCache classCache;

    @BeforeEach
    void setup() {
        _ClassCache.invalidate();
        classCache = _ClassCache.getInstance();
    }

    @Test
    void inhertitedMethodWhenUsingReflectionUtility() {
        val declaredMethods = Can.ofStream(
                _NullSafe.stream(_Reflect.streamAllMethods(Concrete.class, true)));
        assertContainsMethod(declaredMethods, "commonAction");
        assertContainsMethod(declaredMethods, "specificAction");
    }

    @Test
    void inhertitedMethod() {
        val declaredMethods = Can.ofStream(
                classCache.streamPublicOrDeclaredMethods(Concrete.class));
        assertContainsMethod(declaredMethods, "commonAction");
        assertContainsMethod(declaredMethods, "specificAction");
    }

    @Test
    void inhertitedMethodWhenOverride() {
        val declaredMethods = Can.ofStream(
                classCache.streamPublicOrDeclaredMethods(ConcreteOverride.class));
        assertContainsMethod(declaredMethods, "commonAction");
        assertContainsMethod(declaredMethods, "specificAction");
    }

    // -- HELPER

    private void assertContainsMethod(final Can<Method> declaredMethods, final String methodName) {

        final long methodCount =
            declaredMethods.stream()
            .filter(m->m.getName().equals(methodName))
            // using filter over peek here, because peek is unreliable with 'count()' terminal
            .filter(m->{
                assertNotNull(m);
                return true;
            })
            .count();

        assertTrue(methodCount>0);
    }


}
