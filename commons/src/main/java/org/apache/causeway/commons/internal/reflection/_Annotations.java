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

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class _Annotations {

    /**
     * Determine if the specified annotation is either directly present or meta-present.
     * <p>
     * Also includes annotated fields, getter methods might be associated with.
     * If annotations from a getter method are competing with annotations from its corresponding field,
     * let the one win, that is 'nearer' to the <i>Class</i> that is subject to introspection.
     * <p>
     * Perform a full search of the entire type hierarchy,
     * including super-classes and implemented interfaces.
     * Super-class annotations do not need to be meta-annotated with {@link Inherited}.
     *
     * @param <A>
     * @param annotatedElement
     * @param annotationType
     * @return non-null
     */
    public static <A extends Annotation> boolean isPresent(
            final AnnotatedElement annotatedElement,
            final Class<A> annotationType) {

        val collected = collect(annotatedElement, SearchStrategy.TYPE_HIERARCHY);

        if(collected.isPresent(annotationType)) {
            return true;
        }

        // also handle annotated fields, getter methods might be associated with
        return annotatedFieldForAnnotatedElement(annotatedElement, annotationType)
        .map(fieldForGetter->isPresent(fieldForGetter, annotationType))
        .orElse(false);
    }

    /**
     * Optionally create a type-safe synthesized version of this annotation based on presence.
     * <p>
     * Also includes annotated fields, getter methods might be associated with.
     * If annotations from a getter method are competing with annotations from its corresponding field,
     * let the one win, that is 'nearer' to the <i>Class</i> that is subject to introspection.
     * <p>
     * Perform a full search of the entire type hierarchy,
     * including super-classes and implemented interfaces.
     * Super-class annotations do not need to be meta-annotated with {@link Inherited}.
     *
     * @param <A>
     * @param annotatedElement
     * @param annotationType
     * @return non-null
     */
    public static <A extends Annotation> Optional<A> synthesize(
            final AnnotatedElement annotatedElement,
            final Class<A> annotationType) {

        return synthesize(annotatedElement, annotationType, SearchStrategy.TYPE_HIERARCHY);
    }

    /**
     * Optionally create a type-safe synthesized version of this annotation based on presence.
     * <p>
     * Also includes annotated fields, getter methods might be associated with.
     * If annotations from a getter method are competing with annotations from its corresponding field,
     * let the one win, that is 'nearer' to the <i>Class</i> that is subject to introspection.
     * <p>
     * Find only directly declared annotations,
     * without considering {@link Inherited} annotations and
     * without searching super-classes or implemented interfaces.
     *
     * @param <A>
     * @param annotatedElement
     * @param annotationType
     * @return non-null
     */
    public static <A extends Annotation> Optional<A> synthesizeDirect(
            final AnnotatedElement annotatedElement,
            final Class<A> annotationType) {

        return synthesize(annotatedElement, annotationType, SearchStrategy.DIRECT);
    }

    // -- HELPER

    /**
     * Optionally create a type-safe synthesized version of this annotation based on presence.
     * <p>
     * Also includes annotated fields, getter methods might be associated with.
     * If annotations from a getter method are competing with annotations from its corresponding field,
     * let the one win, that is 'nearer' to the <i>Class</i> that is subject to introspection.
     */
    private static <A extends Annotation> Optional<A> synthesize(
            final AnnotatedElement annotatedElement,
            final Class<A> annotationType,
            final SearchStrategy searchStrategy) {

        val collected = collect(annotatedElement, searchStrategy);

        // also handle annotated fields, getter methods might be associated with
        val associated =
                annotatedFieldForAnnotatedElement(annotatedElement, annotationType)
                        .map(fieldForGetter->collect(fieldForGetter, searchStrategy));

        val proxyIfAny = _Annotations_SynthesizedMergedAnnotationInvocationHandler
                .createProxy(collected, associated, annotationType);

        return proxyIfAny;
    }

    /**
     * @apiNote don't publicly expose Spring's {@link MergedAnnotations}
     */
    private static MergedAnnotations collect(
            final AnnotatedElement annotatedElement,
            final SearchStrategy searchStrategy) {
        val collected = MergedAnnotations.from(annotatedElement, searchStrategy);
        return collected;
    }

    private static boolean isAnnotationAllowedOnField(final Class<? extends Annotation> annotationType) {
        val target = annotationType.getAnnotation(Target.class);
        if(target==null) {
            return false;
        }
        for(val elementType : target.value()) {
            if(elementType == ElementType.FIELD) {
                return true;
            }
        }
        return false;
    }

    private static <A extends Annotation> Optional<Field> annotatedFieldForAnnotatedElement(
            final AnnotatedElement annotatedElement,
            final Class<A> annotationType){

        return annotatedElement instanceof Method
                ? annotatedFieldForGetter((Method)annotatedElement, annotationType)
                : Optional.empty();
    }

    private static <A extends Annotation> Optional<Field> annotatedFieldForGetter(
            final Method method,
            final Class<A> annotationType){

        return (method.getName().startsWith("get")
                    || method.getName().startsWith("is"))
                && isAnnotationAllowedOnField(annotationType)
                ? _ClassCache.getInstance()
                        .fieldForGetter(method.getDeclaringClass(), method)
                : Optional.empty();
    }

}
