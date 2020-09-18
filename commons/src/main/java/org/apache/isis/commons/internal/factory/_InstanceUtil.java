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

package org.apache.isis.commons.internal.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.context._Context;

public final class _InstanceUtil {

    private _InstanceUtil() {
    }

    public static Object createInstance(final String className, Object... args) {
        return createInstance(className, (Class<?>) null, null, args);
    }

    public static Object createInstance(final Class<?> cls, Object... args) {
        return createInstance(cls, (Class<?>) null, null, args);
    }

    public static <T> T createInstance(final String className, final Class<T> requiredClass, Object... args) {
        return createInstance(className, (Class<T>) null, requiredClass, args);
    }

    public static <T> T createInstance(final Class<?> cls, final Class<T> requiredClass, Object... args) {
        return createInstance(cls, (Class<T>) null, requiredClass, args);
    }

    public static <T> T createInstance(
            final String className,
            final String defaultTypeName,
            final Class<T> requiredType,
            Object... args) {
        Class<? extends T> defaultType = null;
        if (defaultTypeName != null) {
            try {
                defaultType = _Casts.uncheckedCast(_Context.loadClass(defaultTypeName));
                if (defaultType == null) {
                    throw new _InstanceCreationClassException(String.format("Failed to load default type '%s'", defaultTypeName));
                }
            } catch (final ClassNotFoundException e) {
                throw new _UnavailableClassException(String.format("The default type '%s' cannot be found", defaultTypeName));
            } catch (final NoClassDefFoundError e) {
                throw new _InstanceCreationClassException(String.format("Default type '%s' found, but is missing a dependent class: %s", defaultTypeName, e.getMessage()), e);
            }
        }
        return createInstance(className, defaultType, requiredType, args);
    }

    public static <T> T createInstance(
            final Class<?> cls,
            final String defaultTypeName,
            final Class<T> requiredType,
            Object... args) {
        Class<? extends T> defaultType = null;
        if (defaultTypeName != null) {
            defaultType = loadClass(defaultTypeName, requiredType);
            try {
                defaultType = _Casts.uncheckedCast(_Context.loadClass(defaultTypeName));
                if (defaultType == null) {
                    throw new _InstanceCreationClassException(String.format("Failed to load default type '%s'", defaultTypeName));
                }
            } catch (final ClassNotFoundException e) {
                throw new _UnavailableClassException(String.format("The default type '%s' cannot be found", defaultTypeName));
            } catch (final NoClassDefFoundError e) {
                throw new _InstanceCreationClassException(String.format("Default type '%s' found, but is missing a dependent class: %s", defaultTypeName, e.getMessage()), e);
            }
        }
        return createInstance(cls, defaultType, requiredType, args);
    }

    public static <T> T createInstance(
            final String className,
            final Class<? extends T> defaultType,
            final Class<T> requiredType,
            Object... args) {
        _Assert.assertNotNull("Class to instantiate must be specified", className);
        try {
            final Class<?> cls = _Context.loadClass(className);
            if (cls == null) {
                throw new _InstanceCreationClassException(String.format("Failed to load class '%s'", className));
            }
            return createInstance(cls, defaultType, requiredType, args);
        } catch (final ClassNotFoundException e) {
            if (className.indexOf('.') == -1) {
                throw new _UnavailableClassException(String.format("The component '%s' cannot be found", className));
            }
            throw new _UnavailableClassException(String.format("The class '%s' cannot be found", className));
        } catch (final NoClassDefFoundError e) {
            throw new _InstanceCreationClassException(String.format("Class '%s' found , but is missing a dependent class: %s", className, e.getMessage()), e);
        }
    }

    public static <T> T createInstance(
            final Class<?> cls,
            final Class<? extends T> defaultType,
            final Class<T> requiredType,
            Object... args) {
        _Assert.assertNotNull(cls, "Class to instantiate must be specified");
        try {
            if (requiredType == null || requiredType.isAssignableFrom(cls)) {
                final Class<T> tClass = _Casts.uncheckedCast(cls);

                if(args == null || args.length == 0) {
                    return tClass.newInstance();
                } else {
                    Class<?>[] paramTypes = new Class[args.length];
                    for (int i = 0; i < args.length; i++) {
                        final Object arg = args[i];
                        paramTypes[i] = arg.getClass();
                    }
                    final Constructor<T> constructor = tClass.getConstructor(paramTypes);
                    return constructor.newInstance(args);
                }
            } else {
                throw new _InstanceCreationClassException(String.format("Class '%s' is not of type '%s'", cls.getName(), requiredType));
            }
        } catch (final NoClassDefFoundError e) {
            throw new _InstanceCreationClassException(String.format("Class '%s'found , but is missing a dependent class: %s", cls, e.getMessage()), e);
        } catch (final InstantiationException | InvocationTargetException e) {
            throw new _InstanceCreationException(String.format("Could not instantiate an object of class '%s'; %s", cls.getName(), e.getMessage()), e);
        } catch (final IllegalAccessException e) {
            throw new _InstanceCreationException(String.format("Could not access the class '%s'; %s", cls.getName(), e.getMessage()), e);
        } catch (NoSuchMethodException e) {
            throw new _InstanceCreationException(String.format("Could not find constructor in the class '%s'; %s", cls.getName(), e.getMessage()), e);
        }
    }

    public static Class<?> loadClass(final String className) {
        _Assert.assertNotNull("Class to instantiate must be specified", className);
        try {
            return _Context.loadClass(className);
        } catch (final ClassNotFoundException e) {
            throw new _UnavailableClassException(String.format("The type '%s' cannot be found", className));
        } catch (final NoClassDefFoundError e) {
            throw new _InstanceCreationClassException(String.format("Type '%s' found, but is missing a dependent class: %s", className, e.getMessage()), e);
        }
    }

    public static <R, T extends R> Class<T> loadClass(final String className, final Class<R> requiredType) {
        _Assert.assertNotNull("Class to instantiate must be specified", className);
        try {
            final Class<?> loadedClass = loadClass(className);
            if (requiredType != null && !requiredType.isAssignableFrom(loadedClass)) {
                throw new _InstanceCreationClassException("Class '" + className + "' is not of type '" + requiredType + "'");
            }
            return _Casts.uncheckedCast(loadedClass);
        } catch (final NoClassDefFoundError e) {
            throw new _InstanceCreationClassException(String.format("Default type '%s' found, but is missing a dependent class: %s", className, e.getMessage()), e);
        }
    }

}
