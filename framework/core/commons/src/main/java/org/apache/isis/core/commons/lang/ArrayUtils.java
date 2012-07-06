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

package org.apache.isis.core.commons.lang;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ArrayUtils {

    private ArrayUtils() {
    }

    public static Object[] convertCharToCharacterArray(final Object originalArray) {
        final char[] original = (char[]) originalArray;
        final int len = original.length;
        final Character[] converted = new Character[len];
        for (int i = 0; i < converted.length; i++) {
            converted[i] = Character.valueOf(original[i]);
        }
        return converted;
    }

    public static <T> T[] combine(final T[]... arrays) {
        final List<T> combinedList = new ArrayList<T>();
        for (final T[] array : arrays) {
            for (final T t : array) {
                combinedList.add(t);
            }
        }
        return combinedList.toArray(arrays[0]); // using 1st element of arrays
                                                // to specify the type
    }

    /**
     * Creates a mutable copy of the provided array.
     */
    public static <T> List<T> asList(final T[] items) {
        final List<T> list = new ArrayList<T>();
        for (final T item : items) {
            list.add(item);
        }
        return list;
    }

    /**
     * Creates a mutable copy of the provided array, eliminating duplicates.
     * 
     * <p>
     * The order of the items will be preserved.
     */
    public static <T> Set<T> asOrderedSet(final T[] items) {
        final LinkedHashSet<T> list = new LinkedHashSet<T>();
        if (items != null) {
            for (final T item : items) {
                list.add(item);
            }
        }
        return list;
    }

    /**
     * Creates a mutable list of the provided array, also appending the
     * additional element(s).
     */
    public static <T> List<T> concat(final T[] elements, final T... elementsToAppend) {
        final List<T> result = new ArrayList<T>();
        for (final T element : elements) {
            result.add(element);
        }
        for (final T element : elementsToAppend) {
            if (element != null) {
                result.add(element);
            }
        }
        return result;
    }

    public static String[] append(final String[] args, final String... moreArgs) {
        final ArrayList<String> argList = new ArrayList<String>();
        argList.addAll(Arrays.asList(args));
        argList.addAll(Arrays.asList(moreArgs));
        return argList.toArray(new String[] {});
    }

    /**
     * Creates a mutable list of the provided array, also appending the
     * additional element(s).
     */
    public static <T> List<T> concat(final T[] elements, final List<T> elementsToAppend) {
        final List<T> result = new ArrayList<T>();
        for (final T element : elements) {
            result.add(element);
        }
        for (final T element : elementsToAppend) {
            if (element != null) {
                result.add(element);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <D, S> D[] copy(final S[] source, final Class<D> cls) {
        if (source == null) {
            throw new IllegalArgumentException("Source array cannot be null");
        }
        final D[] destination = (D[]) Array.newInstance(cls, source.length);
        System.arraycopy(source, 0, destination, 0, source.length);
        return destination;
    }

}
