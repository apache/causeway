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

import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

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
    public static <T> Stream<T> stream(@Nullable final T[] array) {
        return array!=null ? Stream.of(array) : Stream.empty();
    }

    /**
     * If {@code nullable} is {@code null} returns the empty stream,
     * otherwise returns a Stream containing the single element {@code nullable}.
     * 
     * @param nullable
     * @return non-null stream object
     */
    public static <T> Stream<T> streamNullable(@Nullable final T nullable) {
        return nullable != null ? Stream.of(nullable) : Stream.empty();
    }

    /**
     * If {@code collection} is {@code null} returns the empty stream,
     * otherwise returns a stream of the collection's elements.
     * @param coll
     * @return non-null stream object
     */
    public static <T> Stream<T> stream(@Nullable final Collection<T> coll){
        return coll!=null ? coll.stream() : Stream.empty();
    }

    /**
     * If {@code iterable} is {@code null} returns the empty stream,
     * otherwise returns a stream of the iterable's elements.
     * @param iterable
     * @return non-null stream object
     */
    public static <T> Stream<T> stream(@Nullable final Iterable<T> iterable){
        return iterable!=null ? stream(iterable.iterator()) : Stream.empty();
    }

    /**
     * If {@code iterator} is {@code null} returns the empty stream,
     * otherwise returns a stream of the iterator's elements.
     * @param iterator
     * @return non-null stream object
     */
    public static <T> Stream<T> stream(@Nullable final Iterator<T> iterator){
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
    public static <T> Stream<T> stream(@Nullable final Stream<T> stream) {
        return stream!=null ? stream : Stream.empty();
    }

    /**
     * If {@code enumeration} is {@code null} returns the empty stream,
     * otherwise returns a stream of the enumeration's elements.
     * @param enumeration
     * @return non-null stream object
     */
    public static <T> Stream<T> stream(final Enumeration<T> enumeration){
        return enumeration!=null
                ? stream(toIterator(enumeration))
                        : Stream.empty();
    }

    // [ahuber] not public, since one time use of enumeration only!
    private static <T> Iterator<T> toIterator(final Enumeration<T> e){
        return new Iterator<T>() {
            @Override
            public T next() {
                return e.nextElement();
            }
            @Override
            public boolean hasNext() {
                return e.hasMoreElements();
            }
        };
    }


    // -- ABSENCE/PRESENCE PREDICATES

    /**
     * Allows to replace a lambda expression {@code x->x!=null} with {@code NullSafe::isPresent}.
     * Equivalent to {@link java.util.Objects#nonNull(Object)}.
     * @param x
     * @return whether {@code x} is not null (present).
     */
    public static boolean isPresent(Object x) {
        return x!=null;
    }

    /**
     * Allows to replace a lambda expression {@code x->x==null} with {@code NullSafe::isAbsent}.<br/>
     * Equivalent to {@link java.util.Objects#isNull(Object)}.
     * @param x
     * @return whether {@code x} is null (absent).
     */
    public static boolean isAbsent(Object x) {
        return x==null;
    }

    // -- EQUALS/COMPARE

    /**
     * equivalent to {@link java.util.Objects#equals(Object, Object)}
     */
    public static boolean equals(final Object x, final Object y) {
        return Objects.equals(x, y);
    }

    /**
     * Natural order compare, with nulls ordered first.
     * @param x
     * @param y
     * @return
     */
    public static <T extends Comparable<T>> int compareNullsFirst(final T x, final T y) {
        return Objects.compare(x, y, Comparator.nullsFirst(Comparator.naturalOrder()));

    }

    /**
     * Natural order compare, with nulls ordered last.
     * @param x
     * @param y
     * @return
     */
    public static <T extends Comparable<T>> int compareNullsLast(final T x, final T y) {
        return Objects.compare(x, y, Comparator.nullsLast(Comparator.naturalOrder()));

    }

    // -- EMTPY CHECKS

    public static boolean isEmpty(String x) { return x==null || x.length() == 0; }
    public static boolean isEmpty(Collection<?> x) { return x==null || x.size() == 0; }
    public static boolean isEmpty(Map<?,?> x) { return x==null || x.size() == 0; }
    public static boolean isEmpty(boolean[] array){ return array==null || array.length == 0;}
    public static boolean isEmpty(byte[] array){ return array==null || array.length == 0;}
    public static boolean isEmpty(char[] array){ return array==null || array.length == 0;}
    public static boolean isEmpty(double[] array){ return array==null || array.length == 0;}
    public static boolean isEmpty(float[] array){ return array==null || array.length == 0;}
    public static boolean isEmpty(int[] array){ return array==null || array.length == 0;}
    public static boolean isEmpty(long[] array){ return array==null || array.length == 0;}
    public static boolean isEmpty(short[] array){ return array==null || array.length == 0;}
    public static <T> boolean isEmpty(T[] array){ return array==null || array.length == 0;}

    // -- SIZE/LENGTH CHECKS

    public static int size(String x){ return x!=null ? x.length() : 0; }
    public static int size(Collection<?> x){ return x!=null ? x.size() : 0; }
    public static int size(Map<?,?> x){ return x!=null ? x.size() : 0; }
    public static int size(boolean[] array){ return array!=null ? array.length : 0; }
    public static int size(byte[] array){ return array!=null ? array.length : 0; }
    public static int size(char[] array){ return array!=null ? array.length : 0; }
    public static int size(double[] array){ return array!=null ? array.length : 0; }
    public static int size(float[] array){ return array!=null ? array.length : 0; }
    public static int size(int[] array){ return array!=null ? array.length : 0; }
    public static int size(long[] array){ return array!=null ? array.length : 0; }
    public static int size(short[] array){ return array!=null ? array.length : 0; }
    public static <T> int size(T[] array){ return array!=null ? array.length : 0; }


    // -- MAP

    /**
     * Null-safe variant of {@link java.util.Map#getOrDefault(Object, Object)}
     * @param map
     * @param key
     * @param defaultValue
     * @return
     */
    public final static <K,V> V getOrDefault(final Map<K, V> map, final K key, final V defaultValue) {
        if(map==null || key==null) {
            return defaultValue;
        }
        return map.getOrDefault(key, defaultValue);
    }





}
