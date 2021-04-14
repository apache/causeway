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

package org.apache.isis.core.metamodel.commons;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.isis.applib.exceptions.UnrecoverableException;

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
            throw new UnrecoverableException(e);
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new UnrecoverableException(e);
        } catch (final IllegalArgumentException e) {
            throw new UnrecoverableException(e);
        } catch (final InstantiationException e) {
            throw new UnrecoverableException(e);
        } catch (final IllegalAccessException e) {
            throw new UnrecoverableException(e);
        } catch (final InvocationTargetException e) {
            throw new UnrecoverableException(e);
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

}
