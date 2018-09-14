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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.exceptions.IsisException;

public final class ArrayExtensions {

    private ArrayExtensions() {
    }

    static Object[] convertPrimitiveToObjectArray(final Object extendee, final Class<?> arrayType) {
        Object[] convertedArray;
        try {
            final Class<?> wrapperClass = ClassExtensions.asWrapped(arrayType);
            final Constructor<?> constructor = wrapperClass.getConstructor(new Class[] { String.class });
            final int len = Array.getLength(extendee);
            convertedArray = (Object[]) Array.newInstance(wrapperClass, len);
            for (int i = 0; i < len; i++) {
                convertedArray[i] = constructor.newInstance(new Object[] { Array.get(extendee, i).toString() });
            }
        } catch (final NoSuchMethodException e) {
            throw new IsisException(e);
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new IsisException(e);
        } catch (final IllegalArgumentException e) {
            throw new IsisException(e);
        } catch (final InstantiationException e) {
            throw new IsisException(e);
        } catch (final IllegalAccessException e) {
            throw new IsisException(e);
        } catch (final InvocationTargetException e) {
            throw new IsisException(e);
        }
        return convertedArray;
    }

    public static Object[] asCharToCharacterArray(final Object extendee) {
        final char[] original = (char[]) extendee;
        final int len = original.length;
        final Character[] converted = new Character[len];
        for (int i = 0; i < converted.length; i++) {
            converted[i] = Character.valueOf(original[i]);
        }
        return converted;
    }

    @SafeVarargs
    public static <T> T[] combine(final T[]... arrays) {
        final List<T> combinedList = _Lists.newArrayList();
        for (final T[] array : arrays) {
            Collections.addAll(combinedList, array);
        }
        return combinedList.toArray(arrays[0]); // using 1st element of arrays to specify the type
    }

    public static <T> T[] appendT(final T[] array, final T obj) {
        final List<T> combinedList = _Lists.newArrayList();
        combinedList.add(obj);
        Collections.addAll(combinedList, array);
        return combinedList.toArray(array); // using array to specify the type
    }
    public static String[] append(final String[] extendee, final String... moreArgs) {
        final List<String> argList = _Lists.newArrayList();
        argList.addAll(Arrays.asList(extendee));
        argList.addAll(Arrays.asList(moreArgs));
        return argList.toArray(new String[] {});
    }

    @SafeVarargs
    public static <T> T coalesce(final T... objects) {
        for (final T object : objects) {
            if (object != null) {
                return object;
            }
        }
        return null;
    }

}
