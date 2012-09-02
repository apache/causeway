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
import java.util.List;

import org.apache.isis.core.metamodel.facetapi.FeatureType;

public abstract class AnnotationBasedFacetFactoryAbstract extends FacetFactoryAbstract {

    public AnnotationBasedFacetFactoryAbstract(final List<FeatureType> featureTypes) {
        super(featureTypes);
    }

    /**
     * Always returns <tt>false</tt>; {@link FacetFactory}s that look for
     * annotations won't recognize methods with prefixes.
     */
    public boolean recognizes(final Method method) {
        return false;
    }

    protected boolean isString(final Class<?> cls) {
        return Annotations.isString(cls);
    }

    protected static <T extends Annotation> T getAnnotation(final Class<?> cls, final Class<T> annotationClass) {
        return Annotations.getAnnotation(cls, annotationClass);
    }

    protected static <T extends Annotation> T getAnnotation(final Method method, final Class<T> annotationClass) {
        return Annotations.getAnnotation(method, annotationClass);
    }

    protected static boolean isAnnotationPresent(final Method method, final Class<? extends Annotation> annotationClass) {
        return Annotations.isAnnotationPresent(method, annotationClass);
    }

    protected static Annotation[][] getParameterAnnotations(final Method method) {
        return Annotations.getParameterAnnotations(method);
    }

}
