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
package org.apache.isis.applib.services.classdiscovery;

import java.util.Set;

import com.google.common.collect.Sets;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.SubclassMatchProcessor;

/**
 * This utility service supports the dynamic discovery of classes from the classpath.  One service that uses this
 * is the <tt>FixtureScripts</tt> domain service.
 *
 */
@DomainService(
        nature = NatureOfService.DOMAIN
)
public class ClassDiscoveryServiceUsingFastClasspathScanner
            extends AbstractService
            implements ClassDiscoveryService2 {


    @Programmatic
    @Override
    public <T> Set<Class<? extends T>> findSubTypesOfClasses(Class<T> type) {
        throw new IllegalStateException("Shouldn't be called as this class implements ClassDiscoveryService2");
    }

    @Programmatic
    @Override
    public <T> Set<Class<? extends T>> findSubTypesOfClasses(Class<T> type, String packagePrefix) {
        final Set<Class<? extends T>> classes = Sets.newLinkedHashSet();
        new FastClasspathScanner(packagePrefix)
                .matchSubclassesOf(type, new SubclassMatchProcessor<T>() {
                    @Override
                    public void processMatch(final Class<? extends T> aClass) {
                        classes.add(aClass);
                    }
                })
                .scan();
        return classes;
    }


}