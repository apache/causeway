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
package org.apache.isis.core.metamodel.commons;

import java.util.function.Function;

import javax.annotation.Nullable;

public final class ClassFunctions {

    private ClassFunctions(){}

    public static <T> Function<Object, T> castTo(final Class<T> type) {
        return new Function<Object, T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T apply(final Object input) {
                return (T) input;
            }
        };
    }

    public static Function<Class<?>, String> packageNameOf() {
        return new Function<Class<?>, String>() {
            @Nullable @Override public String apply(final Class<?> input) {
                return input.getPackage().getName();
            }
        };
    }
}
