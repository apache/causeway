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

import java.util.Map;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import static org.apache.isis.commons.internal.base._With.requiresNotEmpty;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ClassUtil {

    final String JAVA_CLASS_PREFIX = "java.";

    private final Map<String, Class<?>> primitiveByName = Map.of(
            void.class.getName(), void.class,
            boolean.class.getName(), boolean.class,
            char.class.getName(), char.class,
            byte.class.getName(), byte.class,
            short.class.getName(), short.class,
            int.class.getName(), int.class,
            long.class.getName(), long.class,
            float.class.getName(), float.class,
            double.class.getName(), double.class);


    final Map<Class<?>, Object> defaultByPrimitive = Map.of(
            boolean.class, false,
            byte.class, (byte)0,
            short.class, (short)0,
            int.class, 0,
            long.class, 0L,
            float.class, 0.0f,
            double.class, 0.0,
            char.class, (char)0);

    //XXX supposedly use Spring's ClassUtils instead
    final Map<Class<?>, Class<?>> wrapperByPrimitive = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            void.class, Void.class);

    final Map<Class<?>, Class<?>> primitiveByNameWrapper = Map.of(
            Boolean.class, boolean.class,
            Byte.class, byte.class,
            Character.class, char.class,
            Short.class, short.class,
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class,
            Void.class, void.class);

    // //////////////////////////////////////

    /**
     * Unbox the given class if it is a primitive wrapper class,
     * returning the corresponding primitive type instead.
     * @param clazz the class to check
     * @return the original class, or a primitive for the original primitive wrapper type
     */
    public Class<?> unboxPrimitiveIfNecessary(final @NonNull Class<?> clazz) {
        // an optimization, to skip the lookup if possible
        val firstPass = clazz.isPrimitive()
                || !clazz.getPackageName().equals("java.lang")
                ? clazz
                : primitiveByNameWrapper.get(clazz);
        return firstPass!=null
                ? firstPass
                : clazz;
    }

    public Class<?> getBuiltIn(final String name) {
        return primitiveByName.get(name);
    }

    /**
     * Returns the supplied Class so long as it implements (or is a subclass of)
     * the required class, and also has either a constructor accepting the
     * specified param type, or has a no-arg constructor.
     */
    public Class<?> implementingClassOrNull(final String classCandidateName, final Class<?> requiredClass, final Class<?> constructorParamType) {
        if (classCandidateName == null) {
            return null;
        }
        Class<?> classCandidate = null;
        try {
            classCandidate = _Context.loadClass(classCandidateName);
            return ClassExtensions.implementingClassOrNull(classCandidate, requiredClass, constructorParamType);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    public boolean directlyImplements(final Class<?> cls, final Class<?> interfaceType) {
        for (final Class<?> directlyImplementedInterface : cls.getInterfaces()) {
            if (directlyImplementedInterface == interfaceType) {
                return true;
            }
        }
        return false;
    }

    public Class<?> forNameElseFail(final String fullName) {
        requiresNotEmpty(fullName, "fullName");
        final Class<?> builtIn = ClassUtil.getBuiltIn(fullName);
        if (builtIn != null) {
            return builtIn;
        }
        try {
            return _Context.loadClass(fullName);
        } catch (final ClassNotFoundException e) {
            throw _Exceptions.unrecoverable(e);
        }
    }

    public Class<?> forNameElseNull(final String fullName) {
        if (_Strings.isNullOrEmpty(fullName)) {
            return null;
        }
        val primitiveClass = primitiveByName.get(fullName);
        if(primitiveClass!=null) {
            return primitiveClass;
        }
        try {
            return _Context.loadClass(fullName);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Returns {@code cls.getCanonicalName()} if present.
     * Otherwise resorts to fully qualified class-name, with any '$' replaced by '.$'.
     * @param cls
     * @return non-null
     */
    public String getCanonicalName_friendlyToInnerClasses(final @NonNull Class<?> cls) {
        val name = cls.getCanonicalName();
        if(name==null) {
            return cls.getName().replace("$", ".$").replace("..", ".");
        }
        return name;
    }

}
