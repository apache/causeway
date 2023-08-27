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
package org.apache.causeway.commons.internal.reflection;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.functions._Predicates;

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

    /**
     * Other than checking for Method equality, this function is weaker and only
     * checks for method name, method signature equality
     * and whether a overrides b or vice versa.
     * <p>
     * Example of co-variant overriding:
     * <pre>
     * class Parent {
     *     Object getSomething(){ return 10; }
     * }
     *
     * class Child extends Parent {
     *     &#64;Override Integer getSomething() { return 10; }
     * }
     * </pre>
     * @param a
     * @param b
     * @see Method#equals(Object)
     */
    public boolean methodsSame(final Method a, final Method b) {
        if(!a.getName().equals(b.getName())) {
            return false;
        }
        if(a.getParameterCount()!=b.getParameterCount()) {
            return false;
        }
        if(!_Arrays.testAllMatch(a.getParameters(), b.getParameters(),
                (p1, p2)->p1.getType().equals(p2.getType()))) {
            return false;
        }
        return shareSameTypeHierarchy(a.getReturnType(), b.getReturnType());
    }

    /**
     * Weak variant of {@link #methodsSame(Method, Method)},
     * that relaxes parameter checks,
     * if generic parameter bounds are detected on any of the provided methods
     */
    public boolean methodsWeaklySame(final Method a, final Method b) {
        if(!a.getName().equals(b.getName())) {
            return false;
        }
        if(a.getParameterCount()!=b.getParameterCount()) {
            return false;
        }
        if(hasGenericParam(a)
                || hasGenericParam(b)) {
            if(!_Arrays.testAllMatch(a.getParameters(), b.getParameters(),
                    (p1, p2)->shareSameTypeHierarchy(p1.getType(), p2.getType()))) {
                return false;
            }
        } else {
            if(!_Arrays.testAllMatch(a.getParameters(), b.getParameters(),
                    (p1, p2)->p1.getType().equals(p2.getType()))) {
                return false;
            }
        }
        return shareSameTypeHierarchy(a.getReturnType(), b.getReturnType());
    }

    public boolean shareSameTypeHierarchy(final @NonNull Class<?> a, final @NonNull Class<?> b) {
        return a.isAssignableFrom(b)
                || b.isAssignableFrom(a);
    }

    public boolean shareSameTypeHierarchy(final @NonNull Method a, final @NonNull Method b) {
        return shareSameTypeHierarchy(a.getDeclaringClass(), b.getDeclaringClass());
    }

    /**
     * If a and b are related, such that one overrides the other,
     * that one which is overriding the other is returned.
     * @implNote if both declaring type and return type are the same we (arbitrarily) return b
     */
    public Method methodsWhichIsOverridingTheOther(final Method a, final Method b) {
        val aType = a.getDeclaringClass();
        val bType = b.getDeclaringClass();
        if(aType.equals(bType)) {
            val aReturn = a.getReturnType();
            val bReturn = b.getReturnType();
            if(aReturn.equals(bReturn)) {
                // if a and b are not equal, this code path is expected unreachable
                return b;
            }
            return aReturn.isAssignableFrom(bReturn)
                    ? b
                    : a;
        }
        return aType.isAssignableFrom(bType)
                ? b
                : a;
    }

    // -- COMPARATORS

    /**
     * In compliance with the sameness relation {@link #methodsSame(Method, Method)}
     * provides a comparator (with an arbitrarily chosen ordering relation).
     * @apiNote don't depend on the chosen ordering
     * @param a
     * @param b
     * @see #methodsSame(Method, Method)
     */
    public int methodWeakCompare(final Method a, final Method b) {

        int c = a.getName().compareTo(b.getName());
        if(c!=0) {
            return c;
        }
        c = Integer.compare(a.getParameterCount(), b.getParameterCount());
        if(c!=0) {
            return c;
        }
        val paramsA = a.getParameters();
        val paramsB = b.getParameters();
        for(int i=0; i<a.getParameterCount(); ++i) {
            c = typesCompare(paramsA[i].getType(), paramsB[i].getType());
            if(c!=0) {
                return c;
            }
        }
        c = typesCompare(a.getReturnType(), b.getReturnType());
        if(c!=0) {
            return shareSameTypeHierarchy(a.getReturnType(), b.getReturnType())
                    ? 0 // same
                    : c;
        }
        return 0; // equal
    }

    public int typesCompare(final Class<?> a, final Class<?> b) {
        return a.getName().compareTo(b.getName());
    }

    /**
     * Whether the caller can access this reflected object.
     * (method, field or constructor)
     * @param obj
     */
    public boolean canAccess(
            final @Nullable AccessibleObject member,
            final @Nullable Object obj) {
        return member != null
                && member.canAccess(obj);
    }

    /**
     * Returns whether a {@link Member} is public and not synthetic (can be accessed).
     * @param member Member to check
     */
    public boolean isPublicNonSynthetic(final @Nullable Member member) {
        return member != null
                && Modifier.isPublic(member.getModifiers())
                && !member.isSynthetic();
    }

    /**
     * Whether member name equals given {@code memberName}
     * @param memberName
     */
    public <T extends Member> Predicate<T> withName(final @NonNull String memberName) {
        return m -> m != null && memberName.equals(m.getName());
    }

    /**
     * Whether member name starts with given {@code prefix}
     * @param prefix
     */
    public <T extends Member> Predicate<T> withPrefix(final @NonNull String prefix) {
        return m -> m != null && m.getName().startsWith(prefix);
    }

    /**
     * Whether method parameters count equal to given {@code count}
     * @param count
     */
    public Predicate<Method> withMethodParametersCount(final int count) {
        return (final Method m) -> m != null && m.getParameterTypes().length == count;
    }

    /**
     * Whether field type is assignable to given {@code type}
     * @param type
     */
    public <T> Predicate<Field> withTypeAssignableTo(final @NonNull Class<T> type) {
        return (final Field f) -> f != null && type.isAssignableFrom(f.getType());
    }

    // -- FIELDS

    /**
     * Stream fields of given {@code type}
     * @param type (nullable)
     * @param ignoreAccess - whether to include non-public members
     */
    public Stream<Field> streamFields(
            final @Nullable Class<?> type,
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
     * @param ignoreAccess - whether to include non-public members
     */
    public Stream<Field> streamAllFields(
            final @Nullable Class<?> type,
            final boolean ignoreAccess) {

        return streamTypeHierarchy(type, InterfacePolicy.EXCLUDE) // interfaces don't have fields
                .filter(t->!Object.class.equals(t)) // do not process Object class.
                .flatMap(t->streamFields(t, ignoreAccess));
    }

    // -- METHODS
    /**
     * Stream methods of given {@code type}.
     * @param type (nullable)
     * @param ignoreAccess - whether to include non-public members
     * @return non-null
     */
    public Stream<Method> streamMethods(
            final @Nullable Class<?> type,
            final boolean ignoreAccess) {

        if(type==null) {
            return Stream.empty();
        }
        if(ignoreAccess) {
            //FIXME does not include public inherited - thats either intended or not by the caller!
            return _NullSafe.stream(type.getDeclaredMethods());
        }
        return _NullSafe.stream(type.getMethods());
    }

    public Stream<Method> streamInheritedMethods(final Method method) {
        return streamAllMethods(method.getDeclaringClass(), true)
                .filter(candidateMethod->methodsSame(candidateMethod, method));
    }

    /**
     * Stream all methods of given {@code type}, up the super class hierarchy.
     * @param type (nullable)
     * @param ignoreAccess - whether to include non-public members
     * @return non-null
     */
    public Stream<Method> streamAllMethods(
            final @Nullable Class<?> type,
            final boolean ignoreAccess) {

        return streamTypeHierarchy(type, InterfacePolicy.INCLUDE)
                .filter(t->!t.equals(Object.class)) // do not process Object class.
                .flatMap(t->streamMethods(t, ignoreAccess))
                .filter(m->!m.isBridge()); // don't include bridge methods
    }

    // -- SUPER CLASSES

    public enum TypeHierarchyPolicy {
        EXCLUDE,
        INCLUDE;
        public boolean isIncludeTypeHierarchy() {
            return this == TypeHierarchyPolicy.INCLUDE;
        }
    }

    public enum InterfacePolicy {
        EXCLUDE,
        INCLUDE;
        public boolean isIncludeInterfaces() {
            return this == InterfacePolicy.INCLUDE;
        }
    }

    /**
     * Stream all types of given {@code type}, up the super class hierarchy starting with self
     * @param type (nullable)
     * @param interfacePolicy - whether to include all interfaces implemented by given {@code type} at the end
     * @return non-null
     */
    public Stream<Class<?>> streamTypeHierarchy(
            final @Nullable Class<?> type,
            final @NonNull InterfacePolicy interfacePolicy) {

        return interfacePolicy.isIncludeInterfaces()
                ? Stream.concat(
                        Stream.<Class<?>>iterate(type, Objects::nonNull, Class::getSuperclass),
                        ClassUtils.getAllInterfacesForClassAsSet(type).stream())
                : Stream.iterate(type, Objects::nonNull, Class::getSuperclass);

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
    public <T extends Annotation> T getAnnotation(
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
    public <A extends Annotation> A getAnnotation(
            final @NonNull Method method,
            final @NonNull Class<A> annotationCls,
            final boolean searchSupers,
            final boolean ignoreAccess) {

        if (!ignoreAccess && !isPublicNonSynthetic(method)) {
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
    public boolean containsAnnotation(final @Nullable Class<?> cls, final @Nullable String annotationName) {
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

    // -- FIND GETTER

    @SneakyThrows
    public Stream<PropertyDescriptor> streamGetters(final @NonNull Class<?> cls) {
        return Stream.of(
                Introspector.getBeanInfo(cls, Object.class)
                    .getPropertyDescriptors())
                .filter(pd->pd.getReadMethod()!=null);
    }

    public Map<String, Method> getGettersByName(final @NonNull Class<?> cls) {
        return streamGetters(cls)
                .collect(Collectors.toMap(PropertyDescriptor::getName, PropertyDescriptor::getReadMethod));
    }


    public Method getGetter(final Class<?> cls, final String propertyName) throws IntrospectionException {
        final BeanInfo beanInfo = Introspector.getBeanInfo(cls);
        for(PropertyDescriptor pd:beanInfo.getPropertyDescriptors()){
            if(!pd.getName().equals(propertyName))
                continue;
            return pd.getReadMethod();
        }
        return null;
    }

    @SneakyThrows
    public Object readFromGetterOn(
            final @NonNull Method getter,
            final @NonNull Object target) {
        return getter.invoke(target);
    }

    // -- FIND SETTER

    public Method getSetter(final Class<?> cls, final String propertyName) throws IntrospectionException {
        final BeanInfo beanInfo = Introspector.getBeanInfo(cls);
        for(PropertyDescriptor pd:beanInfo.getPropertyDescriptors()){
            if(!pd.getName().equals(propertyName))
                continue;
            return pd.getWriteMethod();
        }
        return null;
    }

    @SneakyThrows
    public void writeToSetterOn(
            final @NonNull Method setter,
            final @NonNull Object target,
            final @NonNull Object value) {
        setter.invoke(target, value);
    }


    // -- MODIFIERS

    public Object getFieldOn(
            final @NonNull Field field,
            final @NonNull Object target) throws IllegalArgumentException, IllegalAccessException {

        /*sonar-ignore-on*/
        if(canAccess(field, target)) {
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

    public void setFieldOn(
            final @NonNull Field field,
            final @NonNull Object target,
            final Object fieldValue) throws IllegalArgumentException, IllegalAccessException {

        /*sonar-ignore-on*/
        if(canAccess(field, target)) {
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


    public Try<Object> invokeMethodOn(
            final @NonNull Method method,
            final @NonNull Object target,
            final Object... args) {

        /*sonar-ignore-on*/
        return Try.call(()->{
            if(canAccess(method, target)) {
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

    public <T> Try<T> invokeConstructor(
            final @NonNull Constructor<T> constructor,
            final Object... args) {

        /*sonar-ignore-on*/
        return Try.call(()->{
            if(canAccess(constructor, null)) {
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

    public Can<Constructor<?>> getDeclaredConstructors(final Class<?> cls) {
        return Can.ofArray(cls.getDeclaredConstructors());
    }

    public Can<Constructor<?>> getPublicConstructors(final Class<?> cls) {
        return Can.ofArray(cls.getConstructors());
    }

    // -- GENENERIC TYPE ARG INTROSPECTION

    @lombok.Value(staticConstructor = "of")
    public class MethodAndImplementingClass {
        final @NonNull Method method;
        final @NonNull Class<?> implementingClass;
        /**
         * [CAUSEWAY-3164] ensures reflection on generic type arguments works in a concurrent introspection setting
         */
        public Try<MethodAndImplementingClass> adopt(final @NonNull ClassLoader classLoader) {
            try {
                val ownerReloaded = Class.forName(implementingClass.getName(), true, classLoader);
                val methodReloaded = ownerReloaded.getMethod(method.getName(), method.getParameterTypes());
                return Try.success(MethodAndImplementingClass.of(methodReloaded, ownerReloaded));
            } catch (Throwable e) {
                return Try.failure(e);
            }
        }
        public Try<MethodAndImplementingClass> adoptIntoDefaultClassLoader() {
            return adopt(_Context.getDefaultClassLoader());
        }
        public Class<?> resolveFirstGenericTypeArgumentOnMethodReturn() {
            return genericTypeArg(ResolvableType.forMethodReturnType(method, implementingClass))
                    .toClass();
        }
        public Class<?> resolveFirstGenericTypeArgumentOnParameter(final int paramIndex) {
            return genericTypeArg(ResolvableType.forMethodParameter(method, paramIndex, implementingClass))
                    .toClass();
        }
        // -- HELPER
        private static ResolvableType genericTypeArg(final ResolvableType nonScalar){
            val genericTypeArg = nonScalar.isArray()
                    ? nonScalar.getComponentType()
                    : nonScalar.getGeneric(0);
            return genericTypeArg;
        }
    }

    @lombok.Value(staticConstructor = "of")
    public class ConstructorAndImplementingClass {
        final @NonNull Constructor<?> constructor;
        final @NonNull Class<?> implementingClass;
        /**
         * [CAUSEWAY-3164] ensures reflection on generic type arguments works in a concurrent introspection setting
         */
        public Try<ConstructorAndImplementingClass> adopt(final @NonNull ClassLoader classLoader) {
            try {
                val ownerReloaded = Class.forName(implementingClass.getName(), true, classLoader);
                val methodReloaded = ownerReloaded.getConstructor(constructor.getParameterTypes());
                return Try.success(ConstructorAndImplementingClass.of(methodReloaded, ownerReloaded));
            } catch (Throwable e) {
                return Try.failure(e);
            }
        }
        public Try<ConstructorAndImplementingClass> adoptIntoDefaultClassLoader() {
            return adopt(_Context.getDefaultClassLoader());
        }
//        public Class<?> resolveFirstGenericTypeArgumentOnMethodReturn() {
//            return genericTypeArg(ResolvableType.forConstructorParameter(executable, implementingClass))
//                    .toClass();
//        }
        public Class<?> resolveFirstGenericTypeArgumentOnParameter(final int paramIndex) {
            return genericTypeArg(ResolvableType.forConstructorParameter(constructor, paramIndex, implementingClass))
                    .toClass();
        }
        // -- HELPER
        private static ResolvableType genericTypeArg(final ResolvableType nonScalar){
            val genericTypeArg = nonScalar.isArray()
                    ? nonScalar.getComponentType()
                    : nonScalar.getGeneric(0);
            return genericTypeArg;
        }
    }

    // -- FILTER

    @UtilityClass
    public class Filter {

        public Predicate<Method> isGetter() {
            return method->method!=null
                    && method.getParameterCount()==0
                    && method.getReturnType()!=void.class
                    && (method.getName().startsWith("get")
                        || (method.getName().startsWith("is")
                                && method.getReturnType()==boolean.class));
        }

        public Predicate<Method> hasReturnType(final Class<?> expectedReturnType) {
            return method->expectedReturnType.isAssignableFrom(method.getReturnType());
        }

        public Predicate<Method> hasReturnTypeAnyOf(final Can<Class<?>> allowedReturnTypes) {
            return method->allowedReturnTypes.stream()
                    .anyMatch(allowedReturnType->allowedReturnType.isAssignableFrom(method.getReturnType()));
        }

        public Predicate<Executable> isPublic() {
            return ex->Modifier.isPublic(ex.getModifiers());
        }

        public Predicate<Executable> paramCount(final int paramCount) {
            return ex->ex.getParameterCount() == paramCount;
        }

        public Predicate<Executable> paramAssignableFrom(final int paramIndex, final Class<?> paramType) {
            return ex->ex.getParameterTypes()[paramIndex].isAssignableFrom(paramType);
        }

        //TODO simple array compare should do
        public Predicate<Executable> paramSignatureMatch(final Class<?>[] matchingParamTypes) {
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

        public Predicate<Executable> paramAssignableFromValue(final int paramIndex, final @Nullable Object value) {
            if(value==null) {
                return _Predicates.alwaysTrue();
            }
            return ex->ex.getParameterTypes()[paramIndex].isAssignableFrom(value.getClass());
        }

    }

    /**
     * Lookup regular method for a synthetic one in the method's declaring class type-hierarchy.
     */
    public Optional<Method> lookupRegularMethodForSynthetic(final @NonNull Method syntheticMethod) {

        if(!syntheticMethod.isSynthetic()) {
            return Optional.of(syntheticMethod);
        }

        return streamTypeHierarchy(syntheticMethod.getDeclaringClass(), InterfacePolicy.INCLUDE)
        .flatMap(type->_NullSafe.stream(type.getDeclaredMethods()))
        .filter(methodMatcherOnNameAndSignature(syntheticMethod))
        .filter(method->!method.isSynthetic())
        .findFirst();
    }

    private Predicate<Method> methodMatcherOnNameAndSignature(final @NonNull Method ref) {
        val refSignature = ref.getParameterTypes();
        return other->
            (!ref.getName().equals(other.getName()))
            ? false
            : Arrays.equals(refSignature, other.getParameterTypes());
    }

    public String methodToShortString(final @NonNull Method method) {

        return method.getName() + "(" +

            Stream.of(method.getParameterTypes())
            .map(parameterType->parameterType.getTypeName())
            .collect(Collectors.joining(", "))

        + ")";
    }

    /**
     * Determine if the supplied method is declared within a non-static <em>inner class</em>.
     */
    public boolean isNonStaticInnerMethod(final @NonNull Method method) {
        return ClassUtils.isInnerClass(method.getDeclaringClass());
    }

    /**
     * Whether has any generic bounds on any return type or parameter type(s).
     */
    public boolean hasGenericBounds(final @NonNull Method method) {
        return hasGenericReturn(method)
                || hasGenericParam(method);
    }

    /**
     * Whether has any generic bounds on the method return type.
     */
    public boolean hasGenericReturn(final @NonNull Method method) {
        return method.getGenericReturnType() instanceof TypeVariable<?>;
    }

    /**
     * Whether has any generic bounds any parameter type(s).
     */
    public boolean hasGenericParam(final @NonNull Method method) {
        final Type[] genericParameterTypes = method.getGenericParameterTypes();
        for(int i=0; i<method.getParameterCount(); ++i) {
            if(genericParameterTypes[i] instanceof TypeVariable<?>) {
                return true;
            }
        }
        return false;
    }

    /**
     * Will fail if methods don't share the same type hierarchy branch.
     * @param methods assumed to pass checks #methodsWeaklySame and #shareSameTypeHierarchy
     * @return the most specific with the type hierarchy
     */
    public Optional<Method> mostSpecificMethodOf(
            final @NonNull Can<Method> methods) {

        switch(methods.getCardinality()) {
        case ZERO:
            return Optional.empty();
        case ONE:
            return methods.getFirst();
        case MULTIPLE:
            break; // fall through
        }

        // assert all methods share the same type hierarchy branch
        val first = methods.getFirstElseFail();
        methods
            .stream()
            .skip(1)
            .forEach(next->{
                _Assert.assertTrue(methodsWeaklySame(first, next));
                _Assert.assertTrue(shareSameTypeHierarchy(first, next));
            });

        return methods
            .stream()
            .distinct()
            .max((a, b)->{
                val clsA = a.getDeclaringClass();
                val clsB = b.getDeclaringClass();
                if(clsA.equals(clsB)) {
                    throw _Exceptions.unrecoverable("methods declared on the same type "
                            + "cannot pass methodsWeaklySame check "
                            + "and at the same time be distinct");
                }
                return clsA.isAssignableFrom(clsB)
                        ? -1
                        : 1;
            });
    }

}
