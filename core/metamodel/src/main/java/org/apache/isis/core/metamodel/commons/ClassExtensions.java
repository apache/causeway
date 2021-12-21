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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import org.apache.isis.applib.exceptions.UnrecoverableException;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.base._Reduction;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.resources._Resources;

import lombok.val;

public final class ClassExtensions {


    private ClassExtensions() {
    }

    public static Object newInstance(final Class<?> type) {
        return ClassExtensions.newInstance(type, _Constants.emptyClasses, _Constants.emptyObjects);
    }

    public static Object newInstance(final Class<?> extendee, final Class<?> constructorParamType, final Object constructorArg) {
        return ClassExtensions.newInstance(extendee, new Class[] { constructorParamType }, new Object[] { constructorArg });
    }

    /**
     * Tries to instantiate using a constructor accepting the supplied
     * arguments; if no such constructor then falls back to trying the no-arg
     * constructor.
     */
    public static Object newInstance(final Class<?> extendee, final Class<?>[] constructorParamTypes, final Object[] constructorArgs) {
        try {
            Constructor<?> constructor;
            try {
                constructor = extendee.getConstructor(constructorParamTypes);
                return constructor.newInstance(constructorArgs);
            } catch (final NoSuchMethodException ex) {
                try {
                    constructor = extendee.getConstructor();
                    return constructor.newInstance();
                } catch (final NoSuchMethodException e) {
                    val msg = String.format("Failed to call contructor for type %s trying, "
                            + "args '%s' then trying no args.",
                            extendee.getName(),
                            _Lists.of(constructorParamTypes).toString());

                    throw _Exceptions.unrecoverable(msg, e);
                }
            }
        } catch (final SecurityException | IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            throw new UnrecoverableException(ex);
        }
    }

    public static String getSuperclass(final Class<?> extendee) {
        final Class<?> superType = extendee.getSuperclass();

        if (superType == null) {
            return null;
        }
        return superType.getName();
    }

    public static boolean isAbstract(final Class<?> extendee) {
        return Modifier.isAbstract(extendee.getModifiers());
    }

    public static boolean isJavaClass(final Class<?> extendee) {
        final String className = extendee.getName();
        return className.startsWith(ClassUtil.JAVA_CLASS_PREFIX) ||
                extendee.getName().startsWith("sun.");
    }

    static Class<?> implementingClassOrNull(final Class<?> extendee, final Class<?> requiredClass, final Class<?> constructorParamType) {
        if (extendee == null) {
            return null;
        }
        if (!requiredClass.isAssignableFrom(extendee)) {
            return null;
        }
        try {
            extendee.getConstructor(new Class[] { constructorParamType });
        } catch (final NoSuchMethodException ex) {
            try {
                extendee.getConstructor(new Class[] {});
            } catch (final NoSuchMethodException e) {
                return null;
            }
        } catch (final SecurityException e) {
            return null;
        }
        final int modifiers = extendee.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
            return null;
        }
        return extendee;
    }

    public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterClass) throws NoSuchMethodException {
        return clazz.getMethod(methodName, parameterClass);
    }

    public static Method getMethodElseNull(final Class<?> clazz, final String methodName, final Class<?>... parameterClass) {
        try {
            return clazz.getMethod(methodName, parameterClass);
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    public static boolean exists(final Class<?> cls, final String resourceName) {
        final URL url = _Resources.getResourceUrl(cls, resourceName);
        return url != null;
    }

    static Class<?> asWrapped(final Class<?> primitiveClassExtendee) {
        return ClassUtil.wrapperByPrimitive.get(primitiveClassExtendee);
    }

    public static Class<? extends Object> asWrappedIfNecessary(final Class<?> cls) {
        return cls.isPrimitive() ? asWrapped(cls) : cls;
    }

    public static Object toDefault(final Class<?> extendee) {
        if(!extendee.isPrimitive()) {
            return null;
        }
        return ClassUtil.defaultByPrimitive.get(extendee);
    }

    /**
     * Returns the corresponding 'null' value for the primitives, or just
     * <tt>null</tt> if the class represents a non-primitive type.
     */
    public static Object getNullOrDefault(final Class<?> type) {
        return ClassUtil.defaultByPrimitive.get(type);
    }

    public static boolean isCompatibleAsReturnType(final Class<?> returnTypeExtendee, final CanBeVoid canBeVoid, final Class<?> type) {
        boolean mayBeVoid = canBeVoid == CanBeVoid.TRUE;

        if (returnTypeExtendee == null) {
            return true;
        }
        if (mayBeVoid && (type == void.class)) {
            return true;
        }

        if (type.isPrimitive()) {
            return returnTypeExtendee.isAssignableFrom(ClassUtil.wrapperByPrimitive.get(type));
        }

        return (returnTypeExtendee.isAssignableFrom(type));
    }

    public static boolean equalsWhenBoxing(final Class<?> t1, final Class<?> t2) {
        if(Objects.equals(t1, t2)) {
            return true;
        }
        return Objects.equals(asWrappedIfNecessary(t1), asWrappedIfNecessary(t2));
    }

    public static final class CommonSuperclassFinder {

        private final _Reduction<Class<?>> reduction = _Reduction.of((common, next) -> {
            Class<?> refine = common;
            while(!refine.isAssignableFrom(next)) {
                refine = refine.getSuperclass();
            }
            return refine;
        });

        public void collect(final Object pojo) {
            reduction.accept(pojo.getClass());
        }

        public Optional<Class<?>> getCommonSuperclass() {
            return reduction.getResult();
        }
    }

}
