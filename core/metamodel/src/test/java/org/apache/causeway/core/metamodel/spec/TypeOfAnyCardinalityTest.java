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
import org.springframework.core.ResolvableType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;

import lombok.SneakyThrows;
import lombok.val;

class TypeOfAnyCardinalityTest {

    // -- SCENARIO: ARRAY

    static abstract class X {
        public abstract CharSequence[] someStrings();
    }

    static class Y extends X {
        @Override
        public CharSequence[] someStrings() {
            return new String[]{};
        }
    }

    static class Z extends X {
        @Override
        public String[] someStrings() {
            return new String[]{};
        }
    }

    @Test
    void testArray() {

        val array = new String[]{};

        assertEquals(
                ProgrammingModelConstants.CollectionSemantics.ARRAY,
                ProgrammingModelConstants.CollectionSemantics.valueOf(array.getClass())
                    .orElse(null));

        val arC = new CharSequence[] {};
        val arS = new String[] {};

        test(X.class, Y.class, Z.class,
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
    void testString() {
        test(A.class, B.class, C.class,
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
    void testUpperBounded() {
        test(E.class, F.class, G.class,
                CharSequence.class, CharSequence.class, String.class,
                Set.class, Set.class, SortedSet.class);
    }

    // -- HELPER

    @SneakyThrows
    void test(final Class<?> a, final Class<?> b, final Class<?> c,
            final Class<?> genericA, final Class<?> genericB, final Class<?> genericC,
            final Class<?> contA, final Class<?> contB, final Class<?> contC) {

        val methodInA = a.getMethod("someStrings", _Constants.emptyClasses);
        val methodInB = b.getMethod("someStrings", _Constants.emptyClasses);
        val methodInC = c.getMethod("someStrings", _Constants.emptyClasses);

        assertNotNull(methodInA);
        assertNotNull(methodInB);
        assertNotNull(methodInC);

        val returnA = ResolvableType.forMethodReturnType(methodInA, a);
        val returnB = ResolvableType.forMethodReturnType(methodInB, b);
        val returnC = ResolvableType.forMethodReturnType(methodInC, c);

        val genericArgA = returnA.isArray()
                ? returnA.getComponentType()
                : returnA.getGeneric(0);
        val genericArgB = returnB.isArray()
                ? returnB.getComponentType()
                : returnB.getGeneric(0);
        val genericArgC = returnC.isArray()
                ? returnC.getComponentType()
                : returnC.getGeneric(0);

        assertNotNull(genericArgA);
        assertNotNull(genericArgB);
        assertNotNull(genericArgC);

        assertEquals(genericA, genericArgA.toClass());
        assertEquals(genericB, genericArgB.toClass());
        assertEquals(genericC, genericArgC.toClass());

        val typeA = TypeOfAnyCardinality.forMethodReturn(a, methodInA);
        val typeB = TypeOfAnyCardinality.forMethodReturn(b, methodInB);
        val typeC = TypeOfAnyCardinality.forMethodReturn(c, methodInC);

        assertEquals(genericA, typeA.getElementType());
        assertEquals(genericB, typeB.getElementType());
        assertEquals(genericC, typeC.getElementType());

        assertEquals(contA, typeA.getContainerType().orElse(null));
        assertEquals(contB, typeB.getContainerType().orElse(null));
        assertEquals(contC, typeC.getContainerType().orElse(null));

    }

}
