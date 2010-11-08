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


	private static void put(Class<?> cls) {
		builtInClasses.put(cls.getName(), cls);
	}

	
    private JavaClassUtils() {}

    
	public static Class<?> getBuiltIn(String name) {
		return builtInClasses.get(name);
	}
    
    public static String[] getInterfaces(final Class<?> type) {
        final Class<?>[] interfaces = type.getInterfaces();
        final Class<?>[] interfacesCopy = new Class[interfaces.length];
        int validInterfaces = 0;
        for (int i = 0; i < interfaces.length; i++) {
            interfacesCopy[validInterfaces++] = interfaces[i];
        }

        final String[] interfaceNames = new String[validInterfaces];
        for (int i = 0; i < validInterfaces; i++) {
            interfaceNames[i] = interfacesCopy[i].getName();
        }

        return interfaceNames;
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

    public static boolean isStatic(final Method method) {
        return Modifier.isStatic(method.getModifiers());
    }
    
    public static boolean isPublic(final Method method) {
        return Modifier.isPublic(method.getModifiers());
    }
    
    public static List<Class<?>> toClasses(List<Object> objectList) {
	    List<Class<?>> classList = new ArrayList<Class<?>>();
	    for (Object service : objectList) {
	    classList.add(service.getClass());
	    }
	    return classList;
    }

    

}
