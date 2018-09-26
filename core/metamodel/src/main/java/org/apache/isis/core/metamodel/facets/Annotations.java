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

package org.apache.isis.core.metamodel.facets;

import java.beans.IntrospectionException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.lang.ThrowableExtensions;
import org.apache.isis.core.commons.reflection.Reflect;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.methodutils.MethodScope;

public final class Annotations  {

    private Annotations() {}

    /**
     * For convenience of the several annotations that apply only to
     * {@link String}s.
     */
    public static boolean isString(final Class<?> cls) {
        return cls.equals(String.class);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getDeclaredAnnotation(Class<?> cls, Class<T> annotationClass) {
        final Annotation[] declaredAnnotations = cls.getDeclaredAnnotations();
        if(declaredAnnotations == null) {
            return null;
        }
        for (Annotation annotation : declaredAnnotations) {
            if(annotationClass.isAssignableFrom(annotation.getClass())) {
                return (T) annotation;
            }
        }
        return null;
    }

    /**
     * Searches for annotation on provided class, and if not found for the
     * superclass.
     *
     * <p>
     * Added to allow bytecode-mangling libraries such as CGLIB to be supported.
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


    static class AnnotationAndDepth<T extends Annotation>
    implements Comparable<AnnotationAndDepth<T>> {
        AnnotationAndDepth(final T annotation, final int depth) {
            this.annotation = annotation;
            this.depth = depth;
        }
        T annotation;

        private static <T extends Annotation> List<T> sorted(
                final List<AnnotationAndDepth<T>> annotationAndDepths) {
            Collections.sort(annotationAndDepths);
            return annotationAndDepths.stream()
                    .map(AnnotationAndDepth::getAnnotation)
                    .collect(Collectors.toList());
        }

        T getAnnotation() {
            return annotation;
        }
        int depth;

        @Override
        public int compareTo(final AnnotationAndDepth<T> o) {
            return depth - o.depth;
        }
    }


    /**
     * Searches for annotation on provided class, and if not found for the
     * superclass.
     */
    public static <T extends Annotation> List<T> getAnnotations(
            final Class<?> cls,
            final Class<T> annotationClass) {

        if (cls == null) {
            return Collections.emptyList();
        }

        final List<AnnotationAndDepth<T>> annotationAndDepths = _Lists.newArrayList();
        for (final Annotation annotation : cls.getAnnotations()) {
            append(annotation, annotationClass, annotationAndDepths);
        }
        if(!annotationAndDepths.isEmpty()) {
            return AnnotationAndDepth.sorted(annotationAndDepths);
        }

        // search superclasses
        final Class<?> superclass = cls.getSuperclass();
        if (superclass != null) {
            try {
                final List<T> annotationsFromSuperclass = getAnnotations(superclass, annotationClass);
                if (!annotationsFromSuperclass.isEmpty()) {
                    return annotationsFromSuperclass;
                }
            } catch (final SecurityException e) {
                // fall through
            }
        }

        // search implemented interfaces
        final Class<?>[] interfaces = cls.getInterfaces();
        for (final Class<?> iface : interfaces) {
            final List<T> annotationsFromInterface = getAnnotations(iface, annotationClass);
            if (!annotationsFromInterface.isEmpty()) {
                return annotationsFromInterface;
            }
        }
        return Collections.emptyList();
    }

    private static <T extends Annotation> void append(
            final Annotation annotation,
            final Class<T> annotationClass,
            final List<AnnotationAndDepth<T>> annotationAndDepths) {
        appendWithDepth(annotation, annotationClass, annotationAndDepths, 0, _Lists.newArrayList());
    }

    private static <T extends Annotation> void appendWithDepth(
            final Annotation annotation,
            final Class<T> annotationClass,
            final List<AnnotationAndDepth<T>> annotationAndDepths,
            final int depth,
            final List<Annotation> visited) {
        if (visited.contains(annotation)) {
            return;
        } else {
            // prevent infinite loop
            visited.add(annotation);
        }
        final Class<? extends Annotation> annotationType = annotation.annotationType();

        // directly annotated
        if(annotationClass.isAssignableFrom(annotationType)) {
            annotationAndDepths.add(new AnnotationAndDepth<>(_Casts.uncheckedCast(annotation), depth));
        }

        // if meta-annotation
        //if(annotationType.getAnnotation(Meta.class) != null) {
        final Annotation[] annotationsOnAnnotation = annotationType.getAnnotations();
        for (final Annotation annotationOnAnnotation : annotationsOnAnnotation) {
            appendWithDepth(annotationOnAnnotation, annotationClass, annotationAndDepths, depth+1, visited);
        }
        //}
    }

    /**
     * Searches for annotation on provided method, and if not found for any
     * inherited methods up from the superclass.
     *
     * <p>
     *     WARN: this method does NOT search for meta-annotations; use {@link #getAnnotations(Class, Class)} for that.
     * </p>
     */
    public static <T extends Annotation> T getAnnotation(
            final Method method,
            final Class<T> annotationClass) {
        if (method == null) {
            return null;
        }
        final Class<?> methodDeclaringClass = method.getDeclaringClass();
        final String methodName = method.getName();

        final T annotation = method.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }


        // search for field
        if ( shouldSearchForField(annotationClass) ) {

            List<String> fieldNameCandidates = fieldNameCandidatesFor(methodName);
            for (String fieldNameCandidate : fieldNameCandidates) {
                try {
                    final Field field = methodDeclaringClass.getDeclaredField(fieldNameCandidate);
                    final T fieldAnnotation = field.getAnnotation(annotationClass);
                    if(fieldAnnotation != null) {
                        return fieldAnnotation;
                    }
                } catch (NoSuchFieldException e) {
                    // fall through
                }
            }
        }

        // search superclasses
        final Class<?> superclass = methodDeclaringClass.getSuperclass();
        if (superclass != null) {
            try {
                final Method parentClassMethod = superclass.getMethod(methodName, method.getParameterTypes());
                return getAnnotation(parentClassMethod, annotationClass);
            } catch (final SecurityException | NoSuchMethodException e) {
                // fall through
            }
        }

        // search implemented interfaces
        final Class<?>[] interfaces = methodDeclaringClass.getInterfaces();
        for (final Class<?> iface : interfaces) {
            try {
                final Method ifaceMethod = iface.getMethod(methodName, method.getParameterTypes());
                return getAnnotation(ifaceMethod, annotationClass);
            } catch (final SecurityException | NoSuchMethodException e) {
                // fall through
            }
        }

        return null;
    }

    /**
     * Searches for annotation on provided method, and if not found for any
     * inherited methods up from the superclass.
     */
    public static <T extends Annotation> List<T> getAnnotations(
            final Method method,
            final Class<T> annotationClass) {
        if (method == null) {
            return Collections.emptyList();
        }

        final List<AnnotationAndDepth<T>> annotationAndDepths = _Lists.newArrayList();
        for (final Annotation annotation : method.getAnnotations()) {
            append(annotation, annotationClass, annotationAndDepths);
        }
        if(!annotationAndDepths.isEmpty()) {
            return AnnotationAndDepth.sorted(annotationAndDepths);
        }



        // search for field
        if ( shouldSearchForField(annotationClass) ) {

            final List<String> fieldNameCandidates = fieldNameCandidatesFor(method.getName());
            for (String fieldNameCandidate : fieldNameCandidates) {
                try {
                    final Field field = method.getDeclaringClass().getDeclaredField(fieldNameCandidate);
                    for(final Annotation annotation: field.getAnnotations()) {
                        append(annotation, annotationClass, annotationAndDepths);
                    }
                } catch (NoSuchFieldException e) {
                    // fall through
                }
            }
        }
        if(!annotationAndDepths.isEmpty()) {
            return AnnotationAndDepth.sorted(annotationAndDepths);
        }


        // search superclasses
        final Class<?> superclass = method.getDeclaringClass().getSuperclass();
        if (superclass != null) {
            try {
                final Method parentClassMethod = superclass.getMethod(method.getName(), method.getParameterTypes());
                final List<T> annotationsFromSuperclass = getAnnotations(parentClassMethod, annotationClass);
                if(!annotationsFromSuperclass.isEmpty()) {
                    return annotationsFromSuperclass;
                }
            } catch (final SecurityException | NoSuchMethodException e) {
                // fall through
            }
        }

        // search implemented interfaces
        final Class<?>[] interfaces = method.getDeclaringClass().getInterfaces();
        for (final Class<?> iface : interfaces) {
            try {
                final Method ifaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                final List<T> annotationsFromInterfaces = getAnnotations(ifaceMethod, annotationClass);
                if(!annotationsFromInterfaces.isEmpty()) {
                    return annotationsFromInterfaces;
                }
            } catch (final SecurityException | NoSuchMethodException e) {
                // fall through
            }
        }

        return Collections.emptyList();
    }

    /**
     * Searches for all no-arg methods or fields with a specified title, returning an
     * {@link Evaluator} object that wraps either. Will search up hierarchy also,
     * including implemented interfaces.
     */
    public static <T extends Annotation> List<Evaluator<T>> getEvaluators(
            final Class<?> cls,
            final Class<T> annotationClass) {
        final List<Evaluator<T>> evaluators = _Lists.newArrayList();
        visitEvaluators(cls, annotationClass, evaluators::add);

        // search implemented interfaces
        final Class<?>[] interfaces = cls.getInterfaces();
        for (final Class<?> iface : interfaces) {
            visitEvaluators(iface, annotationClass, evaluators::add);
        }

        return evaluators;
    }

    /**
     * Starting from the current class {@code cls}, we search down the inheritance
     * hierarchy (super class, super super class, ...), until we find
     * the first class that has at least a field or no-arg method with {@code annotationClass} annotation.
     * <br/>
     * In this hierarchy traversal, implemented interfaces are not processed.
     * @param cls
     * @param annotationClass
     * @param filter
     * @return list of {@link Evaluator} that wraps each annotated member found on the class where
     * the search stopped, or an empty list if no such {@code annotationClass} annotation found.
     *
     * @since 2.0.0
     */
    public static <T extends Annotation> List<Evaluator<T>> firstEvaluatorsInHierarchyHaving(
            final Class<?> cls,
            final Class<T> annotationClass,
            final Predicate<Evaluator<T>> filter) {

        final List<Evaluator<T>> evaluators = _Lists.newArrayList();
        visitEvaluatorsWhile(cls, annotationClass, __->evaluators.isEmpty(), evaluator->{
            if(filter.test(evaluator)) {
                evaluators.add(evaluator);
            }
        });

        return evaluators;
    }

    private static <T extends Annotation> void visitEvaluators(
            final Class<?> cls,
            final Class<T> annotationClass,
            final Consumer<Evaluator<T>> visitor) {
        visitEvaluatorsWhile(cls, annotationClass, __->true, visitor);
    }

    private static <T extends Annotation> void visitEvaluatorsWhile(
            final Class<?> cls,
            final Class<T> annotationClass,
            Predicate<Class<?>> filter,
            final Consumer<Evaluator<T>> visitor) {

        if(!filter.test(cls))
            return; // stop visitation

        visitMethodEvaluators(cls, annotationClass, visitor);
        visitFieldEvaluators(cls, annotationClass, visitor);

        // search super-classes
        final Class<?> superclass = cls.getSuperclass();
        if (superclass != null) {
            visitEvaluatorsWhile(superclass, annotationClass, filter, visitor);
        }

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T extends Annotation> void visitMethodEvaluators(
            final Class<?> cls,
            final Class<T> annotationClass,
            final Consumer<Evaluator<T>> visitor) {

        for (Method method : cls.getDeclaredMethods()) {
            if(MethodScope.OBJECT.matchesScopeOf(method) &&
                    method.getParameterTypes().length == 0) {
                final Annotation annotation = method.getAnnotation(annotationClass);
                if(annotation != null) {
                    visitor.accept(new MethodEvaluator(method, annotation));
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T extends Annotation> void visitFieldEvaluators(
            final Class<?> cls,
            final Class<T> annotationClass,
            final Consumer<Evaluator<T>> visitor) {

        for (final Field field: cls.getDeclaredFields()) {
            final Annotation annotation = field.getAnnotation(annotationClass);
            if(annotation != null) {
                visitor.accept(new FieldEvaluator(field, annotation));
            }
        }
    }

    public static abstract class Evaluator<T extends Annotation> {
        private final T annotation;
        private MethodHandle mh;

        protected Evaluator(final T annotation) {
            this.annotation = annotation;
        }

        public T getAnnotation() {
            return annotation;
        }

        protected abstract MethodHandle createMethodHandle() throws IllegalAccessException;
        protected abstract String name();

        public Object value(final Object obj) {
            if(mh==null) {
                try {
                    mh = createMethodHandle();
                } catch (IllegalAccessException e) {
                    throw new MetaModelException("illegal access of " + name(), e);
                }
            }

            try {
                return mh.invoke(obj);
            } catch (Throwable e) {
                return ThrowableExtensions.handleInvocationException(e, name());
            }

        }
    }

    public static class MethodEvaluator<T extends Annotation> extends Evaluator<T> {
        private final Method method;

        MethodEvaluator(final Method method, final T annotation) {
            super(annotation);
            this.method = method;
        }

        @Override
        protected String name() {
            return method.getName();
        }

        //        public Object value(final Object obj)  {
        //            try {
        //                return method.invoke(obj);
        //            } catch (final InvocationTargetException e) {
        //                ThrowableExtensions.throwWithinIsisException(e, "Exception executing " + method);
        //                return null;
        //            } catch (final IllegalAccessException e) {
        //                throw new MetaModelException("illegal access of " + method, e);
        //            }
        //        }

        public Method getMethod() {
            return method;
        }

        @Override
        protected MethodHandle createMethodHandle() throws IllegalAccessException {
            return Reflect.handleOf(method);
        }
    }

    public static class FieldEvaluator<T extends Annotation> extends Evaluator<T> {
        private final Field field;

        FieldEvaluator(final Field field, final T annotation) {
            super(annotation);
            this.field = field;
        }

        @Override
        protected String name() {
            return field.getName();
        }

        @Override
        protected MethodHandle createMethodHandle() throws IllegalAccessException {
            return Reflect.handleOf(field);
        }

        //        public Object value(final Object obj)  {
        //            try {
        //                field.setAccessible(true);
        //                return field.get(obj);
        //            } catch (final IllegalAccessException e) {
        //                throw new MetaModelException("illegal access of " + field, e);
        //            }
        //        }

        public Field getField() {
            return field;
        }

        public Optional<Method> getGetter(Class<?> originatingClass) {
            try {
                return Optional.ofNullable(
                        Reflect.getGetter(originatingClass, field.getName())	);
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }

    }

    private static List<Class<?>> fieldAnnotationClasses = Collections.unmodifiableList(
            Arrays.<Class<?>>asList(
                    Property.class,
                    PropertyLayout.class,
                    Collection.class,
                    CollectionLayout.class,
                    MemberOrder.class,
                    Pattern.class,
                    javax.annotation.Nullable.class,
                    Title.class,
                    XmlJavaTypeAdapter.class,
                    XmlTransient.class,
                    javax.jdo.annotations.Column.class
                    )
            );
    private static boolean shouldSearchForField(final Class<?> annotationClass) {
        return fieldAnnotationClasses.contains(annotationClass);
    }

    static List<String> fieldNameCandidatesFor(final String methodName) {
        if(methodName == null) {
            return Collections.emptyList();
        }
        int beginIndex;
        if (methodName.startsWith("get")) {
            beginIndex = 3;
        } else if (methodName.startsWith("is")) {
            beginIndex = 2;
        } else {
            beginIndex = -1;
        }
        if(beginIndex == -1) {
            return Collections.emptyList();
        }
        final String suffix = methodName.substring(beginIndex);
        if(suffix.length() == 0) {
            return Collections.emptyList();
        }
        final char c = suffix.charAt(0);
        final char lower = Character.toLowerCase(c);
        final String candidate = "" + lower + suffix.substring(1);
        return Arrays.asList(candidate, "_" + candidate);
    }

    /**
     * Searches for annotation on provided method, and if not found for any
     * inherited methods up from the superclass.
     *
     * <p>
     * Added to allow bytecode-mangling libraries such as CGLIB to be supported.
     */
    public static boolean isAnnotationPresent(final Method method, final Class<? extends Annotation> annotationClass) {
        if (method == null) {
            return false;
        }
        final boolean present = method.isAnnotationPresent(annotationClass);
        if (present) {
            return true;
        }

        final Class<?> methodDeclaringClass = method.getDeclaringClass();

        // search superclasses
        final Class<?> superclass = methodDeclaringClass.getSuperclass();
        if (superclass != null) {
            try {
                final Method parentClassMethod = superclass.getMethod(method.getName(), method.getParameterTypes());
                return isAnnotationPresent(parentClassMethod, annotationClass);
            } catch (final SecurityException | NoSuchMethodException e) {
                // fall through
            }
        }

        // search implemented interfaces
        final Class<?>[] interfaces = methodDeclaringClass.getInterfaces();
        for (final Class<?> iface : interfaces) {
            try {
                final Method ifaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                return isAnnotationPresent(ifaceMethod, annotationClass);
            } catch (final SecurityException | NoSuchMethodException e) {
                // fall through
            }
        }
        return false;
    }

    /**
     * Searches for parameter annotations on provided method, and if not found
     * for any inherited methods up from the superclass.
     *
     * <p>
     * Added to allow bytecode-mangling libraries such as CGLIB to be supported.
     */
    public static <T extends Annotation> List<T> getAnnotations(
            final Method method,
            final int paramNum,
            final Class<T> annotationClass) {

        if(method == null || paramNum < 0 || paramNum >= method.getParameterCount()) {
            return Collections.emptyList();
        }

        final List<AnnotationAndDepth<T>> annotationAndDepths = _Lists.newArrayList();
        final Annotation[] parameterAnnotations = method.getParameterAnnotations()[paramNum];
        for (Annotation annotation : parameterAnnotations) {
            append(annotation, annotationClass, annotationAndDepths);
        }
        if(!annotationAndDepths.isEmpty()) {
            return AnnotationAndDepth.sorted(annotationAndDepths);
        }

        return Collections.emptyList();
    }

}
