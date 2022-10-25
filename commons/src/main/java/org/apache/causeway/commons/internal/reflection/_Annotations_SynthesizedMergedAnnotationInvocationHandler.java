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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.core.annotation.AnnotationConfigurationException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.SynthesizedAnnotation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import org.apache.causeway.commons.internal.base._NullSafe;

import lombok.NonNull;
import lombok.val;

/**
 * {@link InvocationHandler} for an {@link Annotation} that Spring has
 * <em>synthesized</em> (i.e. wrapped in a dynamic proxy) with additional
 * functionality such as attribute alias handling.
 * <p>
 * Copy and pasted from (credits to) Spring!
 * <p>
 * Modified to support MergedAnnotations instead of MergedAnnotation.
 *
 * @param <A> the annotation type
 * @see Annotation
 * @see AnnotationUtils#synthesizeAnnotation(Annotation, AnnotatedElement)
 */
final class _Annotations_SynthesizedMergedAnnotationInvocationHandler<A extends Annotation>
implements InvocationHandler {

    private final @NonNull MergedAnnotations mergedAnnotations;
    private final @Nullable MergedAnnotations additionalAnnotations;
    private final Class<A> type;
    private final _Annotations_AttributeMethods attributes;

    @Nullable
    private volatile Integer hashCode;


    private _Annotations_SynthesizedMergedAnnotationInvocationHandler(
            final MergedAnnotations mergedAnnotations,
            final MergedAnnotations additionalAnnotations,
            final Class<A> type) {

        Assert.notNull(mergedAnnotations, "MergedAnnotations must not be null");
        Assert.notNull(type, "Type must not be null");
        Assert.isTrue(type.isAnnotation(), "Type must be an annotation");
        this.mergedAnnotations = mergedAnnotations;
        this.additionalAnnotations = additionalAnnotations;
        this.type = type;
        this.attributes = _Annotations_AttributeMethods.forAnnotationType(type);
        for (int i = 0; i < this.attributes.size(); i++) {
            getAttributeValue(this.attributes.get(i));
        }

    }


    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        if (ReflectionUtils.isEqualsMethod(method)) {
            return annotationEquals(args[0]);
        }
        if (ReflectionUtils.isHashCodeMethod(method)) {
            return annotationHashCode();
        }
        if (ReflectionUtils.isToStringMethod(method)) {
            return this.mergedAnnotations.toString();
        }
        if (isAnnotationTypeMethod(method)) {
            return this.type;
        }
        if (this.attributes.indexOf(method.getName()) != -1) {
            return getAttributeValue(method);
        }
        throw new AnnotationConfigurationException(String.format(
                "Method [%s] is unsupported for synthesized annotation type [%s]", method, this.type));
    }

    private boolean isAnnotationTypeMethod(final Method method) {
        return (Objects.equals(method.getName(), "annotationType") && method.getParameterCount() == 0);
    }

    /**
     * See {@link Annotation#equals(Object)} for a definition of the required algorithm.
     * @param other the other object to compare against
     */
    private boolean annotationEquals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!this.type.isInstance(other)) {
            return false;
        }
        for (int i = 0; i < this.attributes.size(); i++) {
            Method attribute = this.attributes.get(i);
            Object thisValue = getAttributeValue(attribute);
            Object otherValue = ReflectionUtils.invokeMethod(attribute, other);
            if (!ObjectUtils.nullSafeEquals(thisValue, otherValue)) {
                return false;
            }
        }
        return true;
    }

    /**
     * See {@link Annotation#hashCode()} for a definition of the required algorithm.
     */
    private int annotationHashCode() {
        Integer hashCode = this.hashCode;
        if (hashCode == null) {
            hashCode = computeHashCode();
            this.hashCode = hashCode;
        }
        return hashCode;
    }

    private Integer computeHashCode() {
        int hashCode = 0;
        for (int i = 0; i < this.attributes.size(); i++) {
            Method attribute = this.attributes.get(i);
            Object value = getAttributeValue(attribute);
            hashCode += (127 * attribute.getName().hashCode()) ^ getValueHashCode(value);
        }
        return hashCode;
    }

    private int getValueHashCode(final @Nullable Object value) {
        if(value==null) {
            return 0;
        }
        // Use Arrays.hashCode since ObjectUtils doesn't comply to to
        // Annotation#hashCode()
        if (value instanceof boolean[]) {
            return Arrays.hashCode((boolean[]) value);
        }
        if (value instanceof byte[]) {
            return Arrays.hashCode((byte[]) value);
        }
        if (value instanceof char[]) {
            return Arrays.hashCode((char[]) value);
        }
        if (value instanceof double[]) {
            return Arrays.hashCode((double[]) value);
        }
        if (value instanceof float[]) {
            return Arrays.hashCode((float[]) value);
        }
        if (value instanceof int[]) {
            return Arrays.hashCode((int[]) value);
        }
        if (value instanceof long[]) {
            return Arrays.hashCode((long[]) value);
        }
        if (value instanceof short[]) {
            return Arrays.hashCode((short[]) value);
        }
        if (value instanceof Object[]) {
            return Arrays.hashCode((Object[]) value);
        }
        return value.hashCode();
    }

    @Nullable
    private Object getAttributeValue(final Method method) {
        String name = method.getName();
        Class<?> type = ClassUtils.resolvePrimitiveIfNecessary(method.getReturnType());

        val defaultValue = method.getDefaultValue();

        // for all discovered annotations of this.type determine the effective (attribute) value
        val attributeValue = streamAnnotations()
        .map(mergedAnnotation->(Object)mergedAnnotation.getValue(name, type).orElse(null))
        .filter(_NullSafe::isPresent)
        .filter(value->!value.equals(defaultValue))
        .findFirst()
        .orElse(defaultValue);

        return attributeValue;
    }

    @SuppressWarnings("unchecked")
    static <A extends Annotation> Optional<A> createProxy(
            final @NonNull MergedAnnotations collected,
            final @NonNull Optional<MergedAnnotations> additional,
            final @NonNull Class<A> annotationType) {

        val hasCollected = collected
                .isPresent(annotationType);
        val hasAdditional = additional
                .map(mergedAnnotations->mergedAnnotations.isPresent(annotationType))
                .orElse(false);

        if(!hasCollected
                && !hasAdditional) {
            // annotation is neither present on getter nor field
            return Optional.empty();
        }

        val invocationHandler = hasCollected
                ? new _Annotations_SynthesizedMergedAnnotationInvocationHandler<>(
                        collected, additional.orElse(null), annotationType)
                : new _Annotations_SynthesizedMergedAnnotationInvocationHandler<>(
                        additional.get(), null, annotationType);

        val classLoader = annotationType.getClassLoader();
        val interfaces = isVisible(classLoader, SynthesizedAnnotation.class)
                ? new Class<?>[] {annotationType, SynthesizedAnnotation.class}
                : new Class<?>[] {annotationType};
        val proxy = (A) Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);
        return Optional.of(proxy);
    }

    private static boolean isVisible(final ClassLoader classLoader, final Class<?> interfaceClass) {
        try {
            return Class.forName(interfaceClass.getName(), false, classLoader) == interfaceClass;
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }

    /**
     * If annotations from a getter method are competing with annotations from its corresponding field,
     * let the one win, that is 'nearer' to the <i>Class</i> that is subject to introspection.
     */
    private Stream<MergedAnnotation<A>> streamAnnotations() {
        return additionalAnnotations!=null
                ? Stream.concat(mergedAnnotations.stream(type), additionalAnnotations.stream(type))
                        .sorted((a, b)->Integer.compare(a.getAggregateIndex(), b.getAggregateIndex()))
                : mergedAnnotations.stream(type);
    }

}

