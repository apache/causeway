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
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.util.ReflectionUtils;

import org.apache.isis.commons.internal.base._Strings;

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
     * 
     * @param <A>
     * @param annotatedElement
     * @param annotationType
     * @return non-null
     */
    public static <A extends Annotation> boolean isPresent(
            AnnotatedElement annotatedElement, 
            Class<A> annotationType) {

        return collect(annotatedElement).isPresent(annotationType);
    }
    
    /**
     * Optionally returns the 'nearest' annotation of given type based on presence.
     * 
     * @param <A>
     * @param annotatedElement
     * @param annotationType
     * @return non-null
     */
    public static <A extends Annotation> Optional<A> findNearestAnnotation(
            AnnotatedElement annotatedElement, 
            Class<A> annotationType) {
        //XXX if synthesize has good runtime performance, then we simply us it here
        return synthesize(annotatedElement, annotationType);
    }
    
    //final static _Probe probe1 = _Probe.unlimited().label("synthesizeInherited");
    //final static _Probe probe2 = _Probe.unlimited().label("synthesizeInherited");
    private final static _Annotations_SyntCache syntCache = new _Annotations_SyntCache();
    public static void clearCache() {
        syntCache.clear();
    }
    
    /**
     * Optionally create a type-safe synthesized version of this annotation based on presence.
     * <p>
     * Does support attribute inheritance.
     * 
     * @param <A>
     * @param annotatedElement
     * @param annotationType
     * @return non-null
     */
    public static <A extends Annotation> Optional<A> synthesizeInherited(
            AnnotatedElement annotatedElement, 
            Class<A> annotationType) {
        
        //probe1.println("lookup");
        
        //return _AnnotationsLegacy.nearest(annotatedElement, annotationType);
        
        return calc_synthesizeInherited(annotatedElement, annotationType);
               
        
//        return syntCache.computeIfAbsent(
//                annotatedElement, 
//                annotationType, 
//                _Annotations::calc_synthesizeInherited);
    }
    
    private static <A extends Annotation> Optional<A> calc_synthesizeInherited(
            AnnotatedElement annotatedElement, 
            Class<A> annotationType) {
        
        //probe2.println("cache-miss");
        
        val collected = _Annotations
                .collect(annotatedElement);
        
        if(!collected.isPresent(annotationType)) {
            
            // also handle fields, getter methods might be associated with
            if(annotatedElement instanceof Method && 
                    searchAnnotationOnField(annotationType) ) {
                
                val fieldForGetter = fieldForGetter((Method) annotatedElement);
                if(fieldForGetter!=null) {
                    return synthesizeInherited(fieldForGetter, annotationType);
                }
            }
            
            return Optional.empty();
        }
        
        val proxy = _Annotations_SynthesizedMergedAnnotationInvocationHandler
                .createProxy(collected, annotationType);
        
        return Optional.of(proxy);
    }
    
    
    /**
     * Optionally create a type-safe synthesized version of this annotation based on presence.
     * <p>
     * Does NOT support attribute inheritance.
     * 
     * @param <A>
     * @param annotatedElement
     * @param annotationType
     * @return non-null
     */
    public static <A extends Annotation> Optional<A> synthesize(
            AnnotatedElement annotatedElement, 
            Class<A> annotationType) {
        
        val synthesized = _Annotations
                .collect(annotatedElement)
                .get(annotationType)
                .synthesize(MergedAnnotation::isPresent);
        
        return synthesized;
    }

    
    // -- HELPER
    
    /**
     * @apiNote don't expose Spring's MergedAnnotations
     */
    static MergedAnnotations collect(AnnotatedElement annotatedElement) {
        
        val collected = MergedAnnotations.from(annotatedElement, SearchStrategy.INHERITED_ANNOTATIONS);
        
//        val collected = syntCache.computeIfAbsent(annotatedElement, __->
//            MergedAnnotations.from(annotatedElement, SearchStrategy.INHERITED_ANNOTATIONS));

        return collected;
    }
    
    private static Field fieldForGetter(Method getter) {
        if(ReflectionUtils.isObjectMethod(getter)) {
            return null;
        }
        val fieldNameCandidate = fieldNameForGetter(getter);
        if(fieldNameCandidate==null) {
            return null;
        }
        val declaringClass = getter.getDeclaringClass();
        return ReflectionUtils.findField(declaringClass, fieldNameCandidate);
    }
    
    private static String fieldNameForGetter(Method getter) {
        if(getter.getParameterCount()>0) {
            return null;
        }
        if(getter.getReturnType()==void.class) {
            return null;
        }
        val methodName = getter.getName();
        String fieldName = null;
        if(methodName.startsWith("is") &&  methodName.length() > 2) {
            fieldName = methodName.substring(2);
        } else if(methodName.startsWith("get") &&  methodName.length() > 3) {
            fieldName = methodName.substring(3);
        } else {
            return null;
        }
        return _Strings.decapitalize(fieldName);
    }

    
    private static boolean searchAnnotationOnField(Class<? extends Annotation> annotationType) {
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
    
}
