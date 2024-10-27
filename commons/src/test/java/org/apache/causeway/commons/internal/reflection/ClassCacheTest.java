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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;

import lombok.SneakyThrows;

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

    static class Sample {
        String echoAction(final String x) {return x;}
    }

    private _ClassCache classCache;

    @BeforeEach
    void setup() {
        _ClassCache.invalidate();
        classCache = _ClassCache.getInstance();
    }

    @Test
    void weakMethodLookup() {
        assertNotNull(classCache.lookupResolvedMethodElseFail(Sample.class, "echoAction", new Class<?>[]{Object.class}));
    }
    @Test
    void exactMethodLookup() {
        assertNotNull(classCache.lookupResolvedMethodElseFail(Sample.class, "echoAction", new Class<?>[]{String.class}));
    }
    @Test
    void invalidMethodLookup() {
        assertThrows(NoSuchMethodException.class,
                ()->classCache.lookupResolvedMethodElseFail(Sample.class, "echoAction", new Class<?>[]{Integer.class}));
    }

    @Test
    void inhertitedMethodWhenUsingReflectionUtility() {
        var declaredMethods = Can.ofStream(
                _NullSafe.stream(_Reflect.streamAllMethods(Concrete.class, true)));
        assertContainsMethod(declaredMethods, "commonAction");
        assertContainsMethod(declaredMethods, "specificAction");
    }

    @Test
    void inhertitedMethod() {
        var declaredMethods = Can.ofStream(
                classCache.streamResolvedMethods(Concrete.class));
        assertContainsResolvedMethod(declaredMethods, "commonAction");
        assertContainsResolvedMethod(declaredMethods, "specificAction");
    }

    @Test
    void inhertitedMethodWhenOverride() {
        var declaredMethods = Can.ofStream(
                classCache.streamResolvedMethods(ConcreteOverride.class));
        assertContainsResolvedMethod(declaredMethods, "commonAction");
        assertContainsResolvedMethod(declaredMethods, "specificAction");
    }

    @ParameterizedTest(name = "{index}: {0}")
    @ValueSource(classes = {
            _Abstract.class,
            _AbstractImpl.class,
            _Interface.class,
            _InterfaceImpl.class,
            _GenericAbstract.class,
            _GenericAbstractImpl.class,
            _GenericInterface.class,
            _GenericInterfaceImpl.class,
            _Mixins.Task1.Mixin.class,
            _Mixins.Task2.Mixin.class,
    })
    void methodEnumeration(final Class<?> classUnderTest) {
        var declaredMethods = Can.ofStream(
                classCache.streamResolvedMethods(classUnderTest));

        var expectations = extractExpectations(classUnderTest);
        expectations.assertAll(declaredMethods);
    }

    @Test
    void javaLangObjectPublicMethodsAreIgnored() {
        var javaLangObjectPublicMethods = classCache.streamPublicMethods(Object.class)
                .collect(Can.toCan());
        assertEquals(Can.empty(), javaLangObjectPublicMethods);
    }

    // -- HELPER

    @SneakyThrows
    private _Expectations extractExpectations(final Class<?> classUnderTest) {
        final Class<?> classThatProvidesExpectations = classUnderTest.getSimpleName().equals("Mixin")
                ? classUnderTest.getEnclosingClass()
                : classUnderTest;
        return (_Expectations) classThatProvidesExpectations.getDeclaredMethod("expectations").invoke(null);
    }

    private void assertContainsResolvedMethod(final Can<ResolvedMethod> declaredMethods, final String methodName) {
        assertContainsMethod(declaredMethods.map(ResolvedMethod::method), methodName);
    }

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
