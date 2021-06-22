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
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.isis.applib.exceptions.unrecoverable.MetaModelException;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.metamodel.commons.MethodUtil;
import org.apache.isis.core.metamodel.commons.ThrowableExtensions;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
public final class Annotations  {


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
     * @since 2.0
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
            final Predicate<Class<?>> filter,
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
            if(! MethodUtil.isStatic(method) &&
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
        @Getter private final Method method;

        MethodEvaluator(final Method method, final T annotation) {
            super(annotation);
            this.method = method;
        }

        @Override
        protected String name() {
            return method.getName();
        }

        @Override
        protected MethodHandle createMethodHandle() throws IllegalAccessException {
            return _Reflect.handleOf(method);
        }
    }

    public static class FieldEvaluator<T extends Annotation> extends Evaluator<T> {
        @Getter private final Field field;

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
            return _Reflect.handleOfGetterOn(field);
        }

        public Optional<Method> getGetter(final Class<?> originatingClass) {
            try {
                return Optional.ofNullable(
                        _Reflect.getGetter(originatingClass, field.getName())    );
            } catch (IntrospectionException e) {
                log.warn("failed reflective introspection on {} field {}",
                        originatingClass, field.getName(), e);
            }
            return Optional.empty();
        }

    }

}
