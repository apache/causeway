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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.springframework.core.annotation.MergedAnnotation;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * Provides a quick way to access the attribute methods of an {@link Annotation}
 * with consistent ordering as well as a few useful utility methods.
 * <p>
 * Copy and pasted from (all credits to) Spring!
 */
final class _Annotations_AttributeMethods {

    static final _Annotations_AttributeMethods NONE = new _Annotations_AttributeMethods(null, new Method[0]);

    private static final Map<Class<? extends Annotation>, _Annotations_AttributeMethods> cache =
            new ConcurrentReferenceHashMap<>();

    private static final Comparator<Method> methodComparator = (m1, m2) -> {
        if (m1 != null && m2 != null) {
            return m1.getName().compareTo(m2.getName());
        }
        return m1 != null ? -1 : 1;
    };

    @Nullable
    private final Class<? extends Annotation> annotationType;

    private final Method[] attributeMethods;

    private final boolean[] canThrowTypeNotPresentException;

    private final boolean hasDefaultValueMethod;

    private final boolean hasNestedAnnotation;

    private _Annotations_AttributeMethods(
            final @Nullable Class<? extends Annotation> annotationType,
            final Method[] attributeMethods) {

        this.annotationType = annotationType;
        this.attributeMethods = attributeMethods;
        this.canThrowTypeNotPresentException = new boolean[attributeMethods.length];
        boolean foundDefaultValueMethod = false;
        boolean foundNestedAnnotation = false;
        for (int i = 0; i < attributeMethods.length; i++) {
            Method method = this.attributeMethods[i];
            Class<?> type = method.getReturnType();
            if (method.getDefaultValue() != null) {
                foundDefaultValueMethod = true;
            }
            if (type.isAnnotation() ||
                    (type.isArray() && type.getComponentType().isAnnotation())) {
                foundNestedAnnotation = true;
            }
            //method.setAccessible(true); ... why?
            this.canThrowTypeNotPresentException[i] =
                    type == Class.class ||
                    type == Class[].class ||
                    type.isEnum();
        }
        this.hasDefaultValueMethod = foundDefaultValueMethod;
        this.hasNestedAnnotation = foundNestedAnnotation;
    }

    /**
     * Determine if this instance only contains a single attribute named
     * {@code value}.
     * @return {@code true} if there is only a value attribute
     */
    boolean hasOnlyValueAttribute() {
        return (this.attributeMethods.length == 1 &&
                MergedAnnotation.VALUE.equals(this.attributeMethods[0].getName()));
    }

    /**
     * Determine if values from the given annotation can be safely accessed without
     * causing any {@link TypeNotPresentException TypeNotPresentExceptions}.
     * @param annotation the annotation to check
     * @return {@code true} if all values are present
     * @see #validate(Annotation)
     */
    boolean isValid(final Annotation annotation) {
        assertAnnotation(annotation);
        for (int i = 0; i < size(); i++) {
            if (canThrowTypeNotPresentException(i)) {
                try {
                    get(i).invoke(annotation);
                }
                catch (Throwable ex) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if values from the given annotation can be safely accessed without causing
     * any {@link TypeNotPresentException TypeNotPresentExceptions}. In particular,
     * this method is designed to cover Google App Engine's late arrivar of such
     * exceptions for {@code Class} values (instead of the more typical early
     * {@code Class.getAnnotations() failure}.
     * @param annotation the annotation to validate
     * @throws IllegalStateException if a declared {@code Class} attribute could not be read
     * @see #isValid(Annotation)
     */
    void validate(final Annotation annotation) {
        assertAnnotation(annotation);
        for (int i = 0; i < size(); i++) {
            if (canThrowTypeNotPresentException(i)) {
                try {
                    get(i).invoke(annotation);
                }
                catch (Throwable ex) {
                    throw new IllegalStateException("Could not obtain annotation attribute value for " +
                            get(i).getName() + " declared on " + annotation.annotationType(), ex);
                }
            }
        }
    }

    private void assertAnnotation(final Annotation annotation) {
        Assert.notNull(annotation, "Annotation must not be null");
        if (this.annotationType != null) {
            Assert.isInstanceOf(this.annotationType, annotation);
        }
    }

    /**
     * Get the attribute with the specified name or {@code null} if no
     * matching attribute exists.
     * @param name the attribute name to find
     * @return the attribute method or {@code null}
     */
    @Nullable
    Method get(final String name) {
        int index = indexOf(name);
        return index != -1 ? this.attributeMethods[index] : null;
    }

    /**
     * Get the attribute at the specified index.
     * @param index the index of the attribute to return
     * @return the attribute method
     * @throws IndexOutOfBoundsException if the index is out of range
     * (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    Method get(final int index) {
        return this.attributeMethods[index];
    }

    /**
     * Determine if the attribute at the specified index could throw a
     * {@link TypeNotPresentException} when accessed.
     * @param index the index of the attribute to check
     * @return {@code true} if the attribute can throw a
     * {@link TypeNotPresentException}
     */
    boolean canThrowTypeNotPresentException(final int index) {
        return this.canThrowTypeNotPresentException[index];
    }

    /**
     * Get the index of the attribute with the specified name, or {@code -1}
     * if there is no attribute with the name.
     * @param name the name to find
     * @return the index of the attribute, or {@code -1}
     */
    int indexOf(final String name) {
        for (int i = 0; i < this.attributeMethods.length; i++) {
            if (this.attributeMethods[i].getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the index of the specified attribute, or {@code -1} if the
     * attribute is not in this collection.
     * @param attribute the attribute to find
     * @return the index of the attribute, or {@code -1}
     */
    int indexOf(final Method attribute) {
        for (int i = 0; i < this.attributeMethods.length; i++) {
            if (this.attributeMethods[i].equals(attribute)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the number of attributes in this collection.
     * @return the number of attributes
     */
    int size() {
        return this.attributeMethods.length;
    }

    /**
     * Determine if at least one of the attribute methods has a default value.
     * @return {@code true} if there is at least one attribute method with a default value
     */
    boolean hasDefaultValueMethod() {
        return this.hasDefaultValueMethod;
    }

    /**
     * Determine if at least one of the attribute methods is a nested annotation.
     * @return {@code true} if there is at least one attribute method with a nested
     * annotation type
     */
    boolean hasNestedAnnotation() {
        return this.hasNestedAnnotation;
    }

    /**
     * Get the attribute methods for the given annotation type.
     * @param annotationType the annotation type
     * @return the attribute methods for the annotation type
     */
    static _Annotations_AttributeMethods forAnnotationType(@Nullable final Class<? extends Annotation> annotationType) {
        if (annotationType == null) {
            return NONE;
        }
        return cache.computeIfAbsent(annotationType, _Annotations_AttributeMethods::compute);
    }

    private static _Annotations_AttributeMethods compute(final Class<? extends Annotation> annotationType) {
        Method[] methods = annotationType.getDeclaredMethods();
        int size = methods.length;
        for (int i = 0; i < methods.length; i++) {
            if (!isAttributeMethod(methods[i])) {
                methods[i] = null;
                size--;
            }
        }
        if (size == 0) {
            return NONE;
        }
        Arrays.sort(methods, methodComparator);
        Method[] attributeMethods = Arrays.copyOf(methods, size);
        return new _Annotations_AttributeMethods(annotationType, attributeMethods);
    }

    private static boolean isAttributeMethod(final Method method) {
        return (method.getParameterCount() == 0 && method.getReturnType() != void.class);
    }

    /**
     * Create a description for the given attribute method suitable to use in
     * exception messages and logs.
     * @param attribute the attribute to describe
     * @return a description of the attribute
     */
    static String describe(@Nullable final Method attribute) {
        if (attribute == null) {
            return "(none)";
        }
        return describe(attribute.getDeclaringClass(), attribute.getName());
    }

    /**
     * Create a description for the given attribute method suitable to use in
     * exception messages and logs.
     * @param annotationType the annotation type
     * @param attributeName the attribute name
     * @return a description of the attribute
     */
    static String describe(@Nullable final Class<?> annotationType, @Nullable final String attributeName) {
        if (attributeName == null) {
            return "(none)";
        }
        String in = (annotationType != null ? " in annotation [" + annotationType.getName() + "]" : "");
        return "attribute '" + attributeName + "'" + in;
    }

}
