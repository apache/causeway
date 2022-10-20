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
package org.apache.causeway.core.privileged;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * In the context of the Java platform module system (JPMS),
 * allows for reflective access to all classes on the class-path (not module-path).
 * <p>
 * Requires the module - this class is contained in - to be installed as
 * an automatic module.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0
 */
@UtilityClass
public final class _Privileged {

    // -- METHOD/FIELD HANDLES

    public MethodHandle handleOf(final Method method) throws IllegalAccessException {
        return MethodHandles.lookup().unreflect(method);
    }

    public MethodHandle handleOfGetterOn(final Field field) throws IllegalAccessException {
        return MethodHandles.lookup().unreflectGetter(field);
    }

    // -- CONSTRUCTION

    @SneakyThrows
    public <T> T newInstance(final @NonNull Constructor<T> constructor, final Object ... initargs) {
        return constructor.newInstance(initargs);
    }

    // -- METHOD INVOCATION

    @SneakyThrows
    public Object invoke(final @NonNull MethodHandle mh, final Object ... args) {
        return mh.invoke(args);
    }

    @SneakyThrows
    public Object invokeLookup(final @NonNull MethodHandle mh, final Class<?> targetClass) {
        return mh.invoke(targetClass, MethodHandles.lookup());
    }

    public MethodHandle createPrivateLookupMethodHandle() {
        try {
            final Method privateLookupIn = java.lang.invoke.MethodHandles.class
                    .getMethod("privateLookupIn",
                        Class.class,
                        java.lang.invoke.MethodHandles.Lookup.class);
            return MethodHandles.publicLookup().unreflect(privateLookupIn);
        } catch (Exception e) {
            throw new IllegalStateException("MethodHandles.privateLookupIn(...) is not available", e);
        }
    }

    // -- PRIVILEGED CALLS

    @Deprecated // does not work
    @SneakyThrows
    public <T> T call(final @NonNull Callable<T> callable) {
        return callable.call();
    }

}
