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
package org.apache.isis.applib.util;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassAnnotationMatchProcessor;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.SubclassMatchProcessor;

public class ScanUtils {

    private ScanUtils(){}

    public static <T extends Annotation> Set<Class<?>> scanForAnnotation(final List<String> packagePrefixList, final Class<T> annotationClass) {
        final Set<Class<?>> classes = Sets.newLinkedHashSet();
        new FastClasspathScanner(packagePrefixList.toArray(new String[]{}))
                .matchClassesWithAnnotation(annotationClass,
                        new ClassAnnotationMatchProcessor() {
                            @Override
                            public void processMatch(final Class<?> matchingClass) {
                                classes.add(matchingClass);
                            }
                        })
                .scan();
        return classes;
    }
    public static <T> Set<Class<? extends T>> scanForSubclassesOf(final List<String> packagePrefixList, final Class<T> annotationClass) {
        final Set<Class<? extends T>> classes = Sets.newLinkedHashSet();
        new FastClasspathScanner(packagePrefixList.toArray(new String[]{}))
                .matchSubclassesOf(annotationClass,
                        new SubclassMatchProcessor<T>() {
                            @Override
                            public void processMatch(final Class<? extends T> matchingClass) {
                                classes.add(matchingClass);
                            }
                        })
                .scan();
        return classes;
    }
}
