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
package org.apache.causeway.core.metamodel.spec;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.core.ResolvableType;

import org.apache.causeway.commons.internal.reflection._GenericResolver;
import org.apache.causeway.commons.semantics.CollectionSemantics;

import lombok.SneakyThrows;

class TypeOfAnyCardinalityTest {

    // -- SCENARIO: ARRAY

    static abstract class X {
        public abstract CharSequence[] someStrings();
    }
    static class Y extends X {
        @Override
        public CharSequence[] someStrings() { return new String[]{}; }
    }
    static class Z extends X {
        @Override
        public String[] someStrings() { return new String[]{}; }
    }

    @Test
    void array() {

        var array = new String[]{};

        assertEquals(
                CollectionSemantics.ARRAY,
                CollectionSemantics.valueOf(array.getClass())
                    .orElse(null));

        var arC = new CharSequence[] {};
        var arS = new String[] {};

        assertTypeDetected(X.class, Y.class, Z.class,
                CharSequence.class, CharSequence.class, String.class,
                arC.getClass(), arC.getClass(), arS.getClass());
    }

    // -- SCENARIO: SET vs SORTED_SET

    static abstract class A {
        public abstract Set<String> someStrings();
    }

    static class B extends A {
        @Override
        public Set<String> someStrings() {
            return Collections.emptySet();
        }
    }

    static class C extends A {
        @Override
        public SortedSet<String> someStrings() {
            return Collections.emptySortedSet();
        }
    }

    @Test
    void string() {
        assertTypeDetected(A.class, B.class, C.class,
                String.class, String.class, String.class,
                Set.class, Set.class, SortedSet.class);
    }

    // -- SCENARIO: UPPERBOUND

    static abstract class E {
        public abstract Set<? extends CharSequence> someStrings();
    }

    static class F extends E {
        @Override
        public Set<? extends CharSequence> someStrings() {
            return Collections.emptySet();
        }
    }

    static class G extends E {
        @Override
        public SortedSet<String> someStrings() {
            return Collections.emptySortedSet();
        }
    }

    @Test
    void upperBounded() {
        assertTypeDetected(E.class, F.class, G.class,
                CharSequence.class, CharSequence.class, String.class,
                Set.class, Set.class, SortedSet.class);
    }

    // -- HELPER

    @SneakyThrows
    void assertTypeDetected(final Class<?> a, final Class<?> b, final Class<?> c,
            final Class<?> genericA, final Class<?> genericB, final Class<?> genericC,
            final Class<?> contA, final Class<?> contB, final Class<?> contC) {

        var methodInA = _GenericResolver.testing.resolveMethod(a, "someStrings");
        var methodInB = _GenericResolver.testing.resolveMethod(b, "someStrings");
        var methodInC = _GenericResolver.testing.resolveMethod(c, "someStrings");

        assertNotNull(methodInA);
        assertNotNull(methodInB);
        assertNotNull(methodInC);

        var returnA = ResolvableType.forMethodReturnType(methodInA.method(), a);
        var returnB = ResolvableType.forMethodReturnType(methodInB.method(), b);
        var returnC = ResolvableType.forMethodReturnType(methodInC.method(), c);

        var genericArgA = returnA.isArray()
                ? returnA.getComponentType()
                : returnA.getGeneric(0);
        var genericArgB = returnB.isArray()
                ? returnB.getComponentType()
                : returnB.getGeneric(0);
        var genericArgC = returnC.isArray()
                ? returnC.getComponentType()
                : returnC.getGeneric(0);

        assertNotNull(genericArgA);
        assertNotNull(genericArgB);
        assertNotNull(genericArgC);

        assertEquals(genericA, genericArgA.toClass());
        assertEquals(genericB, genericArgB.toClass());
        assertEquals(genericC, genericArgC.toClass());

        var typeA = _GenericResolver.forMethodReturn(methodInA);
        var typeB = _GenericResolver.forMethodReturn(methodInB);
        var typeC = _GenericResolver.forMethodReturn(methodInC);

        assertEquals(genericA, typeA.elementType());
        assertEquals(genericB, typeB.elementType());
        assertEquals(genericC, typeC.elementType());

        assertEquals(contA, typeA.containerType().orElse(null));
        assertEquals(contB, typeB.containerType().orElse(null));
        assertEquals(contC, typeC.containerType().orElse(null));

    }

}
