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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.core.commons.lang.ThrowableExtensions;
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

    /**
     * Searches for annotation on provided method, and if not found for any
     * inherited methods up from the superclass.
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
     * Searches for all no-arg methods or fields with a specified title, returning an
     * {@link Evaluator} object that wraps either.  Will search up hierarchy also.
     */
    public static <T extends Annotation> List<Evaluator<T>> getEvaluators(
            final Class<?> cls,
            final Class<T> annotationClass) {
        List<Evaluator<T>> evaluators = Lists.newArrayList();
        appendEvaluators(cls, annotationClass, evaluators);
        return evaluators;
    }

    private static <T extends Annotation> void appendEvaluators(
            final Class<?> cls,
            final Class<T> annotationClass,
            final List<Evaluator<T>> evaluators) {

        for (Method method : cls.getDeclaredMethods()) {
            if(MethodScope.OBJECT.matchesScopeOf(method) &&
                    method.getParameterTypes().length == 0) {
                final Annotation annotation = method.getAnnotation(annotationClass);
                if(annotation != null) {
                    evaluators.add(new MethodEvaluator(method, annotation));
                }
            }
        }
        for (final Field field: cls.getDeclaredFields()) {
            final Annotation annotation = field.getAnnotation(annotationClass);
            if(annotation != null) {
                evaluators.add(new FieldEvaluator(field, annotation));
            }
        }

        // search superclasses
        final Class<?> superclass = cls.getSuperclass();
        if (superclass != null) {
            appendEvaluators(superclass, annotationClass, evaluators);
        }

        // search implemented interfaces
        final Class<?>[] interfaces = cls.getInterfaces();
        for (final Class<?> iface : interfaces) {
            appendEvaluators(iface, annotationClass, evaluators);
        }
    }

    public static abstract class Evaluator<T extends Annotation> {

        private final T annotation;

        protected Evaluator(final T annotation) {
            this.annotation = annotation;
        }

        public T getAnnotation() {
            return annotation;
        }

        public abstract Object value(final Object obj) ;
    }

    public static class MethodEvaluator<T extends Annotation> extends Evaluator<T> {
        private final Method method;

        MethodEvaluator(final Method method, final T annotation) {
            super(annotation);
            this.method = method;
        }

        public Object value(final Object obj)  {
            try {
                return method.invoke(obj);
            } catch (final InvocationTargetException e) {
                ThrowableExtensions.throwWithinIsisException(e, "Exception executing " + method);
                return null;
            } catch (final IllegalAccessException e) {
                throw new MetaModelException("illegal access of " + method, e);
            }
        }

        public Method getMethod() {
            return method;
        }
    }

    static class FieldEvaluator<T extends Annotation> extends Evaluator<T> {
        private final Field field;

        FieldEvaluator(final Field field, final T annotation) {
            super(annotation);
            this.field = field;
        }

        public Object value(final Object obj)  {
            try {
                field.setAccessible(true);
                return field.get(obj);
            } catch (final IllegalAccessException e) {
                throw new MetaModelException("illegal access of " + field, e);
            }
        }

        public Field getField() {
            return field;
        }
    }

    private static List<Class<?>> fieldAnnotationClasses = Collections.unmodifiableList(
            Arrays.<Class<?>>asList(
                    Property.class,
                    PropertyLayout.class,
                    Collection.class,
                    CollectionLayout.class,
                    Programmatic.class,
                    MemberOrder.class,
                    javax.annotation.Nullable.class,
                    Title.class,
                    XmlJavaTypeAdapter.class,
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
    public static Annotation[][] getParameterAnnotations(final Method method) {
        if (method == null) {
            return new Annotation[0][0];
        }
        final Annotation[][] allParamAnnotations = method.getParameterAnnotations();

        boolean foundAnnotationsForAnyParameter = false;
        for (final Annotation[] singleParamAnnotations : allParamAnnotations) {
            if (singleParamAnnotations.length > 0) {
                foundAnnotationsForAnyParameter = true;
                break;
            }
        }
        if (foundAnnotationsForAnyParameter) {
            return allParamAnnotations;
        }

        final Class<?> methodDeclaringClass = method.getDeclaringClass();

        // search superclasses
        final Class<?> superclass = methodDeclaringClass.getSuperclass();
        if (superclass != null) {
            try {
                final Method parentClassMethod = superclass.getMethod(method.getName(), method.getParameterTypes());
                return getParameterAnnotations(parentClassMethod);
            } catch (final SecurityException | NoSuchMethodException e) {
                // fall through
            }
        }

        // search implemented interfaces
        final Class<?>[] interfaces = methodDeclaringClass.getInterfaces();
        for (final Class<?> iface : interfaces) {
            try {
                final Method ifaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                return getParameterAnnotations(ifaceMethod);
            } catch (final SecurityException | NoSuchMethodException e) {
                // fall through
            }
        }

        return noParamAnnotationsFor(method);
    }

    private static Annotation[][] noParamAnnotationsFor(final Method method) {
        return new Annotation[method.getParameterTypes().length][0];
    }

}
