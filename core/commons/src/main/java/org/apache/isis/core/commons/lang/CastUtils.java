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

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Vector;

/**
 * Helpers to co-erce existing (Java 1.1 code) into type-safe generics without
 * having to suppress compiler warnings all over the place.
 * 
 */
public final class CastUtils {

    private CastUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(final Object obj) {
        return (T) obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> Enumeration<T> enumerationOver(final Object obj, final Class<T> castTo) {
        return (Enumeration<T>) obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> iteratorOver(final Object obj, final Class<T> castTo) {
        return (Iterator<T>) obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<T> collectionOf(final Object obj, final Class<T> castTo) {
        return (Collection<T>) obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> listOf(final Object obj, final Class<T> castTo) {
        return (List<T>) obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> Vector<T> vectorOf(final Object obj, final Class<T> castTo) {
        return (Vector<T>) obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> setOf(final Object obj, final Class<T> castTo) {
        return (Set<T>) obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> SortedSet<T> sortedSetOf(final Object obj, final Class<T> castTo) {
        return (SortedSet<T>) obj;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mapOf(final Object obj, final Class<K> keyCastTo, final Class<V> valueCastTo) {
        return (Map<K, V>) obj;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> SortedMap<K, V> sortedMapOf(final Object obj, final Class<K> keyCastTo, final Class<V> valueCastTo) {
        return (SortedMap<K, V>) obj;
    }

}
