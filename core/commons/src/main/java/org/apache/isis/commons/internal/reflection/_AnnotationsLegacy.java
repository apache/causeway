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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Lists;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import lombok.val;

public final class _AnnotationsLegacy  {

    private _AnnotationsLegacy() {}
    
    public static <A extends Annotation> Optional<A> nearest(
            AnnotatedElement annotatedElement, 
            Class<A> annotationType) {
        
        if(annotatedElement instanceof Method) {
            return _AnnotationsLegacy.getAnnotations((Method)annotatedElement, annotationType)
                    .stream()
                    .findFirst();
        }
        if(annotatedElement instanceof Parameter) {
            val param = (Parameter)annotatedElement;
            val method = (Method)param.getDeclaringExecutable();
            val params = method.getParameters();
            for(int i = 0 ; i<params.length; ++i) {
                int paramIndex = i;
                if(params[i] == param) {
                    return _AnnotationsLegacy.getAnnotations(method, paramIndex, annotationType)
                            .stream()
                            .findFirst();        
                }
            }
            throw new NoSuchElementException();
            
            
        }
        return _AnnotationsLegacy.getAnnotations((Class<?>)annotatedElement, annotationType)
                .stream()
                .findFirst();

    }

    private static class AnnotationAndDepth<T extends Annotation>
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
     * @deprecated use {@link _Annotations} instead
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
     * @deprecated use {@link _Annotations} instead
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
            declaredFields_matching(method.getDeclaringClass(), isFieldForGetter(method), field->{
                for(final Annotation annotation: field.getAnnotations()) {
                    append(annotation, annotationClass, annotationAndDepths);
                }
            }); 
        }
        if(!annotationAndDepths.isEmpty()) {
            return AnnotationAndDepth.sorted(annotationAndDepths);
        }

        // search superclasses
        final Class<?> superclass = method.getDeclaringClass().getSuperclass();
        if (superclass != null) {
            final Method parentClassMethod = 
                    firstDeclaredMethod_matching(method, superclass, isSuperMethodFor(method)); 

            if(parentClassMethod!=null) {
                final List<T> annotationsFromSuperclass = 
                        getAnnotations(parentClassMethod, annotationClass);
                if(!annotationsFromSuperclass.isEmpty()) {
                    return annotationsFromSuperclass;
                }
            }
        }

        // search implemented interfaces
        final Class<?>[] interfaces = method.getDeclaringClass().getInterfaces();
        for (final Class<?> iface : interfaces) {
            final Method ifaceMethod = 
                    firstDeclaredMethod_matching(method, iface, isSuperMethodFor(method));

            if(ifaceMethod!=null) {
                final List<T> annotationsFromInterfaces = getAnnotations(ifaceMethod, annotationClass);
                if(!annotationsFromInterfaces.isEmpty()) {
                    return annotationsFromInterfaces;
                }
            }
        }

        return Collections.emptyList();
    }


//XXX optimization?
//    private static List<Class<?>> fieldAnnotationClasses = 
//            _Lists.of(
//                    Property.class,
//                    PropertyLayout.class,
//                    Collection.class,
//                    CollectionLayout.class,
//                    Programmatic.class,
//                    MemberOrder.class,
//                    Pattern.class,
//                    javax.annotation.Nullable.class,
//                    Title.class,
//                    XmlJavaTypeAdapter.class,
//                    XmlTransient.class,
//                    javax.jdo.annotations.Column.class
//                    );

    private static boolean shouldSearchForField(final Class<?> annotationClass) {
        return true; //fieldAnnotationClasses.contains(annotationClass);
    }

    /**
     * Searches for parameter annotations on provided method, and if not found
     * for any inherited methods up from the superclass.
     *
     * <p>
     * Added to allow bytecode-mangling libraries such as CGLIB to be supported.
     * @deprecated use {@link _Annotations} instead
     */
    private static <T extends Annotation> List<T> getAnnotations(
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

    // -- HELPER

    private static Method firstDeclaredMethod_matching(
            Method method,
            Class<?> type, 
            Predicate<Method> filter) {

        return stream(type.getDeclaredMethods())
                .filter(filter)
                .findFirst()
                .orElse(null);
    }


    private static void declaredFields_matching(
            Class<?> type, 
            Predicate<Field> filter, 
            Consumer<Field> onField) {

        stream(type.getDeclaredFields())
        .filter(filter)
        .forEach(onField);

    }

    // -- HELPER - PREDICATES

    private static Predicate<Method> isSuperMethodFor(final Method method) {
        return m->_Reflect.same(method, m);
    }

    private static Predicate<Field> isFieldForGetter(final Method getter) {
        return field->{
            int beginIndex;
            final String methodName = getter.getName();
            if (methodName.startsWith("get")) {
                beginIndex = 3;
            } else if (methodName.startsWith("is")) {
                beginIndex = 2;
            } else {
                return false;
            }
            if(methodName.length()==beginIndex) {
                return false;
            }
            final String suffix = methodName.substring(beginIndex);
            final char c = suffix.charAt(0);
            final char lower = Character.toLowerCase(c);
            final String candidate = "" + lower + suffix.substring(1);
            if(field.getName().equals(candidate)) {
                return true;
            }
            if(field.getName().equals("_" + candidate)) {
                return true;
            }
            return false;
        };
    }

}
