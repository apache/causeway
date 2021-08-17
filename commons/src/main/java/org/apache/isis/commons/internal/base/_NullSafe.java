/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.isis.commons.internal.base;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.apache.isis.commons.collections.Can;

/**
 * <h1>- internal use only -</h1>
 * <p>
 *  Provides convenient null check / null safe methods primarily
 * to shortcut null-check idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 *
 */
public final class _NullSafe {

    private _NullSafe(){}

    // -- STREAM CREATION

    /**
     * If {@code array} is {@code null} returns the empty stream,
     * otherwise returns a stream of the array's elements.
     * @param array
     * @return non-null stream object
     */
    public static <T> Stream<T> stream(final @Nullable  T[] array) {
        return array!=null
                ? Stream.of(array)
                : Stream.empty();
    }

    /**
     * If {@code nullable} is {@code null} returns the empty stream,
     * otherwise returns a Stream containing the single element {@code nullable}.
     *
     * @param nullable
     * @return non-null stream object
     */
    public static <T> Stream<T> streamNullable(final @Nullable T nullable) {
        return nullable != null
                ? Stream.of(nullable)
                : Stream.empty();
    }

    /**
     * If {@code can} is {@code null} returns the empty stream,
     * otherwise returns a stream of the can's elements.
     * @param can
     * @return non-null stream object
     */
    public static <T> Stream<T> stream(final @Nullable Can<T> can){
        return can!=null
                ? can.stream()
                : Stream.empty();
    }

    /**
     * If {@code collection} is {@code null} returns the empty stream,
     * otherwise returns a stream of the collection's elements.
     * @param coll
     * @return non-null stream object
     */
    public static <T> Stream<T> stream(final @Nullable Collection<T> coll){
        return coll!=null
                ? coll.stream()
                : Stream.empty();
    }

    /**
     * If {@code iterable} is {@code null} returns the empty stream,
     * otherwise returns a stream of the iterable's elements.
     * @param iterable
     * @return non-null stream object
     */
    public static <T> Stream<T> stream(final @Nullable Iterable<T> iterable){
        return iterable!=null
                ? stream(iterable.iterator())
                : Stream.empty();
    }

    /**
     * If {@code iterator} is {@code null} returns the empty stream,
     * otherwise returns a stream of the iterator's elements.
     * @param iterator
     * @return non-null stream object
     */
    public static <T> Stream<T> stream(final @Nullable Iterator<T> iterator){
        return iterator!=null
                ? StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                        false) //not parallel
                : Stream.empty();
    }

    /**
     * If {@code stream} is {@code null} returns the empty stream,
     * otherwise returns the stream argument.
     * @param stream
     * @return non-null stream object
     */
    public static <T> Stream<T> stream(final @Nullable Stream<T> stream) {
        return stream!=null
                ? stream
                : Stream.empty();
    }

    /**
     * If {@code enumeration} is {@code null} returns the empty stream,
     * otherwise returns a stream of the enumeration's elements.
     * @param enumeration
     * @return non-null stream object
     */
    public static <T> Stream<T> stream(final @Nullable Enumeration<T> enumeration){
        return enumeration!=null
                ? StreamSupport.stream(toSpliterator(enumeration), /*parallel*/false)
                : Stream.empty();
    }

    // not public, used internally for stream(Enumeration) only
    private static <T> Spliterator<T> toSpliterator(final Enumeration<T> e){
        return new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED) {
            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if(e.hasMoreElements()) {
                    action.accept(e.nextElement());
                    return true;
                }
                return false;
            }
            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                while(e.hasMoreElements()) {
                    action.accept(e.nextElement());
                }
            }
        };
    }

    public static Stream<?> streamAutodetect(final @Nullable Object pojo) {
        if(pojo==null) {
            return Stream.empty();
        }
        if(pojo instanceof Collection) {
            return ((Collection<?>)pojo).stream();
        }
        if(pojo instanceof Can) {
            return ((Can<?>)pojo).stream();
        }
        if(pojo.getClass().isArray()) {
            if(Array.getLength(pojo)==0) return Stream.empty();
            if(pojo instanceof Object[]) return Stream.of((Object[]) pojo);
            if(pojo instanceof boolean[]) return primitiveStream((boolean[]) pojo);
            if(pojo instanceof byte[]) return primitiveStream((byte[]) pojo);
            if(pojo instanceof char[]) return primitiveStream((char[]) pojo);
            if(pojo instanceof double[]) return primitiveStream((double[]) pojo);
            if(pojo instanceof float[]) return primitiveStream((float[]) pojo);
            if(pojo instanceof int[]) return primitiveStream((int[]) pojo);
            if(pojo instanceof long[]) return primitiveStream((long[]) pojo);
            if(pojo instanceof short[]) return primitiveStream((short[]) pojo);
        }
        if(pojo instanceof Iterable) {
            return stream((Iterable<?>)pojo);
        }
        if(pojo instanceof Enumeration) {
            return stream((Enumeration<?>)pojo);
        }
        return Stream.of(pojo);
    }

    // not null-safe, but for performance reasons not checked (private anyway) ...

    private static Stream<Boolean> primitiveStream(final boolean[] array) {
        return IntStream.range(0, array.length).mapToObj(s -> array[s]);
    }

    private static Stream<Byte> primitiveStream(final byte[] array) {
        return IntStream.range(0, array.length).mapToObj(s -> array[s]);
    }

    private static Stream<Character> primitiveStream(final char[] array) {
        return IntStream.range(0, array.length).mapToObj(s -> array[s]);
    }

    private static Stream<Float> primitiveStream(final float[] array) {
        return IntStream.range(0, array.length).mapToObj(s -> array[s]);
    }

    private static Stream<Double> primitiveStream(final double[] array) {
        return IntStream.range(0, array.length).mapToObj(s -> array[s]);
    }

    private static Stream<Short> primitiveStream(final short[] array) {
        return IntStream.range(0, array.length).mapToObj(s -> array[s]);
    }

    private static Stream<Integer> primitiveStream(final int[] array) {
        return IntStream.range(0, array.length).mapToObj(s -> array[s]);
    }

    private static Stream<Long> primitiveStream(final long[] array) {
        return IntStream.range(0, array.length).mapToObj(s -> array[s]);
    }

    // -- ABSENCE/PRESENCE PREDICATES


    /**
     * Equivalent to {@link java.util.Objects#nonNull(Object)}.
     * @param x
     * @return whether {@code x} is not null (present).
     *
     * @apiNote we keep this, arguably provides better code readability than {@code Objects#nonNull}
     */
    public static boolean isPresent(final @Nullable Object x) {
        return x!=null;
    }

    /**
     * Equivalent to {@link java.util.Objects#isNull(Object)}.
     * @param x
     * @return whether {@code x} is null (absent).
     *
     * @apiNote we keep this, arguably provides better code readability than {@code Objects#isNull}
     */
    public static boolean isAbsent(final @Nullable Object x) {
        return x==null;
    }

    // -- EMTPY CHECKS

    public static boolean isEmpty(final @Nullable String x) { return x==null || x.length() == 0; }
    public static boolean isEmpty(final @Nullable Can<?> x) { return x==null || x.size() == 0; }
    public static boolean isEmpty(final @Nullable Collection<?> x) { return x==null || x.size() == 0; }
    public static boolean isEmpty(final @Nullable Map<?,?> x) { return x==null || x.size() == 0; }
    public static boolean isEmpty(final @Nullable boolean[] array){ return array==null || array.length == 0;}
    public static boolean isEmpty(final @Nullable byte[] array){ return array==null || array.length == 0;}
    public static boolean isEmpty(final @Nullable char[] array){ return array==null || array.length == 0;}
    public static boolean isEmpty(final @Nullable double[] array){ return array==null || array.length == 0;}
    public static boolean isEmpty(final @Nullable float[] array){ return array==null || array.length == 0;}
    public static boolean isEmpty(final @Nullable int[] array){ return array==null || array.length == 0;}
    public static boolean isEmpty(final @Nullable long[] array){ return array==null || array.length == 0;}
    public static boolean isEmpty(final @Nullable short[] array){ return array==null || array.length == 0;}
    public static <T> boolean isEmpty(final @Nullable T[] array){ return array==null || array.length == 0;}

    // -- SIZE/LENGTH CHECKS

    public static int size(final @Nullable String x){ return x!=null ? x.length() : 0; }
    public static int size(final @Nullable Collection<?> x){ return x!=null ? x.size() : 0; }
    public static int size(final @Nullable Map<?,?> x){ return x!=null ? x.size() : 0; }
    public static int size(final @Nullable boolean[] array){ return array!=null ? array.length : 0; }
    public static int size(final @Nullable byte[] array){ return array!=null ? array.length : 0; }
    public static int size(final @Nullable char[] array){ return array!=null ? array.length : 0; }
    public static int size(final @Nullable double[] array){ return array!=null ? array.length : 0; }
    public static int size(final @Nullable float[] array){ return array!=null ? array.length : 0; }
    public static int size(final @Nullable int[] array){ return array!=null ? array.length : 0; }
    public static int size(final @Nullable long[] array){ return array!=null ? array.length : 0; }
    public static int size(final @Nullable short[] array){ return array!=null ? array.length : 0; }
    public static <T> int size(final @Nullable T[] array){ return array!=null ? array.length : 0; }


    // -- MAP

    /**
     * Null-safe variant of {@link java.util.Map#getOrDefault(Object, Object)}
     * @param map
     * @param key
     * @param defaultValue - (null-able)
     * @return (null-able)
     */
    @Nullable
    public static final <K,V> V getOrDefault(
            final @Nullable Map<K, V> map,
            final @Nullable K key,
            final @Nullable V defaultValue) {

        if(map==null || key==null) {
            return defaultValue;
        }
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * Null-safe variant of {@link java.util.Map#entrySet()}
     * @param <K>
     * @param <V>
     * @param map
     */
    public static <K, V> Set<Map.Entry<K, V>> entrySet(final @Nullable Map<K, V> map) {
        return map==null
                ? Collections.emptySet()
                : map.entrySet();
    }


}
