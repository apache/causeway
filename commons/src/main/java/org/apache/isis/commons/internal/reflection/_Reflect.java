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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import org.springframework.core.annotation.AnnotationUtils;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.base._With;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.functions._Predicates;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Reflection utilities.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0
 */
@UtilityClass
public final class _Reflect {


    // -- PREDICATES

    public static boolean same(Method method, Method superMethod) {
        if(!method.getName().equals(superMethod.getName())) {
            return false;
        }
        if(method.getParameterCount()!=superMethod.getParameterCount()) {
            return false;
        }
        return _Arrays.testAllMatch(method.getParameters(), superMethod.getParameters(),
                (p1, p2)->p1.getType().equals(p2.getType()));
    }

    /**
     * Returns whether a {@link Member} is accessible.
     * @param m Member to check
     * @return {@code true} if <code>m</code> is accessible
     */
    public static boolean isAccessible(final Member m) {
        return m != null && Modifier.isPublic(m.getModifiers()) && !m.isSynthetic();
    }

    /**
     * Whether member name equals given {@code memberName}
     * @param memberName
     * @return
     */
    public static <T extends Member> Predicate<T> withName(final String memberName) {
        _With.requires(memberName, "memberName");
        return m -> m != null && memberName.equals(m.getName());
    }

    /**
     * Whether member name starts with given {@code prefix}
     * @param prefix
     * @return
     */
    public static <T extends Member> Predicate<T> withPrefix(final String prefix) {
        _With.requires(prefix, "prefix");
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
        _With.requires(type, "type");
        return (Field f) -> f != null && type.isAssignableFrom(f.getType());
    }

    // -- FIELDS

    /**
     * Stream fields of given {@code type}
     * @param type (nullable)
     * @param ignoreAccess - determines if underlying method has to be accessible
     * @return
     */
    public static Stream<Field> streamFields(
            @Nullable Class<?> type,
            final boolean ignoreAccess) {

        if(type==null) {
            return Stream.empty();
        }
        if(ignoreAccess) {
            return _NullSafe.stream(type.getDeclaredFields());
        }
        return _NullSafe.stream(type.getFields());
    }

    /**
     * Stream all fields of given {@code type}, up the super class hierarchy.
     * @param type (nullable)
     * @param ignoreAccess - determines if underlying method has to be accessible
     * @return
     */
    public static Stream<Field> streamAllFields(
            @Nullable Class<?> type,
            final boolean ignoreAccess) {

        return streamTypeHierarchy(type, /*includeInterfaces*/  InterfacePolicy.EXCLUDE) // interfaces don't have fields
                .filter(Object.class::equals) // do not process Object class.
                .flatMap(t->streamFields(t, ignoreAccess));
    }

    // -- METHODS
    /**
     * Stream methods of given {@code type}.
     * @param type (nullable)
     * @param ignoreAccess - determines if underlying method has to be accessible
     * @return non-null
     */
    public static Stream<Method> streamMethods(
            @Nullable Class<?> type,
            final boolean ignoreAccess) {

        if(type==null) {
            return Stream.empty();
        }
        if(ignoreAccess) {
            return _NullSafe.stream(type.getDeclaredMethods());
        }
        return _NullSafe.stream(type.getMethods());
    }

    /**
     * Stream all methods of given {@code type}, up the super class hierarchy.
     * @param type (nullable)
     * @param ignoreAccess - determines if underlying method has to be accessible
     * @return non-null
     */
    public static Stream<Method> streamAllMethods(
            @Nullable Class<?> type,
            final boolean ignoreAccess
            ) {

        return streamTypeHierarchy(type, /*includeInterfaces*/  InterfacePolicy.INCLUDE)
                .filter(t->!t.equals(Object.class)) // do not process Object class.
                .flatMap(t->streamMethods(t, ignoreAccess));
    }

    // -- SUPER CLASSES

    public enum InterfacePolicy {
        INCLUDE,
        EXCLUDE
    }

    /**
     * Stream all types of given {@code type}, up the super class hierarchy starting with self
     * @param type (nullable)
     * @param interfacePolicy - whether to include all interfaces implemented by given {@code type}.
     * @return non-null
     */
    public static Stream<Class<?>> streamTypeHierarchy(
            @Nullable Class<?> type,
            final InterfacePolicy interfacePolicy) {

        val includeInterfaces = interfacePolicy == InterfacePolicy.INCLUDE;

        // https://stackoverflow.com/questions/40240450/java8-streaming-a-class-hierarchy?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        // Java 9+ will allow ...
        // return Stream.iterate(type, Objects::nonNull, Class::getSuperclass);

        return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<Class<?>>(Long.MAX_VALUE,
                        Spliterator.ORDERED|Spliterator.IMMUTABLE|Spliterator.NONNULL) {
                    Class<?> current = type;
                    @Override
                    public boolean tryAdvance(Consumer<? super Class<?>> action) {
                        if(current == null) return false;
                        action.accept(current);
                        if(includeInterfaces) {
                            for(Class<?> subIface : current.getInterfaces()) {
                                recur(subIface, action);
                            }
                        }
                        current = current.getSuperclass();
                        return true;
                    }

                    private void recur(Class<?> iface, Consumer<? super Class<?>> action) {
                        action.accept(iface);
                        for(Class<?> subIface : iface.getInterfaces()) {
                            recur(subIface, action);
                        }
                    }

                }, false);
    }

    // -- ANNOTATIONS

    /**
     * Searches for annotation on provided class or any of its super-classes up the type hierarchy 
     * or any implemented interfaces.
     * @param cls
     * @param annotationClass
     * @return the first matching annotation, or {@code null} if not found
     * @throws NullPointerException - if annotationClass is {@code null}
     */
    public static <T extends Annotation> T getAnnotation(
            final Class<?> cls, 
            final Class<T> annotationClass) {

        if (cls == null) {
            return null;
        }
        final T annotation = cls.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }

        // search super-classes
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

    /**
     * <p>Gets the annotation object with the given annotation type that is present on the given method
     * or optionally on any equivalent method in super classes and interfaces. Returns null if the annotation
     * type was not present.</p>
     *
     * <p>Stops searching for an annotation once the first annotation of the specified type has been
     * found. Additional annotations of the specified type will be silently ignored.</p>
     * @param <A>
     *            the annotation type
     * @param method
     *            the {@link Method} to query
     * @param annotationCls
     *            the {@link Annotation} to check if is present on the method
     * @param searchSupers
     *            determines if a lookup in the entire inheritance hierarchy of the given class is performed
     *            if the annotation was not directly present
     * @param ignoreAccess
     *            determines if underlying method has to be accessible
     * @return the first matching annotation, or {@code null} if not found
     * @throws NullPointerException
     *            if the method or annotation are {@code null}
     */
    public static <A extends Annotation> A getAnnotation(
            final Method method, 
            final Class<A> annotationCls,
            final boolean searchSupers, 
            final boolean ignoreAccess) {

        _With.requires(method, "method");
        _With.requires(annotationCls, "annotationCls");
        if (!ignoreAccess && !isAccessible(method)) {
            return null;
        }
        
        if(searchSupers) {
            return AnnotationUtils.findAnnotation(method, annotationCls);    
        } else {
            return AnnotationUtils.getAnnotation(method, annotationCls);
        }
        
//        
//
//        final Stream<Method> methods;
//
//        if(searchSupers) {
//            methods = streamAllMethods(method.getDeclaringClass(), ignoreAccess);    
//        } else {
//            methods = streamMethods(method.getDeclaringClass(), ignoreAccess);
//        }
//
//        return methods
//                .filter(m->same(method, m))
//                .map(m->m.getAnnotation(annotationCls))
//                .filter(_NullSafe::isPresent)
//                .findFirst()
//                .orElse(null);

    }

    /**
     * Whether given {@code cls} is annotated with any {@link Annotation} of given {@code annotationName}. 
     * @param cls
     * @param annotationName - fully qualified class name of the {@link Annotation} to match against
     * @return false - if any of the arguments is null 
     */
    public static boolean containsAnnotation(@Nullable final Class<?> cls, @Nullable String annotationName) {
        if(cls==null || _Strings.isEmpty(annotationName)) {
            return false;
        }
        for(Annotation annot : cls.getAnnotations()) {
            if(annot.annotationType().getName().equals(annotationName)) {
                return true;
            }
        }
        return false;
    }

    // -- METHOD/FIELD HANDLES

    public static MethodHandle handleOf(Method method) throws IllegalAccessException {
        if(!method.isAccessible()) { // java9+ to replace by canAccess 
            /*sonar-ignore-on*/
            method.setAccessible(true);
            MethodHandle mh = MethodHandles.publicLookup().unreflect(method);
            method.setAccessible(false);
            /*sonar-ignore-off*/
            return mh;
        }
        return MethodHandles.publicLookup().unreflect(method);
    }

    public static MethodHandle handleOfGetterOn(Field field) throws IllegalAccessException {
        if(!field.isAccessible()) { // java9+ to replace by canAccess
            /*sonar-ignore-on*/
            field.setAccessible(true);
            MethodHandle mh = MethodHandles.lookup().unreflectGetter(field);
            field.setAccessible(false);
            /*sonar-ignore-off*/
            return mh;
        }
        return MethodHandles.lookup().unreflectGetter(field);
    }

    // -- FIND GETTER
    
    @SneakyThrows
    public static Stream<PropertyDescriptor> streamGetters(@NonNull Class<?> cls) {
        return Stream.of(
                Introspector.getBeanInfo(cls, Object.class)
                    .getPropertyDescriptors())
                .filter(pd->pd.getReadMethod()!=null);
    }
    
    public static Map<String, Method> getGettersByName(@NonNull Class<?> cls) {
        return streamGetters(cls)
                .collect(Collectors.toMap(PropertyDescriptor::getName, PropertyDescriptor::getReadMethod));
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
    
    // -- MODIFIERS
    
    public static Object getFieldOn(
            @NonNull final Field field,
            @NonNull final Object target) throws IllegalArgumentException, IllegalAccessException {
        
        /*sonar-ignore-on*/
        if(field.isAccessible()) {
            return field.get(target);
        }
        try {
            field.setAccessible(true);
            return field.get(target);
        } finally {
            field.setAccessible(false);
        }
        /*sonar-ignore-off*/
    }
    
    public static void setFieldOn(
            @NonNull final Field field,
            @NonNull final Object target,
            final Object fieldValue) throws IllegalArgumentException, IllegalAccessException {
        
        /*sonar-ignore-on*/
        if(field.isAccessible()) {
            field.set(target, fieldValue);
            return;
        }
        try {
            field.setAccessible(true);
            field.set(target, fieldValue);
        } finally {
            field.setAccessible(false);
        }
        /*sonar-ignore-off*/
    }
    
    
    public static Result<Object> invokeMethodOn(
            @NonNull final Method method, 
            @NonNull final Object target, 
            final Object... args) {
        
        /*sonar-ignore-on*/
        return Result.ofNullable(()->{
            if(method.isAccessible()) {
                return method.invoke(target, args);
            }
            try {
                method.setAccessible(true);
                return method.invoke(target, args);
            } finally {
                method.setAccessible(false);
            }
        });
        /*sonar-ignore-off*/
    }
    
    public static <T> Result<T> invokeConstructor(
            @NonNull final Constructor<T> constructor, 
            final Object... args) {
        
        /*sonar-ignore-on*/
        return Result.of(()->{
            if(constructor.isAccessible()) {
                return constructor.newInstance(args);
            }
            try {
                constructor.setAccessible(true);
                return constructor.newInstance(args);
            } finally {
                constructor.setAccessible(false);
            }
        });
        /*sonar-ignore-off*/
    }
    
    
    // -- COMMON CONSTRUCTOR IDIOMS
    
    public static Can<Constructor<?>> getDeclaredConstructors(Class<?> cls) {
        return Can.ofArray(cls.getDeclaredConstructors());
    }
    
    public static Can<Constructor<?>> getPublicConstructors(Class<?> cls) {
        return Can.ofArray(cls.getConstructors());
    }
    
    // -- FILTER
    
    @UtilityClass
    public static class Filter {
        
        public static Predicate<Executable> isPublic() {
            return ex->Modifier.isPublic(ex.getModifiers());
        }
        
        public static Predicate<Executable> paramCount(int paramCount) {
            return ex->ex.getParameterCount() == paramCount;
        }
        
        public static Predicate<Executable> paramAssignableFrom(int paramIndex, Class<?> paramType) {
            return ex->ex.getParameterTypes()[paramIndex].isAssignableFrom(paramType);
        }
        
        public static Predicate<Executable> paramSignatureMatch(Class<?>[] matchingParamTypes) {
            return ex->{
                // check params (if required)
                if (matchingParamTypes != null) {
                    final Class<?>[] parameterTypes = ex.getParameterTypes();
                    if (matchingParamTypes.length != parameterTypes.length) {
                        return false;
                    }

                    for (int c = 0; c < matchingParamTypes.length; c++) {
                        if ((matchingParamTypes[c] != null) && (matchingParamTypes[c] != parameterTypes[c])) {
                            return false;
                        }
                    }
                }
                return true;
            };
        }
        
        public static Predicate<Executable> paramAssignableFromValue(int paramIndex, @Nullable Object value) {
            if(value==null) {
                return _Predicates.alwaysTrue();
            }
            return ex->ex.getParameterTypes()[paramIndex].isAssignableFrom(value.getClass());
        }
        
    }
    

}
