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
package org.apache.causeway.testing.fakedata.applib.services;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import org.apache.causeway.applib.annotation.Programmatic;

/**
 * Returns random instance of a provided collection, with overloads allowing instances to be excluded according to a
 * {@link Predicate}.
 *
 * @since 2.0 {@index}
 */
public class Collections extends AbstractRandomValueGenerator {

    public Collections(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    public <E extends Enum<E>> E anyEnum(final Class<E> enumType) {
        final E[] enumConstants = enumType.getEnumConstants();
        return enumConstants[fake.ints().upTo(enumConstants.length)];
    }

    public <E extends Enum<E>> E anyEnumExcept(final Class<E> enumType, final Predicate<E> except) {
        return find(() -> anyEnum(enumType), except);
    }

    public <T> T anyBounded(final Class<T> cls) {
        final List<T> list = fake.repositoryService.allInstances(cls);
        return anyOf(list);
    }

    public <T> T anyBoundedExcept(final Class<T> cls, final Predicate<T> except) {
        final List<T> list = fake.repositoryService.allInstances(cls);
        return anyOfExcept(list, except);
    }

    public <T> T anyOf(final List<T> list) {
        final int randomIdx = fake.ints().upTo(list.size());
        return list.get(randomIdx);
    }

    public <T> T anyOf(final Collection<T> collection) {
        final int randomIdx = fake.ints().upTo(collection.size());
        int i = 0;
        for (T element : collection) {
            if (randomIdx == i++) {
                return element;
            }
        }
        throw new RuntimeException(
                "failed to obtain random element from collection - most likely a bug in the FakeData service itself");
    }

    public <T> T anyOfExcept(final List<T> list, final Predicate<T> except) {
        return find(() -> anyOf(list), except);
    }

    public <T> T anyOfExcept(final Collection<T> collection, final Predicate<T> except) {
        return find(() -> anyOf(collection), except);
    }

    public char anyOf(final char... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    public char anyOfExcept(final char[] elements, final Predicate<Character> except) {
        return find(() -> anyOf(elements), except);
    }

    public byte anyOf(final byte... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    public byte anyOfExcept(final byte[] elements, final Predicate<Byte> except) {
        return find(() -> anyOf(elements), except);
    }

    public short anyOf(final short... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    public short anyOfExcept(final short[] elements, final Predicate<Short> except) {
        return find(() -> anyOf(elements), except);
    }

    public int anyOf(final int... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    public int anyOfExcept(final int[] elements, final Predicate<Integer> except) {
        return find(() -> anyOf(elements), except);
    }

    public long anyOf(final long... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    @Programmatic
    public long anyOfExcept(final long[] elements, final Predicate<Long> except) {
        return find(() -> anyOf(elements), except);
    }

    public float anyOf(final float... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    public float anyOfExcept(final float[] elements, final Predicate<Float> except) {
        return find(() -> anyOf(elements), except);
    }

    public double anyOf(final double... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    public double anyOfExcept(final double[] elements, final Predicate<Double> except) {
        return find(() -> anyOf(elements), except);
    }

    public boolean anyOf(final boolean... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    public boolean anyOfExcept(final boolean[] elements, final Predicate<Boolean> except) {
        return find(() -> anyOf(elements), except);
    }

    public <T> T anyOf(final T... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    public <T> T anyOfExcept(final T[] elements, final Predicate<T> except) {
        return find(() -> anyOf(elements), except);
    }

    static <E> E find(final Callable<E> block, final Predicate<E> except) {
        for(int i=0; i<100; i++) {
            final E e;
            try {
                e = block.call();
            } catch (Exception e1) {
                throw new RuntimeException("Problem finding candidate values");
            }
            if(!except.test(e)) {
                return e;
            }
        }
        throw new RuntimeException("Failed to find a random element in collection");
    }

}
