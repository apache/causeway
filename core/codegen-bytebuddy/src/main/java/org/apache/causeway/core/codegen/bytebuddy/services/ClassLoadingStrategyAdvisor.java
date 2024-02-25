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
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.SneakyThrows;

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
        this.privateLookupMethodHandle = createPrivateLookupMethodHandle();
    }

    public ClassLoadingStrategy<ClassLoader> getSuitableStrategy(final Class<?> targetClass) {
        try {
            final Object privateLookup = invokeLookup(privateLookupMethodHandle, targetClass);
            return ClassLoadingStrategy.UsingLookup.of(privateLookup);
        } catch (Throwable e) {
            throw _Exceptions.illegalState(e,
                    "Failed to utilize code generation strategy on class '%s'",
                    targetClass.getName());
        }
    }

    // -- HELPER

    @SneakyThrows
    private static Object invokeLookup(final @NonNull MethodHandle mh, final Class<?> targetClass) {
        return mh.invoke(reads(targetClass), MethodHandles.lookup());
    }

    private static MethodHandle createPrivateLookupMethodHandle() {
        // JDK 9+ required
        if (!ClassInjector.UsingLookup.isAvailable()) {
            throw new IllegalStateException("No code generation strategy available");
        }
        try {
            final Method privateLookupIn = java.lang.invoke.MethodHandles.class
                    .getMethod("privateLookupIn",
                        Class.class,
                        java.lang.invoke.MethodHandles.Lookup.class);
            return MethodHandles.publicLookup().unreflect(privateLookupIn);
        } catch (Throwable e) {
            throw _Exceptions.illegalState(e,
                    "MethodHandles.privateLookupIn(...) is not available");
        }
    }

    private static Class<?> reads(final Class<?> cls) {
        final Class<?> otherClass = cls.isArray() ? cls.getComponentType() : cls;
        final Module otherModule = otherClass.getModule();
        //no need for unnamed and java.base types
        if (!otherModule.isNamed()
                || "java.base".equals(otherModule.getName())) {
            return cls;
        }
        thisModule().addReads(otherModule);
        return cls;
    }

    private static Module thisModule() {
        return ClassLoadingStrategyAdvisor.class.getModule();
    }

}
