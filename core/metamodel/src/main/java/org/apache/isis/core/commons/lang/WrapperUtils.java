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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class WrapperUtils {

    private WrapperUtils() {
    }
    
    private static final Map<Class<?>, Object> defaultByPrimitiveClass = 
        MapUtils.asMap(
            boolean.class, false,
            byte.class, (byte)0,
            short.class, (short)0,
            int.class, 0,
            long.class, 0L,
            float.class, 0.0f,
            double.class, 0.0,
            char.class, (char)0
        );


    private static Map<Class<?>, Class<?>> wrapperClasses = 
        MapUtils.asMap(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class
        );

    public static Class<?> wrap(final Class<?> primitiveClass) {
        return wrapperClasses.get(primitiveClass);
    }

    public static Class<?>[] wrapAsNecessary(final Class<?>[] classes) {
        final List<Class<?>> wrappedClasses = new ArrayList<Class<?>>();
        for (final Class<?> cls : classes) {
            wrappedClasses.add(wrapAsNecessary(cls));
        }
        return wrappedClasses.toArray(new Class[] {});
    }

    public static Class<? extends Object> wrapAsNecessary(final Class<?> cls) {
        return cls.isPrimitive() ? wrap(cls) : cls;
    }

    public static Object defaultFor(final Class<?> cls) {
        if(!cls.isPrimitive()) {
            return null;
        }
        return WrapperUtils.defaultByPrimitiveClass.get(cls);
    }

}
