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
import java.util.Optional;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;

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
     * Optionally create a type-safe synthesized version of this annotation based on presence.
     * <p>
     * Does not support attribute inheritance.
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
    
    // -- ATTRIBUTE FETCHERS
    
    /**
     * Optionally create a String from attribute values based on presence.
     * @param <A>
     * @param attributeName
     * @param annotatedElement
     * @param annotationType
     * @param attributeAcceptStrategy
     * @return non-null
     */
    public static <A extends Annotation> Optional<String> getString(
            String attributeName,
            Class<?> annotatedElement, 
            Class<A> annotationType,
            AttributeAcceptStrategy attributeAcceptStrategy) {
        
        val value = _Annotations
                .collect(annotatedElement)
                .stream(annotationType)
                .map(ma->ma.getString(attributeName))
                .filter(attributeAcceptStrategy::acceptString)
                .findFirst();
        
        return value;
    }

    /**
     * Optionally create an Enum from attribute values based on presence.
     * @param <A>
     * @param <E>
     * @param attributeName
     * @param annotatedElement
     * @param annotationType
     * @param enumType
     * @param attributeAcceptStrategy
     * @return non-null
     */
    public static <A extends Annotation, E extends Enum<E>> Optional<E> getEnum(
            String attributeName,
            Class<?> annotatedElement, 
            Class<A> annotationType,
            Class<E> enumType, 
            AttributeAcceptStrategy attributeAcceptStrategy) {
        
        val value = _Annotations
                .collect(annotatedElement)
                .stream(annotationType)
                .map(ma->ma.getEnum(attributeName, enumType))
                .filter(attributeAcceptStrategy::acceptEnum)
                .findFirst();
        
        return value;
    }
    
    // -- SHORTCUTS
    
    public static <A extends Annotation> Optional<String> getString(
            String attributeName,
            Class<?> annotatedElement, 
            Class<A> annotationType) {
        return getString(attributeName, annotatedElement, annotationType, DEFAULT_ATTRIBUTE_ACCEPT_STRATEGY);
    }
    
    public static <A extends Annotation, E extends Enum<E>> Optional<E> getEnum(
            String attributeName,
            Class<?> annotatedElement, 
            Class<A> annotationType,
            Class<E> enumType) {
        return getEnum(attributeName, annotatedElement, annotationType, enumType, DEFAULT_ATTRIBUTE_ACCEPT_STRATEGY);
    }   

    // -- BEHAVIOR
    
    public final static AttributeAcceptStrategy DEFAULT_ATTRIBUTE_ACCEPT_STRATEGY = 
            new AttributeAcceptStrategy() {};
    
    static interface AttributeAcceptStrategy {
        
        default boolean acceptString(String value) {
            return !_Strings.isNullOrEmpty(value);
        }
        
        default boolean acceptEnum(Enum<?> value) {
            return value != null && !value.name().equals("NOT_SPECIFIED"); 
        }
        
    }
    
    // -- HELPER
    
    /**
     * @apiNote don't expose Spring's MergedAnnotation
     */
    private static MergedAnnotations collect(Class<?> annotatedElement) {
        
        //TODO use cache
        val collected = MergedAnnotations.from(annotatedElement, SearchStrategy.INHERITED_ANNOTATIONS);
        return collected;
    }
    
    
    
}
