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
package org.apache.causeway.commons.internal.collections;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collector;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Common Array idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
@UtilityClass
public final class _Arrays {

    // -- PREDICATES

    /**
     * Whether given {@code test} predicate evaluates 'true' for any given pair of elements
     * {@code array1[index]} and {@code array2[index]}, with {@code index=[0..n-1]} and {@code n}
     * the number of elements of {@code array1/2}.
     * @param array1 - nullable
     * @param array2 - nullable
     * @param test - a predicate
     * @return whether there is any matching pair; false - if array1 and array2 are both empty
     * @throws IllegalArgumentException - if array lengths do not match
     * @throws NullPointerException - if {@code test} is null
     */
    public static <T> boolean testAnyMatch(
            final @Nullable T[] array1,
            final @Nullable T[] array2,
            final @NonNull BiPredicate<T, T> test) {

        final int s1 = _NullSafe.size(array1);
        final int s2 = _NullSafe.size(array2);
        if(s1!=s2) {
            throw new IllegalArgumentException("Array length missmatch");
        }
        if(s1==0) {
            return false;
        }
        for(int i=0; i<s1; ++i) {
            if(test.test(array1[i], array2[i])) {
                return true;
            }
        }
        return false;
    }


    /**
     * Whether given {@code test} predicate evaluates 'true' for all given pairs of elements
     * {@code array1[index]} and {@code array2[index]}, with {@code index=[0..n-1]} and {@code n}
     * the number of elements of {@code array1/2}.
     * @param array1 - nullable
     * @param array2 - nullable
     * @param test - a predicate
     * @return whether all pairs match; true - if array1 and array2 are both empty
     * @throws IllegalArgumentException - if array lengths do not match
     * @throws NullPointerException - if {@code test} is null
     */
    public static <T> boolean testAllMatch(
            final @Nullable T[] array1,
            final @Nullable T[] array2,
            final @NonNull BiPredicate<T, T> test) {
        return !testAnyMatch(array1, array2, test.negate());
    }


    /**
     * @param cls
     * @return whether {@code cls} represents an array
     */
    public static boolean isArrayType(final @Nullable Class<?> cls) {
        return cls!=null ? cls.isArray() : false;
    }

    /**
     * For convenience also provided in {@link _Collections}.
     * @param cls
     * @return whether {@code cls} implements the java.util.Collection interface
     * or represents an array
     */
    public static boolean isCollectionOrArrayType(final Class<?> cls) {
        return _Collections.isCollectionType(cls) || _Arrays.isArrayType(cls);
    }

    // -- TO-ARRAY COLLECTORS

    /**
     * Known-size Collector.
     * @param componentType
     * @param length
     */
    public static <T> Collector<T,?,T[]> toArray(final @NonNull Class<T> componentType, final int length){
        return new _Arrays_Collector<T>(componentType, length);
    }

    /**
     * Unknown-size Collector.
     * @param componentType
     */
    public static <T> Collector<T,?,T[]> toArray(final @NonNull Class<T> componentType){
        return new _Arrays_CollectorUnknownSize<T>(componentType);
    }

    // -- CONCATENATION

    /**
     * Returns a new array containing all components {first, *rest}
     * @param first (non-null)
     * @param rest (nullable)
     * @return (non-null)
     * @exception  ArrayStoreException  if an element in the <code>src</code>
     *               array could not be stored into the <code>dest</code> array
     *               because of a type mismatch.
     */
    @SafeVarargs
    public static <T> T[] combine(final @NonNull T first, final @Nullable  T... rest) {
        final int restLength = _NullSafe.size(rest);
        final T[] all = _Casts.uncheckedCast(Array.newInstance(first.getClass(), restLength+1));
        all[0] = first;
        if(restLength>0) {
            System.arraycopy(rest, 0, all, 1, restLength);
        }
        return all;
    }

    /**
     * Returns a new array containing all components {first, *rest}
     * @param type (non-null) explicit array element type
     * @param first (non-null)
     * @param rest (nullable)
     * @return (non-null)
     */
    @SafeVarargs
    public static <T, X extends T, Y extends T> T[] combineWithExplicitType(final @NonNull Class<T> type, final @NonNull X first, final @Nullable  Y... rest) {
        final int restLength = _NullSafe.size(rest);
        final T[] all = _Casts.uncheckedCast(Array.newInstance(type, restLength+1));
        all[0] = first;
        if(restLength>0) {
            System.arraycopy(rest, 0, all, 1, restLength);
        }
        return all;
    }

    /**
     * Returns a new array containing all components {*first, *rest}
     * @param first (nullable)
     * @param rest (nullable)
     * @return (non-null)
     */
    @SafeVarargs
    public static <T> T[] combine(final T[] first, final T... rest) {
        final int firstLength = _NullSafe.size(first);
        final int restLength = _NullSafe.size(rest);
        if(firstLength + restLength == 0) {
            return _Casts.uncheckedCast(_Constants.emptyObjects);
        }
        final Class<?> componentType = firstLength>0 ? first[0].getClass() : rest[0].getClass();
        final T[] all = _Casts.uncheckedCast(Array.newInstance(componentType, firstLength + restLength));
        System.arraycopy(first, 0, all, 0, firstLength);
        System.arraycopy(rest, 0, all, firstLength, restLength);
        return all;
    }

    // -- CONSTRUCTION

    /**
     * Copies a collection's elements into an array.
     *
     * @param collection - the collection to convert
     * @param componentType - type of the elements
     * @return a newly-allocated array into which all the elements of the iterable
     *     have been copied (non-null)
     */
    public static <T> T[] toArray(
            final @Nullable Collection<? extends T> collection,
            final @NonNull  Class<T> componentType) {
        return _NullSafe.stream(collection)
                .collect(toArray(componentType, collection!=null ? collection.size() : 0));
    }

    /**
     * Copies an iterable's elements into an array.
     *
     * @param iterable - the iterable to copy
     * @param componentType - type of the elements
     * @return a newly-allocated array into which all the elements of the iterable
     *     have been copied (non-null)
     */
    public static <T> T[] toArray(
            final @Nullable Iterable<? extends T> iterable,
            final @NonNull  Class<T> componentType) {
// unnecessary optimization
//        if(iterable!=null && (iterable instanceof Collection)) {
//            return toArray((Collection<? extends T>) iterable, componentType);
//        }
        return _NullSafe.stream(iterable)
                .collect(toArray(componentType));
    }

    // -- MODIFICATION

    /**
     * Returns a new array of size {@code array.length - 1} with the element at {@code array[index]} removed.
     *
     * @param <T>
     * @param array
     * @param index
     */
    public static <T> T[] removeByIndex(final T[] array, final int index) {
        if(array==null || array.length<1) {
            throw new IllegalArgumentException("Array must be of lenght 1 or larger.");
        }
        if(index<0 || index>=array.length) {
            val msg = String.format("Array index %d is out of bounds [0, %d]", index, array.length-1);
            throw new IllegalArgumentException(msg);
        }
        final T[] result = Arrays.copyOf(array, array.length - 1);
        // copy the elements from index + 1 till end
        // from original array to the new array
        val remaining = result.length - index;
        System.arraycopy(array, index+1, result, index, remaining);
        return result;
    }

    /**
     * Returns {@code null} if given {@code array} is {@code null} or of length zero,
     * returns given {@code array} otherwise.
     *
     * @param <T>
     * @param array
     * @return null for empty arrays
     */
    public static @Nullable <T> T[] emptyToNull(final @Nullable T[] array) {
        if(array!=null && array.length==0) {
            return null;
        }
        return array;
    }

    /**
     * Returns a sub-array of given array. The
     * sub-array begins at the specified {@code beginIndex} and
     * extends to the element at index {@code endIndex - 1}.
     * Thus the length of the sub-array is {@code endIndex-beginIndex}.
     *
     * @param      array
     * @param      beginIndex   the beginning index, inclusive.
     * @param      endIndex     the ending index, exclusive.
     * @return     the specified sub-array, which always is a copy
     * @exception  IndexOutOfBoundsException  if the
     *             {@code beginIndex} is negative, or
     *             {@code endIndex} is larger than the length of
     *             {@code array} object, or
     *             {@code beginIndex} is larger than
     *             {@code endIndex}.
     */
    public static <T> T[] subArray(final @NonNull T[] array, final int beginIndex, final int endIndex) {
        if (beginIndex < 0) {
            throw new ArrayIndexOutOfBoundsException(beginIndex);
        }
        if (endIndex > array.length) {
            throw new ArrayIndexOutOfBoundsException(endIndex);
        }
        final int subLen = endIndex - beginIndex;
        if (subLen < 0) {
            throw new ArrayIndexOutOfBoundsException(subLen);
        }
        return Arrays.copyOfRange(array, beginIndex, endIndex);
    }



    // -- COMPONENT TYPE INFERENCE

    /**
     * Returns the inferred element type of the specified array type
     * @param arrayType - type of the array for which to infer the element type
     * @return optionally the inferred type, based on whether inference is possible
     */
    public static Optional<Class<?>> inferComponentType(
            final @Nullable Class<?> arrayType) {
        if(!isArrayType(arrayType)) {
            return Optional.empty();
        }
        return Optional.ofNullable(arrayType.getComponentType());
    }

    // -- ACCESSOR

    /**
     * Optionally returns an array element by index, based on whether the index is valid.
     * @param <T>
     * @param array
     * @param index
     */
    public static <T> Optional<T> get(final @Nullable T[] array, final int index) {
        val size = _NullSafe.size(array);
        if(size==0) {
            return Optional.empty();
        }
        val minIndex = 0;
        val maxIndex = size - 1;
        if(index < minIndex ||  index > maxIndex) {
            return Optional.empty();
        }
        return Optional.ofNullable(array[index]);
    }

    // -- TRANSFORMATION

    /**
     * Transforms given {@code array} into a new array of {@code resultElementType} and same size,
     * applying the {@code mapper} function
     * to each element of {@code array}.
     * Returns {@code null} if {@code array} is {@code null};
     * @param <T>
     * @param <R>
     * @param array
     * @param resultElementType
     * @param mapper
     * @return nullable
     */
    @Nullable
    public static <T, R> R[] map(
            final @Nullable T[] array,
            final @NonNull Class<R> resultElementType,
            final @NonNull Function<T, R> mapper) {

        if (array == null) {
            return null;
        }
        val mappedArray = _Casts.<R[]>uncheckedCast(
                Array.newInstance(resultElementType, array.length));
        int i = 0;
        for (val element : array) {
            mappedArray[i++] = mapper.apply(element);
        }
        return mappedArray;
    }

    /**
     * Transforms given {@code array} into a new object array of same size,
     * applying the {@code mapper} function
     * to each element of {@code array}.
     * Returns {@code null} if {@code array} is {@code null};
     * @param <T>
     * @param array
     * @param mapper
     * @return nullable
     */
    @Nullable
    public static <T> Object[] map(
            final @Nullable T[] array,
            final @NonNull Function<T, ?> mapper) {

        if (array == null) {
            return null;
        }
        val mappedArray = new Object[array.length];
        int i = 0;
        for (val element : array) {
            mappedArray[i++] = mapper.apply(element);
        }
        return mappedArray;
    }

    /**
     * Transforms given {@code collection} into an array of {@code resultElementType} and same size,
     * applying the {@code mapper} function
     * to each element of {@code collection}.
     * Returns {@code null} if {@code collection} is {@code null};
     * @param <T>
     * @param <R>
     * @param collection
     * @param resultElementType
     * @param mapper
     * @return nullable
     */
    @Nullable
    public static <T, R> R[] mapCollection(
            final @Nullable Collection<T> collection,
            final @NonNull Class<R> resultElementType,
            final @NonNull Function<T, R> mapper) {

        if (collection == null) {
            return null;
        }
        val mappedArray = _Casts.<R[]>uncheckedCast(
                Array.newInstance(resultElementType, collection.size()));
        int i = 0;
        for (val element : collection) {
            mappedArray[i++] = mapper.apply(element);
        }
        return mappedArray;
    }

    /**
     * Transforms given {@code collection} into an object array of same size,
     * applying the {@code mapper} function
     * to each element of {@code collection}.
     * Returns {@code null} if {@code collection} is {@code null};
     * @param <T>
     * @param collection
     * @param mapper
     * @return nullable
     */
    @Nullable
    public static <T> Object[] mapCollection(
            final @Nullable Collection<T> collection,
            final @NonNull Function<T, ?> mapper) {

        if (collection == null) {
            return null;
        }
        val mappedArray = new Object[collection.size()];
        int i = 0;
        for (val element : collection) {
            mappedArray[i++] = mapper.apply(element);
        }
        return mappedArray;
    }


    // --

}
