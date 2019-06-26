/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.isis.core.commons.reflection;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 *
 * Provides shortcuts for common java.lang.reflect idioms.
 *
 * @since 2.0
 *
 */
public class Reflect {

    public static Object[] emptyObjects = {};
    public static Class<?>[] emptyClasses = {};

    // -- CLASS REFLECTION

    /**
     * Returns declared methods of this class/interface and all super classes/interfaces.
     * @param type
     * @return
     */
    public static List<Method> getAllDeclaredMethods(Class<?> type) {
        final List<Method> methods = new ArrayList<>();

        Stream.of(type.getDeclaredMethods()).forEach(methods::add);
        visitInterfaces(type,c->Stream.of(c.getDeclaredMethods()).forEach(methods::add));
        visitSuperclassesOf(type,c->Stream.of(c.getDeclaredMethods()).forEach(methods::add));
        return methods;
    }

    /**
     * Returns declared fields of this class/interface and all super classes/interfaces.
     * @param type
     * @return
     */
    public static List<Field> getAllDeclaredFields(Class<?> type) {
        final List<Field> fields = new ArrayList<>();

        Stream.of(type.getDeclaredFields()).forEach(fields::add);
        visitInterfaces(type,c->Stream.of(c.getDeclaredFields()).forEach(fields::add));
        visitSuperclassesOf(type,c->Stream.of(c.getDeclaredFields()).forEach(fields::add));
        return fields;
    }

    public static void visitSuperclassesOf(final Class<?> clazz, final Consumer<Class<?>> visitor){
        final Class<?> superclass = clazz.getSuperclass();
        if(superclass!=null){
            visitor.accept(superclass);
            visitSuperclassesOf(superclass, visitor);
        }
    }

    public static void visitInterfaces(final Class<?> clazz, final Consumer<Class<?>> visitor){
        if(clazz.isInterface())
            visitor.accept(clazz);

        for(Class<?> interf : clazz.getInterfaces())
            visitor.accept(interf);
    }

    public static Method getGetter(Class<?> cls, String propertyName) throws IntrospectionException {
        final BeanInfo beanInfo = Introspector.getBeanInfo(cls);
        for(PropertyDescriptor pd:beanInfo.getPropertyDescriptors()){
            if(!pd.getName().equals(propertyName))
                continue;
            return pd.getReadMethod();
        }
        return null;
    }

    public static Method getGetter(Object bean, String propertyName) throws IntrospectionException {
        if(bean==null)
            return null;
        return getGetter(bean, propertyName);
    }

    // -- METHOD HANDLES

    public static MethodHandle handleOf(Method method) throws IllegalAccessException {
        if(!method.isAccessible()) {
            method.setAccessible(true);
            MethodHandle mh = MethodHandles.publicLookup().unreflect(method);
            method.setAccessible(false);
            return mh;
        }
        return MethodHandles.publicLookup().unreflect(method);
    }

    public static MethodHandle handleOf(Field field) throws IllegalAccessException {
        if(!field.isAccessible()) {
            field.setAccessible(true);
            MethodHandle mh = MethodHandles.lookup().unreflectGetter(field);
            field.setAccessible(false);
            return mh;
        }
        return MethodHandles.lookup().unreflectGetter(field);
    }

    // -- PRIMITIVE TYPES

    private static final Set<Class<?>> primitives = new HashSet<>(Arrays.asList(
            boolean.class,
            byte.class,
            char.class,
            double.class,
            float.class,
            int.class,
            long.class,
            short.class
            //void.class //separated out into its own predicate: isVoid(...)
            ));

    private static final Set<Class<?>> primitiveWrappers = new HashSet<>(Arrays.asList(
            Boolean.class,
            Byte.class,
            Character.class,
            Double.class,
            Float.class,
            Integer.class,
            Long.class,
            Short.class
            //Void.class //separated out into its own predicate: isVoid(...)
            ));

    // -- TYPE PREDICATES

    public static boolean isVoid(Class<?> c) {
        Objects.requireNonNull(c);
        return c == void.class || c == Void.class;
    }

    public static boolean isPrimitive(Class<?> c) {
        Objects.requireNonNull(c);
        return primitives.contains(c);
    }

    public static boolean isPrimitiveWrapper(Class<?> c) {
        Objects.requireNonNull(c);
        return primitiveWrappers.contains(c);
    }


    // -- METHOD PREDICATES

    public static boolean isNoArg(Method m) {
        Objects.requireNonNull(m);
        return m.getParameterTypes().length==0;
    }

    public static boolean isPublic(Method m) {
        Objects.requireNonNull(m);
        return Modifier.isPublic(m.getModifiers());
    }

    public static boolean isVoid(Method m) {
        Objects.requireNonNull(m);
        return isVoid(m.getReturnType());
    }






}
