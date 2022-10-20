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
package org.apache.causeway.core.codegen.bytebuddy.services;

import java.lang.invoke.MethodHandle;

import org.apache.causeway.core.privileged._Privileged;

import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

/**
 * package private utility to advise on the ClassLoadingStrategy, dependent on the JVM version we are running on
 *
 * see <a href="https://mydailyjava.blogspot.com/2018/04/jdk-11-and-proxies-in-world-past.html">byte-buddy blog</a>
 */
class ClassLoadingStrategyAdvisor {

    final MethodHandle privateLookupMethodHandle;

    ClassLoadingStrategyAdvisor() {
        // JDK 9+ required
        if (!ClassInjector.UsingLookup.isAvailable()) {
            throw new IllegalStateException("No code generation strategy available");
        }
        this.privateLookupMethodHandle = _Privileged.createPrivateLookupMethodHandle();
    }

    public ClassLoadingStrategy<ClassLoader> getSuitableStrategy(final Class<?> targetClass) {
        try {
            final Object privateLookup = _Privileged
                    .invokeLookup(privateLookupMethodHandle, targetClass);
            return ClassLoadingStrategy.UsingLookup.of(privateLookup);
        } catch (Throwable e) {
            throw new IllegalStateException(
                    String.format("Failed to utilize code generation strategy on class '%s'",
                            targetClass.getName())
                    , e);
        }
    }

}
