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
import java.lang.reflect.Method;

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
     * 
     * <p>
     * Added to allow bytecode-mangling libraries such as CGLIB to be supported.
     */
    public static <T extends Annotation> T getAnnotation(final Method method, final Class<T> annotationClass) {
        if (method == null) {
            return null;
        }
        final T annotation = method.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }

        final Class<?> methodDeclaringClass = method.getDeclaringClass();

        // search superclasses
        final Class<?> superclass = methodDeclaringClass.getSuperclass();
        if (superclass != null) {
            try {
                final Method parentClassMethod = superclass.getMethod(method.getName(), method.getParameterTypes());
                return getAnnotation(parentClassMethod, annotationClass);
            } catch (final SecurityException e) {
                // fall through
            } catch (final NoSuchMethodException e) {
                // fall through
            }
        }

        // search implemented interfaces
        final Class<?>[] interfaces = methodDeclaringClass.getInterfaces();
        for (final Class<?> iface : interfaces) {
            try {
                final Method ifaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                return getAnnotation(ifaceMethod, annotationClass);
            } catch (final SecurityException e) {
                // fall through
            } catch (final NoSuchMethodException e) {
                // fall through
            }
        }
        return null;
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
            } catch (final SecurityException e) {
                // fall through
            } catch (final NoSuchMethodException e) {
                // fall through
            }
        }

        // search implemented interfaces
        final Class<?>[] interfaces = methodDeclaringClass.getInterfaces();
        for (final Class<?> iface : interfaces) {
            try {
                final Method ifaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                return isAnnotationPresent(ifaceMethod, annotationClass);
            } catch (final SecurityException e) {
                // fall through
            } catch (final NoSuchMethodException e) {
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
            } catch (final SecurityException e) {
                // fall through
            } catch (final NoSuchMethodException e) {
                // fall through
            }
        }

        // search implemented interfaces
        final Class<?>[] interfaces = methodDeclaringClass.getInterfaces();
        for (final Class<?> iface : interfaces) {
            try {
                final Method ifaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                return getParameterAnnotations(ifaceMethod);
            } catch (final SecurityException e) {
                // fall through
            } catch (final NoSuchMethodException e) {
                // fall through
            }
        }

        return noParamAnnotationsFor(method);
    }

    private static Annotation[][] noParamAnnotationsFor(final Method method) {
        return new Annotation[method.getParameterTypes().length][0];
    }

}
