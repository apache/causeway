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

package org.apache.isis.core.commons.factory;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.lang.ObjectExtensions;

public final class InstanceUtil {

    private InstanceUtil() {
    }

    public static Object createInstance(final String className) {
        return createInstance(className, (Class<?>) null, null);
    }

    public static Object createInstance(final Class<?> cls) {
        return createInstance(cls, (Class<?>) null, null);
    }

    public static <T> T createInstance(final String className, final Class<T> requiredClass) {
        return createInstance(className, (Class<T>) null, requiredClass);
    }

    public static <T> T createInstance(final Class<?> cls, final Class<T> requiredClass) {
        return createInstance(cls, (Class<T>) null, requiredClass);
    }

    public static <T> T createInstance(final String className, final String defaultTypeName, final Class<T> requiredType) {
        Class<? extends T> defaultType = null;
        if (defaultTypeName != null) {
            try {
                defaultType = ObjectExtensions.asT(Thread.currentThread().getContextClassLoader().loadClass(defaultTypeName));
                if (defaultType == null) {
                    throw new InstanceCreationClassException("Failed to load default type '" + defaultTypeName + "'");
                }
            } catch (final ClassNotFoundException e) {
                throw new UnavailableClassException("The default type '" + defaultTypeName + "' cannot be found");
            } catch (final NoClassDefFoundError e) {
                throw new InstanceCreationClassException("Default type '" + defaultTypeName + "' found, but is missing a dependent class: " + e.getMessage(), e);
            }
        }
        return createInstance(className, defaultType, requiredType);
    }

    public static <T> T createInstance(final Class<?> cls, final String defaultTypeName, final Class<T> requiredType) {
        Class<? extends T> defaultType = null;
        if (defaultTypeName != null) {
            defaultType = loadClass(defaultTypeName, requiredType);
            try {
                defaultType = ObjectExtensions.asT(Thread.currentThread().getContextClassLoader().loadClass(defaultTypeName));
                if (defaultType == null) {
                    throw new InstanceCreationClassException("Failed to load default type '" + defaultTypeName + "'");
                }
            } catch (final ClassNotFoundException e) {
                throw new UnavailableClassException("The default type '" + defaultTypeName + "' cannot be found");
            } catch (final NoClassDefFoundError e) {
                throw new InstanceCreationClassException("Default type '" + defaultTypeName + "' found, but is missing a dependent class: " + e.getMessage(), e);
            }
        }
        return createInstance(cls, defaultType, requiredType);
    }

    public static <T> T createInstance(final String className, final Class<? extends T> defaultType, final Class<T> requiredType) {
        Assert.assertNotNull("Class to instantiate must be specified", className);
        Class<?> cls = null;
        try {
            cls = Thread.currentThread().getContextClassLoader().loadClass(className);
            if (cls == null) {
                throw new InstanceCreationClassException("Failed to load class '" + className + "'");
            }
            return createInstance(cls, defaultType, requiredType);
        } catch (final ClassNotFoundException e) {
            if (className.indexOf('.') == -1) {
                throw new UnavailableClassException("The component '" + className + "' cannot be found");
            }
            throw new UnavailableClassException("The class '" + className + "' cannot be found");
        } catch (final NoClassDefFoundError e) {
            throw new InstanceCreationClassException("Class '" + className + "' found , but is missing a dependent class: " + e.getMessage(), e);
        }
    }

    public static <T> T createInstance(final Class<?> cls, final Class<? extends T> defaultType, final Class<T> requiredType) {
        Assert.assertNotNull("Class to instantiate must be specified", cls);
        try {
            if (requiredType == null || requiredType.isAssignableFrom(cls)) {
                final Class<T> tClass = ObjectExtensions.asT(cls);
                return tClass.newInstance();
            } else {
                throw new InstanceCreationClassException("Class '" + cls.getName() + "' is not of type '" + requiredType + "'");
            }
        } catch (final NoClassDefFoundError e) {
            throw new InstanceCreationClassException("Class '" + cls + "'found , but is missing a dependent class: " + e.getMessage(), e);
        } catch (final InstantiationException e) {
            throw new InstanceCreationException("Could not instantiate an object of class '" + cls.getName() + "'; " + e.getMessage());
        } catch (final IllegalAccessException e) {
            throw new InstanceCreationException("Could not access the class '" + cls.getName() + "'; " + e.getMessage());
        }
    }

    public static Class<?> loadClass(final String className) {
        Assert.assertNotNull("Class to instantiate must be specified", className);
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (final ClassNotFoundException e) {
            throw new UnavailableClassException("The default type '" + className + "' cannot be found");
        } catch (final NoClassDefFoundError e) {
            throw new InstanceCreationClassException("Default type '" + className + "' found, but is missing a dependent class: " + e.getMessage(), e);
        }
    }

    public static <R, T extends R> Class<T> loadClass(final String className, final Class<R> requiredType) {
        Assert.assertNotNull("Class to instantiate must be specified", className);
        try {
            final Class<?> loadedClass = loadClass(className);
            if (requiredType != null && !requiredType.isAssignableFrom(loadedClass)) {
                throw new InstanceCreationClassException("Class '" + className + "' is not of type '" + requiredType + "'");
            }
            return ObjectExtensions.asT(loadedClass);
        } catch (final NoClassDefFoundError e) {
            throw new InstanceCreationClassException("Default type '" + className + "' found, but is missing a dependent class: " + e.getMessage(), e);
        }
    }

}
