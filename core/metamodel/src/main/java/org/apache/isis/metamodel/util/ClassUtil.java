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


package org.apache.isis.metamodel.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.apache.isis.commons.exceptions.IsisException;


public final class ClassUtil {

    private ClassUtil() {}

    public static Object newInstance(final Class<?> type, final Class<?> constructorParamType, Object constructorArg) {
        return newInstance(type, new Class[]{constructorParamType}, new Object[]{constructorArg});
    }

    /**
     * Tries to instantiate using a constructor accepting the supplied arguments; if no such
     * constructor then falls back to trying the no-arg constructor.
     */
    public static Object newInstance(final Class<?> type, final Class<?>[] constructorParamTypes, Object[] constructorArgs) {
        try {
            Constructor<?> constructor;
            try {
                constructor = type.getConstructor(constructorParamTypes);
                return constructor.newInstance(constructorArgs);
            } catch (NoSuchMethodException ex) {
                try {
                    constructor = type.getConstructor();
                    return constructor.newInstance();
                } catch (NoSuchMethodException e) {
                    throw new IsisException(e);
                }
            }
        } catch (SecurityException ex) {
            throw new IsisException(ex);
        } catch (final IllegalArgumentException e) {
            throw new IsisException(e);
        } catch (final InstantiationException e) {
            throw new IsisException(e);
        } catch (final IllegalAccessException e) {
            throw new IsisException(e);
        } catch (final InvocationTargetException e) {
            throw new IsisException(e);
        }
    }

    /**
     * Returns the supplied Class so long as it implements (or is a subclass of) the
     * required class, and also has either a constructor accepting the specified param type,
     * or has a no-arg constructor.
     */
    public static Class<?> implementingClassOrNull(final Class<?> classCandidate, final Class<?> requiredClass, final Class<?> constructorParamType) {
        if (classCandidate == null) {
            return null;
        }
        if (!requiredClass.isAssignableFrom(classCandidate)) {
            return null;
        }
        try {
            classCandidate.getConstructor(new Class[] {constructorParamType});
        } catch (final NoSuchMethodException ex) {
            try {
                classCandidate.getConstructor(new Class[]{});
            } catch (NoSuchMethodException e) {
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
		for(Class<?> directlyImplementedInterface: cls.getInterfaces()) {
			if (directlyImplementedInterface == interfaceType) {
				return true;
			}
		}
		return false;
	}

}

