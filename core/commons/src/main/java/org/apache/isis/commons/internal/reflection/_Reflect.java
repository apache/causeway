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

package org.apache.isis.commons.internal.reflection;

import static org.apache.isis.commons.internal.base._NullSafe.stream;
import static org.apache.isis.commons.internal.base._With.mapIfPresentElse;
import static org.apache.isis.commons.internal.base._With.requires;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Reflection utilities.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0.0
 */
public final class _Reflect {

    private _Reflect() {}

    // -- PREDICATES

    /**
     * Whether member name equals given {@code memberName}
     * @param memberName
     * @return
     */
    public static <T extends Member> Predicate<T> withName(final String memberName) {
        requires(memberName, "memberName");
        return m -> m != null && memberName.equals(m.getName());
    }

    /**
     * Whether member name starts with given {@code prefix}
     * @param prefix
     * @return
     */
    public static <T extends Member> Predicate<T> withPrefix(final String prefix) {
        requires(prefix, "prefix");
        return m -> m != null && m.getName().startsWith(prefix);
    }

    /**
     * Whether method parameters count equal to given {@code count}
     * @param count
     * @return
     */
    public static Predicate<Method> withMethodParametersCount(final int count) {
        return (Method m) -> m != null && m.getParameterTypes().length == count;
    }

    /**
     * Whether field type is assignable to given {@code type}
     * @param type
     * @return
     */
    public static <T> Predicate<Field> withTypeAssignableTo(final Class<T> type) {
        requires(type, "type");
        return (Field f) -> f != null && type.isAssignableFrom(f.getType());
    }

    // -- FIELDS

    /**
     * Stream fields of given {@code type}
     * @param type (nullable)
     * @return
     */
    public static Stream<Field> streamFields(@Nullable Class<?> type) {
        return stream( mapIfPresentElse(type, Class::getDeclaredFields, (Field[])null) );
    }

    /**
     * Stream all fields of given {@code type}, up the super class hierarchy.
     * @param type (nullable)
     * @return
     */
    public static Stream<Field> streamAllFields(@Nullable Class<?> type) {
        return streamTypeHierarchy(type)
                .filter(Object.class::equals) // do not process Object class.
                .flatMap(_Reflect::streamFields);
    }

    // -- METHODS

    /**
     * Stream methods of given {@code type}
     * @param type (nullable)
     * @return
     */
    public static Stream<Method> streamMethods(@Nullable Class<?> type) {
        return stream( mapIfPresentElse(type,
                type.isInterface() ? Class::getMethods : Class::getDeclaredMethods,	(Method[])null) );
    }

    /**
     * Stream all methods of given {@code type}, up the super class hierarchy.
     * @param type (nullable)
     * @return
     */
    public static Stream<Method> streamAllMethods(@Nullable Class<?> type) {
        return streamTypeHierarchy(type)
                .filter(t->!t.equals(Object.class)) // do not process Object class.
                .flatMap(_Reflect::streamMethods);
    }

    // -- SUPER CLASSES

    /**
     * Stream all types of given {@code type}, up the super class hierarchy starting with self
     * @param type (nullable)
     * @return
     */
    public static Stream<Class<?>> streamTypeHierarchy(@Nullable Class<?> type) {

        // https://stackoverflow.com/questions/40240450/java8-streaming-a-class-hierarchy?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        // Java 9+ will allow ...
        // return Stream.iterate(type, Objects::nonNull, Class::getSuperclass);

        return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<Class<?>>(100L,
                        Spliterator.ORDERED|Spliterator.IMMUTABLE|Spliterator.NONNULL) {
                    Class<?> current = type;
                    @Override
                    public boolean tryAdvance(Consumer<? super Class<?>> action) {
                        if(current == null) return false;
                        action.accept(current);
                        current = current.getSuperclass();
                        return true;
                    }
                }, false);
    }

    /**
     * Searches for annotation on provided class, and if not found for the
     * superclass.
     */
    public static <T extends Annotation> T getAnnotation(final Class<?> cls, final Class<T> annotationClass) {
        if (cls == null) {
            return null;
        }
        final T annotation = cls.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }

        // search superclasses
        final Class<?> superclass = cls.getSuperclass();
        if (superclass != null) {
            try {
                final T annotationFromSuperclass = getAnnotation(superclass, annotationClass);
                if (annotationFromSuperclass != null) {
                    return annotationFromSuperclass;
                }
            } catch (final SecurityException e) {
                // fall through
            }
        }

        // search implemented interfaces
        final Class<?>[] interfaces = cls.getInterfaces();
        for (final Class<?> iface : interfaces) {
            final T annotationFromInterface = getAnnotation(iface, annotationClass);
            if (annotationFromInterface != null) {
                return annotationFromInterface;
            }
        }
        return null;
    }


}
