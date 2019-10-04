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
     * Optionally returns the 'nearest' annotation of given type based on presence.
     * 
     * @param <A>
     * @param annotatedElement
     * @param annotationType
     * @return non-null
     */
    public static <A extends Annotation> Optional<A> findNearestAnnotation(
            Class<?> annotatedElement, 
            Class<A> annotationType) {
        //XXX if synthesize has good runtime performance, then we simply us it here
        return synthesize(annotatedElement, annotationType);
    }
    
    /**
     * Optionally create a type-safe synthesized version of this annotation based on presence.
     * <p>
     * Does support attribute inheritance.
     * 
     * @param <A>
     * @param annotatedElement
     * @param annotationType
     * @param attributeAcceptStrategy
     * @return non-null
     */
    public static <A extends Annotation> Optional<A> synthesizeInherited(
            AnnotatedElement annotatedElement, 
            Class<A> annotationType) {
        
        val collected = _Annotations
                .collect(annotatedElement);
        
        if(!collected.isPresent(annotationType)) {
            
            // also handle fields, getter methods might be associated with
            if(annotatedElement instanceof Method) {
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
            Class<?> annotatedElement, 
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
    private static MergedAnnotations collect(AnnotatedElement annotatedElement) {
        
        //TODO use cache if not already
        val collected = MergedAnnotations.from(annotatedElement, SearchStrategy.INHERITED_ANNOTATIONS);
        return collected;
    }
    
    private static Field fieldForGetter(Method getter) {
        if(ReflectionUtils.isObjectMethod(getter)) {
            return null;
        }
        val fieldNameCandidate1 = fieldNameForGetter(getter);
        if(fieldNameCandidate1==null) {
            return null;
        }
        //val fieldNameCandidate2 = "_" + fieldNameCandidate1; //XXX legacy behavior
        
        val declaringClass = getter.getDeclaringClass();
        for(val field : declaringClass.getDeclaredFields()) { //TODO use cache if appropriate ... ReflectionUtils.findField(clazz, name)
            val fieldName = field.getName(); 
            if(fieldName.equals(fieldNameCandidate1)) {
                return field;
            }
//            if(fieldName.equals(fieldNameCandidate2)) { //XXX legacy behavior
//                return field;
//            }
        }
        return null;
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
    
}
