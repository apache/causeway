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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.isis.commons.internal.context._Context;

public final class ClassUtil {

    static final String JAVA_CLASS_PREFIX = "java.";

    private static Map<String, Class<?>> builtInClasses = new HashMap<String, Class<?>>();

    static {
        put(void.class);
        put(boolean.class);
        put(char.class);
        put(byte.class);
        put(short.class);
        put(int.class);
        put(long.class);
        put(float.class);
        put(double.class);
    }

    private static void put(final Class<?> cls) {
        builtInClasses.put(cls.getName(), cls);
    }

    static final Map<Class<?>, Object> defaultByPrimitiveClass =
            MapUtil.asMap(
                    boolean.class, false,
                    byte.class, (byte)0,
                    short.class, (short)0,
                    int.class, 0,
                    long.class, 0L,
                    float.class, 0.0f,
                    double.class, 0.0,
                    char.class, (char)0
                    );
    static Map<Class<?>, Class<?>> wrapperClasses =
            MapUtil.asMap(
                    // TODO: there is a better way of doing this in 1.6 using TypeMirror
                    boolean.class, Boolean.class,
                    byte.class, Byte.class,
                    char.class, Character.class,
                    short.class, Short.class,
                    int.class, Integer.class,
                    long.class, Long.class,
                    float.class, Float.class,
                    double.class, Double.class,
                    void.class, Void.class
                    );

    static Map<Class<?>, Object> defaultByPrimitiveType = new HashMap<Class<?>, Object>();

    static {
        defaultByPrimitiveType.put(byte.class, (byte) 0);
        defaultByPrimitiveType.put(short.class, (short) 0);
        defaultByPrimitiveType.put(int.class, 0);
        defaultByPrimitiveType.put(long.class, 0L);
        defaultByPrimitiveType.put(char.class, 0);
        defaultByPrimitiveType.put(float.class, 0.0F);
        defaultByPrimitiveType.put(double.class, 0.0);
        defaultByPrimitiveType.put(boolean.class, false);
    }

    public static Map<String, Class<?>> primitives = Maps.newHashMap();

    static {
        @SuppressWarnings({ "rawtypes" })
        final List<Class> primitiveClasses = Arrays.<Class> asList(
                boolean.class,
                byte.class,
                short.class,
                int.class,
                long.class,
                float.class,
                double.class,
                char.class);
        for (final Class<?> cls : primitiveClasses) {
            primitives.put(cls.getName(), cls);
        }
    }


    // //////////////////////////////////////



    private ClassUtil() {
    }

    public static Class<?> getBuiltIn(final String name) {
        return builtInClasses.get(name);
    }

    /**
     * Returns the supplied Class so long as it implements (or is a subclass of)
     * the required class, and also has either a constructor accepting the
     * specified param type, or has a no-arg constructor.
     */
    public static Class<?> implementingClassOrNull(final String classCandidateName, final Class<?> requiredClass, final Class<?> constructorParamType) {
        if (classCandidateName == null) {
            return null;
        }
        Class<?> classCandidate = null;
        try {
            classCandidate = Class.forName(classCandidateName);
            return ClassExtensions.implementingClassOrNull(classCandidate, requiredClass, constructorParamType);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    public static boolean directlyImplements(final Class<?> cls, final Class<?> interfaceType) {
        for (final Class<?> directlyImplementedInterface : cls.getInterfaces()) {
            if (directlyImplementedInterface == interfaceType) {
                return true;
            }
        }
        return false;
    }

    public static Class<?> forName(final String fullName) {
        final Class<?> primitiveCls = primitives.get(fullName);
        if (primitiveCls != null) {
            return primitiveCls;
        }
        try {
            return _Context.loadClass(fullName);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> forNameElseNull(final String fullName) {
        if (fullName == null) {
            return null;
        }
        try {
            return _Context.loadClass(fullName);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

}
