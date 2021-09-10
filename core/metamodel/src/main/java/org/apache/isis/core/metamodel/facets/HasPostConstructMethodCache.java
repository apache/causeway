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

import javax.annotation.PostConstruct;

import org.apache.isis.core.metamodel.methods.MethodByClassMap;
import org.apache.isis.core.metamodel.methods.MethodFinder;

import lombok.val;

/**
 * Contract between implementations of RecreatableObjectFacet and their creating facet factories.
 */
public interface HasPostConstructMethodCache {

    MethodByClassMap getPostConstructMethodsCache();

    default Method postConstructMethodFor(final Object pojo) {
        return findAnnotatedMethod(
                // @PostConstruct is allowed to appear on non-public methods
                MethodFinder.notNecessarilyPublic(pojo.getClass(), MethodFinder.ANY_NAME)
                .withRequiredReturnType(void.class),
                PostConstruct.class,
                getPostConstructMethodsCache());
    }

    private static Method findAnnotatedMethod(
            final MethodFinder finder,
            final Class<? extends Annotation> annotationClass,
            final MethodByClassMap methods) {

        val type = finder.getCorrespondingClass();
        return methods.computeIfAbsent(type, __->finder.streamMethodsIgnoringSignature()
                        .filter(method->method.getAnnotation(annotationClass)!=null)
                        .findFirst()).orElse(null);
    }

}