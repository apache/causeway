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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.commons.internal._Constants;

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
    public static <T> List<T> asListT(final Object extendee, final Class<T> castTo) {
        return (List<T>) extendee;
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
        return ClassExtensions.getMethod(object.getClass(), methodName, _Constants.emptyClasses);
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
