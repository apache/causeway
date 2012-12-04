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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JavaClassUtils {

    private static final String JAVA_CLASS_PREFIX = "java.";

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

    private JavaClassUtils() {
    }

    public static Class<?> getBuiltIn(final String name) {
        return builtInClasses.get(name);
    }

    public static String getSuperclass(final Class<?> type) {
        final Class<?> superType = type.getSuperclass();

        if (superType == null) {
            return null;
        }
        return superType.getName();
    }

    public static boolean isAbstract(final Class<?> type) {
        return Modifier.isAbstract(type.getModifiers());
    }

    public static boolean isFinal(final Class<?> type) {
        return Modifier.isFinal(type.getModifiers());
    }

    public static boolean isPublic(final Class<?> type) {
        return Modifier.isPublic(type.getModifiers());
    }

    public static boolean isJavaClass(final Class<?> type) {
        return type.getName().startsWith(JAVA_CLASS_PREFIX) || type.getName().startsWith("sun.");
    }

    public static boolean isPublic(final Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    public static List<Class<?>> toClasses(final List<Object> objectList) {
        final List<Class<?>> classList = new ArrayList<Class<?>>();
        for (final Object service : objectList) {
            classList.add(service.getClass());
        }
        return classList;
    }

    /**
     * Returns the supplied Class so long as it implements (or is a subclass of)
     * the required class, and also has either a constructor accepting the
     * specified param type, or has a no-arg constructor.
     */
    public static Class<?> implementingClassOrNull(final Class<?> classCandidate, final Class<?> requiredClass, final Class<?> constructorParamType) {
        if (classCandidate == null) {
            return null;
        }
        if (!requiredClass.isAssignableFrom(classCandidate)) {
            return null;
        }
        try {
            classCandidate.getConstructor(new Class[] { constructorParamType });
        } catch (final NoSuchMethodException ex) {
            try {
                classCandidate.getConstructor(new Class[] {});
            } catch (final NoSuchMethodException e) {
                return null;
            }
        } catch (final SecurityException e) {
            return null;
        }
        final int modifiers = classCandidate.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
            return null;
        }
        return classCandidate;
    }

    public static Class<?> implementingClassOrNull(final String classCandidateName, final Class<?> requiredClass, final Class<?> constructorParamType) {
        if (classCandidateName == null) {
            return null;
        }
        Class<?> classCandidate = null;
        try {
            classCandidate = Class.forName(classCandidateName);
            return implementingClassOrNull(classCandidate, requiredClass, constructorParamType);
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

    public static boolean isStatic(final Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

}
