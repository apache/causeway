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

import java.io.IOException;
import java.lang.reflect.Method;
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
 * Helpers to co-erce non-generic values into type-safe generics without
 * having to suppress compiler warnings all over the place.
 */
public final class ObjectExtensions {

    private ObjectExtensions() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T asT(final Object extendee) {
        return (T) extendee;
    }

    @SuppressWarnings("unchecked")
    public static <T> Enumeration<T> asEnumerationT(final Object extendee, final Class<T> castTo) {
        return (Enumeration<T>) extendee;
    }

    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> asIteratorT(final Object extendee, final Class<T> castTo) {
        return (Iterator<T>) extendee;
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<T> asCollectionT(final Object extendee, final Class<T> castTo) {
        return (Collection<T>) extendee;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> asListT(final Object extendee, final Class<T> castTo) {
        return (List<T>) extendee;
    }

    @SuppressWarnings("unchecked")
    public static <T> Vector<T> asVectorT(final Object extendee, final Class<T> castTo) {
        return (Vector<T>) extendee;
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> asSetT(final Object extendee, final Class<T> castTo) {
        return (Set<T>) extendee;
    }

    @SuppressWarnings("unchecked")
    public static <T> SortedSet<T> asSortedSetT(final Object extendee, final Class<T> castTo) {
        return (SortedSet<T>) extendee;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asMapKV(final Object extendee, final Class<K> keyCastTo, final Class<V> valueCastTo) {
        return (Map<K, V>) extendee;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> SortedMap<K, V> asSortedMapKV(final Object extendee, final Class<K> keyCastTo, final Class<V> valueCastTo) {
        return (SortedMap<K, V>) extendee;
    }

    public static Object[] asArray(final Object extendee) {
        final Class<?> arrayType = extendee.getClass().getComponentType();
        if (!arrayType.isPrimitive()) {
            return (Object[]) extendee;
        }
        if (arrayType == char.class) {
            return ArrayExtensions.asCharToCharacterArray(extendee);
        } else {
            return ArrayExtensions.convertPrimitiveToObjectArray(extendee, arrayType);
        }
    }

    public static Method getMethod(final Object object, final String methodName, final Class<?>... parameterClass) throws NoSuchMethodException {
        return ClassExtensions.getMethod(object.getClass(), methodName, parameterClass);
    }

    public static Method getMethod(final Object object, final String methodName) throws NoSuchMethodException {
        return ClassExtensions.getMethod(object.getClass(), methodName, new Class[0]);
    }

    public static String classBaseName(final Object forObject) {
        final String name = forObject.getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public static void appendToString(final Object extendee, final Appendable buf) {
        try {
            buf.append(classBaseName(extendee));
            buf.append('@');
            buf.append(Integer.toHexString(extendee.hashCode()));
        } catch (IOException iox) {
            throw new RuntimeException("A problem occurred while appending an object to an appendable", iox);
        }
    }

}
